/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.perfo;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import static fr.cnes.doi.AbstractSpec.classTitle;
import static fr.cnes.doi.AbstractSpec.testTitle;
import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.MdsSpec;
import static fr.cnes.doi.client.BaseClient.DATACITE_MOCKSERVER_PORT;
import fr.cnes.doi.client.ClientProxyTest;
import fr.cnes.doi.resource.mds.DoisResource;
import fr.cnes.doi.security.UtilsHeader;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static fr.cnes.doi.server.DoiServer.JKS_DIRECTORY;
import static fr.cnes.doi.server.DoiServer.JKS_FILE;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import fr.cnes.doi.utils.spec.CoverageAnnotation;
import fr.cnes.doi.utils.spec.Requirement;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ITperformance {

    private static final Logger LOG = Logger.getLogger(ITperformance.class.getName());
    private final ExecutorService clientExec = Executors.newFixedThreadPool(100);

    private static Client cl;
    private MdsSpec mdsServerStub;

    private static final String METADATA_SERVICE = "/mds/metadata";
    private static final String DOIS_SERVICE = "/mds/dois";
    private static final int NB_ITERS = 50;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    public ITperformance() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
        cl = new Client(new Context(), Protocol.HTTPS);
        Series<Parameter> parameters = cl.getContext().getParameters();
        parameters.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));
        parameters.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));
        parameters.add("truststorePath", DoiSettings.getInstance().getString(Consts.SERVER_HTTPS_TRUST_STORE_PATH));
        parameters.add("truststorePassword", DoiSettings.getInstance().getSecret(Consts.SERVER_HTTPS_TRUST_STORE_PASSWD));
        parameters.add("truststoreType", "JKS");
        classTitle("ITperformance");
    }

    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
        this.mdsServerStub = new MdsSpec(DATACITE_MOCKSERVER_PORT);
    }

    @After
    public void tearDown() {
        this.mdsServerStub.finish();
    }

    @Test
    public void testWadlPerformance() {
        testTitle("testWadlPerformance"); 
        long startTime = System.currentTimeMillis();
        ConcurrentHashMap map = new ConcurrentHashMap();
        map.put("nbErrors", 0);
        int nbIter = 0;
        try {
            for (int i = 0; i < NB_ITERS; i++) {
                nbIter = i + 1;
                clientExec.execute(new GetMetadata(i, map));
            }
        } catch (RuntimeException ex) {
        } 
        
        clientExec.shutdown();
        
        try {
            clientExec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
  
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        double meanProcessingTime = elapsedTime / NB_ITERS;
        
        Assert.assertTrue("Test the performances", map.get("nbErrors").equals(0) && nbIter == NB_ITERS);
        LOG.log(Level.INFO, "All working fine : Mean request processing time {0} ms", meanProcessingTime);        
    }

    private static class GetMetadata implements Runnable {

        private final int i;
        private final ConcurrentHashMap map;

        public GetMetadata(int i, ConcurrentHashMap map) {
            this.i = i;
            this.map = map;
        }

        @Override
        public void run() {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
            ClientResource client = new ClientResource(new Context(), "http://localhost:" + port + "/mds?method=options");
            final Series<Parameter> params = client.getContext().getParameters();
            params.set(RESTLET_MAX_TOTAL_CONNECTIONS, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS));
            params.set(RESTLET_MAX_CONNECTIONS_PER_HOST, DoiSettings.getInstance().getString(fr.cnes.doi.settings.Consts.RESTLET_MAX_CONNECTIONS_PER_HOST, DEFAULT_MAX_CONNECTIONS_PER_HOST));

            try {
                Representation rep = client.get();
                String text = rep.getText();
                LOG.log(Level.INFO, "OK for the {0} tests", new Object[]{this.i});
            } catch (RuntimeException ex) {
                int nbErrors = ((int) this.map.get("nbErrors")) + 1;
                this.map.put("nbErrors", nbErrors);
                LOG.log(Level.INFO, "Error after {0} tests :{1}", new Object[]{this.i, ex.getMessage()});
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Error after {0} tests :{1}", new Object[]{this.i, ex.getMessage()});                
            } finally {
                client.release();
            }
        }
    }
    
}
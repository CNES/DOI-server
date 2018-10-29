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
package fr.cnes.doi.integration;

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
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_CONNECTIONS_PER_HOST;
import static fr.cnes.doi.server.DoiServer.RESTLET_MAX_TOTAL_CONNECTIONS;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.io.BufferedReader;
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
import org.junit.experimental.categories.Category;
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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Category(IntegrationTest.class)
public class ITperformance {

    private static final Logger LOG = Logger.getLogger(ITperformance.class.getName());
    private final ExecutorService clientExec = Executors.newFixedThreadPool(200);

    private static Client cl;
    private MdsSpec mdsServerStub;
    private InputStream inputStream;
    private String metadata;

    private static final String METADATA_SERVICE = "/mds/metadata";
    private static final String DOIS_SERVICE = "/mds/dois";
    private static final int NB_ITERS = 101;

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
        this.inputStream = ClientProxyTest.class.getResourceAsStream("/test.xml");
        this.metadata = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        try {
            this.inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ITperformance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
        this.mdsServerStub.finish();
    }

    @Test
    public void testCreateDOIs() {
        testTitle("testCreateDOIs");
        long startTime = System.currentTimeMillis();
        ConcurrentHashMap map = new ConcurrentHashMap();
        map.put("nbErrors", 0);
        this.mdsServerStub.createSpec(MdsSpec.Spec.POST_METADATA_201);
        this.mdsServerStub.createSpec(MdsSpec.Spec.POST_DOI_201);
        
        
        testMultiThreads(CreateDOI.class, map, NB_ITERS);        
        clientExec.shutdown();

        try {
            clientExec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        double meanProcessingTime = elapsedTime / NB_ITERS;

        double expectedTime = 1 * 1000; //1 s per DOI
        System.out.println(map.get("nbErrors") + "  " + meanProcessingTime);
        Assert.assertTrue("Test the performances of DOIs creation", (int) map.get("nbErrors") == 0 && meanProcessingTime <= expectedTime);
        LOG.log(Level.INFO, "All working fine : Mean request processing time {0} ms", meanProcessingTime);
    }
    
    @Test
    public void testCreateDOI() {
        testTitle("testCreateDOI");
        long startTime = System.currentTimeMillis();
        ConcurrentHashMap map = new ConcurrentHashMap();
        map.put("nbErrors", 0);
        this.mdsServerStub.createSpec(MdsSpec.Spec.POST_METADATA_201);
        this.mdsServerStub.createSpec(MdsSpec.Spec.POST_DOI_201);
        
        
        testMultiThreads(CreateDOI.class, map, 1);        
        clientExec.shutdown();

        try {
            clientExec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        double meanProcessingTime = elapsedTime;

        double expectedTime = 1 * 1000; //1 s per DOI
        System.out.println(map.get("nbErrors") + "  " + meanProcessingTime);
        Assert.assertTrue("Test the performances of DOIs creation", (int) map.get("nbErrors") == 0 && meanProcessingTime <= expectedTime);
        LOG.log(Level.INFO, "All working fine : Mean request processing time {0} ms", meanProcessingTime);
        this.mdsServerStub.verifySpec(MdsSpec.Spec.POST_METADATA_201);
        this.mdsServerStub.verifySpec(MdsSpec.Spec.POST_DOI_201);
    }    

    private void testMultiThreads(final Class jobTask, final ConcurrentHashMap map, int nbIters) {
        try {
            for (int i = 0; i < nbIters; i++) {
                System.out.println("i=" + i);
                try {
                    JobTask task = (JobTask) jobTask.newInstance();
                    task.setParameters(map, this.metadata);
                    clientExec.execute((Runnable) task);
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    int nbErrors = (int) map.get("nbErrors");
                    map.put("nbErrors", nbErrors + 1);
                    break;
                }
            }
        } catch (RuntimeException ex) {
            int nbErrors = (int) map.get("nbErrors");
            map.put("nbErrors", nbErrors + 1); 
            LOG.log(Level.SEVERE, null, ex);
        }

    }

    private static class CreateDOI implements Runnable, JobTask {

        private ConcurrentHashMap map;
        private String doiMetadata;

        public CreateDOI() {
        }

        @Override
        public void run() {
            
            Form doiForm = new Form();
            doiForm.add(new Parameter(DoisResource.DOI_PARAMETER, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"));
            doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));

            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
            ClientResource client = new ClientResource("http://localhost:" + port + DOIS_SERVICE);
            client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
            final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

            Map<String, Object> reqAttribs = client.getRequestAttributes();
            Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
            if (headers == null) {
                headers = new Series<>(Header.class);
                reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
            }
            headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
            int code;
            try {
                Representation rep = client.post(doiForm);
                code = client.getStatus().getCode();
            } catch (ResourceException ex) {
                code = ex.getStatus().getCode();
                int nbErrors = (int) this.map.get("nbErrors");
                this.map.put("nbErrors", nbErrors + 1);
            } finally {
                client.release();
            }               

            client = new ClientResource("http://localhost:" + port + METADATA_SERVICE);
            //client.setNext(cl);
            client.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd"));
            reqAttribs = client.getRequestAttributes();
            headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
            if (headers == null) {
                headers = new Series<>(Header.class);
                reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
            }
            headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");

            try {
                Representation rep = client.post(new StringRepresentation(this.doiMetadata, MediaType.APPLICATION_XML));
                code = client.getStatus().getCode();
            } catch (ResourceException ex) {
                code = ex.getStatus().getCode();
                int nbErrors = (int) this.map.get("nbErrors");
                this.map.put("nbErrors", nbErrors + 1);
            } finally {
                client.release();
            }

        }

        @Override
        public void setParameters(final ConcurrentHashMap map, final Object... parameters) {
            this.map = map;
            this.doiMetadata = String.valueOf(parameters[0]);
        }
    }

    public interface JobTask {

        public void setParameters(final ConcurrentHashMap map, final Object... parameters);
    }

}

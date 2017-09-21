/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.mds;

import fr.cnes.doi.InitServerForTest;
import fr.cnes.doi.settings.Consts;
import fr.cnes.doi.settings.DoiSettings;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.datacite.schema.kernel_4.Resource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class PerformanceTest {

    public static final String DOI = "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b";

    private static final Logger LOG = Logger.getLogger(PerformanceTest.class.getName());
    private ExecutorService clientExec = Executors.newFixedThreadPool(100);

    public PerformanceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InitServerForTest.init();
    }

    @AfterClass
    public static void tearDownClass() {
        InitServerForTest.close();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        LOG.info("Start testing");
        for (int i = 0; i < 100; i++) {
            clientExec.execute(new GetMetadata(i));
        }
        Assert.assertTrue(true);
        LOG.info("All working fine");
        clientExec.shutdown();

    }

    private static class GetMetadata implements Runnable {

        private int i;

        public GetMetadata(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
            ClientResource client = new ClientResource("http://localhost:" + port + "/mds/metadata/" + DOI);            
            try {
                Resource resource = client.get(Resource.class);
                LOG.log(Level.INFO, "OK for the {0} tests", new Object[]{this.i});                
            } catch (RuntimeException ex) {
                LOG.log(Level.INFO, "Error after {0} tests :{1}", new Object[]{this.i, ex.getMessage()});
            } finally {
                client.release();
            }
        }
    }
}

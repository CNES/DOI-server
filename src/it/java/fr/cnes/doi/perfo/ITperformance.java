/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.perfo;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
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
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class ITperformance {

    private static final Logger LOG = Logger.getLogger(ITperformance.class.getName());
    private ExecutorService clientExec = Executors.newFixedThreadPool(100);

    public ITperformance() {
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
        LOG.info("TEST : Performance");
        ConcurrentHashMap map = new ConcurrentHashMap();
        map.put("nbErrors", 0);
        int nbIter = 0;
        try {
            for (int i = 0; i < 50; i++) {
                nbIter = i+1;
                clientExec.execute(new GetMetadata(i, map));
            }
        } catch (RuntimeException ex) {

        } finally {
            System.out.println("nbErrors = " + map.get("nbErrors"));
            System.out.println("nbIters = "+nbIter);
        }
        Assert.assertTrue("Test the performances", map.get("nbErrors").equals(0) && nbIter == 50);
        LOG.info("All working fine");
        clientExec.shutdown();
    }

    private static class GetMetadata implements Runnable {

        private int i;
        private ConcurrentHashMap map;

        public GetMetadata(int i, ConcurrentHashMap map) {
            this.i = i;
            this.map = map;
        }

        @Override
        public void run() {
            String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
            ClientResource client = new ClientResource("http://localhost:" + port + "/mds?method=options");
            try {
                Resource resource = client.get(Resource.class);
                System.out.println("ID="+resource.getIdentifier());
                LOG.log(Level.INFO, "OK for the {0} tests", new Object[]{this.i});
            } catch (RuntimeException ex) {
                int nbErrors = ((int) this.map.get("nbErrors")) + 1;
                this.map.put("nbErrors", nbErrors);
                LOG.log(Level.INFO, "Error after {0} tests :{1}", new Object[]{this.i, ex.getMessage()});
            } finally {
                client.release();
            }
        }
    }
}

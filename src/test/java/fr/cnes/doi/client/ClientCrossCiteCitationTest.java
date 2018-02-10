/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package fr.cnes.doi.client;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class ClientCrossCiteCitationTest {
    
    private ClientAndServer mockServer;
    
    public ClientCrossCiteCitationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        mockServer = startClientAndServer(1080);
    }
    
    @After
    public void tearDown() {
        mockServer.stop();
    }
    
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);    

    /**
     * Test of getStyles method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetStyles() throws Exception {        
        System.out.println("getStyles");
        // setting behaviour for test case
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.STYLE_URI).withMethod("GET")).respond(HttpResponse.response().withBody("[\"academy-of-management-review\",\"accident-analysis-and-prevention\",\"acm-sig-proceedings-long-author-list\"]"));                

        // create a GET request client API
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = Arrays.asList("academy-of-management-review","accident-analysis-and-prevention","acm-sig-proceedings-long-author-list");
        List<String> result = instance.getStyles();
        assertEquals(expResult, result);        

        // verify server has received exactly one request
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.STYLE_URI), VerificationTimes.once());        
    }

    /**
     * Test of getLanguages method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetLanguages() throws Exception {
        System.out.println("getLanguages");
        // setting behaviour for test case
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.LOCALE_URI).withMethod("GET")).respond(HttpResponse.response().withBody("[\"af-ZA\",\"ar\",\"bg-BG\",\"ca-AD\",\"cs-CZ\",\"cy-GB\",\"da-DK\",\"de-AT\",\"de-CH\",\"de-DE\",\"el-GR\",\"en-GB\",\"en-US\",\"es-CL\",\"es-ES\",\"es-MX\",\"et-EE\",\"eu\",\"fa-IR\",\"fi-FI\",\"fr-CA\",\"fr-FR\",\"he-IL\",\"hr-HR\",\"hu-HU\",\"id-ID\",\"is-IS\",\"it-IT\",\"ja-JP\",\"km-KH\",\"ko-KR\",\"lt-LT\",\"lv-LV\",\"mn-MN\",\"nb-NO\",\"nl-NL\",\"nn-NO\",\"pl-PL\",\"pt-BR\",\"pt-PT\",\"ro-RO\",\"ru-RU\",\"sk-SK\",\"sl-SI\",\"sr-RS\",\"sv-SE\",\"th-TH\",\"tr-TR\",\"uk-UA\",\"vi-VN\",\"zh-CN\",\"zh-TW\"]"));                
        
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        List<String> expResult = Arrays.asList("af-ZA","ar","bg-BG","ca-AD","cs-CZ","cy-GB","da-DK","de-AT","de-CH","de-DE","el-GR","en-GB","en-US","es-CL","es-ES","es-MX","et-EE","eu","fa-IR","fi-FI","fr-CA","fr-FR","he-IL","hr-HR","hu-HU","id-ID","is-IS","it-IT","ja-JP","km-KH","ko-KR","lt-LT","lv-LV","mn-MN","nb-NO","nl-NL","nn-NO","pl-PL","pt-BR","pt-PT","ro-RO","ru-RU","sk-SK","sl-SI","sr-RS","sv-SE","th-TH","tr-TR","uk-UA","vi-VN","zh-CN","zh-TW");
        List<String> result = instance.getLanguages();
        assertEquals(expResult, result);

        // verify server has received exactly one request
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.LOCALE_URI), VerificationTimes.once());             
    }

    /**
     * Test of getFormat method, of class ClientCrossCiteCitation.
     */
    @Test
    public void testGetFormat() throws Exception {
        System.out.println("getFormat");
        
        // setting behaviour for test case
        mockServer.when(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI).withMethod("GET")).respond(HttpResponse.response().withBody("Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n"));                
        
        String doiName = "10.1145/2783446.2783605";
        String style = "academy-of-management-review";
        String language = "af-ZA";
        ClientCrossCiteCitation instance = new ClientCrossCiteCitation(ClientCrossCiteCitation.Context.DEV);
        String expResult = "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n";
        String result = instance.getFormat(doiName, style, language);
        assertEquals(expResult, result);

        // verify server has received exactly one request
        mockServer.verify(HttpRequest.request(ClientCrossCiteCitation.FORMAT_URI), VerificationTimes.once());          
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.io.IOException;
import java.sql.SQLException;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class TestTOken {

    public static void main(final String[] argv) throws IOException, ClassNotFoundException, SQLException {   
        
        ClientResource client = new ClientResource("http://localhost:8182/suffixProject");
        Form form = new Form();
        form.add("projectName", "MyprojectJC");
        Representation response = client.post(form); 
        String projectID = response.getText();
        client.release();
        
        client = new ClientResource("http://localhost:8182/token");
        form = new Form();
        form.add("identifier", "jcm");
        form.add("projectID", projectID);
        response = client.post(form);
        String token = response.getText();
        System.out.println(token);
        client.release();
        
        
        client = new ClientResource("http://localhost:8182/token/"+token);
        Representation rep = client.get();
        System.out.println("info="+rep.getText());
        client.release();
        

        
        ClientResource clientResource = new ClientResource("http://localhost:8182/mds/metadata");
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH_BEARER);
        //cr.setRawValue(token.getAccessToken());
        cr.setRawValue(token);

        clientResource.setChallengeResponse(cr);

        clientResource.post(null);
    }

}

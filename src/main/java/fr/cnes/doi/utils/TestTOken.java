/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public class TestTOken {

    public static void main(final String[] argv) throws IOException, ClassNotFoundException, SQLException, ParseException {   

        ClientResource client = new ClientResource("http://localhost:8182/admin/suffixProject");
        Form form = new Form();
        form.add("projectName", "Myphhfffcvdscsdfvdffff");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        Representation response = client.post(form); 
        String projectID = response.getText();
        client.release();
        
        client = new ClientResource("http://localhost:8182/admin/token");
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");        
        form = new Form();
        form.add("identifier", "jcm");
        form.add("projectID", projectID);
        response = client.post(form);
        String token = response.getText();
        System.out.println(token);
        client.release();
        
        
        client = new ClientResource("http://localhost:8182/admin/token/"+token);
        client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");        
        Representation rep = client.get();
        System.out.println("info="+rep.getText());
        client.release();
        

        
        ClientResource clientResource = new ClientResource("http://localhost:8182/mds/metadata");
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue(token);
        //cr.setRawValue("asdsqqscsqcqdcqscqc");

        clientResource.setChallengeResponse(cr);

        clientResource.post(null);
    }

}

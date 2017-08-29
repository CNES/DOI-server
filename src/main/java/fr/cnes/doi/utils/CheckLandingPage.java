/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.utils;

import fr.cnes.doi.client.ClientLandingPage;
import fr.cnes.doi.client.ClientSearchDataCite;
import fr.cnes.doi.settings.EmailSettings;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class CheckLandingPage implements Runnable {

    public CheckLandingPage(final String publisher) {
        super();
    }

    public CheckLandingPage() {
        super();
    }

    @Override
    public void run() {
        EmailSettings email = EmailSettings.getInstance();
        String subject;        
        String msg;
        try {

            ClientSearchDataCite client = new ClientSearchDataCite();
            List<String> response = client.getDois();
            ClientLandingPage clientLandingPage = new ClientLandingPage(response);
            
            if(clientLandingPage.isSuccess()) {                           
                subject = "Landing pages checked with success";
                msg = "All landing pages ("+response.size()+") are on-line";
            } else {
                subject = "Landing pages checked with errors";
                List<String> errors = clientLandingPage.getErrors();
                msg = errors.size()+" are off-line !!!\n";
                msg+="List of off-line landing pages:\n";
                msg+="-------------------------------\n";
                for(String error:errors) {
                    msg+="- "+error+"\n";
                }
            }
            email.sendMessage(subject, msg);     
        } catch (Exception ex) {
            email.sendMessage("Unrecoverable errors when checking landing pages", ex.toString());     
            Logger.getLogger(CheckLandingPage.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Hello, that's me");
    }

}

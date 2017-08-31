/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//TODO
// Créer singleton pour DB token
// Extraire la bd de UniqueFileName et le mettre dans un package DB avec DB token
// Vérifier les authentification et authorisations
package fr.cnes.doi.application;

import fr.cnes.doi.settings.ProxySettings;
import fr.cnes.doi.utils.Requirement;
import org.restlet.ext.wadl.WadlApplication;


/**
 * Creates a base application by retrieving the proxy settings.
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Requirement(
        reqId = "DOI_DOC_010",
        reqName = "Documentation des interfaces"
)
public class BaseApplication extends WadlApplication {
    
    /**
     * Proxy settings.
     */
    protected ProxySettings proxySettings;
    
    /**
     * This constructor creates an instance of proxySettings.
     */
    public BaseApplication() {                
        this.proxySettings = ProxySettings.getInstance();
    }
    
}

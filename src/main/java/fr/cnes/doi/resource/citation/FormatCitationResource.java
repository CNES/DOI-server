/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnes.doi.resource.citation;

import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Formats a citation.
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class FormatCitationResource extends BaseCitationResource {
    
    private String doiName;
    private String style;
    private String language;

    /**
     * Init by getting doi, lang and style values.
     * @throws ResourceException 
     */
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();        
        this.doiName = getQueryValue("doi");
        this.language = getQueryValue("lang");
        this.style = getQueryValue("style");
    }

    /**
     * Returns the formatted citation.
     * @return the formatted citation
     */
    @Get
    public String getFormat() {
        getLogger().entering(getClass().getName(), "getFormat",new Object[]{this.doiName, this.language, this.style});
        
        final String result = this.app.getClient().getFormat(this.doiName, this.style, this.language);
        
        getLogger().exiting(getClass().getName(), "getFormats", result);
        return result;
    }
    
}

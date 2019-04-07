/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi;

import fr.cnes.doi.client.ClientMDS;
import java.util.Arrays;
import java.util.List;
import org.mortbay.jetty.HttpMethods;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class MdsSpec extends AbstractSpec {

        
    public enum Spec {
  
        GET_DOI_200("Get DOI", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 200, "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"), 
        GET_DOI_204("Get DOI no content", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 204, ""),        
        GET_DOI_401("Get not allowed DOI", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 401, "Bad credentials"), 
        GET_DOI_403("Get unauthorized DOI", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 403, "login problem or dataset belongs to another party"),
        GET_DOI_404("Get not found DOI", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 404, "DOI not found"),        
        GET_DOI_500("Get DOI with internal server error", HttpMethods.GET, "/" + ClientMDS.DOI_RESOURCE, "10.5072/2783446.2783605", 500, "server internal error, try later and if problem persists please contact us"),
        POST_DOI_201("Create a DOI", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 201, "CREATED"),
        POST_DOI_400("Create a DOI with a bad request", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 400, "request body must be exactly two lines: DOI and URL; wrong domain, wrong prefix"),        
        POST_DOI_401("Create unauthorized DOI", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 401, "no login"), 
        POST_DOI_403("Create forbidden DOI", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 403, "login problem, quota exceeded"),  
        POST_DOI_412("Create DOI with precondition", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 412, "metadata must be uploaded first"),
        POST_DOI_500("Create DOI with internal server error", HttpMethods.POST, "/" + ClientMDS.DOI_RESOURCE, "", 500, "server internal error, try later and if problem persists please contact us"),  
        GET_METADATA_200("Get successfull DOI metadata", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b", 200, XML), 
        GET_METADATA_401("Fail to get DOI metadata due to an unauthorized request", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 401, "no login"), 
        GET_METADATA_403("Fail to get DOI metadata due to a forbidden request", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 403, "login problem or dataset belongs to another party"),          
        GET_METADATA_400("Fail to get DOI metadata due to an unknown DOI", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b", 400, "DOI does not exist in our database"),  
        GET_METADATA_410("Fail to get DOI metadata due to an inactive DOI", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 410, "the requested dataset was marked inactive"), 
        GET_METADATA_500("Fail to get DOI metadata due to an internal server error", HttpMethods.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),  
        POST_METADATA_201("Create DOI metadata", HttpMethods.POST, "/" + ClientMDS.METADATA_RESOURCE, "", 201, "CREATED"),
        POST_METADATA_400("Fail to create DOI metadata due to wrong input parameters", HttpMethods.POST, "/" + ClientMDS.METADATA_RESOURCE, "", 400, "invalid XML, wrong prefix"),
        POST_METADATA_401("Fail to create DOI metadata due to an unauthorized request", HttpMethods.POST, "/" + ClientMDS.METADATA_RESOURCE, "", 401, "no login"),
        POST_METADATA_403("Fail to create DOI metadata due to a forbidden request", HttpMethods.POST, "/" + ClientMDS.METADATA_RESOURCE, "", 403, "login problem, quota exceeded"),
        POST_METADATA_500("Fail to create DOI metadata due to a forbidden request", HttpMethods.POST, "/" + ClientMDS.METADATA_RESOURCE, "", 500, "server internal error, try later and if problem persists please contact us"),
        DELETE_METADATA_200("Delete DOI metadata", HttpMethods.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b", 200, XML),
        DELETE_METADATA_401("Fail to delete DOI metadata due to an unauthorized request", HttpMethods.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 401, "no login"),        
        DELETE_METADATA_403("Fail to delete DOI metadata due to a forbidden request", HttpMethods.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 403, "login problem or dataset belongs to another party"), 
        DELETE_METADATA_404("Fail to delete DOI metadata due to an unknown DOI", HttpMethods.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 404, "DOI does not exist in our database"),  
        DELETE_METADATA_500("Fail to delete DOI metadata due to an internal server error", HttpMethods.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.5072/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),          
        GET_MEDIA_200("Get media", HttpMethods.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 200, "application/fits=http://cnes.fr/test-data"), 
        GET_MEDIA_401("Fail to get media due to an unauthorized request", HttpMethods.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 401, "no login"), 
        GET_MEDIA_403("Fail to get media due to a forbidden request", HttpMethods.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 403, "login problem or dataset belongs to another party"),  
        GET_MEDIA_404("Fail to get media due to an unknown DOI", HttpMethods.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 404, "No media attached to the DOI or DOI does not exist in our database"),     
        GET_MEDIA_500("Fail to get media due to an internal server error", HttpMethods.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"), 
        POST_MEDIA_200("Create a media", HttpMethods.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 200, "operation successful"), 
        POST_MEDIA_400("Fail to create a media due to bad input parameters", HttpMethods.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 400, "one or more of the specified mime-types or urls are invalid"), 
        POST_MEDIA_401("Fail to create a media due to an unauthorized request", HttpMethods.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 401, "no login"),  
        POST_MEDIA_403("Fail to create a media due to a forbidden request", HttpMethods.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 403, "login problem"), 
        POST_MEDIA_500("Fail to create a media due to an internal server error", HttpMethods.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.5072/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us");
             
        private final String description;
        private final String httVerb;
        private final String path;
        private final String templatePath;
        private final int status;
        private final String body;
        
        Spec(final String description, final String httpVerb, final String path, final String templatePath, 
                final int status, final String body) {
            this.description = description;
            this.httVerb = httpVerb;
            this.path = path;
            this.templatePath = templatePath;
            this.status = status;
            this.body = body;
        }
        
        public String getDescription() {
            return this.description;
        }
        
        public String getHttpVerb() {
            return this.httVerb;
        }
        
        public String getPath() {
            return this.path;
        }  
        
        public String getTemplatePath() {
            return this.templatePath;
        }
        
        public int getStatus() {
            return this.status;
        }   
        
        public String getBody() {
            return this.body;
        }
        
        public List<String> getBodyAsList() {
            String values = this.body.substring(1, this.body.length()-1).replaceAll("\"", "");            
            return Arrays.asList(values.split(","));
        }
    }
    
    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\">\n"
            + "    <identifier identifierType=\"DOI\">10.5072/828606/8c3e91ad45ca855b477126bc073ae44b</identifier>\n"
            + "    <creators>\n"
            + "        <creator>\n"
            + "            <creatorName>CNES</creatorName>\n"
            + "        </creator>\n"
            + "    </creators>\n"
            + "    <titles>\n"
            + "        <title>Le portail Éduthèque</title>\n"
            + "    </titles>\n"
            + "    <publisher>CNES</publisher>\n"
            + "    <publicationYear>2015</publicationYear>\n"
            + "    <resourceType resourceTypeGeneral=\"Other\">Portail Éduthèque</resourceType>\n"
            + "</resource>";    
    
    private final MockupServer mockServer;
    
    public MdsSpec(int port) {
        this(port, -1);
    } 
    
    public MdsSpec(int port, int portProxy) {
        this.mockServer = new MockupServer(port, portProxy);
    }      
    
    public void createSpec(final Spec specification) {
        final String path = specification.getTemplatePath().isEmpty() 
                ? specification.getPath() 
                : specification.getPath()+"/"+specification.getTemplatePath();        
        this.mockServer.createSpec(
                specification.getHttpVerb(), path, 
                specification.getStatus(), specification.getBody()
        );
    }
    
    public void verifySpec(final Spec specification) {
        final String path = specification.getTemplatePath().isEmpty() 
                ? specification.getPath() 
                : specification.getPath()+"/"+specification.getTemplatePath();
        this.mockServer.verifySpec(specification.getHttpVerb(), path);
    }    
    
    public void verifySpec(final Spec specification, int exactly) {
        final String path = specification.getTemplatePath().isEmpty() 
                ? specification.getPath() 
                : specification.getPath()+"/"+specification.getTemplatePath();
        this.mockServer.verifySpec(specification.getHttpVerb(), path, exactly);
    }  
    

    public void reset() {
        this.mockServer.reset();
    }    

    public void finish() {
        this.mockServer.close();
    }
        
}

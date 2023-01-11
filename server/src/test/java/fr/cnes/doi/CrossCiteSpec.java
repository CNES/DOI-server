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

import fr.cnes.doi.client.ClientCrossCiteCitation;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jetty.http.HttpMethod;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class CrossCiteSpec extends AbstractSpec {
	
    public enum Spec {
        
        GET_STYLE_200("Get styles", HttpMethod.GET, "/"+ClientCrossCiteCitation.STYLE_URI, 200, "[\"academy-of-management-review\",\"accident-analysis-and-prevention\",\"acm-sig-proceedings-long-author-list\"]"),
        GET_LANGUAGE_200("Get languages", HttpMethod.GET, "/"+ClientCrossCiteCitation.LOCALE_URI, 200, "[\"af-ZA\",\"ar\",\"bg-BG\",\"ca-AD\",\"cs-CZ\",\"cy-GB\",\"da-DK\",\"de-AT\",\"de-CH\",\"de-DE\",\"el-GR\",\"en-GB\",\"en-US\",\"es-CL\",\"es-ES\",\"es-MX\",\"et-EE\",\"eu\",\"fa-IR\",\"fi-FI\",\"fr-CA\",\"fr-FR\",\"he-IL\",\"hr-HR\",\"hu-HU\",\"id-ID\",\"is-IS\",\"it-IT\",\"ja-JP\",\"km-KH\",\"ko-KR\",\"lt-LT\",\"lv-LV\",\"mn-MN\",\"nb-NO\",\"nl-NL\",\"nn-NO\",\"pl-PL\",\"pt-BR\",\"pt-PT\",\"ro-RO\",\"ru-RU\",\"sk-SK\",\"sl-SI\",\"sr-RS\",\"sv-SE\",\"th-TH\",\"tr-TR\",\"uk-UA\",\"vi-VN\",\"zh-CN\",\"zh-TW\"]"),
        GET_FORMAT_200("Get format", HttpMethod.GET, "/"+ClientCrossCiteCitation.FORMAT_URI, 200, "Garza, K., Goble, C., Brooke, J., & Jay, C. 2015. Framing the community data system interface. Proceedings of the 2015 British HCI Conference on - British HCI '15. Presented at the the 2015 British HCI Conference, ACM Press. https://doi.org/10.1145/2783446.2783605.\n"),
        GET_FORMAT_400("Fail to get format due to a bad request", HttpMethod.GET, "/"+ClientCrossCiteCitation.FORMAT_URI, 400, ""),
        GET_FORMAT_404("Fail to get format due to a bad DOI", HttpMethod.GET, "/"+ClientCrossCiteCitation.FORMAT_URI, 404, "DOI not found");                
        
        private final String description;
        private final HttpMethod httVerb;
        private final String path;
        private final int status;
        private final String body;
        
        Spec(final String description, final HttpMethod httpVerb, final String path, 
                final int status, final String body) {
            this.description = description;
            this.httVerb = httpVerb;
            this.path = path;
            this.status = status;
            this.body = body;
        }
        
        public String getDescription() {
            return this.description;
        }
        
        public String getHttpVerb() {
            return this.httVerb.name();
        }
        
        public String getPath() {
            return this.path;
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
    
    private final MockupServer mockServer;
    
    public CrossCiteSpec(int port) {
        this.mockServer = new MockupServer(port);
    }   
      
    public void createSpec(final Spec specification) {
        this.mockServer.createSpec(
                specification.getHttpVerb(), specification.getPath(), 
                specification.getStatus(), specification.getBody()
        );
    }
    
    public void verifySpec(final Spec specification) {
        this.mockServer.verifySpec(specification.getHttpVerb(), specification.getPath());
    }
    
    public void reset() {
        this.mockServer.reset();
    }    
    
    public void finish() {
        this.mockServer.close();
    }
        
}

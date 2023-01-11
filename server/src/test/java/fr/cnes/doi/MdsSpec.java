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
import org.eclipse.jetty.http.HttpMethod;

/**
 *
 * @author Jean-Christophe Malapert
 */
public class MdsSpec extends AbstractSpec {

    public enum Spec {
        GET_DOIS("Get DOI collection", HttpMethod.GET, "/" +ClientMDS.DOI_RESOURCE, "", 200, RESPONSE_DOIS),
        GET_DOI_200("Get DOI", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 200, "https://edutheque.cnes.fr/fr/web/CNES-fr/10884-edutheque.php"),
        GET_DOI_204("Get DOI no content", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 204, ""),
        GET_DOI_401("Get not allowed DOI", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 401, "Bad credentials"),
        GET_DOI_403("Get unauthorized DOI", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 403, "login problem or dataset belongs to another party"),
        GET_DOI_404("Get not found DOI", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 404, "DOI not found"),
        GET_DOI_500("Get DOI with internal server error", HttpMethod.GET, "/" + ClientMDS.DOI_RESOURCE, "10.80163/2783446.2783605", 500, "server internal error, try later and if problem persists please contact us"),
        PUT_DOI_201("Create a DOI", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/EDU/TESTID", 201, "CREATED"),
        PUT_DOI_201_2("Create a DOI", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 201, "CREATED"),
        PUT_DOI_400("Create a DOI with a bad request", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/EDU/TESTID", 400, "request body must be exactly two lines: DOI and URL; wrong domain, wrong prefix"),
        PUT_DOI_401("Create unauthorized DOI", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/EDU/TESTID", 401, "no login"),
        PUT_DOI_403("Create forbidden DOI", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE , "10.80163/EDU/TESTID", 403, "login problem, quota exceeded"),
        PUT_DOI_412("Create DOI with precondition", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/EDU/TESTID", 412, "metadata must be uploaded first"),
        PUT_DOI_412_2("Create DOI with precondition", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae", 412, "metadata must be uploaded first"),
        PUT_DOI_500("Create DOI with internal server error", HttpMethod.PUT, "/" + ClientMDS.DOI_RESOURCE, "10.80163/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),
        GET_METADATA_200("Get successfull DOI metadata", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 200, XML),
        GET_METADATA_401("Fail to get DOI metadata due to an unauthorized request", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 401, "no login"),
        GET_METADATA_403("Fail to get DOI metadata due to a forbidden request", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 403, "login problem or dataset belongs to another party"),
        GET_METADATA_400("Fail to get DOI metadata due to an unknown DOI", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 400, "DOI does not exist in our database"),
        GET_METADATA_410("Fail to get DOI metadata due to an inactive DOI", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 410, "the requested dataset was marked inactive"),
        GET_METADATA_500("Fail to get DOI metadata due to an internal server error", HttpMethod.GET, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),
        PUT_METADATA_201("Create DOI metadata", HttpMethod.PUT, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 201, "CREATED"),
        PUT_METADATA_400("Fail to create DOI metadata due to wrong input parameters", HttpMethod.PUT, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 400, "invalid XML, wrong prefix"),
        PUT_METADATA_401("Fail to create DOI metadata due to an unauthorized request", HttpMethod.PUT, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 401, "no login"),
        PUT_METADATA_403("Fail to create DOI metadata due to a forbidden request", HttpMethod.PUT, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 403, "login problem, quota exceeded"),
        PUT_METADATA_500("Fail to create DOI metadata due to a forbidden request", HttpMethod.PUT, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 500, "server internal error, try later and if problem persists please contact us"),
        DELETE_METADATA_200("Delete DOI metadata", HttpMethod.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 200, XML),
        DELETE_METADATA_401("Fail to delete DOI metadata due to an unauthorized request", HttpMethod.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 401, "no login"),
        DELETE_METADATA_403("Fail to delete DOI metadata due to a forbidden request", HttpMethod.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 403, "login problem or dataset belongs to another party"),
        DELETE_METADATA_404("Fail to delete DOI metadata due to an unknown DOI", HttpMethod.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 404, "DOI does not exist in our database"),
        DELETE_METADATA_500("Fail to delete DOI metadata due to an internal server error", HttpMethod.DELETE, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),
        GET_MEDIA_200("Get media", HttpMethod.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 200, "application/fits=http://cnes.fr/test-data"),
        GET_MEDIA_401("Fail to get media due to an unauthorized request", HttpMethod.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 401, "no login"),
        GET_MEDIA_403("Fail to get media due to a forbidden request", HttpMethod.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 403, "login problem or dataset belongs to another party"),
        GET_MEDIA_404("Fail to get media due to an unknown DOI", HttpMethod.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 404, "No media attached to the DOI or DOI does not exist in our database"),
        GET_MEDIA_500("Fail to get media due to an internal server error", HttpMethod.GET, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),
        POST_MEDIA_200("Create a media", HttpMethod.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 200, "operation successful"),
        POST_MEDIA_400("Fail to create a media due to bad input parameters", HttpMethod.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 400, "one or more of the specified mime-types or urls are invalid"),
        POST_MEDIA_401("Fail to create a media due to an unauthorized request", HttpMethod.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 401, "no login"),
        POST_MEDIA_403("Fail to create a media due to a forbidden request", HttpMethod.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 403, "login problem"),
        POST_MEDIA_500("Fail to create a media due to an internal server error", HttpMethod.POST, "/" + ClientMDS.MEDIA_RESOURCE, "10.80163/EDU/TESTID", 500, "server internal error, try later and if problem persists please contact us"),
    	VALIDATE_METADATA_200("Validate metadata", HttpMethod.POST, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 200, "true"),
    	VALIDATE_METADATA_406("Invalidate metadata", HttpMethod.POST, "/" + ClientMDS.METADATA_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 406, "invalid XML, wrong prefix"),
    	GET_INISTCODE_200("Get inist code", HttpMethod.GET, "/" + ClientMDS.INIST_RESOURCE, "10.80163/828606/8c3e91ad45ca855b477126bc073ae44b", 200, "true");

        private final String description;
        private final HttpMethod httVerb;
        private final String path;
        private final String templatePath;
        private final int status;
        private final String body;

        Spec(final String description, final HttpMethod httpVerb, final String path, final String templatePath,
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
            return this.httVerb.name();
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
            String values = this.body.substring(1, this.body.length() - 1).replaceAll("\"", "");
            return Arrays.asList(values.split(","));
        }
    }

    public static final String RESPONSE_DOIS = "10.24400/329360/F7Q52MNK\n"
            + "10.24400/989788/1021011\n"
            + "10.24400/989788/1026504\n"
            + "10.24400/989788/109097\n"
            + "10.24400/989788/120827\n"
            + "10.24400/989788/144457\n"
            + "10.24400/989788/145294\n"
            + "10.24400/989788/145665\n"
            + "10.24400/989788/224101\n"
            + "10.24400/989788/228061\n"
            + "10.24400/989788/234616\n"
            + "10.24400/989788/240976\n"
            + "10.24400/989788/2783\n"
            + "10.24400/989788/292251\n"
            + "10.24400/989788/308848\n"
            + "10.24400/989788/3291\n"
            + "10.24400/989788/360882\n"
            + "10.24400/989788/499693\n"
            + "10.24400/989788/508778\n"
            + "10.24400/989788/512920\n"
            + "10.24400/989788/545660\n"
            + "10.24400/989788/580902\n"
            + "10.24400/989788/59829\n"
            + "10.24400/989788/635708\n"
            + "10.24400/989788/645361\n"
            + "10.24400/989788/652324\n"
            + "10.24400/989788/658762\n"
            + "10.24400/989788/666451\n"
            + "10.24400/989788/667331\n"
            + "10.24400/989788/674512\n"
            + "10.24400/989788/710059\n"
            + "10.24400/989788/764732\n"
            + "10.24400/989788/791927\n"
            + "10.24400/989788/80818\n"
            + "10.24400/989788/815434\n"
            + "10.24400/989788/830719\n"
            + "10.24400/989788/849995\n"
            + "10.24400/989788/883678\n"
            + "10.24400/989788/896546\n"
            + "10.24400/989788/897410\n"
            + "10.24400/989788/909385\n"
            + "10.24400/989788/918458\n"
            + "10.24400/989788/950314\n"
            + "10.24400/989788/995209";

    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://datacite.org/schema/kernel-4\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\">\n"
            + "    <identifier identifierType=\"DOI\">10.80163/828606/8c3e91ad45ca855b477126bc073ae44b</identifier>\n"
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
                : specification.getPath() + "/" + specification.getTemplatePath();
        this.mockServer.createSpec(
                specification.getHttpVerb(), path,
                specification.getStatus(), specification.getBody()
        );
    }

    public void verifySpec(final Spec specification) {
        final String path = specification.getTemplatePath().isEmpty()
                ? specification.getPath()
                : specification.getPath() + "/" + specification.getTemplatePath();
        this.mockServer.verifySpec(specification.getHttpVerb(), path);
    }

    public void verifySpec(final Spec specification, int exactly) {
        final String path = specification.getTemplatePath().isEmpty()
                ? specification.getPath()
                : specification.getPath() + "/" + specification.getTemplatePath();
        this.mockServer.verifySpec(specification.getHttpVerb(), path, exactly);
    }

    public void reset() {
        this.mockServer.reset();
    }

    public void finish() {
        this.mockServer.close();
    }

}

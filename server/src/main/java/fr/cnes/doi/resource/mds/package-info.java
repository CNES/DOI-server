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
/**
 * Provides resources for Meta Data Store application.
 *
 * The {@link fr.cnes.doi.application.DoiMdsApplication} is the main DOI
 * {@link fr.cnes.doi.application application} that brings together a set of
 * resources handling the life of DOIs.
 *
 * <p>
 * An application is instantiated only once when the server starts. On the
 * contrary a resource is instantiated at each call. The {@link fr.cnes.doi.application.DoiMdsApplication}
 * {@link fr.cnes.doi.application.DoiMdsApplication#createRouter routes} the
 * different resources according to an URI.
 *
 * <ul>
 * <li>the {@link fr.cnes.doi.resource.mds.DoisResource} is routed by the URI
 * {@value fr.cnes.doi.application.DoiMdsApplication#DOI_URI}</li>
 * <li>the {@link fr.cnes.doi.resource.mds.DoiResource} is routed by the URI  {@value fr.cnes.doi.application.DoiMdsApplication#DOI_URI}
 * {@value fr.cnes.doi.application.DoiMdsApplication#DOI_NAME_URI}</li>
 * <li>the {@link fr.cnes.doi.resource.mds.MetadatasResource} is routed by the
 * URI {@value fr.cnes.doi.application.DoiMdsApplication#METADATAS_URI}</li>
 * <li>the {@link fr.cnes.doi.resource.mds.MetadataResource} is routed by the
 * URI  {@value fr.cnes.doi.application.DoiMdsApplication#METADATAS_URI}
 * {@value fr.cnes.doi.application.DoiMdsApplication#DOI_NAME_URI}</li>
 * <li>the {@link fr.cnes.doi.resource.mds.MediaResource} is routed by the URI  {@value fr.cnes.doi.application.DoiMdsApplication#MEDIA_URI}
 * {@value fr.cnes.doi.application.DoiMdsApplication#DOI_NAME_URI}</li>
 * </ul>
 *
 * Some of the above resources are protected by several mechanism. This
 * mechanism is defined as a pipeline:
 * <ul>
 * <li>Request: --&gt;
 * {@link fr.cnes.doi.application.DoiMdsApplication#createAuthenticator Basic auth}(optional)
 * --&gt;{@link fr.cnes.doi.application.DoiMdsApplication#createTokenAuthenticator Token auth}
 * (optional)
 * --&gt;{@link fr.cnes.doi.application.DoiMdsApplication#createMethodAuthorizer Method auth}
 * --&gt;{@link fr.cnes.doi.application.DoiMdsApplication#createRouter router}</li>
 * <li>Response: &lt;--  {@link fr.cnes.doi.application.DoiMdsApplication.SecurityPostProcessingFilter 
 * SecurityPostProcessing} &lt;---</li>
 * </ul>
 *
 * The {@link fr.cnes.doi.client.ClientMDS clientMDS} implements the call to
 * <a href="https://support.datacite.org/docs/mds-api-guide">DataCite</a>. This
 * clients supports the proxy by the use of
 * {@link org.restlet.ext.httpclient4.HttpDOIClientHelper} plugin. This plugin
 * is registered by default when the method
 * {@link fr.cnes.doi.client.BaseClient#getClient} is used. The proxy parameters
 * are automatically set from the config.properties by the use of
 * {@link fr.cnes.doi.settings.ProxySettings}
 *
 *
 * <img src="{@docRoot}/doc-files/mds.png" alt="MDS application">
 *
 * <h2>How to create a DOI</h2>
 * Here are the different steps to create a DOI:
 * <ul>
 * <li>1. Create a project on the server by using the
 * {@link fr.cnes.doi.application.AdminApplication} through the resource
 * {@link fr.cnes.doi.resource.admin.SuffixProjectsResource#createProject}. The
 * response at this resource is an identifier, which is a part of the DOI
 * suffix. This suffix guarantees the uniqueness of the DOI within the
 * institution at condition that the project defines an identifier for each
 * data. This data identifier will be part of the DOI suffix. Thus, the new DOI
 * suffix has the following syntax <b>projectIdentifier/dataIdentifier</b></li>
 * <li>2 - Create the DOI metadata according to the
 * {@value fr.cnes.doi.client.ClientMDS#SCHEMA_DATACITE} schema.</li>
 * <li>3 - Upload the DOI metadata by using the
 * {@link fr.cnes.doi.application.DoiMdsApplication} application through the
 * {@link fr.cnes.doi.resource.mds.MetadatasResource}</li>
 * <li>4 - Upload the landing page URL using
 * {@link fr.cnes.doi.application.DoiMdsApplication} application through the
 * {@link fr.cnes.doi.resource.mds.DoisResource}</li>
 * </ul>
 * As below, an example of a code to create a DOI.
 * <pre>
 * {@code
 *      // 1 - Upload the metadata
 *      // -------------------
 *
 *      // Prepare the request
 *      ClientResource client = new ClientResource("http://localhost:" + port + METADATA_SERVICE);
 *      // Set the login/pwd for basic authentication
 *      client.setChallengeResponse(
 *          new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "malapert", "pwd")
 *      );
 *      // Set the project identifier (which is a part of the DOI) for authorisation
 *      reqAttribs = client.getRequestAttributes();
 *      headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
 *      if (headers == null) {
 *          headers = new Series<>(Header.class);
 *          reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
 *      }
 *      headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
 *      // Request the metadata service
 *      try {
 *          Representation rep = client.post(
 *              new StringRepresentation(this.doiMetadata, MediaType.APPLICATION_XML)
 *          );
 *      } catch (ResourceException ex) {
 *      } finally {
 *          client.release();
 *      }
 *
 *      // 2 - now we need to upload the landing page URL
 *      //------------------------------------------------
 *
 *      // Create the first input parameter : DOI
 *      Form doiForm = new Form();
 *      doiForm.add(
 *          new Parameter(
 *              DoisResource.DOI_PARAMETER,
 *              "10.5072/828606/8c3e91ad45ca855b477126bc073ae44b"
 *          )
 *      );
 *      // Create the second input parameter : landing page
 *      doiForm.add(new Parameter(DoisResource.URL_PARAMETER, "http://www.cnes.fr"));
 *
 *      // Prepare the query
 *      String port = DoiSettings.getInstance().getString(Consts.SERVER_HTTP_PORT);
 *      client = new ClientResource("http://localhost:" + port + DOIS_SERVICE);
 *      // Set the login/password
 *      client.setChallengeResponse(
 *          new ChallengeResponse(
 *              ChallengeScheme.HTTP_BASIC,
 *              "malapert",
 *              "pwd"
 *          )
 *      );
 *      // Define the role based on the project identifier
 *      final String RESTLET_HTTP_HEADERS = "org.restlet.http.headers";
 *      Map<String, Object> reqAttribs = client.getRequestAttributes();
 *      Series headers = (Series) reqAttribs.get(RESTLET_HTTP_HEADERS);
 *      if (headers == null) {
 *          headers = new Series<>(Header.class);
 *          reqAttribs.put(RESTLET_HTTP_HEADERS, headers);
 *      }
 *      headers.add(UtilsHeader.SELECTED_ROLE_PARAMETER, "828606");
 *
 *      // Make the request
 *      try {
 *          Representation rep = client.post(doiForm);
 *      } catch (ResourceException ex) {
 *      } finally {
 *          client.release();
 *      }
 * }
 * </pre>
 *
 * @see fr.cnes.doi.server Architecture definition
 */
package fr.cnes.doi.resource.mds;

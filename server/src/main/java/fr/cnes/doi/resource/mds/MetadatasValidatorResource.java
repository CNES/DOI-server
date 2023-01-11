/*
 * Copyright (C) 2017-2021 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.resource.mds;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.Level;
import org.datacite.schema.kernel_4.Resource;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.xml.sax.SAXException;

import fr.cnes.doi.application.DoiMdsApplication.API_MDS;
import fr.cnes.doi.exception.DoiServerException;
import fr.cnes.doi.utils.spec.Requirement;

/**
 * Resource to handle a collection of metadata.
 *
 * @author Capgemini
 */
public class MetadatasValidatorResource extends BaseMdsResource {

	/**
	 * Function of this resource {@value #VALIDATE_METADATA}.
	 */
	public static final String VALIDATE_METADATA = "Validate Metadata";

	/**
	 * Xsd schema {@value #XSD_SCHEMA}.
	 */
	public static final String XSD_SCHEMA = "/xsd/metadata.xsd";

	/**
	 * Init.
	 *
	 * @throws DoiServerException
	 *             - if a problem happens
	 */
	@Override
	protected void doInit() throws DoiServerException {
		super.doInit();
		LOG.traceEntry();
		setDescription("This resource can validate metadata");
		LOG.traceExit();
	}

	/**
	 * Validate Metadata for a given DOI. The DOI name is given in the metadata
	 * entity.
	 *
	 * @param entity
	 *            Metadata representation
	 * @return short explanation of status code e.g. CREATED,
	 *         HANDLE_ALREADY_EXISTS etc
	 * @throws DoiServerException
	 *             - if the response is not a success
	 *             <ul>
	 *             <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
	 *             <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
	 *             <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
	 *             <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
	 *             <li>{@link API_MDS#DATACITE_PROBLEM}</li>
	 *             <li>{@link API_MDS#METADATA_VALIDATION}</li>
	 *             <li>{@link API_MDS#NETWORK_PROBLEM}</li>
	 *             </ul>
	 */
	@Requirement(reqId = Requirement.DOI_MONIT_020, reqName = Requirement.DOI_MONIT_020_NAME)
	@Requirement(reqId = Requirement.DOI_INTER_070, reqName = Requirement.DOI_INTER_070_NAME)
	@Requirement(reqId = Requirement.DOI_AUTO_020, reqName = Requirement.DOI_AUTO_020_NAME)
	@Requirement(reqId = Requirement.DOI_AUTO_030, reqName = Requirement.DOI_AUTO_030_NAME)
	@Post
	public Representation validateMetadata(final String entity)
			throws DoiServerException {
		LOG.traceEntry("Parameter\n\tentity:{}", entity);
		checkInputs(entity);

		setStatus(Status.SUCCESS_OK);
		return LOG.traceExit(new StringRepresentation(validateResourceObject(entity)));
	}

	/**
	 * Checks inputs.
	 *
	 * Checks if <i>obj</i> is {@link #isObjectExist set}
	 *
	 * @param obj
	 *            object to check
	 * @throws DoiServerException
	 *             - {@link API_MDS#METADATA_VALIDATION}
	 */
	private void checkInputs(final Object obj) throws DoiServerException {
		LOG.traceEntry("Parameter : " + obj);
		if (isObjectNotExist(obj)) {
			throw LOG.throwing(Level.ERROR,
					new DoiServerException(getApplication(),
							API_MDS.METADATA_VALIDATION, "Input is not set"));
		}
		LOG.traceExit();
	}

	/**
	 * Validate the metadata object from its xml string.
	 *
	 * @param entity
	 *            metadata representation
	 * @return the validity or error
	 * @throws DoiServerException
	 *             - if an error was encountered
	 */
	private String validateResourceObject(final String entity) {
		LOG.traceEntry("Parameter : " + entity);
		setStatus(Status.SUCCESS_OK);

		JAXBContext jaxbContext = null;
		try {
			// Get JAXBContext
			jaxbContext = JAXBContext.newInstance(Resource.class);

			// Create Unmarshaller
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			// Setup schema validator
			SchemaFactory sf = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// Convert it to Source
			Source schemaSource = new StreamSource(
					this.getClass().getResource(XSD_SCHEMA).toString());

			// Create the schema
			Schema resourceSchema = sf.newSchema(schemaSource);

			// Set the schema to the unmarshaller
			jaxbUnmarshaller.setSchema(resourceSchema);

			InputStream xmlStream = new ByteArrayInputStream(entity.getBytes());

			// Unmarshal xml file
			jaxbUnmarshaller.unmarshal(xmlStream);

			// If no exception raise the file is valid
			return "true";
		} catch (SAXException ex) {
            throw LOG.throwing(Level.ERROR,
            	new DoiServerException(getApplication(), API_MDS.METADATA_VALIDATION,
                    "Error on schema instanciation : " + ex.getMessage(), ex));
		} catch (JAXBException ex) {
			setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			String str = ex.getLinkedException().toString();
			int lineNumberIndex = str.indexOf("lineNumber:");
			String sLine = str.substring(lineNumberIndex + 12, str.length())
					.split(";")[0];
			int lineNumber = Integer.parseInt(sLine);

			return "{" + lineNumber + "||"
					+ ex.getLinkedException().getMessage() + "}";
		}
	}

	/**
	 * Describes the POST method.
	 *
	 * This request validate or not the metadata. The request body must contain
	 * a valid XML.
	 *
	 * <ul>
	 * <li>{@link API_MDS#METADATA_VALIDATION}</li>
	 * <li>{@link API_MDS#DATACITE_PROBLEM}</li>
	 * <li>{@link API_MDS#NETWORK_PROBLEM}</li>
	 * <li>{@link API_MDS#SECURITY_USER_NO_ROLE}</li>
	 * <li>{@link API_MDS#SECURITY_USER_NOT_IN_SELECTED_ROLE}</li>
	 * <li>{@link API_MDS#SECURITY_USER_PERMISSION}</li>
	 * <li>{@link API_MDS#SECURITY_USER_CONFLICT}</li>
	 * </ul>
	 *
	 * @param info
	 *            Wadl description for POST method
	 */
	@Requirement(reqId = Requirement.DOI_DOC_010, reqName = Requirement.DOI_DOC_010_NAME)
	@Override
	protected final void describePost(final MethodInfo info) {
		info.setName(Method.POST);
		info.setDocumentation("This request validate metadata. "
				+ "The request body must contain a valid XML.");

		addRequestDocToMethod(info,
				Arrays.asList(createQueryParamDoc("selectedRole",
						ParameterStyle.HEADER,
						"A user can select one role when he is associated "
								+ "to several roles",
						false, "xs:string")),
				createQueryRepresentationDoc("metadataRepresentation",
						MediaType.APPLICATION_XML, "DataCite metadata",
						"default:Resource"));
		addResponseDocToMethod(info,
				createResponseDoc(API_MDS.VALIDATE_METADATA.getStatus(),
						API_MDS.VALIDATE_METADATA.getShortMessage(),
						"explainRepresentationID"));
		addResponseDocToMethod(info,
				createResponseDoc(API_MDS.DATACITE_PROBLEM.getStatus(),
						API_MDS.DATACITE_PROBLEM.getShortMessage(),
						"explainRepresentationID"));
		addResponseDocToMethod(info,
				createResponseDoc(API_MDS.NETWORK_PROBLEM.getStatus(),
						API_MDS.NETWORK_PROBLEM.getShortMessage(),
						"explainRepresentationID"));
		super.describePost(info);
	}
}

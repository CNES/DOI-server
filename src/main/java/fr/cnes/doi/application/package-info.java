/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Provides the classes necessary to create applications for the Data Object
 * Identifier server.
 * 
 * <p>
 * The applications package contains:
 * <ul>
 * <li>a  {@link fr.cnes.doi.application.DoiMdsApplication Meta Data Store application} that is responsible to handle (create/update/
 * delete) DOIs.</li>
 * <li>A {@link fr.cnes.doi.application.DoiCrossCiteApplication Cross Cite application} that is responsible to get citation from 
 * a registered DOI</li>
 * <li>A {@link fr.cnes.doi.application.AdminApplication web application} that provides resources for the administration of the tool</li> 
 * </ul>
 * 
 * <h2>1 - Introduction</h2>
 * A Digital Object Identifier (DOI) is an alphanumeric string assigned to uniquely identify an object. 
 * It is tied to a metadata description of the object as well as to a digital location, such as a URL, 
 * where all the details about the object are accessible.<br>
 * DOI server handles DOI number within an organization, which is composed of several projects
 * 
 * <h2>2 - Meta Data Store application</h2>
 * To assign a DOI to document, the following steps are needed :
 * <ul>
 * <li>Uploads metadata for a DOI to DataCite. metadata contains the DOI name and
 * information describing the DOI. Metadata is based on the DataCite schema.</li>
 * <li>Links the DOI name to a landing page</li>
 * <li>Optionally, links a DOI name to others URL through a content type.</li>
 * </ul>
 * 
 * <h2>2 - Cross cite application</h2>
 * A citation is used in scientific articles. According to the scientific articles,
 * the citation formatter is different. This application takes account of the 
 * language of the article and the formatting style to format a citation for a 
 * specific DOI.
 * 
 * <h2>3 - Monitoring application</h2>
 * An application following the monitoring of DataCite
 * 
 * <h2>4 - Administration application</h2>
 * An application providing resources for the administration of the DOI server.
 */
package fr.cnes.doi.application;

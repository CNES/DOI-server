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
 * The applications contains:
 * <ul>
 * <li>a  {@link fr.cnes.doi.application.DoiMdsApplication Meta Data Store application} that is responsible to handle (create/update/
 * delete) DOIs.</li>
 * <li>A {@link fr.cnes.doi.application.DoiCrossCiteApplication Cross Cite application} that is responsible to get citation from 
 * a registered DOI</li>
 * <li>A {@link fr.cnes.doi.application.DoiStatusApplication monitoring application} that is responsible to give the status of DataCite</li>
 * <li>A {@link fr.cnes.doi.application.AdminApplication web application} that provides resources for the administration of the tool</li> 
 * </ul>
 */
package fr.cnes.doi.application;

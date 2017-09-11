/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Provides access to the DOI database.
 *
 * <p>
 * Two databases are needed :
 * <ul>
 * <li>One to access to the {@link fr.cnes.doi.db.ProjectSuffixDB project database}</li>
 * <li>Another one to access to the {@link fr.cnes.doi.db.TokenDB token database}</li>
 * </ul>
 * 
 * <h2>Project database</h2>
 * The project database stores the project name and its identifier. The identifier
 * is used in the DOI name to assure the unique name through the organization.
 * The DOI is built like this:<br>
 * 
 * doi://<i>prefix organization assigned by DataCite</i>/<i>suffix project assign by DOI server to a project</i>/<i>free identifier by the project</i>
 * 
 * 
 * <h2>Token database</h2>
 * The token database stores the created token in order to verify them when there are used by the user.
 */
package fr.cnes.doi.db;

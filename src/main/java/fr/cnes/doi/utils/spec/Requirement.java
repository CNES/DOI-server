/*
 * Copyright (C) 2018 Centre National d'Etudes Spatiales (CNES).
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
package fr.cnes.doi.utils.spec;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to map requirements with code
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Requirements.class)
public @interface Requirement {
    String documentName() default "SGDS-ST-8000-1538-CNES_0100";
    String reqId();
    String reqName();
    CoverageAnnotation coverage() default CoverageAnnotation.COMPLETE;
    String comment() default "[none]";  
    String version() default "1.0";             
    
    String DOI_SRV_010 = "DOI_SRV_010";
    String DOI_SRV_010_NAME = "Création de métadonnées";    
    
    String DOI_SRV_020 = "DOI_SRV_020";
    String DOI_SRV_020_NAME = "Enregistrement d’un DOI";
    
    String DOI_SRV_030 = "DOI_SRV_030";
    String DOI_SRV_030_NAME = "Mise à jour de l’URL de la landing page d'un DOI";        
    
    String DOI_SRV_040 = "DOI_SRV_040";
    String DOI_SRV_040_NAME = "Mise à jour des métadonnées d’un DOI";     

    String DOI_SRV_050 = "DOI_SRV_050";
    String DOI_SRV_050_NAME = "Désactivation d’un DOI";  
    
    String DOI_SRV_060 = "DOI_SRV_060";
    String DOI_SRV_060_NAME = "Récupération des métadonnées";     
    
    String DOI_SRV_070 = "DOI_SRV_070";
    String DOI_SRV_070_NAME = "Récupération de l’URL de la landing page";     
    
    String DOI_SRV_080 = "DOI_SRV_080";
    String DOI_SRV_080_NAME = "Création d’un média";       
    
    String DOI_SRV_090 = "DOI_SRV_090";
    String DOI_SRV_090_NAME = "Récupération des médias";      
    
    String DOI_SRV_100 = "DOI_SRV_100";
    String DOI_SRV_100_NAME = "Listing des styles";       
    
    String DOI_SRV_110 = "DOI_SRV_110";
    String DOI_SRV_110_NAME = "Listing des langues";      
    
    String DOI_SRV_120 = "DOI_SRV_120";
    String DOI_SRV_120_NAME = "Formatage d’une citation";    
    
    String DOI_SRV_130 = "DOI_SRV_130";
    String DOI_SRV_130_NAME = "Création d'un suffixe projet";      
    
    String DOI_SRV_140 = "DOI_SRV_140";
    String DOI_SRV_140_NAME = "Récupération du nom du projet à partir du suffixe projet";    

    String DOI_SRV_150 = "DOI_SRV_150";
    String DOI_SRV_150_NAME = "Création d'un token";          
    
    String DOI_SRV_180 = "DOI_SRV_180";
    String DOI_SRV_180_NAME = "Information sur le token";       
    
    String DOI_SRV_190 = "DOI_SRV_190";
    String DOI_SRV_190_NAME = "Visualisation des informations d'un DOI";       
    
    
    String DOI_MONIT_010 = "DOI_MONIT_010";
    String DOI_MONIT_010_NAME = "Monitoring des temps de réponse"; 

    String DOI_MONIT_020 = "DOI_MONIT_020";
    String DOI_MONIT_020_NAME = "Monitoring de l'état de DataCite";    
    
   
    String DOI_ARCHI_010 = "DOI_ARCHI_010";
    String DOI_ARCHI_010_NAME = "Protocole HTTP et HTTPS pour les API";              
    
    String DOI_ARCHI_020 = "DOI_ARCHI_020";
    String DOI_ARCHI_020_NAME = "Logs";      
    
    String DOI_ARCHI_030 = "DOI_ARCHI_030";
    String DOI_ARCHI_030_NAME = "Plugins";     
        
    String DOI_ARCHI_040 = "DOI_ARCHI_040";
    String DOI_ARCHI_040_NAME = "Chargement des plugins";  
               
    
    String DOI_INTER_010 = "DOI_INTER_010";
    String DOI_INTER_010_NAME = "Interface avec Datacite Metadata Store";     
    
    String DOI_INTER_020 = "DOI_INTER_020";
    String DOI_INTER_020_NAME = "Interface avec CrossCite Citation";      

    String DOI_INTER_030 = "DOI_INTER_030";
    String DOI_INTER_030_NAME = "Interface avec la base de données de gestion des suffixes DOI"; 

    String DOI_INTER_040 = "DOI_INTER_040";
    String DOI_INTER_040_NAME = "Interface avec la base de données de gestion des tokens"; 

    String DOI_INTER_050 = "DOI_INTER_050";
    String DOI_INTER_050_NAME = "Interface avec la base de données de gestion des utilisateurs";     
    
    String DOI_INTER_060 = "DOI_INTER_060";
    String DOI_INTER_060_NAME = "Vérification du schéma de métadonnées";
    
    String DOI_INTER_070 = "DOI_INTER_070";
    String DOI_INTER_070_NAME = "Vérification des entrées fournies pour DataCite";    
    

    String DOI_IHM_010 = "DOI_IHM_010";
    String DOI_IHM_010_NAME = "IHM Web";     
    
    String DOI_IHM_020 = "DOI_IHM_020";
    String DOI_IHM_020_NAME = "IHM de création d’un DOI";     
    
    String DOI_IHM_030 = "DOI_IHM_030";
    String DOI_IHM_030_NAME = "IHM de génération de métadonnées";    

    String DOI_IHM_040 = "DOI_IHM_040";
    String DOI_IHM_040_NAME = "IHM par service REST";     
    
    String DOI_IHM_050 = "DOI_IHM_050";
    String DOI_IHM_050_NAME = "IHM d’admin";       
    
    
    
    String DOI_AUTH_010 = "DOI_AUTH_010";
    String DOI_AUTH_010_NAME = "Authentification par login/mot de passe";      
    
    String DOI_AUTH_020 = "DOI_AUTH_020";
    String DOI_AUTH_020_NAME = "Authentification par token";     
    
    String DOI_AUTH_030 = "DOI_AUTH_030";
    String DOI_AUTH_030_NAME = "Authentification IHM";      
    
    
    
    String DOI_AUTO_010 = "DOI_AUTO_010";
    String DOI_AUTO_010_NAME = "Association des projets";     
    
    String DOI_AUTO_020 = "DOI_AUTO_020";
    String DOI_AUTO_020_NAME = "Sélection du rôle";         
    
    String DOI_AUTO_030 = "DOI_AUTO_030";
    String DOI_AUTO_030_NAME = "Vérification du projet";       
    
    
    String DOI_DOC_010 = "DOI_DOC_010";
    String DOI_DOC_010_NAME = "Documentation des interfaces";         
    
    String DOI_DEV_010 = "DOI_DEV_010";
    String DOI_DEV_010_NAME = "Développement en Java avec Maven";    
    
    String DOI_DEV_020 = "DOI_DEV_020";
    String DOI_DEV_020_NAME = "OpenSource";      
    
    String DOI_DEPL_010 = "DOI_DEPL_010";
    String DOI_DEPL_010_NAME = "Déploiement du serveur central";      
    
    String DOI_CONFIG_010 = "DOI_CONFIG_010";
    String DOI_CONFIG_010_NAME = "Configuration du serveur central";    
    
    String DOI_CONFIG_020 = "DOI_CONFIG_020";
    String DOI_CONFIG_020_NAME = "Evolutivité du schéma de métadonnées";     
    
    String DOI_PERF_010 = "DOI_PERF_010";
    String DOI_PERF_010_NAME = "Nombre de projets";    
    
    String DOI_PERF_020 = "DOI_PERF_020";
    String DOI_PERF_020_NAME = "Temps de création d’un DOI";     
    
    String DOI_PERF_030 = "DOI_PERF_030";
    String DOI_PERF_030_NAME = "Création de DOI en parallèle";       
    
    String DOI_DISPO_010 = "DOI_DISPO_010";
    String DOI_DISPO_010_NAME = "Taux de disponibilité";     
    
    String DOI_DISPO_020 = "DOI_DISPO_020";
    String DOI_DISPO_020_NAME = "Vérification des landing pages";      
    
    String DOI_LIBCLI_010 = "DOI_LIBCLI_010";
    String DOI_LIBCLI_010_NAME = "Template de landing page";     
    
    String DOI_LIBCLI_020 = "DOI_LIBCLI_020";
    String DOI_LIBCLI_020_NAME = "Librairies clientes en Java et Python";     
}

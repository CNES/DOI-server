/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    public static final String DOI_SRV_010 = "DOI_SRV_010";
    public static final String DOI_SRV_010_NAME = "Création de métadonnées";    
    
    public static final String DOI_SRV_020 = "DOI_SRV_020";
    public static final String DOI_SRV_020_NAME = "Enregistrement d’un DOI";
    
    public static final String DOI_SRV_030 = "DOI_SRV_030";
    public static final String DOI_SRV_030_NAME = "Mise à jour de l’URL de la landing page d'un DOI";        
    
    public static final String DOI_SRV_040 = "DOI_SRV_040";
    public static final String DOI_SRV_040_NAME = "Mise à jour des métadonnées d’un DOI";     

    public static final String DOI_SRV_050 = "DOI_SRV_050";
    public static final String DOI_SRV_050_NAME = "Désactivation d’un DOI";  
    
    public static final String DOI_SRV_060 = "DOI_SRV_060";
    public static final String DOI_SRV_060_NAME = "Récupération des métadonnées";     
    
    public static final String DOI_SRV_070 = "DOI_SRV_070";
    public static final String DOI_SRV_070_NAME = "Récupération de l’URL de la landing page";     
    
    public static final String DOI_SRV_080 = "DOI_SRV_080";
    public static final String DOI_SRV_080_NAME = "Création d’un média";       
    
    public static final String DOI_SRV_090 = "DOI_SRV_090";
    public static final String DOI_SRV_090_NAME = "Récupération des médias";      
    
    public static final String DOI_SRV_100 = "DOI_SRV_100";
    public static final String DOI_SRV_100_NAME = "Listing des styles";       
    
    public static final String DOI_SRV_110 = "DOI_SRV_110";
    public static final String DOI_SRV_110_NAME = "Listing des langues";      
    
    public static final String DOI_SRV_120 = "DOI_SRV_120";
    public static final String DOI_SRV_120_NAME = "Formatage d’une citation";    
    
    public static final String DOI_SRV_130 = "DOI_SRV_130";
    public static final String DOI_SRV_130_NAME = "Création d'un suffixe projet";      
    
    public static final String DOI_SRV_140 = "DOI_SRV_140";
    public static final String DOI_SRV_140_NAME = "Récupération du nom du projet à partir du suffixe projet";    

    public static final String DOI_SRV_150 = "DOI_SRV_150";
    public static final String DOI_SRV_150_NAME = "Création d'un token";          
    
    public static final String DOI_SRV_160 = "DOI_SRV_160";
    public static final String DOI_SRV_160_NAME = "Information sur le token";       
    
    public static final String DOI_SRV_170 = "DOI_SRV_170";
    public static final String DOI_SRV_170_NAME = "Vidualisation des informations d'un DOI";       
    
    
    public static final String DOI_MONIT_010 = "DOI_MONIT_010";
    public static final String DOI_MONIT_010_NAME = "Monitoring des temps de réponse"; 

    public static final String DOI_MONIT_020 = "DOI_MONIT_020";
    public static final String DOI_MONIT_020_NAME = "Monitoring de l'état de DataCite";    
    
   
    public static final String DOI_ARCHI_010 = "DOI_ARCHI_010";
    public static final String DOI_ARCHI_010_NAME = "Protocole HTTP et HTTPS pour les API";              
    
    public static final String DOI_ARCHI_020 = "DOI_ARCHI_020";
    public static final String DOI_ARCHI_020_NAME = "Logs";      
    
    public static final String DOI_ARCHI_030 = "DOI_ARCHI_030";
    public static final String DOI_ARCHI_030_NAME = "Plugins";     
        
    public static final String DOI_ARCHI_040 = "DOI_ARCHI_040";
    public static final String DOI_ARCHI_040_NAME = "Chargement des plugins";  
               
    
    public static final String DOI_INTER_010 = "DOI_INTER_010";
    public static final String DOI_INTER_010_NAME = "Interface avec Datacite Metadata Store";     
    
    public static final String DOI_INTER_020 = "DOI_INTER_020";
    public static final String DOI_INTER_020_NAME = "Interface avec CrossCite Citation";      

    public static final String DOI_INTER_030 = "DOI_INTER_030";
    public static final String DOI_INTER_030_NAME = "Interface avec la base de données de gestion des suffixes DOI"; 

    public static final String DOI_INTER_040 = "DOI_INTER_040";
    public static final String DOI_INTER_040_NAME = "Interface avec la base de données de gestion des tokens"; 

    public static final String DOI_INTER_050 = "DOI_INTER_050";
    public static final String DOI_INTER_050_NAME = "Interface avec la base de données de gestion des utilisateurs";     
    
    public static final String DOI_INTER_060 = "DOI_INTER_060";
    public static final String DOI_INTER_060_NAME = "Vérification du schéma de métadonnées";
    
    public static final String DOI_INTER_070 = "DOI_INTER_070";
    public static final String DOI_INTER_070_NAME = "Vérification des entrées fournies pour DataCite";    
    

    public static final String DOI_IHM_010 = "DOI_IHM_010";
    public static final String DOI_IHM_010_NAME = "IHM Web";     
    
    public static final String DOI_IHM_020 = "DOI_IHM_020";
    public static final String DOI_IHM_020_NAME = "IHM de création d’un DOI";     
    
    public static final String DOI_IHM_030 = "DOI_IHM_030";
    public static final String DOI_IHM_030_NAME = "IHM de génération de métadonnées";    

    public static final String DOI_IHM_040 = "DOI_IHM_040";
    public static final String DOI_IHM_040_NAME = "IHM par service REST";     
    
    public static final String DOI_IHM_050 = "DOI_IHM_050";
    public static final String DOI_IHM_050_NAME = "IHM d’admin";       
    
    
    
    public static final String DOI_AUTH_010 = "DOI_AUTH_010";
    public static final String DOI_AUTH_010_NAME = "Authentification par login/mot de passe";      
    
    public static final String DOI_AUTH_020 = "DOI_AUTH_020";
    public static final String DOI_AUTH_020_NAME = "Authentification par token";     
    
    public static final String DOI_AUTH_030 = "DOI_AUTH_030";
    public static final String DOI_AUTH_030_NAME = "Authentification IHM";      
    
    
    
    public static final String DOI_AUTO_010 = "DOI_AUTO_010";
    public static final String DOI_AUTO_010_NAME = "Association des projets";     
    
    public static final String DOI_AUTO_020 = "DOI_AUTO_020";
    public static final String DOI_AUTO_020_NAME = "Sélection du rôle";         
    
    public static final String DOI_AUTO_030 = "DOI_AUTO_030";
    public static final String DOI_AUTO_030_NAME = "Vérification du projet";       
    
    
    public static final String DOI_DOC_010 = "DOI_DOC_010";
    public static final String DOI_DOC_010_NAME = "Documentation des interfaces";         
    
    public static final String DOI_DEV_010 = "DOI_DEV_010";
    public static final String DOI_DEV_010_NAME = "Développement en Java avec Maven";    
    
    public static final String DOI_DEV_020 = "DOI_DEV_020";
    public static final String DOI_DEV_020_NAME = "OpenSource";      
    
    public static final String DOI_DEPL_010 = "DOI_DEPL_010";
    public static final String DOI_DEPL_010_NAME = "Déploiement du serveur central";      
    
    public static final String DOI_CONFIG_010 = "DOI_CONFIG_010";
    public static final String DOI_CONFIG_010_NAME = "Configuration du serveur central";    
    
    public static final String DOI_CONFIG_020 = "DOI_CONFIG_020";
    public static final String DOI_CONFIG_020_NAME = "Evolutivité du schéma de métadonnées";     
    
    public static final String DOI_PERF_010 = "DOI_PERF_010";
    public static final String DOI_PERF_010_NAME = "Nombre de projets";    
    
    public static final String DOI_PERF_020 = "DOI_PERF_020";
    public static final String DOI_PERF_020_NAME = "Temps de création d’un DOI";     
    
    public static final String DOI_PERF_030 = "DOI_PERF_030";
    public static final String DOI_PERF_030_NAME = "Création de DOI en parallèle";       
    
    public static final String DOI_DISPO_010 = "DOI_DISPO_010";
    public static final String DOI_DISPO_010_NAME = "Taux de disponibilité";     
    
    public static final String DOI_DISPO_020 = "DOI_DISPO_020";
    public static final String DOI_DISPO_020_NAME = "Vérification des landing pages";      
    
    public static final String DOI_LIBCLI_010 = "DOI_LIBCLI_010";
    public static final String DOI_LIBCLI_010_NAME = "Template de landing page";     
    
    public static final String DOI_LIBCLI_020 = "DOI_LIBCLI_020";
    public static final String DOI_LIBCLI_020_NAME = "Librairies clientes en Java et Python";     
}

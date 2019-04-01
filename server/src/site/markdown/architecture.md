# Architecture


# Table of contents
1. [Introduction](#introduction)
    1. [Purpose](#purpose)
    2. [Scope](#scope)
    3. [Glossary](#definitions)
    4. [Overview](#overview)
2. [Architectural requirements](#arch_rep)
3. [Architectural Goals and Constraints](#arch_const)
    1. [License](#arch_license)
    2. [Languages](#arch_lang)
    3. [Persistence](#arch_persis)
    4. [Authentication](#arch_auth)
    5. [Changes](#arch_changes)
    6. [Third part API](#arch_third)
    7. [Security](#arch_secu)
    8. [Availability](#arch_avail)
    9. [Performance](#arch_perfo)
4. [Use case view](#use_case)
    1. [Actors](#use_case_actors)
    2. [Use-Case Realizations](#use_case_real)
        1. [Login](#use_case_login)
        2. [Create DOI](#use_case_create_doi)
        3. [Update DOI](#use_case_update_doi)
        4. [Delete DOI](#use_case_delete_doi)
5. [Logical views](#logical)
    1. [Overview](#logical_overview)
    2. [Interface definitions](#logical_interfaces)
        1. [Programmatic interfaces](#programmatic_interfaces")
            1. [The interface AbstractProjectSuffixDBHelper](#p_int)
            2. [The interface AbstractUserRoleDBHelper](#u_int)
            3. [The interface AbstractTokenDBHelper](#t_int)
            4. [The interface IAuthenticationDBHelper](#a_int)
        2. [REST API](#rest_interfaces)
        3. [The client libraries of the DOI-server](#client_lib)
        4. [GUI](#gui)
            1. [Authentication](#gui_auth)
            2. [Creating a DOI](#gui_creating_doi)
            3. [Disabling a DOI](#gui_disabling_doi)
            4. [Citation](#gui_citation_doi)
            5. [Administration](#gui_admin_doi)            
6. [Data view](#data_view)
    1. [Data model](#data_view_model)
    2. [Security](#data_view_security)
        1. [Security for Citations](#data_view_citations)
        2. [Security for MDS](#data_view_mds)
        3. [Security for Admin](#data_view_admin)
        4. [Login/password in the configuration file](#data_view_login_pwd)
        5. [Crypt/Uncrypt the whole configuration file](#data_view_crypt)        
7. [Deployment View](#deployment)
8. [Development and Test Factors](#dev_test)
    1. [Hardware Limitations](#hard_limit)
    2. [Software validation and verification](#software validation)
9. [Notes](#notes)
[Appendix A : Use Case template](#appendix_A)


## 1. Introduction <a name="introduction"/>
This document provides a high level overview and explains the architecture of DOI-server system.

The document defines goals of the architecture, the use cases supported by the system, architectural styles and components that have been selected. The document provides a rationale for the architecture and design decisions made from the conceptual idea to its implementation.

### 1.1 Purpose <a name="purpose"/>
This document provides a comprehensive architectural overview of the system, using a number of different architectural views to depict different aspects of the system. It is intended to capture and convey the significant architectural decisions which have been made on the system.

### 1.2 Scope <a name="scope"/>
The scope of this SAD is to explain the architecture of the DOI-server system.

This document describes the various aspects of the DOI-server system design that are considered to be architecturally significant. These elements and behaviors are fundamental for guiding the construction of the DOI-server system and for understanding this project as a whole. Stakeholders who require a technical understanding of the DOI-server system are encouraged to start by reading the Software Requirements Specification documents developed for this system.

### 1.3 Definitions, Acronyms, and Abbreviations  <a name="definitions"/>
* SAD - Software Architecture Document
* GUI - General User Interface
* DOI - Digital Object Identifier - permanent identifier having this syntax ```doi://<institute prefix>/<project indentifier>/<unique idenfier within a project>```
* MDS - Datacite Metadata Store - External service that stores and registers the DOI
* Citation - External services that creates the citation based on the DOI metadata

### 1.4 Overview  <a name="overview"/>
In order to fully document all the aspects of the architecture, the Software Architecture Document contains the following subsections.

 - Section 2: describes the use of each view
 - Section 3: describes the architectural goals and constraints of the system 
 - Section 4: describes the most important use-case realizations
 - Section 5: describes logical view of the system including interface and operation definitions.
 - Section 6: describes the data view.
 - Section 7: describes how the system will be deployed.
 - Section 8: describes the development and tests factors.

## 2. Architectural Representation  <a name="arch_rep"/>
This document details the architecture using views. The views used to document the DOI-server system are:

### Use Case view
> **Audience:** all the stakeholders of the system, including the end-users.

> **Area:** describes the set of scenarios and/or use cases that represent some significant, central functionality of the system. Describes the actors and use cases for the system, this view presents the needs of the user and is elaborated further at the design level to describe discrete flows and constraints in more detail. This domain vocabulary is independent of any processing model or representational syntax (i.e. XML).

> **Related Artifacts:** Use-Case Model, Use-Case documents

### Logical view
> **Audience:** Designers.

> **Area:** Functional Requirements: describes the design's object model. Also describes the most important use-case realizations and business requirements of the system including interface and operation definitions.

> **Related Artifacts:** Design model

### Data view
> **Audience:** Data specialists, Database administrators

> **Area:** Persistence: describes the architecturally significant persistent elements in the data model as well as how data flows through the system.

> **Related Artifacts:** Data model.

### Deployment view
> **Audience:** Deployment managers.

> **Area:** Topology: describes the mapping of the software onto the hardware and shows the system's distributed aspects. Describes potential deployment structures, by including known and anticipated deployment scenarios in the architecture we allow the implementers to make certain assumptions on network performance, system interaction and so forth.

> **Related Artifacts:** Deployment model.

## 3. Architectural Goals and Constraints <a name="arch_const"/>
This section describes the software requirements and objectives that have some significant impact on the architecture

### 3.1 License <a name="arch_license"/>
The DOI-server will be open source under the LGPLV3 license.

### 3.2 Languages <a name="arch_lang"/>
The DOI-server will be written in JAVA, the GUI in HTML/JavaScript and the different client libraries to consume the DOI-server API will be written in JAVA and python

### 3.3 Persistence <a name="arch_persis"/>
Data persistence will be addressed using any relational databases. By default, an implementation of PostgreSQL database will be used.

### 3.4 Authentication <a name="arch_auth"/>
The authentication will be addressed using an LDAP by default. Others systems could be used. 
Authentication could be performed by providing a username/password or a token. A token could be revoked by the registered user or the amdinistrator.

### 3.5 Changes <a name="arch_changes"/>
The Software Requirements Specification outlines a number of anticipated changes that the application could face over time.  One of the primary goals of the system architecture is to minimize the impact of these changes by minimizing the amount of code that would need to be modified to implement them.  The architecture seeks to do this through the use of modularization and information hiding to isolate components that are likely to change from the rest of the system.

### 3.6 Third part API <a name="arch_third"/>
The system must communicate with multiple third-party APIs: MDS, Cross-Cite and DataCiteSearch

### 3.7 Security <a name="arch_secu"/>
The system must be secured, so that a non allowed user cannot create/update DOI

The system will be interfaced to a proxy in some deployment cases.

The application must implement basic security behaviors:

1. Authentication: Login using at least a user name and a password
2. Authorization: according to their profile, online user must be granted or not to perform some specific actions (Create users, creates projets, create DOI)

### 3.8 Availability <a name="arch_avail"/>
Targeted availability of the DOI-server is 90%. The DOI-server checks the availability of the landing pages. 

### 3.9 Performance <a name="arch_perfo"/>
The DOI-server is able to create 1000 projects whithout degrading the perfomances. The DOI-server is able to create one DOI metadata under one second. The DOI-server is able to create one hundred DOI metadata under five seconds.

## 4. Use case view <a name="use_case"/>
The purpose of the use-case view is to give additional context surrounding the usage of the system and the interactions between its components.  For the purposes of this document, each component is considered a use-case actor.  Section 4.1 lists the current actors and gives a brief description of each in the overall use context of the system.  In section 4.2, the most significant use-cases are outlined and illustrated using UML use-case diagrams and sequence diagrams to clarify the interactions between components.

### 4.1 Actors <a name="use_case_actors"/>

Three actors handle the DOI-server:

![actors](images/actors.png)

> - **Public user** : The public user can read all resources of MDS for any projects as well as Citations. He can also get access to some resources from admin application (GUI, list of created projects, list of registred DOI on DataCite)

> - **Registerd user** : This registered user has the rights of the public user. For a specific project, he can create/update/delete a DOI and he can generate a token

> - **Administrator** : This administrator has the rights of a registered user and some extra rights as adding/removing user from a project, adding/removing a project, generating a token for a registered user

### 4.2 Use-Case Realizations <a name="use_case_real"/>

#### 4.2.1 Login <a name="use_case_login"/>
User credentials are authenticated and user is redirected to application home page with more or less features according to the user rights

> #### Actors
> > public user

> #### Priority
> > Critical

> #### Preconditions
> > The user loads the authenticate.html page


> #### Basic Course
> > - The page sends the credentials to the admin application
> > - The router of the admin application valids the credentials
> > - The router calls the resource AuthenticationResource
> > - The AuthenticationResource generates the token and sends back the token to the GUI
> > - The authenticate.html page stores the token on the session and redirects on the right page
> > - The authenticate.html page asks to the server the list of projects related to the user
> > - The server returns the list of projects
> > - The authenticate.html page stores the list of projects.
> > - The authenticate.htlm page asks to the server if the user is an admininstrator
> > - The server returns true
> > - The authenticate.html page stores true.
> > - The authenticate.html redirects to the doiCreation.html page and get access to the administrator menu

> #### Alternate Course
> > - The page sends the credentials to the admin application
> > - The router of the admin application valids the credentials
> > - The router calls the resource AuthenticationResource
> > - The AuthenticationResource generates the token and sends back to the GUI the token
> > - The authenticate.html page stores the token on the session and redirects on the right page
> > - The authenticate.html page asks to the server the list of projects related to the user
> > - The server returns the list of projects
> > - The authenticate.html page stores the list of projects.
> > - The authenticate.htlm page asks to the server if the user is an admininstrator
> > - The server returns false
> > - The authenticate.html page stores false.
> > - The authenticate.html redirects to the doiCreation.html page and get access to the project and the administrator menu

> #### Alternate Course
> > - The page sends the credentials to the admin application
> > - The router of the admin application valids the credentials
> > - The router calls the resource AuthenticationResource
> > - The AuthenticationResource generates the token and sends back to the GUI the token
> > - The authenticate.html page stores the token on the session and redirects on the right page
> > - The authenticate.html page asks to the server the list of projects related to the user
> > - The server returns an empty list of projects
> > - The authenticate.html page stores the empty list.
> > - The authenticate.htlm page asks to the server if the user is an admininstrator
> > - The server returns false
> > - The authenticate.html page stores false.
> > - The authenticate.html sends an error saying that the user is not linked to a project

> #### Alternate Course
> > - The page sends the credentials to the admin application
> > - The router of the admin application valids the credentials
> > - The router calls the resource AuthenticationResource
> > - The AuthenticationResource generates the token and sends back to the GUI the token
> > - The authenticate.html page stores the token on the session and redirects on the right page
> > - The authenticate.html page asks to the server the list of projects related to the user
> > - The server returns an empty list of projects
> > - The authenticate.html page stores the empty list.
> > - The authenticate.htlm page asks to the server if the user is an admininstrator
> > - The server returns true
> > - The authenticate.html page stores true.
> > - The authenticate.html redirects to the administration.html page and get access only to the administrator menu

> #### Exception Course
> > - The server does not accept the credentials.
> > - The GUI remains on authenticate.html page and display the error

> #### Postconditions
> > The user is located on the doiCreation.html or on the administration.html

> #### Notes¶



#### 4.2.2 Create DOI <a name="use_case_create_doi"/>
> Publish a DOI on DATACITE

> #### Actors
> > Registered user or administrator

> #### Priority
> > Critical

> #### Preconditions
> > The user is on the doiCreation.html page

> #### Basic Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page send the XML metadata and the token to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the XML metadata to MDS Datacite
> > - MDS accepts the metadata
> > - MDS application returns to the doiCreation.hml page that the metadata was accepted
> > - doiCreation.hml confirms to the user that the metadata is uploaded.
> > - doiCreation.html sends the DOI and the landing page URL to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the DOI and the landing page URL to MDS Datacite
> > - MDS Datacite accepts the request
> > - MDS application returns to the doiCreation.hml page that the DOI is created.
> > - doiCreation.hml confirms to the user that the DOI is created. 

> #### Exception Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page send the XML metadata and the token to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the XML metadata to MDS Datacite
> > - MDS accepts the metadata
> > - MDS application returns to the doiCreation.hml page that the metadata was accepted
> > - doiCreation.hml confirms to the user that the metadata is uploaded.
> > - doiCreation.html sends the DOI and the landing page URL to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the DOI and the landing page URL to MDS Datacite
> > - MDS Datacite refuse the request
> > - MDS application returns to the doiCreation.hml page that the DOI is not created.
> > - doiCreation.hml confirms to the user that the DOI is not created. 

> #### Exception Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page send the XML metadata and the token to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the XML metadata to MDS Datacite
> > - MDS accepts the metadata
> > - MDS application returns to the doiCreation.hml page that the metadata was accepted
> > - doiCreation.hml confirms to the user that the metadata is uploaded.
> > - doiCreation.html sends the DOI and the landing page URL to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application does not allow to create the DOI for this project
> > - doiCreation.hml confirms to the user that the DOI is not created. 

> #### Exception Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page send the XML metadata and the token to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application checks if the user is allowed to create the DOI for this project
> > - MDS application sends the XML metadata to MDS Datacite
> > - MDS refuses the metadata
> > - MDS application returns to the doiCreation.hml page that the metadata was refused
> > - doiCreation.hml confirms to the user there is an error.

> #### Exception Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page sends the XML metadata and the token to MDS application
> > - MDS application authentifies the user by the token
> > - MDS application does not allow to create the DOI for this project (the project suffix in the DOI number is not mapped to the user)
> > - doiCreation.hml confirms to the user there is an error.

> #### Exception Course
> > - The user selects a project for which he wants to create a DOI
> > - The user fills the form to create the DOI metadata.
> > - When filling the form, the XML representation is displayed closed to the form
> > - The user fills the landing page field.
> > - The user click on **Create DOI**
> > - The doiCreation.html page sends the XML metadata and the token to MDS application
> > - MDS application cannot authentify the user by the token

> #### Postconditions
> > The DOI metadata has been uploaded in DATACITE and the mapping DOI/landing page is uploaded to DATACITE

> #### Notes¶

#### 4.2.3 Update DOI <a name="use_case_update_doi"/>
> Updates DOI metadata or landing page.

> #### Actors
> > Registered user related to a project

> #### Priority
> > Critical

> #### Preconditions
> > The user is loggued on the doiModification.html page

> #### Basic Course
> > - The user selects a project
> > - The user clicks on the select field
> > - The GUI requests all DOI related to this DOI
> > - The server returns to the GUI the list of the DOI
> > - The user selects a DOI
> > - The DOI metadata and the landing page is loaded on the GUI
> > - Then same as **Creating a DOI**

> #### Alternate Course
> > Step-by-step description of the alternate course

> #### Exception Course
> > Step-by-step description of the exception course

> #### Postconditions
> > The landing page or/and DOI metadata have been updated on DataCite

> #### Notes¶

#### 4.2.4 Disable DOI <a name="use_case_delete_doi"/>
> Updates DOI metadata or landing page.

> #### Actors
> > Registered user related to a project

> #### Priority
> > Critical

> #### Preconditions
> > The user is loggued on the doiDeactivation.html page

> #### Basic Course
> > - The user selects a project
> > - The user clicks on the select field
> > - The GUI requests all DOI related to this DOI
> > - The server returns to the GUI the list of the DOI
> > - The user selects a DOI
> > - The user must rewrite the DOI to make it disable

> #### Exception Course
> > DataCite WS is down

> #### Postconditions
> > The DOI is disabled on DataCite

> #### Notes¶


## 5. Logical view <a name="logical"/>

### 5.1 Overview <a name="logical_overview"/>
The main goal of the logical view is to define the components that will make up the system and to define the interfaces through which they will communicate and interact with one another.  The primary decision-making factor behind defining the system components is the need to isolate the components that are likely to change from the rest of the system.  By clearly defining the interfaces of these components and hiding their internal implementations from the rest of the system, the impact of expected changes can be minimized.  The Software Requirements Specification outlines the changes that are likely to be made to the system.  A summary of these changes and how the logical decomposition of the architecture addresses them is as follows:

**1. Changes to the data persitance component**
> The architecture addresses this by implementing the calls to the persistance database in three components : one for the user management, one for the project management and the last one for token management. The rest of the application will communicate with the persistance database only through the interface exposed by these three components. Therefore any changes to the system to deal with changes the persistance database technology only be made in the internal implementation of this three components. The frequency of this chnages could be high because it depends on the common services if the infrastructure on which the DOI-server is deployed. That's why the implementation of the interface is a plugin.

**2. Changes to the authentication component**
> Similar to the above, this is addressed by implementing calls to the LDAP API in an authentication component.  Changes required to deal with changes to the LDAP API need only be made in the internal implementation of this component and not to the rest of the system. The changes frequency of this chnages could be high because it depends on the common services if the infrastructure on which the DOI-server is deployed. That's why the implementation of the interface is a plugin.

**3. Changes to the MDS API**
> Similar to the above, this is addressed by implementing calls to the MDS API in a MDS Client component.  Changes required to deal with changes to the MDS API need only be made in the internal implementation of this component and not to the rest of the system. The changes frequency should be low. No plugin is provided.

**4. Changes to Cross-cite API**
> Similar to the above, this is addressed by implementing calls to the Cross-cite API in a Cross-cite Client component.  Changes required to deal with changes to the Cross-cite API need only be made in the internal implementation of this component and not to the rest of the system. The changes frequency should be low. No plugin is provided.

**5. Changes to DataCite Search API**
> Similar to the above, this is addressed by implementing calls to the DataCiteSearch API in a DataCiteSearch Client component.  Changes required to deal with changes to the DataCiteSearch API need only be made in the internal implementation of this component and not to the rest of the system. The chnages frequency should be low. No plugin is provided.

**6. Changes to the DataCite schema.**
> The architecture addresses at the server side this by generating the classes from the XSD using JAXB. The generation of the new classes will not have impact on the rest of the system. From the client side, the changes on the schema will have a small impact on the GUI because the GUI represents each element of the XSD as fields so that the user can fill them. The changes frequency should be high. We could implement the changes at the server side by a plugin to avoid to recompile/deploy the server. However, we do not have implemented a solution to adapt automatically the GUI to the new schema. Therefore, if a changes happens, we need to modify the client. It means we need to reploy the server. That's why we do not have taken the decision to provide a plugin at the server side to handle the changes.

![logical view](images/logicalView.png)


| Element                 | Responsibilities                                                                                                  |
| :---------------------- | :---------------------------------------------------------------------------------------------------------------- |
| ClientMDS               | - Handle all communication with the MDS WS                                                                        |
|                         | - Interacts with the Proxy                                                                                        |
| ClientCrossCiteCitation | - Handle all communication with Cross-Cite WS                                                                     |
|                         | - Interacts with the Proxy                                                                                        |
| ClientSearchDataCite    | - Handle all communication with SearchDataCite WS                                                                 |
|                         | - Interacts with the Proxy                                                                                        |
| ClientLandingPage       | Handle all communication with landing pages                                                                       |
|                         | - Interacts with the Proxy                                                                                        |
| EmailSettings           | Handle all communication with the mail server                                                                     |
| LandingPageMonitoring   | - Provides an interface to run asynchronously a periodical task to monitor the availaility of landing pages       |
|                         | - Interacts with ClientLandingPage to check the availability of landing pages                                     |
|                         | - Interacts with EmailSettings to send a notification about off-line landing pages                                |
|                         | - Interacts with IAuthenticationDBHelper to find the registered users related to each project so that an notification is sent to them when an off-line landing page is found |
|                         | - Interacts with the Proxy                                                                                        |
| DOIUserUpdate           | - Provides an interface to run asynchronously a periodical task to save the updated list coming from AutenticationDBHelper |
|                         | - Interacts with IAuthenticationDBHelper to get the list of DOI-server users                                      |
|                         | - Interacts with AbstractUserRoleDBHelper to update the list of  DOI-server users in the database                 |
| UpdateTokenDB           | - Provides an interface to run asynchronously a periodical task to delete expired tokens                          |
|                         | - Interacts with AbstractTokenDBHelper to remove the expired tokens                                               |
| AbstractProjectSuffixDBHelper  | Provides an interface to manage projects in the persistant database                                        |
| AbstractTokenDBHelper   | Provides  an interface to manage token in the persistent database                                                 |
| AbstractUserRoleDBHelper | Provides an interface to manage users in the persistant database                                                 |
| IAuthenticationDBHelper | Provides an interface to manage authentication                                                                    |
| AdminApplication        | - Provides the WS API for handling administration resources                                                       |
|                         | - Interacts with AbstractProjectSuffixDBHelper to manage projects                                                 |
|                         | - Interacts with AbstractTokenDBHelper to manage tokens                                                           |
|                         | - Interacts with AbstractUserRoleDBHelper to manage users                                                         |
|                         | - Interacts with LandingPageMonitoring                                                                            |
|                         | - Interacts with DOIUserUpdate                                                                                    |
|                         | - Interacts with UpdateTokenDB                                                                                    |
| MDSApplication          | - Provides the WS API for handling DOI                                                                            |
|                         | - Interacts with ClientMDS to query MDS WS                                                                        |
|                         | - Interacts with AbstractProjectSuffixDBHelper to check the fine-grained authorization when a registerd user updates/adds a new DOI |
| CrossCiteApplication    | - Provides the WS API for DOI citation                                                                            |
|                         | - Interacts with ClientCrossCiteCitation to query the CrossCite WS                                                |
| Web Portal | - Present users with an HTML-based user interface accessible through a web browser                                             |
|            | - Interacts with other components in the system to allow users to authenticate with DOI-server, choose a DOI project for creating/deleting/updating DOI for the chosen project. |
| Router     | - Use the authentication and a global authorization to filter requests                                                         | 
|            | - Interacts with applications                                                                                                  |
|            | - Interacts with IAuthenticationDBHelper to authenticate user before accessing to applications                                 |
|            | - Interacts with AbstractProjectSuffixDBHelper and AbstractUserRoleDBHelper to create REALM for defining authorizations        |
|            | - Interacts with AbstractTokenDBHelper to remove expired tokens when a user uses an authentication by token                    |
             

### 5.2 Interface definitions <a name="logical_interfaces"/>

#### 5.2.1 Programmatic interfaces <a name="programmatic_interfaces"/>

##### 5.2.1.1 The interface AbstractProjectSuffixDBHelper <a name="p_int"/>

The interface AbstractProjectSuffixDBHelper:

```
    public abstract class AbstractProjectSuffixDBHelper extends Observable {
    
        /**
         * Notification message when the record is added {@value #ADD_RECORD}.
         */
        public static final String ADD_RECORD = "ADD";
    
        /**
         * Notification message when the record is added {@value #DELETE_RECORD}.
         */
        public static final String DELETE_RECORD = "DELETE";
    
        /**
         * Notification message when the record is added {@value #RENAME_RECORD}.
         */
        public static final String RENAME_RECORD = "RENAME";
    
        /**
         * Sets the configuration.
         *
         * @param configuration configuration parameters
         */
        public abstract void setConfiguration(final Object configuration);
    
        /**
         * Adds a suffix project in the database.
         *
         * @param projectID suffix project
         * @param projectName project name
         * @return True when the suffix project is added otherwise False
         */
        public abstract boolean addProjectSuffix(int projectID, String projectName);
    
        /**
         * Rename a project in the database.
         *
         * @param projectID suffix project to be renamed
         * @param newProjectName the new project name
         * @return True when the project has been renamed otherwise False
         */
        public abstract boolean renameProject(int projectID, String newProjectName);
    
        /**
         * Deletes a suffix project from the database.
         *
         * @param projectID the suffix project
         * @return True when the suffix project has been deleted otherwise False
         */
        public abstract boolean deleteProject(int projectID);
    
        /**
         * Tests is a suffix project exists in the database.
         *
         * @param projectID suffix project
         * @return True when the suffix project exists otherwise False
         */
        public abstract boolean isExistID(int projectID);
    
        /**
         * Tests is the project name exists in the database.
         *
         * @param projectName the project name
         * @return True when the project name exists otherwise False.
         */
        public abstract boolean isExistProjectName(String projectName);
    
        /**
         * Returns the project name based on the suffix project.
         *
         * @param projectID the suffix project
         * @return the project name
         * @throws fr.cnes.doi.exception.DOIDbException when an error occurs
         */
        public abstract String getProjectFrom(int projectID) throws DOIDbException;
    
        /**
         * Returns the project suffix based on the project name.
         *
         * @param projectName the project name
         * @return the suffix project
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
         */
        public abstract int getIDFrom(String projectName) throws DOIDbException;
    
        /**
         * Returns the database records.
         *
         * @return the database records
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
         */
        public abstract List<DOIProject> getProjects() throws DOIDbException;
    
        /**
         * Returns the projects related to a specific user.
         *
         * @param userName username
         * @return the projected to an user
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
         */
        public abstract List<DOIProject> getProjectsFromUser(final String userName) throws DOIDbException;
    
        /**
         * Returns the users related to a project.
         *
         * @param doiSuffix project
         * @return the users
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs.
         */
        public abstract List<DOIUser> getAllDOIUsersForProject(final int doiSuffix) throws DOIDbException;
    
    }
```

##### 5.2.1.2 The interface AbstractUserRoleDBHelper <a name="u_int"/>

The interface AbstractUserRoleDBHelper:

```
    public abstract class AbstractUserRoleDBHelper extends Observable {
    
        /**
         * Notification message when an user is added {@value #ADD_USER_NOTIFICATION}.
         */
        public static final String ADD_USER_NOTIFICATION = "AddUserNotification";
    
        /**
         * Notification message when an user is deleted {@value #REMOVE_USER_NOTIFICATION}.
         */
        public static final String REMOVE_USER_NOTIFICATION = "RemoveUserNotification";
    
        /**
         * Realm.
         */
        private static final MyMemoryRealm REALM = new MyMemoryRealm();
    
        /**
         * Sets the configuration.
         *
         * @param configuration configuration parameters
         */
        public abstract void setConfiguration(final Object configuration);
    
        /**
         * Returns the realm.
         *
         * @return the realm
         */
        public MyMemoryRealm getRealm() {
            return REALM;
        }
    
        /**
         * Returns the allowed users for authentication.
         *
         * @return List of users to add for the authentication
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
         */
        public abstract List<DOIUser> getUsers() throws DOIDbException;
    
        /**
         * Get users from a specific role.
         *
         * @param roleName role name
         * @return The users related to a specific role
         * @throws fr.cnes.doi.exception.DOIDbException When an error occurs
         */
        public abstract List<DOIUser> getUsersFromRole(final int roleName) throws DOIDbException;
    
        /**
         * Adds an user to a specific role.
         *
         * @param user user to add
         * @param role role
         * @return True when the user is added otherwise False
         */
        public abstract boolean addUserToRole(final String user, final int role);
    
        /**
         * Removes an user from a specific role.
         *
         * @param user user to remove
         * @param role role
         * @return True when the user is removed otherwise False
         */
        public abstract boolean removeUserToRole(final String user, final int role);
    
        /**
         * Add user to Administrators group
         *
         * @param admin user to add
         * @return True when the user is added in the admin group otherwise False
         */
        public abstract boolean setUserToAdminGroup(final String admin);
    
        /**
         * Remove user to Administrators group
         *
         * @param admin user to add
         * @return True when the user is removed from the admin group otherwise False
         */
        public abstract boolean unsetUserFromAdminGroup(final String admin);
    
        /**
         * Add a DOI user
         *
         * @param username username
         * @param admin True when the user must be added in the admin group otherwise False
         * @return True when the user is added otherwise False
         */
        public abstract boolean addDOIUser(final String username, final Boolean admin);
    
        /**
         * Add a DOI user
         *
         * @param username username
         * @param admin True when the user must be added in the admin group otherwise False
         * @param email email
         * @return True when the the user is added otherwise false
         */
        public abstract boolean addDOIUser(final String username, final Boolean admin, final String email);
    
        /**
         * Tests if the user exists.
         *
         * @param username the user
         * @return True when the user exists otherwise False
         */
        public abstract boolean isUserExist(final String username);
    
        /**
         * Tests is the user is an administrator.
         *
         * @param username user
         * @return True when the user is an administrator otherwise False
         */
        public abstract boolean isAdmin(final String username);
    
        /**
         * Removes the user
         *
         * @param username user
         * @return True when the user is removed otherwise false
         */
        public abstract boolean removeDOIUser(final String username);
    
    }
```

##### 5.2.1.3 The interface AbstractTokenDBHelper <a name="t_int"/>
The interface AbstractTokenDBHelper:

```
    public abstract class AbstractTokenDBHelper extends Observable {
    
        /**
         * Sets the configuration.
         *
         * @param configuration configuration parameters
         */
        public abstract void setConfiguration(final Object configuration);
    
        /**
         * Adds a token in the database
         *
         * @param jwt a token
         * @return True when the token is added to the database otherwise False
         */
        public abstract boolean addToken(String jwt);
    
        /**
         * Deletes a token from the database.
         *
         * @param jwt the token
         * @return True when the token is deletes otherwise false
         */
        public abstract boolean deleteToken(String jwt);
    
        /**
         * Tests if the token exists in the database.
         *
         * @param jwt the token
         * @return True when the token exists in the database otherwise False
         */
        public abstract boolean isExist(String jwt);
    
        /**
         * Tests if the token is expirated.
         *
         * @param jwt the token
         * @return True when the token is expirated otherwise False
         */
        public abstract boolean isExpired(String jwt);
    
        /**
         * Return the token list from database.
         *
         * @return the list of tokens
         * @throws fr.cnes.doi.exception.DOIDbException when an error occurs
         */
        public abstract List<String> getTokens() throws DOIDbException;
    }
```

##### 5.2.1.4 The interface IAuthenticationDBHelper <a name="a_int"/>

The interface to handle the authentication:
 
```
    public interface IAuthenticationDBHelper {
    
        /**
         * Returns the authentication members.
         *
         * @return the authentication members
         * @throws AuthenticationAccessException When a problem occurs
         */
        public List<AuthSystemUser> getDOIProjectMembers() throws AuthenticationAccessException;
    
        /**
         * Authenticates an user via the authentication service.
         *
         * @param login login
         * @param password password
         * @return True when the user is authenticated otherwise False.
         */
        public boolean authenticateUser(final String login, final String password);
    
        /**
         * Returns the user from the authentication access, which will be the administrator of DOI
         * server.
         *
         * @return the administrator
         */
        public String getDOIAdmin();
    
    }
```

#### 5.2.2 REST API <a name="rest_interfaces"/>

```
# YAML describing the DOI client
swagger: "2.0"

info:
  version: "1.0.0"
  title: Doi Server API
  description: Doi Server API
  contact:
    name: Atos Team
    url: 'http://atos.net/'
  license:
    name: LGPL
    url: 'https://www.gnu.org/licenses/lgpl-3.0.txt'
host: localhost:8182
basePath: /
schemes: [http,https]


tags:
 - name: "Mds"
   description: "MDS endpoints "
 - name: "Citation"
   description: "Citation endpoints"
 - name: "Admin"
   description: "Admin endpoints"

security:
 - APIKeyHeader: []
  
paths:
  /mds/dois:
    get:
      produces:
      - text/uri-list
      tags:
      - "Mds"
      description: Get all the dois
      operationId: getAllDois
      responses:
        200:
          description: All doi projects
          schema:
            type: string
        204:
          description: No Content - no DOIs founds
          
        401:
          description: Unauthorized - The request requires user authentication  
           
        500:
          description: Other type of errors
          
    
    post:
      produces:
      - application/json
      consumes:
      - application/x-www-form-urlencoded
      tags:
      - "Mds"
      description: Create a **new** doi project or update the landing page url if it the project already exists
      operationId: postDoi
      parameters:
        - name: url
          in: formData
          description: the URL of the landing page
          required: true
          type: string
        
        - name: doi
          in: formData
          description: the project suffix, which is an unique identifier within the project
          required: true
          type: string  
          
      responses:
        201:
          description: Successful doi project Creation and return short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS, ...
          schema:
            type: string
  
  /mds/metadata:
    post:
      tags:
      - "Mds"
      consumes:
      - application/xml
      description: Create or update doi project metada
      operationId: postDoiMetada
      parameters:
        - name: metada
          in: body
          description: metadata
          required: true
          schema: 
            type: string
            
      responses:
        200:
          description: short explanation of status code e.g. CREATED, HANDLE_ALREADY_EXISTS, ...
          schema:
             type: string
        
        400:
          description: Bad Request - invalid XML, wrong prefix
        
        401:
          description: Unauthorized - no login
          
        403:
          description: Forbidden - login problem or dataset belongs to another party
             
        500:
          description: Internal Server Error - server internal error  

  /mds/dois/{doi_name}:
    get:
      produces:
      - application/json
      tags:
      - "Mds"
      description: Renvoie l'url de la landing page du doi
      operationId: getLandingPageUrl
      parameters:
        - name: doi_name
          in: path
          type: string
          description: doi project name
          required: true
      responses:
        200:
          description: The landing page was retrieved successfully
          schema:
            type: string
        
        204:
          description: the DOI is known to MDS, but is not minted (or not resolvable e.g. due to handle's latency)
        
        500:
          description: Internal Server Error - server internal error 

  /mds/metadata/{doi_name}:
    get:
      tags:
      - "Mds"
      produces:
      - application/xml
      description: Renvoie les metadata d'un doi
      operationId: getAllDoiMetadata
      parameters:
       - name: doi_name
         in: path
         required: true
         type: string

      responses:
        200:
          description: Successful metadata of given doi project retrieval
          schema:
            type: string
            
        401:
          description: Unauthorized - no login
          
        403:
          description: Forbidden - login problem or dataset belongs to another party
          
        404:
          description: Not Found - DOI does not exist in our database
          
        410:
          description: Gone - the requested dataset was marked inactive
        
        500:
          description: Internal Server Error - server internal error 
         
            
    delete:
      tags:
      - "Mds"
      description: This request marks a dataset as 'inactive'
      operationId: cancelDoiProject
      parameters:
        - name: doi_name
          in: path
          type: string
          description: doi project name
          required: true
      responses:
        202:
          description: Cancelling taken into account and return metadata that was deactivated
          schema:
            type: string
          
        401:
          description: Unauthorized - no login
          
        403:
          description: Forbidden - login problem or dataset belongs to another party
          
        404:
          description: Not Found - DOI does not exist in our database
          
        500:
          description: Internal Server Error
          
            
  
  /mds/media/{doi_name}:
    get:
      produces:
      - text/uri-list
      tags:
      - "Mds" 
      description: Retuen a list of pairs of media type and URLs
      operationId: getDoiMedia
      parameters:
        - name: doi_name
          in: path
          description: DOI project name
          required: true
          type: string
          
      responses:
      
        200:
          description: Successful media retrieval          
          schema:
            type: string
            
        500:
          description: Internal Server Error    
              
    post:
      tags:
      - "Mds"
      consumes:
      - application/x-www-form-urlencoded
      description: add an association media/url
      operationId: postMedia
      parameters:
        - name: doi_name
          in: path
          description: DOI project name
          required: true
          type: string
          
        - name: data
          in: body
          description: an association types de media/url
          required: true
          schema:
            type: string
      responses:
        200:
          description: Successful adding association
          schema:
            type: string
          
  /citation/language:
    get:
      tags:
      - "Citation"
      description: Renvoie la liste des langages disponibles
      operationId: getCitationsLanguage
      responses:
        200:
          description: The languages was retrieved successfully
          schema:
            type: array
            items:
              type: string
        500:
          description: Other type of errors
             
  
  /citation/style:
    get:
      tags:
      - "Citation"
      description: Renvoie la liste des styles disponibles
      operationId: getCitationsStyle
      responses:
        200:
          description: The styles was retrieved successfully
          schema:
            type: array
            items:
              type: string
        500:
          description: Other type of errors
            
  /citation/format:
    get:
      tags:
      - "Citation"
      description: Returns the formatted citation
      operationId: getCitation
      parameters:
        - name: doi
          in: query
          description: DOI project name
          required: true
          type: string
        - name: style
          in: query
          description: style
          required: true
          type: string
          
        - name: lang
          in: query
          description: langage
          required: true
          type: string  
             
      responses:
        200:
          description: The citation was retrieved successfully
          schema:
            type: string
        500:
          description: Other type of errors
             
securityDefinitions:
  APIKeyHeader:
    type: apiKey
    in: header
    name: Authorization

```

#### 5.2.3 The client libraries of the DOI-server <a name="client_lib"/>
In addition to the DOI-server API, the clients libraries can be generated automatically based on the YAML description from the DOI-server API. For this, go to http://editor.swagger.io/ and copy and paste the YAML description in the editor. Then click on "generate client" to generate the clients that you wish.


#### 5.2.4 GUI <a name="gui"/>

##### 5.2.4.1 Authentication <a name="gui_auth"/>
A pop-up asks the user to enter their login and password to identify themselves on the DOI SERVER. If it is not part of the "Admin" group the last "Administration" tab of the main page will not be visible.
Once identified the user arrives on the main page and the first project from the list of its associated projects is automatically selected. If the user is not associated with any project, all tabs are grayed out (except "Administration" if the user is part of the "Admin" group.

##### 5.2.4.2 Creating a DOI <a name="gui_creating_doi"/>
The user can create a DOI on the currently selected project :
 - The HMI will need to check the input fields and before sending to verify that the generated xml conforms to the current metadata schema. If this is not the case, an error message will warn the user.
 - On the right side of the screen may be the XML code of the metadata currently entered.
 - A button will import or export metadata in XML format

![creating DOI](images/creating_doi.png)

##### 5.2.4.3 Disabling a DOI <a name="gui_disabling_doi"/>
The deactivation page of DOI permanently deactivates a DOI. For this the user must first enter the DOI (which must belong to the current project), then a confirmation pop-up opens, asking the user to confirm that he wants to permanently disable this DOI and him Requests to enter the complete DOI.

![disabling DOI](images/disabling_doi.png)

##### 5.2.4.4 Citation <a name="gui_citation_doi"/>
The Citation Management page generates a quote from a DOI of the current project, (calls will be handled via citation.crosscite.org)

![quoting DOI](images/citation_doi.png)


##### 5.2.4.5 Administration <a name="gui_admin_doi"/>
If the user is in the "Admin" group, he has access to the last tab, which allows to manage associations between users and projects, as well as the list of users in the Admin group:

![quoting DOI](images/admin_doi.png)

## 6. Data view <a name="data_view"/>

### 6.1 Data model <a name="data_view_model"/>

This diagram illustrates the static data structure and relationships of the main entities that will be stored by the application in its database. Each element represents a database table.  Relationship cardinality is denoted with UML multiplicity notation.

![data model](images/datamodel.png)

```

-- Table: doi_schema.t_doi_users

-- DROP TABLE doi_schema.t_doi_users;

CREATE TABLE doi_schema.t_doi_users
(
  username character varying(255) NOT NULL,
  admin boolean NOT NULL,
  email character varying(255),
  CONSTRAINT t_doi_users_pkey PRIMARY KEY (username)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE doi_schema.t_doi_users
  OWNER TO postgres;
GRANT ALL ON TABLE doi_schema.t_doi_users TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE doi_schema.t_doi_users TO doiserver;



-- Table: doi_schema.t_doi_project

-- DROP TABLE doi_schema.t_doi_project;

CREATE TABLE doi_schema.t_doi_project
(
  suffix integer NOT NULL,
  projectname character varying(1024) NOT NULL,
  CONSTRAINT t_doi_project_pkey PRIMARY KEY (suffix)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE doi_schema.t_doi_project
  OWNER TO postgres;
GRANT ALL ON TABLE doi_schema.t_doi_project TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE doi_schema.t_doi_project TO doiserver;



-- Table: doi_schema.t_doi_assignations

-- DROP TABLE doi_schema.t_doi_assignations;

CREATE TABLE doi_schema.t_doi_assignations
(
  username character varying(255) NOT NULL,
  suffix integer NOT NULL,
  CONSTRAINT t_doi_assignations_pkey PRIMARY KEY (username, suffix)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE doi_schema.t_doi_assignations
  OWNER TO postgres;
GRANT ALL ON TABLE doi_schema.t_doi_assignations TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE doi_schema.t_doi_assignations TO doiserver;



-- Table: doi_schema.t_doi_tokens

-- DROP TABLE doi_schema.t_doi_tokens;

CREATE TABLE doi_schema.t_doi_tokens
(
  token character varying(255) NOT NULL,
  CONSTRAINT t_doi_tokens_pkey PRIMARY KEY (token)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE doi_schema.t_doi_tokens
  OWNER TO postgres;
GRANT ALL ON TABLE doi_schema.t_doi_tokens TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE doi_schema.t_doi_tokens TO doiserver;

```

### 6.2 Security <a name="data_view_security"/>

#### 6.2.1 Security for Citations <a name="data_view_citations"/>
No security, that's only a query to a public web service : https://citation.crosscite.org/

#### 6.2.2 Security for MDS <a name="data_view_mds"/>
By default, the global authorization is for every users in DOI-server database.
Some exceptions at this rule are represented by the following picture:

![MDS](images/securityMds.png)

#### 6.2.3 Security for Admin <a name="data_view_admin"/>
By default, the global authorization is for the administrator.
Some exceptions at this rule are represented by the following picture:

#### 6.2.4 Login/password in the configuration file <a name="data_view_login_pwd"/>
Login/password for each external server is crypted in the configuration file by AES algorithm. The default key is defined in the code. 

![MDS](images/securityAdmin.png)

To change the default key, the DOI-server must be launched with a specific argument in the command line. The value of this argument is the 16 bits secret key (see exploitation manual).

#### 6.2.5 Crypt/Uncrypt the whole configuration file <a name="data_view_crypt"/>
The whole configuration file can be crypted/uncrypted. To active this feature, the DOI-server must launched with a specific argument in the command line (see exploitation manual)

## 7. Deployment view <a name="deployment"/>
The web application will be hosted on a single physical server.  An embedded Jetty in the application will be used. In addition, a PostgreSQL Server instance will also be hosted on the physical server to aid the application in persisting data.
The application will interface with external APIs (LDAP server, Proxy server, Mail server, MDS WS, Cross-cite WS, DataciteSearch WS, landing pages) of which the deployment scenarios are not known.

![MDS](images/design.png)

## 8. Development and Test Factors <a name="dev_test"/>
### 8.1 Hardware Limitations <a name="hard_limit"/>
The DOI-server works on JAVA 8.

### 8.2 Software validation and verification <a name="software_validation"/>
See the README in the DOI-server sources.


## Appendix A : Use Case template <a name="appendix_A"/>
### Use Case: name
> Short description

> #### Actors
> > List of Actors

> #### Priority
> > Low, Normal, Critical

> #### Preconditions
> > List of preconditions that must be fulfilled

> #### Basic Course
> > Step-by-step description of the basic course

> #### Alternate Course
> > Step-by-step description of the alternate course

> #### Exception Course
> > Step-by-step description of the exception course

> #### Postconditions
> > List of postconditions (if apply)

> #### Notes¶



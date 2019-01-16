CREATE DATABASE doidb;
\connect doidb;
CREATE TABLE T_DOI_USERS (
 username varchar(255) NOT NULL,
 admin boolean NOT NULL,
 PRIMARY KEY (username)
);

CREATE TABLE T_DOI_PROJECT (
 suffix int NOT NULL,
 projectname varchar(1024) NOT NULL,
 PRIMARY KEY (suffix) 
);

CREATE TABLE T_DOI_ASSIGNATIONS (
 username varchar(255) NOT NULL,
 suffix int NOT NULL,
 PRIMARY KEY (username, suffix)
);

CREATE TABLE T_DOI_TOKENS (
 token varchar(255) NOT NULL,
 PRIMARY KEY (token)
);

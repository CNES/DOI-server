DROP SCHEMA IF EXISTS doi_schema_test CASCADE;

-- Test database
CREATE SCHEMA doi_schema_test;
CREATE TABLE doi_schema_test.T_DOI_USERS (
 username varchar(255) NOT NULL,
 admin boolean NOT NULL,
 email varchar(255),
 PRIMARY KEY (username)
);

CREATE TABLE doi_schema_test.T_DOI_PROJECT (
 suffix int NOT NULL,
 projectname varchar(1024) NOT NULL,
 PRIMARY KEY (suffix) 
);

CREATE TABLE doi_schema_test.T_DOI_ASSIGNATIONS (
 username varchar(255) NOT NULL,
 suffix int NOT NULL,
 PRIMARY KEY (username, suffix)
);

CREATE TABLE doi_schema_test.T_DOI_TOKENS (
 token varchar(255) NOT NULL,
 PRIMARY KEY (token)
);
GRANT ALL PRIVILEGES ON SCHEMA doi_schema_test TO doiserver_test;
GRANT INSERT,DELETE,SELECT,UPDATE ON ALL TABLES IN SCHEMA doi_schema_test TO doiserver_test;

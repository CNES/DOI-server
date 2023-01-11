# Digital Object Identifier Server

A Digital Object Identifier (DOI) is an alphanumeric string assigned to uniquely identify an object. 
It is tied to a metadata description of the object as well as to a digital location, such as a URL, 
where all the details about the object are accessible.

## 1- Synopsis

This document provides the motivation of the project and the different instructions to both install
and use the DOI-Server. 

## 2- Motivation

To create a DOI, a user needs to be connected to DATACITE so that he sends the metadata and the URL 
of the landing page. Within an organization, the same password to DATACITE cannot be shared for all 
users of the organization.That's why DOI-Server has been created. It allows an user (human or 
programmatic client) to connect to a GUI (or a web service) in order to to a DOI. Each project has 
its own password to connect to DOI-Server and DOI-Server does the rest.
With the DOI-Server, some clients are also provided : a python client, a Java client and a IHM. Each 
client uses the DOI-Server web services to handle the DOI.
DOI-Server is also generic because he can host several plugins to handle the databases (projects, 
role and DOI number generation). 

## 3- Getting Started

These instructions will get you a copy of the project up and running on your local machine for 
development and testing purposes. See deployment for notes on how to deploy the project on a 
live system.

### 3.1- Prerequisities

What things you need to install the software and how to install them

```
Openjdk version 1.8
Apache Maven 3.5.2
Git version 2.17.1
PostgreSQL version 9.2
```

PostgreSQL installation

```
// installing database
yum install postgresql-server postgresql-contrib

// init database
postgresql-setup initdb

// changing authentication mode
sed -i '/\(^host.*127.0.0.1\/32\)/s/ident/md5/' /var/lib/pgsql/data/pg_hba.conf

// starting postgreSQL
systemctl start postgresql

// attaching postgreSQL at boot
systemctl enable postgresql

// changing postgres user
su postgres
psql
\password
<set the password>
\q
exit

// installing syslog
yum install rsyslog
```


### 3.2- Installing for developers

#### 3.2.1- Installing

Clone the repository

```
git clone https://github.com/CNES/DOI-server.git && cd DOI-server
git submodule init
git submodule update
```

Define the settings, which will be used for the compilation

```
mkdir $HOME/.m2
echo "
<settings>
    <servers>
        <server>
            <id>dbpostgresql</id>
            <!-- user for the Postgres database admin -->
            <username></username>
            <!-- password for the Postgres database admin -->
            <password></password>   
        </server>     
    </servers>
    
    <profiles>
        <profile>
            <id>inject-doiserver</id>
            <properties>
                <!-- password for doiserver user -->
                <doiserver-pwd></doiserver-pwd>
                <!-- password for the DOI-server admin -->
                <doi-admin-pwd></doi-admin-pwd>                
            </properties>            
        </profile>
    </profiles>
    
    <activeProfiles>
        <activeProfile>inject-doiserver</activeProfile>
    </activeProfiles>
        
</settings>
" > $HOME/.m2/settings.xml

// Defining the JAVA_HOME variable
export $JAVA_HOME="..."
export PATH=$JAVA_HOME/bin:$PATH

// compiling DOI-server and testing
cd DOI-server && mvn clean install

// Testing integration tests
mvn verify -P integration-test

// Installing
java -jar DOI-server-1.1.7.jar

```

#### 3.2.2- Running DOI-server

Creating the configuration file

```
java -Dlog4j.configurationFile=server/target/log4j2.xml -jar server/target/DOI-server-1.1.7.jar -d > doi.conf
```

Filling the configuration file 

```
vim doi.conf
```

Starting the server

```
java -Dlog4j.configurationFile=server/target/log4j2.xml -jar server/target/DOI-server-1.1.7.jar -f doi.conf --start
```

### 3.3- Installing for end-users (izpack)

#### 3.3.1- Installing

Installing the package by IzPack

```
java -jar DOI-1.1.7.jar
```

#### 3.3.2- Runnig DOI-server

Go to the installation path

```
cd $HOME/DOI
```
Running

```
./doi.sh start
```

### 3.3- Connection to the GUI

```
firefox http(s)://localhost:<port>/ihm
```

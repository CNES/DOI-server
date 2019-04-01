# Usage

## Table of contents
1. [Getting Help](#getting_help)
2. [Getting Version](#getting_version)
3. [Encrypting a password](#encrypting_password)
    1. [Encrypting the password with the default key](#encrypting_password_default_key)
    2. [Encrypting the password with a custom key](#encrypting_password_custom_key)
4. [Decrypting the password](#decrypting_password)
    1. [Decrypting the password with the default key](#decrypting_password_default_key)
    2. [Decrypting the password with a custom key](#decrypting_password_custom_key)
5. [Configuration file](#configuration_file)
6. [Log configuration file](#log_configuration_file)
7. [Starting the server](#starting_server)
    1. [Starting the server with the default key to decrypt the passwords](#starting_server_default_key)
    2. [Starting the server with a custome key to decrypt the passwords](#starting_server_custom_key)
8. [Stopping the server](#stopping_server)



## 1. Getting Help <a name="getting_help"/>

```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -h

------------ Help for DOI Server -----------

Usage: java -jar DOI-server-1.0.0-SNAPSHOT.jar [--secret <key>] [OPTIONS] [-s]


with :
  --secret <key>               : The 16 bits secret key to crypt/decrypt
  --key-sign-secret <key>      : The key to sign the token
                                 If not provided, a default one is used
  -s|--start                   : Starts the server
  -t|--stop                    : Stops the server
with OPTIONS:
  -h|--help                    : This output
  -k|--key-sign                : Creates a key to sign JWT token
  -c <string>                  : Crypts a string in the standard output
  -e <string>                  : Decrypts a string in the standard output
  -d                           : Displays the configuration file
  -f <path>                    : Loads the configuation file
  -y|--cryptProperties <path>  : crypts the properties file on the output standard
  -z|--decryptProperties <path>: Decrypts the properties on the output standard
  -v|--version                 : DOI server version

```

## 2. Getting version <a name="getting_version"/>

```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -v
DOI-server (Copyright 2017-2019 CNES) - Version:1.0.0-SNAPSHOT
```

## 3. Encrypting a password <a name="encrypting_password"/>

### 3.1 Encrypting the password with the default key <a name="encrypting_password_default_key"/>
```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -c test
7nAsnRnwzGL+v/SsnQ4rXg==
```

### 3.2 Encrypting the password with a custom key <a name="encrypting_password_custom_key"/>
```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar --secret wxcvbnqsdfg12345 -c test 
giXEr40f5832YFYgAiWMRA==
```

## 4. Decrypting a password <a name="decrypting_password"/>

### 4.1 Decrypting the password with the default key <a name="decrypting_password_default_key"/>
```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -e 7nAsnRnwzGL+v/SsnQ4rXg==
test
```

### 4.2 Decrypting the password with a custom key <a name="decrypting_password_custom_key"/>
```
malapert@heulet-HP-ZBook-15-G4:~/DOI$ java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar --secret wxcvbnqsdfg12345 -e giXEr40f5832YFYgAiWMRA==
test
```

## 5. Configuration file <a name="configuration_file"/>
See [configuration](./configuration.html)

## 6. Log configuration file <a name="log_configuration_file"/>
```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
   
	<Appenders>

    <Syslog name="syslog" format="RFC5424" host="localhost" port="514"
            protocol="UDP" appName="DOI-SERVER" includeMDC="false" mdcId="doiserver"
            facility="LOCAL0" enterpriseNumber="18060" newLine="false" 
	    messageId="Audit">
            <LoggerFields>
                  <KeyValuePair key="thread" value="%t"/>
                  <KeyValuePair key="priority" value="%p"/>
		  <KeyValuePair key="category" value="%c"/>
		  <KeyValuePair key="message" value="%m"/>
                  <KeyValuePair key="exception" value="%ex"/>
	    </LoggerFields>
                        
    </Syslog>

    <Socket name="syslogsocket" host="localhost" port="514" protocol="UDP">
          <PatternLayout
        pattern="&lt;134&gt;%d{MMM dd HH:mm:ss} ${hostName} testlog4j: {
              &quot;thread&quot;:&quot;%t&quot;,
              &quot;priority&quot;:&quot;%p&quot;,
              &quot;category&quot;:&quot;%c{1}&quot;,
              &quot;exception&quot;:&quot;%exception&quot;
              }%n"
          />
    </Socket>      

        <File name="PERFO" fileName="perfo.log" append="true">     
            <PatternLayout pattern="%-5p | %d{yyyy-MM-dd HH:mm:ss} | [%t] %C{2} (%F:%L) - %m%n"/>   
        </File>
        <File name="FILE" fileName="logfile.log" append="true">
            <PatternLayout pattern="%-5level %d{yyyy-MM-dd HH:mm:ss} %C{2} (%F:%L) - %m%n"/>
        </File>
        <File name="API" fileName="api.log" append="true">                
            <PatternLayout pattern="[%-5p - %t] %d %c - %m%n"/>
        </File>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <PatternLayout pattern="%highlight{%d [%t] %-5level}: %msg%n%throwable"/><!--%highlight{%d [%t] %-5level}: %msg%n%throwable-->
        </Console>
        <Console name="SHELL" target="SYSTEM_OUT">
            <PatternLayout pattern="%msg%n"/>
        </Console>        
    </Appenders>

    <Loggers>
     
        <Logger name="fr.cnes.doi.logging.app" level="INFO">
            <AppenderRef ref="PERFO"/>                        
        </Logger>        
        <Logger name="fr.cnes.doi.logging.api" level="INFO">
            <AppenderRef ref="API"/>                        
        </Logger>
        <Logger name="fr.cnes.doi.application" level="INFO">
            <AppenderRef ref="FILE"/>            
        </Logger>      
        <Logger name="fr.cnes.doi.logging.shell" level="INFO">
             <AppenderRef ref="FILE"/>
        </Logger>  
	<Logger name="fr.cnes.doi.server" level="INFO">
             <AppenderRef ref="syslogsocket"/>
             <AppenderRef ref="SHELL"/>
        </Logger>                    
	<Root level="INFO">
	    <AppenderRef ref="syslog"/>
            <AppenderRef ref="FILE"/>
        </Root>
    </Loggers>

</Configuration>
```

## 7. Starting the server <a name="starting_server"/>
### 7.1 Starting the server with the default key to decrypt the passwords <a name="starting_server_default_key"/>
```
java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --start
```

### 7.2 Starting the server with a custom key to decrypt the passwords <a name="starting_server_custom_key"/>
```
java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar --secret wxcvbnqsdfg12345 -f config.properties --start
```

## 8. Stopping the server <a name="stopping_server"/>
```
java -Dlog4j.configurationFile=./log4j2.xml -jar DOI-server.jar -f config.properties --stop
```


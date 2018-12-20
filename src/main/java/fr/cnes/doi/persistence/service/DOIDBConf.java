package fr.cnes.doi.persistence.service;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DOIDBConf {
	
    private Logger logger = LoggerFactory.getLogger(DOIDBConf.class);
	
	// TODO Parse properties from a property file
	private  String doidbPropertiesFileName = "doidbconf.properties";
	
	private  String doidburl;
	
	private  String user;
	
	private  String pwd;
	
	private  int minConnections;
	
	private  int maxConnections;

	public  DOIDBConf() {
		Properties prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream(doidbPropertiesFileName));
			doidburl = prop.getProperty("doidburl");
			user = prop.getProperty("user");
			pwd = prop.getProperty("pwd");
			minConnections = Integer.parseInt(prop.getProperty("minConnections"));
			maxConnections = Integer.parseInt(prop.getProperty("maxConnections"));
		} catch (IOException e) {
			logger.error("Error occured wheb loading database properties file", e);
		}
	}
	
	public String getDoidburl() {
		return doidburl;
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}

	public int getMinConnections() {
		return minConnections;
	}

	public int getMaxConnections() {
		return maxConnections;
	}
}

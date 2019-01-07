package fr.cnes.doi.persistence.service;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.util.PasswordEncrypter;

public class JDBCConnector {

	private Logger logger = LoggerFactory.getLogger(JDBCConnector.class); 
	private BasicDataSource ds = new BasicDataSource(); 
	
	public JDBCConnector() {
		init();
	}
	
	// Data source initialization
	private void init() {
		DOIDBConf conf = new DOIDBConf(); 
		ds.setUrl(conf.getDoidburl());
		ds.setUsername(conf.getUser());
		System.out.println(conf.getUser() + "<<<<<<<<<< getUser");
		System.out.println(conf.getPwd() + "<<<<<<<<<< getPWD");
		try {
			ds.setPassword(PasswordEncrypter.getInstance().decryptPasswd(conf.getPwd()));
		} catch (Exception e) {
			logger.error("Failure occored in JDBCConnector init()", e);
		}
		ds.setMinIdle(conf.getMinConnections());
		ds.setMinIdle(conf.getMaxConnections());
	}
	
	public Connection getConnection() throws SQLException {
		synchronized (this) {
			return ds.getConnection();
		}	
	}
	
}

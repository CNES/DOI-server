package fr.cnes.doi.persistence.service;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import fr.cnes.doi.persistence.util.PasswordEncrypter;

public class JDBCConnector {

	private BasicDataSource ds = new BasicDataSource(); 
	
	public JDBCConnector() {
		init();
	}
	
	// Data source initialization
	private void init() {
		DOIDBConf conf = new DOIDBConf(); 
		ds.setUrl(conf.getDoidburl());
		ds.setUsername(conf.getUser());
		try {
			ds.setPassword(PasswordEncrypter.getInstance().decryptPasswd(conf.getPwd()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

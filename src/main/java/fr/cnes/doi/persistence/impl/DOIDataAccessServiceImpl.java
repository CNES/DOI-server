package fr.cnes.doi.persistence.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.model.DOIMetadata;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDataAccessService;
import fr.cnes.doi.persistence.service.JDBCConnector;

public class DOIDataAccessServiceImpl implements DOIDataAccessService  {

	private Logger logger = LoggerFactory.getLogger(DOIDataAccessServiceImpl.class);
	private JDBCConnector dbConnector = new JDBCConnector(); 
	
	public List<DOIUser> getAllDOIusers() throws DOIDbException {
		List<DOIUser> users = new ArrayList<DOIUser>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT username, admin FROM T_DOI_USERS");
			while (rs.next()) {
				DOIUser doiuser = new DOIUser();
				doiuser.setAdmin(rs.getBoolean("admin"));
				doiuser.setUsername(rs.getString("username"));
				users.add(doiuser);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIusers",e);
			throw new DOIDbException("An exception occured when calling getAllDOIusers", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return users;
	}

	public List<DOIProject> getAllDOIProjects() throws DOIDbException {
		List<DOIProject> projects = new ArrayList<DOIProject>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT suffix, projectname FROM T_DOI_PROJECT");
			while (rs.next()) {
				DOIProject doiproject = new DOIProject();
				doiproject.setSuffix(rs.getInt("suffix"));
				doiproject.setProjectname(rs.getString("projectname"));
				projects.add(doiproject);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIProjects", e);
			throw new DOIDbException("An exception occured when calling getAllDOIProjects", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return projects;
	}

	public List<DOIProject> getAllDOIProjectsForUser(DOIUser user) throws DOIDbException {
		List<DOIProject> projects = new ArrayList<DOIProject>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement assignationsStatement =  conn.createStatement();
			ResultSet assignations = assignationsStatement.executeQuery("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE username='" + user.getUsername() + "';");
			while (assignations.next()) {
				Statement projectsStatement =  conn.createStatement();
				ResultSet rs = projectsStatement.executeQuery("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=" + assignations.getInt("suffix"));	
				while (rs.next()) {
					DOIProject doiproject = new DOIProject();
					doiproject.setSuffix(rs.getInt("suffix"));
					doiproject.setProjectname(rs.getString("projectname"));
					projects.add(doiproject);
				}
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIProjectsForUser", e);
			throw new DOIDbException("An exception occured when calling getAllDOIProjectsForUser", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return projects;
	}

	public List<DOIUser> getAllDOIUsersForProject(DOIProject project) throws DOIDbException {
		List<DOIUser> users = new ArrayList<DOIUser>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement assignationStatement =  conn.createStatement();
			ResultSet assignations = assignationStatement.executeQuery("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE suffix='" + project.getSuffix() + "'");
			while (assignations.next()) {
				Statement usersStatement =  conn.createStatement();
				ResultSet rs = usersStatement.executeQuery("SELECT username, admin FROM T_DOI_USERS WHERE username='" + assignations.getString("username") + "'");	
				while (rs.next()) {
					DOIUser doiuser = new DOIUser();
					doiuser.setUsername(rs.getString("username"));
					doiuser.setAdmin(rs.getBoolean("admin"));
					users.add(doiuser);
				}
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIUsersForProject", e);
			throw new DOIDbException("An exception occured when calling getAllDOIUsersForProject", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return users;
	}

	public void addDOIUser(String username, Boolean admin) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("INSERT INTO T_DOI_USERS (username, admin) VALUES ('" + username + "', " + admin + ")");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIUser", e);
			throw new DOIDbException("An exception occured when calling addDOIUser", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}		
	}

	public void addDOIProject(int suffix, String projectname) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("INSERT INTO T_DOI_PROJECT (suffix, projectname) VALUES (" + suffix + ", '" + projectname + "');");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProject", e);
			throw new DOIDbException("An exception occured when calling addDOIProject", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}	
	}

	public void addDOIProjectToUser(String username, int suffix) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("INSERT INTO T_DOI_ASSIGNATIONS (username, suffix) VALUES ('" + username + "', " + suffix + ");");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProjectToUser", e);
			throw new DOIDbException("An exception occured when calling addDOIProjectToUser", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}	
	}

	public void removeDOIProjectFromUser(String username, int suffix) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("DELETE FROM T_DOI_ASSIGNATIONS WHERE username = '" + username + "' AND suffix = " + suffix + ";");
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIProjectFromUser", e);
			throw new DOIDbException("An exception occured when calling removeDOIProjectFromUser", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}			
	}

	public void setAdmin(String username) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("UPDATE T_DOI_USERS SET admin = true WHERE username = '" + username + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}		
	}

	public void unsetAdmin(String username) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("UPDATE T_DOI_USERS SET admin = false WHERE username = '" + username + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}	
	}

	public void renameDOIProject(int suffix, String newprojectname) throws DOIDbException {
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			statement.executeUpdate("UPDATE T_DOI_PROJECT SET projectname = '" + newprojectname + "' WHERE suffix = " + suffix + ";");
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}			
	}

	public List<DOIMetadata> getAllDOIMetadata() throws DOIDbException {
		List<DOIMetadata> metadata = new ArrayList<DOIMetadata>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement =  conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT urlLandingPage, email FROM T_DOI_METADATA;");
			while (rs.next()) {
				DOIMetadata doimetadata = new DOIMetadata();
				doimetadata.setUrlLandingPage(rs.getString("urlLandingPage"));
				doimetadata.setEmail(rs.getString("email"));
				metadata.add(doimetadata);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIMetadata", e);
			throw new DOIDbException("An exception occured when calling getAllDOIMetadata", e);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return metadata;
	}
}

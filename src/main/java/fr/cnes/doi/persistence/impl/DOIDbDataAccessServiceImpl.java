package fr.cnes.doi.persistence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.doi.persistence.exceptions.DOIDbException;
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.persistence.service.JDBCConnector;

public class DOIDbDataAccessServiceImpl implements DOIDbDataAccessService {

	private Logger logger = LoggerFactory.getLogger(DOIDbDataAccessServiceImpl.class);
	private JDBCConnector dbConnector;

	
	
	public DOIDbDataAccessServiceImpl() {
    	dbConnector = new JDBCConnector();
    }
	
    public DOIDbDataAccessServiceImpl(String customDbConfigFile) {
    	dbConnector = new JDBCConnector(customDbConfigFile);
    }
	
	public List<DOIUser> getAllDOIusers() throws DOIDbException {
		List<DOIUser> users = new ArrayList<DOIUser>();
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("SELECT username, admin, email FROM T_DOI_USERS");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				DOIUser doiuser = new DOIUser();
				doiuser.setAdmin(rs.getBoolean("admin"));
				doiuser.setUsername(rs.getString("username"));
				doiuser.setEmail(rs.getString("email"));
				users.add(doiuser);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIusers", e);
			throw new DOIDbException("An exception occured when calling getAllDOIusers", e);
		} finally {
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
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
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("SELECT suffix, projectname FROM T_DOI_PROJECT");
			ResultSet rs = statement.executeQuery();
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
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return projects;
	}

	public List<DOIProject> getAllDOIProjectsForUser(String username) throws DOIDbException {
		List<DOIProject> projects = new ArrayList<DOIProject>();
		Connection conn = null;
		PreparedStatement assignationsStatement = null;
		PreparedStatement projectsStatement = null;
		
		try {
			conn = dbConnector.getConnection();
			assignationsStatement = conn.prepareStatement("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE username=?");
			assignationsStatement.setString(1, username);
			ResultSet assignations = assignationsStatement
					.executeQuery();
			while (assignations.next()) {
				projectsStatement = conn.prepareStatement("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
				projectsStatement.setInt(1, assignations.getInt("suffix"));
				ResultSet rs = projectsStatement.executeQuery();
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
			if (assignationsStatement!=null) {
				try {
					assignationsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (projectsStatement!=null) {
				try {
					projectsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return projects;
	}

	public List<DOIUser> getAllDOIUsersForProject(int suffix) throws DOIDbException {
		List<DOIUser> users = new ArrayList<DOIUser>();
		Connection conn = null;
		PreparedStatement assignationsStatement = null;
		PreparedStatement usersStatement = null;
		try {
			conn = dbConnector.getConnection();
			assignationsStatement = conn.prepareStatement("SELECT username, suffix, email FROM T_DOI_ASSIGNATIONS WHERE suffix=?");
			assignationsStatement.setInt(1, suffix);
			ResultSet assignations = assignationsStatement.executeQuery();
			while (assignations.next()) {
				usersStatement = conn.prepareStatement("SELECT username, admin FROM T_DOI_USERS WHERE username=?");
				usersStatement.setString(1, assignations.getString("username"));
				ResultSet rs = usersStatement.executeQuery();
				while (rs.next()) {
					DOIUser doiuser = new DOIUser();
					doiuser.setUsername(rs.getString("username"));
					doiuser.setAdmin(rs.getBoolean("admin"));
					doiuser.setEmail(rs.getString("email"));
					users.add(doiuser);
				}
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getAllDOIUsersForProject", e);
			throw new DOIDbException("An exception occured when calling getAllDOIUsersForProject", e);
		} finally {
			if (assignationsStatement!=null) {
				try {
					assignationsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (usersStatement!=null) {
				try {
					usersStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
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
		PreparedStatement usersStatement = null;
		try {
			conn = dbConnector.getConnection();
			usersStatement = conn.prepareStatement("INSERT INTO T_DOI_USERS (username, admin) VALUES(?, ?)");
			usersStatement.setString(1, username);
			usersStatement.setBoolean(2, admin);
			usersStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIUser", e);
			throw new DOIDbException("An exception occured when calling addDOIUser", e);
		} finally {
			if (usersStatement!=null) {
				try {
					usersStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
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
		PreparedStatement projectStatement = null;
		try {
			conn = dbConnector.getConnection();
			projectStatement = conn.prepareStatement("INSERT INTO T_DOI_PROJECT (suffix, projectname) VALUES(?,?)");
			projectStatement.setInt(1, suffix);
			projectStatement.setString(2, projectname);
			projectStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProject", e);
			throw new DOIDbException("An exception occured when calling addDOIProject", e);
		} finally {
			if (projectStatement!=null) {
				try {
					projectStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
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
		PreparedStatement userStatement = null;
		PreparedStatement projectStatement = null;
		PreparedStatement assignationStatement = null;
		try {
			conn = dbConnector.getConnection();
			userStatement = conn.prepareStatement("SELECT username, admin FROM T_DOI_USERS WHERE username=?");
			userStatement.setString(1, username);
			Boolean userExist = userStatement.executeQuery().next();
			projectStatement = conn.prepareStatement("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
			projectStatement.setInt(1, suffix);
			Boolean projectExist = projectStatement.executeQuery().next();
			if (!userExist || !projectExist) {
				throw new DOIDbException("An exception occured when calling addDOIProjectToUser:" + "user " + username
						+ " or project " + suffix + " don't exist in doi database", null);
			}
			assignationStatement = conn.prepareStatement("INSERT INTO T_DOI_ASSIGNATIONS (username, suffix) VALUES(?,?)");
			assignationStatement.setString(1, username);
			assignationStatement.setInt(2, suffix);
			assignationStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProjectToUser", e);
			throw new DOIDbException("An exception occured when calling addDOIProjectToUser", e);
		} finally {
			
			if (userStatement!=null) {
				try {
					userStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (projectStatement!=null) {
				try {
					projectStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (assignationStatement!=null) {
				try {
					assignationStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (conn != null) {
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
		PreparedStatement assignationsStatement = null;
		try {
			conn = dbConnector.getConnection();
			assignationsStatement = conn.prepareStatement("DELETE FROM T_DOI_ASSIGNATIONS WHERE username =? AND suffix=?");
			assignationsStatement.setString(1, username);
			assignationsStatement.setInt(2, suffix);
			assignationsStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIProjectFromUser", e);
			throw new DOIDbException("An exception occured when calling removeDOIProjectFromUser", e);
		} finally {
			if (assignationsStatement!=null) {
				try {
					assignationsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
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
		PreparedStatement userStatement = null;
		PreparedStatement updateStatement = null;
		try {
			conn = dbConnector.getConnection();
			userStatement = conn.prepareStatement("SELECT username, admin FROM T_DOI_USERS WHERE username=?");
			userStatement.setString(1, username);
			Boolean userExist = userStatement.executeQuery().next();
			if (userExist) {
				updateStatement = conn.prepareStatement("UPDATE T_DOI_USERS SET admin = true WHERE username =?");
				updateStatement.setString(1, username);
				updateStatement.executeUpdate();
			} else {
				throw new DOIDbException("An exception occured when calling setAdmin:" + "user " + username
						 + " don't exist in doi database", null);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			
			if (userStatement != null) {
				try {
					userStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}	
			}
			
			if (updateStatement != null) {
				try {
					updateStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}	
			}
			
			if (conn != null) {
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
		PreparedStatement usersStatement = null;
		try {
			conn = dbConnector.getConnection();
			usersStatement = conn.prepareStatement("UPDATE T_DOI_USERS SET admin = false WHERE username=?");
			usersStatement.setString(1, username);
			usersStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			
			if (usersStatement != null) {
				try {
					usersStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (conn != null) {
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
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("UPDATE T_DOI_PROJECT SET projectname =? WHERE suffix=?");
			statement.setString(1, newprojectname);
			statement.setInt(2, suffix);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
	}

	public String getDOIProjectName(int suffix) throws DOIDbException {
		Connection conn = null;
		String projectNameResult = null;
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
			statement.setInt(1, suffix);
			ResultSet rs = statement
					.executeQuery();
			while (rs.next()) {
				projectNameResult = rs.getString("projectname");
				break;
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getDOIProjectName", e);
			throw new DOIDbException("An exception occured when calling getDOIProjectName", e);
		} finally {
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return projectNameResult;

	}

	public void addToken(String token) throws DOIDbException {
		Connection conn = null;
		PreparedStatement statement  = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("INSERT INTO T_DOI_TOKENS (token) VALUES (?)");
			statement.setString(1, token);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling addToken", e);
			throw new DOIDbException("An exception occured when calling addToken", e);
		} finally {
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
	}

	public void deleteToken(String token) throws DOIDbException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
	        statement = conn.prepareStatement("DELETE FROM T_DOI_TOKENS WHERE token=?");
	        statement.setString(1, token);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling deleteToken", e);
			throw new DOIDbException("An exception occured when calling deleteToken", e);
		} finally {
			
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
	}

	public List<String> getTokens() throws DOIDbException {
		List<String> tokens = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("SELECT token FROM T_DOI_TOKENS");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				tokens.add(rs.getString("token"));
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getToken", e);
			throw new DOIDbException("An exception occured when calling getToken", e);
		} finally {
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
		return tokens;
	}

	public void removeDOIUser(String username) throws DOIDbException {
		Connection conn = null;
		PreparedStatement usersStatement = null;
		PreparedStatement assignationsStatement = null;
		
		try {
			conn = dbConnector.getConnection();
			usersStatement = conn.prepareStatement("DELETE FROM T_DOI_USERS WHERE username=?");
			usersStatement.setString(1, username);
			usersStatement.executeUpdate();
			assignationsStatement = conn.prepareStatement("DELETE FROM T_DOI_ASSIGNATIONS WHERE username=?");
			assignationsStatement.setString(1, username);
			assignationsStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIUser", e);
			throw new DOIDbException("An exception occured when calling removeDOIUser", e);
		} finally {
			if (usersStatement!=null) {
				try {
					usersStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (assignationsStatement!=null) {
				try {
					assignationsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}

	}

	public void addDOIUser(String username, Boolean admin, String email) throws DOIDbException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = dbConnector.getConnection();
			statement = conn.prepareStatement("INSERT INTO T_DOI_USERS (username, admin, email) VALUES(?,?,?)");
			statement.setString(1,username);
			statement.setBoolean(2,admin);
			statement.setString(3,email);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIUser", e);
			throw new DOIDbException("An exception occured when calling addDOIUser", e);
		} finally {
			
			if (statement!=null) {
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}

	}

	public void removeDOIProject(int suffix) throws DOIDbException {

		Connection conn = null;
		PreparedStatement projectStatement = null;
		PreparedStatement assignationsStatement = null;
		try {
			conn = dbConnector.getConnection();
			projectStatement = conn.prepareStatement("DELETE FROM T_DOI_PROJECT WHERE suffix=?");
			projectStatement.setInt(1, suffix);
			projectStatement.executeUpdate();
			
			assignationsStatement = conn.prepareStatement("DELETE FROM T_DOI_ASSIGNATIONS WHERE suffix =?");
			assignationsStatement.setInt(1, suffix);
			assignationsStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIProject", e);
			throw new DOIDbException("An exception occured when calling removeDOIProject", e);
		} finally {
			
			if (projectStatement!=null) {
				try {
					projectStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (assignationsStatement!=null) {
				try {
					assignationsStatement.close();
				} catch (SQLException e) {
					logger.error("Unable to close statement", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Unable to close connection to database", e);
				}
			}
		}
	}	

	@Override
	public Boolean isAdmin(String username) throws DOIDbException {
		DOIUser user = getDoiUserFromDb(username);
		if (user != null) {
			return getDoiUserFromDb(username).getAdmin();
		} else {
			return null;
		}
	}
		
	private DOIUser getDoiUserFromDb(String username) throws DOIDbException {
			Connection conn = null;
			DOIUser doiuser = null;
			try {
				conn = dbConnector.getConnection();
				PreparedStatement statement = conn.prepareStatement("SELECT username, admin, email FROM T_DOI_USERS WHERE username=?");
				statement.setString(1, username);
				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					doiuser = new DOIUser();
					doiuser.setAdmin(rs.getBoolean("admin"));
					doiuser.setUsername(rs.getString("username"));
					doiuser.setEmail(rs.getString("email"));
					break;
				}
			} catch (SQLException e) {
				logger.error("An exception occured when calling getAllDOIusers", e);
				throw new DOIDbException("An exception occured when calling getAllDOIusers", e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						logger.error("Unable to close connection to database", e);
					}
				}
			}
			return doiuser;
		}	
		
	@Override
	public boolean isUserExist(String username) throws DOIDbException {
		return getDoiUserFromDb(username)!=null;
	}	
	
}

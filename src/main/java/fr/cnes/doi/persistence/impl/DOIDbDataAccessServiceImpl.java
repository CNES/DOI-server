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
import fr.cnes.doi.persistence.model.DOIProject;
import fr.cnes.doi.persistence.model.DOIUser;
import fr.cnes.doi.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.persistence.service.JDBCConnector;

public class DOIDbDataAccessServiceImpl implements DOIDbDataAccessService {

	private Logger logger = LoggerFactory.getLogger(DOIDbDataAccessServiceImpl.class);
	private JDBCConnector dbConnector = new JDBCConnector();

	public List<DOIUser> getAllDOIusers() throws DOIDbException {
		List<DOIUser> users = new ArrayList<DOIUser>();
		Connection conn = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT username, admin, email FROM T_DOI_USERS");
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
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
		try {
			conn = dbConnector.getConnection();
			Statement assignationsStatement = conn.createStatement();
			ResultSet assignations = assignationsStatement
					.executeQuery("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE username='" + username + "';");
			while (assignations.next()) {
				Statement projectsStatement = conn.createStatement();
				ResultSet rs = projectsStatement.executeQuery(
						"SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=" + assignations.getInt("suffix"));
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
		try {
			conn = dbConnector.getConnection();
			Statement assignationStatement = conn.createStatement();
			ResultSet assignations = assignationStatement.executeQuery(
					"SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE suffix='" + suffix + "'");
			while (assignations.next()) {
				Statement usersStatement = conn.createStatement();
				ResultSet rs = usersStatement.executeQuery("SELECT username, admin, email FROM T_DOI_USERS WHERE username='"
						+ assignations.getString("username") + "'");
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate(
					"INSERT INTO T_DOI_USERS (username, admin) VALUES ('" + username + "', " + admin + ")");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIUser", e);
			throw new DOIDbException("An exception occured when calling addDOIUser", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate(
					"INSERT INTO T_DOI_PROJECT (suffix, projectname) VALUES (" + suffix + ", '" + projectname + "');");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProject", e);
			throw new DOIDbException("An exception occured when calling addDOIProject", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Boolean userExist = conn.createStatement()
					.executeQuery("SELECT username, admin FROM T_DOI_USERS WHERE username='" + username + "'").next();
			Boolean projectExist = conn.createStatement()
					.executeQuery("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=" + suffix).next();
			if (!userExist || !projectExist) {
				throw new DOIDbException("An exception occured when calling addDOIProjectToUser:" + "user " + username
						+ " or project " + suffix + " don't exist in doi database", null);
			}
			conn.createStatement().executeUpdate(
					"INSERT INTO T_DOI_ASSIGNATIONS (username, suffix) VALUES ('" + username + "', " + suffix + ");");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIProjectToUser", e);
			throw new DOIDbException("An exception occured when calling addDOIProjectToUser", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate(
					"DELETE FROM T_DOI_ASSIGNATIONS WHERE username = '" + username + "' AND suffix = " + suffix + ";");
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIProjectFromUser", e);
			throw new DOIDbException("An exception occured when calling removeDOIProjectFromUser", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Boolean userExist = conn.createStatement()
					.executeQuery("SELECT username, admin FROM T_DOI_USERS WHERE username='" + username + "'").next();
			if (userExist) {
			  Statement statement = conn.createStatement();
			  statement.executeUpdate("UPDATE T_DOI_USERS SET admin = true WHERE username = '" + username + "';");
			} else {
				throw new DOIDbException("An exception occured when calling setAdmin:" + "user " + username
						 + " don't exist in doi database", null);
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("UPDATE T_DOI_USERS SET admin = false WHERE username = '" + username + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate(
					"UPDATE T_DOI_PROJECT SET projectname = '" + newprojectname + "' WHERE suffix = " + suffix + ";");
		} catch (SQLException e) {
			logger.error("An exception occured when calling setAdmin", e);
			throw new DOIDbException("An exception occured when calling setAdmin", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix= " + suffix);
			while (rs.next()) {
				projectNameResult = rs.getString("projectname");
				break;
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getDOIProjectName", e);
			throw new DOIDbException("An exception occured when calling getDOIProjectName", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("INSERT INTO T_DOI_TOKENS (token) VALUES ('" + token + "');");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addToken", e);
			throw new DOIDbException("An exception occured when calling addToken", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("DELETE FROM T_DOI_TOKENS WHERE token = '" + token + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling deleteToken", e);
			throw new DOIDbException("An exception occured when calling deleteToken", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT token FROM T_DOI_TOKENS");
			while (rs.next()) {
				tokens.add(rs.getString("token"));
			}
		} catch (SQLException e) {
			logger.error("An exception occured when calling getToken", e);
			throw new DOIDbException("An exception occured when calling getToken", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			conn.createStatement().executeUpdate("DELETE FROM T_DOI_USERS WHERE username = '" + username + "';");
			conn.createStatement().executeUpdate("DELETE FROM T_DOI_ASSIGNATIONS WHERE username = '" + username + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIUser", e);
			throw new DOIDbException("An exception occured when calling removeDOIUser", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			statement.executeUpdate("INSERT INTO T_DOI_USERS (username, admin, email) VALUES ('" + username + "', "
					+ admin + " ,'" + email + "')");
		} catch (SQLException e) {
			logger.error("An exception occured when calling addDOIUser", e);
			throw new DOIDbException("An exception occured when calling addDOIUser", e);
		} finally {
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
		try {
			conn = dbConnector.getConnection();
			conn.createStatement().executeUpdate("DELETE FROM T_DOI_PROJECT WHERE suffix = '" + suffix + "';");
			conn.createStatement().executeUpdate("DELETE FROM T_DOI_ASSIGNATIONS WHERE suffix = '" + suffix + "';");
		} catch (SQLException e) {
			logger.error("An exception occured when calling removeDOIProject", e);
			throw new DOIDbException("An exception occured when calling removeDOIProject", e);
		} finally {
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
	public boolean isAdmin(String username) throws DOIDbException {
		return getDoiUserFromDb(username).getAdmin();
		
	}
	
	private DOIUser getDoiUserFromDb(String username) throws DOIDbException {
		Connection conn = null;
		DOIUser doiuser = null;
		try {
			conn = dbConnector.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT username, admin, email FROM T_DOI_USERS WHERE username='"+username+"';");
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

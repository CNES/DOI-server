/*
 * Copyright (C) 2017-2019 Centre National d'Etudes Spatiales (CNES).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.cnes.doi.db.persistence.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.persistence.model.DOIProject;
import fr.cnes.doi.db.persistence.model.DOIUser;
import fr.cnes.doi.db.persistence.service.DOIDbDataAccessService;
import fr.cnes.doi.db.persistence.service.JDBCConnector;

public class DOIDbDataAccessServiceImpl implements DOIDbDataAccessService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(DOIDbDataAccessServiceImpl.class.getName());
    /**
     * Connection to the DOI database.
     */
    private final JDBCConnector dbConnector;

    /**
     * Create the implementation of DOI database by creation the JDBC connection from the settings.
     */
    public DOIDbDataAccessServiceImpl() {
        LOGGER.traceEntry();
        dbConnector = new JDBCConnector();
        LOGGER.traceExit();
    }

    /**
     * Create the implementation of DOI database by creation the JDBC connection from a specific
     * configuration file.
     * @param customDbConfigFile configuration file
     */
    public DOIDbDataAccessServiceImpl(final String customDbConfigFile) {
        LOGGER.traceEntry("Parameter : {}", customDbConfigFile);
        dbConnector = new JDBCConnector(customDbConfigFile);
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DOIUser> getAllDOIusers() throws DOIDbException {
        LOGGER.traceEntry();
        final List<DOIUser> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            LOGGER.debug("SELECT username, admin, email FROM T_DOI_USERS");
            statement = conn.prepareStatement("SELECT username, admin, email FROM T_DOI_USERS");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    final DOIUser doiuser = new DOIUser();
                    doiuser.setAdmin(rs.getBoolean("admin"));
                    doiuser.setUsername(rs.getString("username"));
                    doiuser.setEmail(rs.getString("email"));
                    users.add(doiuser);
                }
            } 
            LOGGER.debug("{} DOI users found", users.size());
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getAllDOIusers", e);
            throw LOGGER.throwing(
                    new DOIDbException("An exception occured when calling getAllDOIusers", e)
            );
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(users);
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    public List<DOIProject> getAllDOIProjects() throws DOIDbException {
        LOGGER.traceEntry();
        final List<DOIProject> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            LOGGER.debug("SELECT suffix, projectname FROM T_DOI_PROJECT");
            statement = conn.prepareStatement("SELECT suffix, projectname FROM T_DOI_PROJECT");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    DOIProject doiproject = new DOIProject();
                    doiproject.setSuffix(rs.getInt("suffix"));
                    doiproject.setProjectname(rs.getString("projectname"));
                    projects.add(doiproject);
                }
            }
            LOGGER.debug("{} DOI projects found", projects.size());
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getAllDOIProjects", e);
            throw LOGGER.throwing(
                    new DOIDbException("An exception occured when calling getAllDOIProjects", e)
            );
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(projects);
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    public List<DOIProject> getAllDOIProjectsForUser(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameter : {}", username);
        final List<DOIProject> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement assignationsStatement = null;
        PreparedStatement projectsStatement = null;

        try {
            conn = dbConnector.getConnection();
            assignationsStatement = conn.prepareStatement(
                    "SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE username=?");
            assignationsStatement.setString(1, username);
            LOGGER.debug("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE username={}",username);
            try (ResultSet assignations = assignationsStatement.executeQuery()) {
                while (assignations.next()) {
                    projectsStatement = conn.prepareStatement(
                            "SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
                    projectsStatement.setInt(1, assignations.getInt("suffix"));
                    LOGGER.debug("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix={}",assignations.getInt("suffix"));
                    try (ResultSet rs = projectsStatement.executeQuery()) {
                        while (rs.next()) {
                            final DOIProject doiproject = new DOIProject();
                            doiproject.setSuffix(rs.getInt("suffix"));
                            doiproject.setProjectname(rs.getString("projectname"));
                            projects.add(doiproject);
                        }
                    }
                    LOGGER.debug("{} project found", projects.size());
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getAllDOIProjectsForUser", e);
            throw new DOIDbException("An exception occured when calling getAllDOIProjectsForUser", e);
        } finally {
            if (assignationsStatement != null) {
                try {
                    assignationsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (projectsStatement != null) {
                try {
                    projectsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(projects);
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public List<DOIUser> getAllDOIUsersForProject(int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameter : {}", suffix);
        final List<DOIUser> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement assignationsStatement = null;
        PreparedStatement usersStatement = null;
        try {
            conn = dbConnector.getConnection();
            assignationsStatement = conn.prepareStatement(
                    "SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE suffix=?");
            assignationsStatement.setInt(1, suffix);
            LOGGER.debug("SELECT username, suffix FROM T_DOI_ASSIGNATIONS WHERE suffix={}",suffix);
            try (ResultSet assignations = assignationsStatement.executeQuery()) {
                while (assignations.next()) {
                    usersStatement = conn.prepareStatement(
                            "SELECT username, admin, email FROM T_DOI_USERS WHERE username=?");
                    usersStatement.setString(1, assignations.getString("username"));
                    LOGGER.debug("SELECT username, admin, email FROM T_DOI_USERS WHERE username={}",
                            assignations.getString("username"));
                    try (ResultSet rs = usersStatement.executeQuery()) {
                        while (rs.next()) {
                            DOIUser doiuser = new DOIUser();
                            doiuser.setUsername(rs.getString("username"));
                            doiuser.setAdmin(rs.getBoolean("admin"));
                            doiuser.setEmail(rs.getString("email"));
                            users.add(doiuser);
                        }
                    }
                    LOGGER.debug("{} users found", users.size());
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getAllDOIUsersForProject", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling getAllDOIUsersForProject", e));
        } finally {
            if (assignationsStatement != null) {
                try {
                    assignationsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (usersStatement != null) {
                try {
                    usersStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(users);
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    public void addDOIUser(final String username, final Boolean admin) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}\n  admin:{}", username, admin);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        try {            
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(
                    "INSERT INTO T_DOI_USERS (username, admin) VALUES(?, ?)");
            usersStatement.setString(1, username);
            usersStatement.setBoolean(2, admin);
            LOGGER.debug("INSERT INTO T_DOI_USERS (username, admin) VALUES({}, {})", username, admin);
            usersStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addDOIUser", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling addDOIUser", e));
        } finally {
            if (usersStatement != null) {
                try {
                    usersStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void addDOIProject(final int suffix, final String projectname) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  suffix:{}\n  projectname:{}", suffix, projectname);
        Connection conn = null;
        PreparedStatement projectStatement = null;
        try {
            conn = dbConnector.getConnection();
            projectStatement = conn.prepareStatement(
                    "INSERT INTO T_DOI_PROJECT (suffix, projectname) VALUES(?,?)");
            projectStatement.setInt(1, suffix);
            projectStatement.setString(2, projectname);
            LOGGER.debug("INSERT INTO T_DOI_PROJECT (suffix, projectname) VALUES({},{})", suffix, projectname);
            projectStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addDOIProject", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling addDOIProject", e));
        } finally {
            if (projectStatement != null) {
                try {
                    projectStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void addDOIProjectToUser(final String username, final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}\n  suffix:{}", username, suffix);
        Connection conn = null;
        PreparedStatement userStatement = null;
        PreparedStatement projectStatement = null;
        PreparedStatement assignationStatement = null;
        try {
            conn = dbConnector.getConnection();
            userStatement = conn.prepareStatement(
                    "SELECT username, admin FROM T_DOI_USERS WHERE username=?");
            userStatement.setString(1, username);
            LOGGER.debug("SELECT username, admin FROM T_DOI_USERS WHERE username={}",username);
            Boolean userExist;
            try (ResultSet resultSet = userStatement.executeQuery()) {
                userExist = resultSet.next();                
            }
            LOGGER.debug(userExist);
            projectStatement = conn.prepareStatement(
                    "SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
            projectStatement.setInt(1, suffix);
            LOGGER.debug("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix={}", suffix);
            Boolean projectExist;
            try (ResultSet resultSet = projectStatement.executeQuery()) {
                projectExist = resultSet.next();
            }
            LOGGER.debug(projectExist);
            if (!userExist || !projectExist) {
                LOGGER.error("An exception occured when calling addDOIProjectToUser user " + username + 
                        " or project " + suffix + " don't exist in doi database");
                throw LOGGER.throwing(new DOIDbException(
                        "An exception occured when calling addDOIProjectToUser: user " + username
                        + " or project " + suffix + " don't exist in doi database", null));
            }
            assignationStatement = conn.prepareStatement(
                    "INSERT INTO T_DOI_ASSIGNATIONS (username, suffix) VALUES(?,?)");
            assignationStatement.setString(1, username);
            assignationStatement.setInt(2, suffix);
            LOGGER.debug("INSERT INTO T_DOI_ASSIGNATIONS (username, suffix) VALUES({},{})", username, suffix);
            assignationStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addDOIProjectToUser", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling addDOIProjectToUser", e));
        } finally {

            if (userStatement != null) {
                try {
                    userStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (projectStatement != null) {
                try {
                    projectStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (assignationStatement != null) {
                try {
                    assignationStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void removeDOIProjectFromUser(final String username, final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}\n  suffix:{}", username, suffix);
        Connection conn = null;
        PreparedStatement assignationsStatement = null;
        try {
            conn = dbConnector.getConnection();
            assignationsStatement = conn.prepareStatement(
                    "DELETE FROM T_DOI_ASSIGNATIONS WHERE username =? AND suffix=?");
            assignationsStatement.setString(1, username);
            assignationsStatement.setInt(2, suffix);
            LOGGER.debug("DELETE FROM T_DOI_ASSIGNATIONS WHERE username ={} AND suffix={}", username, suffix);
            assignationsStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling removeDOIProjectFromUser", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling removeDOIProjectFromUser", e));
        } finally {
            if (assignationsStatement != null) {
                try {
                    assignationsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void setAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}",username);
        Connection conn = null;
        PreparedStatement userStatement = null;
        PreparedStatement updateStatement = null;
        try {
            conn = dbConnector.getConnection();
            userStatement = conn.prepareStatement(
                    "SELECT username, admin FROM T_DOI_USERS WHERE username=?");
            userStatement.setString(1, username);
            LOGGER.debug("SELECT username, admin FROM T_DOI_USERS WHERE username={}", username);
            Boolean userExist;
            try (ResultSet resultSet = userStatement.executeQuery()) {
                userExist = resultSet.next();
            }
            LOGGER.debug(userExist);
            if (userExist) {
                updateStatement = conn.prepareStatement(
                        "UPDATE T_DOI_USERS SET admin = true WHERE username =?");
                updateStatement.setString(1, username);
                LOGGER.debug("UPDATE T_DOI_USERS SET admin = true WHERE username ={}", username);
                updateStatement.executeUpdate();
            } else {
                LOGGER.error("An exception occured when calling setAdmin:" + "user " + username
                        + " don't exist in doi database");
                throw LOGGER.throwing(new DOIDbException(
                        "An exception occured when calling setAdmin:" + "user " + username
                        + " don't exist in doi database", null));
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling setAdmin", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling setAdmin", e));
        } finally {

            if (userStatement != null) {
                try {
                    userStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (updateStatement != null) {
                try {
                    updateStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void unsetAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}",username);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(
                    "UPDATE T_DOI_USERS SET admin = false WHERE username=?");
            usersStatement.setString(1, username);
            LOGGER.debug("UPDATE T_DOI_USERS SET admin = false WHERE username={}", username);
            usersStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling setAdmin", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling setAdmin", e));
        } finally {

            if (usersStatement != null) {
                try {
                    usersStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void renameDOIProject(final int suffix, final String newprojectname) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  suffix:{}\n  newprojectname", suffix, newprojectname);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(
                    "UPDATE T_DOI_PROJECT SET projectname =? WHERE suffix=?");
            statement.setString(1, newprojectname);
            statement.setInt(2, suffix);
            LOGGER.debug("UPDATE T_DOI_PROJECT SET projectname ={} WHERE suffix={}", newprojectname, suffix);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling setAdmin", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling setAdmin", e));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public String getDOIProjectName(final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  suffix:{}", suffix);
        Connection conn = null;
        String projectNameResult = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(
                    "SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix=?");
            statement.setInt(1, suffix);
            LOGGER.debug("SELECT suffix, projectname FROM T_DOI_PROJECT WHERE suffix={}", suffix);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    projectNameResult = rs.getString("projectname");
                    break;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getDOIProjectName", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling getDOIProjectName", e));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(projectNameResult);
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void addToken(final String token) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  token:{}", token);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement("INSERT INTO T_DOI_TOKENS (token) VALUES (?)");
            statement.setString(1, token);
            LOGGER.debug("INSERT INTO T_DOI_TOKENS (token) VALUES ({})", token);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addToken", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling addToken", e));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void deleteToken(final String token) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  token:{}", token);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement("DELETE FROM T_DOI_TOKENS WHERE token=?");
            statement.setString(1, token);
            LOGGER.debug("DELETE FROM T_DOI_TOKENS WHERE token={}", token);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling deleteToken", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling deleteToken", e));
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public List<String> getTokens() throws DOIDbException {
        LOGGER.traceEntry();
        final List<String> tokens = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement("SELECT token FROM T_DOI_TOKENS");
            LOGGER.debug("SELECT token FROM T_DOI_TOKENS");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tokens.add(rs.getString("token"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getToken", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling getToken", e));
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(tokens);
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void removeDOIUser(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}", username);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        PreparedStatement assignationsStatement = null;

        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement("DELETE FROM T_DOI_USERS WHERE username=?");
            usersStatement.setString(1, username);
            LOGGER.debug("DELETE FROM T_DOI_USERS WHERE username={}", username);
            usersStatement.executeUpdate();
            assignationsStatement = conn.prepareStatement(
                    "DELETE FROM T_DOI_ASSIGNATIONS WHERE username=?");
            assignationsStatement.setString(1, username);
            LOGGER.debug("DELETE FROM T_DOI_ASSIGNATIONS WHERE username={}", username);
            assignationsStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling removeDOIUser", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling removeDOIUser", e));
        } finally {
            if (usersStatement != null) {
                try {
                    usersStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (assignationsStatement != null) {
                try {
                    assignationsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void addDOIUser(final String username, final Boolean admin, final String email) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}\n  admin:{}\n email:{}", username, admin, email);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(
                    "INSERT INTO T_DOI_USERS (username, admin, email) VALUES(?,?,?)");
            statement.setString(1, username);
            statement.setBoolean(2, admin);
            statement.setString(3, email);
            LOGGER.debug("INSERT INTO T_DOI_USERS (username, admin, email) VALUES({},{},{})", username, admin, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addDOIUser", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling addDOIUser", e));
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void removeDOIProject(int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  suffix:{}", suffix);
        Connection conn = null;
        PreparedStatement projectStatement = null;
        PreparedStatement assignationsStatement = null;
        try {
            conn = dbConnector.getConnection();
            projectStatement = conn.prepareStatement("DELETE FROM T_DOI_PROJECT WHERE suffix=?");
            projectStatement.setInt(1, suffix);
            LOGGER.debug("DELETE FROM T_DOI_PROJECT WHERE suffix={}", suffix);
            projectStatement.executeUpdate();

            assignationsStatement = conn.prepareStatement(
                    "DELETE FROM T_DOI_ASSIGNATIONS WHERE suffix =?");
            assignationsStatement.setInt(1, suffix);
            LOGGER.debug("DELETE FROM T_DOI_ASSIGNATIONS WHERE suffix ={}", suffix);
            assignationsStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling removeDOIProject", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling removeDOIProject", e));
        } finally {

            if (projectStatement != null) {
                try {
                    projectStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (assignationsStatement != null) {
                try {
                    assignationsStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public boolean isAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}", username);
        boolean isAdmin = false;
        final DOIUser user = getDoiUserFromDb(username);        
        if (user != null) {
            isAdmin = getDoiUserFromDb(username).isAdmin();
        }
        return LOGGER.traceExit(isAdmin);
    }

    /**
     * Returns the DOI user from the username.
     * @param username username in the DB
     * @return DOIUser
     * @throws DOIDbException 
     */
    private DOIUser getDoiUserFromDb(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n  username:{}", username);
        Connection conn = null;
        PreparedStatement statement = null;
        DOIUser doiuser = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(
                    "SELECT username, admin, email FROM T_DOI_USERS WHERE username=?");
            statement.setString(1, username);
            LOGGER.debug("SELECT username, admin, email FROM T_DOI_USERS WHERE username={}", username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    doiuser = new DOIUser();
                    doiuser.setAdmin(rs.getBoolean("admin"));
                    doiuser.setUsername(rs.getString("username"));
                    doiuser.setEmail(rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling getAllDOIusers", e);
            throw LOGGER.throwing(new DOIDbException("An exception occured when calling getAllDOIusers", e));
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    LOGGER.error("Unable to close the statement to database", ex);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close connection to database", e);
                }
            }
        }
        return LOGGER.traceExit(doiuser);
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public boolean isUserExist(final String username) throws DOIDbException { 
        LOGGER.traceEntry("Parameters:\n  username:{}", username);
        return LOGGER.traceExit(getDoiUserFromDb(username) != null);
    }

    /**
     * {@inheritDoc}
     */     
    @Override
    public void close() throws DOIDbException {
        LOGGER.traceEntry();
        this.dbConnector.close();
        LOGGER.traceExit();
    }

}

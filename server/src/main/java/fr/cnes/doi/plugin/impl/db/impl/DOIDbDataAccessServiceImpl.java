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
package fr.cnes.doi.plugin.impl.db.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.cnes.doi.exception.DOIDbException;
import fr.cnes.doi.db.model.DOIProject;
import fr.cnes.doi.db.model.DOIUser;
import fr.cnes.doi.plugin.impl.db.service.DOIDbDataAccessService;
import java.util.Map;
import org.apache.logging.log4j.Level;

/**
 * Implementation of the {@link DOIDbDataAccessService} using JDBC.
 *
 * @author Jean-Christophe Malapert (jean-christophe.malapert@cnes.fr)
 */
public final class DOIDbDataAccessServiceImpl implements DOIDbDataAccessService {

    /**
     * Database url
     */
    public static final String DB_URL = "Starter.Database.Doidburl";

    /**
     * Database user
     */
    public static final String DB_USER = "Starter.Database.User";

    /**
     * Database password
     */
    public static final String DB_PWD = "Starter.Database.Pwd";

    /**
     * Minimum number of connection object that are to be kept alive in the pool
     */
    public static final String DB_MIN_IDLE_CONNECTIONS = "Starter.Database.MinIdleConnections";

    /**
     * Minimum number of connection object that are to be kept alive in the pool
     */
    public static final String DB_MAX_IDLE_CONNECTIONS = "Starter.Database.MaxIdleConnections";

    /**
     * Maximum number of active connections that can be allocated at the same time.
     */
    public static final String DB_MAX_ACTIVE_CONNECTIONS = "Starter.Database.MaxActiveConnections";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(DOIDbDataAccessServiceImpl.class.
            getName());

    /**
     * SQL field that contains the username {@value #FIELD_USERNAME}.
     */
    private static final String FIELD_USERNAME = "username";
    /**
     * SQL field that contains the information telling if a user is admin {@value #FIELD_ADMIN}.
     */
    private static final String FIELD_ADMIN = "admin";
    /**
     * SQL field that contains the email user {@value #FIELD_EMAIL}.
     */
    private static final String FIELD_EMAIL = "email";
    /**
     * SQL field that contains the projectname related to a user {@value #FIELD_PROJECTNAME}.
     */
    private static final String FIELD_PROJECTNAME = "projectname";
    /**
     * SQL field that contains the DOI suffix for the project name {@value #FIELD_PROJECT_SUFFIX}.
     */
    private static final String FIELD_PROJECT_SUFFIX = "suffix";
    /**
     * SQL field that contains the authentication token.
     */
    private static final String FIELD_TOKEN = "token";

    /**
     * Select user information.
     */
    private static final String SELECT_DOI_USERS = String.format(
            "SELECT %s, %s, %s FROM T_DOI_USERS",
            FIELD_USERNAME, FIELD_ADMIN, FIELD_EMAIL
    );

    /**
     * Select project information.
     */
    private static final String SELECT_PROJECTS = String.format(
            "SELECT %s, %s FROM T_DOI_PROJECT",
            FIELD_PROJECT_SUFFIX, FIELD_PROJECTNAME);

    /**
     * Select a specific project based on the DOI suffix.
     */
    private static final String SELECT_PROJECT_SUFFIX = String.format(
            "%s WHERE %s=?", SELECT_PROJECTS, FIELD_PROJECT_SUFFIX);

    /**
     * Select project information for a specific user.
     */
    private static final String SELECT_PROJECTS_WITH_CTX_USER = String.format(
            "SELECT %s, %s "
            + "FROM T_DOI_PROJECT "
            + "WHERE %s IN ("
            + "    SELECT %s "
            + "    FROM T_DOI_ASSIGNATIONS "
            + "    WHERE %s=?"
            + ")", FIELD_PROJECT_SUFFIX, FIELD_PROJECTNAME, FIELD_PROJECT_SUFFIX,
            FIELD_PROJECT_SUFFIX, FIELD_USERNAME);

    /**
     * Select user information bases on the DOI suffix.
     */
    private static final String SELECT_PROJECTS_WITH_CTX_SUFFIX = String.format(
            "SELECT %s, %s, %s "
            + "FROM T_DOI_USERS "
            + "WHERE %s IN ("
            + " SELECT %s"
            + " FROM T_DOI_ASSIGNATIONS"
            + " WHERE %s=?"
            + ")", FIELD_USERNAME, FIELD_ADMIN, FIELD_EMAIL, FIELD_USERNAME,
            FIELD_USERNAME, FIELD_PROJECT_SUFFIX);

    /**
     * Select user information based on its username.
     */
    private static final String SELECT_USERS_CLAUSE_USER = SELECT_DOI_USERS + " WHERE username=?";

    /**
     * Checks if the user exists based on its username.
     */
    private static final String SELECT_EXISTS_USERNAME = String.format(
            "SELECT 1 FROM T_DOI_USERS WHERE %s=?", FIELD_USERNAME);

    /**
     * Checks if the project exists based on its DOI suffix.
     */
    private static final String SELECT_EXISTS_SUFFIX = String.format(
            "SELECT 1 FROM T_DOI_PROJECT WHERE %s=?", FIELD_PROJECT_SUFFIX);

    /**
     * Select all tokens.
     */
    private static final String SELECT_TOKEN = String.format(
            "SELECT %s FROM T_DOI_TOKENS", FIELD_TOKEN);

    /**
     * Delete part of the project.
     */
    private static final String DELETE_PROJECT = "DELETE FROM T_DOI_PROJECT";

    /**
     * Delete a project based on its DOI suffix.
     */
    private static final String DELETE_PROJECT_WITH_SUFFIX = String.format(
            "%s WHERE %s=?", DELETE_PROJECT, FIELD_PROJECT_SUFFIX);

    /**
     * Delete a user based on its username.
     */
    private static final String DELETE_DOI_USERS = String.format(
            "DELETE FROM T_DOI_USERS WHERE %s=?", FIELD_USERNAME);

    /**
     * Delete part of the assign.
     */
    private static final String DELETE_ASSIGN = "DELETE FROM T_DOI_ASSIGNATIONS";

    /**
     * Delete assignation based on the username.
     */
    private static final String DELETE_ASSIGN_USERNAME = String.format(
            "%s WHERE %s=?", DELETE_ASSIGN, FIELD_USERNAME);

    /**
     * Delete assignation based on the project DOI suffix.
     */
    private static final String DELETE_ASSIGN_SUFFIX = String.format(
            "%s WHERE %s=?", DELETE_ASSIGN, FIELD_PROJECT_SUFFIX);

    /**
     * Delete assignation based on username and project DOI suffix.
     */
    private static final String DELETE_ASSIGN_USER_AND_SUFFIX = String.format(
            DELETE_ASSIGN_USERNAME + " AND %s=?", FIELD_PROJECT_SUFFIX);

    /**
     * Delete token.
     */
    private static final String DELETE_TOKEN = String.format(
            "DELETE FROM T_DOI_TOKENS WHERE %s=?", FIELD_TOKEN);

    /**
     * Insert user information.
     */
    private static final String INSERT_DOI_USERS = String.format(
            "INSERT INTO T_DOI_USERS (%s, %s) VALUES(?, ?)", FIELD_USERNAME, FIELD_ADMIN);

    /**
     * Insert user information.
     */
    private static final String INSERT_FULL_DOI_UERS = String.format(
            "INSERT INTO T_DOI_USERS (%s, %s, %s) VALUES(?,?,?)",
            FIELD_USERNAME, FIELD_ADMIN, FIELD_EMAIL);

    /**
     * Insert project information.
     */
    private static final String INSERT_DOI_PROJECTS = String.format(
            "INSERT INTO T_DOI_PROJECT (%s, %s) VALUES(?,?)",
            FIELD_PROJECT_SUFFIX, FIELD_PROJECTNAME);

    /**
     * Insert assignation information.
     */
    private static final String INSERT_DOI_ASSIGN = String.format(
            "INSERT INTO T_DOI_ASSIGNATIONS (%s, %s) VALUES(?,?)",
            FIELD_USERNAME, FIELD_PROJECT_SUFFIX);

    /**
     * Insert token.
     */
    private static final String INSERT_TOKEN = String.format(
            "INSERT INTO T_DOI_TOKENS (%s) VALUES (?)", FIELD_TOKEN);

    /**
     * Sets a user as admin.
     */
    private static final String UPDATE_ADMIN_TRUE = String.format(
            "UPDATE T_DOI_USERS SET %s = true WHERE %s =?", FIELD_ADMIN, FIELD_USERNAME);

    /**
     * Unsets a user as admin.
     */
    private static final String UPDATE_ADMIN_FALSE = String.format(
            "UPDATE T_DOI_USERS SET %s = false WHERE %s =?", FIELD_ADMIN, FIELD_USERNAME);

    /**
     * Update project information.
     */
    private static final String UPDATE_PROJECT = String.format(
            "UPDATE T_DOI_PROJECT SET %s =? WHERE %s =?",
            FIELD_PROJECTNAME, FIELD_PROJECT_SUFFIX);

    /**
     * Connection to the DOI database.
     */
    private JDBCConnector dbConnector;

    /**
     * Constructor.
     */
    public DOIDbDataAccessServiceImpl() {
        LOGGER.traceEntry();
        LOGGER.traceExit();
    }

    /**
     * Create the implementation of DOI database by creation the JDBC connection from a specific
     * configuration file.
     *
     * @param dbUrl database URL
     * @param dbUser database user
     * @param dbPwd database password
     * @param options database options
     */
    public DOIDbDataAccessServiceImpl(final String dbUrl, final String dbUser, final String dbPwd,
            final Map<String, Integer> options) {
        LOGGER.traceEntry("Parameter\n\tdbUrl : {}\n\tdbPwd : {}\n\toptions : {}", dbUrl, dbPwd,
                options);
        this.init(dbUrl, dbUser, dbPwd, options);
        LOGGER.traceExit();
    }

    /**
     * Initialize the JDBC connection when it is not defined.
     *
     * @param dbUrl database URL
     * @param dbUser database user
     * @param dbPwd database password
     * @param options database options
     */
    public void init(final String dbUrl, final String dbUser, final String dbPwd,
            final Map<String, Integer> options) {
        LOGGER.traceEntry("Parameter\n\tdbUrl : {}\n\tdbPwd : {}\n\toptions : {}", dbUrl, dbPwd,
                options);
        if (dbConnector == null) {
            dbConnector = new JDBCConnector(dbUrl, dbUser, dbPwd, options);
        }
        LOGGER.traceExit();
    }

    /**
     * Returns the list of DOI projects
     *
     * @param statement Query to T_DOI_PROJECT
     * @return the DOI projects related to the query
     * @throws SQLException When an SQL execption occurs
     */
    private List<DOIProject> getDOIProjects(final PreparedStatement statement) throws SQLException {
        LOGGER.traceEntry("Parameter\n\t statement: {}", statement.toString());
        LOGGER.debug(statement.toString());
        final List<DOIProject> projects = new ArrayList<>();
        try (final ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                final DOIProject doiproject = new DOIProject();
                doiproject.setSuffix(rs.getInt(FIELD_PROJECT_SUFFIX));
                doiproject.setProjectname(rs.getString(FIELD_PROJECTNAME));
                projects.add(doiproject);
            }
        }
        LOGGER.debug("{} DOI projects found", projects.size());
        return LOGGER.traceExit(projects);
    }

    /**
     * Returns the list of DOI users
     *
     * @param statement Query to T_DOI_USERS
     * @return the DOI users related to the query
     * @throws SQLException When an SQL execption occurs
     */
    private List<DOIUser> getDOIUSers(final PreparedStatement statement) throws SQLException {
        LOGGER.traceEntry("Parameter\n\t statement: {}", statement.toString());
        LOGGER.debug(statement.toString());
        final List<DOIUser> users = new ArrayList<>();
        try (final ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                final DOIUser doiuser = new DOIUser();
                doiuser.setUsername(rs.getString(FIELD_USERNAME));
                doiuser.setAdmin(rs.getBoolean(FIELD_ADMIN));
                doiuser.setEmail(rs.getString(FIELD_EMAIL));
                users.add(doiuser);
            }
        }
        LOGGER.debug("{} DOI users found", users.size());
        return LOGGER.traceExit(users);
    }

    /**
     * Returns the list of tokens.
     *
     * @param statement Query to T_DOI_USERS
     * @return the tokens related to the query
     * @throws SQLException When an SQL execption occurs
     */
    private List<String> getTokens(final PreparedStatement statement) throws SQLException {
        LOGGER.traceEntry("Parameter\n\t statement: {}", statement.toString());
        LOGGER.debug(statement.toString());
        final List<String> tokens = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                tokens.add(rs.getString(FIELD_TOKEN));
            }
        }
        LOGGER.debug("{} tokens found", tokens.size());
        return LOGGER.traceExit(tokens);
    }

    /**
     * Updates queries.
     *
     * @param statements statements
     * @throws SQLException - if a problem occurs
     */
    private void updateQueries(final PreparedStatement... statements) throws SQLException {
        for (final PreparedStatement statement : statements) {
            LOGGER.debug(statement.toString());
            statement.executeUpdate();
        }
    }

    /**
     * Returns the list of projectName.
     *
     * @param statement Query to T_DOI_PROJECT
     * @return the tokens related to the query
     * @throws SQLException When an SQL execption occurs
     */
    private List<String> getProjectName(final PreparedStatement statement) throws SQLException {
        LOGGER.traceEntry("Parameter\n\t statement: {}", statement.toString());
        LOGGER.debug(statement.toString());
        final List<String> projectName = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                projectName.add(rs.getString(FIELD_PROJECTNAME));
            }
        }
        return LOGGER.traceExit(projectName);
    }

    /**
     * Returns true when the query returns a non empty result otherwise false.
     *
     * @param statement query
     * @return true when the query returns a non empty result otherwise false
     * @throws SQLException When an SQL execption occurs
     */
    private boolean isQueryExist(final PreparedStatement statement) throws SQLException {
        LOGGER.traceEntry("Parameter\n\t statement: {}", statement.toString());
        final boolean isExist;
        LOGGER.debug(statement.toString());
        try (final ResultSet resultSet = statement.executeQuery()) {
            isExist = resultSet.next();
        }
        LOGGER.debug(isExist);
        return LOGGER.traceExit(isExist);
    }

    /**
     * Close the statements and free the connection.
     *
     * @param conn connection to closeAndRelease
     * @param statements statements to closeAndRelease
     */
    private void closeAndRelease(final Connection conn, final PreparedStatement... statements) {
        for (final PreparedStatement statement : statements) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("Unable to close statement", e);
                }
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

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public List<DOIUser> getAllDOIusers() throws DOIDbException {
        LOGGER.traceEntry();
        final List<DOIUser> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(SELECT_DOI_USERS);
            users.addAll(getDOIUSers(statement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.FATAL,
                    new DOIDbException("An exception occured when calling getAllDOIusers", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        return LOGGER.traceExit(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public List<DOIProject> getAllDOIProjects() throws DOIDbException {
        LOGGER.traceEntry();
        final List<DOIProject> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(SELECT_PROJECTS);
            projects.addAll(getDOIProjects(statement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.FATAL,
                    new DOIDbException("An exception occured when calling getAllDOIProjects", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        return LOGGER.traceExit(projects);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public List<DOIProject> getAllDOIProjectsForUser(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameter\n\t username: {}", username);
        final List<DOIProject> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement projectsStatement = null;

        try {
            conn = dbConnector.getConnection();
            projectsStatement = conn.prepareStatement(SELECT_PROJECTS_WITH_CTX_USER);
            projectsStatement.setString(1, username);
            projects.addAll(getDOIProjects(projectsStatement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.FATAL,
                    new DOIDbException("An exception occured when calling getAllDOIProjectsForUser",
                            e)
            );
        } finally {
            closeAndRelease(conn, projectsStatement);
        }
        return LOGGER.traceExit(projects);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public List<DOIUser> getAllDOIUsersForProject(final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameter\n\t suffix: {}", suffix);
        final List<DOIUser> users = new ArrayList<>();
        Connection conn = null;
        final PreparedStatement assignationsStatement = null;
        PreparedStatement usersStatement = null;
        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(SELECT_PROJECTS_WITH_CTX_SUFFIX);
            usersStatement.setInt(1, suffix);
            users.addAll(getDOIUSers(usersStatement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.FATAL,
                    new DOIDbException(
                            "An exception occured when calling getAllDOIUsersForProject", e));
        } finally {
            closeAndRelease(conn, assignationsStatement, usersStatement);
        }
        return LOGGER.traceExit(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void addDOIUser(final String username, final Boolean admin) throws DOIDbException {
        LOGGER.traceEntry("Parameter\n\t username: {}\n\tadmin: {}", username, admin);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(INSERT_DOI_USERS);
            usersStatement.setString(1, username);
            usersStatement.setBoolean(2, admin);
            this.updateQueries(usersStatement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling addDOIUser", e)
            );
        } finally {
            closeAndRelease(conn, usersStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void addDOIProject(final int suffix, final String projectname) throws DOIDbException {
        LOGGER.traceEntry("Parameter\n\t suffix: {}\n\tprojectname: {}", suffix, projectname);
        Connection conn = null;
        PreparedStatement projectStatement = null;
        try {
            conn = dbConnector.getConnection();
            projectStatement = conn.prepareStatement(INSERT_DOI_PROJECTS);
            projectStatement.setInt(1, suffix);
            projectStatement.setString(2, projectname);
            this.updateQueries(projectStatement);
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling addDOIProject", e);
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling addDOIProject", e)
            );
        } finally {
            closeAndRelease(conn, projectStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void addDOIProjectToUser(final String username, final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}\n\tsuffix:{}", username, suffix);
        Connection conn = null;
        PreparedStatement userStatement = null;
        PreparedStatement projectStatement = null;
        PreparedStatement assignationStatement = null;
        try {
            conn = dbConnector.getConnection();

            userStatement = conn.prepareStatement(SELECT_EXISTS_USERNAME);
            userStatement.setString(1, username);
            final boolean isUserExist = isQueryExist(userStatement);

            projectStatement = conn.prepareStatement(SELECT_EXISTS_SUFFIX);
            projectStatement.setInt(1, suffix);
            final boolean isProjectExist = isQueryExist(projectStatement);

            if (!isUserExist || !isProjectExist) {
                throw LOGGER.throwing(
                        Level.FATAL,
                        new DOIDbException(
                                "An exception occured when calling addDOIProjectToUser: user " + username
                                + " or project " + suffix + " don't exist in doi database", null)
                );
            }
            assignationStatement = conn.prepareStatement(INSERT_DOI_ASSIGN);
            assignationStatement.setString(1, username);
            assignationStatement.setInt(2, suffix);
            this.updateQueries(assignationStatement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling addDOIProjectToUser", e)
            );
        } finally {
            closeAndRelease(conn, userStatement, projectStatement, assignationStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void removeDOIProjectFromUser(final String username, final int suffix) throws
            DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}\n\tsuffix:{}", username, suffix);
        Connection conn = null;
        PreparedStatement assignationsStatement = null;
        try {
            conn = dbConnector.getConnection();
            assignationsStatement = conn.prepareStatement(DELETE_ASSIGN_USER_AND_SUFFIX);
            assignationsStatement.setString(1, username);
            assignationsStatement.setInt(2, suffix);
            this.updateQueries(assignationsStatement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling removeDOIProjectFromUser",
                            e)
            );
        } finally {
            closeAndRelease(conn, assignationsStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void setAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
        Connection conn = null;
        PreparedStatement userStatement = null;
        PreparedStatement updateStatement = null;
        try {
            conn = dbConnector.getConnection();
            userStatement = conn.prepareStatement(SELECT_EXISTS_USERNAME);
            userStatement.setString(1, username);
            final boolean isUserExist = isQueryExist(userStatement);
            if (isUserExist) {
                updateStatement = conn.prepareStatement(UPDATE_ADMIN_TRUE);
                updateStatement.setString(1, username);
                this.updateQueries(updateStatement);
            } else {
                throw LOGGER.throwing(
                        Level.ERROR,
                        new DOIDbException(
                                "An exception occured when calling setAdmin:" + "user " + username
                                + " don't exist in doi database", null)
                );
            }
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling setAdmin", e));
        } finally {
            closeAndRelease(conn, userStatement, updateStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void unsetAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(UPDATE_ADMIN_FALSE);
            usersStatement.setString(1, username);
            this.updateQueries(usersStatement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling setAdmin", e)
            );
        } finally {
            closeAndRelease(conn, usersStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void renameDOIProject(final int suffix, final String newprojectname) throws
            DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tsuffix:{}\n\tnewprojectname", suffix, newprojectname);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(UPDATE_PROJECT);
            statement.setString(1, newprojectname);
            statement.setInt(2, suffix);
            this.updateQueries(statement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling setAdmin", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public String getDOIProjectName(final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tsuffix:{}", suffix);
        Connection conn = null;
        final List<String> projectNameResult = new ArrayList<>();
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(SELECT_PROJECT_SUFFIX);
            statement.setInt(1, suffix);
            projectNameResult.addAll(getProjectName(statement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling getDOIProjectName", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        return LOGGER.traceExit(projectNameResult.isEmpty() ? null : projectNameResult.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void addToken(final String token) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\ttoken:{}", token);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(INSERT_TOKEN);
            statement.setString(1, token);
            this.updateQueries(statement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling addToken", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void deleteToken(final String token) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\ttoken:{}", token);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(DELETE_TOKEN);
            statement.setString(1, token);
            this.updateQueries(statement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling deleteToken", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public List<String> getTokens() throws DOIDbException {
        LOGGER.traceEntry();
        final List<String> tokens = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(SELECT_TOKEN);
            tokens.addAll(getTokens(statement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling getToken", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        return LOGGER.traceExit(tokens);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void removeDOIUser(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
        Connection conn = null;
        PreparedStatement usersStatement = null;
        PreparedStatement assignationsStatement = null;

        try {
            conn = dbConnector.getConnection();
            usersStatement = conn.prepareStatement(DELETE_DOI_USERS);
            usersStatement.setString(1, username);

            assignationsStatement = conn.prepareStatement(DELETE_ASSIGN_USERNAME);
            assignationsStatement.setString(1, username);

            this.updateQueries(usersStatement, assignationsStatement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling removeDOIUser", e)
            );
        } finally {
            closeAndRelease(conn, usersStatement, assignationsStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void addDOIUser(final String username, final Boolean admin, final String email) throws
            DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}\n\tadmin:{}\n\temail:{}", username, admin,
                email);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(INSERT_FULL_DOI_UERS);
            statement.setString(1, username);
            statement.setBoolean(2, admin);
            statement.setString(3, email);
            this.updateQueries(statement);
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling addDOIUser", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    public void removeDOIProject(final int suffix) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tsuffix:{}", suffix);
        Connection conn = null;
        PreparedStatement projectStatement = null;
        PreparedStatement assignationsStatement = null;
        try {
            conn = dbConnector.getConnection();
            projectStatement = conn.prepareStatement(DELETE_PROJECT_WITH_SUFFIX);
            projectStatement.setInt(1, suffix);

            assignationsStatement = conn.prepareStatement(DELETE_ASSIGN_SUFFIX);
            assignationsStatement.setInt(1, suffix);

            this.updateQueries(projectStatement, assignationsStatement);
        } catch (SQLException e) {
            LOGGER.error("An exception occured when calling removeDOIProject", e);
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling removeDOIProject", e)
            );
        } finally {
            closeAndRelease(conn, projectStatement, assignationsStatement);
        }
        LOGGER.traceExit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAdmin(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
        boolean isAdmin = false;
        final DOIUser user = getDoiUserFromDb(username);
        if (user != null) {
            isAdmin = getDoiUserFromDb(username).isAdmin();
        }
        return LOGGER.traceExit(isAdmin);
    }

    /**
     * Returns the DOI user from the username.
     *
     * @param username username in the DB
     * @return DOIUser or null
     * @throws DOIDbException - if a database error occurs.
     */
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
            justification = "Cleans up with closeAndRelease method")
    private DOIUser getDoiUserFromDb(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
        Connection conn = null;
        PreparedStatement statement = null;
        final List<DOIUser> doiusers = new ArrayList<>();
        try {
            conn = dbConnector.getConnection();
            statement = conn.prepareStatement(SELECT_USERS_CLAUSE_USER);
            statement.setString(1, username);
            doiusers.addAll(getDOIUSers(statement));
        } catch (SQLException e) {
            throw LOGGER.throwing(
                    Level.ERROR,
                    new DOIDbException("An exception occured when calling getAllDOIusers", e)
            );
        } finally {
            closeAndRelease(conn, statement);
        }
        return LOGGER.traceExit(doiusers.isEmpty() ? null : doiusers.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserExist(final String username) throws DOIDbException {
        LOGGER.traceEntry("Parameters:\n\tusername:{}", username);
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

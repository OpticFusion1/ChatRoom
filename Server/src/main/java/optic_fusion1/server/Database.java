/*
 * Copyright (C) 2021 Optic_Fusion1
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package optic_fusion1.server;

import optic_fusion1.common.utils.BCrypt;
import optic_fusion1.server.utils.RandomString;
import optic_fusion1.server.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.UUID;

public class Database {

    private static final Logger LOGGER = LogManager.getLogger();

    private Connection connection;

    public Database() {
        try {
            File file = Utils.getFile("data", "database.db");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    LOGGER.fatal(ex.getLocalizedMessage());
                }
            }
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + file.toURI());
            } catch (SQLException ex) {
                LOGGER.fatal(ex.getLocalizedMessage());
            }
            executePrepareStatement();

            // handle creating a server admin user
          boolean serverUserExists = containsUser("admin");
            if (!serverUserExists) {
                LOGGER.debug("A server user does not exist, creating one...");
                createServerOpUser();
            } else LOGGER.debug("A server user has already been created.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void executePrepareStatement() {
        try {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `users` (`username` TEXT NOT NULL PRIMARY KEY, `uuid` BINARY(16) NOT NULL, `pass` CHAR(60) NOT NULL, `isGlobalOp` BOOLEAN NOT NULL DEFAULT false, `nickname` TEXT)").execute();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
    }

    private static final String INSERT_SERVER_OP_USER = "INSERT OR IGNORE INTO `users` (`username`, `uuid`, `pass`, `isGlobalOp`) VALUES (?, ?, ?, ?)";

    public void createServerOpUser() {
        try {
            final String adminPassword = new RandomString().nextString();
            final String adminUUID = UUID.randomUUID().toString();
            PreparedStatement statement = connection.prepareStatement(INSERT_SERVER_OP_USER);
            statement.setString(1, "admin");
            statement.setString(2, adminUUID);
            statement.setString(3, BCrypt.hashpw(adminPassword, BCrypt.gensalt()));
            statement.setBoolean(4, true);
            statement.execute();

            LOGGER.info("------------------------------");
            LOGGER.info("Server admin user has been created, do not loose these credentials!!");
            LOGGER.info(String.format("Username: admin; Password: %s", adminPassword));
            LOGGER.info("------------------------------");
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
    }

    private static final String INSERT_USER = "INSERT OR IGNORE INTO `users` (`username`, `uuid`, `pass`) VALUES (?, ?, ?)";

    public void insertUser(String userName, UUID uniqueId, String hashedPassword) {
        try {
            PreparedStatement statement = connection.prepareStatement(INSERT_USER);
            statement.setString(1, userName);
            statement.setString(2, uniqueId.toString());
            statement.setString(3, hashedPassword);
            statement.execute();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
    }

    private static final String CONTAINS_USER = "SELECT * FROM `users` WHERE `username` LIKE ?";

    public boolean containsUser(String userName) {
        try {
            PreparedStatement statement = connection.prepareStatement(CONTAINS_USER);
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
        return false;
    }

    private static final String GET_UUID = "SELECT uuid FROM `users` WHERE `username` = ?";

    public UUID getUUID(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_UUID);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString("uuid"));
            }
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
        return null;
    }

    private static final String GET_PASSWORD = "SELECT pass FROM `users` WHERE `username` = ?";

    public boolean isPasswordAlreadySet(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_PASSWORD);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
        return false;
    }

    public boolean isPasswordCorrect(String username, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_PASSWORD);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            boolean hasNext = resultSet.next();
            if (hasNext) {
                return BCrypt.checkpw(password, resultSet.getString("pass"));
            }
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
        return false;
    }

    private static final String UPDATE_NICKNAME = "UPDATE users SET nickname = ? WHERE uuid = ?";

    public void updateNickname(UUID uniqueId, String nickname) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_NICKNAME);
            statement.setString(1, nickname);
            statement.setString(2, uniqueId.toString());
            statement.execute();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
    }

    private static final String UPDATE_PASSWORD = "UPDATE users SET pass = ? WHERE uuid = ?";

    public void updatePassword(UUID uniqueId, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD);
            statement.setString(1, password);
            statement.setString(2, uniqueId.toString());
            statement.execute();
        } catch (SQLException ex) {
            LOGGER.fatal(ex.getLocalizedMessage());
        }
    }
}

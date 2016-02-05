/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.discord.bot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

public class DatabaseHandler
{
    
    private Connection connection;
    private Statement stmt;
    private PreparedStatement preparedStmt;
    
    private final String USERS_SQL = "CREATE TABLE IF NOT EXISTS Users" +
                                     "(ID varchar(255) NOT NULL PRIMARY KEY," +
                                     "name varchar(255)" +
                                     ");";
    private final String CHANNELS_SQL = "CREATE TABLE IF NOT EXISTS Channels" +
                                        "(" +
                                        "ID varchar(255) NOT NULL PRIMARY KEY," +
                                        "name varchar(255)" +
                                        ");";
    private final String MESSAGES_SQL = 
                                        "CREATE TABLE IF NOT EXISTS Messages" +
                                        "(" +
                                        "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                        "message TEXT," +
                                        "userId varchar(255)," +
                                        "channelId varchar(255)," +
                                        "username varchar(255)," +
                                        "time DATETIME," +
                                        "FOREIGN KEY (userId) REFERENCES Users(id)," +
                                        "FOREIGN KEY (channelId) REFERENCES Channels(id)" +
                                        ");";
    private final String ALERTS_SQL = 
                                        "CREATE TABLE IF NOT EXISTS Alerts" +
                                        "(" +
                                        "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                        "userId varchar(255)," +
                                        "twitchChannel varchar(255)," +
                                        "channelId varchar(255)," +
                                        "FOREIGN KEY (userId) REFERENCES Users(id)," +
                                        "FOREIGN KEY (channelId) REFERENCES Channels(id)" +
                                        ");";

    public DatabaseHandler()
    {
        System.out.println("Connecting to database...");
        try 
        {
            connection = DriverManager.getConnection(AuthVariables.DB_URL, AuthVariables.DB_USERNAME, AuthVariables.DB_PASSWORD);
            System.out.println("Database connected!");
            stmt = connection.createStatement();
            // Creating database if it does not exist.
            stmt.executeUpdate("USE discord;");
            stmt.executeUpdate(USERS_SQL);
            stmt.executeUpdate(CHANNELS_SQL);
            stmt.executeUpdate(MESSAGES_SQL);
            stmt.executeUpdate(ALERTS_SQL);
        } 
        catch (SQLException e) 
        {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    public void addAlert(String userId, String channelId, String twitchChannel)
    {
        try 
        {
            String query = "INSERT INTO Alerts (userId, twitchChannel, channelId) " +
                            "VALUES (?, ?, ?)" +
                            ";";
            preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, userId);
            preparedStmt.setString(2, twitchChannel);
            preparedStmt.setString(3, channelId);
            preparedStmt.executeUpdate();
            System.out.println("Adding " + userId + ", " + channelId + ", " + twitchChannel);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeAlert(String userId, String channelId, String twitchChannel)
    {
        try 
        {
            String query = "DELETE FROM Alerts " +
                            "WHERE userID = \"" + userId + "\" and channelId = \"" + channelId + "\" and twitchChannel = \"" + twitchChannel + "\"" +
                            ";";
            stmt.executeUpdate(query);
            System.out.println("Removed " + userId + ", " + channelId + ", " + twitchChannel);
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readAlerts(ArrayList<Alert> alertList)
    {
        try 
        {
            stmt = connection.createStatement();
            String query = "SELECT * " +
                           "FROM Alerts " +
                           ";";
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next())
            {
                boolean found = false;
                for (Alert a : alertList)
                {
                    if (a.getChannelId().equals(rs.getString("channelId")) && a.getTwitchChannel().equals(rs.getString("twitchChannel")))
                    {
                        a.getUserIdList().add(rs.getString("userId"));
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    alertList.add(new Alert(rs.getString("userId"), rs.getString("channelId"), rs.getString("twitchChannel")));
                }
            }
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readQuotes(ArrayList<Quote> quoteList)
    {
        try 
        {
            stmt = connection.createStatement();
            String query = "SELECT * " +
                           "FROM Messages " +
                           ";";
            
            ResultSet rs = stmt.executeQuery(query);
            System.out.println("Reading quotes.");
            while (rs.next())
            {
                quoteList.add(new Quote(rs.getString("message"), rs.getString("userId"), rs.getString("channelId"), rs.getString("username"), rs.getDate("time")));
            }
            System.out.println("Added " + quoteList.size() + " quotes.") ;
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void updateDatabase(Message m, TextChannel c)
    {
        try 
        {
            stmt = connection.createStatement();
            // Check if user is in database already.
            String query = "SELECT id " +
                         "FROM Users " +
                         "WHERE id = '" + m.getAuthor().getId() + "'" + 
                         ";";

            ResultSet rs = stmt.executeQuery(query);
            // No such user in database.
            if (!rs.next())
            {
                query = "INSERT INTO Users (id, name) " +
                                  "VALUES ('" + m.getAuthor().getId() + "', '" + m.getAuthor().getUsername() + "')" +
                                  ";";
                stmt.executeUpdate(query);
            }

            // Check if channel is in database already.
            query = "SELECT id " +
                    "FROM Channels " +
                    "WHERE id = '" + c.getId() + "'" + 
                    ";";

            rs = stmt.executeQuery(query);
            // No such channel in database.
            if (!rs.next())
            {
                query = "INSERT INTO Channels (id, name) " +
                        "VALUES ('" + c.getId() + "','" + c.getName() + "')" +
                        ";";
                stmt.executeUpdate(query);
            }

            if (!m.getContent().isEmpty())
            {
                // Insert message to database.
                query = "INSERT INTO Messages (message, userId, channelId, username, time) " +
                        "VALUES (?, ?, ?, ?, ?)" +
                        ";";
                java.util.Date date = new Date();
                Object param = new java.sql.Timestamp(date.getTime());

                /*System.out.println("INSERT INTO Messages (message, userId, channelId, username ,time) " +
                                   "VALUES ('" + m.getContent() + "','" + m.getAuthor().getID() + "','"
                                   + m.getChannel().getID() + "'," + m.getChannel().getName() + "', " + param.toString() + ")" +
                                   ";");
                */
                preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, m.getContent());
                preparedStmt.setString(2, m.getAuthor().getId());      
                preparedStmt.setString(3, c.getId());
                preparedStmt.setString(4, m.getAuthor().getUsername());
                preparedStmt.setObject(5, param); 
                preparedStmt.executeUpdate();  
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

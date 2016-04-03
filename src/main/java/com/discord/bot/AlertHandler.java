
package com.discord.bot;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import net.dv8tion.jda.entities.TextChannel;

public class AlertHandler
{
    private final ArrayList<Alert> alertList;
    private final Bot bot;
    private final DatabaseHandler databaseHandler;
    
    public AlertHandler(Bot bot, DatabaseHandler databaseHandler)
    {
        alertList = new ArrayList<>();
        this.bot = bot;
        this.databaseHandler = databaseHandler;
    }
    
    public void setupAlerts()
    {
        databaseHandler.readAlerts(alertList);
        System.out.println("Read " + alertList.size() + " alerts to alertlist");
    }
    
    public void setAlert(String userId, String channelId, String twitchChannel, boolean add, TextChannel channel)
    {
        // If someone already has a alert to that twitch channel in this discord channel.
        boolean existingAlert = false;
        for (Alert a : alertList)
        {
            if (add && a.getTwitchChannel().equals(twitchChannel) && !a.getChannelId().equals(channelId))
            {
                for (String u : a.getUserIdList())
                {
                    if (u.equals(userId))
                    {
                        channel.sendMessage("Alert already exists on other channel.");
                        return;
                    }
                }
            }
            
            if (a.getTwitchChannel().equals(twitchChannel) && a.getChannelId().equals(channelId))
            {
                for (String user : a.getUserIdList())
                {
                    existingAlert = true;             
                    if (user.equals(userId))
                    {
                        if (add)
                        {
                            channel.sendMessage("Alert already exists.");
                            return;
                        }
                        else
                        {
                            System.out.println("Removing alert");
                            a.getUserIdList().remove(user);
                            if (a.getUserIdList().isEmpty())
                            {
                                System.out.println("No one has alert for that twitch channel anymore.");
                                alertList.remove(a);
                            }
                            databaseHandler.removeAlert(userId, channelId, twitchChannel);
                            channel.sendMessage(":ok_hand:");
                            return;
                        }
                    }
                }
                if (existingAlert)
                {
                    System.out.println("Adding a new user to existing alert.");
                    a.getUserIdList().add(userId);
                    databaseHandler.addAlert(userId, channelId, twitchChannel);
                    channel.sendMessage(":ok_hand:");
                    return;
                }
            }   
        }
        if (add)
        {
            alertList.add(new Alert(userId, channelId, twitchChannel));
            databaseHandler.addAlert(userId, channelId, twitchChannel);
            channel.sendMessage(":ok_hand:");
        }
    }
    
    public void listAlerts(TextChannel channel, String userId)
    {
        String message = "Alerts: ";
        
        for (Alert a : alertList) 
        {
            if (a.getChannelId().equals(channel.getId()))
            {
                for (String user : a.getUserIdList())
                {
                    if (user.equals(userId)) 
                    {
                        message += a.getTwitchChannel() + ", ";
                    }
                }    
            }
        }
        
        String tmp = "";
        if (message.endsWith(", "))
        {
            tmp = message.substring(0, message.length() - 2);
        }
        if (!tmp.isEmpty())
        {
            channel.sendMessage(tmp);
        }
    }
    
    public void checkOnlineStatus(TwitchHandler twitch)
    {
        Timer onlineCheckerTimer = new Timer(true);
        onlineCheckerTimer.scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run() 
            { 
                for (Alert a : alertList)
                {
                    twitch.checkStreamOnlineStatus(a, bot);
                }
            }
        // Every 5 minutes.
        }, 0, 5 * 60 * 1000);
    }
}

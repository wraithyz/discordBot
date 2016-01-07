
package com.discord.bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mb3364.twitch.api.Twitch;
import com.mb3364.twitch.api.handlers.ChannelResponseHandler;
import com.mb3364.twitch.api.handlers.StreamResponseHandler;
import com.mb3364.twitch.api.handlers.UserFollowResponseHandler;
import com.mb3364.twitch.api.models.Channel;
import com.mb3364.twitch.api.models.Stream;
import com.mb3364.twitch.api.models.UserFollow;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchHandler
{
    private Twitch twitch;
    private String followage = "";
    private String userInfo = "";
    private String onlineStatus = "";
            
    private ZonedDateTime lastRandomStreamCall;
    private ArrayList<Integer> randomStreamNumbers;
    private HashMap<String, Boolean> streamStatus;
    
    // 95 streams.
    private final int STREAMLIMIT = 95;
    // 10 minutes.
    private final long TIMELIMIT = 10;
    
    public TwitchHandler()
    {
        twitch = new Twitch();
        randomStreamNumbers = new ArrayList<>();
        streamStatus = new HashMap<>();
        streamStatus.put("forsenlol", false);
        streamStatus.put("sodapoppin", false);
        streamStatus.put("nymn_hs", false);
        streamStatus.put("reckful", false);
    }
    
    public void followAge(String target, String user, Bot bot, sx.blah.discord.handle.obj.Channel channel)
    {
        twitch.users().getFollow(user, target, new UserFollowResponseHandler()
        {
            @Override
            public void onSuccess(UserFollow uf)
            {
                if (uf == null)
                {
                   bot.sendMessage(user + " is not following channel " + target + ".", channel);
                }
                else
                {
                    Date date = uf.getCreatedAt();
                    Date currentTime = new Date();
                    long difference = (currentTime.getTime() - date.getTime());
                    long days = TimeUnit.MILLISECONDS
                                .toDays(difference);
                    difference -= TimeUnit.DAYS.toMillis(days);
                    long hours = TimeUnit.MILLISECONDS
                                .toHours(difference);
                    int years = 0;
                    for (years = 0; days > 365; years++)
                    {
                        days -= 365;
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    String info = user + " has been following " + target + " since "+ Integer.toString(day) + "." +
                                    Integer.toString(month) + "." + Integer.toString(year) +
                                    " (" + Integer.toString(years) + " years, " + Long.toString(days) + 
                                    " days and " + Long.toString(hours) + " hours)";
                    bot.sendMessage(info, channel);
                }
            }

            @Override
            public void onFailure(int i, String string, String string1)
            {
                bot.sendMessage(user + " is not following channel " + target + ".", channel);
            }

            @Override
            public void onFailure(Throwable thrwbl)
            {
                // Not possible?
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }
    
    // Tries to find user in target streams chat.
    public String stalk(String target, String user, Bot bot)
    {
        boolean found = false;
        String json = "";
        String answer = "";
        
        try 
        {
            json = bot.readUrl("https://tmi.twitch.tv/group/user/" + target + "/chatters");
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(TwitchHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        JsonParser jsonParser = new JsonParser();
        JsonObject stalkInfo = jsonParser.parse(json)
            .getAsJsonObject().getAsJsonObject("chatters");

        for (Map.Entry<String,JsonElement> entry : stalkInfo.entrySet())
        {
            JsonArray array = entry.getValue().getAsJsonArray();

            for (JsonElement name : array) 
            {
                if (name.getAsString().equalsIgnoreCase(user))
                {
                    found = true;
                    break;
                }
            }
        }

        if (found)
        {
            answer = user + " is on " + target + " chat.";
        }
        else
        {
            answer = user + " is not on " + target + " chat."; 
        }
        return answer;
    }
    
    public String randomStream(Bot bot)
    {
        String message = "";
        String json = "";
        try 
        {                           
            ZonedDateTime timeNow = ZonedDateTime.now();
            if (lastRandomStreamCall == null || lastRandomStreamCall.compareTo(timeNow.minusMinutes(TIMELIMIT)) <= 0
                || randomStreamNumbers.size() >= STREAMLIMIT)
            {
                json = bot.readUrl("https://api.twitch.tv/kraken/beta/streams/random");
                lastRandomStreamCall = ZonedDateTime.now();
                if (randomStreamNumbers.size() >= STREAMLIMIT)
                {
                    randomStreamNumbers.clear();
                }
            }

            Random r = new Random();
            int choice = r.nextInt(99);

            while (randomStreamNumbers.contains(choice))
            {
                choice = r.nextInt(99);
            }
            if (!randomStreamNumbers.contains(choice))
            {
                randomStreamNumbers.add(choice);
            }

            JsonParser jsonParser = new JsonParser();
            JsonObject streamInfo = jsonParser.parse(json)
                .getAsJsonObject().getAsJsonArray("streams").get(choice)
                .getAsJsonObject();

            JsonObject channelInfo = jsonParser.parse(json)
                .getAsJsonObject().getAsJsonArray("streams").get(choice)
                .getAsJsonObject().getAsJsonObject("channel");

            String name = channelInfo.get("name").getAsString();
            String title = channelInfo.get("status").getAsString();
            String lang = channelInfo.get("language").getAsString();
            String game = streamInfo.get("game").getAsString();
            int viewers = streamInfo.get("viewers").getAsInt();

            String streamInfos = "Name: " + name + " || Game: " + game +
                                           " || Viewers: " + viewers + " || Title: " + 
                                           title + " || Lang: " + lang;

            if (name != null)
            {
                message = "http://www.twitch.tv/" + name;
            }

            if (name != null || game != null || title != null || lang != null)
            {
               message += " || " + streamInfos;
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(TwitchHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }
    
    public void userInfo(String username, Bot bot, sx.blah.discord.handle.obj.Channel channel)
    {
        twitch.channels().get(username, new ChannelResponseHandler()
        {
            @Override
            public void onSuccess(Channel chnl)
            {
                Date date = chnl.getCreatedAt();
                Date currentTime = new Date();
                long difference = (currentTime.getTime() - date.getTime());
                long days = TimeUnit.MILLISECONDS
                            .toDays(difference);
                difference -= TimeUnit.DAYS.toMillis(days);
                long hours = TimeUnit.MILLISECONDS
                            .toHours(difference);
                int years = 0;
                for (years = 0; days > 365; years++)
                {
                    days -= 365;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);
                String info = chnl.getName() + ": Created: " + Integer.toString(day) + "." +
                                            Integer.toString(month) + "." + Integer.toString(year) +
                                            " (" + Integer.toString(years) + " years, " + Long.toString(days) + 
                                            " days and " + Long.toString(hours) + " hours ago)" +
                                            " Followers: " + Integer.toString(chnl.getFollowers()) + "" +
                                            " Views: " + Long.toString(chnl.getViews());
                bot.sendMessage(info, channel);
     }

            @Override
            public void onFailure(int i, String string, String string1)
            {
                bot.sendMessage(username + " does not exist.", channel);
            }
            @Override
            public void onFailure(Throwable thrwbl)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }
     
    public void checkStreamOnlineStatus(Entry<String, Boolean> streamInfo, sx.blah.discord.handle.obj.Channel channel, Bot bot)
    {
        twitch.streams().get(streamInfo.getKey(), new StreamResponseHandler()
        {
            @Override
            public void onSuccess(Stream stream)
            {
                ZonedDateTime zonedDateTime = ZonedDateTime.now();
                int hour = zonedDateTime.getHour();
                int minute = zonedDateTime.getMinute();
                
                if (stream != null)
                {
                    if (!streamInfo.getValue())
                    {
                        streamInfo.setValue(true);
                        System.out.println(hour + ":" + minute + " || " + streamInfo.getKey() + " online.");
                        String message = streamInfo.getKey() + " has come online PogChamp";
                        onlineStatus = "@everyone " + message;
                        onlineStatus += " http://twitch.tv/" + streamInfo.getKey() + " || Game: " + stream.getGame() + " || Title: " + stream.getChannel().getStatus();
                        bot.sendMessage(onlineStatus, channel);
                    }
                }
                else
                {
                    streamInfo.setValue(false);
                    System.out.println(hour + ":" + minute + " || " + streamInfo.getKey() + " offline.");
                }
            }

            @Override
            public void onFailure(int i, String string, String string1)
            {

            }

            @Override
            public void onFailure(Throwable thrwbl)
            {
            }
        });
    }
    
    public void checkOnlineStatus(sx.blah.discord.handle.obj.Channel channel, Bot bot)
    {
        
        if (channel != null)
        {
            Timer uploadCheckerTimer = new Timer(true);
            uploadCheckerTimer.scheduleAtFixedRate(new TimerTask() 
            {
                @Override
                public void run() 
                { 
                    for (Entry<String, Boolean> e : streamStatus.entrySet())
                    {
                        checkStreamOnlineStatus(e, channel, bot);
                    }
                }
            }, 0, 5 * 60 * 1000);
        }  
    }
}

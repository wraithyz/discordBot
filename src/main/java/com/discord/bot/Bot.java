package com.discord.bot;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter
{
    private JDA jda;
    
    private TwitchHandler twitchHandler;
    private ImgurHandler imgurHandler;
    private DatabaseHandler databaseHandler;
    private BingHandler bingHandler;
    private ChatterBotHandler chatterBotHandler;
    private EmoteHandler emoteHandler;
    private CatHandler catHandler;
    
    private boolean loop = false;

    // Commands.
    private final String REPEAT = "!repeat";
    private final String TEST = "!test";
    private final String FOLLOWAGE = "!followage";
    private final String TWITCHINFO = "!twitchinfo";
    private final String COMMAND = "!commands";
    
    private final String RANDOMSTREAM = "!randomstream";
    private final String STREAM = "!stream";
    
    private final String BALL = "!8ball";
    private final String CHAT = "!mörkö";
    private final String LOOP = "!loop";
    private final String STALK = "!stalk";
    private final String IMGUR = "!imgur";
    
    private final String RANDOMQUOTE = "!randomquote";
    private final String QUOTE = "!quote";
    
    private final String STATS = "!stats";
    private final String BING = "!bing";
    private final String CHANNELINFO = "!channelinfo";
    private final String USERINFO = "!userinfo";
    
    private final String RANDOMEMOTE = "!randomemote";
    private final String EMOTE = "!emote";
    
    private final String QUIT = "!quit";
    private final String CAT = "!cat";
    
    private boolean loggedIn = false;
    
    private final String[] COMMANDS = { REPEAT, TEST, FOLLOWAGE, TWITCHINFO, COMMAND, STREAM, BALL, CHAT, 
                                        LOOP, STALK, IMGUR, QUOTE, STATS, BING, CHANNELINFO, 
                                        USERINFO, EMOTE, QUIT, CAT };

    public Bot() 
    {
        twitchHandler = new TwitchHandler();
        imgurHandler = new ImgurHandler();
        databaseHandler = new DatabaseHandler();
        bingHandler = new BingHandler();
        chatterBotHandler = new ChatterBotHandler();
        catHandler = new CatHandler();
        emoteHandler = new EmoteHandler();
        emoteHandler.readBttvEmotes();
        emoteHandler.readJsonEmotes(emoteHandler.readJsonFile(System.getProperty("user.dir") + "/emotes/emotes.json", StandardCharsets.UTF_8));
        emoteHandler.readCurrentEmotes();
    }
    
    public void sendMessage(String message, TextChannel channel)
    {
        if (!message.isEmpty() && channel != null)
        {
            channel.sendMessage(message);
        }
    }

    public String cleanQuotationmarks(String str)
    {
        String after = str.replaceAll("\"", "\\\\\"");
        return after;
    }
    
    public static String readAuthUrl(String urlString) throws Exception
    {
        BufferedReader reader = null;
        try 
        {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Client-ID " + AuthVariables.IMGURID);
            if (conn.getResponseCode() != 200)
            {
                return "";
            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
            {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } 
        finally 
        {
            if (reader != null)
            {
                reader.close();     
            }
        }
    }
        
    public static String readImage(String urlString, String pathTo, String imageType)
    {
        System.out.println("Downloading new emote: " + urlString);
        try 
        {
            URL url = new URL(urlString);
            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(url.openStream())) 
            {
                out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf)))
                {
                    out.write(buf, 0, n);
                }   
                out.close();
            }
            if (out.size() > 0)
            {
                String tmp = out.toString();
                if (imageType.isEmpty())
                {
                    if (tmp.startsWith("GIF"))
                    {
                        imageType = "gif";
                    }
                    else
                    {
                        imageType = "png";
                    } 
                }
                byte[] response = out.toByteArray();
                try (FileOutputStream fos = new FileOutputStream(pathTo + imageType)) 
                {
                    fos.write(response);
                }
            }
        }
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return imageType;
    }
    
    public static String readUrl(String urlString) throws Exception
    {
        BufferedReader reader = null;
        try 
        {
            
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
            {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } 
        finally 
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private String eigthBall()
    {
        Random r = new Random();
    
        int choice = 1 + r.nextInt(28);
        String response = "";

        if ( choice == 1 )
            response = "69% for sure";
        else if ( choice == 2 )
            response = "are you kidding?!";
        else if ( choice == 3 )
            response = "ask again";
        else if ( choice == 4 )
            response = "better not tell you now";
        else if ( choice == 5 )
            response = "definitely... not";
        else if ( choice == 6 )
            response = "dont bet on it";
        else if ( choice == 7 )
            response = "doubtful";
        else if ( choice == 8 )
            response = "for sure";
        else if ( choice == 9 )
            response = "forget about it";
        else if ( choice == 10 )
            response = "hah!";
        else if ( choice == 11 )
            response = "hells no.";
        else if ( choice == 12 )
            response = "if the Twitch gods grant it";
        else if ( choice == 13 )
            response = "in due time";
        else if ( choice == 14 )
            response = "indubitably!";
        else if ( choice == 15 )
            response = "it is certain";
        else if ( choice == 16 )
            response = "it is so'";
        else if ( choice == 17 )
            response = "leaning towards no";
        else if ( choice == 18 )
            response = "look deep in your heart and you will see the answer";
        else if ( choice == 19 )
            response = "most definitely";
        else if ( choice == 20 )
            response = "most likely";
        else if ( choice == 21 )
            response = "my sources say yes";
        else if ( choice == 22 )
            response = "thats a tough one";
        else if ( choice == 23 )
            response = "that's like totally a yes. Duh!";
        else if ( choice == 24 )
            response = "the answer might not be not no";
        else if ( choice == 25 )
            response = "the answer to that isnt pretty";
        else if ( choice == 26 )
            response = "the heavens point to yes";
        else if ( choice == 27 )
            response = "yesterday it would have been a yes, but today its a yep";
        else if ( choice == 28 )
            response = "you will have to wait";                                                                        
        else 
            response = "8-BALL ERROR!";
        return response;
    }
    
    
    public void login(Bot bot, JDABuilder builder)
    {
        try 
        {
            jda = builder.build();
            jda.setDebug(true);
        } 
        catch (LoginException | IllegalArgumentException ex)
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        TextChannel channel = event.getTextChannel();
        try 
        {
            if (!loggedIn)
            {
                TextChannel announceChannel = jda.getTextChannelById("95592065215246336");
                twitchHandler.checkOnlineStatus(announceChannel);
                loggedIn = true;
            }

            Message m = event.getMessage();

            String[] sep = m.getContent().split(" ");
            for (String s : sep)
            {
                boolean found = false;

                if (s.length() < 2 || s.equals("..."))
                {
                    continue;
                }
                if (s.equals("SourPlsl"))
                {
                    sendMessage("( ° ͜ʖ͡°)╭∩╮", channel);
                    continue;
                }
                if (s.endsWith("l"))
                {
                    found = emoteHandler.findEmote(s.substring(0, s.length() - 1), true, channel);
                }
                if (!found)
                {
                    emoteHandler.findEmote(s, false, channel);
                }
            }

            if (!m.getContent().startsWith("!") && !m.getContent().isEmpty())
            {
                databaseHandler.updateDatabase(m, channel);
            }

            if (m.getContent().equals(RANDOMEMOTE) || m.getContent().equals(EMOTE))
            {                        
                emoteHandler.findEmote(emoteHandler.randomEmote(), true, channel);
            }

            if (m.getContent().equals(QUIT) && m.getAuthor().getId().equals(AuthVariables.USERID))
            { 
                sendMessage("Bye guys... BibleThump", channel);
                System.exit(0);
            }

            if (m.getContent().startsWith(REPEAT))
            {
                if (m.getContent().length() > REPEAT.length())
                {
                    String repeat = m.getContent().substring(REPEAT.length() + 1);
                    sendMessage(repeat, channel);      
                }
            }

            if (m.getContent().equals(TEST))
            {
                sendMessage("dog doge", channel);
            }
            
            if (m.getContent().equals(CAT))
            {
                catHandler.randomCat(channel);
            }

            if (m.getContent().equals(CHANNELINFO))
            {
                String id = event.getTextChannel().getId();
                String name = event.getTextChannel().getName();
                sendMessage("Name: " + name + " || id: " + id, channel);
            }

            if (m.getContent().equals(USERINFO))
            {
                String id = event.getAuthor().getId();
                String name = event.getAuthor().getUsername();
                String avatar = event.getAuthor().getAvatarUrl();
                sendMessage("Name: " + name + " || Id: " + id + " || Avatar: " + avatar, channel);
            }

            if (m.getContent().equals(LOOP))
            {
                String state = "";

                if (loop)
                {
                    loop = false;
                    state = "OFF";
                }
                else
                {
                    loop = true;
                    state = "ON";
                }
                sendMessage("LOOP: " + state, channel);
            }

            // Channel stats.
            if (m.getContent().equals(STATS))
            {
                String id = event.getTextChannel().getId();
                sendMessage(databaseHandler.channelStats(id), channel);
            }
            // User stats.
            else if (m.getContent().startsWith(STATS))
            {
                if (m.getContent().length() > STATS.length())
                {
                    String username = m.getContent().substring(STATS.length() + 1);
                    String id = event.getTextChannel().getId();
                    sendMessage(databaseHandler.userStats(username, id), channel);    
                }
            }

            if (m.getContent().equals(RANDOMQUOTE) || m.getContent().equals(QUOTE))
            {
                String id = event.getTextChannel().getId();
                sendMessage(databaseHandler.randomChannelQuote(id), channel);
            }
            else if (m.getContent().startsWith(RANDOMQUOTE))
            {
                if (m.getContent().length() > RANDOMQUOTE.length())
                {
                    String username = m.getContent().substring(RANDOMQUOTE.length() + 1);
                    String id = event.getTextChannel().getId();
                    sendMessage(databaseHandler.randomUserQuote(username, id), channel);
                }
            }
            else if (m.getContent().startsWith(QUOTE))
            {
                if (m.getContent().length() > QUOTE.length())
                {
                    try
                    {
                        String msg = m.getContent().substring(QUOTE.length() + 1);
                        int quotes = Integer.parseInt(msg);
                        for (int i = 0; i < quotes && i <= 10; i++)
                        {
                            sendMessage(databaseHandler.randomChannelQuote(m.getChannelId()), channel);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("Not a number.");
                        String username = m.getContent().substring(QUOTE.length() + 1);
                        String id = event.getTextChannel().getId();
                        sendMessage(databaseHandler.randomUserQuote(username, id), channel);
                    }
                }
            }
            
            if (m.getContent().startsWith(BING))
            {
                if (m.getContent().length() > BING.length())
                {
                    String phrase = m.getContent().substring(BING.length() + 1);
                    sendMessage(bingHandler.bingSearch(phrase), channel);
                }    
            }
            if (m.getContent().startsWith(CHAT))
            {
                if (m.getContent().length() > CHAT.length())
                {
                    String question = m.getContent().substring(CHAT.length() + 1);
                    sendMessage(chatterBotHandler.findAnswer(question, loop), channel);
                }
            }

            if (m.getContent().startsWith(BALL))
            {
                if (m.getContent().length() > BALL.length())
                {
                    sendMessage(eigthBall(), channel);
                }
            }

            if (m.getContent().startsWith(FOLLOWAGE))
            {
                if (m.getContent().length() <= FOLLOWAGE.length())
                {
                    sendMessage("Please include <user> <target>", channel);
                }
                else
                {
                    String username = m.getContent().substring(FOLLOWAGE.length() + 1);
                    String target = username.substring(username.indexOf(" ") + 1);
                    String user = username.substring(0, username.indexOf(" "));
                    twitchHandler.followAge(target, user, channel);
                }
            }

            if (m.getContent().equals(COMMAND))
            {
                String message = "Commands: ";
                for (int i = 0; i < COMMANDS.length; i++)
                {
                    message += COMMANDS[i];
                    if (i + 1 != COMMANDS.length)
                    {
                        message += ", ";
                    }
                }
                sendMessage(message, channel);
            }

            if (m.getContent().startsWith(STALK))
            {
                if (m.getContent().length() <= STALK.length())
                {
                    sendMessage("Please include <user> <target>", channel);
                }
                else
                {
                    String username = m.getContent().substring(STALK.length() + 1);
                    String target = username.substring(username.indexOf(" ") + 1);
                    String user = username.substring(0, username.indexOf(" "));
                    sendMessage(twitchHandler.stalk(target, user), channel);
                }
            }

            // Random stream.
            if (m.getContent().equals(RANDOMSTREAM) || m.getContent().equals(STREAM))
            {
                sendMessage(twitchHandler.randomStream(), channel);
            }

            if (m.getContent().startsWith(TWITCHINFO))
            {
                if (m.getContent().length() <= TWITCHINFO.length())
                {
                    sendMessage("Please include <user>.", channel);
                }
                else
                {
                    String username = m.getContent().substring(TWITCHINFO.length() + 1);
                    twitchHandler.userInfo(username, channel);
                }
            }

            if (m.getContent().equals(IMGUR))
            {
                sendMessage(imgurHandler.randomImgur(), channel);
            }

            else if (m.getContent().startsWith(IMGUR))
            {
                if (m.getContent().length() <= IMGUR.length())
                {
                    sendMessage("Please include <subreddit>.", channel);
                }
                else
                {
                    String subreddit = m.getContent().substring(IMGUR.length() + 1);
                    sendMessage(imgurHandler.randomSubredditImgur(subreddit), channel);
                }
            }
		}
        catch (Exception e)
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
		}
    }
    
	public static void main(String[] args)
    {
        try 
        {
            Bot bot = new Bot();
            JDABuilder builder = new JDABuilder(AuthVariables.EMAIL, AuthVariables.PW);
            builder.addListener(bot);
            bot.login(bot, builder);
        } 
        catch (IllegalArgumentException ex) 
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}

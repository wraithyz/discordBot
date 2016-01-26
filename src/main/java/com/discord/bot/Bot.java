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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
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
    

    private boolean debug = false;
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
    
    private final String UPTIME = "!uptime";
    private final String DEBUG = "!debug";
    
    private boolean loggedIn = false;
    private Instant loggedInTime = null;

    private final String[] eightBallAnswers = { "69% for sure", "Are you kidding?!", "Ask again",
        "Better not tell you now", "Definitely... not", "Dont bet on it", "Doubtful", "For sure",
        "Forget about it", "Hah!", "Hells no.", "In due time", "Indubitably!", "It is certain",
        "It is so", "Leaning towards no", "Look deep in your heart and you will see the answer",
        "Most definitely", "Most likely", "My sources say yes", "Thats a tough one",
        "That's like totally a yes. Duh!", "The answer might not be not no",
        "The answer to that isnt pretty", "The heavens point to yes",
        "Yesterday it would have been a yes, but today its a yep", "You will have to wait" };
    
    private final String[] COMMANDS = { REPEAT, TEST, FOLLOWAGE, TWITCHINFO, COMMAND, STREAM, BALL, CHAT, 
                                        LOOP, STALK, IMGUR, QUOTE, STATS, BING, CHANNELINFO, 
                                        USERINFO, EMOTE, QUIT, CAT, UPTIME, DEBUG };

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
        
    public static String downloadEmote(String urlString, String pathTo, String imageType)
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
        return eightBallAnswers[r.nextInt(eightBallAnswers.length)];
    }
    
    
    public void login(Bot bot, JDABuilder builder)
    {
        try 
        {
            jda = builder.build();
            jda.setDebug(false);
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
                loggedInTime = Instant.now();
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
                Instant now = Instant.now();
                long uptime = Duration.between(loggedInTime, now).getSeconds();
                sendMessage("Quitting. Uptime was: " + LocalTime.MIN.plusSeconds(uptime).toString(), channel);
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
                sendMessage("dog doge xD", channel);
            }
            
            if (m.getContent().equals(DEBUG))
            {
                debug = !debug;
                jda.setDebug(debug);
                sendMessage("Debug: " + Boolean.toString(debug), channel);
            }
            
            if (m.getContent().equals(UPTIME))
            {
                if (loggedInTime != null)
                {
                    Instant now = Instant.now();
                    long uptime = Duration.between(loggedInTime, now).getSeconds();
                    sendMessage("Uptime: " + LocalTime.MIN.plusSeconds(uptime).toString(), channel);
                }
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
                if (avatar != null)
                {
                    sendMessage("Name: " + name + " || Id: " + id + " || Avatar: " + avatar, channel);
                }
                else
                {
                    sendMessage("Name: " + name + " || Id: " + id, channel);
                }
            }
            else if (m.getContent().startsWith(USERINFO))
            {
                if (m.getContent().length() > USERINFO.length())
                {
                    List<User> users = channel.getUsers();
                    String username = m.getContent().substring(USERINFO.length() + 1);
                    for (User user : users)
                    {
                        if (user.getUsername().equalsIgnoreCase(username))
                        {
                            String id = user.getId();
                            String avatar = user.getAvatarUrl();
                            if (avatar != null)
                            {
                                sendMessage("Name: " + username + " || Id: " + id + " || Avatar: " + avatar, channel);
                            }
                            else
                            {
                                sendMessage("Name: " + username + " || Id: " + id, channel);
                            }
                            break;
                        }
                    }
                }
                
            }

            if (m.getContent().equals(LOOP))
            {
                loop = !loop;
                sendMessage("LOOP: " + Boolean.toString(loop), channel);
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

package com.discord.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.util.MessageBuilder;
import java.net.HttpURLConnection;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

class Streams
{
    String channel;
    String game;
}

public class Bot
{
    IDiscordClient client;
    
    private TwitchHandler twitchHandler;
    private ImgurHandler imgurHandler;
    private DatabaseHandler databaseHandler;
    private BingHandler bingHandler;
    private ChatterBotHandler chatterBotHandler;
    
    private boolean loop = false;

    // Commands.
    private final String REPEAT = "!repeat";
    private final String TEST = "!test";
    private final String FOLLOWAGE = "!followage";
    private final String TWITCHINFO = "!twitchinfo";
    private final String COMMAND = "!commands";
    private final String RANDOM = "!random";
    private final String BALL = "!8ball";
    private final String CHAT = "!mörkö";
    private final String LOOP = "!loop";
    private final String STALK = "!stalk";
    private final String IMGUR = "!imgur";
    private final String RANDOMQUOTE = "!randomquote";
    private final String STATS = "!stats";
    private final String BING = "!bing";
    private final String CHANNELINFO = "!channelinfo";
    private final String USERINFO = "!userinfo";
    
    private boolean loggedIn = false;
    
    private final String[] COMMANDS = { REPEAT, TEST, FOLLOWAGE, TWITCHINFO, COMMAND, RANDOM, BALL, CHAT, 
                                        LOOP, STALK, IMGUR, RANDOMQUOTE, STATS, BING, CHANNELINFO, 
                                        USERINFO };

    public Bot()
    {
        twitchHandler = new TwitchHandler();
        imgurHandler = new ImgurHandler();
        //databaseHandler = new DatabaseHandler();
        bingHandler = new BingHandler();
        chatterBotHandler = new ChatterBotHandler();
         
    }
    
    public void sendMessage(String message, sx.blah.discord.handle.obj.Channel channel)
    {
        if (!message.isEmpty() && channel != null)
        {
            new MessageBuilder(client).withContent(cleanQuotationmarks(message)).withChannel(channel).build();
        }
    }

    public String cleanQuotationmarks(String str)
    {
        String after = str.replaceAll("\"", "\\\\\"");
        return after;
    }
    
    public String readAuthUrl(String urlString) throws Exception
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
        
    public String readUrl(String urlString) throws Exception
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
    
    public void loginAndHandleCommands(Bot bot)
    {
        try 
        {
			client = new ClientBuilder().withLogin(AuthVariables.EMAIL, AuthVariables.PW).login();
 
            client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {

                @Override
                public void handle(MessageReceivedEvent messageReceivedEvent)
                {
                    sx.blah.discord.handle.obj.Channel channel = messageReceivedEvent.getMessage().getChannel();
                    if (!loggedIn)
                    {
                        //sx.blah.discord.handle.obj.Channel announceChannel = client.getChannelByID("95592065215246336");
                        sx.blah.discord.handle.obj.Channel announceChannel = client.getChannelByID("107936658980093952");
                        twitchHandler.checkOnlineStatus(announceChannel, bot);
                        loggedIn = true;
                    }
                    					
                    Message m = messageReceivedEvent.getMessage();

                    
					if (!m.getContent().startsWith("!"))
					{
	                    //databaseHandler.updateDatabase(m);
					}
                    
                    if (m.getContent().startsWith(REPEAT))
                    {
                        String repeat = m.getContent().substring("!repeat".length() + 1);
                        sendMessage(repeat, channel);
                    }
                    
					if (m.getContent().equals(TEST))
                    {
                        sendMessage("dog doge", channel);
					}
                    
                    if (m.getContent().equals(CHANNELINFO))
                    {
                        String id = messageReceivedEvent.getMessage().getChannel().getID();
                        String name = messageReceivedEvent.getMessage().getChannel().getName();
                        sendMessage("Name: " + name + " || id: " + id, channel);
					}
                    
                    if (m.getContent().equals(USERINFO))
                    {
                        String id = messageReceivedEvent.getMessage().getAuthor().getID();
                        String name = messageReceivedEvent.getMessage().getAuthor().getName();
                        String avatar = messageReceivedEvent.getMessage().getAuthor().getAvatarURL();
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
                        String id = messageReceivedEvent.getMessage().getID();
                        sendMessage(databaseHandler.channelStats(id), channel);
                    }
                    // User stats.
                    else if (m.getContent().startsWith(STATS))
                    {
                        String username = m.getContent().substring(STATS.length() + 1);
                        String id = messageReceivedEvent.getMessage().getID();
                        sendMessage(databaseHandler.userStats(username, id), channel);
                    }
                    
                    if (m.getContent().equals(RANDOMQUOTE))
                    {
                        String id = messageReceivedEvent.getMessage().getID();
                        sendMessage(databaseHandler.randomChannelQuote(id), channel);
                    }
                    else if (m.getContent().startsWith(RANDOMQUOTE))
                    {
                        String username = m.getContent().substring(RANDOMQUOTE.length() + 1);
                        String id = messageReceivedEvent.getMessage().getID();
                        sendMessage(databaseHandler.randomUserQuote(username, id), channel);
                    }
                    
                    if (m.getContent().startsWith(BING))
                    {
                        if (m.getContent().length() <= BING.length())
                        {
                            sendMessage("Please include a search phrase.", channel);
                        }
                        else
                        {
                            String phrase = m.getContent().substring(BING.length() + 1);
                            sendMessage(bingHandler.bingSearch(phrase), channel);
                        }
                    }    
                            
                    if (m.getContent().startsWith(CHAT))
                    {
                        if (m.getContent().length() <= CHAT.length())
                        {
                            sendMessage("Please include a question.", channel);
                        }
                        else
                        {
                            String question = m.getContent().substring(CHAT.length() + 1);
                            sendMessage(chatterBotHandler.findAnswer(question), channel);
                        }
                    }
                    
                    if (m.getContent().startsWith(BALL))
                    {
                        if (m.getContent().length() <= BALL.length())
                        {
                            sendMessage("Please include a question.", channel);
                        }
                        else
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
                            twitchHandler.followAge(target, user, bot, channel);
                        }
					}

                    if (m.getContent().equals(COMMAND))
                    {
                        String tmp = "";
                        for (int i = 0; i < COMMANDS.length; i++)
                        {
                            tmp += COMMANDS[i];
                            if (i + 1 != COMMANDS.length)
                            {
                                tmp += ", ";
                            }
                        }
                        sendMessage(tmp, channel);
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
                            sendMessage(twitchHandler.stalk(target, user, bot), channel);
                        }
                    }
                    
                    // Random stream.
                    if (m.getContent().equals(RANDOM))
                    {
                        sendMessage(twitchHandler.randomStream(bot), channel);
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
                            twitchHandler.userInfo(username, bot, channel);
                        }
					}
                    
                    if (m.getContent().equals(IMGUR))
                    {
                        sendMessage(imgurHandler.randomImgur(bot), channel);
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
                            sendMessage(imgurHandler.randomSubredditImgur(subreddit, bot), channel);
                        }
                    }
				}
            });
		}
        catch (Exception e)
        {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
		}
    }   

	public static void main(String[] args)
    {
        Bot bot = new Bot();
        bot.loginAndHandleCommands(bot);
	}
}

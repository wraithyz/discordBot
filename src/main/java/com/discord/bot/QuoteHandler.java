
package com.discord.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

public class QuoteHandler
{
    private final ArrayList<Quote> quoteList;
    private final DatabaseHandler databaseHandler;

    public QuoteHandler(DatabaseHandler databaseHandler)
    {
        quoteList = new ArrayList<>();
        this.databaseHandler = databaseHandler;
    }
    
    public void setupQuotes()
    {
        databaseHandler.readQuotes(quoteList);
    }
    
    public void addQuote(Message m, TextChannel channel)
    {
        databaseHandler.updateDatabase(m, channel);
        quoteList.add(new Quote(m.getContent(), m.getAuthor().getId(), channel.getId(), m.getAuthor().getUsername(), new Date()));
    }
    
    public void channelStats(String channelId, TextChannel channel)
    {
        ArrayList<Quote> channelQuotes = new ArrayList<>();
        
        for (Quote q : quoteList)
        {
            if (q.getChannelId().equals(channelId))
            {
                channelQuotes.add(q);
            }
        }
        
        if (channelQuotes.isEmpty())
        {
            channel.sendMessage("No messages recorded in this channel.");
            return;
        }
        Collections.sort(channelQuotes, new Comparator<Quote>() 
        {
            @Override
            public int compare(Quote quote1, Quote quote2)
            {
                return quote1.getTime().compareTo(quote2.getTime());
            }
        });
        channel.sendMessage(channelQuotes.size() + " quotes since " + channelQuotes.get(0).getTime().toString());
    }
    
    public void userStats(String username, String channelId, TextChannel channel)
    {
        ArrayList<Quote> userQuotes = new ArrayList<>();
        
        for (Quote q : quoteList)
        {
            if (q.getChannelId().equals(channelId) && q.getUsername().equalsIgnoreCase(username))
            {
                userQuotes.add(q);
            }
        }
        
        Collections.sort(userQuotes, new Comparator<Quote>() 
        {
            @Override
            public int compare(Quote quote1, Quote quote2)
            {
                return quote1.getTime().compareTo(quote2.getTime());
            }
        });
        
        if (userQuotes.isEmpty())
        {
            channel.sendMessage("No recorded messages in this channel.");
            return;
        }
        channel.sendMessage(userQuotes.get(0).getUsername() + ": " + userQuotes.size() + " messages since " + userQuotes.get(0).getTime().toString());
    }
    
    public void randomChannelQuote(String channelId, int amount, TextChannel channel)
    {
        if (amount > 10)
        {
            amount = 10;
        }
        
        ArrayList<Quote> channelQuotes = new ArrayList<>();
        
        for (Quote q : quoteList)
        {
            if (q.getChannelId().equals(channelId))
            {
                channelQuotes.add(q);
            }
        }
        
        if (channelQuotes.isEmpty()) 
        {
            channel.sendMessage("No recorded messages in this channel.");
            return;
        }
        
        Random r = new Random();
        for (int i = 0; i < amount; i++)
        {
            int choice = r.nextInt(channelQuotes.size());
            channel.sendMessage(channelQuotes.get(choice).getUsername() + ": \"" + 
                                channelQuotes.get(choice).getContent() + "\" (" + 
                                channelQuotes.get(choice).getTime().toString()+ ")");
        }
    }
    
    public void randomUserQuote(String username, String channelId, TextChannel channel)
    {
        ArrayList<Quote> userQuotes = new ArrayList<>();
        
        for (Quote q : quoteList)
        {
            if (q.getChannelId().equals(channelId) && q.getUsername().equalsIgnoreCase(username))
            {
                userQuotes.add(q);
            }
        }
        
        if (userQuotes.isEmpty())
        {
            channel.sendMessage("No recorded messages in this channel.");
            return;
        }
        Random r = new Random();
        int choice = r.nextInt(userQuotes.size());
        channel.sendMessage(username + ": \"" + userQuotes.get(choice).getContent() + "\" (" + userQuotes.get(choice).getTime().toString() + ")");
    }  

    public void randomPhrase(String phrase, String channelId, TextChannel channel)
    {
        ArrayList<Quote> phraseQuotes = new ArrayList<>();
        
        for (Quote q : quoteList)
        {
            if (q.getContent().contains(phrase) && q.getChannelId().equals(channelId))
            {
                phraseQuotes.add(q);
            }
        }
        
        if (phraseQuotes.isEmpty())
        {
            channel.sendMessage("No messages containing such phrase in this channel.");
            return;
        }
        
        Random r = new Random();
        int choice = r.nextInt(phraseQuotes.size());
        channel.sendMessage(phraseQuotes.get(choice).getUsername()+ ": \"" + phraseQuotes.get(choice).getContent() + "\" (" + phraseQuotes.get(choice).getTime().toString() + ")");
    }
    
    public void phraseCount(String phrase, String username, String channelId, TextChannel channel)
    {
        int count = 0;
        
        for (Quote q : quoteList)
        {
            if (username.isEmpty())
            {
                if (q.getContent().contains(phrase) && q.getChannelId().equals(channelId))
                {
                    count++;
                }
            }
            else
            {
                if (q.getContent().contains(phrase) && q.getChannelId().equals(channelId) && q.getUsername().equalsIgnoreCase(username))
                {
                    count++;
                }
            }
        }   
        
        String message = username.isEmpty() ? String.valueOf(count) : username + ": " + String.valueOf(count);
        channel.sendMessage(message);
    }
}

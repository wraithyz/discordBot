
package com.discord.bot;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatterBotHandler
{
    private ChatterBotFactory factory;
    private ChatterBot bot;
    private ChatterBotSession botSession;
    
    public ChatterBotHandler()
    {
        try 
        { 
            factory = new ChatterBotFactory();
            bot = factory.create(ChatterBotType.PANDORABOTS, AuthVariables.PANDORABOT);
            botSession = bot.createSession();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ChatterBotHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String findAnswer(String question, boolean loop)
    {
        String answer = "";
        if (loop)
        {
            answer += "!chat ";
        }
        try 
        {
            answer += botSession.think(question);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ChatterBotHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }
    
}

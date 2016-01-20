/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.discord.bot;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatterBotHandler
{
    ChatterBotFactory factory;
    ChatterBot bot;
    ChatterBotSession botSession;
    
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
            answer = botSession.think(question);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ChatterBotHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }
    
}

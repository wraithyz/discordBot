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
    public String findAnswer(String question)
    {
        String answer = "";
        try 
        { 
            ChatterBotFactory factory = new ChatterBotFactory();

            ChatterBot bot1 = factory.create(ChatterBotType.PANDORABOTS, AuthVariables.PANDORABOT);
            ChatterBotSession bot1session = bot1.createSession();

            answer = bot1session.think(question);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ChatterBotHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }
    
}

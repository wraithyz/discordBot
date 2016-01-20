

package com.discord.bot;

import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.TextChannel;


public class CatHandler
{
    public void randomCat(TextChannel channel)
    {
        String json = "";
        try 
        {
            json = Bot.readUrl("http://random.cat/meow");
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(CatHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        JsonParser jsonParser = new JsonParser();
        String cat = jsonParser.parse(json).getAsJsonObject().get("file").getAsString();
        channel.sendMessage(cat);
    }
}

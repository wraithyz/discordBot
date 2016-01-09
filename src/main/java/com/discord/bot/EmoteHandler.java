

package com.discord.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EmoteHandler
{

    private ArrayList<Emote> emoteList;
    
    public EmoteHandler()
    {
        emoteList = new ArrayList<>();
    }

    // Gets bttv emotes json from bttv api.
    public void readBttvEmotes(Bot bot)
    {
        try 
        {
            String json = bot.readUrl("https://api.betterttv.net/2/emotes");
            JsonParser jsonParser = new JsonParser();
            int size = jsonParser.parse(json).getAsJsonObject().getAsJsonArray("emotes").size();
            System.out.println("Found " + size + " bttv emotes.");
            for (int i = 0; i < size; i++)
            {
                JsonObject emoteInfo = jsonParser.parse(json)
                    .getAsJsonObject().getAsJsonArray("emotes").get(i)
                    .getAsJsonObject();  
                emoteList.add(new Emote(emoteInfo.get("code").getAsString(), 
                                        emoteInfo.get("id").getAsString(), 
                                        emoteInfo.get("imageType").getAsString(), false));
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(EmoteHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Checks all already downloaded bttv emotes.
    public void readCurrentEmotes()
    {
        int found = 0;
        new File(System.getProperty("user.dir") + "/emotes").mkdirs();
        final File folder = new File(System.getProperty("user.dir") + "/emotes");
        for (final File fileEntry : folder.listFiles()) 
        {
            for (Emote e : emoteList)
            {
                if (fileEntry.getName().equals(e.getCode() + "." + e.getImagetype()))
                {
                    e.setDownloaded(true);
                    found++;
                }
            }
        }
        System.out.println(found + " bttv emotes already downloaded.");
    }
    
    // Finds if string is bttv emote. 
    public boolean findEmote(String emote, boolean large, Bot bot, sx.blah.discord.handle.obj.Channel channel)
    {
        Emote e = null;
        boolean found = false;
        for (Emote em : emoteList)
        {
            if (em.getCode().equals(emote))
            {
                e = em;
                found = true;
                break;
            }
        }
        // Found a emote.
        if (e != null)
        {
            String emotepath = System.getProperty("user.dir") + "/emotes/";
            // Emote already downloaded.
            if (e.isDownloaded())
            {
                if (large)
                {
                    bot.sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
                }
                else
                {
                    bot.sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
                }
            }
            // Downloads small and large version of emote.
            else
            {
                bot.readImage("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "1x", 
                               emotepath + e.getCode() + "." + e.getImagetype());
                bot.readImage("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "3x", 
                               emotepath + e.getCode() + "l." + e.getImagetype());
                e.setDownloaded(true);
                if (large)
                {
                    bot.sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
                }
                else
                {
                    bot.sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
                }
            }
        }
        return found;
    }
}

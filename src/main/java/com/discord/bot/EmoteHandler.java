
package com.discord.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class EmoteHandler
{

    private ArrayList<Emote> emoteList;
    private ArrayList<Emote> jsonEmotes;
    
    public EmoteHandler()
    {
        emoteList = new ArrayList<>();
        jsonEmotes = new ArrayList<>();
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
        int largeFound = 0;
        int jsonFound = 0;
        int jsonLargeFound = 0;
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
                if (fileEntry.getName().equals(e.getCode() + "l." + e.getImagetype()))
                {
                    e.setLargeDownloaded(true);
                    largeFound++;
                }
            }
            for (Emote e : jsonEmotes)
            {
                if (fileEntry.getName().equals(e.getCode() + "." + e.getImagetype()))
                {
                    e.setDownloaded(true);
                    jsonFound++;
                }
                if (fileEntry.getName().equals(e.getCode() + "l." + e.getImagetype()))
                {
                    e.setLargeDownloaded(true);
                    jsonLargeFound++;
                }
            }
        }
        System.out.println(found + " bttv emotes already downloaded.");
        System.out.println(largeFound + " large bttv emotes already downloaded.");
        System.out.println(jsonFound + " json bttv emotes already downloaded.");
        System.out.println(jsonLargeFound + " large json bttv emotes already downloaded.");
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
        if (!found)
        {
            for (Emote em : jsonEmotes)
            {
                if (em.getCode().equals(emote))
                {
                    e = em;
                    found = true;
                    break;
                }
            } 
        }
        // Found a emote.
        if (e != null)
        {
            String emotepath = System.getProperty("user.dir") + "/emotes/";
            // Large emote already downloaded.
            if (e.isLargeDownloaded() && large)
            {
                bot.sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
            }
            // Small emote is already downloaded.
            else if (e.isDownloaded() && !large)
            {
                bot.sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
            }
            // Downloads small or large version of emote.
            else
            {
                boolean success = false;

                if (large)
                {
                    success = bot.readImage("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "3x", 
                                            emotepath + e.getCode() + "l." + e.getImagetype());
                    if (success)
                    {
                        e.setLargeDownloaded(true);
                    }
                }
                else
                {
                    success = bot.readImage("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "1x", 
                                            emotepath + e.getCode() + "." + e.getImagetype());
                    if (success)
                    {
                        e.setDownloaded(true);
                    }
                }
                if (!success)
                {
                    System.out.println("Emote download failed, not posting.");
                }
                if (large && success)
                {
                    bot.sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
                }
                else if (success)
                {
                    bot.sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
                }
            }
        }
        return found;
    }
    
    
    public String readJsonFile(String path, Charset encoding) 
    {
        try 
        {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(EmoteHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public void readJsonEmotes(String emotes)
    {
        try 
        {
            JSONParser parser = new JSONParser();            
            JSONObject jSONObject = (JSONObject) parser.parse(emotes);
            Map map = (Map)jSONObject;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) 
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                jsonEmotes.add(new Emote(entry.getKey(), entry.getValue(), "png", false));
            }
            System.out.println("Found " + map.size() + " emotes from JSON.");
         } 
        catch (ParseException ex) 
        {
            Logger.getLogger(EmoteHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String randomEmote()
    {
        Random r = new Random();
        int chosen = r.nextInt(jsonEmotes.size());
        return jsonEmotes.get(chosen).getCode();
    }
}
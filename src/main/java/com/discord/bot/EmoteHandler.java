
package com.discord.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.TextChannel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class EmoteHandler
{

    private final ArrayList<Emote> emoteList;
    private final ArrayList<Emote> jsonEmotes;
    
    public EmoteHandler()
    {
        emoteList = new ArrayList<>();
        jsonEmotes = new ArrayList<>();
    }

    private void sendFile(String path, TextChannel channel)
    {
        File file = new File(path);
        channel.sendFileAsync(file, null);
    }
    
    // Gets bttv emotes json from bttv api.
    public void readBttvEmotes()
    {
        try 
        {
            String json = Bot.readUrl("https://api.betterttv.net/2/emotes");
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
                if (fileEntry.getName().equals(e.getCode() + ".png"))
                {
                    e.setDownloaded(true);
                    e.setImagetype("png");
                    jsonFound++;
                }
                else if (fileEntry.getName().equals(e.getCode() + ".gif"))
                {
                    e.setDownloaded(true);
                    e.setImagetype("gif");
                    jsonFound++;
                }
                if (fileEntry.getName().equals(e.getCode() + "l.png"))
                {
                    e.setLargeDownloaded(true);
                    e.setImagetype("png");
                    jsonLargeFound++;
                }
                else if (fileEntry.getName().equals(e.getCode() + "l.gif"))
                {
                    e.setLargeDownloaded(true);
                    e.setImagetype("gif");
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
    public boolean findEmote(String emote, boolean large, TextChannel channel)
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
                sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
            }
            // Small emote is already downloaded.
            else if (e.isDownloaded() && !large)
            {
                sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
            }
            // Downloads small or large version of emote.
            else
            {
                String imageType = "";

                if (large)
                {
                    imageType = Bot.downloadEmote("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "3x", 
                                            emotepath + e.getCode() + "l.", e.getImagetype());
                    if (!imageType.isEmpty())
                    {
                        e.setLargeDownloaded(true);
                        e.setImagetype(imageType);
                    }
                }
                else
                {
                    imageType = Bot.downloadEmote("https://cdn.betterttv.net/emote/" + e.getId() + "/" + "1x", 
                                            emotepath + e.getCode() + ".", e.getImagetype());
                    if (!imageType.isEmpty())
                    {
                        e.setDownloaded(true);
                        e.setImagetype(imageType);
                    }
                }
                if (imageType.isEmpty())
                {
                    System.out.println("Emote download failed, not posting.");
                }
                if (large && !imageType.isEmpty())
                {
                    sendFile(emotepath + e.getCode() + "l." + e.getImagetype(), channel);
                }
                else if (!imageType.isEmpty())
                {
                    sendFile(emotepath + e.getCode() + "." + e.getImagetype(), channel);
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
            Map<String, String> map = (Map)jSONObject;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) 
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                jsonEmotes.add(new Emote(entry.getKey(), entry.getValue(), "", false));
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

    public boolean addEmote(String emote, String id)
    {
        try 
        {
            boolean validUrl = Bot.checkValidUrl("https://cdn.betterttv.net/emote/" + id + "/1x");
            if (validUrl)
            {
                boolean exists = false;
                for (Emote e : jsonEmotes)
                {
                    if (e.getId().equals(id) || e.getCode().equals(emote))
                    {
                        exists = true;
                        System.out.println("Emote " + emote + " already exists in JSON.");
                        break;
                    }
                }
                if (!exists)
                {
                    for (Emote e : emoteList)
                    {
                        if (e.getId().equals(id) || e.getCode().equals(emote))
                        {
                            exists = true;
                            System.out.println("Emote " + emote + " already exists as global bttv emote.");
                            break;
                        }
                    }    
                }
                
                if (!exists)
                {
                    jsonEmotes.add(new Emote(emote, id, "", false));
                    System.out.println("Adding " + emote + ": " + id);
                    writeJsonFile();
                    return true;
                }
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(EmoteHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void writeJsonFile()
    {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Bot.JSONLOCATION, false)))) 
        {
            out.print("{");
            for (int i = 0; i < jsonEmotes.size(); i++)
            {
                out.print("\"" + jsonEmotes.get(i).getCode() + "\"" + ":" + "\"" + jsonEmotes.get(i).getId() + "\"");
                if (i + 1 != jsonEmotes.size())
                {
                    out.print(",");
                }
            }
            out.print("}");
        }
        catch (IOException e) 
        {
            System.err.println(e);
        } 
    }
    
    public boolean removeEmote(String emote)
    {
        try 
        {
            boolean exists = false;
            for (Emote e : jsonEmotes)
            {
                if (e.getCode().equals(emote))
                {
                    exists = true;
                    System.out.println("Removing emote: " + emote);
                    jsonEmotes.remove(e);
                    break;
                }
            }

            if (!exists)
            {
                writeJsonFile();
                return true;
            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(EmoteHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}


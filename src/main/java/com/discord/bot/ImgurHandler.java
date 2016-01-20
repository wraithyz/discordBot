/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.discord.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Samuli
 */
public class ImgurHandler
{
    private ZonedDateTime lastImgurCall;
    private ArrayList<Integer> randomImgurNumbers;

    private final long IMGURTIMELIMIT = 60;
    private final int IMGURLIMIT = 50;
    // 10 minutes.
    private final long TIMELIMIT = 10;
    
    public ImgurHandler()
    {
        randomImgurNumbers = new ArrayList<>();
    }
    
    public String randomImgur()
    {
        String json = "";
        String message = "";
        try 
        {  
            Random r = new Random();

            int randomPage = 1 + r.nextInt(4);

            ZonedDateTime timeNow = ZonedDateTime.now();
            if (lastImgurCall == null || lastImgurCall.compareTo(timeNow.minusMinutes(TIMELIMIT)) <= 0
                || randomImgurNumbers.size() >= IMGURLIMIT)
            {
                String url = "https://api.imgur.com/3/gallery/random/random/" + Integer.toString(randomPage);
                json = Bot.readAuthUrl(url);
                // There are 5 random imgur sites. For some reason, some of them are sometimes empty.
                // Lets get the first one thats not empty.
                for (int i = 0; json.isEmpty() || i == 5; i++)
                {
                    randomPage = 1 + r.nextInt(4);
                    url = "https://api.imgur.com/3/gallery/random/random/" + Integer.toString(randomPage);
                    json = Bot.readAuthUrl(url);
                }
                lastImgurCall = ZonedDateTime.now();
                if (randomImgurNumbers.size() >= IMGURLIMIT)
                {
                    randomImgurNumbers.clear();
                }
            }

            int choice = 1 + r.nextInt(49);

            while (randomImgurNumbers.contains(choice))
            {
                choice = 1 + r.nextInt(49);
            }
            if (!randomImgurNumbers.contains(choice))
            {
                randomImgurNumbers.add(choice);
            }

            JsonParser jsonParser = new JsonParser();
            JsonObject imgurInfo = jsonParser.parse(json)
                .getAsJsonObject().getAsJsonArray("data").get(choice)
                .getAsJsonObject();

            String link = imgurInfo.get("link").getAsString();
            String title = imgurInfo.get("title").getAsString();

            message = link + " " + title;

        }
        catch (Exception ex)
        {
            Logger.getLogger(ImgurHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    } 
    
    public String randomSubredditImgur(String subreddit)
    {
        String json = "";
        String message = "";
        try 
        {
            String url = "https://api.imgur.com/3/gallery/r/" + subreddit;
            json = Bot.readAuthUrl(url);

            if (!json.isEmpty())
            {
                JsonParser jsonParser = new JsonParser();
                int size = jsonParser.parse(json)
                    .getAsJsonObject().getAsJsonArray("data").size();

                if (size > 0)
                {
                    Random r = new Random();

                    int randomPage = r.nextInt(size);

                    JsonObject imgurInfo = jsonParser.parse(json)
                        .getAsJsonObject().getAsJsonArray("data").get(randomPage)
                        .getAsJsonObject();

                    String link = "";
                    if (imgurInfo.get("type").getAsString().equals("image/gif"))
                    {
                        link = imgurInfo.get("gifv").getAsString();
                    }
                    else
                    {
                        link = imgurInfo.get("link").getAsString();
                    }
                    String title = imgurInfo.get("title").getAsString();


                    if (!link.isEmpty() && !title.isEmpty())
                    {
                        message = link + " " + title;
                    }
                    else if (!link.isEmpty())
                    {
                        message = link;
                    }                                       
                }

            }
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ImgurHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }  
}

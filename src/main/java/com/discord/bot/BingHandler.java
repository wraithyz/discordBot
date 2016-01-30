
package com.discord.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BingHandler
{
    
    public String bingSearch(String searchQuery)
    {
        String link = "";
        try 
        {
            final String bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Image?Query=%%27%s%%27&$format=JSON&Adult=%%27Off%%27";

            final String query = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.name());
            final String bingUrl = String.format(bingUrlPattern, query);            
            
            final String accountKeyEnc = Base64.getEncoder().encodeToString((AuthVariables.ACCOUNTKEY + ":" + AuthVariables.ACCOUNTKEY).getBytes());
            
            final URL url = new URL(bingUrl);
            final URLConnection connection1 = url.openConnection();
            connection1.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection1.getInputStream()))) 
            {
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) 
                {
                    response.append(inputLine);
                }
                try
                {
                    JsonParser jsonParser = new JsonParser();
                    int size = jsonParser.parse(response.toString())
                        .getAsJsonObject().getAsJsonObject("d")
                        .getAsJsonObject().getAsJsonArray("results").size();

                    if (size > 0)
                    {
                        Random r = new Random();
                    
                        int choice = r.nextInt(size);
                        JsonObject searchInfo = jsonParser.parse(response.toString())
                            .getAsJsonObject().getAsJsonObject("d")
                            .getAsJsonObject().getAsJsonArray("results").get(choice)
                            .getAsJsonObject();

                        link = searchInfo.get("MediaUrl").getAsString();
                        link += " " + searchInfo.get("Title").getAsString();
                    }

                }
                catch (JsonSyntaxException | java.lang.IndexOutOfBoundsException e)
                {
                    return "";
                }
            }
        } 
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(BingHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException e)
        {
            Logger.getLogger(BingHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return link;
    }
}

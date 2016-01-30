
package com.discord.bot;

import java.util.ArrayList;
import java.util.HashMap;

public class Alert
{
    private final ArrayList<String> userIdList;
    private final HashMap<String, Boolean> announceList;
    private String channelId;
    private String twitchChannel;

    public Alert(String userId, String channelId, String twitchChannel)
    {
        this.userIdList = new ArrayList<>();
        this.announceList = new HashMap<>();
        this.userIdList.add(userId);
        this.channelId = channelId;
        this.twitchChannel = twitchChannel;
        this.announceList.put(channelId, true);
    }

    public HashMap<String, Boolean> getAnnounceList()
    {
        return announceList;
    }
    
    public ArrayList<String> getUserIdList()
    {
        return userIdList;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String getTwitchChannel()
    {
        return twitchChannel;
    }

    public void setTwitchChannel(String twitchChannel)
    {
        this.twitchChannel = twitchChannel;
    }
}

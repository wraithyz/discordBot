
package com.discord.bot;

import java.util.Date;

public class Quote
{
    private String content;
    private String userId;
    private String channelId;
    private String username;
    private Date time;

    public Quote(String content, String userId, String channelId, String username, Date time)
    {
        this.content = content;
        this.userId = userId;
        this.channelId = channelId;
        this.username = username;
        this.time = time;
    }

    public String getContent()
    {
        return content;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }
   
}

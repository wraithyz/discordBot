
package com.discord.bot;


public class Emote
{
    private String code;
    private String id;
    private String imagetype;
    private boolean downloaded;

    public Emote(String code, String id, String imagetype, boolean downloaded)
    {
        this.code = code;
        this.id = id;
        this.imagetype = imagetype;
        this.downloaded = downloaded;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setImagetype(String imagetype)
    {
        this.imagetype = imagetype;
    }

    public void setDownloaded(boolean downloaded)
    {
        this.downloaded = downloaded;
    }

    public String getCode()
    {
        return code;
    }

    public String getId()
    {
        return id;
    }

    public String getImagetype()
    {
        return imagetype;
    }

    public boolean isDownloaded()
    {
        return downloaded;
    }

}

package net.minecraft.client.resources.language;

import com.mojang.bridge.game.Language;

public class LanguageInfo implements Language, Comparable<LanguageInfo>
{
    private final String code;
    private final String region;
    private final String name;
    private final boolean bidirectional;

    public LanguageInfo(String pCode, String pRegion, String pName, boolean pBidirectional)
    {
        this.code = pCode;
        this.region = pRegion;
        this.name = pName;
        this.bidirectional = pBidirectional;
    }

    public String getCode()
    {
        return this.code;
    }

    public String getName()
    {
        return this.name;
    }

    public String getRegion()
    {
        return this.region;
    }

    public boolean isBidirectional()
    {
        return this.bidirectional;
    }

    public String toString()
    {
        return String.format("%s (%s)", this.name, this.region);
    }

    public boolean equals(Object pOther)
    {
        if (this == pOther)
        {
            return true;
        }
        else
        {
            return !(pOther instanceof LanguageInfo) ? false : this.code.equals(((LanguageInfo)pOther).code);
        }
    }

    public int hashCode()
    {
        return this.code.hashCode();
    }

    public int compareTo(LanguageInfo p_118954_)
    {
        return this.code.compareTo(p_118954_.code);
    }
}

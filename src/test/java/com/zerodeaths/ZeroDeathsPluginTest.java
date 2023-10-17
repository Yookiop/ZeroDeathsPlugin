package com.zerodeaths;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ZeroDeathsPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ZeroDeathsPlugin.class);
        RuneLite.main(args);
    }
}
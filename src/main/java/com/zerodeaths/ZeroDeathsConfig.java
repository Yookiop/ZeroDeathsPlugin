package com.zerodeaths;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("zerodeaths")
public interface ZeroDeathsConfig extends Config
{
	@ConfigItem(
		keyName = "zerodeaths",
		name = "Leaderboard url",
		description = "The link to check the leaderboard"
	)
	default String zerodeaths()
	{
		return "http://zerodeaths-site.s3-website-us-east-1.amazonaws.com/";
	}
}

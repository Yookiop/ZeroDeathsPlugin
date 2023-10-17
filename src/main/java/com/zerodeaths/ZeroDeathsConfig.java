package com.zerodeaths;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ZeroDeathsConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Leaderboard url",
		description = "The link to check the leaderboard"
	)
	default String greeting()
	{
		return "URL TBC";
	}
}

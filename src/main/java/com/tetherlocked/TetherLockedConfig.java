package com.tetherlocked;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface TetherLockedConfig extends Config
{
	@ConfigItem(
		keyName = "greeting",
		name = "Welcome Greeting",
		description = "The message to show to the user when they login"
	)

	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
			keyName = "tether",
			name = "Tether Length",
			description = "The distance the tether is allowed"
	)
	default int getMaxTetherLength()
	{
		return 20;
	}
}

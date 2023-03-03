/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2022, RealestFrenchy <https://github.com/RealestFrenchy>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tetherlocked;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import javax.xml.crypto.dsig.keyinfo.KeyName;

@ConfigGroup("example")
public interface TetherLockedConfig extends Config
{
	@ConfigItem(
		keyName = "screenDarken",
		name = "Screen Darken on tether break?",
		description = "Do they want the Screen to Flash on tether break?"
	)

	default Boolean screenDarken()
	{
		return true;
	}

	@ConfigItem(
			keyName = "Lockout",
			name = "HARDMODE Lockout on tether break?",
			description	= "Do you want to not be able to interact with things on tether break? NOT CURRENTLY WORKING"
	)
	default Boolean Lockout()
	{
		return true;
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

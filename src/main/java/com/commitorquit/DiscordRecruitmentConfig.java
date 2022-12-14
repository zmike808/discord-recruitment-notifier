/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020, MasterKenth
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.commitorquit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("discordrecruitment")
public interface DiscordRecruitmentConfig extends Config
{
	@ConfigSection(
		position = 1,
		name = "Webhook Options",
		description = "Manage how the plugin sends drops to your discord server"
	)
	String webhookOptionsSection = "webhookOptionsSection";


	@ConfigItem(
		keyName = "webhookurl",
		name = "Discord webhook URL(s)",
		description = "The Discord Webhook URL to use",
		section = webhookOptionsSection,
		position = 1
	)
	default String webhookUrl()
	{
		return "";
	}


	@ConfigItem(
			keyName = "sendscreenshot",
			name = "Send screenshot",
			description = "Whether to send a screenshot along with the message",
			section = webhookOptionsSection,
			position = 4
	)
	default boolean sendScreenshot()
	{
		return true;
	}

	@ConfigItem(
			keyName = "sendEmbeddedMessage",
			name = "Send embedded message",
			description = "Whether to send a embedded Discord message",
			section = webhookOptionsSection,
			position = 2
	)
	default boolean sendEmbeddedMessage()
	{
		return true;
	}
}

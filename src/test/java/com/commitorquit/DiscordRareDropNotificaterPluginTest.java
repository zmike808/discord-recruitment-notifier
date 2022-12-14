package com.commitorquit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordRareDropNotificaterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DiscordRecruitmentPlugin.class);
		RuneLite.main(args);
	}
}
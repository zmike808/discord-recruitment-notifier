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

import com.commitorquit.discord.Author;
import com.commitorquit.discord.Embed;
import com.commitorquit.discord.Field;
import com.commitorquit.discord.Webhook;
import com.google.inject.Provides;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;
import org.json.JSONObject;

@Slf4j
@PluginDescriptor(
	name = "Discord Recruitment Notificater",
	description = "Sends a  notification via Discord webhooks whenever you invite someone to your clan.",
	tags = {"discord", "clan", "recruitment", "notification"}
)
public class DiscordRecruitmentPlugin extends Plugin
{
//	private static final String PET_MESSAGE_DUPLICATE = "You have a funny feeling like you would have been followed";
//	private static final ImmutableList<String> PET_MESSAGES = ImmutableList.of(
//		"You have a funny feeling like you're being followed", "You feel something weird sneaking into your backpack",
//		"You have a funny feeling like you would have been followed", PET_MESSAGE_DUPLICATE);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private DiscordRecruitmentConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private DrawManager drawManager;

//	private final RarityChecker rarityChecker = new RarityChecker();

	private CompletableFuture<java.awt.Image> queuedScreenshot = null;

	@Provides
	DiscordRecruitmentConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordRecruitmentConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
//		JsonUtils.getInstance();
		super.startUp();
	}


	@Subscribe
	public void onChatMessage(ChatMessage event) throws ExecutionException, InterruptedException
	{
		if (event.getType() != ChatMessageType.CLAN_MESSAGE)
		{
			return;
		}
		String msg = event.getMessage();
//		msg = Text.standardize(event.getMessage());//remove color
		msg = Text.removeTags(msg).replace('\u00A0', ' ').trim();
		String target_str = " has been invited into the clan by ";
		String invited, recruiter;
		if (msg.contains(target_str))
		{
			String tosplit = msg.replace(target_str, ":");
			log.info("tosplit: " + tosplit);
			String[] splitted = tosplit.split(":");
			invited = Text.toJagexName(splitted[0]);
			recruiter = Text.toJagexName(splitted[1]);

			log.info("recruitment message: " + msg);
			log.info("splitted: " + invited + " " + recruiter);

			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "clan recruitment plugin", msg + " event type: " + event.getType(), null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "clan recruitment plugin", "invited: " + invited + " recruiter " + recruiter, null);
		}
		else
		{
			invited = null;
			recruiter = null;
		}
		log.error("invited: " + invited + " recruiter: " + recruiter);
		if (invited == null || recruiter == null)
		{
			return;
		}
		// onlynotify for your own recruitments
//		if(recruiter != getPlayerName()) {
//			return;
//		}
		Image screenshot = getScreenshot().get();
		log.warn(screenshot.toString());
		sendWebhookData(getWebhookUrls(), createRecruitmentNotification(msg)).get();
		queueScreenshot();
		sendScreenshot(getWebhookUrls(), queuedScreenshot.get());
	}
//			.exceptionally(e ->
//			{
//				log.error(String.format("onChatMessage error: %s", e.getMessage()), e);
//				log.error(event.toString());
//				return null;
//			});


//
//
//	private CompletableFuture<Boolean> canBeSent(int itemId, int quantity, Supplier<CompletableFuture<ItemData>> itemDataSupplier)
//	{
//		CompletableFuture<Boolean> result = new CompletableFuture<>();
//		ItemComposition comp = itemManager.getItemComposition(itemId);
//		String lowerName = comp.getName().toLowerCase();
//
//		List<String> whitelist = Arrays.stream(config.whiteListedItems()
//			.split(",")).filter(itemName -> itemName.length() > 0)
//			.map(String::toLowerCase).collect(Collectors.toList());
//
//		List<String> blacklist = Arrays.stream(config.ignoredKeywords()
//			.split(",")).filter(itemName -> itemName.length() > 0)
//			.map(String::toLowerCase).collect(Collectors.toList());
//
//		if(log.isDebugEnabled())
//		{
//			log.debug(String.format("Checking if %s can be sent", lowerName));
//		}
//
//		if(whitelist.stream().anyMatch(lowerName::equals)){
//			// It's an exact match with whitelist
//			// This must be sent
//
//			if(log.isDebugEnabled())
//			{
//				log.debug("We're whitelisted. We can be sent");
//			}
//
//			result.complete(true);
//			return result;
//		}
//
//		if(blacklist.stream().anyMatch(lowerName::equals)){
//			// Exact match with blacklist
//			// must be ignored
//
//			if(log.isDebugEnabled())
//			{
//				log.debug("We're blacklisted. We cannot be sent");
//			}
//
//			result.complete(false);
//			return result;
//		}
//
//		if(whitelist.stream().anyMatch(lowerName::contains)){
//			// Fuzzy whitelist
//			// is accepted
//
//			if(log.isDebugEnabled())
//			{
//				log.debug("We're fuzzy whitelisted. We can be sent");
//			}
//
//			result.complete(true);
//			return result;
//		}
//
//		if(blacklist.stream().anyMatch(lowerName::contains)){
//			// Fuzzy blacklist
//			// is ignored
//			if(log.isDebugEnabled())
//			{
//				log.debug("We're fuzzy blacklisted. We cannot be sent");
//			}
//
//			result.complete(false);
//			return result;
//		}
//
//		if(log.isDebugEnabled())
//		{
//			log.debug("We're not in any item list. We need to continue our check.");
//		}
//
//
//		return itemDataSupplier.get().thenCompose(itemData -> {
//			result.complete(meetsRequirements(itemData, quantity));
//			return result;
//		});
//	}
//
//	private CompletableFuture<Boolean> processEventNotification(LootRecordType lootRecordType, String eventName, int itemId, int quantity)
//	{
//		ItemData itemData = lootRecordType == LootRecordType.PICKPOCKET ? rarityChecker.CheckRarityPickpocket(eventName, EnrichItem(itemId), itemManager) : rarityChecker.CheckRarityEvent(eventName, EnrichItem(itemId), itemManager);
//
//		queueScreenshot();
//		clientThread.invokeLater(() -> {
//			queueLootNotification(getPlayerName(), getPlayerIconUrl(), itemId, quantity, itemData.Rarity, -1, -1, null,
//				eventName, config.webhookUrl()).thenApply(_v -> true);
//		});
//
//		return CompletableFuture.completedFuture(false);
//	}
////
//	private boolean meetsRequirements(ItemData item, int quantity)
//	{
//		if (item == null)
//		{
//			return false;
//		}
//
//		if (config.sendUniques() && item.Unique)
//		{
//			return true;
//		}
//
//		int totalGeValue = item.GePrice * quantity;
//		int totalHaValue = item.HaPrice * quantity;
//
//		boolean valueMet = totalGeValue >= config.minValue() || totalHaValue >= config.minValue();
//		boolean rarityMet = item.Rarity <= (1f / config.minRarity());
//
//		return config.andInsteadOfOr() ? (valueMet && rarityMet) : (valueMet || rarityMet);
//	}

//
//	private CompletableFuture<Boolean> processNpcNotification(NPC npc, int itemId, int quantity, float rarity)
//	{
//		int npcId = npc.getId();
//		int npcCombatLevel = npc.getCombatLevel();
//		String npcName = npc.getName();
//
//		CompletableFuture<Boolean> f = new CompletableFuture<>();
//		queueScreenshot();
//		clientThread.invokeLater(() -> {
//			queueLootNotification(getPlayerName(), getPlayerIconUrl(), itemId, quantity, rarity, npcId, npcCombatLevel,
//				npcName, null, config.webhookUrl()).handle((_v, e) ->
//			{
//				if (e != null)
//				{
//					f.completeExceptionally(e);
//				}
//				else
//				{
//					f.complete(true);
//				}
//				return null;
//			});
//		});
//
//		return f;
//	}

	private void queueScreenshot()
	{
		if (queuedScreenshot == null && config.sendScreenshot())
		{
			queuedScreenshot = getScreenshot();
		}
	}

//	private void sendScreenshotIfSupposedTo()
//	{
//		if (queuedScreenshot != null && config.sendScreenshot())
//		{
//			CompletableFuture<java.awt.Image> copy = queuedScreenshot;
//			queuedScreenshot = null;
//			copy.thenAccept(screenshot -> sendScreenshot(getWebhookUrls(), screenshot)).handle((v, e) ->
//			{
//				if (e != null)
//				{
//					log.error(String.format("sendScreenshotIfSupposedTo error: %s", e.getMessage()), e);
//				}
//				queuedScreenshot = null;
//				return null;
//			});
//		}
//	}

	//
//	private CompletableFuture<Void> queueLootNotification(String playerName, String playerIconUrl, int itemId,
//														  int quantity, float rarity, int npcId, int npcCombatLevel, String npcName, String eventName, String webhookUrl)
//	{
//		Webhook webhookData = new Webhook();
//
//		Author author = new Author();
//		author.setName(playerName);
//
//		if (playerIconUrl != null)
//		{
//			author.setIcon_url(playerIconUrl);
//		}
//
//		Embed embed = new Embed();
//		embed.setAuthor(author);
//
//		if (config.sendRarityAndValue())
//		{
//			Field rarityField = new Field();
//			rarityField.setName("Rarity");
//			rarityField.setValue(getRarityString(rarity));
//			rarityField.setInline(true);
//
//			Field haValueField = new Field();
//			haValueField.setName("HA Value");
//			haValueField.setValue(getGPValueString(itemManager.getItemComposition(itemId).getHaPrice() * quantity));
//			haValueField.setInline(true);
//
//			Field geValueField = new Field();
//			geValueField.setName("GE Value");
//			geValueField.setValue(getGPValueString(itemManager.getItemPrice(itemId) * quantity));
//			geValueField.setInline(true);
//
//			embed.setFields(new Field[]{rarityField, haValueField, geValueField});
//		}
//
//		Image thumbnail = new Image();
//		String iconUrl = ApiTool.getInstance().getIconUrl(itemId);
//		thumbnail.setUrl(iconUrl);
//		embed.setThumbnail(thumbnail);
//
//		CompletableFuture<Void> descFuture = getLootNotificationDescription(itemId, quantity, npcId, npcCombatLevel,
//			npcName, eventName, !config.sendEmbeddedMessage()).handle((notifDesc, e) ->
//		{
//			if (e != null)
//			{
//				log.error(String.format("queueLootNotification (desc %d) error: %s", itemId, e.getMessage()), e);
//			}
//			embed.setDescription(notifDesc);
//			if (!config.sendEmbeddedMessage())
//			{
//				webhookData.setContent("**" + playerName + "** - " + notifDesc);
//			}
//
//			return null;
//		});
//
//		return CompletableFuture.allOf(descFuture).thenCompose(_v ->
//		{
//			if (config.sendEmbeddedMessage())
//			{
//				webhookData.setEmbeds(new Embed[]{embed});
//			}
//			return sendWebhookData(getWebhookUrls(), webhookData);
//		});
//	}
	private Webhook createRecruitmentNotification(String msg)
	{
		Author author = new Author();
		author.setName(getPlayerName());


		/*
		 * Field rarityField = new Field(); rarityField.setName("Rarity");
		 * rarityField.setValue(getRarityString(rarity)); rarityField.setInline(true);
		 */

		Embed embed = new Embed();
		embed.setAuthor(author);
		embed.setFields(new Field[]{ /* rarityField */});
		embed.setDescription(msg);

		/*
		 * Image thumbnail = new Image(); CompletableFuture<Void> iconFuture =
		 * ApiTool.getInstance().getIconUrl("pet", -1, petName).thenAccept(iconUrl -> {
		 * thumbnail.setUrl(iconUrl); embed.setThumbnail(thumbnail); });
		 */
		Webhook webhookData = new Webhook();
		webhookData.setEmbeds(new Embed[]{embed});
		return webhookData;
	}

//	private CompletableFuture<Void> queueRecruitmentNotification(String msg)
//	{
//		Author author = new Author();
//		author.setName(getPlayerName());
//
//
//		/*
//		 * Field rarityField = new Field(); rarityField.setName("Rarity");
//		 * rarityField.setValue(getRarityString(rarity)); rarityField.setInline(true);
//		 */
//
//		Embed embed = new Embed();
//		embed.setAuthor(author);
//		embed.setFields(new Field[]{ /* rarityField */});
//		embed.setDescription(msg);
//
//		/*
//		 * Image thumbnail = new Image(); CompletableFuture<Void> iconFuture =
//		 * ApiTool.getInstance().getIconUrl("pet", -1, petName).thenAccept(iconUrl -> {
//		 * thumbnail.setUrl(iconUrl); embed.setThumbnail(thumbnail); });
//		 */
//		Webhook webhookData = new Webhook();
//		webhookData.setEmbeds(new Embed[]{embed});
//		return webhookData;
//		return CompletableFuture.allOf().thenCompose(_v ->
//		{
//			Webhook webhookData = new Webhook();
//			webhookData.setEmbeds(new Embed[]{embed});
//			return webhookData;
//			return sendWebhookData(getWebhookUrls(), webhookData);
//		});
//	}

	private CompletableFuture<java.awt.Image> getScreenshot()
	{
		CompletableFuture<java.awt.Image> f = new CompletableFuture<>();
		drawManager.requestNextFrameListener(screenshotImage ->
		{
			f.complete(screenshotImage);
		});
		return f;
	}

	private CompletableFuture<Void> sendWebhookData(List<String> webhookUrls, Webhook webhookData)
	{
		JSONObject json = new JSONObject(webhookData);
		String jsonStr = json.toString();

		List<Throwable> exceptions = new ArrayList<>();
		ApiTool apiTool = new ApiTool();
		List<CompletableFuture<Void>> sends = webhookUrls.stream()
			.map(url -> apiTool.postRaw(url, jsonStr, "application/json").handle((_v, e) ->
			{
				if (e != null)
				{
					exceptions.add(e);
				}
				return null;
			}).thenAccept(_v ->
			{
			})).collect(Collectors.toList());

		return CompletableFuture.allOf(sends.toArray(new CompletableFuture[sends.size()])).thenCompose(_v ->
		{
			if (exceptions.size() > 0)
			{
				log.error(String.format("sendWebhookData got %d error(s)", exceptions.size()));
				exceptions.forEach(t -> log.error(t.getMessage()));
				CompletableFuture<Void> f = new CompletableFuture<>();
				f.completeExceptionally(exceptions.get(0));
				return f;
			}
			return CompletableFuture.completedFuture(null);
		});
	}

	private void sendScreenshot(List<String> webhookUrls, java.awt.Image screenshot)
	{
		try
		{

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write((BufferedImage) screenshot, "png", baos);
			byte[] imageBytes = baos.toByteArray();
			ApiTool apiTool = new ApiTool();
			for (String url : webhookUrls)
			{
				apiTool.postFormImage(url, imageBytes, "image/png").get();
			}

		}
		catch (Exception e)
		{
			log.error("sendScreenshot error: " + e.getMessage(), e);
		}
	}
//			List<Throwable> exceptions = new ArrayList<>();
//						}).thenAccept(_v ->
//						{
//						}).wait();
//					}
//					catch (InterruptedException e)
//					{
//						throw new RuntimeException(e);
//					}
//				});
//
//			return CompletableFuture.allOf(sends.toArray(new CompletableFuture[sends.size()])).thenCompose(_v ->
//			{
//				if (exceptions.size() > 0)
//				{
//					log.error(String.format("sendScreenshot got %d error(s)", exceptions.size()));
//					exceptions.forEach(t -> log.error(t.getMessage()));
//					CompletableFuture<Void> f = new CompletableFuture<>();
//					f.completeExceptionally(exceptions.get(0));
//					return f;
//				}
//				return CompletableFuture.completedFuture(null);
//			});
//		}
//		catch (Exception e)
//		{
//			log.error("Unable to send screenshot", e);
//			return CompletableFuture.completedFuture(null);
//		}


	// TODO: Add Pet notification
//	private CompletableFuture<String> getLootNotificationDescription(int itemId, int quantity, int npcId,
//																	 int npcCombatLevel, String npcName, String eventName, boolean plainText)
//	{
//		ItemComposition itemComp = itemManager.getItemComposition(itemId);
//		String itemUrl = getWikiUrl(itemComp.getName());
//		String baseMsg = (plainText) ?
//			"Just got **" + (quantity > 1 ? quantity + "x " : "") + itemComp.getName() + "**" :
//			"Just got " + (quantity > 1 ? quantity + "x " : "") + "[" + itemComp.getName() + "](" + itemUrl + ")";
//
//		if (npcId >= 0)
//		{
//			String npcUrl = getWikiUrl(npcName);
//			String fullMsg = (plainText) ?
//				baseMsg + " from lvl " + npcCombatLevel + " **" + npcName + "**" :
//				baseMsg + " from lvl " + npcCombatLevel + " [" + npcName + "](" + npcUrl + ")";
//
//			return CompletableFuture.completedFuture(fullMsg);
//		}
//		else if (eventName != null)
//		{
//			String eventUrl = getWikiUrl(eventName);
//			String fullMsg = (plainText) ?
//				baseMsg + " from **" + eventName + "**" :
//				baseMsg + " from [" + eventName + "](" + eventUrl + ")";
//			return CompletableFuture.completedFuture(fullMsg);
//		}
//		else
//		{
//			return CompletableFuture.completedFuture(baseMsg + " from something");
//		}
//	}
//
//	private String getWikiUrl(String search)
//	{
//		return HttpUrl.parse("https://oldschool.runescape.wiki/").newBuilder()
//			.addPathSegments("w/Special:Search").addQueryParameter("search", search).build().toString();
//	}
//
//	private String getPetNotificationDescription(boolean isDuplicate)
//	{
//		if (isDuplicate)
//		{
//			return "Would've gotten a pet, but already has it.";
//		}
//		else
//		{
//			return "Just got a pet.";
//		}
//	}
//
//	private String getGPValueString(int value)
//	{
//		return "```fix\n" + NumberFormat.getNumberInstance(Locale.US).format(value) + " GP\n```";
//	}
//
//	private String getRarityString(float rarity)
//	{
//		return "```glsl\n# 1/" + (1 / rarity) + " (" + (rarity * 100f) + "%)\n```";
//	}
//
//	private String getPlayerIconUrl()
//	{
//		switch (client.getAccountType())
//		{
//			case IRONMAN:
//				return "https://oldschool.runescape.wiki/images/0/09/Ironman_chat_badge.png";
//			case HARDCORE_IRONMAN:
//				return "https://oldschool.runescape.wiki/images/b/b8/Hardcore_ironman_chat_badge.png";
//			case ULTIMATE_IRONMAN:
//				return "https://oldschool.runescape.wiki/images/0/02/Ultimate_ironman_chat_badge.png";
//			case GROUP_IRONMAN:
//				return "https://oldschool.runescape.wiki/images/Group_ironman_chat_badge.png";
//			case HARDCORE_GROUP_IRONMAN:
//				return "https://oldschool.runescape.wiki/images/Hardcore_group_ironman_chat_badge.png";
//			default:
//				return null;
//		}
//	}

	private String getPlayerName()
	{
		return client.getLocalPlayer().getName();
	}

	private List<String> getWebhookUrls()
	{
		return Arrays.asList(config.webhookUrl().split("\n")).stream().filter(u -> u.length() > 0).map(u -> u.trim())
			.collect(Collectors.toList());
	}
}

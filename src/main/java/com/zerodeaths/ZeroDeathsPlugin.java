package com.zerodeaths;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import javax.annotation.Nullable;

@Slf4j
@PluginDescriptor(
		name = "ZeroDeaths"
)
public class ZeroDeathsPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ConfigManager configManager;
	private String username;
	private Boolean firstRun = true;
	private Boolean playerIsDead = false;
	private Boolean scoring = false;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	@Inject
	private OkHttpClient okHttpClient;

	@Override
	protected void startUp() throws Exception
	{
		configManager.setConfiguration("player", "registered", "false");
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			if(firstRun) {
				executorService.scheduleAtFixedRate(this::checkForDeath, 10, 1, TimeUnit.SECONDS);
				executorService.schedule(this::checkAndPerformAction, 10, TimeUnit.SECONDS);
				firstRun = false;
				scoring = true;
			}
		}

		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
			firstRun = true;

			if(scoring) {
				callApi("score");
				scoring = false;
			}
		}
	}


	private void checkAndPerformAction() {
		String registered = configManager.getConfiguration("player", "registered");
		if (registered.equals("false")) {
			callApi("add");
		}
	}

	private void checkForDeath() {
		if (client.getGameState() == GameState.LOGGED_IN) {
			int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);

			log.info("died" + currentHP);

			if (currentHP <= 0) {
				if(!playerIsDead) {
					playerIsDead = true;
					callApi("died");
				}
			} else {
				playerIsDead = false;
			}
		}
	}

	private void callApi(String parameter) {
		// Get the current user's username
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null) {
			username = URLEncoder.encode(Objects.requireNonNull(localPlayer.getName()), StandardCharsets.UTF_8);
		}

		String apiURL = null;
		switch (parameter) {
			case "died":
				apiURL = "https://r7zzw8jrpg.execute-api.eu-west-1.amazonaws.com/dev/addUserDetails?username=" + username + "&died=true";
				break;
			case "score":
				apiURL = "https://r7zzw8jrpg.execute-api.eu-west-1.amazonaws.com/dev/compareAndScore?username=" + username;
				break;
			case "add":
				apiURL = "https://r7zzw8jrpg.execute-api.eu-west-1.amazonaws.com/dev/addUserDetails?username=" + username;
				configManager.setConfiguration("player", "registered", "true");
				break;
		}

        assert apiURL != null;

		RequestBody requestBody = new FormBody.Builder()
				.add("param1", "value1") // Add your POST data here
				.add("param2", "value2")
				.build();

        Request request = new Request.Builder()
				.url(apiURL)
				.post(RequestBody.create(null, new byte[0]))
				.build();

		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@Nullable  Call call, @Nullable  IOException e) {
				//Do nothing
			}

			@Override
			public void onResponse(@Nullable Call call, @Nullable Response response) throws IOException {
				//Do nothing
			}
		});
	}

	@Provides
	ZeroDeathsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZeroDeathsConfig.class);
	}
}

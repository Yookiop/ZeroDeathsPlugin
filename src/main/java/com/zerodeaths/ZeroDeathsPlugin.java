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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	@Override
	protected void startUp() throws Exception
	{

	}

	@Override
	protected void shutDown() throws Exception
	{
		callApi("died");
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

			if (currentHP <= 0) {
				if(!playerIsDead) {
					callApi("died");
					playerIsDead = true;
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

		try
		{
            assert apiURL != null;
            URL url = new URL(apiURL);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set the request method to POST or GET, depending on your API
			connection.setRequestMethod("POST"); // Change to "GET" if needed
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error calling API");
		}
	}

	@Provides
	ZeroDeathsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZeroDeathsConfig.class);
	}
}

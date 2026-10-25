package com.example.client;

import example.playerradar.TestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lista graczy online. Klik przy graczu wysyla JEDEN pakiet testowy do
 * SERWERA (ten sam typ payloadu co w oryginalnym modzie) - Fabric nie ma
 * mechanizmu wysylania customowego payloadu bezposrednio do innego gracza.
 * Twardy limit: maksymalnie 5 wyslanych pakietow na kazda sekunde, liczony
 * globalnie, niezaleznie od tego jak szybko klikasz.
 */
public class TestScreen extends Screen {

	private static final int MAX_PER_SECOND = 5;
	private static long windowStart = 0L;
	private static int sentInWindow = 0;
	private static int sequence = 0;

	public TestScreen() {
		super(Component.literal("Test pakietow - wybierz gracza"));
	}

	@Override
	protected void init() {
		if (this.minecraft == null || this.minecraft.player == null) {
			return;
		}

		List<PlayerInfo> players = new ArrayList<>(this.minecraft.player.connection.getOnlinePlayers());
		String myName = this.minecraft.player.getGameProfile().getName();
		players.removeIf(e -> e.getProfile().getName().equals(myName));
		players.sort(Comparator.comparing(e -> e.getProfile().getName()));

		int y = 32;
		for (PlayerInfo entry : players) {
			String name = entry.getProfile().getName();
			this.addRenderableWidget(Button.builder(
					Component.literal(name + "   [Wyslij pakiet]"),
					btn -> trySend(name)
			).bounds(this.width / 2 - 110, y, 220, 20).build());
			y += 24;
		}

		this.addRenderableWidget(Button.builder(Component.literal("Zamknij"), btn -> this.onClose())
				.bounds(this.width / 2 - 110, y + 10, 220, 20).build());
	}

	private void trySend(String contextName) {
		long now = System.currentTimeMillis();
		if (now - windowStart >= 1000L) {
			windowStart = now;
			sentInWindow = 0;
		}
		if (sentInWindow >= MAX_PER_SECOND) {
			if (this.minecraft != null && this.minecraft.player != null) {
				this.minecraft.player.displayClientMessage(
						Component.literal("Limit " + MAX_PER_SECOND + "/s osiagniety, poczekaj chwile."), true);
			}
			return;
		}

		sentInWindow++;
		sequence++;
		ClientPlayNetworking.send(new TestPayload(sequence));

		if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(
					Component.literal("Wyslano pakiet testowy do serwera (" + sentInWindow + "/" + MAX_PER_SECOND
							+ " w tej sekundzie), kontekst: " + contextName),
					true);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

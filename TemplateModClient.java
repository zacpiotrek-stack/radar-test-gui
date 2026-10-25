package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class TemplateModClient implements ClientModInitializer {

	private static KeyMapping openRadarTestKey;

	@Override
	public void onInitializeClient() {
		openRadarTestKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.radar_test_gui.open",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_L,
				"category.radar_test_gui"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openRadarTestKey.consumeClick()) {
				if (client.player != null) {
					client.setScreen(new TestScreen());
				}
			}
		});
	}
}

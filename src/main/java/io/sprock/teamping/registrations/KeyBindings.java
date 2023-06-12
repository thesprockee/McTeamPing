package io.sprock.teamping.registrations;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBindings {
	private KeyBindings() {
	}

	public static KeyBindings get = new KeyBindings();

	public static KeyBinding[] keyBindings;

	public static void initialize() {
		keyBindings = new KeyBinding[2];
		keyBindings[0] = new KeyBinding("key.teamping.mark", Keyboard.KEY_F, "key.sprockio.categories.teamping");
		keyBindings[1] = new KeyBinding("key.sprockio.sonar", Keyboard.KEY_U, "key.sprockio.categories.teamping");
		for (KeyBinding keyBinding : keyBindings)
			ClientRegistry.registerKeyBinding(keyBinding);
	}
}
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
		keyBindings = new KeyBinding[3];
		keyBindings[0] = new KeyBinding("key.aqupd.menu", Keyboard.KEY_F, "key.aqupd.categories.teamping");
		keyBindings[1] = new KeyBinding("key.aqupd.reset", Keyboard.KEY_U, "key.aqupd.categories.teamping");
		for (KeyBinding keyBinding : keyBindings)
			ClientRegistry.registerKeyBinding(keyBinding);
	}
}
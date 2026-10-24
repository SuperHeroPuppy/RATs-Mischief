package ladysnake.ratsmischief.client;

import ladysnake.ratsmischief.common.possession.RatPossession;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public final class RatPossessionClient {
	private static final KeyBinding RETURN_TO_BODY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.ratsmischief.return_to_body", GLFW.GLFW_KEY_V, "category.ratsmischief"));

	private RatPossessionClient() {}

	public static void initialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.getCameraEntity() == null || client.getCameraEntity() == client.player) {
				return;
			}
			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeFloat(client.player.getYaw());
			packet.writeFloat(client.player.getPitch());
			packet.writeByte((client.options.forwardKey.isPressed() ? 1 : 0) - (client.options.backKey.isPressed() ? 1 : 0));
			packet.writeByte((client.options.rightKey.isPressed() ? 1 : 0) - (client.options.leftKey.isPressed() ? 1 : 0));
			packet.writeBoolean(client.options.jumpKey.isPressed());
			packet.writeBoolean(client.options.attackKey.isPressed());
			packet.writeBoolean(RETURN_TO_BODY.wasPressed());
			ClientPlayNetworking.send(RatPossession.CONTROL_PACKET, packet);
		});
	}
}

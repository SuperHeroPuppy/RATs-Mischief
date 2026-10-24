package ladysnake.ratsmischief.client;

import ladysnake.ratsmischief.common.possession.RatPossession;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModStatusEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public final class RatPossessionClient {
	private static final KeyBinding RETURN_TO_BODY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.ratsmischief.return_to_body", GLFW.GLFW_KEY_V, "category.ratsmischief"));
	private static int controlledRatId = -1;

	private RatPossessionClient() {}

	public static boolean isControllingRat() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.player != null && client.getCameraEntity() instanceof RatEntity;
	}

	public static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(RatPossession.CAMERA_PACKET, (client, handler, buf, responseSender) -> {
			int entityId = buf.readVarInt();
			client.execute(() -> {
				controlledRatId = entityId;
				if (entityId < 0 && client.player != null) client.setCameraEntity(client.player);
			});
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				controlledRatId = -1;
				return;
			}
			if (controlledRatId >= 0 && client.world.getEntityById(controlledRatId) instanceof RatEntity rat
				&& client.getCameraEntity() != rat) {
				client.setCameraEntity(rat);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!isControllingRat()) {
				return;
			}
			RatEntity rat = (RatEntity) client.getCameraEntity();
			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();
			rat.setYaw(yaw);
			rat.setBodyYaw(yaw);
			rat.setHeadYaw(yaw);
			rat.setPitch(pitch);
			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeFloat(yaw);
			packet.writeFloat(pitch);
			packet.writeByte((client.options.forwardKey.isPressed() ? 1 : 0) - (client.options.backKey.isPressed() ? 1 : 0));
			packet.writeByte((client.options.rightKey.isPressed() ? 1 : 0) - (client.options.leftKey.isPressed() ? 1 : 0));
			packet.writeBoolean(client.options.jumpKey.isPressed());
			packet.writeBoolean(client.options.attackKey.isPressed());
			packet.writeBoolean(RETURN_TO_BODY.wasPressed());
			ClientPlayNetworking.send(RatPossession.CONTROL_PACKET, packet);
		});
		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			if (!isControllingRat()) return;
			MinecraftClient client = MinecraftClient.getInstance();
			RatEntity rat = (RatEntity) client.getCameraEntity();
			int health = (int) Math.ceil(rat.getHealth());
			int maxHealth = (int) Math.ceil(rat.getMaxHealth());
			Text status = Text.translatable("hud.ratsmischief.rat_control", health, maxHealth);
			drawContext.drawTextWithShadow(client.textRenderer, status, 8, 8, 0xFFFFFF);
			if (!client.player.hasStatusEffect(ModStatusEffects.RAT_CURSE)) {
				drawContext.drawTextWithShadow(client.textRenderer, Text.translatable("hud.ratsmischief.rat_return"), 8, 20, 0xBBBBBB);
			}
		});
	}
}

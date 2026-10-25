package ladysnake.ratsmischief.client;

import ladysnake.ratsmischief.common.possession.RatPossession;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class RatPossessionClient {
	private static final KeyBinding RETURN_TO_BODY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.ratsmischief.return_to_body", GLFW.GLFW_KEY_V, "category.ratsmischief"));
	private static int controlledRatId = -1;
	private static Vec3d bodyOrigin;
	private static float bodyYaw;
	private static float bodyPitch;
	private static boolean cursed;

	private RatPossessionClient() {}

	public static boolean isControllingRat() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.player != null && client.getCameraEntity() instanceof RatEntity;
	}

	public static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(RatPossession.CAMERA_PACKET, (client, handler, buf, responseSender) -> {
			int entityId = buf.readVarInt();
			Vec3d origin = entityId < 0 ? null : new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			float originYaw = entityId < 0 ? 0 : buf.readFloat();
			float originPitch = entityId < 0 ? 0 : buf.readFloat();
			boolean isCursed = entityId >= 0 && buf.readBoolean();
			client.execute(() -> {
				controlledRatId = entityId;
				bodyOrigin = origin;
				bodyYaw = originYaw;
				bodyPitch = originPitch;
				cursed = isCursed;
				if (entityId < 0 && client.player != null) client.setCameraEntity(client.player);
			});
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				controlledRatId = -1;
				bodyOrigin = null;
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
		WorldRenderEvents.AFTER_ENTITIES.register(RatPossessionClient::renderBodyShell);
	}

	private static void renderBodyShell(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (controlledRatId < 0 || cursed || bodyOrigin == null || !isControllingRat()) return;
		ClientPlayerEntity player = client.player;
		VertexConsumerProvider consumers = context.consumers();
		if (player == null || consumers == null) return;

		boolean wasInvisible = player.isInvisible();
		float yaw = player.getYaw();
		float pitch = player.getPitch();
		float previousYaw = player.prevYaw;
		float previousPitch = player.prevPitch;
		float body = player.bodyYaw;
		float previousBody = player.prevBodyYaw;
		float head = player.headYaw;
		float previousHead = player.prevHeadYaw;
		try {
			player.setInvisible(false);
			player.setYaw(bodyYaw);
			player.prevYaw = bodyYaw;
			player.setPitch(bodyPitch);
			player.prevPitch = bodyPitch;
			player.bodyYaw = player.prevBodyYaw = bodyYaw;
			player.headYaw = player.prevHeadYaw = bodyYaw;
			Vec3d camera = context.camera().getPos();
			int light = WorldRenderer.getLightmapCoordinates(context.world(),
				BlockPos.ofFloored(bodyOrigin.x, bodyOrigin.y + 1, bodyOrigin.z));
			context.matrixStack().push();
			try {
				client.getEntityRenderDispatcher().render(player,
					bodyOrigin.x - camera.x, bodyOrigin.y - camera.y, bodyOrigin.z - camera.z,
					bodyYaw, context.tickDelta(), context.matrixStack(), consumers, light);
			} finally {
				context.matrixStack().pop();
			}
		} finally {
			player.setInvisible(wasInvisible);
			player.setYaw(yaw);
			player.prevYaw = previousYaw;
			player.setPitch(pitch);
			player.prevPitch = previousPitch;
			player.bodyYaw = body;
			player.prevBodyYaw = previousBody;
			player.headYaw = head;
			player.prevHeadYaw = previousHead;
		}
	}
}

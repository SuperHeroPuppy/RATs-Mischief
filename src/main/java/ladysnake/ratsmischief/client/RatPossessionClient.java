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
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

public final class RatPossessionClient {
	private static final KeyBinding RETURN_TO_BODY = KeyBindingHelper.registerKeyBinding(
		new KeyBinding("key.ratsmischief.return_to_body", GLFW.GLFW_KEY_V, "category.ratsmischief"));
	private static int controlledRatId = -1;
	private static int bodySelectedSlot = -1;
	private static Vec3d bodyOrigin;
	private static float bodyYaw;
	private static float bodyPitch;
	private static boolean cursed;
	private static boolean dimensionsApplied;
	private static ItemStack[] bodyEquipment;
	private static OtherClientPlayerEntity bodyShell;
	private static double bodyFallSpeed;
	private static boolean clientRatFlagsSaved;
	private static boolean ratWasNoGravity;
	private static boolean ratWasNoClip;

	private RatPossessionClient() {}

	public static boolean isControllingRat() {
		return getControlledRat() != null;
	}

	public static RatEntity getControlledRat() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.world != null && client.world.getEntityById(controlledRatId) instanceof RatEntity rat ? rat : null;
	}

	public static boolean isReturnKey(int keyCode, int scanCode) {
		return RETURN_TO_BODY.matchesKey(keyCode, scanCode);
	}

	public static void requestReturn() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (controlledRatId >= 0 && !cursed && client.getNetworkHandler() != null) {
			ClientPlayNetworking.send(RatPossession.EXIT_PACKET, PacketByteBufs.empty());
		}
	}

	public static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(RatPossession.CAMERA_PACKET, (client, handler, buf, responseSender) -> {
			int entityId = buf.readVarInt();
			Vec3d origin = entityId < 0 ? null : new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			float originYaw = entityId < 0 ? 0 : buf.readFloat();
			float originPitch = entityId < 0 ? 0 : buf.readFloat();
			boolean isCursed = entityId >= 0 && buf.readBoolean();
			ItemStack[] equipment = entityId < 0 ? null : new ItemStack[EquipmentSlot.values().length];
			if (equipment != null) {
				for (int i = 0; i < equipment.length; i++) equipment[i] = buf.readItemStack();
			}
			client.execute(() -> {
				if (entityId < 0) restoreClientRatPhysics();
				if (entityId >= 0 && bodySelectedSlot < 0 && client.player != null) {
					bodySelectedSlot = client.player.getInventory().selectedSlot;
					client.player.getInventory().selectedSlot = 0;
				}
				controlledRatId = entityId;
				bodyOrigin = origin;
				bodyYaw = originYaw;
				bodyPitch = originPitch;
				cursed = isCursed;
				bodyEquipment = equipment;
				bodyShell = null;
				bodyFallSpeed = 0;
				if (entityId < 0 && client.player != null) {
					if (bodySelectedSlot >= 0) client.player.getInventory().selectedSlot = bodySelectedSlot;
					bodySelectedSlot = -1;
					client.setCameraEntity(client.player);
					client.player.calculateDimensions();
					dimensionsApplied = false;
				}
			});
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				restoreClientRatPhysics();
				controlledRatId = -1;
				bodySelectedSlot = -1;
				bodyOrigin = null;
				bodyEquipment = null;
				bodyShell = null;
				dimensionsApplied = false;
				return;
			}
			while (RETURN_TO_BODY.wasPressed()) requestReturn();
			RatEntity controlled = getControlledRat();
			if (controlled != null) {
				if (!clientRatFlagsSaved) {
					ratWasNoGravity = controlled.hasNoGravity();
					ratWasNoClip = controlled.noClip;
					clientRatFlagsSaved = true;
				}
				controlled.setNoGravity(true);
				controlled.noClip = true;
			}
			if (controlledRatId >= 0 && !cursed && bodyOrigin != null && bodyShell == null)
				createBodyShell(client);
			if (controlledRatId >= 0 && getControlledRat() != null && !dimensionsApplied) {
				client.player.calculateDimensions();
				dimensionsApplied = true;
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			tickBodyShell(client);
			if (!isControllingRat()) {
				return;
			}
			RatEntity rat = getControlledRat();
			float yaw = client.player.getYaw();
			float pitch = client.player.getPitch();
			rat.setPosition(client.player.getX(), client.player.getY(), client.player.getZ());
			rat.setYaw(yaw);
			rat.setBodyYaw(yaw);
			rat.setHeadYaw(yaw);
			rat.setPitch(pitch);
			rat.setVelocity(client.player.getVelocity());
			rat.setOnGround(client.player.isOnGround());
			PacketByteBuf packet = PacketByteBufs.create();
			packet.writeBoolean(client.currentScreen == null && client.options.attackKey.isPressed());
			ClientPlayNetworking.send(RatPossession.CONTROL_PACKET, packet);
		});
		WorldRenderEvents.AFTER_ENTITIES.register(RatPossessionClient::renderBodyShell);
	}

	private static void restoreClientRatPhysics() {
		RatEntity rat = getControlledRat();
		if (rat != null && clientRatFlagsSaved) {
			rat.setNoGravity(ratWasNoGravity);
			rat.noClip = ratWasNoClip;
		}
		clientRatFlagsSaved = false;
	}

	private static void createBodyShell(MinecraftClient client) {
		if (client.player == null || client.world == null) return;
		bodyShell = new OtherClientPlayerEntity(client.world, client.player.getGameProfile());
		bodyShell.setPosition(bodyOrigin.x, bodyOrigin.y, bodyOrigin.z);
		bodyShell.setYaw(bodyYaw);
		bodyShell.prevYaw = bodyYaw;
		bodyShell.setPitch(bodyPitch);
		bodyShell.prevPitch = bodyPitch;
		bodyShell.bodyYaw = bodyShell.prevBodyYaw = bodyYaw;
		bodyShell.headYaw = bodyShell.prevHeadYaw = bodyYaw;
		if (bodyEquipment != null) {
			EquipmentSlot[] slots = EquipmentSlot.values();
			for (int i = 0; i < slots.length; i++) bodyShell.equipStack(slots[i], bodyEquipment[i].copy());
		}
	}

	private static void tickBodyShell(MinecraftClient client) {
		if (bodyShell == null || bodyOrigin == null || client.world == null || client.player == null) return;
		bodyFallSpeed = Math.max(-3.92, (bodyFallSpeed - 0.08) * 0.98);
		Vec3d below = bodyOrigin.add(0, bodyFallSpeed - 0.06, 0);
		BlockHitResult hit = client.world.raycast(new RaycastContext(bodyOrigin.add(0, 0.05, 0), below,
			RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));
		if (hit.getType() == HitResult.Type.BLOCK && hit.getSide() == Direction.UP) {
			bodyOrigin = new Vec3d(bodyOrigin.x, hit.getPos().y, bodyOrigin.z);
			bodyFallSpeed = 0;
		} else {
			bodyOrigin = bodyOrigin.add(0, bodyFallSpeed, 0);
		}
		bodyShell.setPosition(bodyOrigin.x, bodyOrigin.y, bodyOrigin.z);
	}

	private static void renderBodyShell(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (controlledRatId < 0 || cursed || bodyOrigin == null || bodyShell == null || !isControllingRat()) return;
		VertexConsumerProvider consumers = context.consumers();
		if (consumers == null) return;
		Vec3d camera = context.camera().getPos();
		int light = WorldRenderer.getLightmapCoordinates(context.world(),
			BlockPos.ofFloored(bodyOrigin.x, bodyOrigin.y + 1, bodyOrigin.z));
		context.matrixStack().push();
		try {
			client.getEntityRenderDispatcher().render(bodyShell,
				bodyOrigin.x - camera.x, bodyOrigin.y - camera.y, bodyOrigin.z - camera.z,
				bodyYaw, context.tickDelta(), context.matrixStack(), consumers, light);
		} finally {
			context.matrixStack().pop();
		}
	}
}

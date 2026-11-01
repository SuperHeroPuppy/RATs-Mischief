package ladysnake.ratsmischief.client;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	private static NbtCompound bodyTrinkets;
	private static OtherClientPlayerEntity bodyShell;
	private static final Map<UUID, RemoteBody> REMOTE_BODIES = new HashMap<>();
	private static double bodyFallSpeed;
	private static boolean clientRatFlagsSaved;
	private static boolean ratWasNoGravity;
	private static boolean ratWasNoClip;

	private RatPossessionClient() {}

	public static boolean isControllingRat() {
		return getControlledRat() != null;
	}

	public static boolean isRemotePossessor(UUID playerId) {
		return REMOTE_BODIES.containsKey(playerId);
	}

	public static boolean isBodyShellEntity(Object entity) {
		if (entity == bodyShell) return true;
		for (RemoteBody remote : REMOTE_BODIES.values())
			if (entity == remote.shell) return true;
		return false;
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
			NbtCompound trinkets = entityId < 0 ? null : buf.readNbt();
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
				bodyTrinkets = trinkets;
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
		ClientPlayNetworking.registerGlobalReceiver(RatPossession.BODY_PACKET, (client, handler, buf, responseSender) -> {
			boolean active = buf.readBoolean();
			UUID playerId = buf.readUuid();
			if (!active) {
				client.execute(() -> REMOTE_BODIES.remove(playerId));
				return;
			}
			String name = buf.readString(16);
			Vec3d origin = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			float yaw = buf.readFloat();
			float pitch = buf.readFloat();
			boolean remoteCursed = buf.readBoolean();
			ItemStack[] equipment = new ItemStack[EquipmentSlot.values().length];
			for (int i = 0; i < equipment.length; i++) equipment[i] = buf.readItemStack();
			NbtCompound trinkets = buf.readNbt();
			client.execute(() -> REMOTE_BODIES.put(playerId,
				new RemoteBody(new GameProfile(playerId, name), origin, yaw, pitch, remoteCursed, equipment, trinkets)));
		});
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				restoreClientRatPhysics();
				controlledRatId = -1;
				bodySelectedSlot = -1;
				bodyOrigin = null;
				bodyEquipment = null;
				bodyTrinkets = null;
				bodyShell = null;
				REMOTE_BODIES.clear();
				dimensionsApplied = false;
				return;
			}
			while (RETURN_TO_BODY.wasPressed()) requestReturn();
			RatEntity controlled = getControlledRat();
			if (controlled != null) {
				client.player.noClip = client.player.isSpectator();
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
			for (RemoteBody remote : REMOTE_BODIES.values()) remote.tick(client);
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
		applyTrinkets(bodyShell, bodyTrinkets);
	}

	private static void applyTrinkets(OtherClientPlayerEntity shell, NbtCompound snapshot) {
		if (snapshot == null) return;
		TrinketsApi.getTrinketComponent(shell).ifPresent(component -> {
			NbtList entries = snapshot.getList("items", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < entries.size(); i++) {
				NbtCompound entry = entries.getCompound(i);
				Map<String, TrinketInventory> slots = component.getInventory().get(entry.getString("group"));
				if (slots == null) continue;
				TrinketInventory inventory = slots.get(entry.getString("slot"));
				int index = entry.getInt("index");
				if (inventory != null && index >= 0 && index < inventory.size())
					inventory.setStack(index, ItemStack.fromNbt(entry.getCompound("stack")));
			}
		});
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
		if (controlledRatId >= 0 && !cursed && bodyOrigin != null && bodyShell != null && isControllingRat())
			renderShell(context, client, bodyShell, bodyOrigin, bodyYaw);
		for (RemoteBody remote : REMOTE_BODIES.values()) {
			if (!remote.cursed && remote.shell != null)
				renderShell(context, client, remote.shell, remote.origin, remote.yaw);
		}
	}

	private static void renderShell(WorldRenderContext context, MinecraftClient client,
		OtherClientPlayerEntity shell, Vec3d origin, float yaw) {
		VertexConsumerProvider consumers = context.consumers();
		if (consumers == null) return;
		Vec3d camera = context.camera().getPos();
		if (camera.squaredDistanceTo(origin) > 128 * 128) return;
		int light = WorldRenderer.getLightmapCoordinates(context.world(),
			BlockPos.ofFloored(origin.x, origin.y + 1, origin.z));
		context.matrixStack().push();
		try {
			client.getEntityRenderDispatcher().render(shell,
				origin.x - camera.x, origin.y - camera.y, origin.z - camera.z,
				yaw, context.tickDelta(), context.matrixStack(), consumers, light);
		} finally {
			context.matrixStack().pop();
		}
	}

	private static final class RemoteBody {
		final GameProfile profile;
		Vec3d origin;
		final float yaw;
		final float pitch;
		final boolean cursed;
		final ItemStack[] equipment;
		final NbtCompound trinkets;
		OtherClientPlayerEntity shell;
		double fallSpeed;

		RemoteBody(GameProfile profile, Vec3d origin, float yaw, float pitch,
			boolean cursed, ItemStack[] equipment, NbtCompound trinkets) {
			this.profile = profile;
			this.origin = origin;
			this.yaw = yaw;
			this.pitch = pitch;
			this.cursed = cursed;
			this.equipment = equipment;
			this.trinkets = trinkets;
		}

		void tick(MinecraftClient client) {
			if (cursed || client.world == null || client.player == null) return;
			if (shell == null) {
				shell = new OtherClientPlayerEntity(client.world, profile);
				shell.setPosition(origin.x, origin.y, origin.z);
				shell.setYaw(yaw);
				shell.prevYaw = yaw;
				shell.setPitch(pitch);
				shell.prevPitch = pitch;
				shell.bodyYaw = shell.prevBodyYaw = yaw;
				shell.headYaw = shell.prevHeadYaw = yaw;
				EquipmentSlot[] slots = EquipmentSlot.values();
				for (int i = 0; i < slots.length; i++) shell.equipStack(slots[i], equipment[i].copy());
				applyTrinkets(shell, trinkets);
			}
			fallSpeed = Math.max(-3.92, (fallSpeed - 0.08) * 0.98);
			Vec3d below = origin.add(0, fallSpeed - 0.06, 0);
			BlockHitResult hit = client.world.raycast(new RaycastContext(origin.add(0, 0.05, 0), below,
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));
			if (hit.getType() == HitResult.Type.BLOCK && hit.getSide() == Direction.UP) {
				origin = new Vec3d(origin.x, hit.getPos().y, origin.z);
				fallSpeed = 0;
			} else {
				origin = origin.add(0, fallSpeed, 0);
			}
			shell.setPosition(origin.x, origin.y, origin.z);
		}
	}
}

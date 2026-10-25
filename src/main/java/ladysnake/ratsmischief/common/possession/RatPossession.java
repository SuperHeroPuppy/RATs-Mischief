package ladysnake.ratsmischief.common.possession;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModEntities;
import ladysnake.ratsmischief.common.init.ModStatusEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** The Fabric replacement for the rat-specific possession provided by Requiem. */
public final class RatPossession {
	public static final Identifier CONTROL_PACKET = RatsMischief.id("rat_control");
	public static final Identifier EXIT_PACKET = RatsMischief.id("rat_exit");
	public static final Identifier CAMERA_PACKET = RatsMischief.id("rat_camera");
	private static final String RECOVERY_TAG_PREFIX = "ratsmischief_rat_control_";
	private static final String HOST_RECOVERY_TAG_PREFIX = "ratsmischief_rat_host_";
	private static final String BODY_INVENTORY_TAG = "ratsmischief_body_inventory";
	private static final String BODY_SELECTED_SLOT_TAG = "ratsmischief_body_selected_slot";
	private static final UUID SPEED_MODIFIER_ID = UUID.fromString("a2ebbb6b-fd10-4a30-a0c7-dadb9700732e");
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private RatPossession() {}

	public static RatEntity getControlledRat(PlayerEntity player) {
		if (player.getWorld().isClient) return null;
		Session session = SESSIONS.get(player.getUuid());
		return session == null ? null : session.rat;
	}

	public static boolean isControlledRat(RatEntity rat) {
		if (rat.getWorld().isClient) return false;
		return SESSIONS.values().stream().anyMatch(session -> session.rat == rat);
	}

	public static void writeBodyInventory(PlayerEntity player, NbtCompound nbt) {
		if (player.getWorld().isClient) return;
		Session session = SESSIONS.get(player.getUuid());
		if (session != null) {
			nbt.put(BODY_INVENTORY_TAG, session.inventoryBackup.copy());
			nbt.putInt(BODY_SELECTED_SLOT_TAG, session.selectedSlot);
		}
	}

	public static void recoverBodyInventory(PlayerEntity player, NbtCompound nbt) {
		if (player.getWorld().isClient || !nbt.contains(BODY_INVENTORY_TAG, NbtElement.LIST_TYPE)) return;
		NbtList backup = nbt.getList(BODY_INVENTORY_TAG, NbtElement.COMPOUND_TYPE);
		restoreBodyInventory(player, backup);
		if (nbt.contains(BODY_SELECTED_SLOT_TAG, NbtElement.INT_TYPE))
			player.getInventory().selectedSlot = nbt.getInt(BODY_SELECTED_SLOT_TAG);
	}

	private static void restoreBodyInventory(PlayerEntity player, NbtList backup) {
		ArrayList<ItemStack> acquired = new ArrayList<>();
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty()) acquired.add(stack.copy());
			player.getInventory().setStack(i, ItemStack.EMPTY);
		}
		player.getInventory().readNbt(backup);
		for (ItemStack stack : acquired) {
			player.getInventory().insertStack(stack);
			if (!stack.isEmpty()) player.dropItem(stack, false);
		}
		player.getInventory().markDirty();
		player.currentScreenHandler.sendContentUpdates();
	}

	public static boolean isControllerAndHost(Entity first, Entity second) {
		if (first instanceof RatEntity rat && second instanceof PlayerEntity player)
			return isControllerAndHost(player, rat);
		if (!(first instanceof PlayerEntity player) || !(second instanceof RatEntity rat)) return false;
		RatEntity controlled = player.getWorld().isClient
			? (player instanceof RatPossessionView view ? view.ratsmischief$getControlledRat() : null)
			: getControlledRat(player);
		return controlled == rat;
	}

	public static void initialize() {
		ServerPlayNetworking.registerGlobalReceiver(CONTROL_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean attack = buf.readBoolean();
			server.execute(() -> control(player, attack));
		});
		ServerPlayNetworking.registerGlobalReceiver(EXIT_PACKET, (server, player, handler, buf, responseSender) ->
			server.execute(() -> {
				Session session = SESSIONS.get(player.getUuid());
				if (session != null && !session.cursed) end(player, session, false);
			}));
		ServerTickEvents.END_SERVER_TICK.register(RatPossession::tick);
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof RatEntity rat) recoverHostState(rat);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.player;
			Session session = SESSIONS.get(player.getUuid());
			if (session != null) end(player, session, false);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> {
			recoverPlayerState(handler.player);
			if (handler.player.hasStatusEffect(ModStatusEffects.RAT_CURSE)) {
				beginCurse(handler.player);
			}
		}));
	}

	public static boolean beginSpy(ServerPlayerEntity player, RatEntity rat) {
		if (SESSIONS.containsKey(player.getUuid()) || !rat.isAlive() || !rat.isSpy() || !rat.isOwner(player)) {
			return false;
		}
		start(player, rat, false);
		return true;
	}

	public static void beginCurse(ServerPlayerEntity player) {
		if (SESSIONS.containsKey(player.getUuid())) {
			return;
		}
		RatEntity rat = new RatEntity(ModEntities.RAT, player.getWorld());
		rat.copyPositionAndRotation(player);
		rat.setTamed(true);
		rat.setOwner(player);
		if (player.getWorld().spawnEntity(rat)) {
			start(player, rat, true);
		}
	}

	public static void endCurse(ServerPlayerEntity player) {
		Session session = SESSIONS.get(player.getUuid());
		if (session != null && session.cursed) {
			end(player, session, false);
		}
	}

	private static void start(ServerPlayerEntity player, RatEntity rat, boolean cursed) {
		int previousFlags = (player.isInvisible() ? 1 : 0) | (player.isInvulnerable() ? 2 : 0)
			| (player.hasNoGravity() ? 4 : 0) | (player.noClip ? 8 : 0);
		Session session = new Session(rat, player.getPos(), player.getYaw(), player.getPitch(),
			cursed, rat.isAiDisabled(), rat.hasNoGravity(), rat.noClip, previousFlags,
			player.getInventory().writeNbt(new NbtList()), captureBodyEquipment(player),
			player.getInventory().selectedSlot, player.getAbilities().allowFlying,
			player.getAbilities().flying, player.interactionManager.getGameMode());
		SESSIONS.put(player.getUuid(), session);
		rat.addCommandTag(HOST_RECOVERY_TAG_PREFIX + ((session.wasAiDisabled ? 1 : 0)
			+ (session.wasNoGravity ? 2 : 0) + (session.wasNoClip ? 4 : 0)));
		for (int i = 0; i < player.getInventory().size(); i++)
			player.getInventory().setStack(i, ItemStack.EMPTY);
		player.getInventory().selectedSlot = 0;
		ItemStack ratHand = rat.getMainHandStack();
		if (!ratHand.isEmpty()) {
			player.getInventory().setStack(0, ratHand.copy());
			rat.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
		player.getInventory().markDirty();
		player.currentScreenHandler.sendContentUpdates();
		rat.setAiDisabled(true);
		rat.getNavigation().stop();
		rat.setTarget(null);
		// A thrown or sitting spy rat must return to its normal grounded pose.
		rat.setFlying(false);
		rat.setSitting(false);
		rat.setSniffing(false);
		rat.setEating(false);
		rat.setVelocity(Vec3d.ZERO);
		rat.setNoGravity(true);
		rat.noClip = true;
		player.addCommandTag(RECOVERY_TAG_PREFIX + previousFlags);
		player.setInvisible(true);
		player.setInvulnerable(true);
		player.setNoGravity(false);
		player.noClip = false;
		player.setVelocity(Vec3d.ZERO);
		player.getAbilities().flying = false;
		player.getAbilities().allowFlying = false;
		player.sendAbilitiesUpdate();
		EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed != null) {
			speed.removeModifier(SPEED_MODIFIER_ID);
			double currentSpeed = speed.getValue();
			double ratSpeed = rat.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 0.34;
			if (currentSpeed > 0) {
				speed.addTemporaryModifier(new EntityAttributeModifier(SPEED_MODIFIER_ID,
					"Rat possession movement", ratSpeed / currentSpeed - 1,
					EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
			}
		}
		player.calculateDimensions();
		player.teleport((ServerWorld) rat.getWorld(), rat.getX(), rat.getY(), rat.getZ(), rat.getYaw(), rat.getPitch());
		sendCamera(player, rat.getId(), session);
	}

	private static void control(ServerPlayerEntity player, boolean attack) {
		Session session = SESSIONS.get(player.getUuid());
		if (session == null) {
			return;
		}
		RatEntity rat = session.rat;
		if (!rat.isAlive()) {
			return;
		}
		if (attack && session.attackCooldown == 0) {
			rat.getWorld().getOtherEntities(rat, rat.getBoundingBox().expand(2.5), entity -> entity instanceof LivingEntity && entity.isAlive() && entity != player)
				.stream().min((a, b) -> Double.compare(a.squaredDistanceTo(rat), b.squaredDistanceTo(rat)))
				.ifPresent(rat::tryAttack);
			session.attackCooldown = 10;
		}
	}

	private static void tick(MinecraftServer server) {
		for (Map.Entry<UUID, Session> entry : new ArrayList<>(SESSIONS.entrySet())) {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
			Session session = entry.getValue();
			if (player == null) {
				session.rat.setAiDisabled(session.wasAiDisabled);
				session.rat.setNoGravity(session.wasNoGravity);
				session.rat.noClip = session.wasNoClip;
				clearHostRecoveryTag(session.rat);
				if (session.cursed) session.rat.discard();
				SESSIONS.remove(entry.getKey());
				continue;
			}
			if (!session.rat.isAlive() || (session.cursed && !player.hasStatusEffect(ModStatusEffects.RAT_CURSE))) {
				boolean kill = session.cursed && !session.rat.isAlive();
				end(player, session, kill);
				continue;
			}
			if (session.attackCooldown > 0) session.attackCooldown--;
			if (player.getAbilities().allowFlying || player.getAbilities().flying) {
				player.getAbilities().allowFlying = false;
				player.getAbilities().flying = false;
				player.sendAbilitiesUpdate();
			}
			followPlayer(session.rat, player);
		}
	}

	private static void followPlayer(RatEntity rat, ServerPlayerEntity player) {
		rat.setPosition(player.getX(), player.getY(), player.getZ());
		rat.setYaw(player.getYaw());
		rat.setBodyYaw(player.getYaw());
		rat.setHeadYaw(player.getYaw());
		rat.setPitch(player.getPitch());
		rat.setVelocity(player.getVelocity());
		rat.setOnGround(player.isOnGround());
		rat.fallDistance = 0;
	}

	private static void end(ServerPlayerEntity player, Session session, boolean kill) {
		SESSIONS.remove(player.getUuid());
		ItemStack ratHand = player.getInventory().removeStack(0);
		if (!ratHand.isEmpty()) {
			if (session.rat.isAlive() && session.rat.getMainHandStack().isEmpty())
				session.rat.equipStack(EquipmentSlot.MAINHAND, ratHand);
			else player.dropItem(ratHand, false);
		}
		restoreBodyInventory(player, session.inventoryBackup);
		player.getInventory().selectedSlot = session.selectedSlot;
		RatEntity rat = session.rat;
		Vec3d destination = session.cursed ? rat.getPos() : session.origin;
		player.setCameraEntity(player);
		sendCamera(player, -1, null);
		player.removeScoreboardTag(RECOVERY_TAG_PREFIX + session.previousFlags);
		restorePlayerFlags(player, session.previousFlags);
		if (player.interactionManager.getGameMode() == session.startGameMode) {
			player.getAbilities().allowFlying = session.wasAllowedToFly;
			player.getAbilities().flying = session.wasFlying;
		} else {
			player.interactionManager.getGameMode().setAbilities(player.getAbilities());
		}
		player.sendAbilitiesUpdate();
		player.calculateDimensions();
		player.setVelocity(Vec3d.ZERO);
		player.teleport((ServerWorld) player.getWorld(), destination.x, destination.y, destination.z, player.getYaw(), player.getPitch());
		rat.setAiDisabled(session.wasAiDisabled);
		rat.setNoGravity(session.wasNoGravity);
		rat.noClip = session.wasNoClip;
		clearHostRecoveryTag(rat);
		if (session.cursed) rat.discard();
		if (kill) player.kill();
	}

	private static void sendCamera(ServerPlayerEntity player, int entityId, Session session) {
		if (!player.networkHandler.isConnectionOpen()) return;
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(entityId);
		if (session != null) {
			buf.writeDouble(session.origin.x);
			buf.writeDouble(session.origin.y);
			buf.writeDouble(session.origin.z);
			buf.writeFloat(session.originYaw);
			buf.writeFloat(session.originPitch);
			buf.writeBoolean(session.cursed);
			for (ItemStack stack : session.bodyEquipment) buf.writeItemStack(stack);
		}
		ServerPlayNetworking.send(player, CAMERA_PACKET, buf);
	}

	private static void recoverPlayerState(ServerPlayerEntity player) {
		for (String tag : new ArrayList<>(player.getCommandTags())) {
			if (tag.startsWith(RECOVERY_TAG_PREFIX)) {
				try {
					restorePlayerFlags(player, Integer.parseInt(tag.substring(RECOVERY_TAG_PREFIX.length())));
				} catch (NumberFormatException ignored) {
					restorePlayerFlags(player, 0);
				}
				player.removeScoreboardTag(tag);
			}
		}
	}

	private static void restorePlayerFlags(ServerPlayerEntity player, int flags) {
		EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed != null) speed.removeModifier(SPEED_MODIFIER_ID);
		player.setInvisible((flags & 1) != 0);
		player.setInvulnerable((flags & 2) != 0);
		player.setNoGravity((flags & 4) != 0);
		player.noClip = (flags & 8) != 0;
	}

	private static void recoverHostState(RatEntity rat) {
		if (isControlledRat(rat)) return;
		for (String tag : new ArrayList<>(rat.getCommandTags())) {
			if (!tag.startsWith(HOST_RECOVERY_TAG_PREFIX)) continue;
			try {
				int flags = Integer.parseInt(tag.substring(HOST_RECOVERY_TAG_PREFIX.length()));
				rat.setAiDisabled((flags & 1) != 0);
				rat.setNoGravity((flags & 2) != 0);
				rat.noClip = (flags & 4) != 0;
			} catch (NumberFormatException ignored) {
				rat.setAiDisabled(false);
				rat.setNoGravity(false);
				rat.noClip = false;
			}
			rat.removeScoreboardTag(tag);
		}
	}

	private static void clearHostRecoveryTag(RatEntity rat) {
		for (String tag : new ArrayList<>(rat.getCommandTags()))
			if (tag.startsWith(HOST_RECOVERY_TAG_PREFIX)) rat.removeScoreboardTag(tag);
	}

	private static ItemStack[] captureBodyEquipment(PlayerEntity player) {
		EquipmentSlot[] slots = EquipmentSlot.values();
		ItemStack[] stacks = new ItemStack[slots.length];
		for (int i = 0; i < slots.length; i++) stacks[i] = player.getEquippedStack(slots[i]).copy();
		return stacks;
	}

	private static final class Session {
		final RatEntity rat;
		final Vec3d origin;
		final float originYaw;
		final float originPitch;
		final boolean cursed;
		final boolean wasAiDisabled;
		final boolean wasNoGravity;
		final boolean wasNoClip;
		final int previousFlags;
		final NbtList inventoryBackup;
		final ItemStack[] bodyEquipment;
		final int selectedSlot;
		final boolean wasAllowedToFly;
		final boolean wasFlying;
		final GameMode startGameMode;
		int attackCooldown;

		Session(RatEntity rat, Vec3d origin, float originYaw, float originPitch,
		        boolean cursed, boolean wasAiDisabled, boolean wasNoGravity,
		        boolean wasNoClip, int previousFlags, NbtList inventoryBackup, ItemStack[] bodyEquipment,
		        int selectedSlot, boolean wasAllowedToFly, boolean wasFlying, GameMode startGameMode) {
			this.rat = rat;
			this.origin = origin;
			this.originYaw = originYaw;
			this.originPitch = originPitch;
			this.cursed = cursed;
			this.wasAiDisabled = wasAiDisabled;
			this.wasNoGravity = wasNoGravity;
			this.wasNoClip = wasNoClip;
			this.previousFlags = previousFlags;
			this.inventoryBackup = inventoryBackup;
			this.bodyEquipment = bodyEquipment;
			this.selectedSlot = selectedSlot;
			this.wasAllowedToFly = wasAllowedToFly;
			this.wasFlying = wasFlying;
			this.startGameMode = startGameMode;
		}
	}
}

package ladysnake.ratsmischief.common.possession;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModEntities;
import ladysnake.ratsmischief.common.init.ModStatusEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** The Fabric replacement for the rat-specific possession provided by Requiem. */
public final class RatPossession {
	public static final Identifier CONTROL_PACKET = RatsMischief.id("rat_control");
	public static final Identifier CAMERA_PACKET = RatsMischief.id("rat_camera");
	private static final String RECOVERY_TAG_PREFIX = "ratsmischief_rat_control_";
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private RatPossession() {}

	public static void initialize() {
		ServerPlayNetworking.registerGlobalReceiver(CONTROL_PACKET, (server, player, handler, buf, responseSender) -> {
			float yaw = buf.readFloat();
			float pitch = buf.readFloat();
			int forward = buf.readByte();
			int sideways = buf.readByte();
			boolean jump = buf.readBoolean();
			boolean attack = buf.readBoolean();
			boolean exit = buf.readBoolean();
			server.execute(() -> control(player, yaw, pitch, forward, sideways, jump, attack, exit));
		});
		ServerTickEvents.END_SERVER_TICK.register(RatPossession::tick);
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
		Session session = new Session(rat, player.getPos(), cursed, rat.isAiDisabled(), previousFlags);
		SESSIONS.put(player.getUuid(), session);
		rat.setAiDisabled(true);
		player.addCommandTag(RECOVERY_TAG_PREFIX + previousFlags);
		player.setInvisible(true);
		player.setInvulnerable(true);
		player.setNoGravity(true);
		player.noClip = true;
		player.setVelocity(Vec3d.ZERO);
		player.teleport((ServerWorld) rat.getWorld(), rat.getX(), rat.getY(), rat.getZ(), rat.getYaw(), rat.getPitch());
		player.setCameraEntity(rat);
		sendCamera(player, rat.getId());
	}

	private static void control(ServerPlayerEntity player, float yaw, float pitch, int forward, int sideways,
	                            boolean jump, boolean attack, boolean exit) {
		Session session = SESSIONS.get(player.getUuid());
		if (session == null) {
			return;
		}
		if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
			return;
		}
		if (exit && !session.cursed) {
			end(player, session, false);
			return;
		}
		RatEntity rat = session.rat;
		if (!rat.isAlive()) {
			return;
		}
		rat.setYaw(yaw);
		rat.setBodyYaw(yaw);
		rat.setHeadYaw(yaw);
		rat.setPitch(pitch);
		float radians = yaw * MathHelper.RADIANS_PER_DEGREE;
		forward = MathHelper.clamp(forward, -1, 1);
		sideways = MathHelper.clamp(sideways, -1, 1);
		float x = -MathHelper.sin(radians) * forward - MathHelper.cos(radians) * sideways;
		float z = MathHelper.cos(radians) * forward - MathHelper.sin(radians) * sideways;
		Vec3d direction = new Vec3d(x, 0, z);
		if (direction.lengthSquared() > 1) {
			direction = direction.normalize();
		}
		double verticalVelocity = rat.isFlying() && jump ? 0.25 : rat.getVelocity().y;
		rat.setVelocity(direction.x * 0.3, verticalVelocity, direction.z * 0.3);
		rat.velocityDirty = true;
		if (jump && rat.isOnGround()) {
			rat.setVelocity(rat.getVelocity().x, 0.42, rat.getVelocity().z);
		} else if (jump && rat.isTouchingWater()) {
			rat.setVelocity(rat.getVelocity().x, 0.2, rat.getVelocity().z);
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
			player.setVelocity(Vec3d.ZERO);
			player.noClip = true;
			player.fallDistance = 0;
			if (player.squaredDistanceTo(session.rat) > 1) {
				player.teleport((ServerWorld) session.rat.getWorld(), session.rat.getX(), session.rat.getY(), session.rat.getZ(), player.getYaw(), player.getPitch());
			}
			if (player.getCameraEntity() != session.rat) {
				player.setCameraEntity(session.rat);
				sendCamera(player, session.rat.getId());
			}
		}
	}

	private static void end(ServerPlayerEntity player, Session session, boolean kill) {
		SESSIONS.remove(player.getUuid());
		RatEntity rat = session.rat;
		Vec3d destination = session.cursed ? rat.getPos() : session.origin;
		player.setCameraEntity(player);
		sendCamera(player, -1);
		player.removeScoreboardTag(RECOVERY_TAG_PREFIX + session.previousFlags);
		restorePlayerFlags(player, session.previousFlags);
		player.setVelocity(Vec3d.ZERO);
		player.teleport((ServerWorld) player.getWorld(), destination.x, destination.y, destination.z, player.getYaw(), player.getPitch());
		rat.setAiDisabled(session.wasAiDisabled);
		if (session.cursed) rat.discard();
		if (kill) player.kill();
	}

	private static void sendCamera(ServerPlayerEntity player, int entityId) {
		if (!player.networkHandler.isConnectionOpen()) return;
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(entityId);
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
		player.setInvisible((flags & 1) != 0);
		player.setInvulnerable((flags & 2) != 0);
		player.setNoGravity((flags & 4) != 0);
		player.noClip = (flags & 8) != 0;
	}

	private static final class Session {
		final RatEntity rat;
		final Vec3d origin;
		final boolean cursed;
		final boolean wasAiDisabled;
		final int previousFlags;
		int attackCooldown;

		Session(RatEntity rat, Vec3d origin, boolean cursed, boolean wasAiDisabled, int previousFlags) {
			this.rat = rat;
			this.origin = origin;
			this.cursed = cursed;
			this.wasAiDisabled = wasAiDisabled;
			this.previousFlags = previousFlags;
		}
	}
}

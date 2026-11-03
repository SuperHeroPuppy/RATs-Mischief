package ladysnake.ratsmischief.common.command;

import com.mojang.brigadier.CommandDispatcher;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class RatCommand {
	private RatCommand() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("rat")
			.then(CommandManager.literal("pickup")
				.requires(source -> source.getEntity() instanceof ServerPlayerEntity)
				.executes(context -> pickup(context.getSource())))
			.then(CommandManager.literal("item")
				.requires(source -> source.getEntity() instanceof ServerPlayerEntity player && player.isCreative())
				.executes(context -> giveItem(context.getSource()))));
	}

	private static int pickup(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		RatEntity rat = findLookedAtRat(player);
		if (rat == null) {
			source.sendError(Text.literal("Look directly at a rat within five blocks."));
			return 0;
		}
		rat.turnRatIntoItemAndGive(player);
		source.sendFeedback(() -> Text.literal("Picked up rat."), false);
		return 1;
	}

	private static int giveItem(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		ItemStack stack = new ItemStack(ModItems.RAT);
		if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
		source.sendFeedback(() -> Text.literal("Gave rat item."), false);
		return 1;
	}

	private static RatEntity findLookedAtRat(ServerPlayerEntity player) {
		Vec3d start = player.getCameraPosVec(1.0f);
		Vec3d end = start.add(player.getRotationVec(1.0f).multiply(5.0));
		double nearest = start.squaredDistanceTo(player.raycast(5.0, 1.0f, false).getPos());
		RatEntity selected = null;
		Box search = player.getBoundingBox().stretch(end.subtract(start)).expand(1.0);
		for (RatEntity rat : player.getWorld().getEntitiesByClass(RatEntity.class, search, Entity::isAlive)) {
			var hit = rat.getBoundingBox().expand(0.2).raycast(start, end);
			if (hit.isEmpty()) continue;
			double distance = start.squaredDistanceTo(hit.get());
			if (distance < nearest) {
				nearest = distance;
				selected = rat;
			}
		}
		return selected;
	}
}

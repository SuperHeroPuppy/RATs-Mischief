package ladysnake.ratsmischief.common.item;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModEntities;
import ladysnake.ratsmischief.common.world.LoyaltyRatReturns;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraft.text.Style.EMPTY;

public class RatPouchItem extends Item {
	public static final String LOYALTY_POUCH_ID = "LoyaltyPouchId";
	public static final String LOYALTY_SLOT = "LoyaltySlot";
	private static final String LOYALTY_ROSTER = "LoyaltyRoster";
	private static final Predicate<RatEntity> CLOSEST_RAT_PREDICATE = (ratEntity) -> ratEntity.isTamed();
	private final int size;

	public RatPouchItem(Settings settings, int size) {
		super(settings);
		this.size = size;
	}

	public static boolean isBound(ItemStack stack) {
		if (!(stack.getItem() instanceof RatPouchItem)) return false;
		NbtCompound data = stack.getSubNbt(RatsMischief.MOD_ID);
		return data != null && data.containsUuid(LOYALTY_POUCH_ID)
			&& data.contains(LOYALTY_ROSTER, NbtElement.LIST_TYPE);
	}

	public static boolean isFullUnbound(ItemStack stack) {
		if (!(stack.getItem() instanceof RatPouchItem pouch) || isBound(stack)) return false;
		NbtCompound data = stack.getSubNbt(RatsMischief.MOD_ID);
		if (data == null) return false;
		NbtList rats = data.getList("rats", NbtElement.COMPOUND_TYPE);
		if (rats.size() != pouch.size) return false;
		for (NbtElement element : rats) {
			NbtCompound rat = (NbtCompound) element;
			if (!rat.getString("id").equals(RatsMischief.MOD_ID + ":rat")
				|| rat.containsUuid(LOYALTY_POUCH_ID)) return false;
		}
		return true;
	}

	public static ItemStack bindFullPouch(ItemStack input) {
		if (!isFullUnbound(input)) return ItemStack.EMPTY;
		ItemStack output = input.copy();
		output.setCount(1);
		NbtCompound data = output.getOrCreateSubNbt(RatsMischief.MOD_ID);
		NbtList rats = data.getList("rats", NbtElement.COMPOUND_TYPE);
		NbtList boundRats = new NbtList();
		NbtList roster = new NbtList();
		UUID pouchId = UUID.randomUUID();
		Set<UUID> usedIds = new HashSet<>();
		for (int slot = 0; slot < rats.size(); slot++) {
			NbtCompound rat = ((NbtCompound) rats.get(slot)).copy();
			UUID ratId = rat.containsUuid("UUID") ? rat.getUuid("UUID") : UUID.randomUUID();
			while (!usedIds.add(ratId)) ratId = UUID.randomUUID();
			rat.putUuid("UUID", ratId);
			rat.putUuid(LOYALTY_POUCH_ID, pouchId);
			rat.putInt(LOYALTY_SLOT, slot);
			boundRats.add(rat);
			NbtCompound member = new NbtCompound();
			member.putUuid("UUID", ratId);
			roster.add(member);
		}
		data.putUuid(LOYALTY_POUCH_ID, pouchId);
		data.put(LOYALTY_ROSTER, roster);
		data.put("rats", boundRats);
		data.putFloat("filled", 1.0f);
		return output;
	}

	public static boolean isBoundRat(NbtCompound rat) {
		return rat.containsUuid("UUID") && rat.containsUuid(LOYALTY_POUCH_ID)
			&& rat.contains(LOYALTY_SLOT, NbtElement.INT_TYPE) && rat.getInt(LOYALTY_SLOT) >= 0;
	}

	private static boolean acceptsRat(ItemStack pouch, NbtCompound rat) {
		if (!(pouch.getItem() instanceof RatPouchItem item)) return false;
		if (!isBound(pouch)) return !isBoundRat(rat);
		if (!isBoundRat(rat)) return false;
		NbtCompound data = pouch.getSubNbt(RatsMischief.MOD_ID);
		if (!data.getUuid(LOYALTY_POUCH_ID).equals(rat.getUuid(LOYALTY_POUCH_ID))) return false;
		int slot = rat.getInt(LOYALTY_SLOT);
		NbtList roster = data.getList(LOYALTY_ROSTER, NbtElement.COMPOUND_TYPE);
		return slot < item.size && slot < roster.size()
			&& ((NbtCompound) roster.get(slot)).containsUuid("UUID")
			&& ((NbtCompound) roster.get(slot)).getUuid("UUID").equals(rat.getUuid("UUID"));
	}

	private static boolean storeRat(ItemStack pouch, NbtCompound rat, boolean allowExisting) {
		if (!acceptsRat(pouch, rat)) return false;
		NbtCompound data = pouch.getOrCreateSubNbt(RatsMischief.MOD_ID);
		NbtList stored = data.getList("rats", NbtElement.COMPOUND_TYPE);
		if (isBound(pouch)) {
			for (NbtElement element : stored) {
				NbtCompound occupant = (NbtCompound) element;
				if (occupant.getInt(LOYALTY_SLOT) == rat.getInt(LOYALTY_SLOT)) {
					return allowExisting && occupant.containsUuid("UUID")
						&& occupant.getUuid("UUID").equals(rat.getUuid("UUID"));
				}
			}
		}
		if (stored.size() >= ((RatPouchItem) pouch.getItem()).size) return false;
		if (isBound(pouch)) {
			int insertAt = 0;
			while (insertAt < stored.size()
				&& ((NbtCompound) stored.get(insertAt)).getInt(LOYALTY_SLOT) < rat.getInt(LOYALTY_SLOT)) {
				insertAt++;
			}
			stored.add(insertAt, rat.copy());
		} else {
			stored.add(rat.copy());
		}
		data.put("rats", stored);
		data.putFloat("filled", 1.0f);
		return true;
	}

	public static boolean tryStoreReturnedRat(ItemStack pouch, NbtCompound rat) {
		return isBound(pouch) && storeRat(pouch, rat, true);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return isBound(stack) || super.hasGlint(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (world instanceof ServerWorld serverWorld && entity instanceof PlayerEntity player
			&& isBound(stack) && world.getTime() % 20 == 0
			&& LoyaltyRatReturns.get(serverWorld.getServer()).restoreTo(stack)) {
			player.getInventory().markDirty();
		}
	}

	public ItemStack createFilledCreativeStack() {
		ItemStack stack = new ItemStack(this);
		NbtList rats = new NbtList();
		for (int i = 0; i < this.size; i++) {
			NbtCompound rat = new NbtCompound();
			rat.putString("id", RatsMischief.MOD_ID + ":rat");
			rat.putString("RatType", RatEntity.Type.WILD.name());
			rat.putString("Color", "white");
			rat.putFloat("Health", 8.0f);
			rat.putInt("Age", 0);
			rats.add(rat);
		}
		NbtCompound data = stack.getOrCreateSubNbt(RatsMischief.MOD_ID);
		data.put("rats", rats);
		data.putFloat("filled", 1.0f);
		return stack;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (!world.isClient() && user.isSneaking()) {
			ItemStack stack = user.getStackInHand(hand);
			if (isBound(stack)) return useBoundPouch(world, user, stack);
			if (user.getStackInHand(hand).getOrCreateSubNbt(RatsMischief.MOD_ID).getFloat("filled") == 1f) {
				for (NbtElement ratTag : user.getStackInHand(hand).getOrCreateSubNbt(RatsMischief.MOD_ID).getList("rats", NbtType.COMPOUND)) {
					RatEntity rat = ModEntities.RAT.create(world);
					rat.readNbt((NbtCompound) ratTag);
					rat.updatePosition(user.getX(), user.getY(), user.getZ());
					rat.setPos(user.getX(), user.getY(), user.getZ());
					world.spawnEntity(rat);
				}

				user.getStackInHand(hand).getOrCreateSubNbt(RatsMischief.MOD_ID).put("rats", new NbtList());
				user.getStackInHand(hand).getOrCreateSubNbt(RatsMischief.MOD_ID).putFloat("filled", 0F);

				return TypedActionResult.success(user.getStackInHand(hand));
			} else {
				List<RatEntity> closestTamedRats = world.getEntitiesByClass(RatEntity.class, user.getBoundingBox().expand(16.0D), CLOSEST_RAT_PREDICATE);
				List<RatEntity> closestOwnedRats = closestTamedRats.stream().filter(ratEntity -> ratEntity.getOwnerUuid() != null && ratEntity.getOwnerUuid().equals(user.getUuid()) && !ratEntity.isLoyaltyBound()).collect(Collectors.toList());

				int gathered = 0;
				for (RatEntity rat : closestOwnedRats) {
					NbtCompound nbt = new NbtCompound();
					rat.saveSelfNbt(nbt);
					if (storeRat(stack, nbt, false)) {
						rat.playSpawnEffects();
						rat.remove(Entity.RemovalReason.DISCARDED);
						gathered++;
					}
				}
				return gathered > 0 ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
			}
		}

		return super.use(world, user, hand);
	}

	private TypedActionResult<ItemStack> useBoundPouch(World world, PlayerEntity user, ItemStack stack) {
		List<RatEntity> nearby = world.getEntitiesByClass(RatEntity.class, user.getBoundingBox().expand(16.0D),
			rat -> rat.isTamed() && rat.isLoyaltyBound() && rat.hasLoyaltyPouchId(stack));
		int gathered = 0;
		for (RatEntity rat : nearby) {
			NbtCompound tag = new NbtCompound();
				rat.saveSelfNbt(tag);
			if (storeRat(stack, tag, false)) {
				rat.playSpawnEffects();
				rat.remove(Entity.RemovalReason.DISCARDED);
				gathered++;
			}
		}
		if (gathered > 0) return TypedActionResult.success(stack);

		NbtCompound data = stack.getOrCreateSubNbt(RatsMischief.MOD_ID);
		NbtList stored = data.getList("rats", NbtElement.COMPOUND_TYPE);
		if (stored.isEmpty()) return TypedActionResult.fail(stack);
		NbtList remaining = new NbtList();
		int released = 0;
		for (NbtElement element : stored) {
			RatEntity rat = ModEntities.RAT.create(world);
			if (rat == null) {
				remaining.add(element.copy());
				continue;
			}
			rat.readNbt((NbtCompound) element);
			rat.setOwner(user);
			rat.updatePosition(user.getX(), user.getY(), user.getZ());
			if (world.spawnEntity(rat)) released++;
			else remaining.add(element.copy());
		}
		data.put("rats", remaining);
		data.putFloat("filled", remaining.isEmpty() ? 0.0f : 1.0f);
		return released > 0 ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		NbtList NbtList = user.getStackInHand(hand).getOrCreateSubNbt(RatsMischief.MOD_ID).getList("rats", NbtType.COMPOUND);

		if (NbtList.size() < this.size && entity instanceof RatEntity rat
			&& (isBound(stack) ? rat.hasLoyaltyPouchId(stack)
				: rat.getOwnerUuid() != null && rat.getOwnerUuid().equals(user.getUuid()) && !rat.isLoyaltyBound())) {
			NbtCompound NbtCompound = new NbtCompound();
			entity.saveSelfNbt(NbtCompound);
			if (!acceptsRat(stack, NbtCompound)) return ActionResult.FAIL;
			if (user.getWorld().isClient) return ActionResult.SUCCESS;
			if (!storeRat(stack, NbtCompound, false)) return ActionResult.FAIL;
			((RatEntity) entity).playSpawnEffects();
			entity.remove(Entity.RemovalReason.DISCARDED);

			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtList NbtList = stack.getOrCreateSubNbt(RatsMischief.MOD_ID).getList("rats", NbtType.COMPOUND);

		int capacity = isBound(stack)
			? stack.getSubNbt(RatsMischief.MOD_ID).getList(LOYALTY_ROSTER, NbtElement.COMPOUND_TYPE).size()
			: this.size;
		tooltip.add(Text.translatable("item.ratsmischief.rat_pouch.tooltip.capacity", NbtList.size(), capacity)
			.setStyle(EMPTY.withColor(Formatting.GRAY)));
		if (isBound(stack)) {
			tooltip.add(Text.translatable("item.ratsmischief.rat_pouch.tooltip.loyalty")
				.setStyle(EMPTY.withColor(Formatting.GOLD)));
		}

		for (NbtElement ratTag : NbtList) {
			MutableText ratType = Text.translatable("type.ratsmischief." + ((NbtCompound) ratTag).getString("RatType").toLowerCase());

			Style style = EMPTY.withColor(Formatting.DARK_GRAY);
			if (((NbtCompound) ratTag).getString("RatType").equals(RatEntity.Type.GOLD.name())) {
				style = EMPTY.withColor(Formatting.GOLD);
			}
			if (((NbtCompound) ratTag).contains("CustomName")) {
				Matcher matcher = Pattern.compile("\\{\"text\":\"(.+)\"\\}").matcher(((NbtCompound) ratTag).getString("CustomName"));
				if (matcher.find()) {
					String name = matcher.group(1);
					tooltip.add(Text.literal(name).append(" (").append(ratType).append(")").setStyle(style));
				}
			} else {
				tooltip.add(ratType.setStyle(style));
			}
		}

		super.appendTooltip(stack, world, tooltip, context);
	}
}

package ladysnake.ratsmischief.common.item;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.init.ModEntities;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.minecraft.text.Style.EMPTY;

public class RatPouchItem extends Item {
	private static final Predicate<RatEntity> CLOSEST_RAT_PREDICATE = rat -> rat.isTamed();
	private final int size;

	public RatPouchItem(Settings settings, int size) {
		super(settings);
		this.size = size;
	}
	public static boolean captureRat(ItemStack pouch, PlayerEntity player, RatEntity rat) {
		if (player.getWorld().isClient || rat.isRemoved() || !rat.isAlive()) return false;
		if (!rat.isOwner(player)) return false;
		NbtCompound tag = new NbtCompound();
		if (!rat.saveSelfNbt(tag)) return false;
		NbtCompound data = pouch.getOrCreateSubNbt(RatsMischief.MOD_ID);
		NbtList stored = data.getList("rats", NbtElement.COMPOUND_TYPE);
		if (stored.size() >= ((RatPouchItem) pouch.getItem()).size) return false;
		stored.add(tag);
		data.put("rats", stored);
		data.putFloat("filled", 1.0f);
		player.getInventory().markDirty();
		player.currentScreenHandler.sendContentUpdates();
		player.getItemCooldownManager().set(pouch.getItem(), 10);
		rat.playSpawnEffects();
		rat.remove(Entity.RemovalReason.DISCARDED);
		return true;
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
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			return TypedActionResult.fail(user.getStackInHand(hand));
		}
		if (!world.isClient() && user.isSneaking()) {
			ItemStack stack = user.getStackInHand(hand);
			NbtCompound data = stack.getOrCreateSubNbt(RatsMischief.MOD_ID);
			if (data.getFloat("filled") == 1f) {
				NbtList remaining = new NbtList();
				int released = 0;
				for (NbtElement ratTag : data.getList("rats", NbtType.COMPOUND)) {
					RatEntity rat = ModEntities.RAT.create(world);
					if (rat == null) {
						remaining.add(ratTag.copy());
						continue;
					}
					rat.readNbt((NbtCompound) ratTag);
					if (!rat.isTamed() || rat.getOwnerUuid() == null) rat.setOwner(user);
					rat.updatePosition(user.getX(), user.getY(), user.getZ());
					rat.setPos(user.getX(), user.getY(), user.getZ());
					if (world.spawnEntity(rat)) released++;
					else remaining.add(ratTag.copy());
				}

				data.put("rats", remaining);
				data.putFloat("filled", remaining.isEmpty() ? 0F : 1F);

				return released > 0 ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
			} else {
				List<RatEntity> closestTamedRats = world.getEntitiesByClass(RatEntity.class, user.getBoundingBox().expand(16.0D), CLOSEST_RAT_PREDICATE);
				List<RatEntity> closestOwnedRats = closestTamedRats.stream().filter(ratEntity -> ratEntity.getOwnerUuid() != null && ratEntity.getOwnerUuid().equals(user.getUuid())).collect(Collectors.toList());

				int gathered = 0;
				for (RatEntity rat : closestOwnedRats) {
					if (captureRat(stack, user, rat)) {
						gathered++;
					}
				}
				return gathered > 0 ? TypedActionResult.success(stack) : TypedActionResult.fail(stack);
			}
		}

		return super.use(world, user, hand);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (!(entity instanceof RatEntity rat)) return ActionResult.PASS;
		if (user.getWorld().isClient) return ActionResult.SUCCESS;
		return captureRat(stack, user, rat) ? ActionResult.SUCCESS : ActionResult.FAIL;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtList NbtList = stack.getOrCreateSubNbt(RatsMischief.MOD_ID).getList("rats", NbtType.COMPOUND);

		tooltip.add(Text.translatable("item.ratsmischief.rat_pouch.tooltip.capacity", NbtList.size(), this.size)
			.setStyle(EMPTY.withColor(Formatting.GRAY)));

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

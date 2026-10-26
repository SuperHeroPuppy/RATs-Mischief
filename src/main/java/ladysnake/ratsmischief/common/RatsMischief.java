package ladysnake.ratsmischief.common;

import ladysnake.ratsmischief.client.render.item.recipe.SpyRatCraftingRecipe;
import ladysnake.ratsmischief.common.recipe.LoyaltyPouchCraftingRecipe;
import ladysnake.ratsmischief.common.init.*;
import ladysnake.ratsmischief.common.world.RatSpawner;
import ladysnake.ratsmischief.common.possession.RatPossession;
import ladysnake.ratsmischief.common.item.RatMasterOcarinaItem;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Difficulty;
import software.bernie.geckolib.GeckoLib;

public class RatsMischief implements ModInitializer {
	public static final String MOD_ID = "ratsmischief";
	public static final SpecialRecipeSerializer<SpyRatCraftingRecipe> SPY_RAT_RECIPE = RecipeSerializer.register(
		"ratsmischief:crafting_special_spy_rat", new SpecialRecipeSerializer<>(SpyRatCraftingRecipe::new)
	);
	public static final SpecialRecipeSerializer<LoyaltyPouchCraftingRecipe> LOYALTY_POUCH_RECIPE = RecipeSerializer.register(
		"ratsmischief:crafting_special_loyalty_pouch", new SpecialRecipeSerializer<>(LoyaltyPouchCraftingRecipe::new)
	);
	public static final Identifier ANCIENT_CITY_CHESTS = new Identifier("minecraft", "chests/ancient_city");
	public static final Identifier BASTION_TREASURE_CHESTS = new Identifier("minecraft", "chests/bastion_treasure");
	public static final Identifier BASTION_OTHER_CHESTS = new Identifier("minecraft", "chests/bastion_other");
	public static final Identifier BASTION_BRIDGE_CHESTS = new Identifier("minecraft", "chests/bastion_bridge");
	public static final Identifier BASTION_HOGLIN_STABLE_CHESTS = new Identifier("minecraft", "chests/bastion_hoglin_stable");
	public static final Identifier END_CITY_TREASURE_CHESTS = new Identifier("minecraft", "chests/end_city_treasure");

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		GeckoLib.initialize();
		RatPossession.initialize();
		RatMasterOcarinaItem.initializeNetworking();

		// initializing stuff
		ModEntities.initialize();
		ModBlocks.initialize();
//		ModItemGroup.initialize();
		ModItems.initialize();
		ModSoundEvents.initialize();
		ModEnchantments.initialize();
		ModLootTables.initialize();
		ModStatusEffects.initialize();

		// custom rat spawner in abandoned villages
		RatSpawner ratSpawner = new RatSpawner();
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			// spawn rats
			ratSpawner.spawn(world, world.getDifficulty() != Difficulty.PEACEFUL, world.getServer().shouldSpawnAnimals());
		});

		// rat kid painting
		Registry.register(Registries.PAINTING_VARIANT, new Identifier(MOD_ID, "a_rat_in_time"), new PaintingVariant(64, 48));

		// Rat Master components in Ancient Cities; Bellicist components in Bastions.
		UniformLootNumberProvider lootTableRange = UniformLootNumberProvider.create(1, 1);
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, supplier, setter) -> {
			if (ANCIENT_CITY_CHESTS.equals(id)) {
				addChestLoot(supplier, ModItems.UNCLOTHED_INGOT, 0.3f);
				addChestLoot(supplier, ModItems.RAT_MASTER_CLOTH, 0.3f);
				{ // rat curse book
					ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
					EnchantedBookItem.addEnchantment(book, new EnchantmentLevelEntry(ModEnchantments.RAT_CURSE, 1));
					LootPool lootPool = LootPool.builder()
						.rolls(lootTableRange)
						.conditionally(RandomChanceLootCondition.builder(0.3f).build())
						.with(ItemEntry.builder(Items.ENCHANTED_BOOK).apply(SetNbtLootFunction.builder(book.getNbt())).build()).build();
					supplier.pool(lootPool);
				}
			} else if (BASTION_TREASURE_CHESTS.equals(id) || BASTION_OTHER_CHESTS.equals(id)
				|| BASTION_BRIDGE_CHESTS.equals(id) || BASTION_HOGLIN_STABLE_CHESTS.equals(id)) {
				addChestLoot(supplier, ModItems.UNCLOTHED_CONTRACTED_INGOT, 0.25f);
				addChestLoot(supplier, ModItems.RAT_BELLICIST_CLOTH, 0.25f);
				if (BASTION_TREASURE_CHESTS.equals(id)) {
					addChestLoot(supplier, ModItems.RAT_BELLICIST_SMITHING_TEMPLATE, 0.10f);
					addChestLoot(supplier, ModItems.LOYALTY_OF_THE_MISCHIEF, 0.03f);
				}
			} else if (END_CITY_TREASURE_CHESTS.equals(id)) {
				addChestLoot(supplier, ModItems.LOYALTY_OF_THE_MISCHIEF, 0.05f);
			}
		});
	}

	private static void addChestLoot(LootTable.Builder table, Item item, float chance) {
		table.pool(LootPool.builder()
			.rolls(UniformLootNumberProvider.create(1, 1))
			.conditionally(RandomChanceLootCondition.builder(chance).build())
			.with(ItemEntry.builder(item).build())
			.build());
	}
}

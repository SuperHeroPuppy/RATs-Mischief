package ladysnake.ratsmischief.common.init;

import ladysnake.ratsmischief.common.RatsMischief;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ModBlocks {
	Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();

//	Block MOD_BLOCK = createBlock("mod_block", new ModBlock(FabricBlockSettings.of(Material.METAL, MapColor.DEEPSLATE_GRAY).strength(-1.0F, 3600000.0F).sounds(BlockSoundGroup.COPPER)), true);
	Block RAT_MASTER_BLOCK = createBlock("rat_master_block", new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_BRICKS).requiresTool()), true);
	Block RAT_MASTER_BRICKS = createBlock("rat_master_bricks", new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_BRICKS).requiresTool()), true);
	Block RAT_MASTER_TILES = createBlock("rat_master_tiles", new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_BRICKS).requiresTool()), true);
	Block RAT_MASTER_PILLAR = createBlock("rat_master_pillar", new PillarBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_BRICKS).requiresTool()), true);
	Block CONTRACTED_BLOCK = createBlock("contracted_block", new Block(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool()), true);
	Block CONTRACTED_BRICKS = createBlock("contracted_bricks", new Block(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool()), true);
	Block CONTRACTED_TILES = createBlock("contracted_tiles", new Block(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool()), true);
	Block CONTRACTED_PILLAR = createBlock("contracted_pillar", new PillarBlock(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).requiresTool()), true);
	Block ENDER_PEARL_BLOCK = createBlock("ender_pearl_block", new Block(FabricBlockSettings.copyOf(Blocks.END_STONE_BRICKS).requiresTool()), true);
	Block ENDER_EYE_BLOCK = createBlock("ender_eye_block", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block BLINDED_ENDER_EYE_BLOCK = createBlock("blinded_ender_eye_block", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block CONTRACTED_ENDER_PEARL = createBlock("contracted_ender_pearl", new Block(FabricBlockSettings.copyOf(Blocks.END_STONE_BRICKS).requiresTool()), true);
	Block CONTRACTED_CONSTRICTED_ENDER_PEARL = createBlock("contracted_constricted_ender_pearl", new Block(FabricBlockSettings.copyOf(Blocks.END_STONE_BRICKS).requiresTool()), true);
	Block CONTRACTED_ENDER_EYE = createBlock("contracted_ender_eye", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block CONTRACTED_CONSTRICTED_ENDER_EYE = createBlock("contracted_constricted_ender_eye", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block CONTRACTED_BLINDED_ENDER_EYE = createBlock("contracted_blinded_ender_eye", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block CONTRACTED_CONSTRICTED_BLINDED_ENDER_EYE = createBlock("contracted_constricted_blinded_ender_eye", new Block(FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK).requiresTool().luminance(state -> 4)), true);
	Block HEART_OF_THE_SEA_BLOCK = createBlock("heart_of_the_sea_block", new Block(FabricBlockSettings.copyOf(Blocks.PRISMARINE).requiresTool()), true);
	Block CONTRACTED_HEART_OF_THE_SEA = createBlock("contracted_heart_of_the_sea", new Block(FabricBlockSettings.copyOf(Blocks.PRISMARINE).requiresTool()), true);
	Block CONTRACTED_CONSTRICTED_HEART_OF_THE_SEA = createBlock("contracted_constricted_heart_of_the_sea", new Block(FabricBlockSettings.copyOf(Blocks.PRISMARINE).requiresTool()), true);

	static void initialize() {
		BLOCKS.keySet().forEach(block -> Registry.register(Registries.BLOCK, BLOCKS.get(block), block));
	}

	private static <T extends Block> T createBlock(String name, T block, boolean createItem) {
		BLOCKS.put(block, new Identifier(RatsMischief.MOD_ID, name));
		if (createItem) {
			ModItems.ITEMS.put(new BlockItem(block, new FabricItemSettings()), BLOCKS.get(block));
		}
		return block;
	}
}

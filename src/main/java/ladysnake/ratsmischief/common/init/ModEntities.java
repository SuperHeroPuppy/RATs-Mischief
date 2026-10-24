package ladysnake.ratsmischief.common.init;

import ladysnake.ratsmischief.common.RatsMischief;
import ladysnake.ratsmischief.common.entity.RatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ModEntities {
	Map<EntityType<? extends Entity>, Identifier> ENTITIES = new LinkedHashMap<>();

	//	public static EntityType<ModEntity> MOD_ENTITY = createEntity("mod_entity", FabricEntityTypeBuilder.<ModEntity>create(SpawnGroup.MISC, ModEntity::new).dimensions(EntityDimensions.fixed(0f, 0f)).trackRangeBlocks(128).build());
	EntityType<RatEntity> RAT = createEntity("rat", FabricEntityTypeBuilder.<RatEntity>createMob().entityFactory(RatEntity::new).defaultAttributes(RatEntity::createRatAttributes).dimensions(EntityDimensions.changing(0.6F, 0.4F)).trackRangeBlocks(128).build());

	private static <T extends EntityType<? extends Entity>> T createEntity(String name, T entity) {
		ENTITIES.put(entity, new Identifier(RatsMischief.MOD_ID, name));
		return entity;
	}

	static void initialize() {
		ENTITIES.keySet().forEach(entityType -> Registry.register(Registry.ENTITY_TYPE, ENTITIES.get(entityType), entityType));
	}
}

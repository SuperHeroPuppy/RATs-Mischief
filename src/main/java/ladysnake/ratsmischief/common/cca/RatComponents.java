package ladysnake.ratsmischief.common.cca;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;

public final class RatComponents implements EntityComponentInitializer {
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.beginRegistration(PlayerEntity.class, RatHornsComponent.KEY)
			.respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
			.end(RatHornsComponent::new);
	}
}

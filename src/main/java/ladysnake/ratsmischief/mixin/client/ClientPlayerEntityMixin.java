package ladysnake.ratsmischief.mixin.client;

import ladysnake.ratsmischief.client.RatPossessionClient;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossessionView;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements RatPossessionView {
	@Override
	public RatEntity ratsmischief$getControlledRat() {
		return RatPossessionClient.getControlledRat();
	}
}

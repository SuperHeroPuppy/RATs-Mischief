package ladysnake.ratsmischief.common.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.ratsmischief.common.RatsMischief;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public final class RatHornsComponent implements AutoSyncedComponent {
	public static final ComponentKey<RatHornsComponent> KEY = ComponentRegistry.getOrCreate(RatsMischief.id("rathorns"), RatHornsComponent.class);
	private final PlayerEntity player;
	private boolean horns;

	public RatHornsComponent(PlayerEntity player) {
		this.player = player;
	}

	public boolean hasHorns() {
		return this.horns;
	}

	public void setHorns(boolean horns) {
		this.horns = horns;
		KEY.sync(this.player);
	}

	@Override
	public void readFromNbt(NbtCompound nbt) {
		this.horns = nbt.getBoolean("horns");
	}

	@Override
	public void writeToNbt(NbtCompound nbt) {
		nbt.putBoolean("horns", this.horns);
	}
}

package ladysnake.ratsmischief.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ladysnake.ratsmischief.common.entity.RatEntity;
import ladysnake.ratsmischief.common.possession.RatPossession;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "saveNbt", at = @At("HEAD"), cancellable = true)
	private void ratsmischief$doNotSaveTemporaryBody(NbtCompound nbt, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ArmorStandEntity stand && RatPossession.isPossessionBody(stand)) {
			cir.setReturnValue(false);
		}
	}
	@WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V"))
	private void mischief$notSteppedOn(Block block, World world, BlockPos pos, BlockState state, Entity entity, Operation<Void> operation) {
		if (entity instanceof RatEntity) {
			return;
		}
		operation.call(block, world, pos, state, entity);
	}
}

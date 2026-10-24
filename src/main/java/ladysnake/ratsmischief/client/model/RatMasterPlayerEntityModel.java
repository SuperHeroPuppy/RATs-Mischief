package ladysnake.ratsmischief.client.model;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

/** Geometry and UV layout used by the later Rat Master and Bellicist armor textures. */
public final class RatMasterPlayerEntityModel<T extends LivingEntity> extends PlayerEntityModel<T> {
	public RatMasterPlayerEntityModel(ModelPart root, boolean slim) {
		super(root, slim);
	}

	public static ModelData getTexturedModelData(Dilation dilation, boolean slim) {
		ModelData data = BipedEntityModel.getModelData(dilation, 0.0F);
		ModelPartData root = data.getRoot();
		float overlay = 0.25F;
		if (slim) {
			root.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 3, 12, 4, dilation), ModelTransform.pivot(5, 2.5F, 0));
			root.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-2, -2, -2, 3, 12, 4, dilation), ModelTransform.pivot(-5, 2.5F, 0));
			root.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1, -2, -2, 3, 12, 4, dilation.add(overlay)), ModelTransform.pivot(5, 2.5F, 0));
			root.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-2, -2, -2, 3, 12, 4, dilation.add(overlay)), ModelTransform.pivot(-5, 2.5F, 0));
		} else {
			root.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation), ModelTransform.pivot(5, 2, 0));
			root.addChild("left_sleeve", ModelPartBuilder.create().uv(48, 48).cuboid(-1, -2, -2, 4, 12, 4, dilation.add(overlay)), ModelTransform.pivot(5, 2, 0));
			root.addChild("right_sleeve", ModelPartBuilder.create().uv(40, 32).cuboid(-3, -2, -2, 4, 12, 4, dilation.add(overlay)), ModelTransform.pivot(-5, 2, 0));
		}
		root.addChild("left_leg", ModelPartBuilder.create().uv(16, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation), ModelTransform.pivot(1.9F, 12, 0));
		root.addChild("left_pants", ModelPartBuilder.create().uv(0, 48).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(overlay)), ModelTransform.pivot(1.9F, 12, 0));
		root.addChild("right_pants", ModelPartBuilder.create().uv(0, 32).cuboid(-2, 0, -2, 4, 12, 4, dilation.add(overlay)), ModelTransform.pivot(-1.9F, 12, 0));
		root.addChild("jacket", ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild("ear", ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild("cloak", ModelPartBuilder.create(), ModelTransform.NONE);
		return data;
	}
}

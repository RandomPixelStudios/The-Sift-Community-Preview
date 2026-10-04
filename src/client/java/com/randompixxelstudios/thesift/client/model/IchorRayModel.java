package com.randompixxelstudios.thesift.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** A flat ray with broad wings that ripple as it glides, and a long whip tail. */
public class IchorRayModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart tail;

	public IchorRayModel(final ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.leftWing = body.getChild("left_wing");
		this.rightWing = body.getChild("right_wing");
		this.tail = body.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 2.0F, 10.0F), PartPose.offset(0.0F, 22.0F, 0.0F)
		);
		body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 12).addBox(-8.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F), PartPose.offset(-5.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(32, 12).addBox(0.0F, -0.5F, -4.0F, 8.0F, 1.0F, 8.0F), PartPose.offset(5.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 10.0F), PartPose.offset(0.0F, 0.0F, 5.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		float flap = Mth.sin(t * 0.25F) * 0.45F;
		this.leftWing.zRot = flap;
		this.rightWing.zRot = -flap;
		this.tail.yRot = Mth.sin(t * 0.2F) * 0.3F;
	}
}

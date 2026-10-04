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

/** A small moth: a fuzzy body between two wide, softly glowing wings that flutter quickly. */
public class LumothModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart body;
	private final ModelPart leftWing;
	private final ModelPart rightWing;

	public LumothModel(final ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leftWing = this.body.getChild("left_wing");
		this.rightWing = this.body.getChild("right_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 21.0F, 0.0F)
		);
		body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 8).addBox(-7.0F, 0.0F, -3.0F, 7.0F, 0.0F, 6.0F), PartPose.offset(-1.0F, -1.0F, 0.0F));
		body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, -3.0F, 7.0F, 0.0F, 6.0F), PartPose.offset(1.0F, -1.0F, 0.0F));
		body.addOrReplaceChild(
			"antennae", CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -2.0F, 0.0F, 3.0F, 2.0F, 0.0F), PartPose.offsetAndRotation(0.0F, -1.0F, -2.5F, -0.5F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.body.y += Mth.sin(t * 0.3F) * 0.8F;
		float flap = Mth.sin(t * 1.6F) * 0.9F;
		this.leftWing.zRot = flap;
		this.rightWing.zRot = -flap;
	}
}

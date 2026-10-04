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

/** A tall, lanky creature: long dangling arms, thin legs and a sieve-faced head crowned with glowing tendrils. */
public class SifterModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart[] tendrils = new ModelPart[3];

	public SifterModel(final ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
		for (int i = 0; i < 3; i++) {
			this.tendrils[i] = this.head.getChild("tendril" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// legs 14 px, torso 12 px, head 10 px -> about 2.25 blocks tall
		PartDefinition head = root.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.offset(0.0F, -2.0F, 0.0F)
		);
		for (int i = 0; i < 3; i++) {
			head.addOrReplaceChild(
				"tendril" + i,
				CubeListBuilder.create().texOffs(32 + i * 8, 20).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
				PartPose.offset(-2.5F + i * 2.5F, -10.0F, -1.0F + i % 2 * 2.0F)
			);
		}

		root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, -2.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(0, 20).addBox(-2.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F), PartPose.offset(-5.5F, -1.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(16, 20).addBox(-1.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F), PartPose.offset(5.5F, -1.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(0, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F), PartPose.offset(-2.0F, 10.0F, 0.0F)
		);
		root.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(16, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F), PartPose.offset(2.0F, 10.0F, 0.0F)
		);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float walk = state.walkAnimationPos;
		float speed = state.walkAnimationSpeed;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

		this.rightLeg.xRot = Mth.cos(walk * 0.5F) * 1.2F * speed;
		this.leftLeg.xRot = Mth.cos(walk * 0.5F + Mth.PI) * 1.2F * speed;
		// arms dangle and sway loosely, swinging opposite to the legs
		float sway = Mth.sin(state.ageInTicks * 0.07F) * 0.08F;
		this.rightArm.xRot = Mth.cos(walk * 0.5F + Mth.PI) * 0.9F * speed - 0.15F;
		this.leftArm.xRot = Mth.cos(walk * 0.5F) * 0.9F * speed - 0.15F;
		this.rightArm.zRot = 0.1F + sway;
		this.leftArm.zRot = -0.1F - sway;
		this.body.xRot = 0.12F;

		for (int i = 0; i < 3; i++) {
			this.tendrils[i].xRot = Mth.sin(state.ageInTicks * 0.1F + i) * 0.25F;
			this.tendrils[i].zRot = Mth.cos(state.ageInTicks * 0.08F + i * 2.0F) * 0.2F;
		}
	}
}

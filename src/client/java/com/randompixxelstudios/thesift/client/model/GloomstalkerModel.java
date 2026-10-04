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

/**
 * The Gloomstalker: a large cave spider with a bulbous abdomen crowned by glowing crystal spines, six eyes,
 * curved mandibles and eight long, jointed legs.
 */
public class GloomstalkerModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart head;
	private final ModelPart abdomen;
	private final ModelPart leftMandible;
	private final ModelPart rightMandible;
	private final ModelPart[] legs = new ModelPart[8];

	public GloomstalkerModel(final ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.abdomen = root.getChild("abdomen");
		this.leftMandible = this.head.getChild("left_mandible");
		this.rightMandible = this.head.getChild("right_mandible");
		for (int i = 0; i < 8; i++) {
			this.legs[i] = root.getChild("leg" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 6.0F, 8.0F), PartPose.offset(0.0F, 16.0F, -3.0F)
		);
		head.addOrReplaceChild("left_mandible", CubeListBuilder.create().texOffs(90, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(2.0F, 1.0F, -8.0F));
		head.addOrReplaceChild("right_mandible", CubeListBuilder.create().texOffs(90, 6).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(-2.0F, 1.0F, -8.0F));
		root.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 5.0F, 6.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
		PartDefinition abdomen = root.addOrReplaceChild(
			"abdomen", CubeListBuilder.create().texOffs(0, 14).addBox(-5.0F, -5.0F, 0.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 3.0F)
		);
		for (int i = 0; i < 3; i++) {
			abdomen.addOrReplaceChild(
				"spike" + i,
				CubeListBuilder.create().texOffs(100 + i * 9, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
				PartPose.offsetAndRotation(i == 1 ? 0.0F : (i == 0 ? -2.5F : 2.5F), -5.0F, 3.0F + i * 3.0F, -0.35F, 0.0F, i == 0 ? -0.3F : (i == 2 ? 0.3F : 0.0F))
			);
		}

		for (int i = 0; i < 8; i++) {
			boolean right = i % 2 == 0;
			int pair = i / 2;
			CubeListBuilder leg = CubeListBuilder.create()
				.texOffs(right ? 0 : 64, 36 + pair * 4)
				.addBox(right ? -16.0F : 0.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
			root.addOrReplaceChild("leg" + i, leg, PartPose.offset(right ? -3.0F : 3.0F, 16.0F, -1.5F + pair * 1.2F));
		}

		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		float t = state.ageInTicks;
		this.abdomen.xRot = Mth.sin(t * 0.07F) * 0.05F;
		float bite = Mth.sin(t * 0.25F) * 0.15F;
		this.leftMandible.yRot = -0.2F - bite;
		this.rightMandible.yRot = 0.2F + bite;
		float pos = state.walkAnimationPos;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		float[] spread = {0.75F, 0.3F, -0.3F, -0.75F};
		for (int i = 0; i < 8; i++) {
			boolean right = i % 2 == 0;
			int pair = i / 2;
			float side = right ? 1.0F : -1.0F;
			float phase = pair % 2 == 0 ? 0.0F : Mth.PI;
			float swing = -(Mth.cos(pos * 1.3324F + phase + (right ? 0.0F : Mth.PI)) * 0.4F) * speed;
			float lift = Math.abs(Mth.sin(pos * 0.6662F + phase + (right ? 0.0F : Mth.PI)) * 0.4F) * speed;
			this.legs[i].yRot = side * spread[pair] + swing;
			this.legs[i].zRot = side * (0.6F - lift);
		}
	}
}

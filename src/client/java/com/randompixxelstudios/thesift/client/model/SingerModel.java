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

/** A soft floating creature: round body, two wing-like fins, a tail fin and trailing tendrils. */
public class SingerModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart body;
	private final ModelPart leftFin;
	private final ModelPart rightFin;
	private final ModelPart tail;
	private final ModelPart[] tendrils = new ModelPart[4];

	public SingerModel(final ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leftFin = this.body.getChild("left_fin");
		this.rightFin = this.body.getChild("right_fin");
		this.tail = this.body.getChild("tail");
		for (int i = 0; i < 4; i++) {
			this.tendrils[i] = this.body.getChild("tendril" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 10.0F, 12.0F), PartPose.offset(0.0F, 14.0F, 0.0F)
		);
		body.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(0, 24).addBox(-8.0F, 0.0F, -3.0F, 8.0F, 1.0F, 6.0F), PartPose.offset(-6.0F, -5.0F, 0.0F));
		body.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(30, 24).addBox(0.0F, 0.0F, -3.0F, 8.0F, 1.0F, 6.0F), PartPose.offset(6.0F, -5.0F, 0.0F));
		body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 1.0F, 8.0F), PartPose.offset(0.0F, -4.0F, 6.0F));
		for (int i = 0; i < 4; i++) {
			body.addOrReplaceChild(
				"tendril" + i,
				CubeListBuilder.create().texOffs(i * 12, 46).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F),
				PartPose.offset(i % 2 == 0 ? -3.0F : 3.0F, 0.0F, i < 2 ? -3.0F : 3.0F)
			);
		}

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.body.y += Mth.sin(t * 0.08F) * 1.5F;
		this.body.yRot = state.yRot * Mth.DEG_TO_RAD * 0.5F;
		float flap = Mth.sin(t * 0.15F) * 0.45F;
		this.leftFin.zRot = flap;
		this.rightFin.zRot = -flap;
		this.tail.xRot = Mth.sin(t * 0.1F) * 0.25F;
		for (int i = 0; i < 4; i++) {
			this.tendrils[i].xRot = Mth.sin(t * 0.09F + i) * 0.3F;
			this.tendrils[i].zRot = Mth.cos(t * 0.07F + i * 1.7F) * 0.2F;
		}
	}
}

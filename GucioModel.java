package pl.ziomsigiemka.krolik.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import pl.ziomsigiemka.krolik.KrolikMod;
import pl.ziomsigiemka.krolik.entity.PetRabbit;

/** Model Gucia: pulchny królik z czupryną. */
public class GucioModel extends EntityModel<PetRabbit> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(KrolikMod.MODID, "gucio"), "main");

    private final ModelPart root;
    private final ModelPart body, head, leftEar, rightEar, tuft, frontLeft, frontRight, hindLeft, hindRight, tail;
    private float jump;

    public GucioModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.tuft = head.getChild("tuft");
        this.frontLeft = root.getChild("front_left");
        this.frontRight = root.getChild("front_right");
        this.hindLeft = root.getChild("hind_left");
        this.hindRight = root.getChild("hind_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -5.0F, 6, 6, 10),
                PartPose.offset(0.0F, 18.0F, 1.0F));

        PartDefinition head = r.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-2.5F, -4.0F, -5.0F, 5, 5, 5)      // głowa
                        .texOffs(52, 0).addBox(-0.5F, -2.0F, -5.5F, 1, 1, 1)      // nosek
                        .texOffs(24, 16).addBox(-2.0F, -4.5F, -5.5F, 4, 2, 1),    // grzywka
                PartPose.offset(0.0F, 17.0F, -3.0F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -6.0F, -0.5F, 2, 6, 1),
                PartPose.offsetAndRotation(1.6F, -3.5F, -0.8F, -0.15F, 0.0F, 0.2F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(6, 16).addBox(-1.0F, -6.0F, -0.5F, 2, 6, 1),
                PartPose.offsetAndRotation(-1.6F, -3.5F, -0.8F, -0.15F, 0.0F, -0.2F));
        head.addOrReplaceChild("tuft", CubeListBuilder.create().texOffs(12, 16).addBox(-1.5F, -3.0F, -1.5F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, -3.6F, -3.2F, -0.35F, 0.0F, 0.0F));

        r.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(34, 16).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2),
                PartPose.offset(1.8F, 19.0F, -3.0F));
        r.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(42, 16).addBox(-1.0F, 0.0F, -1.0F, 2, 5, 2),
                PartPose.offset(-1.8F, 19.0F, -3.0F));
        r.addOrReplaceChild("hind_left", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, 0.0F, -3.0F, 3, 5, 5),
                PartPose.offset(2.4F, 19.0F, 4.0F));
        r.addOrReplaceChild("hind_right", CubeListBuilder.create().texOffs(16, 24).addBox(-1.5F, 0.0F, -3.0F, 3, 5, 5),
                PartPose.offset(-2.4F, 19.0F, 4.0F));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(32, 24).addBox(-1.5F, -1.5F, 0.0F, 3, 3, 2),
                PartPose.offset(0.0F, 16.5F, 6.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void prepareMobModel(PetRabbit entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.jump = entity.getJumpCompletion(partialTick);
    }

    @Override
    public void setupAnim(PetRabbit entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float hop = Mth.sin(this.jump * (float) Math.PI);
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        body.xRot = -hop * 0.15F;
        frontLeft.xRot = -swing * 0.8F - hop * 0.6F;
        frontRight.xRot = frontLeft.xRot;
        hindLeft.xRot = swing + hop * 0.9F;
        hindRight.xRot = hindLeft.xRot;

        float wiggle = Mth.sin(ageInTicks * 0.1F) * 0.06F;
        leftEar.zRot = 0.2F + wiggle;
        rightEar.zRot = -0.2F - wiggle;
        leftEar.xRot = -0.15F - hop * 0.5F;
        rightEar.xRot = leftEar.xRot;

        tuft.xRot = -0.35F + Mth.sin(ageInTicks * 0.15F) * 0.08F + hop * 0.3F;
        tail.yRot = Mth.sin(ageInTicks * 0.3F) * 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}

/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.markusbordihn.easymodelentities.api.client.EasyModelEntitiesClientApi;
import de.markusbordihn.easymodelentities.api.client.EasyModelShoulderModels;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelShoulderModel;
import de.markusbordihn.easymodelentities.client.network.EasyModelShoulderPacketHandler;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelShoulderSettings;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.Optional;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public class EasyModelShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

  private static final float SHOULDER_SIDE_OFFSET = 0.4f;
  private static final float CROUCHING_SHOULDER_DROP = 0.2f;
  private static final float FORWARD_FACING_YAW = 180.0f;
  private static final float UPRIGHT_ROLL_DEGREES = 180.0f;

  public EasyModelShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
    super(renderer);
  }

  private static ModelShoulderSettings shoulderSettings(Identifier profileId) {
    return EasyModelEntityRenderBackend.resolveContract(profileId, EasyModelAnimationSetting.AUTO)
        .flatMap(
            contract ->
                EasyModelServices.renderProfileService()
                    .getRenderProfile(contract.renderProfileId()))
        .map(EasyModelRenderProfile::shoulder)
        .orElse(ModelShoulderSettings.DEFAULT);
  }

  private static EasyModelEntityRenderOptions shoulderOptions(
      EasyModelShoulderModel shoulderModel, ModelShoulderSettings settings, float ageInTicks) {
    EasyModelEntityRenderOptions options = shoulderModel.options();
    float modelScale = options.scale() == null ? 1.0f : options.scale();
    options = options.withScale(modelScale * settings.scale());
    if (options.animationTicks() == null) {
      options = options.withAnimationTicks(Math.max(0.0f, ageInTicks));
    }
    if (options.animation() == null) {
      options =
          options.withAnimation(
              EasyModelEntitiesClientApi.resolveShoulderAnimation(shoulderModel.profileId()));
    }
    return options;
  }

  @Override
  public void submit(
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      AvatarRenderState renderState,
      float yRot,
      float xRot) {
    this.submitShoulder(poseStack, submitNodeCollector, packedLight, renderState, true);
    this.submitShoulder(poseStack, submitNodeCollector, packedLight, renderState, false);
  }

  private void submitShoulder(
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int packedLight,
      AvatarRenderState renderState,
      boolean leftShoulder) {
    Optional<EasyModelShoulderModel> shoulderModel =
        EasyModelShoulderModels.resolve(
            EasyModelShoulderPacketHandler.shoulderEntity(renderState.id, leftShoulder));
    if (shoulderModel.isEmpty()) {
      return;
    }

    Identifier profileId = shoulderModel.get().profileId();
    ModelShoulderSettings settings = shoulderSettings(profileId);
    Vec3f offset = settings.offset();
    poseStack.pushPose();
    this.getParentModel().body.translateAndRotate(poseStack);
    poseStack.translate(
        leftShoulder ? SHOULDER_SIDE_OFFSET : -SHOULDER_SIDE_OFFSET,
        renderState.isCrouching ? CROUCHING_SHOULDER_DROP : 0.0f,
        0.0f);
    poseStack.mulPose(Axis.ZP.rotationDegrees(UPRIGHT_ROLL_DEGREES));
    poseStack.translate(leftShoulder ? -offset.x() : offset.x(), offset.y(), -offset.z());
    EasyModelEntitiesClientApi.submit(
        profileId,
        poseStack,
        submitNodeCollector,
        packedLight,
        FORWARD_FACING_YAW,
        shoulderOptions(shoulderModel.get(), settings, renderState.ageInTicks));
    poseStack.popPose();
  }
}

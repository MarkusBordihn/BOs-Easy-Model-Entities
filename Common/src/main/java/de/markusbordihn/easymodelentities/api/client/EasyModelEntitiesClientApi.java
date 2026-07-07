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

package de.markusbordihn.easymodelentities.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EasyModelEntitiesClientApi {

  private EasyModelEntitiesClientApi() {}

  public static <T extends Entity & EasyModelRenderable>
      EasyModelEntityRenderDelegate<T> createRenderDelegate() {
    return new EasyModelEntityRenderDelegate<>();
  }

  public static <T extends BlockEntity & EasyModelRenderable>
      EasyModelBlockEntityRenderDelegate<T> createBlockEntityRenderDelegate() {
    return new EasyModelBlockEntityRenderDelegate<>();
  }

  public static boolean render(
      Identifier profileId,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float yaw,
      EasyModelEntityRenderOptions options) {
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");
    Optional<EasyModelRenderState> renderState =
        resolveRenderState(profileId, EasyModelAnimationState.AUTO);
    if (renderState.isEmpty()) {
      return false;
    }
    EasyModelEntityRenderBackend.render(
        renderState.get(),
        yaw,
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options,
        poseStack,
        bufferSource,
        packedLight);
    return true;
  }

  public static boolean render(
      Entity entity,
      Identifier profileId,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      float yaw,
      float partialTick,
      EasyModelEntityRenderOptions options) {
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(profileId, "profileId");
    Objects.requireNonNull(poseStack, "poseStack");
    Objects.requireNonNull(bufferSource, "bufferSource");
    Optional<EasyModelRenderState> renderState =
        resolveRenderState(profileId, EasyModelAnimationState.AUTO);
    if (renderState.isEmpty()) {
      return false;
    }
    EasyModelEntityRenderBackend.render(
        entity,
        renderState.get(),
        yaw,
        partialTick,
        options == null ? EasyModelEntityRenderOptions.DEFAULT : options,
        poseStack,
        bufferSource,
        packedLight);
    return true;
  }

  public static Optional<ModelBounds> getModelBounds(Identifier profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationState.AUTO)
        .map(EasyModelRenderState::bakedModel)
        .map(BakedModel::bounds);
  }

  public static Optional<ModelBounds> getDisplayedBounds(Identifier profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return resolveRenderState(profileId, EasyModelAnimationState.AUTO)
        .map(renderState -> renderState.bakedModel().bounds().scaled(renderState.scale()));
  }

  public static Optional<ModelBodyType> getBodyType(Identifier profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return EasyModelEntityRenderBackend.resolveContract(profileId, EasyModelAnimationState.AUTO)
        .map(EasyModelRuntimeContract::bodyType);
  }

  public static List<Identifier> listRenderableProfileIds() {
    return EasyModelServices.renderProfileService().getRenderProfiles().stream()
        .filter(EasyModelRenderProfile::isActive)
        .map(EasyModelRenderProfile::id)
        .sorted(Comparator.comparing(Identifier::toString))
        .toList();
  }

  public static List<Identifier> listRenderableProfileIds(ModelType modelType) {
    Objects.requireNonNull(modelType, "modelType");
    return listRenderableProfileIds().stream()
        .filter(id -> ModelType.fromProfileId(id) == modelType)
        .toList();
  }

  private static Optional<EasyModelRenderState> resolveRenderState(
      Identifier profileId, EasyModelAnimationState animationState) {
    return EasyModelEntityRenderBackend.resolveContract(profileId, animationState)
        .map(EasyModelEntityRenderBackend::resolveRenderState);
  }
}

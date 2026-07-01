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
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.client.EasyModelPartAnimator;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartAnimationMode;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.item.EasyModelEntitiesItems;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.Optional;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class EasyModelItemModelRenderer {

  private static final float ICON_SCALE = 0.9f;
  private static final float ICON_TILT_X = 5.0f;
  private static final float ICON_ROTATION_Y = 150.0f;
  private static final float MIN_SUBJECT_SIZE = 0.1f;

  private EasyModelItemModelRenderer() {}

  public static void render(
      ItemStack stack,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    Optional<ResourceLocation> profileId = EasyModelEntitiesItems.profileId(stack);
    if (profileId.isEmpty()) {
      return;
    }

    Optional<EasyModelEntityProfile> profile =
        EasyModelEntitiesApi.getProfile(profileId.get()).filter(EasyModelEntityProfile::isActive);
    if (profile.isEmpty()) {
      return;
    }

    EasyModelRuntimeContract contract =
        EasyModelRuntimeContract.fromProfile(profile.get(), EasyModelAnimationState.IDLE);
    EasyModelRenderState renderState = EasyModelEntityRenderBackend.resolveRenderState(contract);

    ModelBounds bounds = renderState.bakedModel().bounds();
    float horizontal = (float) Math.hypot(bounds.sizeX(), bounds.sizeZ());
    float subject = Math.max(Math.max(horizontal, bounds.sizeY()), MIN_SUBJECT_SIZE);
    float fit = ICON_SCALE / subject;
    Vec3f center = bounds.center();

    poseStack.pushPose();
    poseStack.translate(0.5f, 0.5f, 0.5f);
    poseStack.mulPose(Axis.XP.rotationDegrees(ICON_TILT_X));
    poseStack.mulPose(Axis.YP.rotationDegrees(ICON_ROTATION_Y));
    poseStack.scale(-fit, -fit, fit);
    poseStack.translate(-center.x(), -center.y(), -center.z());

    EasyModelBakedModelRenderer.render(
        renderState.bakedModel(),
        renderState,
        0.0f,
        0.0f,
        0.0f,
        0.0f,
        EasyModelPartAnimator.NONE,
        EasyModelPartAnimationMode.ADD,
        poseStack,
        bufferSource,
        LightTexture.FULL_BRIGHT);
    poseStack.popPose();
  }
}

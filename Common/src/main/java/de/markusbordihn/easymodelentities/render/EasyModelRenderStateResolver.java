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

package de.markusbordihn.easymodelentities.render;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBakeResult;
import de.markusbordihn.easymodelentities.data.model.bake.ModelCacheKey;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationMode;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationSettings;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.model.bake.EasyModelBakeService;
import de.markusbordihn.easymodelentities.model.bake.ModelFallbackFactory;
import de.markusbordihn.easymodelentities.model.bake.ModelTextureResolver;
import de.markusbordihn.easymodelentities.renderprofile.EasyModelRenderProfileService;
import de.markusbordihn.easymodelentities.runtime.AssetPairing;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EasyModelRenderStateResolver {

  public static final ResourceLocation FALLBACK_TEXTURE = ModelTextureResolver.FALLBACK_TEXTURE;
  private static final ResourceLocation FALLBACK_MODEL =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "fallback");
  private static final ModelAnimationSettings NO_ANIMATION =
      new ModelAnimationSettings(ModelAnimationMode.NONE, 1.0f, 1.0f);

  private EasyModelRenderStateResolver() {}

  public static EasyModelRenderState resolve(
      EasyModelRuntimeContract contract,
      EasyModelRenderProfileService renderProfileService,
      EasyModelBakeService bakeService,
      ResourceManager resourceManager) {
    Objects.requireNonNull(contract, "contract");
    Objects.requireNonNull(renderProfileService, "renderProfileService");
    Objects.requireNonNull(bakeService, "bakeService");
    Objects.requireNonNull(resourceManager, "resourceManager");

    return renderProfileService
        .getRenderProfile(contract.renderProfileId())
        .filter(EasyModelRenderProfile::isRenderable)
        .map(renderProfile -> resolveProfile(contract, renderProfile, bakeService, resourceManager))
        .orElseGet(
            () ->
                fallback(
                    contract,
                    new ModelRenderProfileValidationIssue(
                        ModelRenderProfileStatus.MISSING_RENDER_PROFILE,
                        "render_profile",
                        "Missing render profile " + contract.renderProfileId() + ".")));
  }

  private static EasyModelRenderState resolveProfile(
      EasyModelRuntimeContract contract,
      EasyModelRenderProfile renderProfile,
      EasyModelBakeService bakeService,
      ResourceManager resourceManager) {
    if (renderProfile.bodyType() != contract.bodyType()) {
      return fallback(
          contract,
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.CLIENT_BODY_TYPE_MISMATCH,
              "body_type",
              "Render profile body type "
                  + renderProfile.bodyType().getSerializedName()
                  + " does not match runtime body type "
                  + contract.bodyType().getSerializedName()
                  + "."));
    }

    if (!AssetPairing.matches(contract.version(), renderProfile.version())) {
      return fallback(
          contract,
          new ModelRenderProfileValidationIssue(
              ModelRenderProfileStatus.CLIENT_ASSET_MISMATCH,
              "version",
              "Render profile version does not match runtime version."));
    }

    ModelBakeResult bakeResult = bakeService.bake(renderProfile, resourceManager);
    boolean fallbackTexture =
        bakeResult.validationIssues().stream()
            .anyMatch(issue -> issue.status() == ModelRenderProfileStatus.MISSING_TEXTURE);
    Map<Integer, ResourceLocation> textures = bakeResult.bakedModel().textures();
    ResourceLocation texture = textures.getOrDefault(0, FALLBACK_TEXTURE);
    return new EasyModelRenderState(
        bakeResult.bakedModel(),
        texture,
        textures,
        renderProfile.scale(),
        renderProfile.shadowRadius(),
        renderProfile.visibleBoundsWidth(),
        renderProfile.visibleBoundsHeight(),
        renderProfile.visibleBoundsOffset(),
        renderProfile.bodyType(),
        renderProfile.animation(),
        bakeResult.fallback(),
        fallbackTexture,
        bakeResult.validationIssues());
  }

  private static EasyModelRenderState fallback(
      EasyModelRuntimeContract contract, ModelRenderProfileValidationIssue issue) {
    ModelCacheKey cacheKey = new ModelCacheKey(FALLBACK_MODEL, contract.version());
    return new EasyModelRenderState(
        ModelFallbackFactory.createFallback(
            cacheKey.modelId(), contract.width(), contract.height()),
        FALLBACK_TEXTURE,
        1.0f,
        0.3f,
        contract.bodyType(),
        NO_ANIMATION,
        true,
        true,
        List.of(issue));
  }
}

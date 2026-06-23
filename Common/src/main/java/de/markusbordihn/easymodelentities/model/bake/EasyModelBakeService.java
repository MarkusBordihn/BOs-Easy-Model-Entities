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

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.data.model.bake.*;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public interface EasyModelBakeService {

  EasyModelBakeService EMPTY = new EasyModelBakeService() {};

  default ModelBakeResult bake(
      EasyModelRenderProfile renderProfile, ResourceManager resourceManager) {
    ModelCacheKey cacheKey = cacheKey(renderProfile.model(), cacheDiscriminator(renderProfile));
    return ModelBakeResult.failure(
        cacheKey,
        ModelFallbackFactory.createFallback(cacheKey.modelId()),
        renderProfile.validationIssues());
  }

  default Optional<ModelBakeResult> getCached(ResourceLocation modelId, String assetFingerprint) {
    return Optional.empty();
  }

  default ModelCacheKey cacheKey(ResourceLocation modelId, String assetFingerprint) {
    return new ModelCacheKey(modelId, assetFingerprint);
  }

  default String cacheDiscriminator(EasyModelRenderProfile renderProfile) {
    return renderProfile.assetFingerprint().isBlank()
        ? renderProfile.version()
        : renderProfile.assetFingerprint();
  }

  default void clearCache() {}
}

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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileValidationIssue;
import java.util.List;
import java.util.Objects;

public record ModelBakeResult(
    ModelCacheKey cacheKey,
    BakedModel bakedModel,
    boolean fallback,
    List<ModelRenderProfileValidationIssue> validationIssues) {

  public ModelBakeResult {
    Objects.requireNonNull(cacheKey, "cacheKey");
    Objects.requireNonNull(bakedModel, "bakedModel");
    validationIssues = List.copyOf(Objects.requireNonNull(validationIssues, "validationIssues"));
  }

  public static ModelBakeResult success(
      ModelCacheKey cacheKey,
      BakedModel bakedModel,
      List<ModelRenderProfileValidationIssue> validationIssues) {
    return new ModelBakeResult(cacheKey, bakedModel, false, validationIssues);
  }

  public static ModelBakeResult failure(
      ModelCacheKey cacheKey, List<ModelRenderProfileValidationIssue> validationIssues) {
    return new ModelBakeResult(
        cacheKey, ModelFallbackFactory.createFallback(cacheKey.modelId()), true, validationIssues);
  }

  public boolean successful() {
    return !this.fallback;
  }
}

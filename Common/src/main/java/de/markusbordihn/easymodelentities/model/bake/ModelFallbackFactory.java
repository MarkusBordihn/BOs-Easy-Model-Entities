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

import de.markusbordihn.easymodelentities.data.model.bake.*;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class ModelFallbackFactory {

  private ModelFallbackFactory() {}

  public static BakedModel createFallback(ResourceLocation modelId) {
    return createFallback(modelId, 0.6f, 1.8f);
  }

  public static BakedModel createFallback(ResourceLocation modelId, float width, float height) {
    float cubeWidth = Math.max(width, 0.1f) * 16.0f;
    float cubeHeight = Math.max(height, 0.1f) * 16.0f;
    return new BakedModel(
        modelId,
        16,
        16,
        List.of(
            new BakedModelPart(
                "root",
                new float[] {0.0f, 24.0f, 0.0f},
                new float[] {0.0f, 0.0f, 0.0f},
                List.of(
                    new BakedModelCube(
                        new int[] {0, 0},
                        new float[] {-cubeWidth / 2.0f, -cubeHeight, -cubeWidth / 2.0f},
                        new float[] {cubeWidth, cubeHeight, cubeWidth},
                        false)),
                List.of())));
  }
}

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

import java.util.List;
import net.minecraft.resources.ResourceLocation;

final class ModelFallbackFactory {

  private ModelFallbackFactory() {}

  static BakedModel createFallback(ResourceLocation modelId) {
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
                        new float[] {-4.0f, -8.0f, -4.0f},
                        new float[] {8.0f, 8.0f, 8.0f},
                        false)),
                List.of())));
  }
}

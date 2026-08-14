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

import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSlot;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record EasyModelResolvedTextures(Map<Integer, EasyModelTextureSlot> byIndex) {

  public static final EasyModelResolvedTextures NONE = new EasyModelResolvedTextures(Map.of());

  public EasyModelResolvedTextures {
    byIndex = Map.copyOf(Objects.requireNonNull(byIndex, "byIndex"));
  }

  public static EasyModelResolvedTextures of(Map<Integer, EasyModelTextureSlot> byIndex) {
    return byIndex == null || byIndex.isEmpty() ? NONE : new EasyModelResolvedTextures(byIndex);
  }

  public boolean isEmpty() {
    return this.byIndex.isEmpty();
  }

  public ResourceLocation texture(int textureIndex) {
    EasyModelTextureSlot slot = this.byIndex.get(textureIndex);
    return slot == null ? null : slot.texture().orElse(null);
  }

  public EasyModelTextureBlend blend(int textureIndex) {
    EasyModelTextureSlot slot = this.byIndex.get(textureIndex);
    return slot == null ? EasyModelTextureBlend.DEFAULT : slot.blend();
  }
}

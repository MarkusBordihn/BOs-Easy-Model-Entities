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

package de.markusbordihn.easymodelentities.profile;

import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record EasyModelEntityProfile(
    ResourceLocation id,
    ResourceLocation hostEntityType,
    String movementType,
    EasyModelBodyType bodyType,
    ResourceLocation renderProfileId,
    String assetFingerprint,
    float width,
    float height,
    float eyeHeight,
    Set<ResourceLocation> traits) {

  public EasyModelEntityProfile {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(hostEntityType, "hostEntityType");
    Objects.requireNonNull(movementType, "movementType");
    Objects.requireNonNull(bodyType, "bodyType");
    Objects.requireNonNull(renderProfileId, "renderProfileId");
    Objects.requireNonNull(assetFingerprint, "assetFingerprint");
    traits = Set.copyOf(Objects.requireNonNull(traits, "traits"));
  }

  public boolean hasTrait(ResourceLocation traitId) {
    return this.traits.contains(traitId);
  }
}

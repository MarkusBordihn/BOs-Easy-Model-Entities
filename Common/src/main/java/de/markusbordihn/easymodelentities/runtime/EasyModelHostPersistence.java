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

package de.markusbordihn.easymodelentities.runtime;

import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class EasyModelHostPersistence {

  public static final String PROFILE_ID_TAG = "ProfileId";
  public static final String RENDER_PROFILE_ID_TAG = "RenderProfileId";
  public static final String VERSION_TAG = "Version";
  public static final String BODY_TYPE_TAG = "BodyType";
  public static final String ANIMATION_STATE_TAG = "AnimationState";

  private EasyModelHostPersistence() {}

  public static ResourceLocation parseResourceLocation(String resourceLocation) {
    return resourceLocation == null || resourceLocation.isBlank()
        ? null
        : ResourceLocation.tryParse(resourceLocation);
  }

  public static ResourceLocation parseResourceLocationOrMissing(
      String resourceLocation, ResourceLocation missing) {
    ResourceLocation parsedResourceLocation = parseResourceLocation(resourceLocation);
    return parsedResourceLocation == null ? missing : parsedResourceLocation;
  }

  public static State read(CompoundTag compoundTag) {
    return new State(
        parseResourceLocation(compoundTag.getString(PROFILE_ID_TAG)),
        parseResourceLocation(compoundTag.getString(RENDER_PROFILE_ID_TAG)),
        compoundTag.getString(VERSION_TAG),
        ModelBodyType.bySerializedName(compoundTag.getString(BODY_TYPE_TAG)),
        EasyModelAnimationState.bySerializedName(compoundTag.getString(ANIMATION_STATE_TAG)));
  }

  public record State(
      ResourceLocation profileId,
      ResourceLocation renderProfileId,
      String version,
      ModelBodyType bodyType,
      EasyModelAnimationState animationState) {}
}

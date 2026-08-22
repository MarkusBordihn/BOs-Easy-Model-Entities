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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class EasyModelHostPersistence {

  public static final String PROFILE_ID_TAG = "ProfileId";
  public static final String RENDER_PROFILE_ID_TAG = "RenderProfileId";
  public static final String VERSION_TAG = "Version";
  public static final String BODY_TYPE_TAG = "BodyType";
  public static final String ANIMATION_TAG = "Animation";
  public static final String TEXTURE_TAG = "Texture";
  public static final String OPACITY_TAG = "Opacity";
  public static final String LIGHT_LEVEL_TAG = "LightLevel";

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
        readAnimation(compoundTag),
        readTexture(compoundTag),
        readOpacity(compoundTag),
        readLightLevel(compoundTag));
  }

  public static void writeAnimation(CompoundTag compoundTag, EasyModelAnimationSetting animation) {
    compoundTag.put(ANIMATION_TAG, animation.createTag());
  }

  public static void writeTexture(CompoundTag compoundTag, EasyModelTextureSetting texture) {
    if (texture.isEmpty()) {
      compoundTag.remove(TEXTURE_TAG);
      return;
    }

    compoundTag.put(TEXTURE_TAG, texture.createTag());
  }

  public static void writeOpacity(CompoundTag compoundTag, float opacity) {
    if (!EasyModelDisplaySettings.hasOpacityOverride(opacity)) {
      compoundTag.remove(OPACITY_TAG);
      return;
    }

    compoundTag.putFloat(OPACITY_TAG, EasyModelDisplaySettings.clampOpacityOverride(opacity));
  }

  public static void writeLightLevel(CompoundTag compoundTag, int lightLevel) {
    if (lightLevel <= EasyModelDisplaySettings.NO_LIGHT_LEVEL) {
      compoundTag.remove(LIGHT_LEVEL_TAG);
      return;
    }

    compoundTag.putInt(LIGHT_LEVEL_TAG, EasyModelDisplaySettings.clampLightLevel(lightLevel));
  }

  private static float readOpacity(CompoundTag compoundTag) {
    if (!compoundTag.contains(OPACITY_TAG)) {
      return EasyModelDisplaySettings.NO_OPACITY;
    }

    return EasyModelDisplaySettings.clampOpacityOverride(compoundTag.getFloat(OPACITY_TAG));
  }

  private static int readLightLevel(CompoundTag compoundTag) {
    if (!compoundTag.contains(LIGHT_LEVEL_TAG)) {
      return EasyModelDisplaySettings.NO_LIGHT_LEVEL;
    }

    return EasyModelDisplaySettings.clampLightLevel(compoundTag.getInt(LIGHT_LEVEL_TAG));
  }

  private static EasyModelAnimationSetting readAnimation(CompoundTag compoundTag) {
    return EasyModelAnimationSetting.fromTag(compoundTag.get(ANIMATION_TAG))
        .orElse(EasyModelAnimationSetting.AUTO);
  }

  private static EasyModelTextureSetting readTexture(CompoundTag compoundTag) {
    return EasyModelTextureSetting.fromTag(compoundTag.get(TEXTURE_TAG))
        .orElse(EasyModelTextureSetting.EMPTY);
  }

  public record State(
      ResourceLocation profileId,
      ResourceLocation renderProfileId,
      String version,
      ModelBodyType bodyType,
      EasyModelAnimationSetting animation,
      EasyModelTextureSetting texture,
      float opacity,
      int lightLevel) {}
}

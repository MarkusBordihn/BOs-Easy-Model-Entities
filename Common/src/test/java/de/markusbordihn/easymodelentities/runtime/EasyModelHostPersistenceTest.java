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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelHostPersistenceTest {

  private static final ResourceLocation SAD =
      ResourceLocation.fromNamespaceAndPath("example", "textures/entity/echo/screen_sad.png");

  @Test
  void textureRoundTripsThroughTheSaveTag() {
    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SAD);
    CompoundTag compoundTag = new CompoundTag();

    EasyModelHostPersistence.writeTexture(compoundTag, textureSetting);

    assertEquals(textureSetting, EasyModelHostPersistence.read(compoundTag).texture());
  }

  @Test
  @DisplayName("An empty texture setting leaves no tag behind, so old saves stay untouched")
  void emptyTextureWritesNoTag() {
    CompoundTag compoundTag = new CompoundTag();
    EasyModelHostPersistence.writeTexture(compoundTag, EasyModelTextureSetting.of("screen", SAD));
    assertTrue(compoundTag.contains(EasyModelHostPersistence.TEXTURE_TAG));

    EasyModelHostPersistence.writeTexture(compoundTag, EasyModelTextureSetting.EMPTY);

    assertFalse(compoundTag.contains(EasyModelHostPersistence.TEXTURE_TAG));
  }

  @Test
  void missingTextureTagReadsAsEmpty() {
    assertEquals(
        EasyModelTextureSetting.EMPTY, EasyModelHostPersistence.read(new CompoundTag()).texture());
  }

  @Test
  void opacityRoundTripsThroughTheSaveTag() {
    CompoundTag compoundTag = new CompoundTag();

    EasyModelHostPersistence.writeOpacity(compoundTag, 0.4f);

    assertEquals(0.4f, EasyModelHostPersistence.read(compoundTag).opacity(), 0.0001f);
  }

  @Test
  @DisplayName("A fully opaque override survives the save tag instead of reading as no override")
  void fullyOpaqueOverrideRoundTrips() {
    CompoundTag compoundTag = new CompoundTag();

    EasyModelHostPersistence.writeOpacity(compoundTag, EasyModelDisplaySettings.MAX_OPACITY);

    assertTrue(compoundTag.contains(EasyModelHostPersistence.OPACITY_TAG));
    assertEquals(
        EasyModelDisplaySettings.MAX_OPACITY,
        EasyModelHostPersistence.read(compoundTag).opacity(),
        0.0001f);
  }

  @Test
  @DisplayName("Clearing the opacity leaves no tag behind, so old saves stay untouched")
  void clearedOpacityWritesNoTag() {
    CompoundTag compoundTag = new CompoundTag();
    EasyModelHostPersistence.writeOpacity(compoundTag, 0.4f);
    assertTrue(compoundTag.contains(EasyModelHostPersistence.OPACITY_TAG));

    EasyModelHostPersistence.writeOpacity(compoundTag, EasyModelDisplaySettings.NO_OPACITY);

    assertFalse(compoundTag.contains(EasyModelHostPersistence.OPACITY_TAG));
  }

  @Test
  void missingOpacityTagReadsAsNoOverride() {
    assertEquals(
        EasyModelDisplaySettings.NO_OPACITY,
        EasyModelHostPersistence.read(new CompoundTag()).opacity(),
        0.0001f);
  }

  @Test
  void lightLevelRoundTripsThroughTheSaveTag() {
    CompoundTag compoundTag = new CompoundTag();

    EasyModelHostPersistence.writeLightLevel(compoundTag, 12);

    assertEquals(12, EasyModelHostPersistence.read(compoundTag).lightLevel());
  }

  @Test
  @DisplayName("Clearing the light level leaves no tag behind, so old saves stay untouched")
  void clearedLightLevelWritesNoTag() {
    CompoundTag compoundTag = new CompoundTag();
    EasyModelHostPersistence.writeLightLevel(compoundTag, 12);
    assertTrue(compoundTag.contains(EasyModelHostPersistence.LIGHT_LEVEL_TAG));

    EasyModelHostPersistence.writeLightLevel(compoundTag, EasyModelDisplaySettings.NO_LIGHT_LEVEL);

    assertFalse(compoundTag.contains(EasyModelHostPersistence.LIGHT_LEVEL_TAG));
  }

  @Test
  void missingLightLevelTagReadsAsNoOverride() {
    assertEquals(
        EasyModelDisplaySettings.NO_LIGHT_LEVEL,
        EasyModelHostPersistence.read(new CompoundTag()).lightLevel());
  }
}

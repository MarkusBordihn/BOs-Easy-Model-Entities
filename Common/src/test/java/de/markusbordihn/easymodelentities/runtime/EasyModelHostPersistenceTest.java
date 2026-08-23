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
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelHostPersistenceTest {

  private static final Identifier SAD =
      Identifier.fromNamespaceAndPath("example", "textures/entity/echo/screen_sad.png");

  private static EasyModelHostPersistence.State read(CompoundTag compoundTag) {
    return EasyModelHostPersistence.read(
        TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, compoundTag));
  }

  private static EasyModelTextureSetting readTexture(CompoundTag compoundTag) {
    return read(compoundTag).texture();
  }

  private static CompoundTag writeTexture(EasyModelTextureSetting textureSetting) {
    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeTexture(valueOutput, textureSetting);

    return valueOutput.buildResult();
  }

  private static CompoundTag writeOpacity(float opacity) {
    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeOpacity(valueOutput, opacity);

    return valueOutput.buildResult();
  }

  private static CompoundTag writeLightLevel(int lightLevel) {
    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeLightLevel(valueOutput, lightLevel);

    return valueOutput.buildResult();
  }

  @Test
  void textureRoundTripsThroughTheSaveTag() {
    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SAD);

    assertEquals(textureSetting, readTexture(writeTexture(textureSetting)));
  }

  @Test
  @DisplayName("An empty texture setting leaves no tag behind, so old saves stay untouched")
  void emptyTextureWritesNoTag() {
    assertTrue(
        writeTexture(EasyModelTextureSetting.of("screen", SAD))
            .contains(EasyModelHostPersistence.TEXTURE_TAG));

    assertFalse(
        writeTexture(EasyModelTextureSetting.EMPTY).contains(EasyModelHostPersistence.TEXTURE_TAG));
  }

  @Test
  void missingTextureTagReadsAsEmpty() {
    assertEquals(EasyModelTextureSetting.EMPTY, readTexture(new CompoundTag()));
  }

  @Test
  void opacityRoundTripsThroughTheSaveTag() {
    assertEquals(0.4f, read(writeOpacity(0.4f)).opacity(), 0.0001f);
  }

  @Test
  @DisplayName("A fully opaque override survives the save tag instead of reading as no override")
  void fullyOpaqueOverrideRoundTrips() {
    CompoundTag compoundTag = writeOpacity(EasyModelDisplaySettings.MAX_OPACITY);

    assertTrue(compoundTag.contains(EasyModelHostPersistence.OPACITY_TAG));
    assertEquals(EasyModelDisplaySettings.MAX_OPACITY, read(compoundTag).opacity(), 0.0001f);
  }

  @Test
  @DisplayName("Clearing the opacity leaves no tag behind, so old saves stay untouched")
  void clearedOpacityWritesNoTag() {
    assertTrue(writeOpacity(0.4f).contains(EasyModelHostPersistence.OPACITY_TAG));

    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeOpacity(valueOutput, 0.4f);
    EasyModelHostPersistence.writeOpacity(valueOutput, EasyModelDisplaySettings.NO_OPACITY);

    assertFalse(valueOutput.buildResult().contains(EasyModelHostPersistence.OPACITY_TAG));
  }

  @Test
  void missingOpacityTagReadsAsNoOverride() {
    assertEquals(EasyModelDisplaySettings.NO_OPACITY, read(new CompoundTag()).opacity(), 0.0001f);
  }

  @Test
  void lightLevelRoundTripsThroughTheSaveTag() {
    assertEquals(12, read(writeLightLevel(12)).lightLevel());
  }

  @Test
  @DisplayName("Clearing the light level leaves no tag behind, so old saves stay untouched")
  void clearedLightLevelWritesNoTag() {
    assertTrue(writeLightLevel(12).contains(EasyModelHostPersistence.LIGHT_LEVEL_TAG));

    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeLightLevel(valueOutput, 12);
    EasyModelHostPersistence.writeLightLevel(valueOutput, EasyModelDisplaySettings.NO_LIGHT_LEVEL);

    assertFalse(valueOutput.buildResult().contains(EasyModelHostPersistence.LIGHT_LEVEL_TAG));
  }

  @Test
  void missingLightLevelTagReadsAsNoOverride() {
    assertEquals(EasyModelDisplaySettings.NO_LIGHT_LEVEL, read(new CompoundTag()).lightLevel());
  }
}

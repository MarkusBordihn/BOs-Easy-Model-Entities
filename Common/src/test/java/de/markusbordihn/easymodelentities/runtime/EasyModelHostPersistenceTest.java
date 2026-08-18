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

  private static EasyModelTextureSetting readTexture(CompoundTag compoundTag) {
    return EasyModelHostPersistence.read(
            TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, compoundTag))
        .texture();
  }

  private static CompoundTag writeTexture(EasyModelTextureSetting textureSetting) {
    TagValueOutput valueOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
    EasyModelHostPersistence.writeTexture(valueOutput, textureSetting);

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
}

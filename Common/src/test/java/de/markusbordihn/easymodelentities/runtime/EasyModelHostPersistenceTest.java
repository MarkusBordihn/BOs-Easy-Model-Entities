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
}

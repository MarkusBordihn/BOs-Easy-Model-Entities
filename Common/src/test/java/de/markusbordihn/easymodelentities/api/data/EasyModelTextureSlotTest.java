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

package de.markusbordihn.easymodelentities.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelTextureSlotTest {

  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mnemosyne_lost_echoes", "textures/entity/echo/screen_sad.png");

  @Test
  @DisplayName("A slot without extra fields stays a plain string, so saves stay compact")
  void encodesAsPlainString() {
    Tag tag =
        EasyModelTextureSlot.CODEC
            .encodeStart(NbtOps.INSTANCE, new EasyModelTextureSlot(TEXTURE))
            .result()
            .orElseThrow();

    assertEquals(StringTag.valueOf(TEXTURE.toString()), tag);
  }

  @Test
  void plainStringParsesBackIntoASlot() {
    EasyModelTextureSlot slot =
        EasyModelTextureSlot.CODEC
            .parse(NbtOps.INSTANCE, StringTag.valueOf(TEXTURE.toString()))
            .result()
            .orElseThrow();

    assertEquals(Optional.of(TEXTURE), slot.texture());
    assertEquals(EasyModelTextureBlend.CUTOUT, slot.blend());
  }

  @Test
  @DisplayName("The object form and unknown fields must parse, so later slot options stay readable")
  void objectFormWithUnknownFieldsParses() {
    CompoundTag compoundTag = new CompoundTag();
    compoundTag.putString("texture", TEXTURE.toString());
    compoundTag.putBoolean("emissive", true);

    EasyModelTextureSlot slot =
        EasyModelTextureSlot.CODEC.parse(NbtOps.INSTANCE, compoundTag).result().orElseThrow();

    assertEquals(Optional.of(TEXTURE), slot.texture());
  }

  @Test
  @DisplayName("A translucent slot falls back to the object form, so the blend survives a save")
  void translucentSlotEncodesAsObject() {
    EasyModelTextureSlot slot = EasyModelTextureSlot.of(TEXTURE, EasyModelTextureBlend.TRANSLUCENT);
    Tag tag = EasyModelTextureSlot.CODEC.encodeStart(NbtOps.INSTANCE, slot).result().orElseThrow();

    assertEquals(
        slot, EasyModelTextureSlot.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow());
    assertEquals(CompoundTag.class, tag.getClass());
  }

  @Test
  @DisplayName("A blend without a texture keeps the original texture of the model")
  void blendOnlySlotHasNoTexture() {
    EasyModelTextureSlot slot = EasyModelTextureSlot.of(EasyModelTextureBlend.TRANSLUCENT);
    Tag tag = EasyModelTextureSlot.CODEC.encodeStart(NbtOps.INSTANCE, slot).result().orElseThrow();

    assertEquals(Optional.empty(), slot.texture());
    assertEquals(
        slot, EasyModelTextureSlot.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow());
  }

  @Test
  void rejectsNullTexture() {
    assertThrows(NullPointerException.class, () -> new EasyModelTextureSlot(null));
  }
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelTextureSettingTest {

  private static final ResourceLocation SCREEN =
      new ResourceLocation("mnemosyne_lost_echoes", "textures/entity/echo/screen_sad.png");
  private static final ResourceLocation BODY =
      new ResourceLocation("mnemosyne_lost_echoes", "textures/entity/echo/body_worn.png");

  @Test
  void roundTripsThroughItsTag() {
    EasyModelTextureSetting setting =
        EasyModelTextureSetting.of("screen", SCREEN)
            .withSlot(EasyModelTextureSetting.DEFAULT_SLOT, BODY);

    assertEquals(setting, EasyModelTextureSetting.fromTag(setting.createTag()).orElseThrow());
  }

  @Test
  void emptyTagReadsAsAnEmptySetting() {
    assertTrue(EasyModelTextureSetting.fromTag(new CompoundTag()).orElseThrow().isEmpty());
    assertTrue(EasyModelTextureSetting.fromTag(null).isEmpty());
  }

  @Test
  void slotNamesAreNormalized() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("  Screen  ", SCREEN);

    assertEquals(Map.of("screen", new EasyModelTextureSlot(SCREEN)), setting.slots());
    assertEquals(SCREEN, setting.texture("SCREEN").orElseThrow());
  }

  @Test
  @DisplayName("Index escapes stay usable, other reserved names must not become slots")
  void reservedSlotNames() {
    assertEquals("#2", EasyModelTextureSetting.normalizeSlot("#2").orElseThrow());
    assertEquals("2", EasyModelTextureSetting.normalizeSlot("2").orElseThrow());
    assertTrue(EasyModelTextureSetting.normalizeSlot("#screen").isEmpty());
    assertTrue(EasyModelTextureSetting.normalizeSlot("profile:screen").isEmpty());
    assertTrue(EasyModelTextureSetting.normalizeSlot("").isEmpty());
    assertTrue(EasyModelTextureSetting.normalizeSlot(null).isEmpty());
    assertTrue(EasyModelTextureSetting.normalizeSlot("a".repeat(65)).isEmpty());
  }

  @Test
  void invalidSlotNameIsDroppedInsteadOfFailing() {
    assertTrue(EasyModelTextureSetting.of("profile:screen", SCREEN).isEmpty());
    assertTrue(
        new EasyModelTextureSetting(Map.of("bad name", new EasyModelTextureSlot(SCREEN)))
            .isEmpty());
  }

  @Test
  void slotCountIsCapped() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.EMPTY;
    for (int i = 0; i < EasyModelTextureSetting.MAX_SLOTS + 8; i++) {
      setting = setting.withSlot("slot_" + i, SCREEN);
    }

    assertEquals(EasyModelTextureSetting.MAX_SLOTS, setting.slots().size());
  }

  @Test
  void copyMethodsDoNotMutateTheSource() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("screen", SCREEN);

    assertEquals(1, setting.slots().size());
    assertEquals(2, setting.withSlot("default", BODY).slots().size());
    assertTrue(setting.withoutSlot("screen").isEmpty());
    assertTrue(setting.withoutSlots().isEmpty());
    assertEquals(SCREEN, setting.texture("screen").orElseThrow());
  }

  @Test
  void unchangedCopyReturnsTheSameInstance() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("screen", SCREEN);

    assertSame(setting, setting.withSlot("screen", SCREEN));
    assertSame(setting, setting.withSlot("screen", null));
    assertSame(setting, setting.withoutSlot("unknown"));
  }

  @Test
  void unknownSlotHasNoTexture() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("screen", SCREEN);

    assertTrue(setting.texture("unknown").isEmpty());
    assertFalse(setting.isEmpty());
  }

  @Test
  @DisplayName("A texture change keeps the blend of its slot, and the other way round")
  void textureAndBlendAreSetIndependently() {
    EasyModelTextureSetting setting =
        EasyModelTextureSetting.of("screen", SCREEN)
            .withBlend("screen", EasyModelTextureBlend.TRANSLUCENT);

    assertEquals(EasyModelTextureBlend.TRANSLUCENT, setting.blend("screen"));
    assertEquals(
        EasyModelTextureBlend.TRANSLUCENT, setting.withSlot("screen", BODY).blend("screen"));
    assertEquals(BODY, setting.withSlot("screen", BODY).texture("screen").orElseThrow());
    assertEquals(
        EasyModelTextureBlend.CUTOUT,
        setting.withSlot("screen", BODY, EasyModelTextureBlend.CUTOUT).blend("screen"));
  }

  @Test
  @DisplayName("A blend can be set for a slot that keeps its own texture")
  void blendWithoutATextureIsItsOwnSlot() {
    EasyModelTextureSetting setting =
        EasyModelTextureSetting.EMPTY.withBlend("screen", EasyModelTextureBlend.TRANSLUCENT);

    assertTrue(setting.texture("screen").isEmpty());
    assertEquals(EasyModelTextureBlend.TRANSLUCENT, setting.blend("screen"));
    assertEquals(setting, EasyModelTextureSetting.fromTag(setting.createTag()).orElseThrow());
  }

  @Test
  @DisplayName("Resetting the blend of a texture-less slot drops it instead of keeping a no-op")
  void blendResetRemovesAnEmptySlot() {
    EasyModelTextureSetting setting =
        EasyModelTextureSetting.EMPTY
            .withBlend("screen", EasyModelTextureBlend.TRANSLUCENT)
            .withBlend("screen", EasyModelTextureBlend.CUTOUT);

    assertTrue(setting.isEmpty());
    assertEquals(EasyModelTextureBlend.CUTOUT, setting.blend("unknown"));
  }

  @Test
  void slotsAreImmutable() {
    EasyModelTextureSetting setting = EasyModelTextureSetting.of("screen", SCREEN);

    assertThrows(
        UnsupportedOperationException.class,
        () -> setting.slots().put("default", new EasyModelTextureSlot(BODY)));
  }
}

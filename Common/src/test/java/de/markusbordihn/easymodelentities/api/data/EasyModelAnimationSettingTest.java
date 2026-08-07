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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationSettingTest {

  @Test
  void standardStateRoundTripsThroughItsTag() {
    EasyModelAnimationSetting setting =
        new EasyModelAnimationSetting(EasyModelAnimation.WALK, EasyModelAnimationLoop.LOOP);

    assertEquals(setting, EasyModelAnimationSetting.fromTag(setting.createTag()).orElseThrow());
  }

  @Test
  @DisplayName("A named clip must survive persistence, otherwise a loop is lost on world reload")
  void namedClipRoundTripsThroughItsTag() {
    EasyModelAnimationSetting setting =
        new EasyModelAnimationSetting(
            EasyModelAnimation.named("spin"), EasyModelAnimationLoop.LOOP);

    EasyModelAnimationSetting loaded =
        EasyModelAnimationSetting.fromTag(setting.createTag()).orElseThrow();

    assertTrue(loaded.animation().isNamed());
    assertEquals("spin", loaded.animation().name());
    assertEquals(EasyModelAnimationLoop.LOOP, loaded.loop());
  }

  @Test
  void tagWithoutLoopFallsBackToTheClipSetting() {
    CompoundTag compoundTag = new CompoundTag();
    compoundTag.putString("animation", "idle");

    EasyModelAnimationSetting loaded = EasyModelAnimationSetting.fromTag(compoundTag).orElseThrow();

    assertEquals(EasyModelAnimation.IDLE, loaded.animation());
    assertEquals(EasyModelAnimationLoop.CLIP, loaded.loop());
  }

  @Test
  void unreadableTagStaysEmptySoCallersCanFallBack() {
    assertTrue(EasyModelAnimationSetting.fromTag(null).isEmpty());
    assertTrue(EasyModelAnimationSetting.fromTag(new CompoundTag()).isEmpty());
  }

  @Test
  void loopOverrideOnlyAppliesToAnExplicitAnimation() {
    assertNull(EasyModelAnimationSetting.AUTO.withLoop(EasyModelAnimationLoop.LOOP).loopOverride());
    assertEquals(
        Boolean.TRUE,
        EasyModelAnimationSetting.of(EasyModelAnimation.named("spin"))
            .withLoop(EasyModelAnimationLoop.LOOP)
            .loopOverride());
    assertEquals(
        Boolean.FALSE,
        EasyModelAnimationSetting.of(EasyModelAnimation.IDLE)
            .withLoop(EasyModelAnimationLoop.ONCE)
            .loopOverride());
    assertNull(EasyModelAnimationSetting.of(EasyModelAnimation.IDLE).loopOverride());
  }

  @Test
  void copyMethodsKeepTheOtherComponent() {
    EasyModelAnimationSetting setting =
        new EasyModelAnimationSetting(EasyModelAnimation.FLY, EasyModelAnimationLoop.LOOP);

    assertEquals(
        EasyModelAnimationLoop.LOOP, setting.withAnimation(EasyModelAnimation.SWIM).loop());
    assertEquals(EasyModelAnimation.FLY, setting.withLoop(EasyModelAnimationLoop.ONCE).animation());
  }

  @Test
  void rejectsNullComponents() {
    assertThrows(
        NullPointerException.class,
        () -> new EasyModelAnimationSetting(null, EasyModelAnimationLoop.CLIP));
    assertThrows(
        NullPointerException.class,
        () -> new EasyModelAnimationSetting(EasyModelAnimation.IDLE, null));
  }

  @Test
  void unknownLoopNameFallsBackToTheClipSetting() {
    Tag tag =
        EasyModelAnimationLoop.CODEC
            .encodeStart(NbtOps.INSTANCE, EasyModelAnimationLoop.LOOP)
            .result()
            .orElseThrow();

    assertEquals(EasyModelAnimationLoop.LOOP, EasyModelAnimationLoop.parse("LOOP").orElseThrow());
    assertTrue(EasyModelAnimationLoop.parse("nonsense").isEmpty());
    assertEquals("loop", tag.asString().orElseThrow());
  }
}

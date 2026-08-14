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

package de.markusbordihn.easymodelentities.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationVariantMode;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelAnimationVariantTrackerTest {

  private static final long SEED = 4711L;

  private static BakedModel model(boolean loop, float length, String... clipNames) {
    Map<String, ModelAnimationClip> clips = new HashMap<>();
    for (String clipName : clipNames) {
      clips.put(clipName, new ModelAnimationClip(clipName, length, loop, Map.of()));
    }

    return new BakedModel(
        ResourceLocation.fromNamespaceAndPath("example", "variants"),
        16,
        16,
        List.of(),
        Map.of(),
        false,
        clips,
        null);
  }

  private static EasyModelAnimationPlaybackFrame automaticFrame() {
    return EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.AUTO, 0.0f, null, false);
  }

  private static EasyModelAnimationVariantFrame resolveIdle(
      EasyModelAnimationVariantTracker<Object> tracker,
      Object target,
      BakedModel bakedModel,
      double currentTicks) {
    return tracker.resolve(
        target,
        SEED,
        bakedModel,
        ModelAnimationVariantMode.RANDOM,
        automaticFrame(),
        "idle",
        currentTicks,
        0.0f,
        0.0f);
  }

  private static EasyModelAnimationVariantFrame resolve(
      EasyModelAnimationVariantTracker<Object> tracker,
      Object target,
      BakedModel bakedModel,
      String automaticClipName,
      float walkCycles,
      float attackAmount) {
    return tracker.resolve(
        target,
        SEED,
        bakedModel,
        ModelAnimationVariantMode.RANDOM,
        automaticFrame(),
        automaticClipName,
        0.0,
        walkCycles,
        attackAmount);
  }

  @Test
  @DisplayName("A looping clip keeps its variant until the cycle ends and then restarts at zero")
  void loopingClipRotatesAtTheCycleBoundary() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3");

    String first = resolveIdle(tracker, target, bakedModel, 0.0).clipName();
    assertEquals(first, resolveIdle(tracker, target, bakedModel, 19.0).clipName());

    EasyModelAnimationVariantFrame rotated = resolveIdle(tracker, target, bakedModel, 20.0);
    assertNotEquals(first, rotated.clipName());
    assertEquals(0.0f, rotated.clipTicks(), 0.0001f);
    assertEquals(5.0f, resolveIdle(tracker, target, bakedModel, 25.0).clipTicks(), 0.0001f);
  }

  @Test
  @DisplayName("A clip that does not loop never rotates")
  void nonLoopingClipNeverRotates() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(false, 1.0f, "idle", "idle_2", "idle_3");

    String first = resolveIdle(tracker, target, bakedModel, 0.0).clipName();
    assertEquals(first, resolveIdle(tracker, target, bakedModel, 200.0).clipName());
    assertEquals(first, resolveIdle(tracker, target, bakedModel, 4000.0).clipName());
  }

  @Test
  void zeroLengthClipNeverRotates() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 0.0f, "idle", "idle_2");

    String first = resolveIdle(tracker, target, bakedModel, 0.0).clipName();
    assertEquals(first, resolveIdle(tracker, target, bakedModel, 500.0).clipName());
  }

  @Test
  @DisplayName("Movement clips rotate on every completed step cycle and keep the swing clock")
  void movementRotatesOnStepCycles() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "walk", "walk_2", "walk_3");

    EasyModelAnimationVariantFrame first = resolve(tracker, target, bakedModel, "walk", 0.4f, 0.0f);
    assertEquals(-1.0f, first.clipTicks());
    assertEquals(
        first.clipName(), resolve(tracker, target, bakedModel, "walk", 0.9f, 0.0f).clipName());
    assertNotEquals(
        first.clipName(), resolve(tracker, target, bakedModel, "walk", 1.2f, 0.0f).clipName());
  }

  @Test
  @DisplayName("Attack clips rotate once per swing, not while the swing is running")
  void attackRotatesOnTheRisingEdge() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(false, 1.0f, "attack", "attack_2", "attack_3");

    resolve(tracker, target, bakedModel, "attack", 0.0f, 0.0f);
    String swing = resolve(tracker, target, bakedModel, "attack", 0.0f, 0.1f).clipName();
    assertEquals(swing, resolve(tracker, target, bakedModel, "attack", 0.0f, 0.9f).clipName());

    resolve(tracker, target, bakedModel, "attack", 0.0f, 0.0f);
    assertNotEquals(swing, resolve(tracker, target, bakedModel, "attack", 0.0f, 0.1f).clipName());
  }

  @Test
  @DisplayName("Sequential mode walks through every variant in order")
  void sequentialModeCyclesDeterministically() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3");

    Set<String> seen = new LinkedHashSet<>();
    for (int cycle = 0; cycle < 4; cycle++) {
      seen.add(
          tracker
              .resolve(
                  target,
                  SEED,
                  bakedModel,
                  ModelAnimationVariantMode.SEQUENTIAL,
                  automaticFrame(),
                  "idle",
                  cycle * 20.0,
                  0.0f,
                  0.0f)
              .clipName());
    }

    assertEquals(Set.of("idle", "idle_2", "idle_3"), seen);
  }

  @Test
  @DisplayName("A random rotation never repeats the clip it just played")
  void randomRotationNeverRepeatsImmediately() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3");

    String previous = resolveIdle(tracker, target, bakedModel, 0.0).clipName();
    for (int cycle = 1; cycle < 40; cycle++) {
      String current = resolveIdle(tracker, target, bakedModel, cycle * 20.0).clipName();
      assertNotEquals(previous, current);
      previous = current;
    }
  }

  @Test
  void sameSeedProducesTheSameSequence() {
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3");
    EasyModelAnimationVariantTracker<Object> first = new EasyModelAnimationVariantTracker<>();
    EasyModelAnimationVariantTracker<Object> second = new EasyModelAnimationVariantTracker<>();
    Object firstTarget = new Object();
    Object secondTarget = new Object();

    for (int cycle = 0; cycle < 8; cycle++) {
      assertEquals(
          resolveIdle(first, firstTarget, bakedModel, cycle * 20.0).clipName(),
          resolveIdle(second, secondTarget, bakedModel, cycle * 20.0).clipName());
    }
  }

  @Test
  @DisplayName("An explicit playback and an explicitly named clip are never rotated")
  void explicitAnimationsAreNotRotated() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3");

    assertSame(
        EasyModelAnimationVariantFrame.NONE,
        tracker.resolve(
            target,
            SEED,
            bakedModel,
            ModelAnimationVariantMode.RANDOM,
            EasyModelAnimationPlaybackFrame.single(EasyModelAnimation.IDLE, 0.0f, null, true),
            "idle",
            0.0,
            0.0f,
            0.0f));
    assertNull(
        tracker
            .resolve(
                target,
                SEED,
                bakedModel,
                ModelAnimationVariantMode.RANDOM,
                EasyModelAnimationPlaybackFrame.single(
                    EasyModelAnimation.named("idle_2"), 0.0f, null, false),
                "idle",
                0.0,
                0.0f,
                0.0f)
            .clipName());
  }

  @Test
  void disabledVariantModeReturnsNoOverride() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();

    assertSame(
        EasyModelAnimationVariantFrame.NONE,
        tracker.resolve(
            new Object(),
            SEED,
            model(true, 1.0f, "idle", "idle_2"),
            ModelAnimationVariantMode.NONE,
            automaticFrame(),
            "idle",
            0.0,
            0.0f,
            0.0f));
  }

  @Test
  @DisplayName("The blend source keeps the variant that was playing when the blend started")
  void previousAnimationIsReadWithoutRotating() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();
    BakedModel bakedModel = model(true, 1.0f, "idle", "idle_2", "idle_3", "sit", "sit_2");

    String idle = resolveIdle(tracker, target, bakedModel, 0.0).clipName();
    EasyModelAnimationVariantFrame blend =
        tracker.resolve(
            target,
            SEED,
            bakedModel,
            ModelAnimationVariantMode.RANDOM,
            new EasyModelAnimationPlaybackFrame(
                EasyModelAnimation.SIT,
                0.0f,
                EasyModelAnimation.IDLE,
                5.0f,
                0.5f,
                null,
                null,
                false),
            "idle",
            0.0,
            0.0f,
            0.0f);

    assertEquals(idle, blend.previousClipName());
    assertTrue(blend.clipName().startsWith("sit"));
  }

  @Test
  void aClipRemovedByAReloadFallsBackToTheFirstVariant() {
    EasyModelAnimationVariantTracker<Object> tracker = new EasyModelAnimationVariantTracker<>();
    Object target = new Object();

    resolveIdle(tracker, target, model(true, 1.0f, "idle", "idle_2", "idle_3"), 0.0);
    assertEquals(
        "idle", resolveIdle(tracker, target, model(true, 1.0f, "idle", "idle_9"), 0.0).clipName());
  }
}

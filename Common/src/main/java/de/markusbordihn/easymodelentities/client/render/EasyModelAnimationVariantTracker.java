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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelAnimationVariantMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;

final class EasyModelAnimationVariantTracker<T> {

  private static final int MAX_CATCH_UP_CYCLES = 64;
  private static final float TICKS_PER_SECOND = 20.0f;

  private final Map<T, Map<String, Variant>> variants =
      Collections.synchronizedMap(new WeakHashMap<>());

  static String baseNameOf(
      EasyModelAnimation animation, Supplier<String> automaticClipNameSupplier) {
    if (animation == null || animation.isNamed()) {
      return null;
    }
    if (animation == EasyModelAnimation.AUTO) {
      String automaticClipName = automaticClipNameSupplier.get();
      return automaticClipName == null || automaticClipName.isEmpty()
          ? null
          : ModelAnimationClips.baseName(automaticClipName);
    }

    return EasyModelBakedModelRenderer.forcedClipName(animation);
  }

  private static long mix(long seed, int baseHash, int rollCounter) {
    long value = seed + 0x9e3779b97f4a7c15L * (baseHash + 31L * rollCounter);
    value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
    value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
    return value ^ (value >>> 31);
  }

  private static void roll(Variant variant, Roll roll) {
    int size = roll.clipNames().size();
    if (roll.mode() == ModelAnimationVariantMode.SEQUENTIAL) {
      variant.index = (variant.index + 1) % size;
    } else {
      variant.rollCounter++;
      int index =
        Math.floorMod(
            mix(roll.seed(), roll.baseName().hashCode(), variant.rollCounter), size);
      variant.index = index == variant.index ? (index + 1) % size : index;
    }
    variant.clipName = roll.clipNames().get(variant.index);
  }

  private static boolean loops(ModelAnimationClip clip, Boolean loopOverride) {
    if (loopOverride != null) {
      return loopOverride;
    }

    return clip != null && clip.loop();
  }

  private static EasyModelAnimationVariantFrame resolveMovement(
      Variant variant, Roll roll, float walkCycles, String previousClipName) {
    if (walkCycles < variant.lastWalkCycles
        || Math.floor(walkCycles) > Math.floor(variant.lastWalkCycles)) {
      roll(variant, roll);
    }
    variant.lastWalkCycles = walkCycles;

    return new EasyModelAnimationVariantFrame(variant.clipName, -1.0f, previousClipName);
  }

  private static EasyModelAnimationVariantFrame resolveAttack(
      Variant variant, Roll roll, float attackAmount, String previousClipName) {
    if (variant.lastAttackAmount <= 0.0f && attackAmount > 0.0f) {
      roll(variant, roll);
    }
    variant.lastAttackAmount = attackAmount;

    return new EasyModelAnimationVariantFrame(variant.clipName, -1.0f, previousClipName);
  }

  private static EasyModelAnimationVariantFrame resolveAge(
      Variant variant,
      Roll roll,
      BakedModel bakedModel,
      Boolean loopOverride,
      double currentTicks,
      String previousClipName) {
    if (currentTicks < variant.startTicks) {
      variant.startTicks = currentTicks;
    }

    ModelAnimationClip clip = bakedModel.animations().get(variant.clipName);
    float length = clip == null ? 0.0f : clip.length();
    if (length > 0.0f && loops(clip, loopOverride)) {
      double cycleTicks = length * TICKS_PER_SECOND;
      int cycles = (int) Math.floor((currentTicks - variant.startTicks) / cycleTicks);
      if (cycles >= MAX_CATCH_UP_CYCLES) {
        variant.startTicks = currentTicks;
        roll(variant, roll);
      } else if (cycles >= 1) {
        variant.startTicks += cycles * cycleTicks;
        roll(variant, roll);
      }
    }

    return new EasyModelAnimationVariantFrame(
        variant.clipName,
        (float) Math.max(0.0, currentTicks - variant.startTicks),
        previousClipName);
  }

  private static String peek(
      Map<String, Variant> targetVariants, BakedModel bakedModel, String previousBaseName) {
    if (previousBaseName == null) {
      return null;
    }

    Variant variant = targetVariants.get(previousBaseName);
    return variant == null
            || variant.clipName == null
            || !bakedModel.animations().containsKey(variant.clipName)
        ? null
        : variant.clipName;
  }

  void clear(T target) {
    this.variants.remove(target);
  }

  void clearAll() {
    this.variants.clear();
  }

  EasyModelAnimationVariantFrame resolve(
      T target,
      long seed,
      BakedModel bakedModel,
      ModelAnimationVariantMode mode,
      EasyModelAnimationPlaybackFrame playbackFrame,
      String automaticClipName,
      double currentTicks,
      float walkCycles,
      float attackAmount) {
    Objects.requireNonNull(automaticClipName, "automaticClipName");
    return this.resolve(
        target,
        seed,
        bakedModel,
        mode,
        playbackFrame,
        () -> automaticClipName,
        currentTicks,
        walkCycles,
        attackAmount);
  }

  EasyModelAnimationVariantFrame resolve(
      T target,
      long seed,
      BakedModel bakedModel,
      ModelAnimationVariantMode mode,
      EasyModelAnimationPlaybackFrame playbackFrame,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      float walkCycles,
      float attackAmount) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(bakedModel, "bakedModel");
    Objects.requireNonNull(playbackFrame, "playbackFrame");
    if (mode == ModelAnimationVariantMode.NONE || playbackFrame.playbackDriven()) {
      return EasyModelAnimationVariantFrame.NONE;
    }

    String baseName = baseNameOf(playbackFrame.animation(), automaticClipNameSupplier);
    String previousBaseName =
        baseNameOf(playbackFrame.previousAnimation(), automaticClipNameSupplier);
    if (baseName == null && previousBaseName == null) {
      return EasyModelAnimationVariantFrame.NONE;
    }

    synchronized (this.variants) {
      Map<String, Variant> targetVariants =
          this.variants.computeIfAbsent(target, ignored -> new HashMap<>());
      String previousClipName = peek(targetVariants, bakedModel, previousBaseName);
      List<String> clipNames =
          baseName == null ? List.of() : bakedModel.animationVariants().variantsOf(baseName);
      if (clipNames.size() < 2) {
        return new EasyModelAnimationVariantFrame(null, -1.0f, previousClipName);
      }

      Variant variant = targetVariants.computeIfAbsent(baseName, ignored -> new Variant());
      if (variant.clipName == null || !clipNames.contains(variant.clipName)) {
        variant.index = 0;
        variant.clipName = clipNames.get(0);
        variant.startTicks = currentTicks;
      }

      Roll roll = new Roll(seed, baseName, clipNames, mode);
      return switch (ModelAnimationClips.familyOf(baseName)) {
        case MOVEMENT -> resolveMovement(variant, roll, walkCycles, previousClipName);
        case ATTACK -> resolveAttack(variant, roll, attackAmount, previousClipName);
        case AGE ->
            resolveAge(
                variant,
                roll,
                bakedModel,
                playbackFrame.loopOverride(),
                currentTicks,
                previousClipName);
      };
    }
  }

  private record Roll(
      long seed, String baseName, List<String> clipNames, ModelAnimationVariantMode mode) {}

  private static final class Variant {

    private String clipName;
    private int index;
    private double startTicks;
    private float lastWalkCycles;
    private float lastAttackAmount;
    private int rollCounter;
  }
}

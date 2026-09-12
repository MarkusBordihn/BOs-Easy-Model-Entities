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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlayback;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationPlaybackMode;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSequence;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationStep;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationSwitchTiming;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelAnimationTransition;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.util.Mth;

final class EasyModelAnimationPlaybackTracker<T> {

  private static final double LOOP_START_NORMALIZATION_TICKS = 100_000.0;

  private final Map<T, Playback> playbacks = Collections.synchronizedMap(new WeakHashMap<>());

  private static void applyPending(
      Playback playback,
      EasyModelAnimation fallback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    Request request = playback.pending.poll();
    if (request == null) {
      return;
    }

    playback.queued.clear();
    EasyModelAnimation requestedAnimation =
        request.release ? fallback : request.restart ? playback.animation : request.animation;
    if (requestedAnimation == null) {
      playback.pending.clear();
      return;
    }

    if (request.restart
        || request.transition.timing() == EasyModelAnimationSwitchTiming.IMMEDIATE) {
      switchAnimation(
          playback,
          requestedAnimation,
          currentTicks,
          request.restart ? 0.0f : request.transition.blendDurationTicks(),
          -1.0f,
          !request.release);
      initializeCompletion(playback, request, currentTicks);
      stageFollowUps(playback);
      refreshQueueBoundary(playback, automaticClipNameSupplier, clips);
      return;
    }

    double boundary = nextBoundary(playback, automaticClipNameSupplier, clips);
    if (boundary <= playback.lastTicks) {
      switchAnimation(
          playback,
          requestedAnimation,
          currentTicks,
          request.transition.blendDurationTicks(),
          -1.0f,
          !request.release);
      initializeCompletion(playback, request, currentTicks);
      stageFollowUps(playback);
      refreshQueueBoundary(playback, automaticClipNameSupplier, clips);
      return;
    }

    playback.queued.addLast(request);
    playback.queueBoundary = boundary;
    stageFollowUps(playback);
  }

  private static void stageFollowUps(Playback playback) {
    playback.queued.addAll(playback.pending);
    playback.pending.clear();
  }

  private static void refreshQueueBoundary(
      Playback playback,
      Supplier<String> automaticClipNameSupplier,
      Map<String, ModelAnimationClip> clips) {
    if (playback.queued.isEmpty()) {
      return;
    }

    playback.queueBoundary =
        playback.playback == null
            ? nextBoundary(playback, automaticClipNameSupplier, clips)
            : Double.POSITIVE_INFINITY;
  }

  private static void applyQueued(
      Playback playback,
      EasyModelAnimation fallback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    Request queued = playback.queued.peek();
    if (queued == null || currentTicks < playback.queueBoundary) {
      return;
    }

    float previousTicks = (float) Math.max(0.0, playback.queueBoundary - playback.startTicks);
    playback.queued.poll();
    switchAnimation(
        playback,
        queued.release ? fallback : queued.animation,
        playback.queueBoundary,
        queued.transition.blendDurationTicks(),
        previousTicks,
        !queued.release);
    initializeCompletion(playback, queued, playback.queueBoundary);
    refreshQueueBoundary(playback, automaticClipNameSupplier, clips);
  }

  private static double nextBoundary(
      Playback playback,
      Supplier<String> automaticClipNameSupplier,
      Map<String, ModelAnimationClip> clips) {
    ModelAnimationClip clip = clips.get(clipName(playback.animation, automaticClipNameSupplier));
    float animationLength = animationLength(playback, clip);
    if (animationLength <= 0.0f) {
      return playback.lastTicks;
    }

    double durationTicks = animationLength * 20.0;
    if (!isLooping(playback, clip)) {
      return Math.max(playback.startTicks + durationTicks, playback.lastTicks);
    }

    double elapsedTicks = Math.max(0.0, playback.lastTicks - playback.startTicks);
    return playback.startTicks + (Math.floor(elapsedTicks / durationTicks) + 1.0) * durationTicks;
  }

  private static String clipName(
      EasyModelAnimation animation, Supplier<String> automaticClipNameSupplier) {
    if (animation.isNamed()) {
      return animation.name();
    }

    if (animation == EasyModelAnimation.AUTO) {
      return Objects.requireNonNull(automaticClipNameSupplier.get(), "automaticClipName");
    }

    return animation == EasyModelAnimation.RUN ? EasyModelAnimation.WALK.name() : animation.name();
  }

  private static void switchAnimation(
      Playback playback,
      EasyModelAnimation animation,
      double switchTicks,
      float blendDurationTicks,
      float frozenPreviousTicks,
      boolean overridden) {
    boolean blend = blendDurationTicks > 0.0f && !animation.equals(playback.animation);
    if (blend) {
      playback.previousLoopOverride = currentLoopOverride(playback);
      playback.previousAnimation = playback.animation;
      playback.previousStartTicks = playback.startTicks;
      playback.frozenPreviousTicks = frozenPreviousTicks;
      playback.blendStartTicks = switchTicks;
      playback.blendDurationTicks = blendDurationTicks;
    } else {
      playback.clearBlend();
    }
    playback.animation = animation;
    playback.startTicks = switchTicks;
    playback.overridden = overridden;
  }

  private static void initializeCompletion(Playback playback, Request request, double switchTicks) {
    if (request.release || request.playback == null) {
      playback.clearCompletion();
      return;
    }

    playback.playback = request.playback;
    playback.transition = request.transition;
    playback.playStartTicks = switchTicks;
    playback.remainingPlays = effectivePlays(request.playback);
    playback.autoReverted = false;
  }

  private static int effectivePlays(EasyModelAnimationPlayback playback) {
    return switch (playback.mode()) {
      case ONCE -> 1;
      case LOOP -> 0;
      case REPEAT -> Math.max(1, playback.repeatCount());
    };
  }

  private static float animationLength(Playback playback, ModelAnimationClip clip) {
    return clip == null ? EasyModelFallbackAnimation.length(playback.animation) : clip.length();
  }

  private static boolean isLooping(Playback playback, ModelAnimationClip clip) {
    Boolean loopOverride = currentLoopOverride(playback);
    if (loopOverride != null) {
      return loopOverride;
    }

    return clip == null ? EasyModelFallbackAnimation.loops(playback.animation) : clip.loop();
  }

  private static Boolean currentLoopOverride(Playback playback) {
    return playback.overridden && playback.playback != null && !playback.autoReverted
        ? playback.playback.mode() == EasyModelAnimationPlaybackMode.LOOP
        : null;
  }

  private static void checkCompletion(
      Playback playback,
      EasyModelAnimation fallback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    if (!playback.overridden || playback.autoReverted || playback.playback == null) {
      return;
    }

    float durationTicks = playback.playback.durationTicks();
    if (durationTicks > 0.0f && currentTicks >= playback.playStartTicks + durationTicks) {
      completePlayback(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      return;
    }
    if (playback.playback.mode() == EasyModelAnimationPlaybackMode.LOOP) {
      return;
    }

    ModelAnimationClip clip = clips.get(clipName(playback.animation, automaticClipNameSupplier));
    float animationLength = animationLength(playback, clip);
    if (animationLength <= 0.0f) {
      completePlayback(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      return;
    }

    double clipLengthTicks = animationLength * 20.0;
    double elapsedTicks = Math.max(0.0, currentTicks - playback.startTicks);
    int completions = (int) Math.floor(elapsedTicks / clipLengthTicks);
    if (completions < 1) {
      return;
    }
    if (completions >= playback.remainingPlays) {
      completePlayback(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      return;
    }

    playback.startTicks += completions * clipLengthTicks;
    playback.remainingPlays -= completions;
  }

  private static void completePlayback(
      Playback playback,
      EasyModelAnimation fallback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    Request queued = playback.queued.poll();
    if (queued != null) {
      float previousTicks = (float) Math.max(0.0, currentTicks - playback.startTicks);
      switchAnimation(
          playback,
          queued.release ? fallback : queued.animation,
          currentTicks,
          queued.transition.blendDurationTicks(),
          previousTicks,
          !queued.release);
      initializeCompletion(playback, queued, currentTicks);
      refreshQueueBoundary(playback, automaticClipNameSupplier, clips);
      return;
    }

    float blendDurationTicks =
        playback.transition == null ? 0.0f : playback.transition.blendDurationTicks();
    float previousTicks = (float) Math.max(0.0, currentTicks - playback.startTicks);
    switchAnimation(playback, fallback, currentTicks, blendDurationTicks, previousTicks, false);
    playback.clearCompletion();
    playback.autoReverted = true;
  }

  private static void normalizeLoopStart(
      Playback playback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    double elapsedTicks = currentTicks - playback.startTicks;
    if (elapsedTicks < LOOP_START_NORMALIZATION_TICKS
        || (playback.playback != null
            && playback.playback.mode() != EasyModelAnimationPlaybackMode.LOOP)) {
      return;
    }

    ModelAnimationClip clip = clips.get(clipName(playback.animation, automaticClipNameSupplier));
    float animationLength = animationLength(playback, clip);
    if (animationLength <= 0.0f || !isLooping(playback, clip)) {
      return;
    }

    double durationTicks = animationLength * 20.0;
    playback.startTicks += Math.floor(elapsedTicks / durationTicks) * durationTicks;
  }

  private static EasyModelAnimationPlaybackFrame frame(Playback playback, double currentTicks) {
    float animationTicks = (float) Math.max(0.0, currentTicks - playback.startTicks);
    Boolean loopOverride = currentLoopOverride(playback);
    if (playback.previousAnimation == null) {
      return EasyModelAnimationPlaybackFrame.single(
          playback.animation, animationTicks, loopOverride, playback.overridden);
    }

    float blendProgress =
        Mth.clamp(
            (float) ((currentTicks - playback.blendStartTicks) / playback.blendDurationTicks),
            0.0f,
            1.0f);
    if (blendProgress >= 1.0f) {
      playback.clearBlend();
      return EasyModelAnimationPlaybackFrame.single(
          playback.animation, animationTicks, loopOverride, playback.overridden);
    }

    float previousTicks =
        playback.frozenPreviousTicks >= 0.0f
            ? playback.frozenPreviousTicks
            : (float) Math.max(0.0, currentTicks - playback.previousStartTicks);

    return new EasyModelAnimationPlaybackFrame(
        playback.animation,
        animationTicks,
        playback.previousAnimation,
        previousTicks,
        blendProgress,
        loopOverride,
        playback.previousLoopOverride,
        true);
  }

  void play(T target, EasyModelAnimation animation, EasyModelAnimationTransition transition) {
    play(target, animation, EasyModelAnimationPlayback.DEFAULT, transition);
  }

  void play(
      T target,
      EasyModelAnimation animation,
      EasyModelAnimationPlayback animationPlayback,
      EasyModelAnimationTransition transition) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(animationPlayback, "animationPlayback");
    Objects.requireNonNull(transition, "transition");
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.computeIfAbsent(target, ignored -> new Playback());
      playback.stage(new Request(animation, animationPlayback, transition, false, false));
    }
  }

  void playSequence(T target, EasyModelAnimationSequence sequence) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(sequence, "sequence");
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.computeIfAbsent(target, ignored -> new Playback());
      playback.pending.clear();
      for (EasyModelAnimationStep step : sequence.steps()) {
        playback.pending.addLast(
            new Request(step.animation(), step.playback(), step.transition(), false, false));
      }
      if (!sequence.fallback().equals(EasyModelAnimation.AUTO)) {
        playback.pending.addLast(
            new Request(
                sequence.fallback(),
                EasyModelAnimationPlayback.DEFAULT.withMode(EasyModelAnimationPlaybackMode.LOOP),
                EasyModelAnimationTransition.AFTER_CURRENT,
                false,
                false));
      }
    }
  }

  void stop(T target, EasyModelAnimationTransition transition) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(transition, "transition");
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.get(target);
      if (playback == null || !playback.hasPlaybackState()) {
        return;
      }

      playback.stage(new Request(EasyModelAnimation.AUTO, null, transition, false, true));
    }
  }

  void restart(T target) {
    Objects.requireNonNull(target, "target");
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.get(target);
      if (playback == null || playback.animation == null) {
        return;
      }

      playback.stage(
          new Request(
              null,
              playback.playback,
              playback.transition == null
                  ? EasyModelAnimationTransition.IMMEDIATE
                  : playback.transition,
              true,
              false));
    }
  }

  boolean hasPlayback(T target) {
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.get(target);
      return playback != null && playback.hasPlaybackState();
    }
  }

  void clear(T target) {
    this.playbacks.remove(target);
  }

  EasyModelAnimationPlaybackFrame resolve(
      T target,
      EasyModelAnimation fallback,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    return resolve(target, fallback, () -> "", currentTicks, clips);
  }

  EasyModelAnimationPlaybackFrame resolve(
      T target,
      EasyModelAnimation fallback,
      String automaticClipName,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    Objects.requireNonNull(automaticClipName, "automaticClipName");
    return resolve(target, fallback, () -> automaticClipName, currentTicks, clips);
  }

  EasyModelAnimationPlaybackFrame resolve(
      T target,
      EasyModelAnimation fallback,
      Supplier<String> automaticClipNameSupplier,
      double currentTicks,
      Map<String, ModelAnimationClip> clips) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(fallback, "fallback");
    Objects.requireNonNull(automaticClipNameSupplier, "automaticClipNameSupplier");
    Objects.requireNonNull(clips, "clips");
    if (!Double.isFinite(currentTicks)) {
      throw new IllegalArgumentException("currentTicks must be finite.");
    }
    synchronized (this.playbacks) {
      Playback playback = this.playbacks.computeIfAbsent(target, ignored -> new Playback());
      if (playback.animation == null || currentTicks < playback.lastTicks) {
        playback.reset(fallback, currentTicks);
      } else if (!playback.overridden && !playback.animation.equals(fallback)) {
        playback.reset(fallback, currentTicks);
      }
      applyPending(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      applyQueued(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      checkCompletion(playback, fallback, automaticClipNameSupplier, currentTicks, clips);
      normalizeLoopStart(playback, automaticClipNameSupplier, currentTicks, clips);
      playback.lastTicks = currentTicks;
      return frame(playback, currentTicks);
    }
  }

  private record Request(
      EasyModelAnimation animation,
      EasyModelAnimationPlayback playback,
      EasyModelAnimationTransition transition,
      boolean restart,
      boolean release) {}

  private static final class Playback {

    private final Deque<Request> pending = new ArrayDeque<>();
    private final Deque<Request> queued = new ArrayDeque<>();
    private EasyModelAnimation animation;
    private double startTicks;
    private double lastTicks;
    private boolean overridden;
    private double queueBoundary;
    private EasyModelAnimation previousAnimation;
    private double previousStartTicks;
    private float frozenPreviousTicks = -1.0f;
    private double blendStartTicks;
    private float blendDurationTicks;
    private EasyModelAnimationPlayback playback;
    private EasyModelAnimationTransition transition;
    private double playStartTicks;
    private int remainingPlays;
    private boolean autoReverted;
    private Boolean previousLoopOverride;

    private void stage(Request request) {
      this.pending.clear();
      this.pending.addLast(request);
    }

    private void reset(EasyModelAnimation animation, double currentTicks) {
      this.animation = animation;
      this.startTicks = currentTicks;
      this.lastTicks = currentTicks;
      this.overridden = false;
      this.queued.clear();
      clearCompletion();
      clearBlend();
    }

    private boolean hasPlaybackState() {
      return this.overridden
          || !this.pending.isEmpty()
          || !this.queued.isEmpty()
          || this.previousAnimation != null;
    }

    private void clearCompletion() {
      this.playback = null;
      this.transition = null;
      this.playStartTicks = 0.0;
      this.remainingPlays = 0;
      this.autoReverted = false;
    }

    private void clearBlend() {
      this.previousAnimation = null;
      this.previousStartTicks = 0.0;
      this.frozenPreviousTicks = -1.0f;
      this.blendStartTicks = 0.0;
      this.blendDurationTicks = 0.0f;
      this.previousLoopOverride = null;
    }
  }
}

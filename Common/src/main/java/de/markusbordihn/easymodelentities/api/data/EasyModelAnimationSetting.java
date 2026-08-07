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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public record EasyModelAnimationSetting(EasyModelAnimation animation, EasyModelAnimationLoop loop) {

  public static final EasyModelAnimationSetting AUTO =
      new EasyModelAnimationSetting(EasyModelAnimation.AUTO, EasyModelAnimationLoop.CLIP);

  public static final Codec<EasyModelAnimationSetting> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING
                          .xmap(
                              serializedName ->
                                  EasyModelAnimation.parse(serializedName)
                                      .orElse(EasyModelAnimation.AUTO),
                              EasyModelAnimation::serializedName)
                          .fieldOf("animation")
                          .forGetter(EasyModelAnimationSetting::animation),
                      EasyModelAnimationLoop.CODEC
                          .optionalFieldOf("loop", EasyModelAnimationLoop.CLIP)
                          .forGetter(EasyModelAnimationSetting::loop))
                  .apply(instance, EasyModelAnimationSetting::new));

  public EasyModelAnimationSetting {
    Objects.requireNonNull(animation, "animation");
    Objects.requireNonNull(loop, "loop");
  }

  public static EasyModelAnimationSetting of(EasyModelAnimation animation) {
    return new EasyModelAnimationSetting(animation, EasyModelAnimationLoop.CLIP);
  }

  public static Optional<EasyModelAnimationSetting> fromTag(Tag tag) {
    return tag == null ? Optional.empty() : CODEC.parse(NbtOps.INSTANCE, tag).result();
  }

  public Tag createTag() {
    return CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseGet(CompoundTag::new);
  }

  public EasyModelAnimationSetting withAnimation(EasyModelAnimation animation) {
    return new EasyModelAnimationSetting(animation, this.loop);
  }

  public EasyModelAnimationSetting withLoop(EasyModelAnimationLoop loop) {
    return new EasyModelAnimationSetting(this.animation, loop);
  }

  public Boolean loopOverride() {
    return this.animation == EasyModelAnimation.AUTO ? null : this.loop.override();
  }
}

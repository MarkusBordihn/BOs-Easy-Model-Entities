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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public record EasyModelTextureSlot(
    Optional<ResourceLocation> texture, EasyModelTextureBlend blend) {

  public static final Codec<EasyModelTextureSlot> OBJECT_CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      ResourceLocation.CODEC
                          .optionalFieldOf("texture")
                          .forGetter(EasyModelTextureSlot::texture),
                      EasyModelTextureBlend.CODEC
                          .optionalFieldOf("blend", EasyModelTextureBlend.DEFAULT)
                          .forGetter(EasyModelTextureSlot::blend))
                  .apply(instance, EasyModelTextureSlot::new));

  public static final Codec<EasyModelTextureSlot> CODEC =
      Codec.either(ResourceLocation.CODEC, OBJECT_CODEC)
          .xmap(
              either -> either.map(EasyModelTextureSlot::new, Function.identity()),
              slot ->
                  slot.isSimple() ? Either.left(slot.texture().orElseThrow()) : Either.right(slot));

  public EasyModelTextureSlot {
    Objects.requireNonNull(texture, "texture");
    Objects.requireNonNull(blend, "blend");
  }

  public EasyModelTextureSlot(ResourceLocation texture) {
    this(Optional.of(texture), EasyModelTextureBlend.DEFAULT);
  }

  public static EasyModelTextureSlot of(ResourceLocation texture, EasyModelTextureBlend blend) {
    return new EasyModelTextureSlot(Optional.of(texture), blend);
  }

  public static EasyModelTextureSlot of(EasyModelTextureBlend blend) {
    return new EasyModelTextureSlot(Optional.empty(), blend);
  }

  public boolean isSimple() {
    return this.texture.isPresent() && this.blend == EasyModelTextureBlend.DEFAULT;
  }

  public boolean isEmpty() {
    return this.texture.isEmpty() && this.blend == EasyModelTextureBlend.DEFAULT;
  }

  public EasyModelTextureSlot withTexture(ResourceLocation texture) {
    return new EasyModelTextureSlot(Optional.of(texture), this.blend);
  }

  public EasyModelTextureSlot withBlend(EasyModelTextureBlend blend) {
    return new EasyModelTextureSlot(this.texture, blend);
  }
}

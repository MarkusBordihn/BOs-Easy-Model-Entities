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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.decoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class ModelDecoderRegistry implements EasyModelDecoderRegistry {

  private final Map<String, EasyModelDecoder> decodersByFormat;

  public ModelDecoderRegistry(Map<String, EasyModelDecoder> decodersByFormat) {
    this.decodersByFormat =
        Map.copyOf(Objects.requireNonNull(decodersByFormat, "decodersByFormat"));
  }

  public static ModelDecoderRegistry createDefault() {
    Map<String, EasyModelDecoder> decoders = new LinkedHashMap<>();
    decoders.put(BlockbenchBbModelDecoder.FORMAT, new BlockbenchBbModelDecoder());
    return new ModelDecoderRegistry(decoders);
  }

  @Override
  public boolean hasDecoder(String format) {
    return this.decodersByFormat.containsKey(format);
  }

  @Override
  public Set<String> getDecoderFormats() {
    return this.decodersByFormat.keySet();
  }

  @Override
  public Optional<EasyModelDecoder> findDecoder(ResourceLocation modelId, Resource resource) {
    for (EasyModelDecoder decoder : this.decodersByFormat.values()) {
      if (decoder.supports(modelId, resource)) {
        return Optional.of(decoder);
      }
    }

    return Optional.empty();
  }
}

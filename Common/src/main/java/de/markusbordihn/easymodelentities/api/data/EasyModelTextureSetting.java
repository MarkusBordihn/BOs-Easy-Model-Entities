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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record EasyModelTextureSetting(Map<String, EasyModelTextureSlot> slots) {

  public static final String DEFAULT_SLOT = "default";
  public static final char INDEX_PREFIX = '#';
  public static final int MAX_SLOTS = 16;
  public static final int MAX_SLOT_NAME_LENGTH = 64;

  public static final EasyModelTextureSetting EMPTY = new EasyModelTextureSetting(Map.of());

  public static final Codec<EasyModelTextureSetting> CODEC =
      Codec.unboundedMap(Codec.STRING, EasyModelTextureSlot.CODEC)
          .xmap(EasyModelTextureSetting::new, EasyModelTextureSetting::slots);

  public EasyModelTextureSetting {
    slots = normalize(slots);
  }

  public static EasyModelTextureSetting of(String slot, ResourceLocation texture) {
    return EMPTY.withSlot(slot, texture);
  }

  public static Optional<EasyModelTextureSetting> fromTag(Tag tag) {
    return tag == null ? Optional.empty() : CODEC.parse(NbtOps.INSTANCE, tag).result();
  }

  public static Optional<String> normalizeSlot(String slot) {
    if (slot == null) {
      return Optional.empty();
    }

    String normalized = slot.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()
        || normalized.length() > MAX_SLOT_NAME_LENGTH
        || !isValidSlotName(normalized)) {
      return Optional.empty();
    }
    return Optional.of(normalized);
  }

  private static boolean isValidSlotName(String slot) {
    if (slot.charAt(0) == INDEX_PREFIX) {
      return slot.length() > 1 && isDigits(slot, 1);
    }

    for (int i = 0; i < slot.length(); i++) {
      char character = slot.charAt(i);
      if (!isSlotNameCharacter(character)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSlotNameCharacter(char character) {
    return (character >= 'a' && character <= 'z')
        || (character >= '0' && character <= '9')
        || character == '_'
        || character == '-';
  }

  private static boolean isDigits(String value, int fromIndex) {
    for (int i = fromIndex; i < value.length(); i++) {
      char character = value.charAt(i);
      if (character < '0' || character > '9') {
        return false;
      }
    }
    return true;
  }

  private static Map<String, EasyModelTextureSlot> normalize(
      Map<String, EasyModelTextureSlot> slots) {
    if (slots == null || slots.isEmpty()) {
      return Map.of();
    }

    Map<String, EasyModelTextureSlot> sorted = new TreeMap<>();
    for (Map.Entry<String, EasyModelTextureSlot> entry : slots.entrySet()) {
      if (entry.getValue() == null || entry.getValue().isEmpty()) {
        continue;
      }
      normalizeSlot(entry.getKey()).ifPresent(slot -> sorted.put(slot, entry.getValue()));
    }
    if (sorted.size() <= MAX_SLOTS) {
      return Map.copyOf(sorted);
    }

    Map<String, EasyModelTextureSlot> capped = new LinkedHashMap<>();
    for (Map.Entry<String, EasyModelTextureSlot> entry : sorted.entrySet()) {
      if (capped.size() >= MAX_SLOTS) {
        break;
      }
      capped.put(entry.getKey(), entry.getValue());
    }
    return Map.copyOf(capped);
  }

  public CompoundTag createTag() {
    Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(null);
    return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
  }

  public boolean isEmpty() {
    return this.slots.isEmpty();
  }

  public Optional<EasyModelTextureSlot> slot(String slot) {
    return normalizeSlot(slot).map(this.slots::get);
  }

  public Optional<ResourceLocation> texture(String slot) {
    return this.slot(slot).flatMap(EasyModelTextureSlot::texture);
  }

  public EasyModelTextureBlend blend(String slot) {
    return this.slot(slot).map(EasyModelTextureSlot::blend).orElse(EasyModelTextureBlend.DEFAULT);
  }

  public EasyModelTextureSetting withSlot(String slot, ResourceLocation texture) {
    if (texture == null) {
      return this;
    }

    return this.withSlot(slot, currentSlot -> currentSlot.withTexture(texture));
  }

  public EasyModelTextureSetting withSlot(
      String slot, ResourceLocation texture, EasyModelTextureBlend blend) {
    if (texture == null || blend == null) {
      return this;
    }

    return this.withSlot(slot, currentSlot -> EasyModelTextureSlot.of(texture, blend));
  }

  public EasyModelTextureSetting withBlend(String slot, EasyModelTextureBlend blend) {
    if (blend == null) {
      return this;
    }

    return this.withSlot(slot, currentSlot -> currentSlot.withBlend(blend));
  }

  private EasyModelTextureSetting withSlot(
      String slot, UnaryOperator<EasyModelTextureSlot> update) {
    Optional<String> normalized = normalizeSlot(slot);
    if (normalized.isEmpty()) {
      return this;
    }

    EasyModelTextureSlot currentSlot =
        this.slots.getOrDefault(
            normalized.get(),
            new EasyModelTextureSlot(Optional.empty(), EasyModelTextureBlend.DEFAULT));
    EasyModelTextureSlot updatedSlot = update.apply(currentSlot);
    if (updatedSlot.equals(currentSlot)) {
      return this;
    }

    Map<String, EasyModelTextureSlot> updated = new LinkedHashMap<>(this.slots);
    if (updatedSlot.isEmpty()) {
      updated.remove(normalized.get());
    } else {
      updated.put(normalized.get(), updatedSlot);
    }
    return new EasyModelTextureSetting(updated);
  }

  public EasyModelTextureSetting withoutSlot(String slot) {
    Optional<String> normalized = normalizeSlot(slot);
    if (normalized.isEmpty() || !this.slots.containsKey(normalized.get())) {
      return this;
    }

    Map<String, EasyModelTextureSlot> updated = new LinkedHashMap<>(this.slots);
    updated.remove(normalized.get());
    return new EasyModelTextureSetting(updated);
  }

  public EasyModelTextureSetting withoutSlots() {
    return EMPTY;
  }
}

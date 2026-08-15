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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSlot;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import de.markusbordihn.easymodelentities.model.bake.ModelTextureResolver;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EasyModelTextureOverrides {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final int MAX_CACHE_ENTRIES = 512;
  private static final Map<OverrideKey, EasyModelResolvedTextures> RESOLVED =
      Collections.synchronizedMap(
          new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(
                Map.Entry<OverrideKey, EasyModelResolvedTextures> eldest) {
              return size() > MAX_CACHE_ENTRIES;
            }
          });
  private static final Map<Identifier, Optional<ModelRenderProfileValidationIssue>>
      VALIDATED_TEXTURES =
          Collections.synchronizedMap(
              new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(
                    Map.Entry<Identifier, Optional<ModelRenderProfileValidationIssue>>
                        eldest) {
                  return size() > MAX_CACHE_ENTRIES;
                }
              });

  static {
    EasyModelReloadDispatcher.addRenderProfileReloadListener(EasyModelTextureOverrides::clear);
  }

  private EasyModelTextureOverrides() {}

  public static EasyModelResolvedTextures resolve(
      EasyModelRenderState renderState, EasyModelTextureSetting textureSetting) {
    if (!isOverridable(renderState, textureSetting)) {
      return EasyModelResolvedTextures.NONE;
    }

    return resolve(renderState, textureSetting, Minecraft.getInstance().getResourceManager());
  }

  static EasyModelResolvedTextures resolve(
      EasyModelRenderState renderState,
      EasyModelTextureSetting textureSetting,
      ResourceManager resourceManager) {
    if (!isOverridable(renderState, textureSetting) || resourceManager == null) {
      return EasyModelResolvedTextures.NONE;
    }

    OverrideKey key = keyOf(renderState, textureSetting);
    EasyModelResolvedTextures cached = RESOLVED.get(key);
    if (cached != null) {
      return cached;
    }

    EasyModelResolvedTextures resolved =
        EasyModelResolvedTextures.of(overrides(renderState, textureSetting, resourceManager));
    RESOLVED.put(key, resolved);
    return resolved;
  }

  private static OverrideKey keyOf(
      EasyModelRenderState renderState, EasyModelTextureSetting textureSetting) {
    return new OverrideKey(
        renderState.bakedModel().modelId(), renderState.texture(), textureSetting);
  }

  public static void clear() {
    RESOLVED.clear();
    VALIDATED_TEXTURES.clear();
  }

  public static Optional<Identifier> baseTexture(
      EasyModelRenderState renderState, String slot) {
    if (renderState == null) {
      return Optional.empty();
    }

    return EasyModelTextureSetting.normalizeSlot(slot)
        .map(normalizedSlot -> textureIndex(renderState, normalizedSlot))
        .filter(textureIndex -> textureIndex >= 0)
        .map(
            textureIndex ->
                textureIndex == 0
                    ? renderState.texture()
                    : renderState.textures().get(textureIndex));
  }

  private static boolean isOverridable(
      EasyModelRenderState renderState, EasyModelTextureSetting textureSetting) {
    return renderState != null
        && textureSetting != null
        && !textureSetting.isEmpty()
        && !renderState.fallbackModel()
        && !renderState.fallbackTexture();
  }

  private static Map<Integer, EasyModelTextureSlot> overrides(
      EasyModelRenderState renderState,
      EasyModelTextureSetting textureSetting,
      ResourceManager resourceManager) {
    Map<Integer, EasyModelTextureSlot> overrides = new LinkedHashMap<>();
    for (Map.Entry<String, EasyModelTextureSlot> entry : textureSetting.slots().entrySet()) {
      int textureIndex = textureIndex(renderState, entry.getKey());
      if (textureIndex < 0) {
        log.debug(
            "Ignoring texture override for unknown slot {} of model {}.",
            entry.getKey(),
            renderState.bakedModel().modelId());
        continue;
      }

      EasyModelTextureSlot textureSlot = entry.getValue();
      Optional<Identifier> texture = textureSlot.texture();
      if (texture.isPresent()) {
        Optional<ModelRenderProfileValidationIssue> issue =
            validateTexture(texture.get(), resourceManager);
        if (issue.isPresent()) {
          log.debug(
              "Ignoring texture override {} for slot {}: {}",
              texture.get(),
              entry.getKey(),
              issue.get().message());
          textureSlot = EasyModelTextureSlot.of(textureSlot.blend());
        }
      }
      if (textureSlot.isEmpty()) {
        continue;
      }
      overrides.put(textureIndex, textureSlot);
    }

    return overrides;
  }

  private static Optional<ModelRenderProfileValidationIssue> validateTexture(
      Identifier texture, ResourceManager resourceManager) {
    return VALIDATED_TEXTURES.computeIfAbsent(
        texture,
        validatedTexture ->
            ModelTextureResolver.validateTextureDimensions(validatedTexture, resourceManager));
  }

  private static int textureIndex(EasyModelRenderState renderState, String slot) {
    if (EasyModelTextureSetting.DEFAULT_SLOT.equals(slot)) {
      return 0;
    }

    Integer namedIndex = renderState.bakedModel().textureNames().get(slot);
    if (namedIndex != null) {
      return usableIndex(renderState, namedIndex);
    }

    return usableIndex(renderState, escapedIndex(slot));
  }

  private static int escapedIndex(String slot) {
    String index =
        slot.length() > 1 && slot.charAt(0) == EasyModelTextureSetting.INDEX_PREFIX
            ? slot.substring(1)
            : slot;
    try {
      return Integer.parseInt(index);
    } catch (NumberFormatException exception) {
      return -1;
    }
  }

  private static int usableIndex(EasyModelRenderState renderState, int textureIndex) {
    if (textureIndex == 0 || renderState.textures().containsKey(textureIndex)) {
      return textureIndex;
    }

    return -1;
  }

  private record OverrideKey(
      Identifier modelId, Identifier baseTexture, EasyModelTextureSetting setting) {}
}

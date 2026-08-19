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

import de.markusbordihn.easymodelentities.event.EasyModelReloadDispatcher;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class EasyModelTextureVariants {

  public static final String VARIANT_DIRECTORY = "variants";
  private static final String PNG_SUFFIX = ".png";
  private static final int MAX_VARIANTS = 256;
  private static final int MAX_CACHE_ENTRIES = 256;
  private static final Map<Identifier, List<Identifier>> VARIANTS =
      Collections.synchronizedMap(
          new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Identifier, List<Identifier>> eldest) {
              return size() > MAX_CACHE_ENTRIES;
            }
          });

  static {
    EasyModelReloadDispatcher.addRenderProfileReloadListener(EasyModelTextureVariants::clear);
  }

  private EasyModelTextureVariants() {}

  public static List<Identifier> variants(Identifier baseTexture, ResourceManager resourceManager) {
    if (baseTexture == null || resourceManager == null) {
      return List.of();
    }

    List<Identifier> cached = VARIANTS.get(baseTexture);
    if (cached != null) {
      return cached;
    }

    List<Identifier> variants = discover(baseTexture, resourceManager);
    VARIANTS.put(baseTexture, variants);
    return variants;
  }

  public static void clear() {
    VARIANTS.clear();
  }

  private static List<Identifier> discover(
      Identifier baseTexture, ResourceManager resourceManager) {
    String path = baseTexture.getPath();
    int lastSeparator = path.lastIndexOf('/');
    if (lastSeparator <= 0) {
      return List.of();
    }

    String directory = path.substring(0, lastSeparator);
    TreeSet<Identifier> variants = new TreeSet<>();
    for (Identifier candidate :
        resourceManager
            .listResources(
                directory, resourceLocation -> resourceLocation.getPath().endsWith(PNG_SUFFIX))
            .keySet()) {
      if (!candidate.getNamespace().equals(baseTexture.getNamespace())
          || !isVariantPath(directory, candidate.getPath())) {
        continue;
      }
      variants.add(candidate);
      if (variants.size() >= MAX_VARIANTS) {
        break;
      }
    }

    return List.copyOf(variants);
  }

  private static boolean isVariantPath(String directory, String path) {
    if (path.length() <= directory.length() + 1) {
      return false;
    }

    String relativePath = path.substring(directory.length() + 1);
    int separator = relativePath.indexOf('/');
    if (separator < 0) {
      return true;
    }

    return relativePath.startsWith(VARIANT_DIRECTORY + "/")
        && relativePath.indexOf('/', VARIANT_DIRECTORY.length() + 1) < 0;
  }
}

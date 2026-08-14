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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelTextureVariantsTest {

  private static final String DIRECTORY = "textures/entity/echo";
  private static final ResourceLocation BASE =
      new ResourceLocation("example", DIRECTORY + "/screen_default.png");
  private static final ResourceLocation SIBLING =
      new ResourceLocation("example", DIRECTORY + "/screen_sad.png");
  private static final ResourceLocation IN_VARIANT_DIRECTORY =
      new ResourceLocation("example", DIRECTORY + "/variants/happy.png");
  private static final ResourceLocation NESTED_TOO_DEEP =
      new ResourceLocation("example", DIRECTORY + "/variants/legacy/happy.png");
  private static final ResourceLocation OTHER_NAMESPACE =
      new ResourceLocation("other", DIRECTORY + "/screen_sad.png");

  private static ResourceManager resourceManager(ResourceLocation... resources) {
    ResourceManager resourceManager = mock(ResourceManager.class);
    Map<ResourceLocation, Resource> listing = new LinkedHashMap<>();
    for (ResourceLocation resourceLocation : resources) {
      listing.put(resourceLocation, new Resource(mock(PackResources.class), () -> null));
    }
    when(resourceManager.listResources(eq(DIRECTORY), any())).thenReturn(listing);
    return resourceManager;
  }

  @BeforeEach
  void resetCache() {
    EasyModelTextureVariants.clear();
  }

  @Test
  @DisplayName("Both layouts are found: siblings of the texture and a variants sub folder")
  void findsSiblingsAndVariantDirectory() {
    ResourceManager resourceManager =
        resourceManager(BASE, SIBLING, IN_VARIANT_DIRECTORY, NESTED_TOO_DEEP, OTHER_NAMESPACE);

    assertEquals(
        List.of(BASE, SIBLING, IN_VARIANT_DIRECTORY),
        EasyModelTextureVariants.variants(BASE, resourceManager));
  }

  @Test
  void unknownBaseTextureHasNoVariants() {
    assertTrue(EasyModelTextureVariants.variants(BASE, resourceManager()).isEmpty());
    assertTrue(
        EasyModelTextureVariants.variants(
                new ResourceLocation("example", "screen.png"), resourceManager())
            .isEmpty());
    assertTrue(EasyModelTextureVariants.variants(BASE, null).isEmpty());
  }

  @Test
  void clearedCacheListsAgain() {
    assertTrue(EasyModelTextureVariants.variants(BASE, resourceManager()).isEmpty());
    assertTrue(EasyModelTextureVariants.variants(BASE, resourceManager(BASE)).isEmpty());

    EasyModelTextureVariants.clear();

    assertEquals(List.of(BASE), EasyModelTextureVariants.variants(BASE, resourceManager(BASE)));
  }
}

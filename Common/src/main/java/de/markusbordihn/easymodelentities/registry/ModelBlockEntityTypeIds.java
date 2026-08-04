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

package de.markusbordihn.easymodelentities.registry;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ModelBlockEntityTypeIds {

  public static final ResourceLocation STATIC_BLOCK_ENTITY =
      ModelResourcePaths.modResourceLocation("static_block_entity");
  public static final ResourceLocation TICKING_BLOCK_ENTITY =
      ModelResourcePaths.modResourceLocation("ticking_block_entity");
  public static final ResourceLocation ANIMATED_BLOCK_ENTITY =
      ModelResourcePaths.modResourceLocation("animated_block_entity");
  public static final ResourceLocation ANIMATED_RANDOMLY_BLOCK_ENTITY =
      ModelResourcePaths.modResourceLocation("animated_randomly_block_entity");
  public static final Set<ResourceLocation> SUPPORTED_HOST_BLOCK_ENTITY_TYPES =
      Set.of(
          STATIC_BLOCK_ENTITY,
          TICKING_BLOCK_ENTITY,
          ANIMATED_BLOCK_ENTITY,
          ANIMATED_RANDOMLY_BLOCK_ENTITY);

  private ModelBlockEntityTypeIds() {}

  public static boolean isSupportedHostBlockEntityType(ResourceLocation blockEntityTypeId) {
    return SUPPORTED_HOST_BLOCK_ENTITY_TYPES.contains(blockEntityTypeId);
  }
}

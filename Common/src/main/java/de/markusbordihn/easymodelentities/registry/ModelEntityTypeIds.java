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

import de.markusbordihn.easymodelentities.Constants;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ModelEntityTypeIds {

  public static final ResourceLocation GROUND_ENTITY =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "ground_entity");
  public static final ResourceLocation STATIC_ENTITY =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "static_entity");
  public static final ResourceLocation AQUATIC_ENTITY =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "aquatic_entity");
  public static final ResourceLocation AMPHIBIOUS_ENTITY =
      ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "amphibious_entity");
  public static final Set<ResourceLocation> SUPPORTED_HOST_ENTITY_TYPES =
      Set.of(GROUND_ENTITY, STATIC_ENTITY, AQUATIC_ENTITY, AMPHIBIOUS_ENTITY);

  private ModelEntityTypeIds() {}

  public static boolean isSupportedHostEntityType(ResourceLocation entityTypeId) {
    return SUPPORTED_HOST_ENTITY_TYPES.contains(entityTypeId);
  }
}

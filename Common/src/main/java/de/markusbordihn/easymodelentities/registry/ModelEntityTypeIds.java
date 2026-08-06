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
import net.minecraft.resources.Identifier;

public final class ModelEntityTypeIds {

  public static final Identifier GROUND_ENTITY = ModelResourcePaths.modIdentifier("ground_entity");
  public static final Identifier STATIC_ENTITY = ModelResourcePaths.modIdentifier("static_entity");
  public static final Identifier AQUATIC_ENTITY =
      ModelResourcePaths.modIdentifier("aquatic_entity");
  public static final Identifier AMPHIBIOUS_ENTITY =
      ModelResourcePaths.modIdentifier("amphibious_entity");
  public static final Set<Identifier> SUPPORTED_HOST_ENTITY_TYPES =
      Set.of(GROUND_ENTITY, STATIC_ENTITY, AQUATIC_ENTITY, AMPHIBIOUS_ENTITY);

  private ModelEntityTypeIds() {}

  public static boolean isSupportedHostEntityType(Identifier entityTypeId) {
    return SUPPORTED_HOST_ENTITY_TYPES.contains(entityTypeId);
  }
}

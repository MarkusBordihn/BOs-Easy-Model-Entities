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

package de.markusbordihn.easymodelentities.data;

import de.markusbordihn.easymodelentities.api.data.EasyModelBodyType;
import de.markusbordihn.easymodelentities.api.data.EasyModelProfileType;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBounds;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.ModelBounds;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import java.util.Objects;

public final class EasyModelApiMapper {

  private EasyModelApiMapper() {}

  public static EasyModelProfileType profileType(ModelType modelType) {
    Objects.requireNonNull(modelType, "modelType");
    return switch (modelType) {
      case ENTITY -> EasyModelProfileType.ENTITY;
      case BLOCK_ENTITY -> EasyModelProfileType.BLOCK_ENTITY;
    };
  }

  public static EasyModelBodyType bodyType(ModelBodyType bodyType) {
    Objects.requireNonNull(bodyType, "bodyType");
    return switch (bodyType) {
      case STATIC -> EasyModelBodyType.STATIC;
      case BIPED -> EasyModelBodyType.BIPED;
      case QUADRUPED -> EasyModelBodyType.QUADRUPED;
      case AQUATIC -> EasyModelBodyType.AQUATIC;
      case AMPHIBIOUS -> EasyModelBodyType.AMPHIBIOUS;
      case WINGED -> EasyModelBodyType.WINGED;
      case WINGED_HUMANOID -> EasyModelBodyType.WINGED_HUMANOID;
      case ARTHROPOD -> EasyModelBodyType.ARTHROPOD;
      case CUBOID -> EasyModelBodyType.CUBOID;
      case FLOATING -> EasyModelBodyType.FLOATING;
    };
  }

  public static EasyModelVec3f vector(Vec3f vector) {
    Objects.requireNonNull(vector, "vector");
    return new EasyModelVec3f(vector.x(), vector.y(), vector.z());
  }

  public static EasyModelBounds bounds(ModelBounds bounds) {
    Objects.requireNonNull(bounds, "bounds");
    return new EasyModelBounds(vector(bounds.min()), vector(bounds.max()));
  }
}

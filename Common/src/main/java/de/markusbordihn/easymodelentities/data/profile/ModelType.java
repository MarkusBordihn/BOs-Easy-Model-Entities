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

package de.markusbordihn.easymodelentities.data.profile;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public enum ModelType {
  ENTITY,
  BLOCK_ENTITY;

  private final String serializedName = this.name().toLowerCase(Locale.ROOT);

  public static ModelType fromProfileId(Identifier profileId) {
    if (profileId != null && profileId.getPath().startsWith(BLOCK_ENTITY.serializedName + "/")) {
      return BLOCK_ENTITY;
    }
    return ENTITY;
  }

  public static boolean hasModelTypeDirectory(Identifier profileId) {
    if (profileId == null) {
      return false;
    }
    String path = profileId.getPath();
    for (ModelType modelType : values()) {
      if (path.startsWith(modelType.serializedName + "/")) {
        return true;
      }
    }

    return false;
  }

  public static Optional<ModelType> bySerializedName(String serializedName) {
    if (serializedName == null) {
      return Optional.empty();
    }

    String normalizedName = serializedName.toLowerCase(Locale.ROOT);
    for (ModelType modelType : values()) {
      if (modelType.getSerializedName().equals(normalizedName)) {
        return Optional.of(modelType);
      }
    }

    return Optional.empty();
  }

  public String getSerializedName() {
    return this.serializedName;
  }
}

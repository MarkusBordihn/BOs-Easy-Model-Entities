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

public enum ModelMovementType {
  GROUND,
  STATIC;

  public static Optional<ModelMovementType> bySerializedName(String serializedName) {
    if (serializedName == null) {
      return Optional.empty();
    }

    String normalizedName = serializedName.toLowerCase(Locale.ROOT);
    for (ModelMovementType movementType : values()) {
      if (movementType.getSerializedName().equals(normalizedName)) {
        return Optional.of(movementType);
      }
    }

    return Optional.empty();
  }

  public String getSerializedName() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public boolean isGround() {
    return this == GROUND;
  }

  public float defaultSpeed() {
    return isGround() ? 0.22f : 0.0f;
  }

  public float defaultStepHeight() {
    return isGround() ? 0.6f : 0.0f;
  }

  public boolean defaultGravity() {
    return isGround();
  }

  public ModelBehaviorMode defaultBehaviorMode() {
    return isGround() ? ModelBehaviorMode.IDLE_ONLY : ModelBehaviorMode.STATIC;
  }
}

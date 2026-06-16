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

package de.markusbordihn.easymodelentities.data.model;

import java.util.Locale;

public enum ModelPartType {
  ROOT,
  BODY,
  HEAD,
  LEFT_ARM,
  RIGHT_ARM,
  LEFT_LEG,
  RIGHT_LEG,
  FRONT_LEFT_LEG,
  FRONT_RIGHT_LEG,
  BACK_LEFT_LEG,
  BACK_RIGHT_LEG,
  MIDDLE_FRONT_LEFT_LEG,
  MIDDLE_FRONT_RIGHT_LEG,
  MIDDLE_BACK_LEFT_LEG,
  MIDDLE_BACK_RIGHT_LEG,
  LEFT_WING,
  RIGHT_WING,
  TAIL,
  TAIL_FIN,
  UNKNOWN;

  private final String tagName = this.name().toLowerCase(Locale.ROOT);

  public static ModelPartType get(String modelPart) {
    if (modelPart == null || modelPart.isEmpty()) {
      return ModelPartType.UNKNOWN;
    }

    try {
      return ModelPartType.valueOf(modelPart.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      for (ModelPartType modelPartTypeEnum : ModelPartType.values()) {
        if (modelPartTypeEnum.tagName.equalsIgnoreCase(modelPart)) {
          return modelPartTypeEnum;
        }
      }

      return ModelPartType.UNKNOWN;
    }
  }

  public String getTagName() {
    return this.tagName;
  }
}

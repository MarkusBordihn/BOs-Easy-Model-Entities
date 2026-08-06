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

package de.markusbordihn.easymodelentities.api.data.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum EasyModelPartType {
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

  private static final Map<String, EasyModelPartType> BY_TAG_NAME = byTagName();

  private final String tagName = this.name().toLowerCase(Locale.ROOT);

  public static EasyModelPartType fromPartName(String partName) {
    if (partName == null || partName.isEmpty()) {
      return UNKNOWN;
    }

    return BY_TAG_NAME.getOrDefault(partName.toLowerCase(Locale.ROOT), UNKNOWN);
  }

  private static Map<String, EasyModelPartType> byTagName() {
    Map<String, EasyModelPartType> byTagName = new HashMap<>();
    for (EasyModelPartType partType : values()) {
      byTagName.put(partType.tagName, partType);
    }

    return Map.copyOf(byTagName);
  }

  public String getTagName() {
    return this.tagName;
  }
}

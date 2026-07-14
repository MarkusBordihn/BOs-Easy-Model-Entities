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

import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModelPart;
import java.util.List;
import java.util.Set;

public final class ModelAnimationClips {

  public static final String IDLE = "idle";
  public static final String WALK = "walk";
  public static final String SWIM = "swim";
  public static final String FLY = "fly";
  public static final String HURT = "hurt";
  public static final String DEATH = "death";
  public static final String ATTACK = "attack";

  public static final List<String> STANDARD = List.of(IDLE, WALK, SWIM, FLY, HURT, DEATH, ATTACK);
  public static final Set<String> STANDARD_NAMES = Set.copyOf(STANDARD);

  private ModelAnimationClips() {}

  public static String normalize(String name) {
    return name == null ? "" : DecodedModelPart.normalizeName(name);
  }
}

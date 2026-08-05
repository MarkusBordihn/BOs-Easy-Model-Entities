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

package de.markusbordihn.easymodelentities.api.data;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class EasyModelAnimation {

  public static final EasyModelAnimation AUTO = standard("auto");
  public static final EasyModelAnimation IDLE = standard("idle");
  public static final EasyModelAnimation WALK = standard("walk");
  public static final EasyModelAnimation RUN = standard("run");
  public static final EasyModelAnimation HURT = standard("hurt");
  public static final EasyModelAnimation DEATH = standard("death");
  public static final EasyModelAnimation SWIM = standard("swim");
  public static final EasyModelAnimation FLY = standard("fly");
  public static final EasyModelAnimation ATTACK = standard("attack");
  public static final EasyModelAnimation SIT = standard("sit");

  private static final String NAMED_PREFIX = "named:";
  private static final List<EasyModelAnimation> STANDARD_STATES =
      List.of(AUTO, IDLE, WALK, RUN, HURT, DEATH, SWIM, FLY, ATTACK, SIT);

  private final String name;
  private final boolean named;

  private EasyModelAnimation(String name, boolean named) {
    this.name = name;
    this.named = named;
  }

  private static EasyModelAnimation standard(String name) {
    return new EasyModelAnimation(name, false);
  }

  public static EasyModelAnimation named(String name) {
    String normalizedName = normalizeName(name);
    if (normalizedName.isEmpty()) {
      throw new IllegalArgumentException("Animation name must not be blank.");
    }

    return new EasyModelAnimation(normalizedName, true);
  }

  public static Optional<EasyModelAnimation> parse(String serializedName) {
    if (serializedName == null) {
      return Optional.empty();
    }
    String normalizedName = serializedName.trim().toLowerCase(Locale.ROOT);
    if (normalizedName.startsWith(NAMED_PREFIX)) {
      String namedAnimation = normalizedName.substring(NAMED_PREFIX.length());
      return namedAnimation.isBlank() ? Optional.empty() : Optional.of(named(namedAnimation));
    }

    return STANDARD_STATES.stream()
        .filter(animation -> animation.name.equals(normalizedName))
        .findFirst();
  }

  public static List<EasyModelAnimation> standardStates() {
    return STANDARD_STATES;
  }

  private static String normalizeName(String name) {
    return Objects.requireNonNull(name, "name").trim().toLowerCase(Locale.ROOT);
  }

  public String name() {
    return this.name;
  }

  public boolean isNamed() {
    return this.named;
  }

  public String serializedName() {
    return this.named ? NAMED_PREFIX + this.name : this.name;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof EasyModelAnimation animation)) {
      return false;
    }

    return this.named == animation.named && this.name.equals(animation.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.named);
  }

  @Override
  public String toString() {
    return serializedName();
  }
}

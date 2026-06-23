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

package de.markusbordihn.easymodelentities.schema;

import java.util.Optional;

public record SemanticSchemaVersion(int major, int minor, int patch)
    implements Comparable<SemanticSchemaVersion> {

  public static Optional<SemanticSchemaVersion> parse(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String[] parts = value.trim().split("\\.");
    if (parts.length != 3) {
      return Optional.empty();
    }
    try {
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);
      int patch = Integer.parseInt(parts[2]);
      if (major < 0 || minor < 0 || patch < 0) {
        return Optional.empty();
      }
      return Optional.of(new SemanticSchemaVersion(major, minor, patch));
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  @Override
  public int compareTo(SemanticSchemaVersion other) {
    int majorComparison = Integer.compare(this.major, other.major);
    if (majorComparison != 0) {
      return majorComparison;
    }
    int minorComparison = Integer.compare(this.minor, other.minor);
    if (minorComparison != 0) {
      return minorComparison;
    }

    return Integer.compare(this.patch, other.patch);
  }
}

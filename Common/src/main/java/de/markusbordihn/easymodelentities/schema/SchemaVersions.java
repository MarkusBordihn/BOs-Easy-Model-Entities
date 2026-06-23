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

public final class SchemaVersions {

  private SchemaVersions() {}

  public static Classification classify(String declared, String current) {
    if (declared != null && declared.equals(current)) {
      return Classification.CURRENT;
    }
    Optional<SemanticSchemaVersion> declaredVersion = SemanticSchemaVersion.parse(declared);
    Optional<SemanticSchemaVersion> currentVersion = SemanticSchemaVersion.parse(current);
    if (declaredVersion.isEmpty() || currentVersion.isEmpty()) {
      return Classification.UNPARSEABLE;
    }
    int comparison = declaredVersion.get().compareTo(currentVersion.get());
    if (comparison == 0) {
      return Classification.CURRENT;
    }

    return comparison < 0 ? Classification.OLDER : Classification.NEWER;
  }

  public enum Classification {
    CURRENT,
    OLDER,
    NEWER,
    UNPARSEABLE
  }
}

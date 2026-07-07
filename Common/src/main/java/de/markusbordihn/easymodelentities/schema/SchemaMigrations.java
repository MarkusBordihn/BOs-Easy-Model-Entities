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

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SchemaMigrations {

  public static final SchemaMigrations DEFAULT =
      new SchemaMigrations(List.of(new ClientLinkRemovalMigration()));

  private final List<SchemaMigration> migrations;

  public SchemaMigrations(List<SchemaMigration> migrations) {
    this.migrations = List.copyOf(Objects.requireNonNull(migrations, "migrations"));
  }

  public Optional<JsonObject> migrate(JsonObject root, String fromVersion, String currentVersion) {
    Objects.requireNonNull(root, "root");
    String current = fromVersion;
    JsonObject working = root;
    boolean progressed = true;
    while (!Objects.equals(current, currentVersion) && progressed) {
      progressed = false;
      for (SchemaMigration migration : this.migrations) {
        if (migration.from().equals(current)) {
          working = migration.apply(working);
          current = migration.to();
          progressed = true;
          break;
        }
      }
    }

    return Objects.equals(current, currentVersion) ? Optional.of(working) : Optional.empty();
  }

  private static final class ClientLinkRemovalMigration implements SchemaMigration {

    @Override
    public String from() {
      return "0.1.0";
    }

    @Override
    public String to() {
      return "0.2.0";
    }

    @Override
    public JsonObject apply(JsonObject input) {
      input.remove("client");
      input.addProperty("schema_version", to());
      return input;
    }
  }
}

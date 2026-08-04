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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class SchemaMigrationV0_1ToV0_2 implements SchemaMigration {

  public static final String FROM_VERSION = "0.1.0";
  public static final String TO_VERSION = "0.2.0";
  public static final String MIGRATED_RENDER_PROFILE_FIELD = "_legacy_render_profile";
  private static final String CLIENT_FIELD = "client";
  private static final String RENDER_PROFILE_FIELD = "render_profile";

  @Override
  public String from() {
    return FROM_VERSION;
  }

  @Override
  public String to() {
    return TO_VERSION;
  }

  @Override
  public JsonObject apply(JsonObject input) {
    JsonElement client = input.remove(CLIENT_FIELD);
    if (client != null && client.isJsonObject()) {
      JsonElement renderProfile = client.getAsJsonObject().get(RENDER_PROFILE_FIELD);
      if (renderProfile != null && !renderProfile.isJsonNull()) {
        input.add(MIGRATED_RENDER_PROFILE_FIELD, renderProfile.deepCopy());
      }
    }
    input.addProperty("schema_version", to());
    return input;
  }
}

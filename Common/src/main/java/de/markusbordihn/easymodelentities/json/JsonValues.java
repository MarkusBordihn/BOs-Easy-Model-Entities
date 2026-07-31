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

package de.markusbordihn.easymodelentities.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;

public final class JsonValues {

  private JsonValues() {}

  public static void reportUnknownFields(
      JsonElement value,
      String fieldPrefix,
      Set<String> knownFields,
      BiConsumer<String, String> issueReporter) {
    if (value == null || !value.isJsonObject()) {
      return;
    }

    for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
      if (knownFields.contains(entry.getKey())) {
        continue;
      }
      String field = fieldPrefix + entry.getKey();
      issueReporter.accept(field, "Unknown field " + field + " is ignored, check the spelling.");
    }
  }

  public static boolean hasField(JsonObject jsonObject, String field) {
    return jsonObject != null && jsonObject.has(field) && !jsonObject.get(field).isJsonNull();
  }

  public static <T> T optionalObject(
      JsonElement value,
      String field,
      Class<T> objectClass,
      Gson gson,
      BiConsumer<String, String> issueReporter) {
    if (value == null || value.isJsonNull()) {
      return null;
    }
    if (!value.isJsonObject()) {
      issueReporter.accept(field, "Field " + field + " must be an object.");
      return null;
    }

    return gson.fromJson(value, objectClass);
  }

  public static Float parseFloat(
      JsonElement value, String field, BiConsumer<String, String> issueReporter) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      issueReporter.accept(field, "Field " + field + " must be a number.");
      return null;
    }
    float floatValue = value.getAsFloat();
    if (!Float.isFinite(floatValue)) {
      issueReporter.accept(field, "Field " + field + " must be finite.");
      return null;
    }

    return floatValue;
  }

  public static float optionalFloat(
      JsonElement value,
      float defaultValue,
      String field,
      BiConsumer<String, String> issueReporter) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    Float floatValue = parseFloat(value, field, issueReporter);
    return floatValue == null ? defaultValue : floatValue;
  }

  public static boolean optionalBoolean(
      JsonElement value,
      boolean defaultValue,
      String field,
      BiConsumer<String, String> issueReporter) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
      issueReporter.accept(field, "Field " + field + " must be a boolean.");
      return defaultValue;
    }

    return value.getAsBoolean();
  }

  public static ResourceLocation parseResourceLocation(
      String rawValue, String field, BiConsumer<String, String> issueReporter) {
    ResourceLocation resourceLocation = ResourceLocation.tryParse(rawValue);
    if (resourceLocation == null) {
      issueReporter.accept(field, "Invalid ResourceLocation " + rawValue + ".");
    }

    return resourceLocation;
  }

  public static String requiredString(
      JsonElement value, String field, BiConsumer<String, String> issueReporter) {
    if (value == null || value.isJsonNull()) {
      issueReporter.accept(field, "Missing required field " + field + ".");
      return null;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      issueReporter.accept(field, "Field " + field + " must be a string.");
      return null;
    }

    return value.getAsString();
  }

  public static String optionalString(
      JsonElement value,
      String defaultValue,
      String field,
      BiConsumer<String, String> issueReporter) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      issueReporter.accept(field, "Field " + field + " must be a string.");
      return defaultValue;
    }

    return value.getAsString();
  }
}

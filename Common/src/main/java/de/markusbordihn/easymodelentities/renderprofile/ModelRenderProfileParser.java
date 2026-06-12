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

package de.markusbordihn.easymodelentities.renderprofile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.profile.ModelPackPair;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public final class ModelRenderProfileParser {

  private static final Gson GSON = new Gson();
  private static final String EMPTY_VALUE = "";
  private static final String JSON_FIELD = "json";
  private static final String SCHEMA_VERSION_FIELD = "schema_version";
  private static final String ID_FIELD = "id";
  private static final String PACK_PAIR_FIELD = "pack_pair";
  private static final String PAIR_ID_FIELD = "pair_id";
  private static final String ASSET_FINGERPRINT_FIELD = "asset_fingerprint";
  private static final String BODY_TYPE_FIELD = "body_type";
  private static final String MODEL_FIELD = "model";
  private static final String TEXTURE_FIELD = "texture";
  private static final String RENDERING_FIELD = "rendering";
  private static final String SCALE_FIELD = "scale";
  private static final String SHADOW_RADIUS_FIELD = "shadow_radius";
  private static final String VISIBLE_BOUNDS_WIDTH_FIELD = "visible_bounds_width";
  private static final String VISIBLE_BOUNDS_HEIGHT_FIELD = "visible_bounds_height";
  private static final String VISIBLE_BOUNDS_OFFSET_FIELD = "visible_bounds_offset";
  private static final String ANIMATION_FIELD = "animation";
  private static final String MODE_FIELD = "mode";
  private static final String IDLE_FIELD = "idle";
  private static final String WALK_FIELD = "walk";
  private static final String RUN_FIELD = "run";
  private static final String HURT_FIELD = "hurt";
  private static final String DEATH_FIELD = "death";
  private static final String SWING_SPEED_FIELD = "swing_speed";
  private static final String WALK_SPEED_MULTIPLIER_FIELD = "walk_speed_multiplier";
  private static final String PACK_PAIR_PAIR_ID_FIELD = PACK_PAIR_FIELD + "." + PAIR_ID_FIELD;
  private static final String PACK_PAIR_ASSET_FINGERPRINT_FIELD =
      PACK_PAIR_FIELD + "." + ASSET_FINGERPRINT_FIELD;
  private static final String RENDERING_SCALE_FIELD = RENDERING_FIELD + "." + SCALE_FIELD;
  private static final String RENDERING_SHADOW_RADIUS_FIELD =
      RENDERING_FIELD + "." + SHADOW_RADIUS_FIELD;
  private static final String RENDERING_VISIBLE_BOUNDS_WIDTH_FIELD =
      RENDERING_FIELD + "." + VISIBLE_BOUNDS_WIDTH_FIELD;
  private static final String RENDERING_VISIBLE_BOUNDS_HEIGHT_FIELD =
      RENDERING_FIELD + "." + VISIBLE_BOUNDS_HEIGHT_FIELD;
  private static final String RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD =
      RENDERING_FIELD + "." + VISIBLE_BOUNDS_OFFSET_FIELD;
  private static final String ANIMATION_MODE_FIELD = ANIMATION_FIELD + "." + MODE_FIELD;
  private static final String ANIMATION_IDLE_FIELD = ANIMATION_FIELD + "." + IDLE_FIELD;
  private static final String ANIMATION_WALK_FIELD = ANIMATION_FIELD + "." + WALK_FIELD;
  private static final String ANIMATION_RUN_FIELD = ANIMATION_FIELD + "." + RUN_FIELD;
  private static final String ANIMATION_HURT_FIELD = ANIMATION_FIELD + "." + HURT_FIELD;
  private static final String ANIMATION_DEATH_FIELD = ANIMATION_FIELD + "." + DEATH_FIELD;
  private static final String ANIMATION_SWING_SPEED_FIELD =
      ANIMATION_FIELD + "." + SWING_SPEED_FIELD;
  private static final String ANIMATION_WALK_SPEED_MULTIPLIER_FIELD =
      ANIMATION_FIELD + "." + WALK_SPEED_MULTIPLIER_FIELD;
  private static final float DEFAULT_SCALE = 1.0f;
  private static final float DEFAULT_SHADOW_RADIUS = 0.3f;
  private static final float DEFAULT_VISIBLE_BOUNDS_WIDTH = 1.0f;
  private static final float DEFAULT_VISIBLE_BOUNDS_HEIGHT = 1.0f;
  private static final float DEFAULT_SWING_SPEED = 1.0f;
  private static final float DEFAULT_WALK_SPEED_MULTIPLIER = 1.0f;

  private ModelRenderProfileParser() {}

  public static EasyModelRenderProfile parse(ResourceLocation expectedId, Reader reader) {
    Objects.requireNonNull(expectedId, "expectedId");
    Objects.requireNonNull(reader, "reader");

    JsonElement jsonElement;
    try {
      jsonElement = JsonParser.parseReader(reader);
    } catch (JsonParseException exception) {
      return invalidFallback(
          expectedId,
          ModelRenderProfileStatus.INVALID_JSON,
          JSON_FIELD,
          "Malformed render profile JSON: " + exception.getMessage());
    }

    if (!jsonElement.isJsonObject()) {
      return invalidFallback(
          expectedId,
          ModelRenderProfileStatus.INVALID_JSON,
          JSON_FIELD,
          "Render profile JSON must be an object.");
    }

    return parseObject(expectedId, jsonElement.getAsJsonObject());
  }

  static EasyModelRenderProfile invalidFallback(
      ResourceLocation expectedId, ModelRenderProfileStatus status, String field, String message) {
    List<ModelRenderProfileValidationIssue> issues =
        List.of(new ModelRenderProfileValidationIssue(status, field, message));
    return new EasyModelRenderProfile(
        expectedId,
        EMPTY_VALUE,
        new ModelPackPair(EMPTY_VALUE, EMPTY_VALUE),
        ModelBodyType.STATIC,
        ModelResourcePaths.defaultModelId(expectedId),
        ModelResourcePaths.defaultTextureId(expectedId),
        defaultRenderSettings(),
        defaultAnimationSettings(),
        status,
        issues);
  }

  private static EasyModelRenderProfile parseObject(
      ResourceLocation expectedId, JsonObject jsonObject) {
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    RawRenderProfile rawProfile = GSON.fromJson(jsonObject, RawRenderProfile.class);

    String schemaVersion = requiredString(rawProfile.schemaVersion, SCHEMA_VERSION_FIELD, issues);
    if (schemaVersion != null && !Constants.SCHEMA_VERSION.equals(schemaVersion)) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_SCHEMA_VERSION,
          SCHEMA_VERSION_FIELD,
          "Unsupported schema_version " + schemaVersion + ".");
    }

    ResourceLocation renderProfileId =
        parseRequiredResourceLocation(rawProfile.id, ID_FIELD, issues);
    if (renderProfileId != null && !expectedId.equals(renderProfileId)) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
          ID_FIELD,
          "Render profile id " + renderProfileId + " does not match path id " + expectedId + ".");
    }

    RawPackPair rawPackPair =
        optionalObject(rawProfile.packPair, PACK_PAIR_FIELD, RawPackPair.class, issues);
    ModelPackPair packPair =
        new ModelPackPair(
            optionalString(
                rawPackPair == null ? null : rawPackPair.pairId,
                EMPTY_VALUE,
                PACK_PAIR_PAIR_ID_FIELD,
                issues),
            optionalString(
                rawPackPair == null ? null : rawPackPair.assetFingerprint,
                EMPTY_VALUE,
                PACK_PAIR_ASSET_FINGERPRINT_FIELD,
                issues));
    ModelBodyType bodyType = parseBodyType(rawProfile.bodyType, issues);
    ResourceLocation model = parseRequiredResourceLocation(rawProfile.model, MODEL_FIELD, issues);
    ResourceLocation texture =
        parseRequiredResourceLocation(rawProfile.texture, TEXTURE_FIELD, issues);
    ModelRenderSettings renderSettings = parseRenderSettings(rawProfile.rendering, issues);
    ModelAnimationSettings animationSettings = parseAnimationSettings(rawProfile.animation, issues);

    return new EasyModelRenderProfile(
        expectedId,
        schemaVersion == null ? EMPTY_VALUE : schemaVersion,
        packPair,
        bodyType == null ? ModelBodyType.STATIC : bodyType,
        model == null ? ModelResourcePaths.defaultModelId(expectedId) : model,
        texture == null ? ModelResourcePaths.defaultTextureId(expectedId) : texture,
        renderSettings,
        animationSettings,
        ModelRenderProfileStatus.statusForIssues(issues),
        issues);
  }

  private static ModelRenderSettings parseRenderSettings(
      JsonElement renderingElement, List<ModelRenderProfileValidationIssue> issues) {
    RawRendering rawRendering =
        optionalObject(renderingElement, RENDERING_FIELD, RawRendering.class, issues);
    float visibleBoundsHeight =
        optionalFloat(
            rawRendering == null ? null : rawRendering.visibleBoundsHeight,
            DEFAULT_VISIBLE_BOUNDS_HEIGHT,
            RENDERING_VISIBLE_BOUNDS_HEIGHT_FIELD,
            issues);
    float[] visibleBoundsOffset =
        optionalFloatArray(
            rawRendering == null ? null : rawRendering.visibleBoundsOffset,
            new float[] {0.0f, visibleBoundsHeight / 2.0f, 0.0f},
            RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD,
            issues);

    return new ModelRenderSettings(
        optionalFloat(
            rawRendering == null ? null : rawRendering.scale,
            DEFAULT_SCALE,
            RENDERING_SCALE_FIELD,
            issues),
        optionalFloat(
            rawRendering == null ? null : rawRendering.shadowRadius,
            DEFAULT_SHADOW_RADIUS,
            RENDERING_SHADOW_RADIUS_FIELD,
            issues),
        optionalFloat(
            rawRendering == null ? null : rawRendering.visibleBoundsWidth,
            DEFAULT_VISIBLE_BOUNDS_WIDTH,
            RENDERING_VISIBLE_BOUNDS_WIDTH_FIELD,
            issues),
        visibleBoundsHeight,
        visibleBoundsOffset[0],
        visibleBoundsOffset[1],
        visibleBoundsOffset[2]);
  }

  private static ModelAnimationSettings parseAnimationSettings(
      JsonElement animationElement, List<ModelRenderProfileValidationIssue> issues) {
    RawAnimation rawAnimation =
        optionalObject(animationElement, ANIMATION_FIELD, RawAnimation.class, issues);
    ModelAnimationMode mode = parseAnimationMode(rawAnimation, issues);

    return new ModelAnimationSettings(
        mode,
        optionalString(
            rawAnimation == null ? null : rawAnimation.idle,
            IDLE_FIELD,
            ANIMATION_IDLE_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.walk,
            WALK_FIELD,
            ANIMATION_WALK_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.run, RUN_FIELD, ANIMATION_RUN_FIELD, issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.hurt,
            HURT_FIELD,
            ANIMATION_HURT_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.death,
            DEATH_FIELD,
            ANIMATION_DEATH_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.swingSpeed,
            DEFAULT_SWING_SPEED,
            ANIMATION_SWING_SPEED_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.walkSpeedMultiplier,
            DEFAULT_WALK_SPEED_MULTIPLIER,
            ANIMATION_WALK_SPEED_MULTIPLIER_FIELD,
            issues));
  }

  private static ModelRenderSettings defaultRenderSettings() {
    return new ModelRenderSettings(
        DEFAULT_SCALE,
        DEFAULT_SHADOW_RADIUS,
        DEFAULT_VISIBLE_BOUNDS_WIDTH,
        DEFAULT_VISIBLE_BOUNDS_HEIGHT,
        0.0f,
        DEFAULT_VISIBLE_BOUNDS_HEIGHT / 2.0f,
        0.0f);
  }

  private static ModelAnimationSettings defaultAnimationSettings() {
    return new ModelAnimationSettings(
        ModelAnimationMode.AUTOMATIC,
        IDLE_FIELD,
        WALK_FIELD,
        RUN_FIELD,
        HURT_FIELD,
        DEATH_FIELD,
        DEFAULT_SWING_SPEED,
        DEFAULT_WALK_SPEED_MULTIPLIER);
  }

  private static ModelBodyType parseBodyType(
      JsonElement value, List<ModelRenderProfileValidationIssue> issues) {
    String bodyTypeName = requiredString(value, BODY_TYPE_FIELD, issues);
    if (bodyTypeName == null) {
      return null;
    }
    String normalizedBodyType = bodyTypeName.toLowerCase(Locale.ROOT);
    for (ModelBodyType bodyType : ModelBodyType.values()) {
      if (bodyType.getSerializedName().equals(normalizedBodyType)) {
        return bodyType;
      }
    }

    addIssue(
        issues,
        ModelRenderProfileStatus.INVALID_BODY_TYPE,
        BODY_TYPE_FIELD,
        "Unsupported body type " + bodyTypeName + ".");
    return null;
  }

  private static ModelAnimationMode parseAnimationMode(
      RawAnimation rawAnimation, List<ModelRenderProfileValidationIssue> issues) {
    String modeName =
        optionalString(
            rawAnimation == null ? null : rawAnimation.mode,
            ModelAnimationMode.AUTOMATIC.getSerializedName(),
            ANIMATION_MODE_FIELD,
            issues);
    return ModelAnimationMode.bySerializedName(modeName)
        .orElseGet(
            () -> {
              addIssue(
                  issues,
                  ModelRenderProfileStatus.INVALID_ANIMATION_MODE,
                  ANIMATION_MODE_FIELD,
                  "Unsupported animation mode " + modeName + ".");
              return ModelAnimationMode.AUTOMATIC;
            });
  }

  private static <T> T optionalObject(
      JsonElement value,
      String field,
      Class<T> objectClass,
      List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return null;
    }
    if (!value.isJsonObject()) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          field,
          "Field " + field + " must be an object.");
      return null;
    }

    return GSON.fromJson(value, objectClass);
  }

  private static String requiredString(
      JsonElement value, String field, List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      addIssue(
          issues, statusForRequiredField(field), field, "Missing required field " + field + ".");
      return null;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      addIssue(
          issues, statusForRequiredField(field), field, "Field " + field + " must be a string.");
      return null;
    }

    return value.getAsString();
  }

  private static String optionalString(
      JsonElement value,
      String defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          field,
          "Field " + field + " must be a string.");
      return defaultValue;
    }

    return value.getAsString();
  }

  private static ResourceLocation parseRequiredResourceLocation(
      JsonElement value, String field, List<ModelRenderProfileValidationIssue> issues) {
    String rawValue = requiredString(value, field, issues);
    if (rawValue == null) {
      return null;
    }
    ResourceLocation resourceLocation = ResourceLocation.tryParse(rawValue);
    if (resourceLocation == null) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
          field,
          "Invalid ResourceLocation " + rawValue + ".");
    }

    return resourceLocation;
  }

  private static float optionalFloat(
      JsonElement value,
      float defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    Float floatValue = parseFloat(value, field, issues);
    return floatValue == null ? defaultValue : floatValue;
  }

  private static Float parseFloat(
      JsonElement value, String field, List<ModelRenderProfileValidationIssue> issues) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          field,
          "Field " + field + " must be a number.");
      return null;
    }
    float floatValue = value.getAsFloat();
    if (!Float.isFinite(floatValue)) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          field,
          "Field " + field + " must be finite.");
      return null;
    }

    return floatValue;
  }

  private static float[] optionalFloatArray(
      JsonElement value,
      float[] defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonArray() || value.getAsJsonArray().size() != 3) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          field,
          "Field " + field + " must be an array with 3 numbers.");
      return defaultValue;
    }

    float[] values = new float[3];
    for (int index = 0; index < values.length; index++) {
      Float floatValue = parseFloat(value.getAsJsonArray().get(index), field, issues);
      if (floatValue == null) {
        return defaultValue;
      }
      values[index] = floatValue;
    }

    return values;
  }

  private static ModelRenderProfileStatus statusForRequiredField(String field) {
    if (SCHEMA_VERSION_FIELD.equals(field)) {
      return ModelRenderProfileStatus.INVALID_SCHEMA_VERSION;
    }
    if (BODY_TYPE_FIELD.equals(field)) {
      return ModelRenderProfileStatus.INVALID_BODY_TYPE;
    }

    return ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION;
  }

  private static void addIssue(
      List<ModelRenderProfileValidationIssue> issues,
      ModelRenderProfileStatus status,
      String field,
      String message) {
    issues.add(new ModelRenderProfileValidationIssue(status, field, message));
  }

  private static class RawRenderProfile {
    @SerializedName(SCHEMA_VERSION_FIELD)
    JsonElement schemaVersion;

    @SerializedName(ID_FIELD)
    JsonElement id;

    @SerializedName(PACK_PAIR_FIELD)
    JsonElement packPair;

    @SerializedName(BODY_TYPE_FIELD)
    JsonElement bodyType;

    @SerializedName(MODEL_FIELD)
    JsonElement model;

    @SerializedName(TEXTURE_FIELD)
    JsonElement texture;

    @SerializedName(RENDERING_FIELD)
    JsonElement rendering;

    @SerializedName(ANIMATION_FIELD)
    JsonElement animation;
  }

  private static class RawPackPair {
    @SerializedName(PAIR_ID_FIELD)
    JsonElement pairId;

    @SerializedName(ASSET_FINGERPRINT_FIELD)
    JsonElement assetFingerprint;
  }

  private static class RawRendering {
    @SerializedName(SCALE_FIELD)
    JsonElement scale;

    @SerializedName(SHADOW_RADIUS_FIELD)
    JsonElement shadowRadius;

    @SerializedName(VISIBLE_BOUNDS_WIDTH_FIELD)
    JsonElement visibleBoundsWidth;

    @SerializedName(VISIBLE_BOUNDS_HEIGHT_FIELD)
    JsonElement visibleBoundsHeight;

    @SerializedName(VISIBLE_BOUNDS_OFFSET_FIELD)
    JsonElement visibleBoundsOffset;
  }

  private static class RawAnimation {
    @SerializedName(MODE_FIELD)
    JsonElement mode;

    @SerializedName(IDLE_FIELD)
    JsonElement idle;

    @SerializedName(WALK_FIELD)
    JsonElement walk;

    @SerializedName(RUN_FIELD)
    JsonElement run;

    @SerializedName(HURT_FIELD)
    JsonElement hurt;

    @SerializedName(DEATH_FIELD)
    JsonElement death;

    @SerializedName(SWING_SPEED_FIELD)
    JsonElement swingSpeed;

    @SerializedName(WALK_SPEED_MULTIPLIER_FIELD)
    JsonElement walkSpeedMultiplier;
  }
}

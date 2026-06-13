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
import de.markusbordihn.easymodelentities.profile.ModelPresetType;
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
  private static final String PRESET_TYPE_FIELD = "preset_type";
  private static final String VERSION_FIELD = "version";
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
        Constants.SCHEMA_VERSION,
        EMPTY_VALUE,
        ModelBodyType.STATIC,
        ModelResourcePaths.defaultModelId(expectedId),
        ModelResourcePaths.defaultTextureId(expectedId),
        defaultRenderSettings(ModelPresetType.STATUE),
        defaultAnimationSettings(ModelPresetType.STATUE),
        status,
        issues);
  }

  private static EasyModelRenderProfile parseObject(
      ResourceLocation expectedId, JsonObject jsonObject) {
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    RawRenderProfile rawProfile = GSON.fromJson(jsonObject, RawRenderProfile.class);

    String schemaVersion = parseSchemaVersion(rawProfile.schemaVersion, issues);
    String version = optionalString(rawProfile.version, EMPTY_VALUE, VERSION_FIELD, issues);
    ModelPresetType presetType = parsePresetType(rawProfile.presetType, issues);
    ModelPresetType resolvedPresetType = presetType == null ? ModelPresetType.STATUE : presetType;
    boolean custom = resolvedPresetType.isCustom();

    ModelBodyType bodyType =
        parseBodyType(rawProfile.bodyType, defaultBodyType(resolvedPresetType), custom, issues);
    ResourceLocation model =
        parseOptionalResourceLocation(
            rawProfile.model, ModelResourcePaths.defaultModelId(expectedId), MODEL_FIELD, issues);
    ResourceLocation texture =
        parseOptionalResourceLocation(
            rawProfile.texture,
            ModelResourcePaths.defaultTextureId(expectedId),
            TEXTURE_FIELD,
            issues);
    ModelRenderSettings renderSettings =
        parseRenderSettings(rawProfile.rendering, resolvedPresetType, issues);
    ModelAnimationSettings animationSettings =
        parseAnimationSettings(rawProfile.animation, resolvedPresetType, issues);

    return new EasyModelRenderProfile(
        expectedId,
        schemaVersion,
        version,
        bodyType == null ? defaultBodyType(resolvedPresetType) : bodyType,
        model == null ? ModelResourcePaths.defaultModelId(expectedId) : model,
        texture == null ? ModelResourcePaths.defaultTextureId(expectedId) : texture,
        renderSettings,
        animationSettings,
        ModelRenderProfileStatus.statusForIssues(issues),
        issues);
  }

  private static ModelRenderSettings parseRenderSettings(
      JsonElement renderingElement,
      ModelPresetType presetType,
      List<ModelRenderProfileValidationIssue> issues) {
    RawRendering rawRendering =
        optionalObject(renderingElement, RENDERING_FIELD, RawRendering.class, issues);
    ModelRenderSettings defaults = defaultRenderSettings(presetType);
    float visibleBoundsHeight =
        optionalFloat(
            rawRendering == null ? null : rawRendering.visibleBoundsHeight,
            defaults.visibleBoundsHeight(),
            RENDERING_VISIBLE_BOUNDS_HEIGHT_FIELD,
            issues);
    float[] visibleBoundsOffset =
        optionalFloatArray(
            rawRendering == null ? null : rawRendering.visibleBoundsOffset,
            new float[] {
              defaults.visibleBoundsOffsetX(),
              defaults.visibleBoundsOffsetY(),
              defaults.visibleBoundsOffsetZ()
            },
            RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD,
            issues);

    return new ModelRenderSettings(
        optionalFloat(
            rawRendering == null ? null : rawRendering.scale,
            defaults.scale(),
            RENDERING_SCALE_FIELD,
            issues),
        optionalFloat(
            rawRendering == null ? null : rawRendering.shadowRadius,
            defaults.shadowRadius(),
            RENDERING_SHADOW_RADIUS_FIELD,
            issues),
        optionalFloat(
            rawRendering == null ? null : rawRendering.visibleBoundsWidth,
            defaults.visibleBoundsWidth(),
            RENDERING_VISIBLE_BOUNDS_WIDTH_FIELD,
            issues),
        visibleBoundsHeight,
        visibleBoundsOffset[0],
        visibleBoundsOffset[1],
        visibleBoundsOffset[2]);
  }

  private static ModelAnimationSettings parseAnimationSettings(
      JsonElement animationElement,
      ModelPresetType presetType,
      List<ModelRenderProfileValidationIssue> issues) {
    RawAnimation rawAnimation =
        optionalObject(animationElement, ANIMATION_FIELD, RawAnimation.class, issues);
    ModelAnimationSettings defaults = defaultAnimationSettings(presetType);
    ModelAnimationMode mode = parseAnimationMode(rawAnimation, defaults.mode(), issues);

    return new ModelAnimationSettings(
        mode,
        optionalString(
            rawAnimation == null ? null : rawAnimation.idle,
            defaults.idle(),
            ANIMATION_IDLE_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.walk,
            defaults.walk(),
            ANIMATION_WALK_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.run,
            defaults.run(),
            ANIMATION_RUN_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.hurt,
            defaults.hurt(),
            ANIMATION_HURT_FIELD,
            issues),
        optionalString(
            rawAnimation == null ? null : rawAnimation.death,
            defaults.death(),
            ANIMATION_DEATH_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.swingSpeed,
            defaults.swingSpeed(),
            ANIMATION_SWING_SPEED_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.walkSpeedMultiplier,
            defaults.walkSpeedMultiplier(),
            ANIMATION_WALK_SPEED_MULTIPLIER_FIELD,
            issues));
  }

  private static String parseSchemaVersion(
      JsonElement schemaVersionElement, List<ModelRenderProfileValidationIssue> issues) {
    String schemaVersion =
        optionalString(
            schemaVersionElement,
            Constants.SCHEMA_VERSION,
            SCHEMA_VERSION_FIELD,
            issues,
            ModelRenderProfileStatus.INVALID_SCHEMA_VERSION);
    if (!Constants.SCHEMA_VERSION.equals(schemaVersion)) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_SCHEMA_VERSION,
          SCHEMA_VERSION_FIELD,
          "Unsupported schema_version " + schemaVersion + ".");
    }

    return schemaVersion;
  }

  private static ModelPresetType parsePresetType(
      JsonElement value, List<ModelRenderProfileValidationIssue> issues) {
    String presetTypeName = requiredString(value, PRESET_TYPE_FIELD, issues);
    if (presetTypeName == null) {
      return null;
    }

    return ModelPresetType.bySerializedName(presetTypeName)
        .orElseGet(
            () -> {
              addIssue(
                  issues,
                  ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
                  PRESET_TYPE_FIELD,
                  "Unsupported preset type " + presetTypeName + ".");
              return null;
            });
  }

  private static ModelBodyType parseBodyType(
      JsonElement value,
      ModelBodyType defaultValue,
      boolean required,
      List<ModelRenderProfileValidationIssue> issues) {
    String bodyTypeName =
        required
            ? requiredString(value, BODY_TYPE_FIELD, issues)
            : optionalString(value, defaultValue.getSerializedName(), BODY_TYPE_FIELD, issues);
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
      RawAnimation rawAnimation,
      ModelAnimationMode defaultMode,
      List<ModelRenderProfileValidationIssue> issues) {
    String modeName =
        optionalString(
            rawAnimation == null ? null : rawAnimation.mode,
            defaultMode.getSerializedName(),
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
              return defaultMode;
            });
  }

  private static ModelBodyType defaultBodyType(ModelPresetType presetType) {
    return switch (presetType) {
      case HUMANOID_STILL, HUMANOID_WANDERING -> ModelBodyType.BIPED;
      case QUADRUPED_STILL, QUADRUPED_WANDERING -> ModelBodyType.QUADRUPED;
      case AQUATIC_STILL, AQUATIC_SWIMMING -> ModelBodyType.AQUATIC;
      case WINGED_STILL, WINGED_WANDERING -> ModelBodyType.WINGED;
      case WINGED_HUMANOID_STILL, WINGED_HUMANOID_WANDERING -> ModelBodyType.WINGED_HUMANOID;
      case ARTHROPOD_STILL, ARTHROPOD_WANDERING -> ModelBodyType.ARTHROPOD;
      case CUBOID_STILL, CUBOID_HOPPING -> ModelBodyType.CUBOID;
      case FLOATING_STILL -> ModelBodyType.FLOATING;
      case CUSTOM, STATIC, STATUE -> ModelBodyType.STATIC;
    };
  }

  private static ModelRenderSettings defaultRenderSettings(ModelPresetType presetType) {
    return switch (presetType) {
      case QUADRUPED_STILL, QUADRUPED_WANDERING -> renderSettings(0.9f, 0.9f, 0.45f, 0.45f);
      case AQUATIC_STILL, AQUATIC_SWIMMING -> renderSettings(0.7f, 0.4f, 0.2f, 0.25f);
      case WINGED_STILL, WINGED_WANDERING -> renderSettings(0.6f, 0.9f, 0.45f, 0.25f);
      case WINGED_HUMANOID_STILL, WINGED_HUMANOID_WANDERING ->
          renderSettings(0.6f, 0.8f, 0.4f, 0.25f);
      case ARTHROPOD_STILL, ARTHROPOD_WANDERING -> renderSettings(1.4f, 0.9f, 0.45f, 0.7f);
      case CUBOID_STILL, CUBOID_HOPPING -> renderSettings(1.0f, 1.0f, 0.5f, 0.5f);
      case FLOATING_STILL -> renderSettings(1.0f, 1.0f, 0.5f, 0.5f);
      case CUSTOM, STATIC, STATUE, HUMANOID_STILL, HUMANOID_WANDERING ->
          renderSettings(0.6f, 1.8f, 0.9f, 0.3f);
    };
  }

  private static ModelRenderSettings renderSettings(
      float boundsWidth, float boundsHeight, float boundsOffsetY, float shadowRadius) {
    return new ModelRenderSettings(
        DEFAULT_SCALE, shadowRadius, boundsWidth, boundsHeight, 0.0f, boundsOffsetY, 0.0f);
  }

  private static ModelAnimationSettings defaultAnimationSettings(ModelPresetType presetType) {
    ModelAnimationMode mode =
        presetType == ModelPresetType.CUSTOM
                || presetType == ModelPresetType.STATIC
                || presetType == ModelPresetType.STATUE
            ? ModelAnimationMode.NONE
            : ModelAnimationMode.AUTOMATIC;
    return new ModelAnimationSettings(
        mode,
        IDLE_FIELD,
        WALK_FIELD,
        RUN_FIELD,
        HURT_FIELD,
        DEATH_FIELD,
        DEFAULT_SWING_SPEED,
        DEFAULT_WALK_SPEED_MULTIPLIER);
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
    return optionalString(
        value, defaultValue, field, issues, ModelRenderProfileStatus.INVALID_RENDER_SETTINGS);
  }

  private static String optionalString(
      JsonElement value,
      String defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues,
      ModelRenderProfileStatus status) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      addIssue(issues, status, field, "Field " + field + " must be a string.");
      return defaultValue;
    }

    return value.getAsString();
  }

  private static ResourceLocation parseOptionalResourceLocation(
      JsonElement value,
      ResourceLocation defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues) {
    String rawValue = optionalString(value, null, field, issues);
    if (rawValue == null) {
      return defaultValue;
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

    return ModelRenderProfileStatus.INVALID_RENDER_SETTINGS;
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

    @SerializedName(PRESET_TYPE_FIELD)
    JsonElement presetType;

    @SerializedName(VERSION_FIELD)
    JsonElement version;

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

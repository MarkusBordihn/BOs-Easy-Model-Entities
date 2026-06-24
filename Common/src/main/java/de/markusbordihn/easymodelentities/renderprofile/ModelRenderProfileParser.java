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
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.data.profile.ModelPresetType;
import de.markusbordihn.easymodelentities.data.renderprofile.*;
import de.markusbordihn.easymodelentities.json.JsonValues;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import de.markusbordihn.easymodelentities.schema.SchemaMigrations;
import de.markusbordihn.easymodelentities.schema.SchemaVersions;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class ModelRenderProfileParser {

  private static final Gson GSON = new Gson();
  private static final String EMPTY_VALUE = "";
  private static final String JSON_FIELD = "json";
  private static final String SCHEMA_VERSION_FIELD = "schema_version";
  private static final String PRESET_TYPE_FIELD = "preset_type";
  private static final String VERSION_FIELD = "version";
  private static final String ASSET_FINGERPRINT_FIELD = "asset_fingerprint";
  private static final String BODY_TYPE_FIELD = "body_type";
  private static final String MODEL_FIELD = "model";
  private static final String TEXTURE_FIELD = "texture";
  private static final String TEXTURES_FIELD = "textures";
  private static final String RENDERING_FIELD = "rendering";
  private static final String SCALE_FIELD = "scale";
  private static final String SHADOW_RADIUS_FIELD = "shadow_radius";
  private static final String VISIBLE_BOUNDS_WIDTH_FIELD = "visible_bounds_width";
  private static final String VISIBLE_BOUNDS_HEIGHT_FIELD = "visible_bounds_height";
  private static final String VISIBLE_BOUNDS_OFFSET_FIELD = "visible_bounds_offset";
  private static final String ANIMATION_FIELD = "animation";
  private static final String MODE_FIELD = "mode";
  private static final String SWING_SPEED_FIELD = "swing_speed";
  private static final String WALK_SPEED_MULTIPLIER_FIELD = "walk_speed_multiplier";
  private static final String IDLE_STRENGTH_FIELD = "idle_strength";
  private static final String GAIT_FIELD = "gait";
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
  private static final String ANIMATION_SWING_SPEED_FIELD =
      ANIMATION_FIELD + "." + SWING_SPEED_FIELD;
  private static final String ANIMATION_WALK_SPEED_MULTIPLIER_FIELD =
      ANIMATION_FIELD + "." + WALK_SPEED_MULTIPLIER_FIELD;
  private static final String ANIMATION_IDLE_STRENGTH_FIELD =
      ANIMATION_FIELD + "." + IDLE_STRENGTH_FIELD;
  private static final String ANIMATION_GAIT_FIELD = ANIMATION_FIELD + "." + GAIT_FIELD;
  private static final float DEFAULT_SCALE = 1.0f;
  private static final float DEFAULT_SWING_SPEED = 1.0f;
  private static final float DEFAULT_WALK_SPEED_MULTIPLIER = 1.0f;
  private static final float DEFAULT_IDLE_STRENGTH = 1.0f;

  private ModelRenderProfileParser() {}

  public static EasyModelRenderProfile parse(Identifier expectedId, Reader reader) {
    return parse(expectedId, reader, SchemaMigrations.DEFAULT);
  }

  public static EasyModelRenderProfile parse(
      Identifier expectedId, Reader reader, SchemaMigrations migrations) {
    Objects.requireNonNull(expectedId, "expectedId");
    Objects.requireNonNull(reader, "reader");
    Objects.requireNonNull(migrations, "migrations");

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

    return parseObject(expectedId, jsonElement.getAsJsonObject(), migrations);
  }

  static EasyModelRenderProfile invalidFallback(
      Identifier expectedId, ModelRenderProfileStatus status, String field, String message) {
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
      Identifier expectedId, JsonObject jsonObject, SchemaMigrations migrations) {
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    String schemaVersion =
        optionalString(
            jsonObject.get(SCHEMA_VERSION_FIELD),
            Constants.SCHEMA_VERSION,
            SCHEMA_VERSION_FIELD,
            issues,
            ModelRenderProfileStatus.INVALID_SCHEMA_VERSION);
    JsonObject effectiveObject =
        applySchemaMigrations(jsonObject, schemaVersion, migrations, issues);
    RawRenderProfile rawProfile = GSON.fromJson(effectiveObject, RawRenderProfile.class);

    String version = optionalString(rawProfile.version, EMPTY_VALUE, VERSION_FIELD, issues);
    String assetFingerprint =
        optionalString(rawProfile.assetFingerprint, EMPTY_VALUE, ASSET_FINGERPRINT_FIELD, issues);
    ModelPresetType presetType = parsePresetType(rawProfile.presetType, issues);
    ModelPresetType resolvedPresetType = presetType == null ? ModelPresetType.STATUE : presetType;
    boolean custom = resolvedPresetType.isCustom();

    ModelBodyType bodyType =
        parseBodyType(rawProfile.bodyType, resolvedPresetType.defaultBodyType(), custom, issues);
    Identifier model =
        parseOptionalResourceLocation(
            rawProfile.model, ModelResourcePaths.defaultModelId(expectedId), MODEL_FIELD, issues);
    Identifier texture =
        parseOptionalResourceLocation(
            rawProfile.texture,
            ModelResourcePaths.defaultTextureId(expectedId),
            TEXTURE_FIELD,
            issues);
    Map<Integer, Identifier> textures = parseTextures(rawProfile.textures, issues);
    ModelRenderSettings renderSettings =
        parseRenderSettings(rawProfile.rendering, resolvedPresetType, issues);
    ModelAnimationSettings animationSettings =
        parseAnimationSettings(rawProfile.animation, resolvedPresetType, issues);

    return new EasyModelRenderProfile(
        expectedId,
        schemaVersion,
        version,
        bodyType == null ? resolvedPresetType.defaultBodyType() : bodyType,
        model == null ? ModelResourcePaths.defaultModelId(expectedId) : model,
        texture == null ? ModelResourcePaths.defaultTextureId(expectedId) : texture,
        textures,
        renderSettings,
        animationSettings,
        ModelRenderProfileStatus.statusForIssues(issues),
        issues,
        assetFingerprint);
  }

  private static ModelRenderSettings parseRenderSettings(
      JsonElement renderingElement,
      ModelPresetType presetType,
      List<ModelRenderProfileValidationIssue> issues) {
    RawRendering rawRendering =
        optionalObject(renderingElement, RENDERING_FIELD, RawRendering.class, issues);
    ModelRenderSettings defaults = defaultRenderSettings(presetType);

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
        optionalFloat(
            rawRendering == null ? null : rawRendering.visibleBoundsHeight,
            defaults.visibleBoundsHeight(),
            RENDERING_VISIBLE_BOUNDS_HEIGHT_FIELD,
            issues),
        parseVisibleBoundsOffset(
            rawRendering == null ? null : rawRendering.visibleBoundsOffset,
            defaults.visibleBoundsOffset(),
            issues));
  }

  private static Vec3f parseVisibleBoundsOffset(
      JsonElement value, Vec3f defaultValue, List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonArray() || value.getAsJsonArray().size() != 3) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RENDER_SETTINGS,
          RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD,
          "Field " + RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD + " must be an array of 3 numbers.");
      return defaultValue;
    }

    float[] offset = new float[3];
    for (int index = 0; index < 3; index++) {
      Float component =
          parseFloat(
              value.getAsJsonArray().get(index), RENDERING_VISIBLE_BOUNDS_OFFSET_FIELD, issues);
      if (component == null) {
        return defaultValue;
      }
      offset[index] = component;
    }

    return Vec3f.of(offset);
  }

  private static ModelAnimationSettings parseAnimationSettings(
      JsonElement animationElement,
      ModelPresetType presetType,
      List<ModelRenderProfileValidationIssue> issues) {
    RawAnimation rawAnimation =
        optionalObject(animationElement, ANIMATION_FIELD, RawAnimation.class, issues);
    ModelAnimationSettings defaults = defaultAnimationSettings(presetType);
    ModelAnimationMode mode = parseAnimationMode(rawAnimation, defaults.mode(), issues);
    ModelGaitType gait = parseGait(rawAnimation, defaults.gait(), issues);

    return new ModelAnimationSettings(
        mode,
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.swingSpeed,
            defaults.swingSpeed(),
            ANIMATION_SWING_SPEED_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.walkSpeedMultiplier,
            defaults.walkSpeedMultiplier(),
            ANIMATION_WALK_SPEED_MULTIPLIER_FIELD,
            issues),
        optionalFloat(
            rawAnimation == null ? null : rawAnimation.idleStrength,
            defaults.idleStrength(),
            ANIMATION_IDLE_STRENGTH_FIELD,
            issues),
        gait);
  }

  private static ModelGaitType parseGait(
      RawAnimation rawAnimation,
      ModelGaitType defaultGait,
      List<ModelRenderProfileValidationIssue> issues) {
    String gaitName =
        optionalString(
            rawAnimation == null ? null : rawAnimation.gait,
            defaultGait.getSerializedName(),
            ANIMATION_GAIT_FIELD,
            issues);
    return ModelGaitType.bySerializedName(gaitName)
        .orElseGet(
            () -> {
              addIssue(
                  issues,
                  ModelRenderProfileStatus.INVALID_ANIMATION_MODE,
                  ANIMATION_GAIT_FIELD,
                  "Unsupported gait " + gaitName + ".");
              return defaultGait;
            });
  }

  private static JsonObject applySchemaMigrations(
      JsonObject jsonObject,
      String schemaVersion,
      SchemaMigrations migrations,
      List<ModelRenderProfileValidationIssue> issues) {
    SchemaVersions.Classification classification =
        SchemaVersions.classify(schemaVersion, Constants.SCHEMA_VERSION);
    if (classification == SchemaVersions.Classification.CURRENT) {
      return jsonObject;
    }
    if (classification == SchemaVersions.Classification.OLDER) {
      Optional<JsonObject> migrated =
          migrations.migrate(jsonObject, schemaVersion, Constants.SCHEMA_VERSION);
      if (migrated.isPresent()) {
        return migrated.get();
      }
    }

    addIssue(
        issues,
        ModelRenderProfileStatus.INVALID_SCHEMA_VERSION,
        SCHEMA_VERSION_FIELD,
        "Unsupported schema_version " + schemaVersion + ".");
    return jsonObject;
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

  private static ModelRenderSettings defaultRenderSettings(ModelPresetType presetType) {
    return switch (presetType) {
      case QUADRUPED_STILL, QUADRUPED_WANDERING, AMPHIBIOUS_STILL, AMPHIBIOUS_WANDERING ->
          renderSettings(0.45f);
      case AQUATIC_STILL, AQUATIC_SWIMMING -> renderSettings(0.25f);
      case WINGED_STILL, WINGED_WANDERING -> renderSettings(0.25f);
      case WINGED_HUMANOID_STILL, WINGED_HUMANOID_WANDERING -> renderSettings(0.25f);
      case ARTHROPOD_STILL, ARTHROPOD_WANDERING -> renderSettings(0.7f);
      case CUBOID_STILL, CUBOID_HOPPING -> renderSettings(0.5f);
      case FLOATING_STILL -> renderSettings(0.5f);
      case CUSTOM, STATIC, STATUE, HUMANOID_STILL, HUMANOID_WANDERING -> renderSettings(0.3f);
    };
  }

  private static ModelRenderSettings renderSettings(float shadowRadius) {
    return new ModelRenderSettings(DEFAULT_SCALE, shadowRadius, 0.0f, 0.0f, Vec3f.ZERO);
  }

  private static ModelAnimationSettings defaultAnimationSettings(ModelPresetType presetType) {
    ModelAnimationMode mode =
        presetType == ModelPresetType.CUSTOM
                || presetType == ModelPresetType.STATIC
                || presetType == ModelPresetType.STATUE
            ? ModelAnimationMode.NONE
            : ModelAnimationMode.AUTOMATIC;
    return new ModelAnimationSettings(
        mode, DEFAULT_SWING_SPEED, DEFAULT_WALK_SPEED_MULTIPLIER, DEFAULT_IDLE_STRENGTH);
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
    return JsonValues.requiredString(
        value,
        field,
        (issueField, message) ->
            addIssue(issues, statusForRequiredField(issueField), issueField, message));
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
    return JsonValues.optionalString(
        value,
        defaultValue,
        field,
        (issueField, message) -> addIssue(issues, status, issueField, message));
  }

  private static Map<Integer, Identifier> parseTextures(
      JsonElement value, List<ModelRenderProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return Map.of();
    }
    if (!value.isJsonObject()) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
          TEXTURES_FIELD,
          "Field " + TEXTURES_FIELD + " must be an object.");
      return Map.of();
    }

    Map<Integer, Identifier> textures = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
      String field = TEXTURES_FIELD + "." + entry.getKey();
      int index;
      try {
        index = Integer.parseInt(entry.getKey().trim());
      } catch (NumberFormatException exception) {
        addIssue(
            issues,
            ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
            field,
            "Texture key " + entry.getKey() + " must be an integer index.");
        continue;
      }
      if (index < 0) {
        addIssue(
            issues,
            ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
            field,
            "Texture index " + index + " must not be negative.");
        continue;
      }
      Identifier texture = parseOptionalResourceLocation(entry.getValue(), null, field, issues);
      if (texture != null) {
        textures.put(index, texture);
      }
    }

    return textures;
  }

  private static Identifier parseOptionalResourceLocation(
      JsonElement value,
      Identifier defaultValue,
      String field,
      List<ModelRenderProfileValidationIssue> issues) {
    String rawValue = optionalString(value, null, field, issues);
    if (rawValue == null) {
      return defaultValue;
    }
    Identifier identifier = Identifier.tryParse(rawValue);
    if (identifier == null) {
      addIssue(
          issues,
          ModelRenderProfileStatus.INVALID_RESOURCE_LOCATION,
          field,
          "Invalid Identifier " + rawValue + ".");
    }

    return identifier;
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
    @SerializedName(PRESET_TYPE_FIELD)
    JsonElement presetType;

    @SerializedName(VERSION_FIELD)
    JsonElement version;

    @SerializedName(ASSET_FINGERPRINT_FIELD)
    JsonElement assetFingerprint;

    @SerializedName(BODY_TYPE_FIELD)
    JsonElement bodyType;

    @SerializedName(MODEL_FIELD)
    JsonElement model;

    @SerializedName(TEXTURE_FIELD)
    JsonElement texture;

    @SerializedName(TEXTURES_FIELD)
    JsonElement textures;

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

    @SerializedName(SWING_SPEED_FIELD)
    JsonElement swingSpeed;

    @SerializedName(WALK_SPEED_MULTIPLIER_FIELD)
    JsonElement walkSpeedMultiplier;

    @SerializedName(IDLE_STRENGTH_FIELD)
    JsonElement idleStrength;

    @SerializedName(GAIT_FIELD)
    JsonElement gait;
  }
}

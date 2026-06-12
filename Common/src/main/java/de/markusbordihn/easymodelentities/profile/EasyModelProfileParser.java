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

package de.markusbordihn.easymodelentities.profile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.registry.ModelEntityTypeIds;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class EasyModelProfileParser {

  private static final Gson GSON = new Gson();
  private static final String EMPTY_VALUE = "";
  private static final String ISSUE_JSON_FIELD = "json";
  private static final String SCHEMA_VERSION_FIELD = "schema_version";
  private static final String ID_FIELD = "id";
  private static final String PACK_PAIR_FIELD = "pack_pair";
  private static final String PAIR_ID_FIELD = "pair_id";
  private static final String ASSET_FINGERPRINT_FIELD = "asset_fingerprint";
  private static final String HOST_FIELD = "host";
  private static final String ENTITY_TYPE_FIELD = "entity_type";
  private static final String MOVEMENT_TYPE_FIELD = "movement_type";
  private static final String BODY_TYPE_FIELD = "body_type";
  private static final String CLIENT_FIELD = "client";
  private static final String RENDER_PROFILE_FIELD = "render_profile";
  private static final String DIMENSIONS_FIELD = "dimensions";
  private static final String WIDTH_FIELD = "width";
  private static final String HEIGHT_FIELD = "height";
  private static final String EYE_HEIGHT_FIELD = "eye_height";
  private static final String MOVEMENT_FIELD = "movement";
  private static final String SPEED_FIELD = "speed";
  private static final String STEP_HEIGHT_FIELD = "step_height";
  private static final String GRAVITY_FIELD = "gravity";
  private static final String BEHAVIOR_FIELD = "behavior";
  private static final String MODE_FIELD = "mode";
  private static final String LOOK_AT_PLAYERS_FIELD = "look_at_players";
  private static final String RANDOM_STROLL_FIELD = "random_stroll";
  private static final String ATTRIBUTES_FIELD = "attributes";
  private static final String MAX_HEALTH_FIELD = "max_health";
  private static final String MOVEMENT_SPEED_FIELD = "movement_speed";
  private static final String FOLLOW_RANGE_FIELD = "follow_range";
  private static final String TRAITS_FIELD = "traits";
  private static final String PACK_PAIR_PAIR_ID_FIELD =
      PACK_PAIR_FIELD + "." + PAIR_ID_FIELD;
  private static final String PACK_PAIR_ASSET_FINGERPRINT_FIELD =
      PACK_PAIR_FIELD + "." + ASSET_FINGERPRINT_FIELD;
  private static final String HOST_ENTITY_TYPE_FIELD =
      HOST_FIELD + "." + ENTITY_TYPE_FIELD;
  private static final String HOST_MOVEMENT_TYPE_FIELD =
      HOST_FIELD + "." + MOVEMENT_TYPE_FIELD;
  private static final String HOST_BODY_TYPE_FIELD = HOST_FIELD + "." + BODY_TYPE_FIELD;
  private static final String CLIENT_RENDER_PROFILE_FIELD =
      CLIENT_FIELD + "." + RENDER_PROFILE_FIELD;
  private static final String DIMENSIONS_WIDTH_FIELD = DIMENSIONS_FIELD + "." + WIDTH_FIELD;
  private static final String DIMENSIONS_HEIGHT_FIELD = DIMENSIONS_FIELD + "." + HEIGHT_FIELD;
  private static final String DIMENSIONS_EYE_HEIGHT_FIELD =
      DIMENSIONS_FIELD + "." + EYE_HEIGHT_FIELD;
  private static final String MOVEMENT_SPEED_PATH = MOVEMENT_FIELD + "." + SPEED_FIELD;
  private static final String MOVEMENT_STEP_HEIGHT_FIELD =
      MOVEMENT_FIELD + "." + STEP_HEIGHT_FIELD;
  private static final String MOVEMENT_GRAVITY_FIELD =
      MOVEMENT_FIELD + "." + GRAVITY_FIELD;
  private static final String BEHAVIOR_MODE_FIELD = BEHAVIOR_FIELD + "." + MODE_FIELD;
  private static final String BEHAVIOR_LOOK_AT_PLAYERS_FIELD =
      BEHAVIOR_FIELD + "." + LOOK_AT_PLAYERS_FIELD;
  private static final String BEHAVIOR_RANDOM_STROLL_FIELD =
      BEHAVIOR_FIELD + "." + RANDOM_STROLL_FIELD;
  private static final String ATTRIBUTES_MAX_HEALTH_FIELD =
      ATTRIBUTES_FIELD + "." + MAX_HEALTH_FIELD;
  private static final String ATTRIBUTES_MOVEMENT_SPEED_FIELD =
      ATTRIBUTES_FIELD + "." + MOVEMENT_SPEED_FIELD;
  private static final String ATTRIBUTES_FOLLOW_RANGE_FIELD =
      ATTRIBUTES_FIELD + "." + FOLLOW_RANGE_FIELD;
  private static final float DEFAULT_MAX_HEALTH = 10.0f;
  private static final float DEFAULT_FOLLOW_RANGE = 16.0f;

  private EasyModelProfileParser() {}

  public static EasyModelEntityProfile parse(ResourceLocation expectedId, Reader reader) {
    Objects.requireNonNull(expectedId, "expectedId");
    Objects.requireNonNull(reader, "reader");

    JsonElement jsonElement;
    try {
      jsonElement = JsonParser.parseReader(reader);
    } catch (JsonParseException exception) {
      return invalidFallback(
          expectedId,
          ModelProfileStatus.INVALID_JSON,
          ISSUE_JSON_FIELD,
          "Malformed server profile JSON: " + exception.getMessage());
    }

    if (!jsonElement.isJsonObject()) {
      return invalidFallback(
          expectedId,
          ModelProfileStatus.INVALID_JSON,
          ISSUE_JSON_FIELD,
          "Server profile JSON must be an object.");
    }

    return parseObject(expectedId, jsonElement.getAsJsonObject());
  }

  private static EasyModelEntityProfile parseObject(
      ResourceLocation expectedId, JsonObject jsonObject) {
    List<ModelProfileValidationIssue> issues = new ArrayList<>();
    RawProfile rawProfile = GSON.fromJson(jsonObject, RawProfile.class);
    String schemaVersion = requiredString(rawProfile.schemaVersion, SCHEMA_VERSION_FIELD, issues);
    if (schemaVersion != null && !Constants.SCHEMA_VERSION.equals(schemaVersion)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_SCHEMA_VERSION,
          SCHEMA_VERSION_FIELD,
          "Unsupported schema_version " + schemaVersion + ".");
    }

    ResourceLocation profileId =
        parseRequiredResourceLocation(rawProfile.id, ID_FIELD, issues);
    if (profileId != null && !expectedId.equals(profileId)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_RESOURCE_LOCATION,
          ID_FIELD,
          "Profile id " + profileId + " does not match path id " + expectedId + ".");
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

    RawHost rawHost = requiredObject(rawProfile.host, HOST_FIELD, RawHost.class, issues);
    ResourceLocation hostEntityType =
        parseRequiredResourceLocation(
            rawHost == null ? null : rawHost.entityType, HOST_ENTITY_TYPE_FIELD, issues);
    if (hostEntityType != null
        && !ModelEntityTypeIds.isSupportedHostEntityType(hostEntityType)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_HOST_ENTITY,
          HOST_ENTITY_TYPE_FIELD,
          "Unsupported host entity type " + hostEntityType + ".");
    }

    ModelMovementType movementType = parseMovementType(rawHost, issues);

    ModelBodyType bodyType = parseBodyType(rawHost, issues);
    RawClient rawClient = requiredObject(rawProfile.client, CLIENT_FIELD, RawClient.class, issues);
    ResourceLocation renderProfile =
        parseRequiredResourceLocation(
            rawClient == null ? null : rawClient.renderProfile,
            CLIENT_RENDER_PROFILE_FIELD,
            issues);
    RawDimensions rawDimensions =
        requiredObject(rawProfile.dimensions, DIMENSIONS_FIELD, RawDimensions.class, issues);
    Float width =
        requiredFloat(
            rawDimensions == null ? null : rawDimensions.width, DIMENSIONS_WIDTH_FIELD, issues);
    Float height =
        requiredFloat(
            rawDimensions == null ? null : rawDimensions.height, DIMENSIONS_HEIGHT_FIELD, issues);
    Float eyeHeight =
        requiredFloat(
            rawDimensions == null ? null : rawDimensions.eyeHeight,
            DIMENSIONS_EYE_HEIGHT_FIELD,
            issues);

    if (width != null && !isInRange(width, 0.01f, 8.0f)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_DIMENSIONS,
          DIMENSIONS_WIDTH_FIELD,
          "Width must be between 0.01 and 8.0.");
    }
    if (height != null && !isInRange(height, 0.01f, 8.0f)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_DIMENSIONS,
          DIMENSIONS_HEIGHT_FIELD,
          "Height must be between 0.01 and 8.0.");
    }
    if (eyeHeight != null
        && height != null
        && (!Float.isFinite(eyeHeight) || eyeHeight < 0.0f || eyeHeight > height)) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_DIMENSIONS,
          DIMENSIONS_EYE_HEIGHT_FIELD,
          "Eye height must be between 0.0 and " + DIMENSIONS_HEIGHT_FIELD + ".");
    }

    ModelMovementType resolvedMovementType =
        movementType == null ? ModelMovementType.STATIC : movementType;
    RawMovement rawMovement =
        optionalObject(rawProfile.movement, MOVEMENT_FIELD, RawMovement.class, issues);
    ModelMovementSettings movement =
        parseMovement(rawMovement, resolvedMovementType, issues);
    RawBehavior rawBehavior =
        optionalObject(rawProfile.behavior, BEHAVIOR_FIELD, RawBehavior.class, issues);
    ModelBehaviorSettings behavior =
        parseBehavior(rawBehavior, resolvedMovementType, issues);
    RawAttributes rawAttributes =
        optionalObject(rawProfile.attributes, ATTRIBUTES_FIELD, RawAttributes.class, issues);
    ModelAttributes attributes =
        parseAttributes(rawAttributes, movement, issues);
    Set<ResourceLocation> traits = parseTraits(rawProfile.traits, issues);
    ModelProfileStatus status = statusForIssues(issues);

    return new EasyModelEntityProfile(
        expectedId,
        schemaVersion == null ? EMPTY_VALUE : schemaVersion,
        packPair,
        new ModelHostSettings(
            hostEntityType == null ? ModelEntityTypeIds.STATIC_ENTITY : hostEntityType,
            resolvedMovementType,
            bodyType == null ? ModelBodyType.STATIC : bodyType),
        new ModelClientSettings(renderProfile == null ? expectedId : renderProfile),
        new ModelDimensions(
            width == null ? 0.01f : width,
            height == null ? 0.01f : height,
            eyeHeight == null ? 0.0f : eyeHeight),
        movement,
        behavior,
        attributes,
        traits,
        status,
        issues);
  }

  private static ModelMovementSettings parseMovement(
      RawMovement rawMovement,
      ModelMovementType movementType,
      List<ModelProfileValidationIssue> issues) {
    float speed =
        optionalFloat(
            rawMovement == null ? null : rawMovement.speed,
            movementType.defaultSpeed(),
            MOVEMENT_SPEED_PATH,
            issues);
    float stepHeight =
        optionalFloat(
            rawMovement == null ? null : rawMovement.stepHeight,
            movementType.defaultStepHeight(),
            MOVEMENT_STEP_HEIGHT_FIELD,
            issues);
    boolean gravity =
        optionalBoolean(
            rawMovement == null ? null : rawMovement.gravity,
            movementType.defaultGravity(),
            MOVEMENT_GRAVITY_FIELD,
            issues);

    if (!isInRange(speed, 0.0f, 2.0f)) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          MOVEMENT_SPEED_PATH,
          "Movement speed must be between 0.0 and 2.0.");
    }
    if (!isInRange(stepHeight, 0.0f, 2.0f)) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          MOVEMENT_STEP_HEIGHT_FIELD,
          "Step height must be between 0.0 and 2.0.");
    }

    return new ModelMovementSettings(speed, stepHeight, gravity);
  }

  private static ModelBehaviorSettings parseBehavior(
      RawBehavior rawBehavior,
      ModelMovementType movementType,
      List<ModelProfileValidationIssue> issues) {
    ModelBehaviorMode defaultMode = movementType.defaultBehaviorMode();
    ModelBehaviorMode mode = parseOptionalBehaviorMode(rawBehavior, defaultMode, issues);
    boolean lookAtPlayers =
        optionalBoolean(
            rawBehavior == null ? null : rawBehavior.lookAtPlayers,
            mode.defaultLookAtPlayers(),
            BEHAVIOR_LOOK_AT_PLAYERS_FIELD,
            issues);
    boolean randomStroll =
        optionalBoolean(
            rawBehavior == null ? null : rawBehavior.randomStroll,
            false,
            BEHAVIOR_RANDOM_STROLL_FIELD,
            issues);
    return new ModelBehaviorSettings(mode, lookAtPlayers, randomStroll);
  }

  private static ModelMovementType parseMovementType(
      RawHost rawHost, List<ModelProfileValidationIssue> issues) {
    String movementTypeName =
        requiredString(
            rawHost == null ? null : rawHost.movementType, HOST_MOVEMENT_TYPE_FIELD, issues);
    if (movementTypeName == null) {
      return null;
    }

    return ModelMovementType.bySerializedName(movementTypeName)
        .orElseGet(
            () -> {
              addIssue(
                  issues,
                  ModelProfileStatus.INVALID_HOST_ENTITY,
                  HOST_MOVEMENT_TYPE_FIELD,
                  "Unsupported movement type " + movementTypeName + ".");
              return null;
            });
  }

  private static ModelBehaviorMode parseOptionalBehaviorMode(
      RawBehavior rawBehavior,
      ModelBehaviorMode defaultMode,
      List<ModelProfileValidationIssue> issues) {
    String modeName =
        optionalString(
            rawBehavior == null ? null : rawBehavior.mode,
            defaultMode.getSerializedName(),
            BEHAVIOR_MODE_FIELD,
            issues);
    return ModelBehaviorMode.bySerializedName(modeName)
        .orElseGet(
            () -> {
              addIssue(
                  issues,
                  ModelProfileStatus.DISABLED,
                  BEHAVIOR_MODE_FIELD,
                  "Unsupported behavior mode " + modeName + ".");
              return defaultMode;
            });
  }

  private static ModelAttributes parseAttributes(
      RawAttributes rawAttributes,
      ModelMovementSettings movement,
      List<ModelProfileValidationIssue> issues) {
    float maxHealth =
        optionalFloat(
            rawAttributes == null ? null : rawAttributes.maxHealth,
            DEFAULT_MAX_HEALTH,
            ATTRIBUTES_MAX_HEALTH_FIELD,
            issues);
    float movementSpeed =
        optionalFloat(
            rawAttributes == null ? null : rawAttributes.movementSpeed,
            movement.speed(),
            ATTRIBUTES_MOVEMENT_SPEED_FIELD,
            issues);
    float followRange =
        optionalFloat(
            rawAttributes == null ? null : rawAttributes.followRange,
            DEFAULT_FOLLOW_RANGE,
            ATTRIBUTES_FOLLOW_RANGE_FIELD,
            issues);

    if (!Float.isFinite(maxHealth) || maxHealth < 0.0f) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          ATTRIBUTES_MAX_HEALTH_FIELD,
          "Invalid max health.");
    }
    if (!Float.isFinite(movementSpeed) || movementSpeed < 0.0f) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          ATTRIBUTES_MOVEMENT_SPEED_FIELD,
          "Invalid movement speed attribute.");
    }
    if (!Float.isFinite(followRange) || followRange < 0.0f) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          ATTRIBUTES_FOLLOW_RANGE_FIELD,
          "Invalid follow range.");
    }

    return new ModelAttributes(maxHealth, movementSpeed, followRange);
  }

  private static Set<ResourceLocation> parseTraits(
      JsonElement traitsElement, List<ModelProfileValidationIssue> issues) {
    if (traitsElement == null || traitsElement.isJsonNull()) {
      return Set.of();
    }
    if (!traitsElement.isJsonArray()) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          TRAITS_FIELD,
          "Traits must be an array.");
      return Set.of();
    }
    if (traitsElement.getAsJsonArray().size() > 64) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          TRAITS_FIELD,
          "Profiles support at most 64 traits.");
      return Set.of();
    }

    Set<ResourceLocation> traits = new LinkedHashSet<>();
    for (JsonElement traitElement : traitsElement.getAsJsonArray()) {
      if (!traitElement.isJsonPrimitive() || !traitElement.getAsJsonPrimitive().isString()) {
        addIssue(
            issues,
            ModelProfileStatus.DISABLED,
            TRAITS_FIELD,
            "Trait ids must be strings.");
        continue;
      }
      ResourceLocation traitId = ResourceLocation.tryParse(traitElement.getAsString());
      if (traitId == null) {
        addIssue(
            issues,
            ModelProfileStatus.DISABLED,
            TRAITS_FIELD,
            "Invalid trait id " + traitElement.getAsString() + ".");
        continue;
      }
      traits.add(traitId);
    }

    return traits;
  }

  private static ModelBodyType parseBodyType(
      RawHost rawHost, List<ModelProfileValidationIssue> issues) {
    String bodyTypeName =
        requiredString(rawHost == null ? null : rawHost.bodyType, HOST_BODY_TYPE_FIELD, issues);
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
        ModelProfileStatus.INVALID_HOST_ENTITY,
        HOST_BODY_TYPE_FIELD,
        "Unsupported body type " + bodyTypeName + ".");
    return null;
  }

  private static <T> T requiredObject(
      JsonElement value,
      String field,
      Class<T> objectClass,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      addIssue(
          issues, statusForRequiredField(field), field, "Missing required field " + field + ".");
      return null;
    }
    if (!value.isJsonObject()) {
      addIssue(
          issues, statusForRequiredField(field), field, "Field " + field + " must be an object.");
      return null;
    }

    return GSON.fromJson(value, objectClass);
  }

  private static <T> T optionalObject(
      JsonElement value,
      String field,
      Class<T> objectClass,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return null;
    }
    if (!value.isJsonObject()) {
      addIssue(
          issues, ModelProfileStatus.DISABLED, field, "Field " + field + " must be an object.");
      return null;
    }

    return GSON.fromJson(value, objectClass);
  }

  private static String requiredString(
      JsonElement value,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      addIssue(
          issues,
          statusForRequiredField(issueField),
          issueField,
          "Missing required field " + issueField + ".");
      return null;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      addIssue(
          issues,
          statusForRequiredField(issueField),
          issueField,
          "Field " + issueField + " must be a string.");
      return null;
    }

    return value.getAsString();
  }

  private static String optionalString(
      JsonElement value,
      String defaultValue,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          issueField,
          "Field " + issueField + " must be a string.");
      return defaultValue;
    }

    return value.getAsString();
  }

  private static ResourceLocation parseRequiredResourceLocation(
      JsonElement value,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    String rawValue = requiredString(value, issueField, issues);
    if (rawValue == null) {
      return null;
    }
    ResourceLocation resourceLocation = ResourceLocation.tryParse(rawValue);
    if (resourceLocation == null) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_RESOURCE_LOCATION,
          issueField,
          "Invalid ResourceLocation " + rawValue + ".");
    }

    return resourceLocation;
  }

  private static Float requiredFloat(
      JsonElement value,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      addIssue(
          issues,
          ModelProfileStatus.INVALID_DIMENSIONS,
          issueField,
          "Missing required field " + issueField + ".");
      return null;
    }

    return parseFloat(value, issueField, issues, ModelProfileStatus.INVALID_DIMENSIONS);
  }

  private static float optionalFloat(
      JsonElement value,
      float defaultValue,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    Float floatValue = parseFloat(value, issueField, issues, ModelProfileStatus.DISABLED);
    return floatValue == null ? defaultValue : floatValue;
  }

  private static Float parseFloat(
      JsonElement value,
      String issueField,
      List<ModelProfileValidationIssue> issues,
      ModelProfileStatus status) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      addIssue(issues, status, issueField, "Field " + issueField + " must be a number.");
      return null;
    }
    float floatValue = value.getAsFloat();
    if (!Float.isFinite(floatValue)) {
      addIssue(issues, status, issueField, "Field " + issueField + " must be finite.");
      return null;
    }

    return floatValue;
  }

  private static boolean optionalBoolean(
      JsonElement value,
      boolean defaultValue,
      String issueField,
      List<ModelProfileValidationIssue> issues) {
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
      addIssue(
          issues,
          ModelProfileStatus.DISABLED,
          issueField,
          "Field " + issueField + " must be a boolean.");
      return defaultValue;
    }

    return value.getAsBoolean();
  }

  static EasyModelEntityProfile invalidFallback(
      ResourceLocation expectedId, ModelProfileStatus status, String field, String message) {
    List<ModelProfileValidationIssue> issues =
        List.of(new ModelProfileValidationIssue(status, field, message));
    return new EasyModelEntityProfile(
        expectedId,
        EMPTY_VALUE,
        new ModelPackPair(EMPTY_VALUE, EMPTY_VALUE),
        new ModelHostSettings(
            ModelEntityTypeIds.STATIC_ENTITY,
            ModelMovementType.STATIC,
            ModelBodyType.STATIC),
        new ModelClientSettings(expectedId),
        new ModelDimensions(0.01f, 0.01f, 0.0f),
        new ModelMovementSettings(
            ModelMovementType.STATIC.defaultSpeed(),
            ModelMovementType.STATIC.defaultStepHeight(),
            false),
        new ModelBehaviorSettings(ModelBehaviorMode.STATIC, false, false),
        new ModelAttributes(
            DEFAULT_MAX_HEALTH,
            ModelMovementType.STATIC.defaultSpeed(),
            DEFAULT_FOLLOW_RANGE),
        Set.of(),
        status,
        issues);
  }

  private static boolean isInRange(float value, float minimum, float maximum) {
    return Float.isFinite(value) && value >= minimum && value <= maximum;
  }

  private static void addIssue(
      List<ModelProfileValidationIssue> issues,
      ModelProfileStatus status,
      String field,
      String message) {
    issues.add(new ModelProfileValidationIssue(status, field, message));
  }

  private static ModelProfileStatus statusForRequiredField(String field) {
    if (SCHEMA_VERSION_FIELD.equals(field)) {
      return ModelProfileStatus.INVALID_SCHEMA_VERSION;
    }
    if (field.startsWith(DIMENSIONS_FIELD)) {
      return ModelProfileStatus.INVALID_DIMENSIONS;
    }
    if (field.startsWith(HOST_FIELD)) {
      return ModelProfileStatus.INVALID_HOST_ENTITY;
    }

    return ModelProfileStatus.INVALID_RESOURCE_LOCATION;
  }

  private static ModelProfileStatus statusForIssues(
      List<ModelProfileValidationIssue> issues) {
    return issues.stream()
        .map(ModelProfileValidationIssue::status)
        .min(Comparator.comparingInt(Enum::ordinal))
        .orElse(ModelProfileStatus.ACTIVE);
  }

  private static class RawProfile {
    @SerializedName(SCHEMA_VERSION_FIELD)
    JsonElement schemaVersion;

    @SerializedName(ID_FIELD)
    JsonElement id;

    @SerializedName(PACK_PAIR_FIELD)
    JsonElement packPair;

    @SerializedName(HOST_FIELD)
    JsonElement host;

    @SerializedName(CLIENT_FIELD)
    JsonElement client;

    @SerializedName(DIMENSIONS_FIELD)
    JsonElement dimensions;

    @SerializedName(MOVEMENT_FIELD)
    JsonElement movement;

    @SerializedName(BEHAVIOR_FIELD)
    JsonElement behavior;

    @SerializedName(ATTRIBUTES_FIELD)
    JsonElement attributes;

    @SerializedName(TRAITS_FIELD)
    JsonElement traits;
  }

  private static class RawPackPair {
    @SerializedName(PAIR_ID_FIELD)
    JsonElement pairId;

    @SerializedName(ASSET_FINGERPRINT_FIELD)
    JsonElement assetFingerprint;
  }

  private static class RawHost {
    @SerializedName(ENTITY_TYPE_FIELD)
    JsonElement entityType;

    @SerializedName(MOVEMENT_TYPE_FIELD)
    JsonElement movementType;

    @SerializedName(BODY_TYPE_FIELD)
    JsonElement bodyType;
  }

  private static class RawClient {
    @SerializedName(RENDER_PROFILE_FIELD)
    JsonElement renderProfile;
  }

  private static class RawDimensions {
    @SerializedName(WIDTH_FIELD)
    JsonElement width;

    @SerializedName(HEIGHT_FIELD)
    JsonElement height;

    @SerializedName(EYE_HEIGHT_FIELD)
    JsonElement eyeHeight;
  }

  private static class RawMovement {
    @SerializedName(SPEED_FIELD)
    JsonElement speed;

    @SerializedName(STEP_HEIGHT_FIELD)
    JsonElement stepHeight;

    @SerializedName(GRAVITY_FIELD)
    JsonElement gravity;
  }

  private static class RawBehavior {
    @SerializedName(MODE_FIELD)
    JsonElement mode;

    @SerializedName(LOOK_AT_PLAYERS_FIELD)
    JsonElement lookAtPlayers;

    @SerializedName(RANDOM_STROLL_FIELD)
    JsonElement randomStroll;
  }

  private static class RawAttributes {
    @SerializedName(MAX_HEALTH_FIELD)
    JsonElement maxHealth;

    @SerializedName(MOVEMENT_SPEED_FIELD)
    JsonElement movementSpeed;

    @SerializedName(FOLLOW_RANGE_FIELD)
    JsonElement followRange;
  }
}

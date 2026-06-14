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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.easymodelentities.model.decoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.decoder.*;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class BlockbenchBbModelDecoder implements EasyModelDecoder {

  public static final String FORMAT = "bbmodel";
  public static final int MAX_MODEL_FILE_SIZE_BYTES = 2 * 1024 * 1024;
  public static final int SOFT_MODEL_FILE_SIZE_BYTES = 1024 * 1024;
  public static final int MAX_TEXTURE_SIZE = 2048;
  public static final int SOFT_TEXTURE_SIZE = 128;
  public static final int MAX_BONE_COUNT = 128;
  public static final int SOFT_BONE_COUNT = 96;
  public static final int MAX_CUBE_COUNT = 512;
  public static final int SOFT_CUBE_COUNT = 384;
  public static final int MAX_HIERARCHY_DEPTH = 32;
  public static final int SOFT_HIERARCHY_DEPTH = 24;
  public static final int MAX_ANIMATION_COUNT = 16;
  private static final float MAX_ABSOLUTE_MODEL_VALUE = 100_000.0f;
  private static final int BUFFER_SIZE = 8192;

  private static byte[] readModelBytes(Resource resource) throws EasyModelDecodeException {
    try (InputStream inputStream = resource.open();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      int totalBytes = 0;
      while ((bytesRead = inputStream.read(buffer)) >= 0) {
        totalBytes += bytesRead;
        if (totalBytes > MAX_MODEL_FILE_SIZE_BYTES) {
          throw new EasyModelDecodeException("Model file exceeds the 2 MB asset budget.");
        }
        outputStream.write(buffer, 0, bytesRead);
      }

      return outputStream.toByteArray();
    } catch (IOException exception) {
      throw new EasyModelDecodeException("Could not read model resource.", exception);
    }
  }

  private static JsonObject parseRoot(ResourceLocation modelId, byte[] modelBytes)
      throws EasyModelDecodeException {
    try {
      JsonElement jsonElement =
          JsonParser.parseString(new String(modelBytes, StandardCharsets.UTF_8));
      if (!jsonElement.isJsonObject()) {
        throw new EasyModelDecodeException("Model " + modelId + " must be a JSON object.");
      }

      return jsonElement.getAsJsonObject();
    } catch (JsonParseException exception) {
      throw new EasyModelDecodeException("Malformed bbmodel JSON.", exception);
    }
  }

  private static void validateMeta(JsonObject root) throws EasyModelDecodeException {
    JsonObject meta = requiredObject(root, "meta");
    String formatVersion = requiredString(meta, "format_version");
    String modelFormat = requiredString(meta, "model_format");
    if (!formatVersion.startsWith("5.")) {
      throw new EasyModelDecodeException(
          "Unsupported Blockbench format_version " + formatVersion + ".");
    }
    if (!"modded_entity".equals(modelFormat)) {
      throw new EasyModelDecodeException(
          "Unsupported Blockbench model_format " + modelFormat + ".");
    }
  }

  private static int[] parseResolution(JsonObject root) throws EasyModelDecodeException {
    JsonObject resolution = requiredObject(root, "resolution");
    int width = requiredPositiveInt(resolution, "width");
    int height = requiredPositiveInt(resolution, "height");
    return new int[] {width, height};
  }

  private static void validateAnimationBudget(JsonObject root) throws EasyModelDecodeException {
    JsonElement animationsElement = root.get("animations");
    if (animationsElement == null || animationsElement.isJsonNull()) {
      return;
    }
    if (!animationsElement.isJsonArray()) {
      throw new EasyModelDecodeException("Field animations must be an array.");
    }
    if (animationsElement.getAsJsonArray().size() > MAX_ANIMATION_COUNT) {
      throw new EasyModelDecodeException(
          "Animation count exceeds the reserved limit of " + MAX_ANIMATION_COUNT + ".");
    }
  }

  private static void validateSourceBudgets(int boneCount, int cubeCount)
      throws EasyModelDecodeException {
    if (boneCount > MAX_BONE_COUNT) {
      throw new EasyModelDecodeException("Bone count exceeds " + MAX_BONE_COUNT + ".");
    }
    if (cubeCount > MAX_CUBE_COUNT) {
      throw new EasyModelDecodeException("Cube count exceeds " + MAX_CUBE_COUNT + ".");
    }
  }

  private static Map<String, RawElement> parseElements(JsonArray elementsArray)
      throws EasyModelDecodeException {
    Map<String, RawElement> elementsByUuid = new LinkedHashMap<>();
    for (JsonElement element : elementsArray) {
      JsonObject elementObject = requireObjectElement(element, "elements");
      String type = optionalString(elementObject, "type", "cube");
      if (!"cube".equals(type)) {
        throw new EasyModelDecodeException("Unsupported Blockbench element type " + type + ".");
      }
      float[] from = requiredFloatArray(elementObject, "from");
      float[] to = requiredFloatArray(elementObject, "to");
      float[] dimensions = new float[] {to[0] - from[0], to[1] - from[1], to[2] - from[2]};
      int[] uvOffset = optionalIntArray(elementObject, "uv_offset", new int[] {0, 0});
      RawElement rawElement =
          new RawElement(
              requiredString(elementObject, "uuid"),
              requiredString(elementObject, "name"),
              from,
              to,
              uvOffset,
              parseFaceUvs(elementObject, uvOffset, dimensions),
              optionalFloatArray(elementObject, "origin", new float[] {0.0f, 0.0f, 0.0f}),
              optionalFloatArray(elementObject, "rotation", new float[] {0.0f, 0.0f, 0.0f}),
              optionalBoolean(elementObject, "mirror_uv", false));
      if (elementsByUuid.put(rawElement.uuid(), rawElement) != null) {
        throw new EasyModelDecodeException("Duplicate element uuid " + rawElement.uuid() + ".");
      }
    }

    return elementsByUuid;
  }

  private static ModelCubeFaceUvs parseFaceUvs(
      JsonObject elementObject, int[] uvOffset, float[] dimensions)
      throws EasyModelDecodeException {
    ModelCubeFaceUvs boxUvs = ModelCubeFaceUvs.fromBoxUv(uvOffset, dimensions);
    JsonElement facesElement = elementObject.get("faces");
    if (facesElement == null || facesElement.isJsonNull()) {
      return boxUvs;
    }
    if (!facesElement.isJsonObject()) {
      throw new EasyModelDecodeException("Field faces must be an object.");
    }

    JsonObject faces = facesElement.getAsJsonObject();
    return new ModelCubeFaceUvs(
        faceUv(faces, "north", boxUvs.north()),
        faceUv(faces, "east", boxUvs.east()),
        faceUv(faces, "south", boxUvs.south()),
        faceUv(faces, "west", boxUvs.west()),
        faceUv(faces, "up", boxUvs.up()),
        faceUv(faces, "down", boxUvs.down()));
  }

  private static float[] faceUv(JsonObject faces, String face, float[] fallback)
      throws EasyModelDecodeException {
    JsonElement faceElement = faces.get(face);
    if (faceElement == null || faceElement.isJsonNull()) {
      return fallback;
    }
    if (!faceElement.isJsonObject()) {
      throw new EasyModelDecodeException("Face " + face + " must be an object.");
    }

    JsonElement uvElement = faceElement.getAsJsonObject().get("uv");
    return uvElement == null || uvElement.isJsonNull()
        ? fallback
        : parseFloatArray(uvElement, "faces." + face + ".uv", 4);
  }

  private static Map<String, RawGroup> parseGroups(JsonArray groupsArray)
      throws EasyModelDecodeException {
    Map<String, RawGroup> groupsByUuid = new HashMap<>();
    for (JsonElement group : groupsArray) {
      JsonObject groupObject = requireObjectElement(group, "groups");
      RawGroup rawGroup =
          new RawGroup(
              requiredString(groupObject, "uuid"),
              requiredString(groupObject, "name"),
              optionalFloatArray(groupObject, "origin", new float[] {0.0f, 0.0f, 0.0f}),
              optionalFloatArray(groupObject, "rotation", new float[] {0.0f, 0.0f, 0.0f}));
      if (groupsByUuid.put(rawGroup.uuid(), rawGroup) != null) {
        throw new EasyModelDecodeException("Duplicate group uuid " + rawGroup.uuid() + ".");
      }
    }

    return groupsByUuid;
  }

  private static DecodedModelPart convertNode(
      JsonObject node,
      Map<String, RawElement> elementsByUuid,
      Map<String, RawGroup> groupsByUuid,
      RawGroup parentGroup,
      int depth,
      Set<String> groupPath)
      throws EasyModelDecodeException {
    if (depth > MAX_HIERARCHY_DEPTH) {
      throw new EasyModelDecodeException("Hierarchy depth exceeds " + MAX_HIERARCHY_DEPTH + ".");
    }

    String groupUuid = requiredString(node, "uuid");
    RawGroup group = groupsByUuid.get(groupUuid);
    if (group == null) {
      throw new EasyModelDecodeException(
          "Outliner references unknown group uuid " + groupUuid + ".");
    }
    if (!groupPath.add(groupUuid)) {
      throw new EasyModelDecodeException(
          "Cyclic bone hierarchy detected at group " + group.name() + ".");
    }

    List<DecodedModelCube> cubes = new ArrayList<>();
    List<DecodedModelPart> children = new ArrayList<>();
    JsonElement childrenElement = node.get("children");
    if (childrenElement != null && !childrenElement.isJsonNull()) {
      if (!childrenElement.isJsonArray()) {
        throw new EasyModelDecodeException("Outliner children must be an array.");
      }
      for (JsonElement childElement : childrenElement.getAsJsonArray()) {
        if (childElement.isJsonPrimitive() && childElement.getAsJsonPrimitive().isString()) {
          RawElement rawElement = elementsByUuid.get(childElement.getAsString());
          if (rawElement == null) {
            throw new EasyModelDecodeException(
                "Outliner references unknown element uuid " + childElement.getAsString() + ".");
          }
          cubes.add(convertCube(rawElement, group));
        } else if (childElement.isJsonObject()) {
          children.add(
              convertNode(
                  childElement.getAsJsonObject(),
                  elementsByUuid,
                  groupsByUuid,
                  group,
                  depth + 1,
                  groupPath));
        } else {
          throw new EasyModelDecodeException(
              "Outliner children must be group objects or element ids.");
        }
      }
    }
    groupPath.remove(groupUuid);

    return new DecodedModelPart(
        group.name(),
        groupOffset(group, parentGroup),
        rotationRadians(group.rotation()),
        cubes,
        children);
  }

  private static DecodedModelCube convertCube(RawElement element, RawGroup group) {
    float[] from = element.from();
    float[] to = element.to();
    float[] groupOrigin = group.origin();
    float[] elementOrigin = element.origin();
    return new DecodedModelCube(
        element.uvOffset(),
        element.faceUvs(),
        new float[] {from[0] - groupOrigin[0], -(to[1] - groupOrigin[1]), from[2] - groupOrigin[2]},
        new float[] {to[0] - from[0], to[1] - from[1], to[2] - from[2]},
        element.mirrorUv(),
        element.name(),
        new float[] {
          -(elementOrigin[0] - groupOrigin[0]),
          -(elementOrigin[1] - groupOrigin[1]),
          elementOrigin[2] - groupOrigin[2]
        },
        elementRotationRadians(element.rotation()),
        new float[] {
          from[0] - elementOrigin[0], -(to[1] - elementOrigin[1]), from[2] - elementOrigin[2]
        });
  }

  private static float[] groupOffset(RawGroup group, RawGroup parentGroup) {
    float[] groupOrigin = group.origin();
    if (parentGroup == null) {
      return new float[] {groupOrigin[0], 24.0f - groupOrigin[1], groupOrigin[2]};
    }

    float[] parentOrigin = parentGroup.origin();
    return new float[] {
      groupOrigin[0] - parentOrigin[0],
      parentOrigin[1] - groupOrigin[1],
      groupOrigin[2] - parentOrigin[2]
    };
  }

  private static float[] rotationRadians(float[] rotation) {
    return new float[] {
      (float) Math.toRadians(rotation[0]),
      (float) Math.toRadians(rotation[1]),
      (float) Math.toRadians(rotation[2])
    };
  }

  private static float[] elementRotationRadians(float[] rotation) {
    float yRotation = (float) Math.toRadians(rotation[1]);
    if (Math.abs(yRotation + Math.PI) < 0.01f) {
      yRotation = (float) Math.PI;
    }

    return new float[] {
      -(float) Math.toRadians(rotation[0]), yRotation, (float) Math.toRadians(rotation[2])
    };
  }

  private static void addSoftBudgetWarnings(
      DecodedModel decodedModel, List<ModelRenderProfileValidationIssue> issues) {
    if (decodedModel.boneCount() > SOFT_BONE_COUNT) {
      issues.add(
          warning(
              "model",
              "Bone count "
                  + decodedModel.boneCount()
                  + " is above the recommended limit of "
                  + SOFT_BONE_COUNT
                  + "."));
    }
    if (decodedModel.cubeCount() > SOFT_CUBE_COUNT) {
      issues.add(
          warning(
              "model",
              "Cube count "
                  + decodedModel.cubeCount()
                  + " is above the recommended limit of "
                  + SOFT_CUBE_COUNT
                  + "."));
    }
    if (decodedModel.hierarchyDepth() > SOFT_HIERARCHY_DEPTH) {
      issues.add(
          warning(
              "model",
              "Hierarchy depth "
                  + decodedModel.hierarchyDepth()
                  + " is above the recommended limit of "
                  + SOFT_HIERARCHY_DEPTH
                  + "."));
    }
  }

  private static ModelRenderProfileValidationIssue warning(String field, String message) {
    return new ModelRenderProfileValidationIssue(ModelRenderProfileStatus.ACTIVE, field, message);
  }

  private static JsonObject requiredObject(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonObject()) {
      throw new EasyModelDecodeException("Field " + field + " must be an object.");
    }

    return value.getAsJsonObject();
  }

  private static JsonArray requiredArray(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonArray()) {
      throw new EasyModelDecodeException("Field " + field + " must be an array.");
    }

    return value.getAsJsonArray();
  }

  private static JsonObject requireObjectElement(JsonElement element, String field)
      throws EasyModelDecodeException {
    if (!element.isJsonObject()) {
      throw new EasyModelDecodeException("Elements of " + field + " must be objects.");
    }

    return element.getAsJsonObject();
  }

  private static String requiredString(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      throw new EasyModelDecodeException("Field " + field + " must be a string.");
    }
    String stringValue = value.getAsString();
    if (stringValue.isBlank()) {
      throw new EasyModelDecodeException("Field " + field + " must not be blank.");
    }

    return stringValue;
  }

  private static String optionalString(JsonObject jsonObject, String field, String defaultValue)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      throw new EasyModelDecodeException("Field " + field + " must be a string.");
    }

    return value.getAsString();
  }

  private static int requiredPositiveInt(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new EasyModelDecodeException("Field " + field + " must be a number.");
    }
    int intValue = value.getAsInt();
    if (intValue <= 0) {
      throw new EasyModelDecodeException("Field " + field + " must be positive.");
    }

    return intValue;
  }

  private static float[] requiredFloatArray(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null) {
      throw new EasyModelDecodeException("Missing required field " + field + ".");
    }

    return parseFloatArray(value, field);
  }

  private static float[] optionalFloatArray(
      JsonObject jsonObject, String field, float[] defaultValue) throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    return value == null || value.isJsonNull() ? defaultValue : parseFloatArray(value, field);
  }

  private static float[] parseFloatArray(JsonElement value, String field)
      throws EasyModelDecodeException {
    return parseFloatArray(value, field, 3);
  }

  private static float[] parseFloatArray(JsonElement value, String field, int expectedSize)
      throws EasyModelDecodeException {
    if (!value.isJsonArray() || value.getAsJsonArray().size() != expectedSize) {
      throw new EasyModelDecodeException(
          "Field " + field + " must be an array with " + expectedSize + " numbers.");
    }
    float[] values = new float[expectedSize];
    for (int index = 0; index < values.length; index++) {
      JsonElement element = value.getAsJsonArray().get(index);
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
        throw new EasyModelDecodeException("Field " + field + " must contain only numbers.");
      }
      values[index] = element.getAsFloat();
      validateModelNumber(values[index], field);
    }

    return values;
  }

  private static int[] optionalIntArray(JsonObject jsonObject, String field, int[] defaultValue)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonArray() || value.getAsJsonArray().size() != 2) {
      throw new EasyModelDecodeException("Field " + field + " must be an array with 2 numbers.");
    }
    int[] values = new int[2];
    for (int index = 0; index < values.length; index++) {
      JsonElement element = value.getAsJsonArray().get(index);
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
        throw new EasyModelDecodeException("Field " + field + " must contain only numbers.");
      }
      values[index] = element.getAsInt();
    }

    return values;
  }

  private static boolean optionalBoolean(JsonObject jsonObject, String field, boolean defaultValue)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
      throw new EasyModelDecodeException("Field " + field + " must be a boolean.");
    }

    return value.getAsBoolean();
  }

  private static void validateModelNumber(float value, String field)
      throws EasyModelDecodeException {
    if (!Float.isFinite(value)) {
      throw new EasyModelDecodeException("Field " + field + " must contain finite values.");
    }
    if (Math.abs(value) > MAX_ABSOLUTE_MODEL_VALUE) {
      throw new EasyModelDecodeException("Field " + field + " contains an absurdly large value.");
    }
  }

  @Override
  public boolean supports(ResourceLocation modelId, Resource resource) {
    return modelId != null && resource != null;
  }

  @Override
  public DecodedModel decode(ResourceLocation modelId, Resource resource)
      throws EasyModelDecodeException {
    Objects.requireNonNull(modelId, "modelId");
    Objects.requireNonNull(resource, "resource");

    byte[] modelBytes = readModelBytes(resource);
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    if (modelBytes.length > SOFT_MODEL_FILE_SIZE_BYTES) {
      issues.add(
          warning(
              "model",
              "Model file "
                  + modelId
                  + " is larger than 1 MB and should be optimized for runtime use."));
    }

    JsonObject root = parseRoot(modelId, modelBytes);
    validateMeta(root);
    int[] resolution = parseResolution(root);
    if (resolution[0] > MAX_TEXTURE_SIZE || resolution[1] > MAX_TEXTURE_SIZE) {
      throw new EasyModelDecodeException(
          "Model texture resolution "
              + resolution[0]
              + "x"
              + resolution[1]
              + " exceeds "
              + MAX_TEXTURE_SIZE
              + "x"
              + MAX_TEXTURE_SIZE
              + ".");
    }
    if (resolution[0] > SOFT_TEXTURE_SIZE || resolution[1] > SOFT_TEXTURE_SIZE) {
      issues.add(
          warning(
              "texture",
              "Model texture resolution "
                  + resolution[0]
                  + "x"
                  + resolution[1]
                  + " is larger than the recommended 64x64 or 128x128 size."));
    }

    JsonArray elementsArray = requiredArray(root, "elements");
    JsonArray groupsArray = requiredArray(root, "groups");
    JsonArray outlinerArray = requiredArray(root, "outliner");
    validateAnimationBudget(root);
    validateSourceBudgets(groupsArray.size(), elementsArray.size());

    Map<String, RawElement> elementsByUuid = parseElements(elementsArray);
    Map<String, RawGroup> groupsByUuid = parseGroups(groupsArray);
    List<DecodedModelPart> rootParts = new ArrayList<>();
    for (JsonElement outlinerElement : outlinerArray) {
      if (!outlinerElement.isJsonObject()) {
        throw new EasyModelDecodeException("Root outliner entries must be group objects.");
      }
      rootParts.add(
          convertNode(
              outlinerElement.getAsJsonObject(),
              elementsByUuid,
              groupsByUuid,
              null,
              1,
              new HashSet<>()));
    }

    DecodedModel decodedModel =
        new DecodedModel(modelId, resolution[0], resolution[1], rootParts, issues);
    addSoftBudgetWarnings(decodedModel, issues);
    return new DecodedModel(modelId, resolution[0], resolution[1], rootParts, issues);
  }

  private record RawElement(
      String uuid,
      String name,
      float[] from,
      float[] to,
      int[] uvOffset,
      ModelCubeFaceUvs faceUvs,
      float[] origin,
      float[] rotation,
      boolean mirrorUv) {}

  private record RawGroup(String uuid, String name, float[] origin, float[] rotation) {}
}

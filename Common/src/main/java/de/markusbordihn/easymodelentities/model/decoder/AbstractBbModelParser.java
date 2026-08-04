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

package de.markusbordihn.easymodelentities.model.decoder;

import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_ANIMATION_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_BONE_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_CUBE_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_HIERARCHY_DEPTH;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_MODEL_FILE_SIZE_BYTES;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_TEXTURE_SIZE;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_BONE_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_CUBE_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_HIERARCHY_DEPTH;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_MODEL_FILE_SIZE_BYTES;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_TEXTURE_SIZE;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.FaceUv;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClips;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationKeyframe;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
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
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public abstract class AbstractBbModelParser {

  private static final float MAX_ABSOLUTE_MODEL_VALUE = 100_000.0f;
  private static final int BUFFER_SIZE = 8192;

  protected static byte[] readModelBytes(Resource resource) throws EasyModelDecodeException {
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

  protected static JsonObject parseRoot(ResourceLocation modelId, byte[] modelBytes)
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

  protected static String modelFormat(JsonObject root) throws EasyModelDecodeException {
    JsonObject meta = requiredObject(root, "meta");
    String formatVersion = requiredString(meta, "format_version");
    if (!formatVersion.startsWith("5.")) {
      throw new EasyModelDecodeException(
          "Unsupported Blockbench format_version " + formatVersion + ".");
    }

    return requiredString(meta, "model_format");
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

  private static Map<String, ModelAnimationClip> parseAnimations(
      JsonObject root,
      Map<String, RawGroup> groupsByUuid,
      List<ModelRenderProfileValidationIssue> issues) {
    JsonElement animationsElement = root.get("animations");
    if (animationsElement == null || !animationsElement.isJsonArray()) {
      return Map.of();
    }

    Map<String, ModelAnimationClip> clips = new LinkedHashMap<>();
    List<String> customNames = new ArrayList<>();
    for (JsonElement animationElement : animationsElement.getAsJsonArray()) {
      if (!animationElement.isJsonObject()) {
        continue;
      }
      JsonObject animationObject = animationElement.getAsJsonObject();
      String clipName;
      try {
        clipName = ModelAnimationClips.normalize(optionalString(animationObject, "name", ""));
      } catch (EasyModelDecodeException exception) {
        continue;
      }
      if (clipName.isEmpty()) {
        continue;
      }
      boolean customName = !ModelAnimationClips.STANDARD_NAMES.contains(clipName);
      try {
        ModelAnimationClip clip = parseAnimationClip(clipName, animationObject, groupsByUuid);
        if (clip != null) {
          clips.put(clipName, clip);
          if (customName) {
            customNames.add(clipName);
          }
        }
      } catch (EasyModelDecodeException exception) {
        issues.add(
            warning(
                "animations",
                "Ignoring animation clip " + clipName + ": " + exception.getMessage()));
      }
    }

    if (!customNames.isEmpty()) {
      issues.add(
          warning(
              "animations",
              "Kept custom keyframe animation(s) "
                  + String.join(", ", customNames)
                  + ": only the standard clips "
                  + String.join(", ", ModelAnimationClips.STANDARD)
                  + " are played automatically; custom clips are played by name via the API."));
    }

    return clips;
  }

  private static ModelAnimationClip parseAnimationClip(
      String clipName, JsonObject animationObject, Map<String, RawGroup> groupsByUuid)
      throws EasyModelDecodeException {
    boolean loop = "loop".equals(optionalString(animationObject, "loop", "once"));
    float length = optionalNonNegativeFloat(animationObject, "length", 0.0f);
    float framesPerSecond = optionalNonNegativeFloat(animationObject, "snapping", 0.0f);
    JsonElement animatorsElement = animationObject.get("animators");
    if (animatorsElement == null || !animatorsElement.isJsonObject()) {
      return null;
    }

    Map<String, ModelAnimationBoneTrack> boneTracks = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> animatorEntry :
        animatorsElement.getAsJsonObject().entrySet()) {
      if (!animatorEntry.getValue().isJsonObject()) {
        continue;
      }
      JsonObject animatorObject = animatorEntry.getValue().getAsJsonObject();
      if (!"bone".equals(optionalString(animatorObject, "type", "bone"))) {
        continue;
      }
      RawGroup group = groupsByUuid.get(animatorEntry.getKey());
      String boneName =
          ModelAnimationClips.normalize(
              group != null ? group.name() : optionalString(animatorObject, "name", ""));
      if (boneName.isEmpty()) {
        continue;
      }
      ModelAnimationBoneTrack boneTrack = parseBoneTrack(animatorObject);
      if (boneTrack != null && !boneTrack.isEmpty()) {
        boneTracks.put(boneName, boneTrack);
      }
    }

    if (boneTracks.isEmpty()) {
      return null;
    }
    if (length <= 0.0f) {
      length =
          boneTracks.values().stream()
              .map(ModelAnimationBoneTrack::lastKeyframeTime)
              .reduce(0.0f, Math::max);
    }
    return new ModelAnimationClip(clipName, length, loop, framesPerSecond, boneTracks);
  }

  private static ModelAnimationBoneTrack parseBoneTrack(JsonObject animatorObject)
      throws EasyModelDecodeException {
    JsonElement keyframesElement = animatorObject.get("keyframes");
    if (keyframesElement == null || !keyframesElement.isJsonArray()) {
      return null;
    }

    List<ModelAnimationKeyframe> rotationKeyframes = new ArrayList<>();
    List<ModelAnimationKeyframe> positionKeyframes = new ArrayList<>();
    for (JsonElement keyframeElement : keyframesElement.getAsJsonArray()) {
      JsonObject keyframeObject = requireObjectElement(keyframeElement, "keyframes");
      String channel = optionalString(keyframeObject, "channel", "");
      boolean rotationChannel = "rotation".equals(channel);
      if (!rotationChannel && !"position".equals(channel)) {
        continue;
      }
      float time = optionalNonNegativeFloat(keyframeObject, "time", 0.0f);
      boolean step = "step".equals(optionalString(keyframeObject, "interpolation", "linear"));
      Vec3f value = keyframeDataPoint(keyframeObject, rotationChannel);
      (rotationChannel ? rotationKeyframes : positionKeyframes)
          .add(new ModelAnimationKeyframe(time, value, step));
    }

    return new ModelAnimationBoneTrack(
        sortedKeyframes(rotationKeyframes), sortedKeyframes(positionKeyframes));
  }

  private static List<ModelAnimationKeyframe> sortedKeyframes(
      List<ModelAnimationKeyframe> keyframes) {
    keyframes.sort((left, right) -> Float.compare(left.time(), right.time()));
    return keyframes;
  }

  private static Vec3f keyframeDataPoint(JsonObject keyframeObject, boolean rotationChannel)
      throws EasyModelDecodeException {
    JsonElement dataPointsElement = keyframeObject.get("data_points");
    if (dataPointsElement == null
        || !dataPointsElement.isJsonArray()
        || dataPointsElement.getAsJsonArray().isEmpty()) {
      throw new EasyModelDecodeException("Keyframe is missing data_points.");
    }
    JsonElement dataPointElement = dataPointsElement.getAsJsonArray().get(0);
    if (!dataPointElement.isJsonObject()) {
      throw new EasyModelDecodeException("Keyframe data_points must contain objects.");
    }
    JsonObject dataPoint = dataPointElement.getAsJsonObject();
    float x = keyframeAxisValue(dataPoint, "x");
    float y = keyframeAxisValue(dataPoint, "y");
    float z = keyframeAxisValue(dataPoint, "z");
    if (rotationChannel) {
      return new Vec3f(
          -(float) Math.toRadians(x), -(float) Math.toRadians(y), (float) Math.toRadians(z));
    }
    return new Vec3f(-x, -y, z);
  }

  private static float keyframeAxisValue(JsonObject dataPoint, String axis)
      throws EasyModelDecodeException {
    JsonElement value = dataPoint.get(axis);
    if (value == null || value.isJsonNull()) {
      return 0.0f;
    }
    if (value.isJsonPrimitive()) {
      JsonPrimitive primitive = value.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        float floatValue = primitive.getAsFloat();
        validateModelNumber(floatValue, "data_points." + axis);
        return floatValue;
      }
      if (primitive.isString()) {
        String stringValue = primitive.getAsString().trim();
        if (stringValue.isEmpty()) {
          return 0.0f;
        }
        try {
          float floatValue = Float.parseFloat(stringValue);
          validateModelNumber(floatValue, "data_points." + axis);
          return floatValue;
        } catch (NumberFormatException exception) {
          throw new EasyModelDecodeException(
              "Keyframe expression \"" + stringValue + "\" is not supported.");
        }
      }
    }
    throw new EasyModelDecodeException("Keyframe axis " + axis + " must be a number.");
  }

  private static float optionalNonNegativeFloat(
      JsonObject jsonObject, String field, float defaultValue) throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new EasyModelDecodeException("Field " + field + " must be a number.");
    }
    float floatValue = value.getAsFloat();
    validateModelNumber(floatValue, field);
    return Math.max(floatValue, 0.0f);
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
        faceUv(faces, ModelCubeFace.NORTH.getTagName(), boxUvs.north()),
        faceUv(faces, ModelCubeFace.EAST.getTagName(), boxUvs.east()),
        faceUv(faces, ModelCubeFace.SOUTH.getTagName(), boxUvs.south()),
        faceUv(faces, ModelCubeFace.WEST.getTagName(), boxUvs.west()),
        faceUv(faces, ModelCubeFace.UP.getTagName(), boxUvs.up()),
        faceUv(faces, ModelCubeFace.DOWN.getTagName(), boxUvs.down()));
  }

  private static CubeFaceVisibility parseFaceVisibility(JsonObject elementObject)
      throws EasyModelDecodeException {
    JsonElement facesElement = elementObject.get("faces");
    if (facesElement == null || !facesElement.isJsonObject()) {
      return CubeFaceVisibility.ALL;
    }

    boolean perFaceUv = !optionalBoolean(elementObject, "box_uv", false);
    JsonObject faces = facesElement.getAsJsonObject();
    CubeFaceVisibility visibility = CubeFaceVisibility.ALL;
    for (ModelCubeFace face : ModelCubeFace.values()) {
      JsonElement faceElement = faces.get(face.getTagName());
      if (faceElement == null || !faceElement.isJsonObject()) {
        continue;
      }
      JsonObject faceObject = faceElement.getAsJsonObject();
      boolean hasTextureKey = faceObject.has("texture");
      boolean nullTexture = hasTextureKey && faceObject.get("texture").isJsonNull();
      if (nullTexture || (perFaceUv && !hasTextureKey)) {
        visibility = visibility.without(face);
      }
    }

    return visibility;
  }

  private static boolean hasResolvedFaceUvs(JsonObject elementObject) {
    JsonElement facesElement = elementObject.get("faces");
    if (facesElement == null || !facesElement.isJsonObject()) {
      return false;
    }
    for (Map.Entry<String, JsonElement> face : facesElement.getAsJsonObject().entrySet()) {
      JsonElement faceElement = face.getValue();
      if (faceElement.isJsonObject()) {
        JsonElement uvElement = faceElement.getAsJsonObject().get("uv");
        if (uvElement != null && !uvElement.isJsonNull()) {
          return true;
        }
      }
    }

    return false;
  }

  private static FaceUv faceUv(JsonObject faces, String face, FaceUv fallback)
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
        : FaceUv.of(parseFloatArray(uvElement, "faces." + face + ".uv", 4));
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

  private static String outlinerEntryName(
      JsonElement outlinerElement, Map<String, RawElement> elementsByUuid) {
    if (!outlinerElement.isJsonPrimitive() || !outlinerElement.getAsJsonPrimitive().isString()) {
      return String.valueOf(outlinerElement);
    }

    RawElement element = elementsByUuid.get(outlinerElement.getAsString());
    return element != null ? element.name() : outlinerElement.getAsString();
  }

  private static void warnAboutUnusedElements(
      Map<String, RawElement> elementsByUuid,
      Set<String> referencedElements,
      List<ModelRenderProfileValidationIssue> issues) {
    List<String> unusedNames = new ArrayList<>();
    for (RawElement element : elementsByUuid.values()) {
      if (!referencedElements.contains(element.uuid())) {
        unusedNames.add(element.name());
      }
    }
    if (unusedNames.isEmpty()) {
      return;
    }

    issues.add(
        warning(
            "model",
            "Ignoring "
                + unusedNames.size()
                + " cube(s) that are not part of any group in the outliner: "
                + String.join(", ", unusedNames)
                + "."));
  }

  private static DecodedModelPart convertNode(
      JsonObject node,
      Map<String, RawElement> elementsByUuid,
      Map<String, RawGroup> groupsByUuid,
      RawGroup parentGroup,
      int depth,
      Set<String> groupPath,
      Set<String> referencedElements)
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
          referencedElements.add(rawElement.uuid());
          cubes.add(convertCube(rawElement, group));
        } else if (childElement.isJsonObject()) {
          children.add(
              convertNode(
                  childElement.getAsJsonObject(),
                  elementsByUuid,
                  groupsByUuid,
                  group,
                  depth + 1,
                  groupPath,
                  referencedElements));
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
        new Vec3f(-(to[0] - groupOrigin[0]), -(to[1] - groupOrigin[1]), from[2] - groupOrigin[2]),
        new Vec3f(to[0] - from[0], to[1] - from[1], to[2] - from[2]),
        element.mirrorUv(),
        element.name(),
        new Vec3f(
            -(elementOrigin[0] - groupOrigin[0]),
            -(elementOrigin[1] - groupOrigin[1]),
            elementOrigin[2] - groupOrigin[2]),
        elementRotationRadians(element.rotation()),
        new Vec3f(
            -(to[0] - elementOrigin[0]), -(to[1] - elementOrigin[1]), from[2] - elementOrigin[2]),
        element.textureIndex(),
        element.faceVisibility());
  }

  private static Vec3f groupOffset(RawGroup group, RawGroup parentGroup) {
    float[] groupOrigin = group.origin();
    if (parentGroup == null) {
      return new Vec3f(-groupOrigin[0], 24.0f - groupOrigin[1], groupOrigin[2]);
    }

    float[] parentOrigin = parentGroup.origin();
    return new Vec3f(
        parentOrigin[0] - groupOrigin[0],
        parentOrigin[1] - groupOrigin[1],
        groupOrigin[2] - parentOrigin[2]);
  }

  private static Vec3f rotationRadians(float[] rotation) {
    float yRotation = -(float) Math.toRadians(rotation[1]);
    if (Math.abs(yRotation + Math.PI) < 0.01f) {
      yRotation = (float) Math.PI;
    }

    return new Vec3f(
        -(float) Math.toRadians(rotation[0]), yRotation, (float) Math.toRadians(rotation[2]));
  }

  private static Vec3f elementRotationRadians(float[] rotation) {
    return rotationRadians(rotation);
  }

  private static void addSoftBudgetWarnings(
      List<DecodedModelPart> rootParts, List<ModelRenderProfileValidationIssue> issues) {
    int boneCount = rootParts.stream().mapToInt(DecodedModelPart::partCount).sum();
    if (boneCount > SOFT_BONE_COUNT) {
      issues.add(
          warning(
              "model",
              "Bone count "
                  + boneCount
                  + " is above the recommended limit of "
                  + SOFT_BONE_COUNT
                  + "."));
    }
    int cubeCount = rootParts.stream().mapToInt(DecodedModelPart::cubeCount).sum();
    if (cubeCount > SOFT_CUBE_COUNT) {
      issues.add(
          warning(
              "model",
              "Cube count "
                  + cubeCount
                  + " is above the recommended limit of "
                  + SOFT_CUBE_COUNT
                  + "."));
    }
    int hierarchyDepth =
        rootParts.stream().mapToInt(DecodedModelPart::hierarchyDepth).max().orElse(0);
    if (hierarchyDepth > SOFT_HIERARCHY_DEPTH) {
      issues.add(
          warning(
              "model",
              "Hierarchy depth "
                  + hierarchyDepth
                  + " is above the recommended limit of "
                  + SOFT_HIERARCHY_DEPTH
                  + "."));
    }
  }

  protected static ModelRenderProfileValidationIssue warning(String field, String message) {
    return new ModelRenderProfileValidationIssue(ModelRenderProfileStatus.ACTIVE, field, message);
  }

  protected static JsonObject requiredObject(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonObject()) {
      throw new EasyModelDecodeException("Field " + field + " must be an object.");
    }

    return value.getAsJsonObject();
  }

  protected static JsonArray requiredArray(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || !value.isJsonArray()) {
      throw new EasyModelDecodeException("Field " + field + " must be an array.");
    }

    return value.getAsJsonArray();
  }

  protected static JsonObject requireObjectElement(JsonElement element, String field)
      throws EasyModelDecodeException {
    if (!element.isJsonObject()) {
      throw new EasyModelDecodeException("Elements of " + field + " must be objects.");
    }

    return element.getAsJsonObject();
  }

  protected static String requiredString(JsonObject jsonObject, String field)
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

  protected static String optionalString(JsonObject jsonObject, String field, String defaultValue)
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

  protected static int requiredPositiveInt(JsonObject jsonObject, String field)
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

  protected static int optionalNonNegativeInt(JsonObject jsonObject, String field, int defaultValue)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null || value.isJsonNull()) {
      return defaultValue;
    }
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      throw new EasyModelDecodeException("Field " + field + " must be a number.");
    }
    int intValue = value.getAsInt();
    if (intValue < 0) {
      throw new EasyModelDecodeException("Field " + field + " must not be negative.");
    }

    return intValue;
  }

  protected static float[] requiredFloatArray(JsonObject jsonObject, String field)
      throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    if (value == null) {
      throw new EasyModelDecodeException("Missing required field " + field + ".");
    }

    return parseFloatArray(value, field);
  }

  protected static float[] optionalFloatArray(
      JsonObject jsonObject, String field, float[] defaultValue) throws EasyModelDecodeException {
    JsonElement value = jsonObject.get(field);
    return value == null || value.isJsonNull() ? defaultValue : parseFloatArray(value, field);
  }

  protected static float[] parseFloatArray(JsonElement value, String field)
      throws EasyModelDecodeException {
    return parseFloatArray(value, field, 3);
  }

  protected static float[] parseFloatArray(JsonElement value, String field, int expectedSize)
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

  protected static int[] optionalIntArray(JsonObject jsonObject, String field, int[] defaultValue)
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

  protected static boolean optionalBoolean(
      JsonObject jsonObject, String field, boolean defaultValue) throws EasyModelDecodeException {
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

  private static int textureId(JsonObject textureObject, int positionalIndex)
      throws EasyModelDecodeException {
    String id = optionalString(textureObject, "id", "");
    if (id.isBlank()) {
      return positionalIndex;
    }
    try {
      return Math.max(Integer.parseInt(id.trim()), 0);
    } catch (NumberFormatException exception) {
      return positionalIndex;
    }
  }

  protected abstract int textureIndex(
      JsonObject elementObject, List<ModelRenderProfileValidationIssue> issues)
      throws EasyModelDecodeException;

  protected List<DecodedTexture> parseTextures(JsonObject root) throws EasyModelDecodeException {
    JsonElement texturesElement = root.get("textures");
    if (texturesElement == null || texturesElement.isJsonNull()) {
      return List.of();
    }
    if (!texturesElement.isJsonArray()) {
      throw new EasyModelDecodeException("Field textures must be an array.");
    }

    List<DecodedTexture> textures = new ArrayList<>();
    int positionalIndex = 0;
    for (JsonElement textureElement : texturesElement.getAsJsonArray()) {
      JsonObject textureObject = requireObjectElement(textureElement, "textures");
      textures.add(
          new DecodedTexture(
              textureId(textureObject, positionalIndex),
              optionalString(textureObject, "namespace", ""),
              optionalString(textureObject, "folder", ""),
              optionalString(textureObject, "relative_path", ""),
              optionalString(textureObject, "name", ""),
              optionalNonNegativeInt(textureObject, "uv_width", 0),
              optionalNonNegativeInt(textureObject, "uv_height", 0)));
      positionalIndex++;
    }

    return textures;
  }

  public final DecodedModel parse(ResourceLocation modelId, JsonObject root, int modelByteLength)
      throws EasyModelDecodeException {
    List<ModelRenderProfileValidationIssue> issues = new ArrayList<>();
    if (modelByteLength > SOFT_MODEL_FILE_SIZE_BYTES) {
      issues.add(
          warning(
              "model",
              "Model file "
                  + modelId
                  + " is larger than 1 MB and should be optimized for runtime use."));
    }

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

    Map<String, RawElement> elementsByUuid = parseElements(elementsArray, issues);
    Map<String, RawGroup> groupsByUuid = parseGroups(groupsArray);
    Map<String, ModelAnimationClip> animations = parseAnimations(root, groupsByUuid, issues);
    List<DecodedModelPart> rootParts = new ArrayList<>();
    Set<String> referencedElements = new HashSet<>();
    for (JsonElement outlinerElement : outlinerArray) {
      if (!outlinerElement.isJsonObject()) {
        throw new EasyModelDecodeException(
            "Outliner entry "
                + outlinerEntryName(outlinerElement, elementsByUuid)
                + " is not a group: every cube must be placed inside a group (bone).");
      }
      rootParts.add(
          convertNode(
              outlinerElement.getAsJsonObject(),
              elementsByUuid,
              groupsByUuid,
              null,
              1,
              new HashSet<>(),
              referencedElements));
    }
    warnAboutUnusedElements(elementsByUuid, referencedElements, issues);

    List<DecodedTexture> textures = parseTextures(root);
    addSoftBudgetWarnings(rootParts, issues);
    return new DecodedModel(
        modelId, resolution[0], resolution[1], rootParts, textures, animations, issues);
  }

  private Map<String, RawElement> parseElements(
      JsonArray elementsArray, List<ModelRenderProfileValidationIssue> issues)
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
      boolean mirror =
          optionalBoolean(elementObject, "mirror_uv", false) && !hasResolvedFaceUvs(elementObject);
      RawElement rawElement =
          new RawElement(
              requiredString(elementObject, "uuid"),
              requiredString(elementObject, "name"),
              from,
              to,
              uvOffset,
              parseFaceUvs(elementObject, uvOffset, dimensions),
              textureIndex(elementObject, issues),
              optionalFloatArray(elementObject, "origin", new float[] {0.0f, 0.0f, 0.0f}),
              optionalFloatArray(elementObject, "rotation", new float[] {0.0f, 0.0f, 0.0f}),
              mirror,
              parseFaceVisibility(elementObject));
      if (elementsByUuid.put(rawElement.uuid(), rawElement) != null) {
        throw new EasyModelDecodeException("Duplicate element uuid " + rawElement.uuid() + ".");
      }
    }

    return elementsByUuid;
  }

  private record RawElement(
      String uuid,
      String name,
      float[] from,
      float[] to,
      int[] uvOffset,
      ModelCubeFaceUvs faceUvs,
      int textureIndex,
      float[] origin,
      float[] rotation,
      boolean mirrorUv,
      CubeFaceVisibility faceVisibility) {}

  private record RawGroup(String uuid, String name, float[] origin, float[] rotation) {}
}

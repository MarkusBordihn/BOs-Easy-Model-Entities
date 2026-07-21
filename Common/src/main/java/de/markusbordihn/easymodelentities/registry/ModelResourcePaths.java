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

package de.markusbordihn.easymodelentities.registry;

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public final class ModelResourcePaths {

  public static final String SERVER_PROFILE_DIRECTORY = Constants.MOD_ID + "/profiles";
  public static final String ENTITY_SERVER_PROFILE_DIRECTORY = SERVER_PROFILE_DIRECTORY + "/entity";
  public static final String BLOCK_ENTITY_SERVER_PROFILE_DIRECTORY =
      SERVER_PROFILE_DIRECTORY + "/block_entity";
  public static final String RENDER_PROFILE_DIRECTORY = Constants.MOD_ID + "/render_profiles";
  public static final String MODEL_DIRECTORY = Constants.MOD_ID + "/models";
  public static final String TEXTURE_ENTITY_DIRECTORY = "textures/entity";
  private static final String ASSETS_ROOT = "assets";
  private static final String DATA_ROOT = "data";

  private ModelResourcePaths() {}

  public static String serverProfilePath(ResourceLocation profileId) {
    return dataPath(
        profileId,
        pathWithExtension(SERVER_PROFILE_DIRECTORY, profileId, ResourceFileExtension.JSON));
  }

  public static String entityServerProfilePath(ResourceLocation profileId) {
    return dataPath(
        profileId,
        pathWithExtension(ENTITY_SERVER_PROFILE_DIRECTORY, profileId, ResourceFileExtension.JSON));
  }

  public static String blockEntityServerProfilePath(ResourceLocation profileId) {
    return dataPath(
        profileId,
        pathWithExtension(
            BLOCK_ENTITY_SERVER_PROFILE_DIRECTORY, profileId, ResourceFileExtension.JSON));
  }

  public static String renderProfilePath(ResourceLocation renderProfileId) {
    return assetPath(
        renderProfileId,
        pathWithExtension(RENDER_PROFILE_DIRECTORY, renderProfileId, ResourceFileExtension.JSON));
  }

  public static String modelPath(ResourceLocation modelId) {
    return assetPath(modelId, withExtension(modelId.getPath(), ResourceFileExtension.BBMODEL));
  }

  public static ResourceLocation modelResourceLocation(ResourceLocation modelId) {
    return modelResourceLocation(modelId, ResourceFileExtension.BBMODEL.getExtension());
  }

  public static ResourceLocation modelResourceLocation(ResourceLocation modelId, String extension) {
    Objects.requireNonNull(modelId, "modelId");
    return ResourceLocation.fromNamespaceAndPath(
        modelId.getNamespace(), withExtension(modelId.getPath(), extension));
  }

  public static String texturePath(ResourceLocation textureId) {
    return assetPath(textureId, textureId.getPath());
  }

  public static ResourceLocation textureResourceLocation(ResourceLocation textureId) {
    return Objects.requireNonNull(textureId, "textureId");
  }

  public static ResourceLocation defaultModelId(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return ResourceLocation.fromNamespaceAndPath(
        profileId.getNamespace(),
        joinPath(MODEL_DIRECTORY, profileNameWithoutModelType(profileId)));
  }

  public static ResourceLocation defaultTextureId(ResourceLocation profileId) {
    Objects.requireNonNull(profileId, "profileId");
    return ResourceLocation.fromNamespaceAndPath(
        profileId.getNamespace(),
        joinPath(
            TEXTURE_ENTITY_DIRECTORY,
            withExtension(profileNameWithoutModelType(profileId), ResourceFileExtension.PNG)));
  }

  private static String profileNameWithoutModelType(ResourceLocation profileId) {
    String path = profileId.getPath();
    for (ModelType modelType : ModelType.values()) {
      String modelTypePrefix = modelType.getSerializedName() + "/";
      if (path.startsWith(modelTypePrefix)) {
        return path.substring(modelTypePrefix.length());
      }
    }

    return path;
  }

  private static String assetPath(ResourceLocation resourceLocation, String path) {
    return namespacedPath(ASSETS_ROOT, resourceLocation, path);
  }

  private static String dataPath(ResourceLocation resourceLocation, String path) {
    return namespacedPath(DATA_ROOT, resourceLocation, path);
  }

  private static String namespacedPath(
      String root, ResourceLocation resourceLocation, String path) {
    Objects.requireNonNull(resourceLocation, "resourceLocation");
    return joinPath(root, resourceLocation.getNamespace(), path);
  }

  private static String pathWithExtension(
      String directory, ResourceLocation resourceLocation, ResourceFileExtension fileExtension) {
    Objects.requireNonNull(resourceLocation, "resourceLocation");
    return joinPath(directory, withExtension(resourceLocation.getPath(), fileExtension));
  }

  private static String withExtension(String path, ResourceFileExtension fileExtension) {
    return withExtension(path, fileExtension.getExtension());
  }

  private static String withExtension(String path, String extension) {
    Objects.requireNonNull(extension, "extension");
    extension = extension.startsWith(".") ? extension : "." + extension;
    requireSafeRelativePath(extension.substring(1));
    return path.endsWith(extension) ? path : path + extension;
  }

  private static String joinPath(String... paths) {
    for (String path : paths) {
      requireSafeRelativePath(path);
    }

    return String.join("/", paths);
  }

  private static String requireSafeRelativePath(String path) {
    Objects.requireNonNull(path, "path");
    if (path.isBlank()
        || path.startsWith("/")
        || path.contains("\\")
        || !isSafeFileSystemPath(path)) {
      throw new IllegalArgumentException("Invalid resource path: " + path);
    }

    return path;
  }

  private static boolean isSafeFileSystemPath(String path) {
    Path fileSystemPath;
    try {
      fileSystemPath = Path.of(path);
    } catch (InvalidPathException exception) {
      return false;
    }

    if (fileSystemPath.isAbsolute() || !fileSystemPath.equals(fileSystemPath.normalize())) {
      return false;
    }

    for (Path pathSegment : fileSystemPath) {
      if (pathSegment.toString().isBlank()) {
        return false;
      }
    }

    return true;
  }
}

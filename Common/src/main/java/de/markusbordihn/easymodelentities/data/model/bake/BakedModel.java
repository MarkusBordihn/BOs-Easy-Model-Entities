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

package de.markusbordihn.easymodelentities.data.model.bake;

import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationVariants;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record BakedModel(
    ResourceLocation modelId,
    int textureWidth,
    int textureHeight,
    List<BakedModelPart> rootParts,
    Map<Integer, ResourceLocation> textures,
    boolean cullBackfaces,
    Map<String, ModelAnimationClip> animations,
    ModelBounds bounds,
    ModelAnimationVariants animationVariants,
    Map<String, Integer> textureNames) {

  public BakedModel {
    Objects.requireNonNull(modelId, "modelId");
    if (textureWidth <= 0 || textureHeight <= 0) {
      throw new IllegalArgumentException("Texture dimensions must be positive.");
    }
    rootParts = List.copyOf(Objects.requireNonNull(rootParts, "rootParts"));
    textures = Map.copyOf(Objects.requireNonNull(textures, "textures"));
    animations = Map.copyOf(Objects.requireNonNull(animations, "animations"));
    bounds = bounds == null ? ModelBounds.EMPTY : bounds;
    animationVariants =
        animationVariants == null ? ModelAnimationVariants.of(animations) : animationVariants;
    textureNames = textureNames == null ? Map.of() : Map.copyOf(textureNames);
  }

  public BakedModel(
      ResourceLocation modelId,
      int textureWidth,
      int textureHeight,
      List<BakedModelPart> rootParts,
      Map<Integer, ResourceLocation> textures,
      boolean cullBackfaces,
      Map<String, ModelAnimationClip> animations,
      ModelBounds bounds,
      Map<String, Integer> textureNames) {
    this(
        modelId,
        textureWidth,
        textureHeight,
        rootParts,
        textures,
        cullBackfaces,
        animations,
        bounds,
        null,
        textureNames);
  }

  public BakedModel(
      ResourceLocation modelId,
      int textureWidth,
      int textureHeight,
      List<BakedModelPart> rootParts,
      Map<Integer, ResourceLocation> textures,
      boolean cullBackfaces,
      Map<String, ModelAnimationClip> animations,
      ModelBounds bounds) {
    this(
        modelId,
        textureWidth,
        textureHeight,
        rootParts,
        textures,
        cullBackfaces,
        animations,
        bounds,
        Map.of());
  }

  public BakedModel(
      ResourceLocation modelId,
      int textureWidth,
      int textureHeight,
      List<BakedModelPart> rootParts,
      Map<Integer, ResourceLocation> textures,
      boolean cullBackfaces,
      ModelBounds bounds) {
    this(
        modelId, textureWidth, textureHeight, rootParts, textures, cullBackfaces, Map.of(), bounds);
  }

  public BakedModel(
      ResourceLocation modelId,
      int textureWidth,
      int textureHeight,
      List<BakedModelPart> rootParts,
      Map<Integer, ResourceLocation> textures,
      boolean cullBackfaces) {
    this(
        modelId,
        textureWidth,
        textureHeight,
        rootParts,
        textures,
        cullBackfaces,
        Map.of(),
        ModelBounds.EMPTY);
  }

  public BakedModel(
      ResourceLocation modelId,
      int textureWidth,
      int textureHeight,
      List<BakedModelPart> rootParts) {
    this(modelId, textureWidth, textureHeight, rootParts, Map.of(), false);
  }

  public int partCount() {
    return rootParts.stream().mapToInt(BakedModelPart::partCount).sum();
  }

  public int cubeCount() {
    return rootParts.stream().mapToInt(BakedModelPart::cubeCount).sum();
  }
}

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

package de.markusbordihn.easymodelentities.client.render;

import de.markusbordihn.easymodelentities.api.EasyModelReloadEvents;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelVec3f;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelItemAnchor;
import de.markusbordihn.easymodelentities.data.EasyModelApiMapper;
import de.markusbordihn.easymodelentities.data.model.ModelPartType;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModel;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public final class EasyModelItemAnchorResolver {

  private static final Map<AnchorCacheKey, Optional<EasyModelItemAnchor>> PROFILE_ANCHOR_CACHE =
      new ConcurrentHashMap<>();
  private static final Map<AnchorCacheKey, Optional<EasyModelItemAnchor>> MODEL_ANCHOR_CACHE =
      new ConcurrentHashMap<>();

  static {
    EasyModelReloadEvents.onProfileReload(EasyModelItemAnchorResolver::clearCaches);
    EasyModelReloadEvents.onRenderProfileReload(EasyModelItemAnchorResolver::clearCaches);
  }

  private EasyModelItemAnchorResolver() {}

  public static Optional<EasyModelItemAnchor> getItemAnchor(Identifier profileId, HumanoidArm arm) {
    AnchorCacheKey cacheKey = new AnchorCacheKey(profileId, arm);
    return PROFILE_ANCHOR_CACHE.computeIfAbsent(
        cacheKey, key -> resolve(key.profileOrModelId(), key.arm()));
  }

  public static Optional<EasyModelItemAnchor> getItemAnchor(BakedModel bakedModel, HumanoidArm arm) {
    AnchorCacheKey cacheKey = new AnchorCacheKey(bakedModel.modelId(), arm);
    return MODEL_ANCHOR_CACHE.computeIfAbsent(
        cacheKey, key -> resolve(bakedModel.rootParts(), key.arm()));
  }

  private static void clearCaches() {
    PROFILE_ANCHOR_CACHE.clear();
    MODEL_ANCHOR_CACHE.clear();
  }

  private static Optional<EasyModelItemAnchor> resolve(Identifier profileId, HumanoidArm arm) {
    Optional<EasyModelRenderState> renderState =
        EasyModelEntityRenderBackend.resolveContract(profileId, EasyModelAnimationSetting.AUTO)
            .map(EasyModelEntityRenderBackend::resolveRenderState);
    if (renderState.isEmpty()) {
      return Optional.empty();
    }

    return getItemAnchor(renderState.get().bakedModel(), arm);
  }

  static Optional<EasyModelItemAnchor> resolve(List<BakedModelPart> rootParts, HumanoidArm arm) {
    boolean leftArm = arm == HumanoidArm.LEFT;

    BakedModelPart itemPart =
        findPart(rootParts, leftArm ? ModelPartType.LEFT_ITEM : ModelPartType.RIGHT_ITEM);
    if (itemPart != null) {
      return Optional.of(new EasyModelItemAnchor(itemPart.name(), EasyModelVec3f.ZERO));
    }

    BakedModelPart handPart =
        findPart(rootParts, leftArm ? ModelPartType.LEFT_HAND : ModelPartType.RIGHT_HAND);
    if (handPart != null) {
      return Optional.of(anchor(handPart, cubeTipOffset(handPart)));
    }

    BakedModelPart armPart =
        findPart(rootParts, leftArm ? ModelPartType.LEFT_ARM : ModelPartType.RIGHT_ARM);
    if (armPart != null) {
      return Optional.of(anchor(armPart, cubeTipOffset(armPart)));
    }

    BakedModelPart headPart = findPart(rootParts, ModelPartType.HEAD);
    if (headPart != null) {
      return Optional.of(anchor(headPart, cubeMouthOffset(headPart)));
    }

    BakedModelPart bodyPart = findPart(rootParts, ModelPartType.BODY);
    if (bodyPart != null) {
      return Optional.of(anchor(bodyPart, cubeMouthOffset(bodyPart)));
    }

    return Optional.empty();
  }

  private static EasyModelItemAnchor anchor(BakedModelPart part, Vec3f offset) {
    return new EasyModelItemAnchor(part.name(), EasyModelApiMapper.vector(offset));
  }

  private static BakedModelPart findPart(List<BakedModelPart> parts, ModelPartType partType) {
    return findPart(parts, partType.getTagName());
  }

  private static BakedModelPart findPart(List<BakedModelPart> parts, String name) {
    for (BakedModelPart part : parts) {
      if (name.equals(part.name().toLowerCase(Locale.ROOT))) {
        return part;
      }
      BakedModelPart childMatch = findPart(part.children(), name);
      if (childMatch != null) {
        return childMatch;
      }
    }
    return null;
  }

  private static Vec3f cubeTipOffset(BakedModelPart part) {
    return cubeBounds(part).tipOffset();
  }

  private static Vec3f cubeMouthOffset(BakedModelPart part) {
    return cubeBounds(part).mouthOffset();
  }

  private static CubeBounds cubeBounds(BakedModelPart part) {
    CubeBounds bounds = new CubeBounds();
    accumulateCubeBounds(part, Vec3f.ZERO, bounds);
    return bounds;
  }

  private static void accumulateCubeBounds(BakedModelPart part, Vec3f offset, CubeBounds bounds) {
    for (BakedModelCube cube : part.cubes()) {
      bounds.include(cube.position().add(offset), cube.dimensions());
    }
    for (BakedModelPart child : part.children()) {
      accumulateCubeBounds(child, offset.add(child.offset()), bounds);
    }
  }

  private record AnchorCacheKey(Identifier profileOrModelId, HumanoidArm arm) {}

  private static final class CubeBounds {

    private float minX = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;
    private float minZ = Float.POSITIVE_INFINITY;
    private float maxZ = Float.NEGATIVE_INFINITY;

    private void include(Vec3f position, Vec3f dimensions) {
      this.minX = Math.min(this.minX, position.x());
      this.maxX = Math.max(this.maxX, position.x() + dimensions.x());
      this.minY = Math.min(this.minY, position.y());
      this.maxY = Math.max(this.maxY, position.y() + dimensions.y());
      this.minZ = Math.min(this.minZ, position.z());
      this.maxZ = Math.max(this.maxZ, position.z() + dimensions.z());
    }

    private Vec3f tipOffset() {
      if (!Float.isFinite(this.minX)) {
        return Vec3f.ZERO;
      }

      return new Vec3f((this.minX + this.maxX) * 0.5f, this.maxY, (this.minZ + this.maxZ) * 0.5f);
    }

    private Vec3f mouthOffset() {
      if (!Float.isFinite(this.minX)) {
        return Vec3f.ZERO;
      }

      return new Vec3f((this.minX + this.maxX) * 0.5f, (this.minY + this.maxY) * 0.5f, this.minZ);
    }
  }
}

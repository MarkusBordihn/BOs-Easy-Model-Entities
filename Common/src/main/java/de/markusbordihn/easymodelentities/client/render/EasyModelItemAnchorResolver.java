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
import de.markusbordihn.easymodelentities.api.data.client.EasyModelItemAnchor;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

public final class EasyModelItemAnchorResolver {

  private static final String ANCHOR_SUFFIX = "_item";
  private static final String HAND_SUFFIX = "_hand";
  private static final String ARM_SUFFIX = "_arm";

  private static final Map<AnchorCacheKey, Optional<EasyModelItemAnchor>> ANCHOR_CACHE =
      new ConcurrentHashMap<>();

  static {
    EasyModelReloadEvents.onProfileReload(ANCHOR_CACHE::clear);
    EasyModelReloadEvents.onRenderProfileReload(ANCHOR_CACHE::clear);
  }

  private EasyModelItemAnchorResolver() {}

  public static Optional<EasyModelItemAnchor> getItemAnchor(
      ResourceLocation profileId, HumanoidArm arm) {
    AnchorCacheKey cacheKey = new AnchorCacheKey(profileId, arm);
    return ANCHOR_CACHE.computeIfAbsent(cacheKey, key -> resolve(key.profileId(), key.arm()));
  }

  private static Optional<EasyModelItemAnchor> resolve(
      ResourceLocation profileId, HumanoidArm arm) {
    Optional<EasyModelRenderState> renderState =
        EasyModelEntityRenderBackend.resolveContract(profileId, EasyModelAnimationState.AUTO)
            .map(EasyModelEntityRenderBackend::resolveRenderState);
    if (renderState.isEmpty()) {
      return Optional.empty();
    }

    return resolve(renderState.get().bakedModel().rootParts(), arm);
  }

  static Optional<EasyModelItemAnchor> resolve(List<BakedModelPart> rootParts, HumanoidArm arm) {
    String side = arm == HumanoidArm.LEFT ? "left" : "right";
    BakedModelPart anchorPart = findPart(rootParts, side + ANCHOR_SUFFIX);
    if (anchorPart != null) {
      return Optional.of(new EasyModelItemAnchor(anchorPart.name(), Vec3f.ZERO));
    }

    BakedModelPart handPart = findPart(rootParts, side + HAND_SUFFIX);
    if (handPart != null) {
      return Optional.of(new EasyModelItemAnchor(handPart.name(), cubeTipOffset(handPart)));
    }

    BakedModelPart armPart = findPart(rootParts, side + ARM_SUFFIX);
    if (armPart != null) {
      return Optional.of(new EasyModelItemAnchor(armPart.name(), cubeTipOffset(armPart)));
    }

    return Optional.empty();
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
    CubeBounds bounds = new CubeBounds();
    accumulateCubeBounds(part, Vec3f.ZERO, bounds);
    return bounds.tipOffset();
  }

  private static void accumulateCubeBounds(BakedModelPart part, Vec3f offset, CubeBounds bounds) {
    for (BakedModelCube cube : part.cubes()) {
      bounds.include(cube.position().add(offset), cube.dimensions());
    }
    for (BakedModelPart child : part.children()) {
      accumulateCubeBounds(child, offset.add(child.offset()), bounds);
    }
  }

  private record AnchorCacheKey(ResourceLocation profileId, HumanoidArm arm) {}

  private static final class CubeBounds {

    private float minX = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;
    private float minZ = Float.POSITIVE_INFINITY;
    private float maxZ = Float.NEGATIVE_INFINITY;

    private void include(Vec3f position, Vec3f dimensions) {
      this.minX = Math.min(this.minX, position.x());
      this.maxX = Math.max(this.maxX, position.x() + dimensions.x());
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
  }
}

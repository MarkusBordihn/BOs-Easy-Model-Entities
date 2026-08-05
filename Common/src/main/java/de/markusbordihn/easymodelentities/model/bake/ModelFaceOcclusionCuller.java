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

package de.markusbordihn.easymodelentities.model.bake;

import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class ModelFaceOcclusionCuller {

  private static final float EPSILON = 1.0e-3f;

  private ModelFaceOcclusionCuller() {}

  static Result cull(List<BakedModelPart> rootParts, ModelBodyType bodyType) {
    List<List<Entry>> buckets = new ArrayList<>();
    if (bodyType == ModelBodyType.STATIC) {
      List<Entry> rootBucket = newBucket(buckets);
      for (BakedModelPart part : rootParts) {
        collectRigid(part, Vec3f.ZERO, rootBucket, buckets);
      }
    } else {
      for (BakedModelPart part : rootParts) {
        collectPerPart(part, buckets);
      }
    }

    Map<BakedModelCube, CubeFaceVisibility> updates = new IdentityHashMap<>();
    int culledFaces = 0;
    for (List<Entry> bucket : buckets) {
      culledFaces += occludeBucket(bucket, updates);
    }
    if (updates.isEmpty()) {
      return new Result(rootParts, 0);
    }

    List<BakedModelPart> rebuilt = new ArrayList<>(rootParts.size());
    for (BakedModelPart part : rootParts) {
      rebuilt.add(rebuild(part, updates));
    }
    return new Result(rebuilt, culledFaces);
  }

  private static List<Entry> newBucket(List<List<Entry>> buckets) {
    List<Entry> bucket = new ArrayList<>();
    buckets.add(bucket);
    return bucket;
  }

  private static void collectRigid(
      BakedModelPart part, Vec3f parentOffset, List<Entry> bucket, List<List<Entry>> buckets) {
    Vec3f localOffset = parentOffset.add(part.offset());
    List<Entry> targetBucket;
    Vec3f frameOffset;
    if (part.rotation().isZero()) {
      targetBucket = bucket;
      frameOffset = localOffset;
    } else {
      targetBucket = newBucket(buckets);
      frameOffset = Vec3f.ZERO;
    }

    for (BakedModelCube cube : part.cubes()) {
      targetBucket.add(new Entry(cube, cube.position().add(frameOffset)));
    }
    for (BakedModelPart child : part.children()) {
      collectRigid(child, frameOffset, targetBucket, buckets);
    }
  }

  private static void collectPerPart(BakedModelPart part, List<List<Entry>> buckets) {
    if (!part.cubes().isEmpty()) {
      List<Entry> bucket = newBucket(buckets);
      for (BakedModelCube cube : part.cubes()) {
        bucket.add(new Entry(cube, cube.position()));
      }
    }
    for (BakedModelPart child : part.children()) {
      collectPerPart(child, buckets);
    }
  }

  private static int occludeBucket(
      List<Entry> bucket, Map<BakedModelCube, CubeFaceVisibility> updates) {
    PlaneIndex planeIndex = new PlaneIndex(bucket);
    int culled = 0;
    for (Entry entry : bucket) {
      CubeFaceVisibility visibility = entry.cube().faceVisibility();
      for (ModelCubeFace face : ModelCubeFace.values()) {
        if (!visibility.isVisible(face)) {
          continue;
        }
        if (isOccluded(entry, face, planeIndex)) {
          visibility = visibility.without(face);
          culled++;
        }
      }
      if (visibility != entry.cube().faceVisibility()) {
        updates.put(entry.cube(), visibility);
      }
    }
    return culled;
  }

  private static boolean isOccluded(Entry entry, ModelCubeFace face, PlaneIndex planeIndex) {
    int axis = axis(face);
    boolean maxPlane = isMaxPlane(face);
    int axisB = (axis + 1) % 3;
    int axisC = (axis + 2) % 3;
    float plane = maxPlane ? entry.max(axis) : entry.min(axis);

    long planeBucket = PlaneIndex.bucket(plane);
    for (long candidateBucket = planeBucket - 1;
        candidateBucket <= planeBucket + 1;
        candidateBucket++) {
      List<Entry> candidates = planeIndex.entries(axis, !maxPlane, candidateBucket);
      if (candidates == null) {
        continue;
      }
      for (Entry other : candidates) {
        if (other == entry) {
          continue;
        }
        float otherPlane = maxPlane ? other.min(axis) : other.max(axis);
        if (Math.abs(otherPlane - plane) <= EPSILON
            && covers(other, entry, axisB)
            && covers(other, entry, axisC)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean covers(Entry cover, Entry target, int axis) {
    return cover.min(axis) <= target.min(axis) + EPSILON
        && cover.max(axis) >= target.max(axis) - EPSILON;
  }

  private static int axis(ModelCubeFace face) {
    return switch (face) {
      case EAST, WEST -> 0;
      case UP, DOWN -> 1;
      case NORTH, SOUTH -> 2;
    };
  }

  private static boolean isMaxPlane(ModelCubeFace face) {
    return switch (face) {
      case WEST, DOWN, SOUTH -> true;
      case EAST, UP, NORTH -> false;
    };
  }

  private static BakedModelPart rebuild(
      BakedModelPart part, Map<BakedModelCube, CubeFaceVisibility> updates) {
    List<BakedModelCube> cubes = new ArrayList<>(part.cubes().size());
    for (BakedModelCube cube : part.cubes()) {
      CubeFaceVisibility updated = updates.get(cube);
      cubes.add(updated == null ? cube : cube.withFaceVisibility(updated));
    }
    List<BakedModelPart> children = new ArrayList<>(part.children().size());
    for (BakedModelPart child : part.children()) {
      children.add(rebuild(child, updates));
    }
    return new BakedModelPart(part.name(), part.offset(), part.rotation(), cubes, children);
  }

  record Result(List<BakedModelPart> rootParts, int culledFaces) {}

  private static final class PlaneIndex {

    private final Map<PlaneKey, List<Entry>> entriesByPlane = new HashMap<>();

    private PlaneIndex(List<Entry> entries) {
      for (Entry entry : entries) {
        if (entry.degenerate()) {
          continue;
        }
        for (int axis = 0; axis < 3; axis++) {
          add(axis, false, entry.min(axis), entry);
          add(axis, true, entry.max(axis), entry);
        }
      }
    }

    private static long bucket(float plane) {
      return Math.round(plane / EPSILON);
    }

    private void add(int axis, boolean maxPlane, float plane, Entry entry) {
      this.entriesByPlane
          .computeIfAbsent(
              new PlaneKey(axis, maxPlane, bucket(plane)), ignored -> new ArrayList<>())
          .add(entry);
    }

    private List<Entry> entries(int axis, boolean maxPlane, long planeBucket) {
      return this.entriesByPlane.get(new PlaneKey(axis, maxPlane, planeBucket));
    }
  }

  private record PlaneKey(int axis, boolean maxPlane, long bucket) {}

  private record Entry(BakedModelCube cube, Vec3f min) {

    private static float component(Vec3f vector, int axis) {
      return switch (axis) {
        case 0 -> vector.x();
        case 1 -> vector.y();
        default -> vector.z();
      };
    }

    float min(int axis) {
      return component(this.min, axis);
    }

    float max(int axis) {
      return component(this.min, axis) + component(this.cube.dimensions(), axis);
    }

    boolean degenerate() {
      Vec3f dimensions = this.cube.dimensions();
      return dimensions.x() <= EPSILON || dimensions.y() <= EPSILON || dimensions.z() <= EPSILON;
    }
  }
}

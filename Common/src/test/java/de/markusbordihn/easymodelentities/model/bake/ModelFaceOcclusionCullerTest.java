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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.easymodelentities.data.model.CubeFaceVisibility;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFace;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelFaceOcclusionCullerTest {

  private static BakedModelCube cube(Vec3f position, Vec3f dimensions) {
    int[] uvOffset = {0, 0};
    return new BakedModelCube(
        uvOffset,
        ModelCubeFaceUvs.fromBoxUv(
            uvOffset, new float[] {dimensions.x(), dimensions.y(), dimensions.z()}),
        position,
        dimensions,
        false);
  }

  private static BakedModelPart part(String name, List<BakedModelCube> cubes) {
    return part(name, cubes, List.of());
  }

  private static BakedModelPart part(
      String name, List<BakedModelCube> cubes, List<BakedModelPart> children) {
    return new BakedModelPart(name, Vec3f.ZERO, Vec3f.ZERO, cubes, children);
  }

  private static CubeFaceVisibility visibilityOf(BakedModelPart part, int cubeIndex) {
    return part.cubes().get(cubeIndex).faceVisibility();
  }

  @Test
  void staticStackedCubesInSamePartCullTouchingFaces() {
    BakedModelCube lower = cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelCube upper = cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelPart root = part("root", List.of(lower, upper));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(root), ModelBodyType.STATIC);

    assertEquals(2, result.culledFaces());
    BakedModelPart culled = result.rootParts().get(0);
    assertFalse(visibilityOf(culled, 0).isVisible(ModelCubeFace.DOWN));
    assertTrue(visibilityOf(culled, 0).isVisible(ModelCubeFace.UP));
    assertFalse(visibilityOf(culled, 1).isVisible(ModelCubeFace.UP));
    assertTrue(visibilityOf(culled, 1).isVisible(ModelCubeFace.DOWN));
  }

  @Test
  void staticStackedCubesInSeparatePartsCullAcrossBones() {
    BakedModelPart lower =
        part("base", List.of(cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f))));
    BakedModelPart upper =
        part("tower", List.of(cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f))));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(lower, upper), ModelBodyType.STATIC);

    assertEquals(2, result.culledFaces());
  }

  @Test
  void animatedSeparatePartsAreNotCulledAcrossBones() {
    BakedModelPart body =
        part("body", List.of(cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f))));
    BakedModelPart leg =
        part("left_leg", List.of(cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f))));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(body, leg), ModelBodyType.QUADRUPED);

    assertEquals(0, result.culledFaces());
    assertEquals(List.of(body, leg), result.rootParts());
  }

  @Test
  void animatedStackedCubesInSameBoneAreCulled() {
    BakedModelCube lower = cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelCube upper = cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelPart leg = part("left_leg", List.of(lower, upper));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(leg), ModelBodyType.QUADRUPED);

    assertEquals(2, result.culledFaces());
  }

  @Test
  void partiallyOverlappingFaceIsNotCulled() {
    BakedModelCube lower = cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelCube upper = cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(4.0f, 8.0f, 8.0f));
    BakedModelPart root = part("root", List.of(lower, upper));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(root), ModelBodyType.STATIC);

    assertEquals(1, result.culledFaces());
    BakedModelPart culled = result.rootParts().get(0);
    assertTrue(visibilityOf(culled, 0).isVisible(ModelCubeFace.DOWN));
    assertFalse(visibilityOf(culled, 1).isVisible(ModelCubeFace.UP));
  }

  @Test
  void touchingFacesWithinToleranceRemainIndexedTogether() {
    BakedModelCube lower = cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelCube upper = cube(new Vec3f(0.0f, 8.0005f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(
            List.of(part("root", List.of(lower, upper))), ModelBodyType.STATIC);

    assertEquals(2, result.culledFaces());
  }

  @Test
  void rotatedPartBreaksRigidChain() {
    BakedModelCube lower = cube(new Vec3f(0.0f, 0.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelCube upper = cube(new Vec3f(0.0f, 8.0f, 0.0f), new Vec3f(8.0f, 8.0f, 8.0f));
    BakedModelPart rotated =
        new BakedModelPart(
            "tower", Vec3f.ZERO, new Vec3f(0.0f, 0.5f, 0.0f), List.of(upper), List.of());
    BakedModelPart root = part("root", List.of(lower), List.of(rotated));

    ModelFaceOcclusionCuller.Result result =
        ModelFaceOcclusionCuller.cull(List.of(root), ModelBodyType.STATIC);

    assertEquals(0, result.culledFaces());
  }
}

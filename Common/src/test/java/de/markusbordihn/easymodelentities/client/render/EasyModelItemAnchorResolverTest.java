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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.easymodelentities.api.data.client.EasyModelItemAnchor;
import de.markusbordihn.easymodelentities.data.model.ModelCubeFaceUvs;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelCube;
import de.markusbordihn.easymodelentities.data.model.bake.BakedModelPart;
import java.util.List;
import net.minecraft.world.entity.HumanoidArm;
import org.junit.jupiter.api.Test;

class EasyModelItemAnchorResolverTest {

  private static BakedModelPart part(String name, List<BakedModelCube> cubes) {
    return new BakedModelPart(name, Vec3f.ZERO, Vec3f.ZERO, cubes, List.of());
  }

  private static BakedModelCube cube(Vec3f position, Vec3f dimensions) {
    return new BakedModelCube(
        new int[] {0, 0},
        ModelCubeFaceUvs.fromBoxUv(
            new int[] {0, 0}, new float[] {dimensions.x(), dimensions.y(), dimensions.z()}),
        position,
        dimensions,
        false);
  }

  @Test
  void dedicatedAnchorTakesPrecedenceOverHandFallback() {
    BakedModelPart hand =
        part("right_hand", List.of(cube(Vec3f.ZERO, new Vec3f(2.0f, 6.0f, 2.0f))));
    BakedModelPart anchor = part("right_item", List.of());

    EasyModelItemAnchor result =
        EasyModelItemAnchorResolver.resolve(List.of(hand, anchor), HumanoidArm.RIGHT).orElseThrow();

    assertEquals("right_item", result.partName());
    assertEquals(Vec3f.ZERO, result.localOffset());
  }

  @Test
  void rotatedChildCubeContributesToArmFallback() {
    BakedModelPart rotatedCube =
        new BakedModelPart(
            "right_arm_cube_r1",
            new Vec3f(2.0f, 3.0f, 4.0f),
            new Vec3f(0.0f, 0.0f, 0.5f),
            List.of(cube(new Vec3f(-1.0f, 0.0f, -2.0f), new Vec3f(2.0f, 6.0f, 4.0f))),
            List.of());
    BakedModelPart arm =
        new BakedModelPart("right_arm", Vec3f.ZERO, Vec3f.ZERO, List.of(), List.of(rotatedCube));

    EasyModelItemAnchor result =
        EasyModelItemAnchorResolver.resolve(List.of(arm), HumanoidArm.RIGHT).orElseThrow();

    assertEquals("right_arm", result.partName());
    assertEquals(new Vec3f(2.0f, 9.0f, 4.0f), result.localOffset());
  }

  @Test
  void selectsAnchorForRequestedArm() {
    BakedModelPart leftHand = part("left_hand", List.of(cube(Vec3f.ZERO, Vec3f.ZERO)));
    BakedModelPart rightHand = part("right_hand", List.of(cube(Vec3f.ZERO, Vec3f.ZERO)));

    EasyModelItemAnchor result =
        EasyModelItemAnchorResolver.resolve(List.of(leftHand, rightHand), HumanoidArm.LEFT)
            .orElseThrow();

    assertEquals("left_hand", result.partName());
  }
}

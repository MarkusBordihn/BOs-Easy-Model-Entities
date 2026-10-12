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

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelBlockEntityRenderOptions;
import de.markusbordihn.easymodelentities.block.EasyModelHostBlock;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EasyModelHostBlockEntityRenderer<T extends EasyModelHostBlockEntity>
    implements BlockEntityRenderer<T, EasyModelBlockEntityRenderState> {

  private static final Map<Direction, EasyModelBlockEntityRenderOptions> FACING_OPTIONS =
      createFacingOptions();

  public EasyModelHostBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    EasyModelEntityCullingCompat.register();
  }

  @Override
  public EasyModelBlockEntityRenderState createRenderState() {
    return new EasyModelBlockEntityRenderState();
  }

  private static Map<Direction, EasyModelBlockEntityRenderOptions> createFacingOptions() {
    Map<Direction, EasyModelBlockEntityRenderOptions> options = new EnumMap<>(Direction.class);
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      options.put(
          direction, EasyModelBlockEntityRenderOptions.DEFAULT.withYawDegrees(direction.toYRot()));
    }
    return options;
  }

  private static EasyModelBlockEntityRenderOptions facingOptions(BlockState blockState) {
    if (!blockState.hasProperty(EasyModelHostBlock.FACING)) {
      return EasyModelBlockEntityRenderOptions.DEFAULT;
    }

    return FACING_OPTIONS.get(blockState.getValue(EasyModelHostBlock.FACING));
  }

  @Override
  public void extractRenderState(
      T blockEntity,
      EasyModelBlockEntityRenderState renderState,
      float partialTick,
      Vec3 cameraPos,
      ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
    BlockEntityRenderer.super.extractRenderState(
        blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
    EasyModelBlockEntityRenderBackend.extractRenderState(
        blockEntity,
        blockEntity.getEasyModelRuntimeContract(),
        renderState,
        partialTick,
        facingOptions(blockEntity.getBlockState()));
  }

  @Override
  public void submit(
      EasyModelBlockEntityRenderState renderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState) {
    if (renderState.easyModelRenderState == null) {
      return;
    }
    EasyModelBlockEntityRenderBackend.render(
        renderState, poseStack, submitNodeCollector, renderState.lightCoords);
  }

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }
}

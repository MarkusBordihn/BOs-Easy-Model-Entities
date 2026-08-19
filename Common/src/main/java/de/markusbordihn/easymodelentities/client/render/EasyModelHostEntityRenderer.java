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
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EasyModelHostEntityRenderer<T extends Entity & EasyModelEntityHost>
    extends EntityRenderer<T, EasyModelEntityRenderState> {

  public EasyModelHostEntityRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.shadowRadius = 0.3f;
    EasyModelEntityCullingCompat.register();
  }

  private static float cullingYaw(Entity entity) {
    return entity instanceof LivingEntity livingEntity ? livingEntity.yBodyRot : entity.getYRot();
  }

  @Override
  public EasyModelEntityRenderState createRenderState() {
    return new EasyModelEntityRenderState();
  }

  @Override
  public void extractRenderState(
      T entity, EasyModelEntityRenderState renderState, float partialTick) {
    super.extractRenderState(entity, renderState, partialTick);
    EasyModelEntityRenderBackend.extractRenderState(
        entity, entity.getEasyModelRuntimeContract(), renderState, partialTick);
  }

  protected float getShadowRadius(EasyModelEntityRenderState renderState) {
    return renderState.easyModelRenderState != null
        ? renderState.easyModelRenderState.shadowRadius()
        : this.shadowRadius;
  }

  @Override
  public void submit(
      EasyModelEntityRenderState renderState,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState cameraRenderState) {
    if (renderState.easyModelRenderState == null) {
      return;
    }

    EasyModelEntityRenderBackend.render(
        renderState, poseStack, submitNodeCollector, renderState.lightCoords);
  }

  @Override
  public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
    var easyModelRenderState =
        EasyModelEntityRenderBackend.resolveRenderState(entity.getEasyModelRuntimeContract());
    if (!easyModelRenderState.hasVisibleBounds()) {
      return super.shouldRender(entity, frustum, camX, camY, camZ);
    }

    if (!entity.shouldRender(camX, camY, camZ)) {
      return false;
    }

    if (!affectedByCulling(entity)) {
      return true;
    }

    return frustum.isVisible(
        EasyModelCullingBounds.visibleBounds(
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            easyModelRenderState.visibleBoundsWidth(),
            easyModelRenderState.visibleBoundsHeight(),
            easyModelRenderState.visibleBoundsOffset(),
            cullingYaw(entity)));
  }
}

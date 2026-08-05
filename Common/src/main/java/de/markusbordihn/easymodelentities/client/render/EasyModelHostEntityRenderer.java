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
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class EasyModelHostEntityRenderer<T extends Entity & EasyModelEntityHost>
    extends EntityRenderer<T, EasyModelEntityRenderState> {

  public EasyModelHostEntityRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.shadowRadius = 0.3f;
  }

  private static float bodyYaw(Entity entity, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot)
        : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
  }

  @Override
  public EasyModelEntityRenderState createRenderState() {
    return new EasyModelEntityRenderState();
  }

  @Override
  public void extractRenderState(
      T entity, EasyModelEntityRenderState renderState, float partialTick) {
    super.extractRenderState(entity, renderState, partialTick);
    renderState.easyModelRenderState =
        EasyModelEntityRenderBackend.resolveRenderState(entity.getEasyModelRuntimeContract());
    renderState.playbackFrame =
        EasyModelEntityRenderBackend.playbackFrame(
            entity, renderState.easyModelRenderState, partialTick);
    renderState.entityYaw = bodyYaw(entity, partialTick);
    renderState.limbSwing =
        entity instanceof LivingEntity le ? le.walkAnimation.position(partialTick) : 0.0f;
    renderState.limbSwingAmount =
        entity instanceof LivingEntity le
            ? Math.min(le.walkAnimation.speed(partialTick), 1.0f)
            : 0.0f;
    renderState.airborneAmount = EasyModelEntityRenderBackend.airborneAmount(entity);
    renderState.attackAmount = EasyModelEntityRenderBackend.attackAmount(entity, partialTick);
    renderState.headLook =
        EasyModelEntityRenderBackend.headLook(entity, renderState.entityYaw, partialTick);
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
    Vec3f offset = easyModelRenderState.visibleBoundsOffset();
    double halfWidth = easyModelRenderState.visibleBoundsWidth() / 2.0;
    double height = easyModelRenderState.visibleBoundsHeight();
    double centerX = entity.getX() + offset.x();
    double centerZ = entity.getZ() + offset.z();
    double baseY = entity.getY() + offset.y();
    return frustum.isVisible(
        new AABB(
            centerX - halfWidth,
            baseY,
            centerZ - halfWidth,
            centerX + halfWidth,
            baseY + height,
            centerZ + halfWidth));
  }
}

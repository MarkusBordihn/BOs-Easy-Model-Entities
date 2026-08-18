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
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EasyModelHostEntityRenderer<T extends Entity & EasyModelEntityHost>
    extends EntityRenderer<T> {

  public EasyModelHostEntityRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.shadowRadius = 0.3f;
    EasyModelEntityCullingCompat.register();
  }

  private static float bodyYaw(Entity entity, float entityYaw, float partialTick) {
    return entity instanceof LivingEntity livingEntity
        ? Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot)
        : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
  }

  private static float cullingYaw(Entity entity) {
    return entity instanceof LivingEntity livingEntity ? livingEntity.yBodyRot : entity.getYRot();
  }

  private static int packedOverlay(Entity entity) {
    if (!(entity instanceof LivingEntity livingEntity)) {
      return OverlayTexture.NO_OVERLAY;
    }

    return OverlayTexture.pack(
        OverlayTexture.u(0.0f),
        OverlayTexture.v(livingEntity.hurtTime > 0 || livingEntity.deathTime > 0));
  }

  @Override
  public void render(
      T entity,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    EasyModelRenderState renderState = resolveRenderState(entity);
    this.shadowRadius = renderState.shadowRadius();
    EasyModelEntityRenderBackend.render(
        entity,
        renderState,
        bodyYaw(entity, entityYaw, partialTick),
        partialTick,
        poseStack,
        bufferSource,
        packedLight,
        packedOverlay(entity));
    super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }

  @Override
  public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
    EasyModelRenderState renderState = resolveRenderState(entity);
    if (!renderState.hasVisibleBounds()) {
      return super.shouldRender(entity, frustum, camX, camY, camZ);
    }
    if (!entity.shouldRender(camX, camY, camZ)) {
      return false;
    }
    return entity.noCulling
        || frustum.isVisible(
            EasyModelCullingBounds.visibleBounds(
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                renderState.visibleBoundsWidth(),
                renderState.visibleBoundsHeight(),
                renderState.visibleBoundsOffset(),
                cullingYaw(entity)));
  }

  @Override
  public ResourceLocation getTextureLocation(T entity) {
    return resolveRenderState(entity).texture();
  }

  private EasyModelRenderState resolveRenderState(T entity) {
    return EasyModelEntityRenderBackend.resolveRenderState(entity.getEasyModelRuntimeContract());
  }
}

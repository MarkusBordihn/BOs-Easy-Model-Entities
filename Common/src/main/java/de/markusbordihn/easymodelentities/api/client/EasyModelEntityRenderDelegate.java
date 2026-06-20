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

package de.markusbordihn.easymodelentities.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelEntityRenderOptions;
import de.markusbordihn.easymodelentities.api.data.client.EasyModelPartDefinition;
import de.markusbordihn.easymodelentities.client.render.EasyModelEntityRenderBackend;
import de.markusbordihn.easymodelentities.data.render.EasyModelRenderState;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelRuntimeContract;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class EasyModelEntityRenderDelegate<T extends Entity & EasyModelRenderable> {

  EasyModelEntityRenderDelegate() {}

  public void render(
      T entity,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight) {
    render(
        entity,
        entityYaw,
        partialTick,
        poseStack,
        bufferSource,
        packedLight,
        EasyModelEntityRenderOptions.DEFAULT);
  }

  public void render(
      T entity,
      float entityYaw,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      EasyModelEntityRenderOptions options) {
    EasyModelRenderState renderState =
        EasyModelEntityRenderBackend.resolveRenderState(contract(entity));
    EasyModelEntityRenderBackend.render(
        entity, renderState, entityYaw, partialTick, options, poseStack, bufferSource, packedLight);
  }

  public Identifier getTextureLocation(T entity) {
    return EasyModelEntityRenderBackend.resolveRenderState(contract(entity)).texture();
  }

  public List<EasyModelPartDefinition> rootModelParts(T entity) {
    EasyModelRenderState renderState =
        EasyModelEntityRenderBackend.resolveRenderState(contract(entity));
    return renderState.bakedModel().rootParts().stream()
        .map(EasyModelPartDefinitions::fromBakedPart)
        .toList();
  }

  public List<EasyModelPartDefinition> modelParts(T entity) {
    return EasyModelPartDefinitions.flatten(rootModelParts(entity));
  }

  private EasyModelRuntimeContract contract(T entity) {
    return EasyModelEntityRenderBackend.runtimeContract(
        entity,
        entity,
        EasyModelServices.profileService(),
        EasyModelServices.renderProfileService());
  }
}

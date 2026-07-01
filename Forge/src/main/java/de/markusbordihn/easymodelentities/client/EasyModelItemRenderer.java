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

package de.markusbordihn.easymodelentities.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.markusbordihn.easymodelentities.client.render.EasyModelItemModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EasyModelItemRenderer extends BlockEntityWithoutLevelRenderer {

  public EasyModelItemRenderer() {
    super(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        Minecraft.getInstance().getEntityModels());
  }

  @Override
  public void renderByItem(
      ItemStack stack,
      ItemDisplayContext displayContext,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay) {
    EasyModelItemModelRenderer.render(stack, poseStack, bufferSource, packedLight, packedOverlay);
  }
}

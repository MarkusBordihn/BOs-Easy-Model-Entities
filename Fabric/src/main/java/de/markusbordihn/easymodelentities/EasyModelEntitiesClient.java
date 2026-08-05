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

package de.markusbordihn.easymodelentities;

import de.markusbordihn.easymodelentities.client.render.EasyModelHostBlockEntityRenderer;
import de.markusbordihn.easymodelentities.client.render.EasyModelHostEntityRenderer;
import de.markusbordihn.easymodelentities.client.render.EasyModelItemModelRenderer;
import de.markusbordihn.easymodelentities.network.EasyModelAnimationNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyModelEntitiesClient implements ClientModInitializer {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public void onInitializeClient() {
    log.info("Initializing {} (Fabric Client) ...", Constants.MOD_NAME);
    EasyModelAnimationNetworkHandler.registerClient();
    EntityRendererRegistry.register(
        EasyModelEntityTypes.INSTANCE.groundEntityType(), EasyModelHostEntityRenderer::new);
    EntityRendererRegistry.register(
        EasyModelEntityTypes.INSTANCE.staticEntityType(), EasyModelHostEntityRenderer::new);
    EntityRendererRegistry.register(
        EasyModelEntityTypes.INSTANCE.aquaticEntityType(), EasyModelHostEntityRenderer::new);
    EntityRendererRegistry.register(
        EasyModelEntityTypes.INSTANCE.amphibiousEntityType(), EasyModelHostEntityRenderer::new);
    BlockEntityRenderers.register(
        EasyModelBlockEntityTypes.INSTANCE.staticBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    BlockEntityRenderers.register(
        EasyModelBlockEntityTypes.INSTANCE.tickingBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    BlockEntityRenderers.register(
        EasyModelBlockEntityTypes.INSTANCE.animatedBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    BlockEntityRenderers.register(
        EasyModelBlockEntityTypes.INSTANCE.animatedRandomlyBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    BuiltinItemRendererRegistry.DynamicItemRenderer itemRenderer =
        (stack, mode, poseStack, bufferSource, light, overlay) ->
            EasyModelItemModelRenderer.render(stack, poseStack, bufferSource, light, overlay);
    BuiltinItemRendererRegistry.INSTANCE.register(EasyModelItems.ENTITY_SPAWN, itemRenderer);
    BuiltinItemRendererRegistry.INSTANCE.register(EasyModelItems.BLOCK_SPAWN, itemRenderer);
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
        .registerReloadListener(new ModelRenderProfileReloadListenerWrapper());
  }
}

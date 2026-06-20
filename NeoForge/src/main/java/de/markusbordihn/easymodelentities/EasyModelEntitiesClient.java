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
import de.markusbordihn.easymodelentities.renderprofile.ModelRenderProfileReloadListener;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class EasyModelEntitiesClient {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public EasyModelEntitiesClient(IEventBus modEventBus) {
    log.info("Initializing {} (NeoForge Client) ...", Constants.MOD_NAME);
    modEventBus.addListener(this::registerEntityRenderers);
    modEventBus.addListener(this::registerClientReloadListeners);
  }

  private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(
        NeoForgeEasyModelEntityTypes.INSTANCE.groundEntityType(), EasyModelHostEntityRenderer::new);
    event.registerEntityRenderer(
        NeoForgeEasyModelEntityTypes.INSTANCE.staticEntityType(), EasyModelHostEntityRenderer::new);
    event.registerEntityRenderer(
        NeoForgeEasyModelEntityTypes.INSTANCE.aquaticEntityType(),
        EasyModelHostEntityRenderer::new);
    event.registerEntityRenderer(
        NeoForgeEasyModelEntityTypes.INSTANCE.amphibiousEntityType(),
        EasyModelHostEntityRenderer::new);
    event.registerBlockEntityRenderer(
        NeoForgeEasyModelBlockEntityTypes.INSTANCE.staticBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(
        NeoForgeEasyModelBlockEntityTypes.INSTANCE.tickingBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(
        NeoForgeEasyModelBlockEntityTypes.INSTANCE.animatedBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(
        NeoForgeEasyModelBlockEntityTypes.INSTANCE.animatedRandomlyBlockEntityType(),
        EasyModelHostBlockEntityRenderer::new);
  }

  private void registerClientReloadListeners(AddClientReloadListenersEvent event) {
    Identifier listenerId =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "render_profile_reload_listener");
    event.addListener(listenerId, new ModelRenderProfileReloadListener());
    event.addDependency(VanillaClientListeners.LAST, listenerId);
  }
}

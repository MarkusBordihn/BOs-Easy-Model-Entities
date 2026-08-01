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

import de.markusbordihn.easymodelentities.command.EasyModelEntitiesCommand;
import de.markusbordihn.easymodelentities.diagnostics.DefaultEasyModelDiagnosticsService;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntityFactory;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyModelEntities implements ModInitializer {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public void onInitialize() {
    log.info("Initializing {} (Fabric) ...", Constants.MOD_NAME);

    Constants.GAME_DIR = FabricLoader.getInstance().getGameDir();
    Constants.CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    FabricTrackedDataRegistry.register(
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "body_type"),
        EasyModelEntityDataSerializers.BODY_TYPE);
    FabricTrackedDataRegistry.register(
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "animation_state"),
        EasyModelEntityDataSerializers.ANIMATION_STATE);
    EasyModelEntityTypes.register();
    EasyModelBlockEntityTypes.register();
    EasyModelItems.register();
    EasyModelCreativeModeTabs.register();
    EasyModelServices.setEntityFactory(
        new EasyModelHostEntityFactory(EasyModelEntityTypes.INSTANCE));
    EasyModelServices.setBlockEntityTypeProvider(EasyModelBlockEntityTypes.INSTANCE);
    EasyModelServices.setDiagnosticsService(new DefaultEasyModelDiagnosticsService());

    ResourceManagerHelper.get(PackType.SERVER_DATA)
        .registerReloadListener(new EasyModelProfileReloadListenerWrapper());
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> EasyModelEntitiesCommand.register(dispatcher));
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> EasyModelServices.clearProfileService());
  }
}

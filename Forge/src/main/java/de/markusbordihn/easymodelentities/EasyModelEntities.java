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
import de.markusbordihn.easymodelentities.profile.EasyModelProfileReloadListener;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class EasyModelEntities {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public EasyModelEntities(FMLJavaModLoadingContext context) {
    log.info("Initializing {} (Forge) ...", Constants.MOD_NAME);

    BusGroup modBusGroup = context.getModBusGroup();

    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();

    EasyModelEntityDataSerializers.register();
    EasyModelEntityTypes.register(modBusGroup);
    EasyModelBlockEntityTypes.register(modBusGroup);
    EasyModelItems.register(modBusGroup);
    EasyModelCreativeModeTabs.register(modBusGroup);
    EasyModelServices.setEntityFactory(
        new EasyModelHostEntityFactory(EasyModelEntityTypes.INSTANCE));
    EasyModelServices.setBlockEntityTypeProvider(EasyModelBlockEntityTypes.INSTANCE);
    EasyModelServices.setDiagnosticsService(new DefaultEasyModelDiagnosticsService());

    if (FMLEnvironment.dist.isClient()) {
      new EasyModelEntitiesClient();
    }
  }

  @SubscribeEvent
  public static void addReloadListeners(AddReloadListenerEvent event) {
    event.addListener(new EasyModelProfileReloadListener());
  }

  @SubscribeEvent
  public static void registerCommands(RegisterCommandsEvent event) {
    EasyModelEntitiesCommand.register(event.getDispatcher());
  }
}

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
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.diagnostics.DefaultEasyModelDiagnosticsService;
import de.markusbordihn.easymodelentities.entity.EasyModelHostEntityFactory;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.profile.EasyModelProfileReloadListener;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.resource.VanillaServerListeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class EasyModelEntities {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS =
      DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Constants.MOD_ID);

  @SuppressWarnings("unused")
  public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ModelBodyType>>
      BODY_TYPE_SERIALIZER =
          ENTITY_DATA_SERIALIZERS.register(
              "body_type", () -> EasyModelEntityDataSerializers.BODY_TYPE);

  @SuppressWarnings("unused")
  public static final DeferredHolder<
          EntityDataSerializer<?>, EntityDataSerializer<EasyModelAnimationState>>
      ANIMATION_STATE_SERIALIZER =
          ENTITY_DATA_SERIALIZERS.register(
              "animation_state", () -> EasyModelEntityDataSerializers.ANIMATION_STATE);

  public EasyModelEntities(IEventBus modEventBus) {
    log.info("Initializing {} (NeoForge) ...", Constants.MOD_NAME);

    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();

    ENTITY_DATA_SERIALIZERS.register(modEventBus);
    NeoForgeEasyModelEntityTypes.register(modEventBus);
    NeoForgeEasyModelBlockEntityTypes.register(modEventBus);
    EasyModelServices.setEntityFactory(
        new EasyModelHostEntityFactory(NeoForgeEasyModelEntityTypes.INSTANCE));
    EasyModelServices.setBlockEntityTypeProvider(NeoForgeEasyModelBlockEntityTypes.INSTANCE);
    EasyModelServices.setDiagnosticsService(new DefaultEasyModelDiagnosticsService());

    NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
    NeoForge.EVENT_BUS.addListener(this::registerCommands);
  }

  private void addReloadListeners(AddServerReloadListenersEvent event) {
    Identifier listenerId =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "profile_reload_listener");
    event.addListener(listenerId, new EasyModelProfileReloadListener());
    event.addDependency(VanillaServerListeners.LAST, listenerId);
  }

  private void registerCommands(RegisterCommandsEvent event) {
    EasyModelEntitiesCommand.register(event.getDispatcher());
  }
}

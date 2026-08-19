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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class EasyModelEntityCullingCompat {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static boolean registered;

  private EasyModelEntityCullingCompat() {}

  static synchronized void register() {
    if (registered) {
      return;
    }

    try {
      Class<?> baseClass = Class.forName("dev.tr7zw.entityculling.EntityCullingModBase");
      Field instanceField = baseClass.getDeclaredField("instance");
      instanceField.setAccessible(true);
      Object instance = instanceField.get(null);
      if (instance == null) {
        return;
      }

      Method entityWhitelist = baseClass.getMethod("addDynamicEntityWhitelist", Function.class);
      Method blockEntityWhitelist =
          baseClass.getMethod("addDynamicBlockEntityWhitelist", Function.class);
      entityWhitelist.invoke(
          instance, (Function<Entity, Boolean>) entity -> entity instanceof EasyModelEntityHost);
      blockEntityWhitelist.invoke(
          instance,
          (Function<BlockEntity, Boolean>)
              blockEntity -> blockEntity instanceof EasyModelHostBlockEntity);
      registered = true;
    } catch (ClassNotFoundException ignored) {
      registered = true;
    } catch (ReflectiveOperationException | RuntimeException exception) {
      registered = true;
      log.warn("Unable to register EntityCulling compatibility.", exception);
    }
  }
}

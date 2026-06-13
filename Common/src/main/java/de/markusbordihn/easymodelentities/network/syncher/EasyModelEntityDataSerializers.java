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

package de.markusbordihn.easymodelentities.network.syncher;

import de.markusbordihn.easymodelentities.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.runtime.EasyModelAnimationState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public final class EasyModelEntityDataSerializers {

  public static final EntityDataSerializer<String> STRING = EntityDataSerializers.STRING;
  public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializers.FLOAT;
  public static final EntityDataSerializer<ModelBodyType> BODY_TYPE =
      enumSerializer(ModelBodyType.class);
  public static final EntityDataSerializer<EasyModelAnimationState> ANIMATION_STATE =
      enumSerializer(EasyModelAnimationState.class);

  private static boolean registered = false;

  private EasyModelEntityDataSerializers() {}

  public static <T> EntityDataAccessor<T> defineId(
      Class<? extends Entity> entityClass, EntityDataSerializer<T> serializer) {
    return SynchedEntityData.defineId(entityClass, serializer);
  }

  public static void register() {
    if (registered) {
      return;
    }

    registerSerializer(BODY_TYPE);
    registerSerializer(ANIMATION_STATE);
    registered = true;
  }

  private static <T extends Enum<T>> EntityDataSerializer<T> enumSerializer(Class<T> enumClass) {
    return EntityDataSerializer.simpleEnum(enumClass);
  }

  private static void registerSerializer(EntityDataSerializer<?> serializer) {
    EntityDataSerializers.registerSerializer(serializer);
  }
}

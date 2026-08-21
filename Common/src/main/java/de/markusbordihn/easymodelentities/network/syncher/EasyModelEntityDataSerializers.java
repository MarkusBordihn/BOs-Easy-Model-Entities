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

import de.markusbordihn.easymodelentities.api.data.EasyModelAnimation;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationLoop;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.data.profile.ModelBodyType;
import de.markusbordihn.easymodelentities.network.animation.ClientboundEasyModelAnimationPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public final class EasyModelEntityDataSerializers {

  public static final EntityDataSerializer<String> STRING = EntityDataSerializers.STRING;
  public static final EntityDataSerializer<Float> FLOAT = EntityDataSerializers.FLOAT;
  public static final EntityDataSerializer<Integer> INT = EntityDataSerializers.INT;
  public static final EntityDataSerializer<ModelBodyType> BODY_TYPE =
      enumSerializer(ModelBodyType.class);
  public static final EntityDataSerializer<EasyModelAnimationSetting> ANIMATION_SETTING =
      new EntityDataSerializer<>() {

        @Override
        public void write(FriendlyByteBuf buffer, EasyModelAnimationSetting animation) {
          buffer.writeUtf(
              animation.animation().serializedName(),
              ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH);
          buffer.writeEnum(animation.loop());
        }

        @Override
        public EasyModelAnimationSetting read(FriendlyByteBuf buffer) {
          EasyModelAnimation animation =
              EasyModelAnimation.parse(
                      buffer.readUtf(ClientboundEasyModelAnimationPacket.MAX_ANIMATION_NAME_LENGTH))
                  .orElse(EasyModelAnimation.AUTO);
          return new EasyModelAnimationSetting(
              animation, buffer.readEnum(EasyModelAnimationLoop.class));
        }

        @Override
        public EasyModelAnimationSetting copy(EasyModelAnimationSetting animation) {
          return animation;
        }
      };
  public static final EntityDataSerializer<EasyModelTextureSetting> TEXTURE_SETTING =
      new EntityDataSerializer<>() {

        @Override
        public void write(FriendlyByteBuf buffer, EasyModelTextureSetting textureSetting) {
          buffer.writeNbt(textureSetting.createTag());
        }

        @Override
        public EasyModelTextureSetting read(FriendlyByteBuf buffer) {
          return EasyModelTextureSetting.fromTag(buffer.readNbt())
              .orElse(EasyModelTextureSetting.EMPTY);
        }

        @Override
        public EasyModelTextureSetting copy(EasyModelTextureSetting textureSetting) {
          return textureSetting;
        }
      };

  private static boolean registered = false;

  private EasyModelEntityDataSerializers() {}

  public static void register() {
    if (registered) {
      return;
    }

    registerSerializer(BODY_TYPE);
    registerSerializer(ANIMATION_SETTING);
    registerSerializer(TEXTURE_SETTING);
    registered = true;
  }

  private static <T extends Enum<T>> EntityDataSerializer<T> enumSerializer(Class<T> enumClass) {
    return EntityDataSerializer.simpleEnum(enumClass);
  }

  private static void registerSerializer(EntityDataSerializer<?> serializer) {
    EntityDataSerializers.registerSerializer(serializer);
  }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EasyModelEntityDataSerializersTest {

  private static final Identifier SAD =
      Identifier.fromNamespaceAndPath("example", "textures/entity/echo/screen_sad.png");

  private static RegistryFriendlyByteBuf buffer() {
    return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
  }

  private static StreamCodec<? super RegistryFriendlyByteBuf, EasyModelTextureSetting>
      textureSettingCodec() {
    return EasyModelEntityDataSerializers.TEXTURE_SETTING.codec();
  }

  private static EasyModelTextureSetting roundTrip(EasyModelTextureSetting textureSetting) {
    RegistryFriendlyByteBuf buffer = buffer();
    textureSettingCodec().encode(buffer, textureSetting);
    return textureSettingCodec().decode(buffer);
  }

  @Test
  void textureSettingRoundTripsOverTheWire() {
    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SAD);

    assertEquals(textureSetting, roundTrip(textureSetting));
  }

  @Test
  void emptyTextureSettingRoundTripsAsEmpty() {
    assertEquals(EasyModelTextureSetting.EMPTY, roundTrip(EasyModelTextureSetting.EMPTY));
  }

  @Test
  @DisplayName("An unknown extra field in the payload still reads as today's state")
  void unknownPayloadFieldsAreIgnored() {
    CompoundTag slot = new CompoundTag();
    slot.putString("texture", SAD.toString());
    slot.putBoolean("emissive", true);
    CompoundTag payload = new CompoundTag();
    payload.put("screen", slot);
    payload.put("legacy", StringTag.valueOf(SAD.toString()));
    RegistryFriendlyByteBuf buffer = buffer();
    buffer.writeNbt(payload);

    EasyModelTextureSetting textureSetting = textureSettingCodec().decode(buffer);

    assertEquals(SAD, textureSetting.texture("screen").orElse(null));
    assertEquals(SAD, textureSetting.texture("legacy").orElse(null));
  }

  @Test
  void copyKeepsTheImmutableInstance() {
    EasyModelTextureSetting textureSetting = EasyModelTextureSetting.of("screen", SAD);

    assertSame(textureSetting, EasyModelEntityDataSerializers.TEXTURE_SETTING.copy(textureSetting));
  }
}

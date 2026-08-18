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

package de.markusbordihn.easymodelentities.gametest;

import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

public final class EntityDataSerializerGameTest {

  private EntityDataSerializerGameTest() {}

  public static void serializersAreForgeRegistered(GameTestHelper helper) {
    Identifier bodyTypeId = ModelResourcePaths.modIdentifier("body_type");
    Identifier animationSettingId = ModelResourcePaths.modIdentifier("animation_setting");
    Identifier textureSettingId = ModelResourcePaths.modIdentifier("texture_setting");

    GameTestHelpers.assertTrue(
        helper,
        "Entity data serializers are not registered through the Forge registry!",
        ForgeRegistries.ENTITY_DATA_SERIALIZERS.get().getValue(bodyTypeId)
                == EasyModelEntityDataSerializers.BODY_TYPE
            && ForgeRegistries.ENTITY_DATA_SERIALIZERS.get().getValue(animationSettingId)
                == EasyModelEntityDataSerializers.ANIMATION_SETTING
            && ForgeRegistries.ENTITY_DATA_SERIALIZERS.get().getValue(textureSettingId)
                == EasyModelEntityDataSerializers.TEXTURE_SETTING);
  }
}

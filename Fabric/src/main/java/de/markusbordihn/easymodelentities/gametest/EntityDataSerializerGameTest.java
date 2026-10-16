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

import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.network.syncher.EasyModelEntityDataSerializers;
import de.markusbordihn.easymodelentities.registry.ModelResourcePaths;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
public class EntityDataSerializerGameTest {

  @GameTest(structure = Constants.MOD_ID + ":gametest.3x3x3")
  public void serializersAreFabricRegistered(GameTestHelper helper) {
    GameTestHelpers.runIfEnabled(
        helper,
        enabledHelper -> {
          Identifier bodyTypeId = ModelResourcePaths.modIdentifier("body_type");
          Identifier animationSettingId = ModelResourcePaths.modIdentifier("animation_setting");
          Identifier textureSettingId = ModelResourcePaths.modIdentifier("texture_setting");

          GameTestHelpers.assertTrue(
              enabledHelper,
              "Entity data serializers are not registered through the Fabric registry!",
              FabricEntityDataRegistry.get(bodyTypeId) == EasyModelEntityDataSerializers.BODY_TYPE
                  && FabricEntityDataRegistry.get(animationSettingId)
                      == EasyModelEntityDataSerializers.ANIMATION_SETTING
                  && FabricEntityDataRegistry.get(textureSettingId)
                      == EasyModelEntityDataSerializers.TEXTURE_SETTING);
        });
  }
}

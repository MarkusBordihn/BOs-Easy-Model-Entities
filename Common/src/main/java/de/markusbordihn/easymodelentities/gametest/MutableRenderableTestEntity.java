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

import de.markusbordihn.easymodelentities.api.EasyModelMutableRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelAnimationSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelDisplaySettings;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.Level;

final class MutableRenderableTestEntity extends Marker implements EasyModelMutableRenderable {

  private final Identifier profileId;
  private EasyModelAnimationSetting animationSetting = EasyModelAnimationSetting.AUTO;
  private EasyModelTextureSetting textureSetting = EasyModelTextureSetting.EMPTY;
  private float opacity = EasyModelDisplaySettings.NO_OPACITY;
  private int lightLevel = EasyModelDisplaySettings.NO_LIGHT_LEVEL;

  MutableRenderableTestEntity(Level level, Identifier profileId) {
    super(EntityType.MARKER, level);
    this.profileId = profileId;
  }

  @Override
  public Identifier getEasyModelProfileId() {
    return this.profileId;
  }

  @Override
  public EasyModelAnimationSetting getEasyModelAnimationSetting() {
    return this.animationSetting;
  }

  @Override
  public void setEasyModelAnimationSetting(EasyModelAnimationSetting animationSetting) {
    this.animationSetting = animationSetting;
  }

  @Override
  public EasyModelTextureSetting getEasyModelTextureSetting() {
    return this.textureSetting;
  }

  @Override
  public void setEasyModelTextureSetting(EasyModelTextureSetting textureSetting) {
    this.textureSetting = textureSetting;
  }

  @Override
  public float getEasyModelOpacity() {
    return this.opacity;
  }

  @Override
  public void setEasyModelOpacity(float opacity) {
    this.opacity = opacity;
  }

  @Override
  public int getEasyModelLightLevel() {
    return this.lightLevel;
  }

  @Override
  public void setEasyModelLightLevel(int lightLevel) {
    this.lightLevel = lightLevel;
  }
}

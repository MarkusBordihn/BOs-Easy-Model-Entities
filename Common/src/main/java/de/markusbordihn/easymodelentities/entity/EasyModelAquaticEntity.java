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

package de.markusbordihn.easymodelentities.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EasyModelAquaticEntity extends EasyModelWaterHostEntity {

  public EasyModelAquaticEntity(EntityType<? extends WaterAnimal> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(
        4,
        new RandomSwimmingGoal(this, 1.0, 10) {
          @Override
          public boolean canUse() {
            return EasyModelAquaticEntity.this.shouldRandomStroll() && super.canUse();
          }
        });
    this.goalSelector.addGoal(
        8,
        new LookAtPlayerGoal(this, Player.class, 8.0f) {
          @Override
          public boolean canUse() {
            return EasyModelAquaticEntity.this.shouldLookAtPlayers() && super.canUse();
          }
        });
  }
}

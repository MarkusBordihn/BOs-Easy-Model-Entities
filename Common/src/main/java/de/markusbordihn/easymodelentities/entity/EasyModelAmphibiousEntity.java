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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EasyModelAmphibiousEntity extends EasyModelHostEntity {

  public EasyModelAmphibiousEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  protected PathNavigation createNavigation(Level level) {
    return new AmphibiousPathNavigation(this, level);
  }

  @Override
  public boolean isPushedByFluid() {
    return false;
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(
        7,
        new RandomStrollGoal(this, 1.0) {
          @Override
          public boolean canUse() {
            return EasyModelAmphibiousEntity.this.shouldRandomStroll() && super.canUse();
          }
        });
    this.goalSelector.addGoal(
        8,
        new LookAtPlayerGoal(this, Player.class, 8.0f) {
          @Override
          public boolean canUse() {
            return EasyModelAmphibiousEntity.this.shouldLookAtPlayers() && super.canUse();
          }
        });
  }
}

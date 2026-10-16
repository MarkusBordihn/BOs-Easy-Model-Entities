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

package de.markusbordihn.easymodelentities.command;

import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.RENDERABLE_ENTITY;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.blockPosition;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.blockTarget;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.getRenderables;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.sendBlockEntityResult;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.sendMissingMutableBlockEntity;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.targetBranches;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelBlockEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.BlockTarget;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class EasyModelProfileCommand {

  private EasyModelProfileCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("profile")
        .requires(source -> source.hasPermission(2))
        .then(
            targetBranches(
                Commands.literal("get"),
                RENDERABLE_ENTITY,
                (target, blockTarget) -> target.executes(context -> get(context, blockTarget))))
        .then(
            Commands.literal("set")
                .then(
                    Commands.literal("block")
                        .then(
                            blockPosition()
                                .then(
                                    EasyModelEntitiesCommand.profileIdArgument()
                                        .executes(EasyModelProfileCommand::setBlock)))));
  }

  private static int get(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    return getRenderables(
        context,
        blockTarget,
        target ->
            sendProfileLine(
                source,
                target.blockPos().toShortString(),
                EasyModelBlockEntitiesApi.getProfileId(target.blockEntity())),
        entity ->
            sendProfileLine(
                source, entity.getName().getString(), EasyModelEntitiesApi.getProfileId(entity)));
  }

  private static int setBlock(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    ResourceLocation profileId = EasyModelEntitiesCommand.parseProfileId(context);
    Optional<String> rejectionMessage =
        EasyModelEntitiesCommand.placeBlockRejectionMessage(profileId);
    if (rejectionMessage.isPresent()) {
      source.sendFailure(Component.literal(rejectionMessage.get()));
      return 0;
    }

    BlockTarget target = blockTarget(context);
    if (target.blockEntity() == null) {
      return sendMissingMutableBlockEntity(source);
    }

    ResourceLocation requiredType =
        EasyModelServices.profileService()
            .getProfile(profileId)
            .map(EasyModelEntityProfile::hostBlockEntityType)
            .orElseThrow();
    ResourceLocation actualType =
        BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(target.blockEntity().getType());
    if (!requiredType.equals(actualType)) {
      source.sendFailure(
          Component.literal(
              "Profile "
                  + profileId
                  + " needs block entity "
                  + requiredType
                  + ", found "
                  + actualType
                  + "."));
      return 0;
    }

    if (!EasyModelBlockEntitiesApi.setProfileId(target.blockEntity(), profileId)) {
      return sendMissingMutableBlockEntity(source);
    }

    return sendBlockEntityResult(source, "Set profile " + profileId);
  }

  private static int sendProfileLine(
      CommandSourceStack source, String targetName, Optional<ResourceLocation> profileId) {
    if (profileId.isEmpty()) {
      source.sendSuccess(() -> Component.literal(targetName + ": no profile"), false);
      return 0;
    }

    source.sendSuccess(() -> Component.literal(targetName + ": profile " + profileId.get()), false);
    return 1;
  }
}

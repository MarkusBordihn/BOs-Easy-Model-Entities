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

import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.HOST_ENTITY;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.RENDERABLE_ENTITY;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.getRenderables;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.targetBranches;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.updateHosts;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelBlockEntitiesApi;
import de.markusbordihn.easymodelentities.api.EasyModelEntitiesApi;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSlot;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class EasyModelTextureCommand {

  private static final String SLOT_ARGUMENT = "slot";
  private static final String TEXTURE_ARGUMENT = "texture";

  private EasyModelTextureCommand() {}

  static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("texture")
        .requires(source -> source.hasPermission(2))
        .then(setCommand())
        .then(blendCommand())
        .then(clearCommand())
        .then(getCommand());
  }

  private static <T extends ArgumentBuilder<CommandSourceStack, T>> T withBlendLiterals(
      T builder, Function<EasyModelTextureBlend, Command<CommandSourceStack>> command) {
    for (EasyModelTextureBlend blend : EasyModelTextureBlend.values()) {
      builder.then(Commands.literal(blend.getSerializedName()).executes(command.apply(blend)));
    }
    return builder;
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setCommand() {
    return targetBranches(
        Commands.literal("set"),
        HOST_ENTITY,
        (target, blockTarget) ->
            target.then(setDefaultSlot(blockTarget)).then(setNamedSlot(blockTarget)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setDefaultSlot(boolean blockTarget) {
    return Commands.literal(EasyModelTextureSetting.DEFAULT_SLOT)
        .then(
            withBlendLiterals(
                Commands.argument(TEXTURE_ARGUMENT, ResourceLocationArgument.id())
                    .executes(
                        context ->
                            set(context, blockTarget, EasyModelTextureSetting.DEFAULT_SLOT, null)),
                blend ->
                    context ->
                        set(context, blockTarget, EasyModelTextureSetting.DEFAULT_SLOT, blend)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setNamedSlot(boolean blockTarget) {
    return Commands.literal(SLOT_ARGUMENT)
        .then(
            Commands.argument(SLOT_ARGUMENT, StringArgumentType.string())
                .then(
                    withBlendLiterals(
                        Commands.argument(TEXTURE_ARGUMENT, ResourceLocationArgument.id())
                            .executes(
                                context ->
                                    set(
                                        context,
                                        blockTarget,
                                        StringArgumentType.getString(context, SLOT_ARGUMENT),
                                        null)),
                        blend ->
                            context ->
                                set(
                                    context,
                                    blockTarget,
                                    StringArgumentType.getString(context, SLOT_ARGUMENT),
                                    blend))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> blendCommand() {
    return targetBranches(
        Commands.literal("blend"),
        HOST_ENTITY,
        (target, blockTarget) ->
            target.then(blendDefaultSlot(blockTarget)).then(blendNamedSlot(blockTarget)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> blendDefaultSlot(boolean blockTarget) {
    return withBlendLiterals(
        Commands.literal(EasyModelTextureSetting.DEFAULT_SLOT),
        blend ->
            context -> setBlend(context, blockTarget, EasyModelTextureSetting.DEFAULT_SLOT, blend));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> blendNamedSlot(boolean blockTarget) {
    return Commands.literal(SLOT_ARGUMENT)
        .then(
            withBlendLiterals(
                Commands.argument(SLOT_ARGUMENT, StringArgumentType.string()),
                blend ->
                    context ->
                        setBlend(
                            context,
                            blockTarget,
                            StringArgumentType.getString(context, SLOT_ARGUMENT),
                            blend)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> clearCommand() {
    return targetBranches(
        Commands.literal("clear"),
        HOST_ENTITY,
        (target, blockTarget) ->
            target
                .executes(context -> clear(context, blockTarget, null))
                .then(clearDefaultSlot(blockTarget))
                .then(clearNamedSlot(blockTarget)));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> clearDefaultSlot(boolean blockTarget) {
    return Commands.literal(EasyModelTextureSetting.DEFAULT_SLOT)
        .executes(context -> clear(context, blockTarget, EasyModelTextureSetting.DEFAULT_SLOT));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> clearNamedSlot(boolean blockTarget) {
    return Commands.literal(SLOT_ARGUMENT)
        .then(
            Commands.argument(SLOT_ARGUMENT, StringArgumentType.string())
                .executes(
                    context ->
                        clear(
                            context,
                            blockTarget,
                            StringArgumentType.getString(context, SLOT_ARGUMENT))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> getCommand() {
    return targetBranches(
        Commands.literal("get"),
        RENDERABLE_ENTITY,
        (target, blockTarget) -> target.executes(context -> getTexture(context, blockTarget)));
  }

  private static int set(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String slot,
      EasyModelTextureBlend blend)
      throws CommandSyntaxException {
    Optional<String> normalizedSlot = normalizedSlot(context, slot);
    if (normalizedSlot.isEmpty()) {
      return 0;
    }

    ResourceLocation texture = ResourceLocationArgument.getId(context, TEXTURE_ARGUMENT);
    if (blend == null) {
      return update(
          context,
          blockTarget,
          "Set texture",
          setting -> setting.withSlot(normalizedSlot.get(), texture));
    }

    return update(
        context,
        blockTarget,
        "Set texture",
        setting -> setting.withSlot(normalizedSlot.get(), texture, blend));
  }

  private static int setBlend(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String slot,
      EasyModelTextureBlend blend)
      throws CommandSyntaxException {
    Optional<String> normalizedSlot = normalizedSlot(context, slot);
    if (normalizedSlot.isEmpty()) {
      return 0;
    }

    return update(
        context,
        blockTarget,
        "Set texture blend",
        setting -> setting.withBlend(normalizedSlot.get(), blend));
  }

  private static int clear(
      CommandContext<CommandSourceStack> context, boolean blockTarget, String slot)
      throws CommandSyntaxException {
    if (slot == null) {
      return update(context, blockTarget, "Cleared texture", EasyModelTextureSetting::withoutSlots);
    }

    Optional<String> normalizedSlot = normalizedSlot(context, slot);
    if (normalizedSlot.isEmpty()) {
      return 0;
    }

    return update(
        context,
        blockTarget,
        "Cleared texture",
        setting -> setting.withoutSlot(normalizedSlot.get()));
  }

  private static int getTexture(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    return getRenderables(
        context,
        blockTarget,
        target -> {
          sendTextureLines(
              source,
              target.blockPos().toShortString(),
              EasyModelBlockEntitiesApi.getTextureSetting(target.blockEntity()));
          return 1;
        },
        entity -> {
          sendTextureLines(
              source, entity.getName().getString(), EasyModelEntitiesApi.getTextureSetting(entity));
          return 1;
        });
  }

  private static int update(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String action,
      UnaryOperator<EasyModelTextureSetting> update)
      throws CommandSyntaxException {
    return updateHosts(
        context,
        blockTarget,
        action,
        hostBlockEntity ->
            hostBlockEntity.setEasyModelTexture(
                update.apply(hostBlockEntity.getEasyModelTextureSetting())),
        hostEntity ->
            hostEntity.setEasyModelTexture(update.apply(hostEntity.getEasyModelTextureSetting())));
  }

  private static Optional<String> normalizedSlot(
      CommandContext<CommandSourceStack> context, String slot) {
    Optional<String> normalizedSlot = EasyModelTextureSetting.normalizeSlot(slot);
    if (normalizedSlot.isEmpty()) {
      context.getSource().sendFailure(Component.literal("Invalid texture slot name: " + slot));
    }
    return normalizedSlot;
  }

  private static void sendTextureLines(
      CommandSourceStack source, String targetName, EasyModelTextureSetting setting) {
    if (setting.isEmpty()) {
      source.sendSuccess(() -> Component.literal(targetName + ": no texture override"), false);
      return;
    }

    setting
        .slots()
        .forEach(
            (slot, textureSlot) ->
                source.sendSuccess(
                    () -> Component.literal(textureLine(targetName, slot, textureSlot)), false));
  }

  private static String textureLine(
      String targetName, String slot, EasyModelTextureSlot textureSlot) {
    String texture =
        textureSlot.texture().map(ResourceLocation::toString).orElse("original texture");
    if (textureSlot.blend() == EasyModelTextureBlend.DEFAULT) {
      return targetName + " " + slot + ": " + texture;
    }

    return targetName
        + " "
        + slot
        + ": "
        + texture
        + " ("
        + textureSlot.blend().getSerializedName()
        + ")";
  }
}

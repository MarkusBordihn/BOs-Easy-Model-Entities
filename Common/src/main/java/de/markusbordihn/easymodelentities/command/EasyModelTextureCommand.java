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

import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.BlockTarget;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.POSITION_ARGUMENT;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.TARGETS_ARGUMENT;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.blockTarget;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.isRenderableEntity;
import static de.markusbordihn.easymodelentities.command.EasyModelCommandTargets.sendResult;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.api.EasyModelRenderable;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureBlend;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSetting;
import de.markusbordihn.easymodelentities.api.data.EasyModelTextureSlot;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.entity.EasyModelEntityHost;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

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
        .then(queryCommand());
  }

  private static <T extends ArgumentBuilder<CommandSourceStack, T>> T withBlendLiterals(
      T builder, Function<EasyModelTextureBlend, Command<CommandSourceStack>> command) {
    for (EasyModelTextureBlend blend : EasyModelTextureBlend.values()) {
      builder.then(Commands.literal(blend.getSerializedName()).executes(command.apply(blend)));
    }
    return builder;
  }

  private static ArgumentBuilder<CommandSourceStack, ?> setCommand() {
    return Commands.literal("set")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    entity -> entity instanceof EasyModelEntityHost))
                        .then(setDefaultSlot(false))
                        .then(setNamedSlot(false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .then(setDefaultSlot(true))
                        .then(setNamedSlot(true))));
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
    return Commands.literal("blend")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    entity -> entity instanceof EasyModelEntityHost))
                        .then(blendDefaultSlot(false))
                        .then(blendNamedSlot(false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .then(blendDefaultSlot(true))
                        .then(blendNamedSlot(true))));
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
    return Commands.literal("clear")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context,
                                    builder,
                                    entity -> entity instanceof EasyModelEntityHost))
                        .executes(context -> clear(context, false, null))
                        .then(clearDefaultSlot(false))
                        .then(clearNamedSlot(false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .executes(context -> clear(context, true, null))
                        .then(clearDefaultSlot(true))
                        .then(clearNamedSlot(true))));
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

  private static ArgumentBuilder<CommandSourceStack, ?> queryCommand() {
    return Commands.literal("query")
        .then(
            Commands.literal("entity")
                .then(
                    Commands.argument(TARGETS_ARGUMENT, EntityArgument.entities())
                        .suggests(
                            (context, builder) ->
                                EasyModelCommandSuggestions.suggestEntities(
                                    context, builder, EasyModelCommandTargets::isRenderableEntity))
                        .executes(context -> query(context, false))))
        .then(
            Commands.literal("block")
                .then(
                    Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                        .executes(context -> query(context, true))));
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

  private static int query(CommandContext<CommandSourceStack> context, boolean blockTarget)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity() instanceof EasyModelRenderable renderable)) {
        source.sendFailure(Component.literal("No Easy Model block entity at position."));
        return 0;
      }
      sendTextureLines(
          source, target.blockPos().toShortString(), renderable.getEasyModelTextureSetting());
      return 1;
    }

    int queried = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (!isRenderableEntity(entity)) {
        continue;
      }
      sendTextureLines(source, entity.getName().getString(), textureSetting(entity));
      queried++;
    }
    return queried == 0 ? sendResult(source, "Queried texture", 0, "entities") : queried;
  }

  private static int update(
      CommandContext<CommandSourceStack> context,
      boolean blockTarget,
      String action,
      UnaryOperator<EasyModelTextureSetting> update)
      throws CommandSyntaxException {
    if (blockTarget) {
      BlockTarget target = blockTarget(context);
      if (!(target.blockEntity() instanceof EasyModelHostBlockEntity hostBlockEntity)) {
        context
            .getSource()
            .sendFailure(Component.literal("No Easy Model host block entity at position."));
        return 0;
      }
      hostBlockEntity.setEasyModelTexture(
          update.apply(hostBlockEntity.getEasyModelTextureSetting()));
      return sendResult(context.getSource(), action, 1, "block entity");
    }

    int updated = 0;
    for (Entity entity : EntityArgument.getEntities(context, TARGETS_ARGUMENT)) {
      if (entity instanceof EasyModelEntityHost hostEntity) {
        hostEntity.setEasyModelTexture(update.apply(hostEntity.getEasyModelTextureSetting()));
        updated++;
      }
    }
    return sendResult(context.getSource(), action, updated, updated == 1 ? "entity" : "entities");
  }

  private static Optional<String> normalizedSlot(
      CommandContext<CommandSourceStack> context, String slot) {
    Optional<String> normalizedSlot = EasyModelTextureSetting.normalizeSlot(slot);
    if (normalizedSlot.isEmpty()) {
      context.getSource().sendFailure(Component.literal("Invalid texture slot name: " + slot));
    }
    return normalizedSlot;
  }

  private static EasyModelTextureSetting textureSetting(Entity entity) {
    if (entity instanceof EasyModelEntityHost hostEntity) {
      return hostEntity.getEasyModelTextureSetting();
    }
    if (entity instanceof EasyModelRenderable renderable) {
      return renderable.getEasyModelTextureSetting();
    }

    return EasyModelTextureSetting.EMPTY;
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

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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.easymodelentities.Constants;
import de.markusbordihn.easymodelentities.blockentity.EasyModelHostBlockEntity;
import de.markusbordihn.easymodelentities.data.diagnostics.ModelDiagnostic;
import de.markusbordihn.easymodelentities.data.profile.EasyModelEntityProfile;
import de.markusbordihn.easymodelentities.data.profile.ModelProfileValidationIssue;
import de.markusbordihn.easymodelentities.data.profile.ModelType;
import de.markusbordihn.easymodelentities.data.renderprofile.EasyModelRenderProfile;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileStatus;
import de.markusbordihn.easymodelentities.data.renderprofile.ModelRenderProfileValidationIssue;
import de.markusbordihn.easymodelentities.registry.EasyModelServices;
import de.markusbordihn.easymodelentities.spawn.EasyModelSpawnSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class EasyModelEntitiesCommand {

  private static final String PROFILE_ID_ARGUMENT = "profile_id";
  private static final String POSITION_ARGUMENT = "pos";

  private EasyModelEntitiesCommand() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
        Commands.literal(Constants.MOD_COMMAND)
            .then(
                Commands.literal("list_profiles").executes(EasyModelEntitiesCommand::listProfiles))
            .then(
                Commands.literal("validate_profiles")
                    .requires(
                        source ->
                            source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .executes(EasyModelEntitiesCommand::validateProfiles))
            .then(
                Commands.literal("debug_profile")
                    .requires(
                        source ->
                            source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(
                        Commands.argument(PROFILE_ID_ARGUMENT, IdentifierArgument.id())
                            .suggests(
                                (context, builder) -> {
                                  EasyModelServices.profileService()
                                      .getProfileIds()
                                      .forEach(profileId -> builder.suggest(profileId.toString()));
                                  return builder.buildFuture();
                                })
                            .executes(EasyModelEntitiesCommand::debugProfile)))
            .then(
                Commands.literal("summon")
                    .requires(
                        source ->
                            source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(
                        Commands.argument(PROFILE_ID_ARGUMENT, IdentifierArgument.id())
                            .suggests(
                                (context, builder) -> {
                                  EasyModelServices.profileService()
                                      .getProfileIds()
                                      .forEach(profileId -> builder.suggest(profileId.toString()));
                                  return builder.buildFuture();
                                })
                            .executes(EasyModelEntitiesCommand::summon)))
            .then(
                Commands.literal("place_block")
                    .requires(
                        source ->
                            source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(
                        Commands.argument(PROFILE_ID_ARGUMENT, IdentifierArgument.id())
                            .suggests(
                                (context, builder) -> {
                                  EasyModelServices.profileService()
                                      .getProfileIds()
                                      .forEach(profileId -> builder.suggest(profileId.toString()));
                                  return builder.buildFuture();
                                })
                            .executes(EasyModelEntitiesCommand::placeBlock)
                            .then(
                                Commands.argument(POSITION_ARGUMENT, BlockPosArgument.blockPos())
                                    .executes(EasyModelEntitiesCommand::placeBlock))))
            .then(EasyModelAnimationCommand.register())
            .then(EasyModelBehaviorCommand.register()));
  }

  static List<String> listProfileLines() {
    List<EasyModelEntityProfile> profiles = sortedProfiles();
    if (profiles.isEmpty()) {
      return List.of("No Easy Model Entities profiles loaded.");
    }

    return profiles.stream()
        .map(
            profile ->
                profile.id()
                    + " "
                    + (profile.isActive() ? "active" : "invalid: " + profile.status().name()))
        .toList();
  }

  static List<String> validateProfileLines() {
    List<EasyModelEntityProfile> profiles = sortedProfiles();
    long activeProfiles = profiles.stream().filter(EasyModelEntityProfile::isActive).count();
    long invalidProfiles = profiles.size() - activeProfiles;
    List<String> lines = new ArrayList<>();
    lines.add(
        "Easy Model Entities profiles: "
            + activeProfiles
            + " active, "
            + invalidProfiles
            + " invalid");
    List<ModelDiagnostic> diagnostics = EasyModelServices.diagnosticsService().getDiagnostics();
    if (diagnostics.isEmpty()) {
      appendDirectProfileValidationIssues(profiles, lines);
    } else {
      appendDiagnostics(diagnostics, lines);
    }

    if (EasyModelServices.renderProfileService().getRenderProfiles().isEmpty()) {
      lines.add("Render profile diagnostics unavailable.");
      return lines;
    }

    return lines;
  }

  static List<String> debugProfileLines(Identifier profileId) {
    Optional<EasyModelEntityProfile> profile =
        EasyModelServices.profileService().getProfile(profileId);
    if (profile.isEmpty()) {
      return List.of("Unknown Easy Model Entities profile: " + profileId);
    }

    EasyModelEntityProfile profileValue = profile.get();
    List<String> lines = new ArrayList<>();
    lines.add("profile id: " + profileValue.id());
    lines.add("server profile status: " + profileValue.status().name());
    lines.add("model type: " + profileValue.modelType().getSerializedName());
    if (profileValue.modelType() == ModelType.BLOCK_ENTITY) {
      lines.add("block entity type: " + profileValue.hostBlockEntityType());
      lines.add("block entity preset: " + profileValue.blockEntityPresetType().getSerializedName());
    } else {
      lines.add("entity type: " + profileValue.hostEntityType());
      lines.add("movement type: " + profileValue.movementType().getSerializedName());
    }
    lines.add("body type: " + profileValue.bodyType().getSerializedName());
    lines.add(
        "dimensions: width="
            + profileValue.width()
            + " height="
            + profileValue.height()
            + " eye_height="
            + profileValue.eyeHeight());
    lines.add("render profile id: " + profileValue.renderProfileId());
    lines.add("expected version: " + profileValue.version());

    Optional<EasyModelRenderProfile> renderProfile =
        EasyModelServices.renderProfileService().getRenderProfile(profileValue.renderProfileId());
    if (renderProfile.isEmpty()) {
      lines.add(
          EasyModelServices.renderProfileService().getRenderProfiles().isEmpty()
              ? "client render profile status: unavailable"
              : "client render profile status: " + ModelRenderProfileStatus.MISSING_RENDER_PROFILE);
      appendProfileIssues(profileValue, lines);
      return lines;
    }

    EasyModelRenderProfile renderProfileValue = renderProfile.get();
    lines.add("client render profile status: " + renderProfileValue.status().name());
    lines.add("local render version: " + renderProfileValue.version());
    lines.add("body type mismatch: " + (renderProfileValue.bodyType() != profileValue.bodyType()));
    lines.add(
        "missing model: "
            + hasRenderIssue(renderProfileValue, ModelRenderProfileStatus.MISSING_MODEL));
    lines.add(
        "missing texture: "
            + hasRenderIssue(renderProfileValue, ModelRenderProfileStatus.MISSING_TEXTURE));
    lines.add("fallback active: " + fallbackActive(renderProfileValue));
    appendProfileIssues(profileValue, lines);
    appendRenderProfileIssues(renderProfileValue, lines);
    return lines;
  }

  static Optional<String> summonRejectionMessage(Identifier profileId) {
    return EasyModelSpawnSupport.entitySpawnRejection(profileId);
  }

  static Optional<String> placeBlockRejectionMessage(Identifier profileId) {
    return EasyModelSpawnSupport.blockPlacementRejection(profileId);
  }

  private static int listProfiles(CommandContext<CommandSourceStack> context) {
    return sendSuccess(context.getSource(), listProfileLines());
  }

  private static int validateProfiles(CommandContext<CommandSourceStack> context) {
    return sendSuccess(context.getSource(), validateProfileLines());
  }

  private static int debugProfile(CommandContext<CommandSourceStack> context) {
    Identifier profileId = parseProfileId(context);
    return sendSuccess(context.getSource(), debugProfileLines(profileId));
  }

  private static int summon(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    Identifier profileId = parseProfileId(context);
    Optional<String> rejectionMessage = summonRejectionMessage(profileId);
    if (rejectionMessage.isPresent()) {
      source.sendFailure(Component.literal(rejectionMessage.get()));
      return 0;
    }

    Optional<Entity> entity =
        EasyModelServices.entityFactory()
            .createEntity(source.getLevel(), profileId, source.getPosition());
    if (entity.isEmpty()) {
      source.sendFailure(
          Component.literal(
              "Could not create Easy Model Entities host entity for " + profileId + "."));
      return 0;
    }

    source.getLevel().addFreshEntity(entity.get());
    Identifier entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.get().getType());
    source.sendSuccess(
        () -> Component.literal("Summoned " + entityType + " with profile " + profileId + "."),
        true);
    return 1;
  }

  private static int placeBlock(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    Identifier profileId = parseProfileId(context);
    Optional<String> rejectionMessage = placeBlockRejectionMessage(profileId);
    if (rejectionMessage.isPresent()) {
      source.sendFailure(Component.literal(rejectionMessage.get()));
      return 0;
    }

    EasyModelEntityProfile profile =
        EasyModelServices.profileService().getProfile(profileId).orElseThrow();
    Identifier blockId = blockIdForProfile(profile).orElseThrow();
    Block block = BuiltInRegistries.BLOCK.get(blockId).map(Holder.Reference::value).orElse(null);
    if (block == Blocks.AIR) {
      source.sendFailure(
          Component.literal("Missing Easy Model Entities host block " + blockId + "."));
      return 0;
    }

    BlockPos blockPos = parseBlockPos(context, source);
    Level level = source.getLevel();
    BlockState blockState = block.defaultBlockState();
    if (!level.setBlock(blockPos, blockState, Block.UPDATE_ALL)) {
      source.sendFailure(
          Component.literal("Could not place Easy Model Entities block at " + blockPos + "."));
      return 0;
    }

    if (level.getBlockEntity(blockPos) instanceof EasyModelHostBlockEntity hostBlockEntity) {
      hostBlockEntity.setEasyModelProfileId(profileId);
    } else {
      source.sendFailure(
          Component.literal("Placed block at " + blockPos + " without Easy Model BlockEntity."));
      return 0;
    }

    source.sendSuccess(
        () -> Component.literal("Placed " + blockId + " with profile " + profileId + "."), true);
    return 1;
  }

  private static Identifier parseProfileId(CommandContext<CommandSourceStack> context) {
    return IdentifierArgument.getId(context, PROFILE_ID_ARGUMENT);
  }

  private static BlockPos parseBlockPos(
      CommandContext<CommandSourceStack> context, CommandSourceStack source)
      throws CommandSyntaxException {
    return context.getNodes().stream()
            .anyMatch(node -> POSITION_ARGUMENT.equals(node.getNode().getName()))
        ? BlockPosArgument.getLoadedBlockPos(context, POSITION_ARGUMENT)
        : BlockPos.containing(source.getPosition());
  }

  static Optional<Identifier> blockIdForProfile(EasyModelEntityProfile profile) {
    return EasyModelSpawnSupport.hostBlockId(profile);
  }

  private static List<EasyModelEntityProfile> sortedProfiles() {
    return EasyModelServices.profileService().getProfiles().stream()
        .sorted(Comparator.comparing(profile -> profile.id().toString()))
        .toList();
  }

  private static int sendSuccess(CommandSourceStack source, List<String> lines) {
    lines.forEach(line -> source.sendSuccess(() -> Component.literal(line), false));
    return lines.size();
  }

  private static void appendProfileIssues(EasyModelEntityProfile profile, List<String> lines) {
    profile
        .validationIssues()
        .forEach(
            issue ->
                lines.add(
                    "validation issue: "
                        + issue.status().name()
                        + " "
                        + issue.field()
                        + ": "
                        + issue.message()));
  }

  private static void appendDirectProfileValidationIssues(
      List<EasyModelEntityProfile> profiles, List<String> lines) {
    for (EasyModelEntityProfile profile : profiles) {
      for (ModelProfileValidationIssue issue : profile.validationIssues()) {
        lines.add(profile.id() + " error " + issue.status().name() + ": " + issue.message());
      }
    }
  }

  private static void appendDiagnostics(List<ModelDiagnostic> diagnostics, List<String> lines) {
    for (ModelDiagnostic diagnostic : diagnostics) {
      if (diagnostic.profileId().isPresent()) {
        lines.add(
            diagnostic.profileId().get()
                + " "
                + diagnostic.severity().name().toLowerCase()
                + " "
                + diagnostic.code()
                + ": "
                + diagnostic.message());
      }
    }
  }

  private static void appendRenderProfileIssues(
      EasyModelRenderProfile renderProfile, List<String> lines) {
    renderProfile
        .validationIssues()
        .forEach(
            issue ->
                lines.add(
                    "render validation issue: "
                        + issue.status().name()
                        + " "
                        + issue.field()
                        + ": "
                        + issue.message()));
  }

  private static boolean hasRenderIssue(
      EasyModelRenderProfile renderProfile, ModelRenderProfileStatus status) {
    return renderProfile.status() == status
        || renderProfile.validationIssues().stream()
            .map(ModelRenderProfileValidationIssue::status)
            .anyMatch(status::equals);
  }

  private static boolean fallbackActive(EasyModelRenderProfile renderProfile) {
    return renderProfile.usesFallbackModel()
        || renderProfile.usesFallbackTexture()
        || hasRenderIssue(renderProfile, ModelRenderProfileStatus.MISSING_MODEL)
        || hasRenderIssue(renderProfile, ModelRenderProfileStatus.MISSING_TEXTURE)
        || hasRenderIssue(renderProfile, ModelRenderProfileStatus.FALLBACK_ACTIVE);
  }
}

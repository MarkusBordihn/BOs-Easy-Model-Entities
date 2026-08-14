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

package de.markusbordihn.easymodelentities.model.decoder;

import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.MAX_ANIMATION_COUNT;
import static de.markusbordihn.easymodelentities.data.contract.ModelAssetBudgets.SOFT_ANIMATION_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.markusbordihn.easymodelentities.data.model.ModelAnimationBoneTrack;
import de.markusbordihn.easymodelentities.data.model.ModelAnimationClip;
import de.markusbordihn.easymodelentities.data.model.Vec3f;
import de.markusbordihn.easymodelentities.data.model.decoder.DecodedModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BbModelAnimationDecoderTest {

  private static final Identifier MODEL_ID = Identifier.fromNamespaceAndPath("example", "model");
  private static final float DELTA = 1e-4f;

  private static String modelWithAnimations(String animationsJson) {
    return "{\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"modded_entity\"},"
        + "\"resolution\":{\"width\":64,\"height\":64},"
        + "\"elements\":[{\"name\":\"c\",\"from\":[-1,0,-1],\"to\":[1,2,1],"
        + "\"origin\":[0,1,0],\"uv_offset\":[0,0],\"type\":\"cube\",\"uuid\":\"e1\"}],"
        + "\"groups\":[{\"uuid\":\"g\",\"name\":\"Body\",\"origin\":[0,0,0],\"rotation\":[0,0,0]}],"
        + "\"outliner\":[{\"uuid\":\"g\",\"children\":[\"e1\"]}],"
        + "\"animations\":"
        + animationsJson
        + "}";
  }

  private static String animationsFixture(String fixtureName) throws IOException {
    try (InputStream inputStream =
        BbModelAnimationDecoderTest.class
            .getClassLoader()
            .getResourceAsStream("bbmodel/animations/" + fixtureName)) {
      if (inputStream == null) {
        throw new IOException("Missing animations fixture " + fixtureName);
      }

      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static Resource resource(String value) {
    PackResources packResources = mock(PackResources.class);
    return new Resource(
        packResources, () -> new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
  }

  private static DecodedModel decode(String fixtureName) throws Exception {
    return decodeAnimations(animationsFixture(fixtureName));
  }

  private static DecodedModel decodeAnimations(String animationsJson) throws Exception {
    return new BlockbenchBbModelDecoder()
        .decode(MODEL_ID, resource(modelWithAnimations(animationsJson)));
  }

  private static String[] clipNames(int count) {
    String[] clipNames = new String[count];
    for (int index = 0; index < count; index++) {
      clipNames[index] = "clip" + index;
    }

    return clipNames;
  }

  private static String clips(String... clipNames) throws IOException {
    String template = animationsFixture("attack_clip.json");
    StringBuilder animations = new StringBuilder("[");
    for (int index = 0; index < clipNames.length; index++) {
      animations
          .append(index == 0 ? "" : ",")
          .append(
              template
                  .substring(template.indexOf('{'), template.lastIndexOf('}') + 1)
                  .replace("a-attack", "a-" + clipNames[index])
                  .replace("\"name\": \"attack\"", "\"name\": \"" + clipNames[index] + "\""));
    }

    return animations.append("]").toString();
  }

  @Test
  void decodesStandardClipWithRotationAndPositionKeyframes() throws Exception {
    DecodedModel model = decode("idle_rotation_position.json");

    assertEquals(1, model.animations().size());
    ModelAnimationClip clip = model.animations().get("idle");
    assertNotNull(clip);
    assertEquals(2.0f, clip.length(), DELTA);
    assertEquals(12.0f, clip.framesPerSecond(), DELTA);
    assertTrue(clip.loop());

    ModelAnimationBoneTrack track = clip.track("body");
    assertNotNull(track);

    Vec3f rotation = track.rotationAt(0.0f);
    assertEquals(-(float) Math.toRadians(90), rotation.x(), DELTA);
    assertEquals((float) Math.toRadians(45), rotation.y(), DELTA);
    assertEquals((float) Math.toRadians(30), rotation.z(), DELTA);

    Vec3f position = track.positionAt(1.0f);
    assertEquals(-2.0f, position.x(), DELTA);
    assertEquals(-4.0f, position.y(), DELTA);
    assertEquals(-6.0f, position.z(), DELTA);
  }

  @Test
  void keepsNonStandardClipNamesWithNote() throws Exception {
    String attackAnimation = animationsFixture("attack_clip.json");

    DecodedModel model = decodeAnimations(attackAnimation.replace("attack", "dance"));

    assertNotNull(model.animations().get("dance"));
    assertTrue(
        model.validationIssues().stream().anyMatch(issue -> issue.message().contains("dance")));
  }

  @Test
  void doesNotReportEmptyCustomClipAsKept() throws Exception {
    DecodedModel model = decodeAnimations("[{\"name\":\"dance\",\"animators\":{}}]");

    assertTrue(model.animations().isEmpty());
    assertFalse(
        model.validationIssues().stream()
            .anyMatch(
                issue -> issue.message().contains("Kept custom keyframe animation(s) dance")));
  }

  @Test
  void decodesAttackClip() throws Exception {
    DecodedModel model = decode("attack_clip.json");

    assertNotNull(model.animations().get("attack"));
  }

  @Test
  void decodesHurtAndDeathClips() throws Exception {
    String attackAnimation = animationsFixture("attack_clip.json");

    DecodedModel hurt = decodeAnimations(attackAnimation.replace("attack", "hurt"));
    DecodedModel death = decodeAnimations(attackAnimation.replace("attack", "death"));

    assertNotNull(hurt.animations().get("hurt"));
    assertNotNull(death.animations().get("death"));
  }

  @Test
  void discardsClipWithUnsupportedExpressionButKeepsModel() throws Exception {
    DecodedModel model = decode("walk_molang_expression.json");

    assertNull(model.animations().get("walk"));
    assertEquals(1, model.rootParts().size());
    assertTrue(
        model.validationIssues().stream().anyMatch(issue -> issue.message().contains("walk")));
  }

  @Test
  void derivesLengthFromKeyframesWhenMissing() throws Exception {
    DecodedModel model = decode("idle_missing_length.json");

    ModelAnimationClip clip = model.animations().get("idle");
    assertNotNull(clip);
    assertEquals(1.5f, clip.length(), DELTA);
  }

  @Test
  void matchesUppercaseClipNames() throws Exception {
    DecodedModel model = decode("idle_uppercase_name.json");

    assertNotNull(model.animations().get("idle"));
  }

  @Test
  @DisplayName("A numbered variant is not reported as an unplayable custom clip")
  void variantsAreNotReportedAsCustomClips() throws Exception {
    DecodedModel model = decodeAnimations(clips("idle", "idle_2", "idle_3", "wave"));

    assertTrue(
        model.validationIssues().stream()
            .noneMatch(
                issue ->
                    issue.message().contains("Kept custom") && issue.message().contains("idle_2")));
    assertTrue(
        model.validationIssues().stream()
            .anyMatch(
                issue ->
                    issue.message().contains("Kept custom") && issue.message().contains("wave")));
  }

  @Test
  @DisplayName("Clip variants do not raise a validation issue")
  void variantsDoNotRaiseValidationIssue() throws Exception {
    DecodedModel model = decodeAnimations(clips("idle", "idle_2", "idle_3"));

    assertTrue(
        model.validationIssues().stream().noneMatch(issue -> issue.message().contains("variant")));
  }

  @Test
  void decodesMoreThanSixteenClips() throws Exception {
    assertEquals(20, decodeAnimations(clips(clipNames(20))).animations().size());
  }

  @Test
  @DisplayName("Above the recommended clip count the model still decodes, with a warning")
  void moreThanThirtyTwoClipsWarnWithoutFailing() throws Exception {
    DecodedModel model = decodeAnimations(clips(clipNames(SOFT_ANIMATION_COUNT + 1)));

    assertEquals(SOFT_ANIMATION_COUNT + 1, model.animations().size());
    assertTrue(
        model.validationIssues().stream()
            .anyMatch(issue -> issue.message().contains("larger than the recommended")));
  }

  @Test
  void theRecommendedClipCountItselfDoesNotWarn() throws Exception {
    DecodedModel model = decodeAnimations(clips(clipNames(SOFT_ANIMATION_COUNT)));

    assertTrue(
        model.validationIssues().stream()
            .noneMatch(issue -> issue.message().contains("larger than the recommended")));
  }

  @Test
  void moreThanSixtyFourClipsAreRejected() {
    assertThrows(
        EasyModelDecodeException.class,
        () -> decodeAnimations(clips(clipNames(MAX_ANIMATION_COUNT + 1))));
  }

  @Test
  void theClipLimitItselfIsAccepted() throws Exception {
    assertEquals(
        MAX_ANIMATION_COUNT,
        decodeAnimations(clips(clipNames(MAX_ANIMATION_COUNT))).animations().size());
  }
}

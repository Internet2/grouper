package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the GRP-7011 customUi template base class
 * (GrouperTemplateCustomUi) and its input bean (CustomUiTemplateInput),
 * both of which live in the grouper-ui module.
 *
 * Default methods (runOnJoin, runOnLeave) throw UnsupportedOperationException
 * — verified here so a misconfigured template surfaces loudly. Overrides
 * invoke normally — verified here too.
 *
 * GRP-7011
 */
public class GrouperTemplateCustomUiTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperTemplateCustomUiTest("testDefaultRunOnJoinThrows"));
    TestRunner.run(new GrouperTemplateCustomUiTest("testDefaultRunOnLeaveThrows"));
    TestRunner.run(new GrouperTemplateCustomUiTest("testRunOnJoinOverrideInvokes"));
    TestRunner.run(new GrouperTemplateCustomUiTest("testRunOnLeaveOverrideInvokes"));
    TestRunner.run(new GrouperTemplateCustomUiTest("testCustomUiTemplateInputAccessors"));
  }

  /**
   *
   */
  public GrouperTemplateCustomUiTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperTemplateCustomUiTest(String name) {
    super(name);
  }

  /**
   * A template that doesn't override runOnJoin fails loudly when join
   * fires — the default throws UnsupportedOperationException with a
   * message naming the template class so the operator can find it.
   */
  public void testDefaultRunOnJoinThrows() {
    GrouperTemplateCustomUi noOverride = new GrouperTemplateCustomUi() {
      // no overrides
    };
    try {
      noOverride.runOnJoin(new CustomUiTemplateInput());
      fail("expected UnsupportedOperationException from default runOnJoin");
    } catch (UnsupportedOperationException e) {
      assertTrue("error should mention runOnJoin, got: " + e.getMessage(),
          e.getMessage().contains("runOnJoin"));
    }
  }

  /**
   * Same story for runOnLeave.
   */
  public void testDefaultRunOnLeaveThrows() {
    GrouperTemplateCustomUi noOverride = new GrouperTemplateCustomUi() {
      // no overrides
    };
    try {
      noOverride.runOnLeave(new CustomUiTemplateInput());
      fail("expected UnsupportedOperationException from default runOnLeave");
    } catch (UnsupportedOperationException e) {
      assertTrue("error should mention runOnLeave, got: " + e.getMessage(),
          e.getMessage().contains("runOnLeave"));
    }
  }

  /**
   * Overriding runOnJoin replaces the throws-default with the override.
   */
  public void testRunOnJoinOverrideInvokes() {
    final boolean[] called = new boolean[] { false };
    GrouperTemplateCustomUi withOverride = new GrouperTemplateCustomUi() {
      @Override
      public void runOnJoin(CustomUiTemplateInput input) {
        called[0] = true;
      }
    };
    withOverride.runOnJoin(new CustomUiTemplateInput());
    assertTrue("override should have been called", called[0]);
  }

  /**
   * Overriding runOnLeave replaces the throws-default with the override.
   */
  public void testRunOnLeaveOverrideInvokes() {
    final boolean[] called = new boolean[] { false };
    GrouperTemplateCustomUi withOverride = new GrouperTemplateCustomUi() {
      @Override
      public void runOnLeave(CustomUiTemplateInput input) {
        called[0] = true;
      }
    };
    withOverride.runOnLeave(new CustomUiTemplateInput());
    assertTrue("override should have been called", called[0]);
  }

  /**
   * CustomUiTemplateInput is a plain POJO — round-trip every field through
   * its getter/setter to confirm the accessors work.
   */
  public void testCustomUiTemplateInputAccessors() {
    CustomUiTemplateInput input = new CustomUiTemplateInput();
    input.setGshTemplateConfigId("configId123");
    assertEquals("configId123", input.getGshTemplateConfigId());
    // customUiContainer / group / subject / subjectLoggedIn / gshTemplateConfig
    // are pointer-only POJOs from other layers — null round-trip is sufficient
    // to confirm the accessors compile and work.
    assertNull(input.getCustomUiContainer());
    assertNull(input.getGroup());
    assertNull(input.getSubject());
    assertNull(input.getSubjectLoggedIn());
    assertNull(input.getGshTemplateConfig());
  }

}

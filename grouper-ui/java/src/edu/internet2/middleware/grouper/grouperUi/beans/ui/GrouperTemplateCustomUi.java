package edu.internet2.middleware.grouper.grouperUi.beans.ui;

/**
 * Base class for compiled Java GSH custom-UI templates
 * (<code>templateType=customUi</code>, <code>templateMode=compiled</code>).
 *
 * The dispatcher — a per-action bridge method on CustomUiContainer
 * (gshRunJoinScript / gshRunLeaveScript, called from the corresponding
 * UiV2CustomUi controller methods) — reads the configured GSH template id,
 * resolves the compiled class through GshTemplateClassLoaderRegistry,
 * instantiates a subclass of this class, builds a CustomUiTemplateInput,
 * and calls the action-appropriate method (runOnJoin or runOnLeave).
 *
 * <h3>Per-action methods, throws-default</h3>
 *
 * Each action the framework supports has its own method on this class
 * (runOnJoin and runOnLeave today; more will be added as the custom UI
 * framework grows). The default implementation throws
 * UnsupportedOperationException so a template configured to handle an
 * action it didn't override fails loudly rather than silently doing
 * nothing. This is the right default for custom UI specifically — the
 * framework only calls a method on a template if config said "use this
 * template for this action," so a missing override is a configuration
 * mistake.
 *
 * (Compare with Grouper hooks where no-op defaults are correct, since the
 * hooks framework fires every event method on every registered hook
 * regardless of whether the hook author cared about the event. Different
 * dispatcher semantics, different default.)
 *
 * <h3>Adding a new action method</h3>
 *
 * Future hook points (runOnButtonClick, runOnEmailSend,
 * runOnConfigureSave, etc.) are added here with the same
 * throws-default pattern. Existing templates remain unaffected because
 * they never override new methods that don't apply to them.
 *
 * <h3>Module placement</h3>
 *
 * Lives in the grouper-ui module because CustomUiContainer (which
 * CustomUiTemplateInput carries) is in grouper-ui. Other GrouperTemplate*
 * base classes live in grouper.
 *
 * GRP-7011
 */
public abstract class GrouperTemplateCustomUi {

  /**
   * Override when the template is configured to handle the user clicking
   * the enroll/join button on a custom UI screen. Typical work: add the
   * subject to the group, write attribute timestamps, audit.
   *
   * Default throws — if the template is configured for the join action
   * but doesn't override this method, fail loudly.
   *
   * @param customUiTemplateInput the bundled context — container, group,
   *   subject, logged-in subject, template config
   */
  public void runOnJoin(CustomUiTemplateInput customUiTemplateInput) {
    throw new UnsupportedOperationException(
        "runOnJoin not overridden on " + this.getClass().getName()
            + "; this template was configured for the join action but does not implement it");
  }

  /**
   * Override when the template is configured to handle the user clicking
   * the unenroll/leave button. Typical work: clean up attributes, audit,
   * possibly veto by throwing.
   *
   * Default throws — same reasoning as runOnJoin.
   *
   * @param customUiTemplateInput the bundled context
   */
  public void runOnLeave(CustomUiTemplateInput customUiTemplateInput) {
    throw new UnsupportedOperationException(
        "runOnLeave not overridden on " + this.getClass().getName()
            + "; this template was configured for the leave action but does not implement it");
  }

  // Future hook-point methods (runOnButtonClick, runOnEmailSend, etc.)
  // added here with the same throws-default pattern as new actions are
  // wired into the custom UI framework.

}

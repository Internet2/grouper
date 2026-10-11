package edu.internet2.middleware.grouper.app.provisioning;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * GRP-7458: unit tests for the unexpected-error (UNE) threshold decision used by
 * {@link GrouperProvisioningLogic#errorHandling()}.  The decision is a pure static method so it can be
 * tested without building a full provisioner run.
 */
public class GrouperProvisioningErrorHandlingThresholdTest extends TestCase {

  public GrouperProvisioningErrorHandlingThresholdTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningErrorHandlingThresholdTest("testUnexpectedErrorPercentThreshold"));
  }

  /**
   * A fatal / systemic failure always fails the run, regardless of the daemonShouldFail toggle or thresholds.
   */
  public void testFatalErrorAlwaysFails() {
    // fatal=true overrides everything: even daemonShouldFail=false and generous thresholds
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(true, false, false, 0, 100, 1000, 100));
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(true, true, false, 1, 100, 1000, 100));
  }

  /**
   * If the daemon is configured not to fail on object errors, the run never fails, regardless of (non-fatal) errors.
   */
  public void testDaemonShouldNotFailNeverFails() {
    // non-unexpected error present, lots of unexpected errors, but daemonShouldFail is false and not fatal
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, false, true, 100, 100, 0, 0));
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, false, false, 100, 100, 0, 0));
  }

  /**
   * No errors at all -> the run does not fail.
   */
  public void testNoErrorsDoesNotFail() {
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 0, 100, 0, 0));
  }

  /**
   * A non-unexpected (e.g. validation) object error keeps the historical all-or-nothing behavior and fails the run.
   */
  public void testNonUnexpectedErrorFailsImmediately() {
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, true, 0, 100, 0, 0));
    // still fails even with generous unexpected thresholds, because the non-unexpected error is all-or-nothing
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, true, 0, 100, 1000, 100));
  }

  /**
   * Default thresholds (0 / 0) preserve the historical behavior: a single unexpected error fails the run.
   */
  public void testDefaultThresholdsFailOnFirstUnexpectedError() {
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 1, 100, 0, 0));
  }

  /**
   * With an absolute count threshold, unexpected errors at or below the threshold do not fail the run, and
   * exceeding it does.
   */
  public void testUnexpectedErrorCountThreshold() {
    // maxCount 5, percent effectively disabled (100): 5 errors out of 100 -> under count, under percent -> no fail
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 5, 100, 5, 100));
    // 6 errors out of 100 -> count 6 > 5 -> fail
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 6, 100, 5, 100));
  }

  /**
   * With a percent threshold, the run fails when the percent of processed objects with unexpected errors is
   * exceeded, even if the absolute count threshold is not.
   */
  public void testUnexpectedErrorPercentThreshold() {
    // maxCount high (100), maxPercent 10: 10 of 100 = 10% -> not greater than 10 -> no fail
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 10, 100, 100, 10));
    // 11 of 100 = 11% > 10 -> fail (percent trips even though count 11 <= 100)
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 11, 100, 100, 10));
  }

  /**
   * Percent uses long math, so a rare error in a large run is not silently truncated to 0%.
   */
  public void testPercentNoIntegerTruncation() {
    // 1 of 1000 = 0.1%.  With maxPercent 0 this must still count as > 0% and fail (no truncation to 0)
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 1, 1000, 1000, 0));
    // with maxPercent 1, 0.1% is not greater than 1% -> no fail (and count 1 <= 1000)
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 1, 1000, 1000, 1));
  }

  /**
   * Guard against divide-by-zero: when nothing was processed but an unexpected error exists, percent is treated
   * as 100, so any percent threshold below 100 fails the run.
   */
  public void testZeroObjectsProcessedTreatedAsFullPercent() {
    // maxCount high so only percent can trip; totalObjectsProcessed 0 -> percent 100 > 99 -> fail
    assertTrue(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 1, 0, 1000, 99));
    // percent threshold of 100 is not exceeded by 100 -> no fail from percent, and count 1 <= 1000 -> no fail
    assertFalse(GrouperProvisioningLogic.errorHandlingShouldFailRun(false, true, false, 1, 0, 1000, 100));
  }
}

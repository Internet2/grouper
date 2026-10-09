/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.agent;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgent;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentTurnResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * one turn of the agent running in a background thread, which the screen polls.  written by the
 * background thread and read by request threads, so the fields are volatile, and the status is
 * written last so a request which sees it finished also sees the result and the conversation as
 * the thread left them
 */
public class GrouperUiAgentRun {

  /**
   * where the run is
   */
  public static enum Status {

    /** still working */
    running,

    /** finished, see the result */
    done,

    /** failed; the error is in the log, not kept here */
    failed,

    /** never started: every agent thread on this node was busy */
    busy,

    /** never started: the agent is not set up correctly, e.g. no external system configured */
    notSetUp;
  }

  /** identifies this run, so a poll for an earlier run is ignored */
  private final String runId = GrouperUtil.uniqueId();

  /** when it started */
  private final long startedMillis = System.currentTimeMillis();

  /** where the run is */
  private volatile Status status = Status.running;

  /** what came of it, when done */
  private volatile GrouperAiAgentTurnResult result;

  /** the agent running the turn, so the user can stop it */
  private volatile GrouperAiAgent grouperAiAgent;

  /** if the user asked to stop */
  private volatile boolean cancelRequested = false;

  /**
   * @param theGrouperAiAgent the agent running the turn
   */
  void assignGrouperAiAgent(GrouperAiAgent theGrouperAiAgent) {
    this.grouperAiAgent = theGrouperAiAgent;
  }

  /**
   * the user asked to stop.  the turn stops before its next model call, see
   * {@link GrouperAiAgent#requestCancel()}
   */
  public void requestCancel() {
    this.cancelRequested = true;
    GrouperAiAgent theGrouperAiAgent = this.grouperAiAgent;
    if (theGrouperAiAgent != null) {
      theGrouperAiAgent.requestCancel();
    }
  }

  /**
   * @return if the user asked to stop
   */
  public boolean isCancelRequested() {
    return this.cancelRequested;
  }

  /**
   * @return seconds left in the turn's current wait for the AI provider's rate limit to clear, 0
   * if it is not waiting for one
   */
  public long getRateLimitWaitSecondsLeft() {
    GrouperAiAgent theGrouperAiAgent = this.grouperAiAgent;
    return theGrouperAiAgent == null ? 0 : theGrouperAiAgent.getRateLimitWaitSecondsLeft();
  }

  /**
   * @return identifies this run
   */
  public String getRunId() {
    return this.runId;
  }

  /**
   * @return when it started
   */
  public long getStartedMillis() {
    return this.startedMillis;
  }

  /**
   * @return how long it has been running, in seconds
   */
  public long getElapsedSeconds() {
    return (System.currentTimeMillis() - this.startedMillis) / 1000;
  }

  /**
   * @return where the run is
   */
  public Status getStatus() {
    return this.status;
  }

  /**
   * @return true if still working
   */
  public boolean isRunning() {
    return this.status == Status.running;
  }

  /**
   * @return what came of it, when done
   */
  public GrouperAiAgentTurnResult getResult() {
    return this.result;
  }

  /**
   * the run finished.  the result is set before the status, see the class comment
   * @param theResult what came of it
   */
  void finish(GrouperAiAgentTurnResult theResult) {
    this.result = theResult;
    this.status = Status.done;
  }

  /**
   * the run failed.  the error itself is logged, not kept, since it can carry detail from the
   * provider which is not for the screen
   */
  void fail() {
    this.status = Status.failed;
  }

  /**
   * the run never started because every agent thread on this node was busy
   */
  void busy() {
    this.status = Status.busy;
  }

  /**
   * the run never started because the agent is not set up correctly
   */
  void notSetUp() {
    this.status = Status.notSetUp;
  }

}

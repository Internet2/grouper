/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgent;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentConversation;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentSettings;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentUsage;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentUsageReportRow;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentToolCall;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentTurnResult;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AiAgentContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAiAgentMessage;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentCaller;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentConversationService;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentRun;
import edu.internet2.middleware.grouper.ui.agent.GrouperUiAgentToolRunner;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * the AI agent screen (Miscellaneous &gt; AI assistant): a conversation with the agent, the scope the
 * user allows it for the session, and the writes waiting for the user's approval.
 *
 * <p>a turn runs in a background thread (see {@link GrouperUiAgentConversationService}), so
 * sending a message or an approval starts the turn and returns at once, and the page polls
 * {@link #aiAgentStatus} until the turn is over.</p>
 */
public class UiV2AiAgent extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2AiAgent.class);

  /** how often the page asks whether the turn is over */
  private static final int POLL_MILLIS = 2000;

  /**
   * the screen
   * @param request
   * @param response
   */
  public void aiAgent(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        return;
      }

      // e.g. the page was left while a turn finished, so no poll saw it end
      storeSessionIfTurnOver();

      populateScope();
      populateConversation(null);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/aiAgent/aiAgent.jsp"));

      // coming back to the screen while a turn is still going
      AiAgentContainer aiAgentContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer();
      if (aiAgentContainer.isRunning()) {
        schedulePoll(aiAgentContainer.getRunId());
      }

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * send a message
   * @param request
   * @param response
   */
  public void aiAgentSend(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      // the turn's thread takes this session's subject, see GrouperCallable, so the checks run
      // during the turn have a session too
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String text = StringUtils.trimToNull(request.getParameter("aiAgentMessage"));
      if (text == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#aiAgentMessageId", text("aiAgentMessageRequired")));
        return;
      }

      // checked here as well as in the agent, so the message stays in the box to be shortened
      // rather than being cleared and then refused
      if (text.length() > GrouperAiAgentSettings.fromConfig().getMaxUserMessageChars()) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#aiAgentMessageId", text("aiAgentStatus_messageTooLong")));
        return;
      }

      if (isRunning()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentStillWorking")));
        return;
      }

      // the agent checks this too; checking here keeps the message in the box rather than clearing it
      // and then refusing it
      GrouperAiAgentSettings settings = GrouperUiAgentConversationService.retrieveSettings(
          GrouperUiFilter.retrieveSubjectLoggedIn());
      // with no member yet there is no usage yet either, so there is nothing to check here; the turn
      // creates the member and checks again
      Long memberInternalId = GrouperUiAgentToolRunner.retrieveMemberInternalId(
          GrouperUiFilter.retrieveSubjectLoggedIn(), false);
      if (memberInternalId != null
          && (settings.getMaxQuestionsPerUserPerDay() != null || settings.getMaxTokensPerUserPerDay() != null)) {
        GrouperAiAgentUsage usage = new GrouperAiAgentUsage(memberInternalId, settings);
        // in the same order as the agent checks them.  if usage cannot be read the message is
        // refused, as the agent would refuse it
        try {
          if (!usage.isWithinQuestionLimit()) {
            showTurnStatusNow(text("aiAgentStatus_questionLimitReached"), true);
            return;
          }
          if (!usage.isWithinLimit()) {
            showTurnStatusNow(text("aiAgentStatus_limitReached"), true);
            return;
          }
        } catch (RuntimeException re) {
          LOG.error("Error reading AI agent usage, so the message is refused until it can be read", re);
          showTurnStatusNow(text("aiAgentStatus_usageUnavailable"), true);
          return;
        }
      }
      GrouperAiAgentConversation existingConversation = GrouperUiAgentConversationService.retrieveConversation();
      if (settings.getMaxTokensPerConversation() != null && existingConversation != null
          && GrouperAiAgent.conversationLimitTokens(existingConversation) >= settings.getMaxTokensPerConversation()) {
        showTurnStatusNow(text("aiAgentStatus_conversationLimitReached"), true);
        return;
      }
      if (existingConversation != null && existingConversation.isContextFull()) {
        showTurnStatusNow(text("aiAgentStatus_contextFull"), true);
        return;
      }

      // what the screen shows while the turn runs: the conversation so far and the new message.
      // taken before the turn starts, since the turn adds the message to the conversation itself
      List<GuiAiAgentMessage> snapshot = GuiAiAgentMessage.convert(
          GrouperUiAgentConversationService.retrieveConversation(), text);

      GrouperUiAgentRun run = GrouperUiAgentConversationService.startSendUserMessage(text);
      if (!checkStarted(run)) {
        return;
      }

      populateConversation(snapshot);
      renderConversation();
      announce(text("aiAgentAnnounceWorking"));
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#aiAgentMessageId').val('')"));
      schedulePoll(run.getRunId());

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the user decided on the writes waiting for approval: the checked ones run, the rest are
   * declined
   * @param request
   * @param response
   */
  public void aiAgentResolve(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      // the turn's thread takes this session's subject, see GrouperCallable, so the checks run
      // during the turn have a session too
      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (isRunning()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentStillWorking")));
        return;
      }

      GrouperAiAgentConversation conversation = GrouperUiAgentConversationService.retrieveConversation();
      if (conversation == null || conversation.getPendingToolCalls().size() == 0) {
        // e.g. a form left open in another tab after the changes were decided there.  redrawn so the
        // stale form goes away
        assignTurnStatus(text("aiAgentNothingPending"), true);
        populateConversation(null);
        renderConversation();
        moveToConversationFocus();
        return;
      }

      // only ids of calls actually waiting count.  what runs is the stored call, never anything else
      // from the request
      Set<String> pendingIds = new HashSet<String>();
      for (GrouperAiAgentToolCall toolCall : conversation.getPendingToolCalls()) {
        pendingIds.add(toolCall.getId());
      }

      // the form has to have shown exactly what is waiting now.  one left open in another tab while
      // the waiting changes were replaced would otherwise decline changes the user never saw, since
      // everything not approved is declined, or approve them with Make all.  nothing is run or
      // declined; the user is shown the changes as they are now.  the form sends one fingerprint
      // rather than the ids, since the UI's ajax sends only the last of several same named hidden fields
      if (!StringUtils.equals(request.getParameter("aiAgentShownFingerprint"),
          AiAgentContainer.pendingFingerprint(pendingIds))) {
        assignTurnStatus(text("aiAgentPendingChanged"), false);
        populateConversation(null);
        renderConversation();
        moveToConversationFocus();
        announce(text("aiAgentPendingChanged"));
        return;
      }

      // all, checked or decline, from the button the user selected
      String decision = request.getParameter("aiAgentDecision");
      Set<String> approvedIds = new HashSet<String>();
      if (StringUtils.equals("all", decision)) {
        // exactly the changes the form showed, as the fingerprint just confirmed
        approvedIds.addAll(pendingIds);
      } else if (!StringUtils.equals("decline", decision)) {
        for (String approvedId : parameterValues(request, "aiAgentApprove")) {
          if (pendingIds.contains(approvedId)) {
            approvedIds.add(approvedId);
          }
        }

        // "make the checked changes" with none checked would decline them all, which is easy to do
        // by mistake after reading the list and agreeing with it.  declining is what Decline all is for
        if (approvedIds.size() == 0) {
          showTurnStatusNow(text("aiAgentApproveNoneChecked"), true);
          return;
        }
      }

      List<GuiAiAgentMessage> snapshot = GuiAiAgentMessage.convert(conversation, null);

      GrouperUiAgentRun run = GrouperUiAgentConversationService.startResolvePendingToolCalls(approvedIds);
      if (!checkStarted(run)) {
        return;
      }

      populateConversation(snapshot);
      renderConversation();
      announce(text("aiAgentAnnounceWorking"));
      schedulePoll(run.getRunId());

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the page asks whether the turn is over
   * @param request
   * @param response
   */
  public void aiAgentStatus(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        // e.g. removed from the group, or the agent turned off, during the turn.  the turn is stopped
        // before its next step, so losing access takes effect now rather than when the message ends.
        // polling stops here, so the working indicator and its Stop button are taken away rather than
        // left spinning
        GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();
        if (run != null && run.isRunning()) {
          run.requestCancel();
        }
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newScript("$('#aiAgentWorkingDivId').remove()"));
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String runId = request.getParameter("runId");
      GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();

      // a poll for a run which is not the current one: it was replaced from another tab, or is gone,
      // e.g. after a new login or a restart.  the page would otherwise keep showing it as working, so
      // it is redrawn as things are now, and follows the current run if there is one
      if (run == null || !StringUtils.equals(runId, run.getRunId())) {
        storeSessionIfTurnOver();
        populateConversation(null);
        renderConversation();
        if (run != null && run.isRunning()) {
          schedulePoll(run.getRunId());
        }
        return;
      }

      if (run.isRunning()) {
        // only the elapsed time changes.  the rest of the indicator, including the Stop button and
        // the status text screen readers announce, is left alone so it does not lose focus or
        // repeat every poll
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#aiAgentElapsedId",
            "(" + run.getElapsedSeconds() + "s)"));

        // and, while it waits for the AI provider's rate limit to clear, how long until it tries
        // again, so a long wait is not a mystery.  not a live region, so it is not read out each poll
        long rateLimitWaitSeconds = run.getRateLimitWaitSecondsLeft();
        String rateLimitWaitText = "";
        if (rateLimitWaitSeconds > 0) {
          GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer()
            .setRateLimitWaitSeconds(rateLimitWaitSeconds);
          rateLimitWaitText = text("aiAgentRateLimitWaiting");
        }
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#aiAgentRateLimitId", rateLimitWaitText));

        schedulePoll(run.getRunId());
        return;
      }

      // the turn is over: its messages and any changes waiting for approval go back into the session
      storeSessionIfTurnOver();

      // the status is shown in the conversation, next to the approval box, rather than at the top of
      // the page
      String statusText = null;
      if (run.getStatus() == GrouperUiAgentRun.Status.failed) {
        statusText = text("aiAgentStatusFailed");
        assignTurnStatus(statusText, true);
      } else {
        GrouperAiAgentTurnResult result = run.getResult();
        if (result != null && result.getStatus() != null
            && result.getStatus() != GrouperAiAgentTurnResult.Status.answered) {
          boolean error = result.getStatus() != GrouperAiAgentTurnResult.Status.needsConfirmation
              && result.getStatus() != GrouperAiAgentTurnResult.Status.cancelled;
          statusText = text("aiAgentStatus_" + result.getStatus().name());
          assignTurnStatus(statusText, error);
        }
      }

      populateConversation(null);
      renderConversation();
      moveToConversationFocus();
      announce(statusText != null ? statusText : text("aiAgentAnnounceAnswered"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * stop the running turn.  it stops before its next model call; a call already in progress
   * finishes first, and the poll picks up the end as usual
   * @param request
   * @param response
   */
  public void aiAgentStop(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      // someone who has lost access has their turn stopped by the next status poll instead, see
      // aiAgentStatus
      if (!checkEnabled()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String runId = request.getParameter("runId");
      GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();

      // only the turn this session is running, and only while it is running
      if (run == null || !StringUtils.equals(runId, run.getRunId()) || !run.isRunning()) {
        return;
      }

      run.requestCancel();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#aiAgentWorkingStatusId", text("aiAgentStopping")));
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#aiAgentStopButtonId').hide()"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * start over
   * @param request
   * @param response
   */
  public void aiAgentNewConversation(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      if (!GrouperUiAgentConversationService.newConversation()) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentStillWorking")));
        return;
      }

      populateConversation(null);
      renderConversation();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, text("aiAgentNewConversationDone")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * save what the agent may do in this session
   * @param request
   * @param response
   */
  public void aiAgentScopeSubmit(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkEnabled()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      // only categories the user's groups allow are kept.  anything else would do nothing, and
      // keeping it would make the saved scope say something untrue
      Set<GrouperToolCategory> allowed = GrouperUiAgentToolRunner.retrieveCategoriesAllowedByMembership(
          GrouperUiAgentCaller.fromRequest());
      Set<GrouperToolCategory> chosen = new LinkedHashSet<GrouperToolCategory>();
      for (String value : parameterValues(request, "aiAgentScope")) {
        for (GrouperToolCategory category : allowed) {
          if (StringUtils.equals(category.name(), value)) {
            chosen.add(category);
          }
        }
      }

      SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
      sessionContainer.setAiAgentToolScope(chosen);
      sessionContainer.storeToSession();

      populateScope();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#aiAgentScopeDivId",
          "/WEB-INF/grouperUi2/aiAgent/aiAgentScope.jsp"));
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, text("aiAgentScopeSaved")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /** most users the usage report shows */
  private static final int USAGE_REPORT_MAX_ROWS = 1000;

  /**
   * the admin usage report: AI agent usage per user over a range of days, in tokens.  Grouper
   * sysadmins only, since it names individual users.  available
   * whether or not the agent is on, so past usage can still be looked at
   * @param request
   * @param response
   */
  public void aiAgentUsageReport(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkSysadmin()) {
        return;
      }

      // the last 30 days, today included
      // in the agent's time zone, the one usage days are recorded in
      Calendar calendar = Calendar.getInstance(GrouperAiAgentUsage.timeZone());
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      simpleDateFormat.setTimeZone(GrouperAiAgentUsage.timeZone());
      String to = simpleDateFormat.format(calendar.getTime());
      calendar.add(Calendar.DAY_OF_YEAR, -29);
      String from = simpleDateFormat.format(calendar.getTime());

      populateUsageReport(from, to);

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/aiAgent/aiAgentUsageReport.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * run the admin usage report for the days on the form
   * @param request
   * @param response
   */
  public void aiAgentUsageReportSubmit(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkSysadmin()) {
        return;
      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String from = StringUtils.trimToNull(request.getParameter("aiAgentReportFrom"));
      String to = StringUtils.trimToNull(request.getParameter("aiAgentReportTo"));

      if (day(from) == null || day(to) == null || day(from) > day(to)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentReportInvalidDates")));
        return;
      }

      populateUsageReport(from, to);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#aiAgentUsageReportResultsId",
          "/WEB-INF/grouperUi2/aiAgent/aiAgentUsageReportResults.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * fill in the usage report
   * @param from first day, yyyy-mm-dd, already checked
   * @param to last day, yyyy-mm-dd, already checked
   */
  private static void populateUsageReport(String from, String to) {

    AiAgentContainer aiAgentContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer();
    aiAgentContainer.setUsageReportFrom(from);
    aiAgentContainer.setUsageReportTo(to);

    // tokens only.  no cost estimate: prices change, models change, and a day can mix models, so
    // today's prices applied to history would mislead.  actual charges are the provider's or the
    // gateway's to report
    List<GrouperAiAgentUsageReportRow> rows = GrouperAiAgentUsageReportRow.retrieveReport(day(from), day(to));

    GrouperAiAgentUsageReportRow total = new GrouperAiAgentUsageReportRow();
    for (GrouperAiAgentUsageReportRow row : rows) {
      total.add(row);
    }

    // most first, counted as for the limits: input not read from the cache, plus output
    Collections.sort(rows, new Comparator<GrouperAiAgentUsageReportRow>() {

      public int compare(GrouperAiAgentUsageReportRow row1, GrouperAiAgentUsageReportRow row2) {
        return Long.compare(row2.getLimitTokens(), row1.getLimitTokens());
      }
    });

    if (rows.size() > USAGE_REPORT_MAX_ROWS) {
      rows = new ArrayList<GrouperAiAgentUsageReportRow>(rows.subList(0, USAGE_REPORT_MAX_ROWS));
      aiAgentContainer.setUsageReportTruncated(true);
    }

    aiAgentContainer.setUsageReportRows(rows);
    aiAgentContainer.setUsageReportTotal(total);
  }

  /**
   * @param yyyyMmDd a day from the form, yyyy-mm-dd
   * @return the day as yyyymmdd, as the usage table keeps it, or null if it is not a real day
   */
  static Long day(String yyyyMmDd) {
    if (yyyyMmDd == null || !yyyyMmDd.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return null;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    simpleDateFormat.setLenient(false);
    try {
      simpleDateFormat.parse(yyyyMmDd);
    } catch (ParseException pe) {
      return null;
    }
    return Long.parseLong(yyyyMmDd.replace("-", ""));
  }

  /**
   * @return true if the user is a Grouper sysadmin, otherwise shows a message and returns false
   */
  private static boolean checkSysadmin() {
    if (PrivilegeHelper.isWheelOrRoot(GrouperUiFilter.retrieveSubjectLoggedIn())) {
      return true;
    }
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
        text("aiAgentReportNotAllowed")));
    return false;
  }

  /**
   * @return true if the agent is on and the user is in the group allowed to use it, otherwise
   * shows a message and returns false.  every action checks this, polls included, so removing
   * someone from the group stops them within a minute, even mid conversation
   */
  private boolean checkEnabled() {
    if (!GrouperUiAgentConversationService.isEnabled()) {
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          text("aiAgentNotEnabled")));
      return false;
    }
    if (!GrouperUiAgentConversationService.isInUsersGroup(GrouperUiFilter.retrieveSubjectLoggedIn())) {
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          text("aiAgentNotAllowed")));
      return false;
    }
    // in the group, but no MCP group gives the agent anything to do for them
    if (!GrouperUiAgentConversationService.hasAnyToolAccess(GrouperUiFilter.retrieveSubjectLoggedIn())) {
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          text("aiAgentNoMcpAccess")));
      return false;
    }
    return true;
  }

  /**
   * @param run what starting a turn returned
   * @return true if the turn started, otherwise shows why not and returns false.  the text box is
   * left as it was, so a message can be sent again
   */
  private static boolean checkStarted(GrouperUiAgentRun run) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    if (run == null) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentStillWorking")));
      return false;
    }
    if (run.getStatus() == GrouperUiAgentRun.Status.busy) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentBusy")));
      return false;
    }
    if (run.getStatus() == GrouperUiAgentRun.Status.notSetUp) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("aiAgentNotSetUp")));
      return false;
    }
    return true;
  }

  /**
   * set the session container in the session again once the turn is over.  the turn changes the
   * conversation in place, on its own thread, which has no request to do this from; session
   * replication generally copies an attribute when it is set, not when the object in it changes, so
   * without this a replicated copy of the session could miss the turn's messages and the changes
   * waiting for approval.  called from requests which see the turn is over
   */
  private static void storeSessionIfTurnOver() {
    GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();
    if (run != null && run.isRunning()) {
      return;
    }
    SessionContainer.retrieveFromSession().storeToSession();
  }

  /**
   * @return true if a turn is running for this session
   */
  private static boolean isRunning() {
    GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();
    return run != null && run.isRunning();
  }

  /**
   * fill in the conversation part of the screen
   * @param snapshot the lines to show while a turn is starting, or null to read the conversation
   */
  private static void populateConversation(List<GuiAiAgentMessage> snapshot) {

    AiAgentContainer aiAgentContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer();

    GrouperAiAgentConversation conversation = GrouperUiAgentConversationService.retrieveConversation();
    GrouperUiAgentRun run = GrouperUiAgentConversationService.retrieveRun();

    boolean running = run != null && run.isRunning();
    aiAgentContainer.setRunning(running);
    if (running) {
      aiAgentContainer.setRunId(run.getRunId());
      aiAgentContainer.setElapsedSeconds(run.getElapsedSeconds());
      aiAgentContainer.setStopping(run.isCancelRequested());
      aiAgentContainer.setRateLimitWaitSeconds(run.getRateLimitWaitSecondsLeft());
    }

    aiAgentContainer.setGuiMessages(snapshot != null ? snapshot : GuiAiAgentMessage.convert(conversation, null));

    if (conversation != null) {
      // approvals are only offered when nothing is running, so the list is not being changed
      if (!running) {
        aiAgentContainer.setPendingConfirmations(
            new ArrayList<GrouperAiAgentTurnResult.PendingConfirmation>(conversation.getPendingConfirmations()));
      }
      aiAgentContainer.setSummarized(!StringUtils.isBlank(conversation.getSummary()));
      aiAgentContainer.setInputTokens(conversation.getInputTokens());
      aiAgentContainer.setCacheReadTokens(conversation.getCacheReadTokens());
      aiAgentContainer.setOutputTokens(conversation.getOutputTokens());
    }

    // the user's own limits, raised by any group overrides they are in
    GrouperAiAgentSettings settings = GrouperUiAgentConversationService.retrieveSettings(
        GrouperUiFilter.retrieveSubjectLoggedIn());

    // this conversation's usage against its budget, from the conversation's own totals
    if (settings.getMaxTokensPerConversation() != null) {
      aiAgentContainer.setMaxTokensPerConversation(settings.getMaxTokensPerConversation());
      aiAgentContainer.setConversationTokensUsed(
          conversation == null ? 0 : GrouperAiAgent.conversationLimitTokens(conversation));
    }

    // today's usage against the daily limits, across all of the user's conversations and nodes.
    // only worth a database read when there is a limit to show it against
    Long maxTokensPerDay = settings.getMaxTokensPerUserPerDay();
    Long maxQuestionsPerDay = settings.getMaxQuestionsPerUserPerDay();
    if (maxTokensPerDay != null || maxQuestionsPerDay != null) {
      try {
        // drawing the screen never creates the member: with none yet, nothing has been used today
        Long memberInternalId = GrouperUiAgentToolRunner.retrieveMemberInternalId(
            GrouperUiFilter.retrieveSubjectLoggedIn(), false);
        long[] usage = memberInternalId == null ? new long[5]
            : GrouperAiAgentUsage.retrieveUsage(memberInternalId, GrouperAiAgentUsage.today());
        if (maxQuestionsPerDay != null) {
          aiAgentContainer.setQuestionsUsedToday(usage[0]);
          aiAgentContainer.setMaxQuestionsPerDay(maxQuestionsPerDay);
        }
        if (maxTokensPerDay != null) {
          aiAgentContainer.setTokensUsedToday(GrouperAiAgentUsage.limitTokens(usage[2], usage[3], usage[4]));
          aiAgentContainer.setMaxTokensPerDay(maxTokensPerDay);
        }
      } catch (RuntimeException re) {
        // the screen works without it
        LOG.error("Error reading AI agent usage", re);
      }
    }
  }

  /**
   * fill in the scope form: the categories the user's groups allow, and which are chosen
   */
  private static void populateScope() {

    AiAgentContainer aiAgentContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer();

    Set<GrouperToolCategory> allowed = GrouperUiAgentToolRunner.retrieveCategoriesAllowedByMembership(
        GrouperUiAgentCaller.fromRequest());
    Set<GrouperToolCategory> chosen = SessionContainer.retrieveFromSession().getAiAgentToolScope();

    List<AiAgentContainer.GuiAiAgentScopeOption> scopeOptions = new ArrayList<AiAgentContainer.GuiAiAgentScopeOption>();
    for (GrouperToolCategory category : allowed) {
      scopeOptions.add(new AiAgentContainer.GuiAiAgentScopeOption(category.name(), chosen.contains(category)));
    }
    aiAgentContainer.setScopeOptions(scopeOptions);
  }

  /**
   * tell screen reader users what happened, in the status line outside the conversation, so the
   * conversation itself is not read out again each time it is redrawn
   * @param message what to say, plain text
   */
  private static void announce(String message) {
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtml("#aiAgentAnnounceId",
        GrouperUtil.escapeHtml(message, true)));
  }

  /**
   * show a turn's status in the conversation, next to the approval box, the next time the
   * conversation is drawn.  the top of the page is not used: its message scrolls the page away
   * from the conversation the user is working in
   * @param statusText the status, text from the text file
   * @param error true for an error, false for information
   */
  private static void assignTurnStatus(String statusText, boolean error) {
    AiAgentContainer aiAgentContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAiAgentContainer();
    aiAgentContainer.setTurnStatusText(statusText);
    aiAgentContainer.setTurnStatusError(error);
  }

  /**
   * show a turn's status in the conversation without drawing it again, e.g. a message refused
   * before a turn started, and bring it into view.  the message the user typed stays in its box
   * @param statusText the status, text from the text file
   * @param error true for an error, false for information
   */
  private static void showTurnStatusNow(String statusText, boolean error) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#aiAgentTurnStatusId",
        "<div class=\"alert " + (error ? "alert-error" : "alert-info") + "\">" + statusText + "</div>"));
    guiResponseJs.addAction(GuiScreenAction.newScript(
        "if ($('#aiAgentTurnStatusId').length) { $('#aiAgentTurnStatusId')[0].scrollIntoView({block: 'nearest'}); }"));
    announce(statusText);
  }

  /**
   * after the conversation is drawn, keep the user where they are working: on the changes waiting
   * for approval if there are any, so a keyboard user can decide straight away, or else on the
   * turn's status.  only scrolled to, not focused, so a screen reader does not read the status
   * twice: it is announced already
   */
  private static void moveToConversationFocus() {
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newScript(
        "if ($('#aiAgentApprovalsHeaderId').length) { $('#aiAgentApprovalsHeaderId').focus(); } "
        + "else if ($('#aiAgentTurnStatusId .alert').length) { $('#aiAgentTurnStatusId')[0].scrollIntoView({block: 'nearest'}); }"));
  }

  /**
   * redraw the conversation part of the screen
   */
  private static void renderConversation() {
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp(
        "#aiAgentConversationDivId", "/WEB-INF/grouperUi2/aiAgent/aiAgentConversation.jsp"));
  }

  /**
   * ask the page to poll for the turn in a moment.  only while the screen is still showing, so
   * leaving the screen stops the polling.  there is one timer for the page: scheduling replaces
   * any earlier one, so leaving the screen and coming back while a turn runs does not start a
   * second chain of polls alongside the first
   * @param runId the run, made by GrouperUtil.uniqueId so safe in a script
   */
  private static void schedulePoll(String runId) {
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newScript(
        "if (window.grouperAiAgentPollTimer) { clearTimeout(window.grouperAiAgentPollTimer); } "
        + "window.grouperAiAgentPollTimer = setTimeout(function() { window.grouperAiAgentPollTimer = null; "
        + "if ($('#aiAgentConversationDivId').length) { "
        + "ajax('../app/UiV2AiAgent.aiAgentStatus?runId=" + runId + "'); } }, " + POLL_MILLIS + ")"));
  }

  /**
   * a multi valued parameter, however the browser sent it
   * @param request the request
   * @param name the parameter
   * @return the values, never null
   */
  private static List<String> parameterValues(HttpServletRequest request, String name) {
    List<String> result = new ArrayList<String>();
    String[] names = new String[] {name + "[]", name};
    for (String theName : names) {
      String[] values = request.getParameterValues(theName);
      if (values != null) {
        for (String value : values) {
          if (!StringUtils.isBlank(value)) {
            result.add(value);
          }
        }
      }
    }
    return result;
  }

  /**
   * @param key the text key
   * @return the text
   */
  private static String text(String key) {
    return TextContainer.retrieveFromRequest().getText().get(key);
  }

}

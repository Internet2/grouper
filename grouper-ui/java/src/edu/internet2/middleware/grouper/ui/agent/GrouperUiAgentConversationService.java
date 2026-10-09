/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.agent;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgent;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentConversation;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentSettings;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentTurnResult;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiAgentUsage;
import edu.internet2.middleware.grouper.ai.agent.GrouperAiLlmClient;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.mcp.GrouperMcpGroupMembership;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.mcp.GrouperMcpAuthUser;
import edu.internet2.middleware.subject.Subject;

/**
 * the AI agent conversation of the user logged in to the UI.
 *
 * <p>the conversation, including any writes waiting for approval, is kept in the HTTP session, so
 * it is private to the user and goes away when they log out.  a turn can take minutes, longer than
 * a proxy lets one request run, so it runs in a background thread and the screen polls
 * {@link #retrieveRun()}.  one turn at a time per session.</p>
 *
 * <p>call the public methods from a UI request thread.</p>
 */
public class GrouperUiAgentConversationService {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiAgentConversationService.class);

  /**
   * reads only the one setting, not all of them, since this is asked by pages other than the
   * agent's own (the Miscellaneous link).  a mistyped agent setting then only affects the agent
   * @return true if the AI agent is turned on
   */
  public static boolean isEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.ai.agent.enabled", false);
  }

  /**
   * @param subject the user logged in to the UI
   * @return the settings with the user's limits: the defaults, raised by any group overrides they
   * are in.  use this, not the plain settings, wherever a limit is enforced or shown
   */
  public static GrouperAiAgentSettings retrieveSettings(Subject subject) {
    return GrouperAiAgentSettings.fromConfig().applyLimitOverrides(subject);
  }

  /**
   * @param subject the user logged in to the UI
   * @return true if the agent is turned on, the user is in the group allowed to use it
   * (grouper.ai.agent.users), and their MCP groups give the agent at least one kind of tool to use
   * for them.  checked against Grouper, cached for a minute on each node, so removing someone takes
   * effect within a minute
   */
  public static boolean isAllowed(Subject subject) {
    return isInUsersGroup(subject) && hasAnyToolAccess(subject);
  }

  /**
   * @param subject the user logged in to the UI
   * @return true if the agent is turned on and the user is in the group allowed to use it
   * (grouper.ai.agent.users)
   */
  public static boolean isInUsersGroup(Subject subject) {
    if (subject == null || !isEnabled()) {
      return false;
    }
    return GrouperMcpGroupMembership.isSubjectInGroup(new GrouperMcpAuthUser(subject),
        GrouperAiAgentSettings.CONFIG_USERS_GROUP);
  }

  /**
   * without any, every message would still call the model, just with no tools: a general chatbot
   * that cannot do anything in Grouper, paid for by the institution
   * @param subject the user logged in to the UI
   * @return true if the user's MCP groups allow at least one tool category, whatever the session
   * scope.  a plain MCP caller, so only group membership is checked
   */
  public static boolean hasAnyToolAccess(Subject subject) {
    if (subject == null) {
      return false;
    }
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    for (GrouperToolCategory category : GrouperToolCategory.values()) {
      if (GrouperToolAccess.isAllowed(category, authUser)) {
        return true;
      }
    }
    return false;
  }

  /**
   * start a turn for a message from the user
   * @param text what the user wrote
   * @return the run to poll, with status busy if it could not start, or null if a turn is already
   * running
   */
  public static GrouperUiAgentRun startSendUserMessage(String text) {
    return startTurn(false, text, null);
  }

  /**
   * start a turn for the user's decisions on the writes waiting for approval
   * @param approvedToolCallIds the ids of the calls the user approved; the rest are declined
   * @return the run to poll, with status busy if it could not start, or null if a turn is already
   * running
   */
  public static GrouperUiAgentRun startResolvePendingToolCalls(Set<String> approvedToolCallIds) {
    return startTurn(true, null, approvedToolCallIds);
  }

  /**
   * forget the conversation, including anything waiting for approval
   * @return false if a turn is running, in which case nothing was changed
   */
  public static boolean newConversation() {
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    synchronized (sessionContainer) {
      GrouperUiAgentRun run = sessionContainer.getAiAgentRun();
      if (run != null && run.isRunning()) {
        return false;
      }
      sessionContainer.setAiAgentConversation(null);
      sessionContainer.setAiAgentRun(null);
    }
    sessionContainer.storeToSession();
    return true;
  }

  /**
   * @return the conversation in this session, null if none.  do not read it while a turn is
   * running, the background thread is changing it
   */
  public static GrouperAiAgentConversation retrieveConversation() {
    return SessionContainer.retrieveFromSession().getAiAgentConversation();
  }

  /**
   * @return the turn running in the background for this session, or the last one, null if none
   */
  public static GrouperUiAgentRun retrieveRun() {
    return SessionContainer.retrieveFromSession().getAiAgentRun();
  }

  /**
   * start a turn in a background thread
   * @param resolve true to resolve the writes waiting for approval, false to send a message
   * @param text the message, if sending one
   * @param approvedToolCallIds the approved calls, if resolving
   * @return the run, or null if a turn is already running
   */
  private static GrouperUiAgentRun startTurn(final boolean resolve, final String text,
      final Set<String> approvedToolCallIds) {

    // everything the thread needs from the request is captured here, since the thread has none
    GrouperUiAgentCaller caller = GrouperUiAgentCaller.fromRequest();

    // the user's own limits, raised by any group overrides, for both the usage recorder and the
    // agent's conversation budget
    GrouperAiAgentSettings settings = retrieveSettings(caller.getSubject());
    if (!settings.isEnabled()) {
      throw new RuntimeException("The AI agent is not enabled (grouper.ai.agent.enabled)");
    }

    // checked here as well as on the screen, since this is where a turn is actually started
    if (!isAllowed(caller.getSubject())) {
      throw new RuntimeException("Not allowed to use the AI agent: not in the group in "
          + GrouperAiAgentSettings.CONFIG_USERS_GROUP + ", or in none of the MCP groups");
    }

    // e.g. no external system or, for OpenAI, no model configured.  the user is told the assistant
    // is not set up rather than shown an error page, and the admin gets the detail in the log
    GrouperAiLlmClient llmClient = null;
    try {
      llmClient = settings.createLlmClient();
    } catch (RuntimeException re) {
      LOG.error("The AI agent is not set up correctly", re);
      GrouperUiAgentRun notSetUpRun = new GrouperUiAgentRun();
      notSetUpRun.notSetUp();
      return notSetUpRun;
    }

    final GrouperAiAgent grouperAiAgent = new GrouperAiAgent(llmClient,
        new GrouperUiAgentToolExecution(caller), settings);
    grouperAiAgent.setUsageRecorder(new GrouperAiAgentUsage(
        caller.getMemberInternalId(), settings));

    SessionContainer sessionContainer = caller.getSessionContainer();

    final GrouperUiAgentRun run = new GrouperUiAgentRun();
    run.assignGrouperAiAgent(grouperAiAgent);
    final GrouperAiAgentConversation conversation;

    // one turn at a time: a second click, or a second tab, gets told to wait
    synchronized (sessionContainer) {
      GrouperUiAgentRun previousRun = sessionContainer.getAiAgentRun();
      if (previousRun != null && previousRun.isRunning()) {
        return null;
      }
      GrouperAiAgentConversation existing = sessionContainer.getAiAgentConversation();
      if (existing == null) {
        existing = new GrouperAiAgentConversation();
        sessionContainer.setAiAgentConversation(existing);
      }
      conversation = existing;
      sessionContainer.setAiAgentRun(run);
    }
    sessionContainer.storeToSession();

    // constructed here so it carries this request's audit context and Grouper session subject into
    // the thread
    final GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("aiAgentTurn") {

      @Override
      public Void callLogic() {
        assignAuditContext();

        GrouperAiAgentTurnResult result = null;
        synchronized (conversation) {
          if (resolve) {
            result = grouperAiAgent.resolvePendingToolCalls(conversation, approvedToolCallIds);
          } else {
            result = grouperAiAgent.sendUserMessage(conversation, text);
          }
        }
        run.finish(result);
        return null;
      }
    };

    // wrapped so that whatever goes wrong is logged and ends the run, including in what
    // GrouperCallable does before callLogic, e.g. starting the user's Grouper session.  otherwise the
    // run would stay running with nothing behind it, and this session could not start another turn
    // until the user logged out
    Callable<Void> turn = new Callable<Void>() {

      public Void call() {
        try {
          grouperCallable.call();
        } catch (Exception e) {
          // the thread ends here, so this is the only place it gets logged
          LOG.error("Error in AI agent turn", e);
        } finally {
          // never leave the screen polling a run which is not going anywhere
          if (run.isRunning()) {
            run.fail();
          }
        }
        return null;
      }
    };

    try {
      GrouperUtil.executorServiceSubmit(retrieveExecutorService(settings), turn);
    } catch (RejectedExecutionException ree) {
      // nothing ran, so the conversation is as it was and the user can simply try again
      run.busy();
    } catch (RuntimeException re) {
      // the run is already in the session as running.  with no thread behind it, it would stay that
      // way and block this session from starting another turn
      run.fail();
      throw re;
    }

    return run;
  }

  /**
   * on the agent's thread: audit what the agent's tools change as the AI agent in the UI
   * (grouperUiAiAgent) rather than as the UI, so someone reading a group's audit trail can tell a
   * change the user made themselves from one they approved from their assistant.  the logged in
   * user and address are kept from the UI request, which GrouperCallable carried over.  a new
   * context rather than changing the carried one, which is the request's own.  GrouperCallable puts
   * the thread's previous context back when the turn ends
   */
  private static void assignAuditContext() {
    GrouperContext requestContext = GrouperContext.retrieveDefaultContext();
    GrouperContext agentContext = GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.UI_AI_AGENT, false, false);
    if (requestContext != null) {
      agentContext.setCallerIpAddress(requestContext.getCallerIpAddress());
      agentContext.setLoggedInMemberId(requestContext.getLoggedInMemberId());
      agentContext.setLoggedInMemberIdActAs(requestContext.getLoggedInMemberIdActAs());
    }
  }

  /** the agent's own threads on this node, see {@link #retrieveExecutorService(GrouperAiAgentSettings)} */
  private static ThreadPoolExecutor executorService = null;

  /**
   * the agent's own threads, rather than Grouper's shared pool: a turn can hold a thread for
   * minutes, and many of them at once should not hold up the other work that pool does.  when all
   * are busy a new turn is refused rather than queued, so the user is told at once instead of
   * watching a turn which has not started.  one pool per node, sized from config when first used
   * @param settings the settings
   * @return the executor
   */
  private static synchronized ThreadPoolExecutor retrieveExecutorService(GrouperAiAgentSettings settings) {
    if (executorService == null) {
      int maxThreads = Math.max(1, settings.getMaxConcurrentTurnsPerNode());
      ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>(), new ThreadFactory() {

            /** numbers the threads so they can be told apart in a thread dump */
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable runnable) {
              Thread thread = new Thread(runnable, "grouperAiAgent-" + this.threadNumber.getAndIncrement());
              thread.setDaemon(true);
              return thread;
            }
          }, new ThreadPoolExecutor.AbortPolicy());
      threadPoolExecutor.allowCoreThreadTimeOut(true);
      executorService = threadPoolExecutor;
    }
    return executorService;
  }

}

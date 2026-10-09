/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * per request state that the web service logic needs, with no dependency on the servlet API.
 *
 * <p>GrouperServiceJ2ee is a Filter, so anything that reads its thread locals can only be called
 * from a web service request.  This class holds the same state so that a caller which is not a
 * web service request, for instance the UI, can populate it without pretending to be one.  The
 * filter populates this and no longer owns it.</p>
 *
 * <p>Whoever populates this is responsible for calling {@link #clearThreadLocals()} when the
 * request is over, since threads are pooled and reused.</p>
 */
public class GrouperWsRequestContext {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperWsRequestContext.class);

  /**
   * debug map for the current request, logged when the request finishes
   */
  private static ThreadLocal<Map<String, Object>> threadLocalDebugMap = new ThreadLocal<Map<String, Object>>();

  /**
   * when the current request started
   */
  private static ThreadLocal<Long> threadLocalRequestStartMillis = new ThreadLocal<Long>();

  /**
   * true if the current request came in over REST
   */
  private static ThreadLocal<Boolean> threadLocalRestRequest = new ThreadLocal<Boolean>();

  /**
   * socket address the current request came from.  deliberately not X-Forwarded-For, see
   * {@link #retrieveRemoteAddr()}
   */
  private static ThreadLocal<String> threadLocalRemoteAddr = new ThreadLocal<String>();

  /**
   * debug map for the current request.  if nothing has started a request on this thread this
   * returns a throwaway map rather than null, since the caller is only going to put things in it
   * and nothing is going to log it.  note that the throwaway map is deliberately not kept in the
   * thread local: a thread outside a request, for instance a daemon, would never clear it
   * @return the debug map, never null
   */
  public static Map<String, Object> retrieveDebugMap() {

    Map<String, Object> debugMap = threadLocalDebugMap.get();

    if (debugMap == null) {
      return new LinkedHashMap<String, Object>();
    }

    return debugMap;
  }

  /**
   * @param debugMap the debug map for this request
   */
  public static void assignDebugMap(Map<String, Object> debugMap) {
    threadLocalDebugMap.set(debugMap);
  }

  /**
   * @return when this request started, or 0 if not in a request
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return GrouperUtil.longValue(requestStartMillis, 0);
  }

  /**
   * @param requestStartMillis when this request started
   */
  public static void assignRequestStartMillis(long requestStartMillis) {
    threadLocalRequestStartMillis.set(requestStartMillis);
  }

  /**
   * fallback for working out who is authenticated, registered once at startup by the web service
   * filter.  a front door should prefer {@link #assignSubjectResolverForRequest}: this one is a
   * single static, so if the UI and the web service ever share a classloader (both enabled in one
   * webapp) whichever registered last would answer for both
   */
  private static volatile GrouperWsSubjectResolver subjectResolver = null;

  /**
   * how the front door that is handling the current request works out who is authenticated.  set
   * at the start of each request and cleared with the other thread locals, so a web service
   * request and a UI request running side by side in one webapp each get their own answer
   */
  private static ThreadLocal<GrouperWsSubjectResolver> threadLocalSubjectResolver =
      new ThreadLocal<GrouperWsSubjectResolver>();

  /**
   * register the fallback resolver, used only when the current request has not assigned its own.
   * a second registration of a different kind is logged, since it would silently change who the
   * other front door thinks is logged in
   * @param theSubjectResolver the resolver
   */
  public static void assignSubjectResolver(GrouperWsSubjectResolver theSubjectResolver) {
    GrouperWsSubjectResolver existing = subjectResolver;
    if (existing != null && theSubjectResolver != null
        && existing.getClass() != theSubjectResolver.getClass()) {
      LOG.warn("Replacing the registered GrouperWsSubjectResolver " + existing.getClass().getName()
          + " with " + theSubjectResolver.getClass().getName() + ".  Front doors should assign "
          + "their resolver per request with assignSubjectResolverForRequest instead");
    }
    subjectResolver = theSubjectResolver;
  }

  /**
   * the registered fallback resolver, so a test which replaces it can put it back
   * @return the fallback resolver, or null if none is registered
   */
  static GrouperWsSubjectResolver retrieveSubjectResolver() {
    return subjectResolver;
  }

  /**
   * set how the current request works out who is authenticated.  the caller must call
   * {@link #clearThreadLocals()} in a finally when the request is over
   * @param theSubjectResolver the resolver for this request
   */
  public static void assignSubjectResolverForRequest(GrouperWsSubjectResolver theSubjectResolver) {
    threadLocalSubjectResolver.set(theSubjectResolver);
  }

  /**
   * the subject authenticated for this request.  asks the resolver the current request assigned,
   * or the registered fallback if it did not, every time.
   *
   * <p>the answer is deliberately not cached here.  servlet threads are pooled, so a cached
   * subject which outlived its request would be handed to whoever got that thread next.  the
   * resolver is free to cache internally if it is expensive, where the lifetime is its own to
   * reason about.  this also matches what the web service did before this class existed, which
   * resolved from the request on every call</p>
   *
   * @return the subject
   */
  public static Subject retrieveSubjectLoggedIn() {

    GrouperWsSubjectResolver theSubjectResolver = threadLocalSubjectResolver.get();

    if (theSubjectResolver == null) {
      theSubjectResolver = subjectResolver;
    }

    if (theSubjectResolver == null) {
      throw new RuntimeException("No GrouperWsSubjectResolver is assigned for this request or "
          + "registered, so there is no way to work out who is logged in.  grouper-ws assigns one "
          + "in GrouperServiceJ2ee");
    }

    return theSubjectResolver.retrieveSubjectLoggedIn();
  }

  /**
   * @return true if the current request came in over REST
   */
  public static boolean isRestRequest() {
    Boolean restRequest = threadLocalRestRequest.get();
    return restRequest != null && restRequest;
  }

  /**
   * @param restRequest true if the current request came in over REST, null to clear
   */
  public static void assignRestRequest(Boolean restRequest) {
    threadLocalRestRequest.set(restRequest);
  }

  /**
   * the socket address the current request came from, i.e. ServletRequest.getRemoteAddr().  this
   * is deliberately not X-Forwarded-For: it is handed to GSH templates, which may use it for IP
   * checks, and a caller can set that header to anything.  it also matches what the UI passes
   * @return the socket address, or null if not in a request
   */
  public static String retrieveRemoteAddr() {
    return threadLocalRemoteAddr.get();
  }

  /**
   * @param remoteAddr the socket address the current request came from, not X-Forwarded-For
   */
  public static void assignRemoteAddr(String remoteAddr) {
    threadLocalRemoteAddr.set(remoteAddr);
  }

  /**
   * clear everything for this thread.  must be called when the request finishes, in a finally
   */
  public static void clearThreadLocals() {
    threadLocalDebugMap.remove();
    threadLocalRequestStartMillis.remove();
    threadLocalRestRequest.remove();
    threadLocalRemoteAddr.remove();
    threadLocalSubjectResolver.remove();
  }

}

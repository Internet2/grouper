/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.subject.Subject;

/**
 * which resolver answers "who is logged in".  no database needed: the resolvers here are told apart
 * by what they do rather than by the subject they return
 */
public class GrouperWsRequestContextTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperWsRequestContextTest.class);
  }

  /**
   * @param name
   */
  public GrouperWsRequestContextTest(String name) {
    super(name);
  }

  /** stands in for the static fallback a web service registers at startup */
  private static class ThrowingResolver implements GrouperWsSubjectResolver {

    /**
     * @see GrouperWsSubjectResolver#retrieveSubjectLoggedIn()
     */
    public Subject retrieveSubjectLoggedIn() {
      throw new RuntimeException("fallback resolver");
    }
  }

  /** stands in for the resolver one request assigns for itself */
  private static class NullResolver implements GrouperWsSubjectResolver {

    /**
     * @see GrouperWsSubjectResolver#retrieveSubjectLoggedIn()
     */
    public Subject retrieveSubjectLoggedIn() {
      return null;
    }
  }

  /**
   * the resolver a request assigns wins over the static fallback, and clearing the request puts it
   * back to the fallback.  this is what keeps a UI request and a web service request in the same
   * webapp from answering with each other's resolver
   */
  public void testRequestResolverTakesPrecedence() {

    GrouperWsSubjectResolver previousSubjectResolver = GrouperWsRequestContext.retrieveSubjectResolver();

    GrouperWsRequestContext.clearThreadLocals();
    GrouperWsRequestContext.assignSubjectResolver(new ThrowingResolver());

    try {

      // no request resolver: the fallback answers
      try {
        GrouperWsRequestContext.retrieveSubjectLoggedIn();
        fail("expected the fallback resolver to answer");
      } catch (RuntimeException re) {
        assertEquals("fallback resolver", re.getMessage());
      }

      // a request resolver answers instead of the fallback
      GrouperWsRequestContext.assignSubjectResolverForRequest(new NullResolver());
      assertNull(GrouperWsRequestContext.retrieveSubjectLoggedIn());

      // once the request is over, the fallback answers again
      GrouperWsRequestContext.clearThreadLocals();
      try {
        GrouperWsRequestContext.retrieveSubjectLoggedIn();
        fail("expected the fallback resolver to answer after the request was cleared");
      } catch (RuntimeException re) {
        assertEquals("fallback resolver", re.getMessage());
      }

    } finally {
      GrouperWsRequestContext.clearThreadLocals();
      GrouperWsRequestContext.assignSubjectResolver(previousSubjectResolver);
    }
  }

  /**
   * with nothing assigned or registered there is no way to know who is logged in, which is an
   * error rather than an anonymous answer
   */
  public void testNoResolverFails() {

    GrouperWsSubjectResolver previousSubjectResolver = GrouperWsRequestContext.retrieveSubjectResolver();

    GrouperWsRequestContext.clearThreadLocals();
    GrouperWsRequestContext.assignSubjectResolver(null);

    try {
      GrouperWsRequestContext.retrieveSubjectLoggedIn();
      fail("expected an error when no resolver is assigned or registered");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("No GrouperWsSubjectResolver"));
    } finally {
      GrouperWsRequestContext.assignSubjectResolver(previousSubjectResolver);
    }
  }

}

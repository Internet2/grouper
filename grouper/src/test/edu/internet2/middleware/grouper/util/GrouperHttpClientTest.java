/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.Predicate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * test the retry logic of the http client
 */
public class GrouperHttpClientTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperHttpClientTest("testRetryNetworkIssuePredicateDefault"));
  }

  /**
   * @param name
   */
  public GrouperHttpClientTest(String name) {
    super(name);
  }

  /**
   * transient socket and tls transport problems should be retried
   */
  public void testRetryNetworkIssuePredicateDefaultRetryable() {

    Predicate<Throwable> predicate = GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT;

    // this is the one from the ticket, a connection reset used to abort the whole run
    assertTrue(predicate.test(new SocketException("Connection reset")));

    // in real life it is wrapped a few levels deep
    assertTrue(predicate.test(new RuntimeException("Error connecting to 'https://someUrl'",
        new IOException("problem", new SocketException("Connection reset")))));

    assertTrue(predicate.test(new SocketException("Broken pipe")));
    assertTrue(predicate.test(new ConnectException("Connection refused")));
    assertTrue(predicate.test(new SocketTimeoutException("Read timed out")));
    assertTrue(predicate.test(new ConnectTimeoutException("connect timed out")));
    assertTrue(predicate.test(new NoHttpResponseException("the target failed to respond")));
    assertTrue(predicate.test(new SSLException("Connection reset by peer")));

    // legacy behavior, a timeout reported by something which isnt one of the types above
    assertTrue(predicate.test(new RuntimeException("the operation timed out")));
  }

  /**
   * problems which will not fix themselves on a retry should not be retried
   */
  public void testRetryNetworkIssuePredicateDefaultNotRetryable() {

    Predicate<Throwable> predicate = GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT;

    assertFalse(predicate.test(new UnknownHostException("someHostWhichDoesNotExist")));
    assertFalse(predicate.test(new RuntimeException("Error connecting to 'https://someUrl'",
        new UnknownHostException("someHostWhichDoesNotExist"))));
    assertFalse(predicate.test(new SSLHandshakeException("PKIX path building failed")));
    assertFalse(predicate.test(new SSLPeerUnverifiedException("hostname does not match")));
    assertFalse(predicate.test(new IllegalStateException("this is a bug in the caller")));
    assertFalse(predicate.test(new IOException("404 not found")));
  }

  /**
   * a cyclic chain of causes should not loop forever
   */
  public void testRetryNetworkIssuePredicateDefaultCyclicCause() {

    Throwable cyclic = new RuntimeException("cyclic") {

      private static final long serialVersionUID = 1L;

      @Override
      public synchronized Throwable getCause() {
        return this;
      }
    };

    assertFalse(GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT.test(cyclic));
  }

  /**
   * callers can plug in their own decision, and null goes back to the default
   */
  public void testAssignRetryNetworkIssuePredicate() {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    assertEquals(GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT,
        grouperHttpClient.getRetryNetworkIssuePredicate());

    Predicate<Throwable> neverRetry = new Predicate<Throwable>() {

      @Override
      public boolean test(Throwable throwable) {
        return false;
      }
    };

    grouperHttpClient.assignRetryNetworkIssuePredicate(neverRetry);
    assertEquals(neverRetry, grouperHttpClient.getRetryNetworkIssuePredicate());
    assertFalse(grouperHttpClient.getRetryNetworkIssuePredicate().test(new SocketException("Connection reset")));

    grouperHttpClient.assignRetryNetworkIssuePredicate(null);
    assertEquals(GrouperHttpClient.RETRY_NETWORK_ISSUE_PREDICATE_DEFAULT,
        grouperHttpClient.getRetryNetworkIssuePredicate());
  }

  /**
   * a network error which never clears up should retry the configured number of times and then
   * throw, it should not silently return a response code of -1
   */
  public void testExecuteRequestRetriesThenThrows() {

    int port = -1;
    try {
      // get a port which nothing is listening on so the connect is refused
      ServerSocket serverSocket = new ServerSocket(0);
      port = serverSocket.getLocalPort();
      serverSocket.close();
    } catch (IOException ioe) {
      throw new RuntimeException("Cannot find a free port", ioe);
    }

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignUrl("http://127.0.0.1:" + port + "/somePath");
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.get);
    grouperHttpClient.assignTimeoutMillies(5000);
    grouperHttpClient.setRetryForThrottlingOrNetworkIssues(2);
    grouperHttpClient.setRetryForThrottlingOrNetworkIssuesSleepMillis(0);
    grouperHttpClient.setRetryForThrottlingOrNetworkIssuesBackOffMillis(0);

    try {
      grouperHttpClient.executeRequest();
      fail("Should have thrown, the connection is refused every time");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Error connecting to"));
    }

    assertEquals(2, grouperHttpClient.getRetryForThrottlingTimesItWasRetried());
  }

}

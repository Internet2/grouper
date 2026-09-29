package edu.internet2.middleware.grouper.j2ee.status;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * test the status servlet output options
 */
public class GrouperStatusServletTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperStatusServletTest("testSendHttpCodeOnlySuccess"));
  }

  public GrouperStatusServletTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("ws.diagnostic.sendHttpCodeOnly");
    super.tearDown();
  }

  /**
   * call the status servlet
   * @param diagnosticType
   * @param output gets the response body
   * @return the http status code
   */
  private int callStatus(String diagnosticType, StringWriter output) throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getParameter("diagnosticType")).thenReturn(diagnosticType);
    Mockito.when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.when(response.getWriter()).thenReturn(new PrintWriter(output));
    new GrouperStatusServlet().doGet(request, response);
    // 200 is the default if the servlet never sets a status, 500 is set explicitly on failure
    org.mockito.ArgumentCaptor<Integer> statusCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(response, Mockito.atLeast(0)).setStatus(statusCaptor.capture());
    return statusCaptor.getAllValues().isEmpty() ? 200 : statusCaptor.getValue();
  }

  /**
   * by default (no setting) the details are still sent
   */
  public void testDefaultSendsDetails() throws Exception {
    StringWriter output = new StringWriter();
    assertEquals(200, callStatus("trivial", output));
    assertTrue(output.toString(), output.toString().contains("grouperVersion"));
    assertTrue(output.toString(), output.toString().contains(GrouperUtil.hostname()));
  }

  /**
   * only the success heading is returned
   */
  public void testSendHttpCodeOnlySuccess() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.diagnostic.sendHttpCodeOnly", "true");
    StringWriter output = new StringWriter();
    assertEquals(200, callStatus("trivial", output));
    assertTrue(output.toString(), output.toString().contains("Grouper status SUCCESS"));
    assertFalse(output.toString(), output.toString().contains("grouperVersion"));
    assertFalse(output.toString(), output.toString().contains("See logs"));
  }

  /**
   * the diagnostic type options are still shown when the type is missing
   */
  public void testSendHttpCodeOnlyMissingDiagnosticTypeShowsOptions() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("ws.diagnostic.sendHttpCodeOnly", "true");
    StringWriter output = new StringWriter();
    assertEquals(500, callStatus(null, output));
    assertTrue(output.toString(), output.toString().contains("diagnosticType"));
  }
}

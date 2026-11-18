package edu.internet2.middleware.grouper.app.workflow;

import java.lang.reflect.Method;
import java.util.Date;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix 99: the workflow audit line (appended to the stored instance form, which the view
 * instance screen prints raw) must html escape the subject id, name and source name, the action
 * and the state.  Subject values come from subject sources and are not trusted markup.
 * Before the fix this does not compile (GrouperWorkflowAuditLine is missing), and the service
 * part returns the raw img tag.
 * </pre>
 */
public class GrouperWorkflowAuditLineTest extends GrouperTest {

  /** script payload */
  private static final String PAYLOAD = "<img src=x onerror=alert(1)>\"'&";

  /** the payload escaped */
  private static final String PAYLOAD_ESCAPED_START = "&lt;img src=x onerror=alert(1)&gt;";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowAuditLineTest("testServiceAppendAuditLineEscapes"));
  }

  /**
   */
  public GrouperWorkflowAuditLineTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowAuditLineTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSessionIfNotStarted();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }

  /**
   * a subject with the payload in its id and name, in the jdbc test source
   * @return the subject
   */
  private static Subject payloadSubject() {
    return new SubjectImpl("id" + PAYLOAD, "name" + PAYLOAD, "description", "person",
        SubjectTestHelper.SUBJ0.getSourceId());
  }

  /**
   * both placeholder spellings, values escaped, template markup kept
   */
  public void testAuditLineHtmlEscapes() {

    Subject subject = payloadSubject();
    Date date = new Date();

    String template7 = "<b>##subjectSource##</b>: ##subjectId##, ##subjectName## clicked ##buttonText## at state ##state## on timestamp: ##timestamp##";
    String template5 = "<b>$$subjectSource$$</b>: $$subjectId$$, $$subjectName$$ clicked $$buttonText$$ at state $$state$$ on timestamp: $$timestamp$$";

    for (String template : new String[] {template7, template5}) {
      String html = GrouperWorkflowAuditLine.auditLineHtml(template, subject, "state" + PAYLOAD, "action" + PAYLOAD, date);

      assertTrue(html, html.startsWith("<div><b>"));
      assertTrue(html, html.endsWith("</div>"));
      assertFalse("no raw img tag: " + html, html.contains("<img"));
      assertTrue(html, html.contains("id" + PAYLOAD_ESCAPED_START));
      assertTrue(html, html.contains("name" + PAYLOAD_ESCAPED_START));
      assertTrue(html, html.contains("action" + PAYLOAD_ESCAPED_START));
      assertTrue(html, html.contains("state" + PAYLOAD_ESCAPED_START));
      assertTrue(html, html.contains("&quot;"));
      assertTrue(html, html.contains("&amp;"));
      assertFalse("all placeholders replaced: " + html, html.contains("##") || html.contains("$$"));
      // the template's own text stays
      assertTrue(html, html.contains(" clicked "));
    }
  }

  /**
   * plain values render as before, nulls are empty, a value that looks like a placeholder is
   * not expanded again
   */
  public void testAuditLineHtmlPlainAndNull() {

    Subject subject = new SubjectImpl("abc123", "##state##", "description", "person",
        SubjectTestHelper.SUBJ0.getSourceId());

    String html = GrouperWorkflowAuditLine.auditLineHtml("##subjectId## / ##subjectName## / ##state##", subject, "initiate", "initiate", new Date());
    assertEquals("<div>abc123 / ##state## / initiate</div>", html);

    html = GrouperWorkflowAuditLine.auditLineHtml("[##subjectName##][##state##][##unknown##]", null, null, null, null);
    assertEquals("<div>[][][##unknown##]</div>", html);
  }

  /**
   * the instance service builds its audit line through the escaping (the method is private, so
   * call it by reflection; same signature in every release)
   * @throws Exception
   */
  public void testServiceAppendAuditLineEscapes() throws Exception {

    Method method = GrouperWorkflowInstanceService.class.getDeclaredMethod("appendAuditLine",
        String.class, Subject.class, String.class, String.class, Date.class);
    method.setAccessible(true);

    String html = (String) method.invoke(null, "<form></form>", payloadSubject(),
        GrouperWorkflowConstants.INITIATE_STATE, GrouperWorkflowConstants.INITIATE_ACTION, new Date());

    assertTrue(html, html.startsWith("<form></form><div>"));
    assertFalse("subject name must not be markup: " + html, html.contains("<img"));
    assertTrue(html, html.contains("name" + PAYLOAD_ESCAPED_START));
  }
}

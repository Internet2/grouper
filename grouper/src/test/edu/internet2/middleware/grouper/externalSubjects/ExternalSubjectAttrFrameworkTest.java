/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.subj.TestSubject;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class ExternalSubjectAttrFrameworkTest extends GrouperTest {

  /**
   * @param name
   */
  public ExternalSubjectAttrFrameworkTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSubjectAttrFrameworkTest("testSendInviteOne"));
  }
  
  /**
   * grouper session
   */
  private GrouperSession grouperSession;

  
  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.startRootSession();
  }

  /**
   * 
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperSession.stopQuietly(this.grouperSession);
  }

  /**
   * 
   */
  public void testSendInviteOne() {
    
    ExternalSubjectInviteBean externalSubjectInviteBean = new ExternalSubjectInviteBean();
    
    Subject subject1 = SubjectTestHelper.SUBJ0;
    
    Member member1 = MemberFinder.findBySubject(this.grouperSession, subject1, true);
    
    externalSubjectInviteBean.setMemberId(member1.getUuid());

    long emailCount = GrouperEmail.testingEmailCount;
    
    Set<String> emailAddresses = GrouperUtil.toSet("a@b.c");
    String errors = ExternalSubjectAttrFramework.inviteExternalUsers(emailAddresses, 
        externalSubjectInviteBean, "this is the subject", "this is the email");
    
    assertTrue(errors, StringUtils.isBlank(errors));
    
    assertEquals(emailCount + 1, GrouperEmail.testingEmailCount);
    assertEquals(1, GrouperEmail.testingEmails().size());
    
    String body = GrouperEmail.testingEmails().get(0).getBody();
    assertTrue(body.contains("this is the email\n\nhttp://"));
    assertTrue(body.contains("grouperExternal/appHtml/grouper.html?operation=ExternalSubjectSelfRegister.index&externalSubjectInviteId="));
    
    //grab the uuid
    Pattern pattern = Pattern.compile(".*externalSubjectInviteId=([0-9a-z]+)", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(body);
    
    assertTrue(body, matcher.matches());
    String uuid = matcher.group(1);
    
    assertTrue(GrouperEmail.testingEmails().get(0).getSubject().contains("this is the subject"));
    assertEquals("a@b.c", GrouperEmail.testingEmails().get(0).getTo());
    
    //lets make sure the invite is there
    List<ExternalSubjectInviteBean> externalSubjectInviteBeans = ExternalSubjectInviteBean.findByUuid(uuid);
    
    assertEquals(1, GrouperUtil.length(externalSubjectInviteBeans));
    
    assertEquals("a@b.c", externalSubjectInviteBeans.get(0).getEmailAddress());
    assertEquals(uuid, externalSubjectInviteBeans.get(0).getUuid());
    assertEquals(member1.getUuid(), externalSubjectInviteBeans.get(0).getMemberId());
    assertEquals(body, externalSubjectInviteBeans.get(0).getEmail());
    assertTrue(Math.abs(System.currentTimeMillis() - externalSubjectInviteBeans.get(0).getInviteDate()) < 1000);
    assertEquals(0, GrouperUtil.length(externalSubjectInviteBeans.get(0).getEmailsWhenRegistered()));
    assertEquals(0, GrouperUtil.length(externalSubjectInviteBeans.get(0).getGroupIds()));
    
    //lets delete the invite...
    assertTrue(externalSubjectInviteBeans.get(0).deleteFromDb());
    assertFalse(externalSubjectInviteBeans.get(0).deleteFromDb());
    
    externalSubjectInviteBeans = ExternalSubjectInviteBean.findByUuid(uuid);
    assertEquals(0, GrouperUtil.length(externalSubjectInviteBeans));
  }
  
}

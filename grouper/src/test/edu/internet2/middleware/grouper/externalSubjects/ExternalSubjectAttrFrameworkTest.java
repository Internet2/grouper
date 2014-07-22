/**
 * Copyright 2012 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
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
    //TestRunner.run(new ExternalSubjectAttrFrameworkTest("testSendInviteOne"));
    TestRunner.run(ExternalSubjectAttrFrameworkTest.class);
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.ui.url", "http://whatever/grouper/");
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
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjectsInviteExpireAfterDays", "-1");
    
    try {
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
              
      assertTrue(body, body.contains("grouperExternal/appHtml/grouper.html?operation=ExternalSubjectSelfRegister.externalSubjectSelfRegister&externalSubjectInviteId="));
      
      //grab the uuid
      Pattern pattern = Pattern.compile(".*externalSubjectInviteId=([0-9a-z]+)", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(body);
      
      assertTrue(body, matcher.matches());
      String uuid = matcher.group(1);
      
      assertTrue(GrouperEmail.testingEmails().get(0).getSubject().contains("this is the subject"));
      assertEquals("a@b.c", GrouperEmail.testingEmails().get(0).getTo());
      
      //lets make sure the invite is there
      externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(uuid);
      
      assertNotNull(externalSubjectInviteBean);
      
      assertEquals("a@b.c", externalSubjectInviteBean.getEmailAddress());
      assertEquals(uuid, externalSubjectInviteBean.getUuid());
      assertEquals(member1.getUuid(), externalSubjectInviteBean.getMemberId());
      assertEquals(body, externalSubjectInviteBean.getEmail());
      long nearNow = Math.abs(System.currentTimeMillis() - externalSubjectInviteBean.getInviteDate());
      assertTrue(System.currentTimeMillis() + ", " + externalSubjectInviteBean.getInviteDate() + ", " + nearNow, nearNow < 5000 );
      assertEquals(0, GrouperUtil.length(externalSubjectInviteBean.getEmailsWhenRegistered()));
      assertEquals(0, GrouperUtil.length(externalSubjectInviteBean.getGroupIds()));
      assertNull(externalSubjectInviteBean.getExpireDate());
      
      //lets delete the invite...
      assertTrue(externalSubjectInviteBean.deleteFromDb());
      assertFalse(externalSubjectInviteBean.deleteFromDb());
      
      externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(uuid);
      assertNull(externalSubjectInviteBean);
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjectsInviteExpireAfterDays");

    }
  }

  /**
   * 
   */
  public void testSendInviteFinite() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjectsInviteExpireAfterDays", "1");
    
    try {
    
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
      assertTrue(body.contains("grouperExternal/appHtml/grouper.html?operation=ExternalSubjectSelfRegister.externalSubjectSelfRegister&externalSubjectInviteId="));
      
      //grab the uuid
      Pattern pattern = Pattern.compile(".*externalSubjectInviteId=([0-9a-z]+)", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(body);
      
      assertTrue(body, matcher.matches());
      String uuid = matcher.group(1);
      
      assertTrue(GrouperEmail.testingEmails().get(0).getSubject().contains("this is the subject"));
      assertEquals("a@b.c", GrouperEmail.testingEmails().get(0).getTo());
      
      //lets make sure the invite is there
      externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(uuid);
      
      assertNotNull(externalSubjectInviteBean);
      
      assertEquals("a@b.c", externalSubjectInviteBean.getEmailAddress());
      assertEquals(uuid, externalSubjectInviteBean.getUuid());
      assertEquals(member1.getUuid(), externalSubjectInviteBean.getMemberId());
      assertEquals(body, externalSubjectInviteBean.getEmail());
      long nearNow = Math.abs(System.currentTimeMillis() - externalSubjectInviteBean.getInviteDate());
      assertTrue(System.currentTimeMillis() + ", " + externalSubjectInviteBean.getInviteDate() + ", " + nearNow, nearNow < 5000 );
      assertEquals(0, GrouperUtil.length(externalSubjectInviteBean.getEmailsWhenRegistered()));
      assertEquals(0, GrouperUtil.length(externalSubjectInviteBean.getGroupIds()));
      
      long expireDate = externalSubjectInviteBean.getExpireDate();
      nearNow = Math.abs((System.currentTimeMillis() + (1000 * 60 * 60 * 24) ) - expireDate);
      assertTrue(System.currentTimeMillis() + ", " + expireDate + ", " + nearNow, nearNow < 5000 );
      
      String attributeAssignId = HibernateSession.bySqlStatic().select(String.class, "select attribute_assign_id from grouper_ext_subj_invite_v");
      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      assertNotNull(attributeAssign.getDisabledTimeDb());
      assertEquals(expireDate, (long)attributeAssign.getDisabledTimeDb());
      
      //lets delete the invite...
      assertTrue(externalSubjectInviteBean.deleteFromDb());
      assertFalse(externalSubjectInviteBean.deleteFromDb());
      
      externalSubjectInviteBean = ExternalSubjectInviteBean.findByUuid(uuid);
      assertNull(externalSubjectInviteBean);
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjectsInviteExpireAfterDays");
    }
  }
  
}

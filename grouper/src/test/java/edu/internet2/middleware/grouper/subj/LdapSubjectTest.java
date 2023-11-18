/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class LdapSubjectTest extends GrouperTest {

  /**
   * 
   */
  public LdapSubjectTest() {
  }
  
  /**
   * @param name
   */
  public LdapSubjectTest(String name) {
    super(name);
    
  }



  /**
   * @param args
   */
  public static void main(String[] args) {
    
    TestRunner.run(LdapSubjectTest.class);
    
  }

  /**
   * 
   */
  public void testSearch() {
    
    
    //<source adapterClass="edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter">
    //  <id>cmu</id>
    //  <name>cmu</name>
    //  <type>person</type>
    //  <init-param>
    //    <param-name>INITIAL_CONTEXT_FACTORY</param-name>
    //    <param-value>com.sun.jndi.ldap.LdapCtxFactory</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>PROVIDER_URL</param-name>
    //    <param-value>ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>SECURITY_AUTHENTICATION</param-name>
    //    <param-value>none</param-value>
    //    <!-- param-value>simple</param-value -->
    //  </init-param>
    //  <!-- init-param>
    //    <param-name>SECURITY_PRINCIPAL</param-name>
    //    <param-value>CN=grouperad,OU=Service Accounts,OU=Admin,DC=clinlan,DC=local</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>SECURITY_CREDENTIALS</param-name>
    //    <param-value>/etc/grouper/ADSource.pass</param-value>
    //  </init-param -->
    //   <init-param>
    //    <param-name>SubjectID_AttributeType</param-name>
    //    <param-value>guid</param-value>
    //  </init-param>
    //   <init-param>
    //    <param-name>SubjectID_formatToLowerCase</param-name>
    //    <param-value>false</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>Name_AttributeType</param-name>
    //    <param-value>cn</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>Description_AttributeType</param-name>
    //    <param-value>nameLong</param-value>
    //  </init-param>
    //  
    //  <!--  /// 
    //        /// For filter use  -->
    //  
    //  <search>
    //      <searchType>searchSubject</searchType>
    //      <param>
    //          <param-name>filter</param-name>
    //          <param-value>
    //              (&amp; (guid=%TERM%) (objectclass=cmuPerson))
    //          </param-value>
    //      </param>
    //      <param>
    //          <param-name>scope</param-name>
    //          <!--  Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE  -->
    //          <param-value>
    //              ONELEVEL_SCOPE            
    //          </param-value>
    //      </param>
    //      <param>
    //          <param-name>base</param-name>
    //          <param-value>
    //              ou=person
    //          </param-value>
    //      </param>
    //       
    //  </search>
    //  <search>
    //      <searchType>searchSubjectByIdentifier</searchType>
    //      <param>
    //          <param-name>filter</param-name>
    //          <param-value>
    //              (&amp; (cmuAndrewId=%TERM%) (objectclass=cmuPerson))
    //          </param-value>
    //      </param>
    //      <param>
    //          <param-name>scope</param-name>
    //          <param-value>
    //              ONELEVEL_SCOPE            
    //          </param-value>
    //      </param>
    //      <param>
    //          <param-name>base</param-name>
    //          <param-value>
    //              ou=person
    //          </param-value>
    //      </param>
    //  </search>
    //  
    //  <search>
    //     <searchType>search</searchType>
    //       <param>
    //          <param-name>filter</param-name>
    //          <param-value>
    //              (&amp; (|(|(cmuAndrewId=%TERM%)(cn=*%TERM%*))(guid=%TERM%))(objectclass=cmuPerson))
    //          </param-value>
    //      </param>
    //      <param>
    //          <param-name>scope</param-name>
    //          <param-value>
    //              ONELEVEL_SCOPE            
    //          </param-value>
    //      </param>
    //       <param>
    //          <param-name>base</param-name>
    //          <param-value>
    //              ou=person
    //          </param-value>
    //      </param>
    //  </search>
    //
    //  <!-- you need this to be able to reference GrouperUtilElSafe in scripts -->
    //  <init-param>
    //    <param-name>subjectVirtualAttributeVariable_grouperUtilElSafe</param-name>
    //    <param-value>edu.internet2.middleware.grouper.util.GrouperUtilElSafe</param-value>
    //  </init-param>    
    //
    //  <!-- make sure this is set -->
    //  <init-param>
    //    <param-name>subjectVirtualAttribute_0_nameLong</param-name>
    //    <param-value>${grouperUtilElSafe.appendIfNotBlankString(grouperUtilElSafe.defaultIfBlank(subject.getAttributeValue('cn'), ''), ' - ', grouperUtilElSafe.defaultIfBlank(subject.getAttributeValue('eduPersonSchoolCollegeName'), ''))}</param-value>
    //  </init-param>
    //  
    //  <init-param>
    //    <param-name>sortAttribute0</param-name>
    //    <param-value>nameLong</param-value>
    //  </init-param>
    //  <init-param>
    //    <param-name>searchAttribute0</param-name>
    //    <param-value>nameLong</param-value>
    //  </init-param>
    //
    //  <internal-attribute>searchAttribute0</internal-attribute>
    //
    //  <!-- ///Attributes you would like to display when doing a search  -->
    //  <attribute>eduPersonSchoolCollegeName</attribute>
    //  <attribute>sn</attribute>
    //  <attribute>cmuStudentClass</attribute>
    //  <attribute>givenName</attribute>
    //  <attribute>mail</attribute>
    // 
    //</source>

    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.ldap.source", false)) {

      Subject subject = SubjectFinder.findByIdAndSource("00000000-0000-1000-2F4C-0800207F02E6", "cmu", true);
      
      assertEquals("Vincent Lun", subject.getName());
  
      assertEquals("vlun@andrew.cmu.edu", subject.getAttributeValue("mail"));
  
      assertEquals("Vincent Lun - Student Employment", subject.getDescription());
      
      assertEquals("Vincent Lun - Student Employment", subject.getAttributeValue("nameLong"));
      
      //check the search and sort attributes
      Member member = MemberFinder.findBySubject(GrouperSession.startRootSession(), subject, true);

      assertEquals("Vincent Lun - Student Employment", member.getSortString0());
      assertEquals("vincent lun - student employment", member.getSearchString0());
      
      Subject subject2 = SubjectFinder.findByIdentifierAndSource("vlun", "cmu", true);
      
      assertEquals(subject.getId(), subject2.getId());

      Set<Subject> subjects = SubjectFinder.findAll("Vincent Lun", "cmu");
      
      //hmmm, will this be one?  maybe
      assertEquals(1, GrouperUtil.length(subjects));

      assertEquals(subject.getId(), subjects.iterator().next().getId());
    }    
  }
  
  
}

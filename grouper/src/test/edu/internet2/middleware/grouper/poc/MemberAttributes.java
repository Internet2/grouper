/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class MemberAttributes {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject subject = SubjectFinder.findById("10021368", true);
    Member member = MemberFinder.findBySubject(grouperSession, subject, true); 
    member.updateMemberAttributes(member.getSubject(), true);

    
    member = MemberFinder.findByUuid(grouperSession, member.getUuid(), true);
    
    member.updateMemberAttributes(member.getSubject(), true);
    
    
  }

}

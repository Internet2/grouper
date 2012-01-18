/**
 * 
 */
package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class EsbConsumerTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new EsbConsumerTest("testFilter"));
  }
  
  /**
   * 
   * @param name
   */
  public EsbConsumerTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testFilter() {
    
    EsbEvent esbEvent = new EsbEvent();
    esbEvent.setEventType("MEMBERSHIP_DELETE");
    esbEvent.setMembershipType("flattened");
    assertTrue(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' "));
    assertFalse(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'immediate' "));

    esbEvent.setSourceId("pennperson");
    
    assertTrue(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.sourceId == 'pennperson' "));
    assertFalse(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.sourceId == 'g:gsa' "));
    
    esbEvent.setGroupName("a:b:something");

    assertTrue(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.sourceId == 'pennperson' && event.groupName =~ '^a\\:b\\:.*$' "));
    assertFalse(EsbConsumer.matchesFilter(esbEvent, "event.eventType == 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.sourceId == 'pennperson' && event.groupName =~ '^a\\:c\\:.*$' "));

    Subject subject = SubjectFinder.findById(SubjectTestHelper.SUBJ0_ID, true);

    // See https://bugs.internet2.edu/jira/browse/GRP-450
    String loginid = subject.getAttributeValue("loginid");
    if (StringUtils.isBlank(loginid)) {
      loginid = subject.getAttributeValue("LOGINID");
    }
    assertTrue(StringUtils.isNotBlank(loginid));
    
    esbEvent.setSubjectId(subject.getId());
    esbEvent.setSourceId(subject.getSourceId());
    esbEvent.addSubjectAttribute("loginid", "abc");
    
    assertTrue(EsbConsumer.matchesFilter(esbEvent, "event.subjectHasAttribute('loginid')"));
    assertFalse(EsbConsumer.matchesFilter(esbEvent, "event.subjectHasAttribute('loginidabc')"));
    
    
  }
  
  /**
   * 
   */
  public void testJsonSubject() {
    EsbEvent esbEvent = new EsbEvent();
    GrouperSession.startRootSession();
    Subject subject = SubjectFinder.findById("GrouperSystem", true);
    esbEvent.setSourceId(subject.getSourceId());
    esbEvent.setSubjectId(subject.getId());
      
    assertNotNull(esbEvent.retrieveSubject());
    
    EsbEvents esbEvents = new EsbEvents();
    esbEvents.setEsbEvent(new EsbEvent[]{esbEvent});
    
    String eventJsonString = GrouperUtil.jsonConvertToNoWrap(esbEvents);
    
    eventJsonString = GrouperUtil.indent(eventJsonString, true);
    
    System.out.println(eventJsonString);
    
  }
  
}

/**
 * @author mchyzer
 * $Id: TestSubjectFinder.java,v 1.1 2009-12-27 02:31:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import java.util.Set;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 *
 */
public class TestSubjectFinder extends GrouperTest {

  /**
   * @param name
   */
  public TestSubjectFinder(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFindAll() {
    Set<Source> sources = GrouperUtil.toSet(SourceManager.getInstance().getSource("jdbc"));
    Set<Subject> subjects = SubjectFinder.findAll("whatever", sources);
    assertEquals(0, GrouperUtil.length(subjects));
    
    sources = GrouperUtil.convertSources("jdbc,g:isa");
    
    subjects = SubjectFinder.findAll(SubjectTestHelper.SUBJ0_ID);
    
    assertEquals(1, GrouperUtil.length(subjects));
    
    assertEquals(SubjectTestHelper.SUBJ0_ID, subjects.iterator().next().getId());
    
    
    
  }
  
}

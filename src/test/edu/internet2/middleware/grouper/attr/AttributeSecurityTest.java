/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 *
 */
public class AttributeSecurityTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeSecurityTest("testAddAttribute"));
  }

  /** grouper sesion */
  static GrouperSession grouperSession;
  /** edu stem */
  private Stem edu;
  /** test group */
  private Group group;
  /** root stem */
  private Stem root;
  
  /**
   * 
   */
  public AttributeSecurityTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeSecurityTest(String name) {
    super(name);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    group = StemHelper.addChildGroup(this.edu, "group", "the group");
    @SuppressWarnings("unused")
    Subject subject = SubjectTestHelper.SUBJ0;
  }

  /**
   * @throws Exception 
   */
  public void testAddAttribute() throws Exception {
    group.addMember(SubjectTestHelper.SUBJ0);
  }
}

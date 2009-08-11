/**
 * @author mchyzer
 * $Id: GrouperSessionTest.java,v 1.1.2.1 2009-08-11 20:16:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import junit.textui.TestRunner;
import net.sf.ehcache.CacheManager;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperSessionTest extends GrouperTest {

  /** edu stem */
  private Stem edu;
  /** grouper sesion */
  private GrouperSession grouperSession;
  /** root stem */
  private Stem root;
  /** add group */
  @SuppressWarnings("unused")
  private Group aGroup = null;

  /**
   * 
   */
  public GrouperSessionTest() {
  }

  /**
   * @param name
   */
  public GrouperSessionTest(String name) {
    super(name);

  }

  /**
   * test caches in grouper session
   * @throws Exception 
   */
  public void testCaches() throws Exception {
    
    int cacheSize = CacheManager.ALL_CACHE_MANAGERS.size();
    Subject subject = SubjectTestHelper.SUBJ0;
    GrouperSession grouperSession = GrouperSession.start(subject);
    GroupFinder.findByName(grouperSession, "edu:aGroup", false);
    GrouperSession rootSession = grouperSession.internal_getRootSession();
    GroupFinder.findByName(rootSession, "edu:aGroup", false);
    assertTrue(CacheManager.ALL_CACHE_MANAGERS.size() + ", " + cacheSize, CacheManager.ALL_CACHE_MANAGERS.size() > cacheSize);
    grouperSession.stop();
    assertEquals(CacheManager.ALL_CACHE_MANAGERS.size(), cacheSize);
    
    
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperSessionTest("testCaches"));
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    try {
      this.grouperSession     = SessionHelper.getRootSession();
      this.root  = StemHelper.findRootStem(grouperSession);
      this.edu   = StemHelper.addChildStem(root, "edu", "education");
      this.aGroup = edu.addChildGroup("aGroup", "aGroup");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  

}

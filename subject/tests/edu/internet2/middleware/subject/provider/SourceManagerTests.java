package edu.internet2.middleware.subject.provider;

import java.util.Collection;
import java.util.Iterator;

import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;

import junit.framework.*;

/**
 * Unit tests for SourceManager.
 */
public class SourceManagerTests extends TestCase {

	private SourceManager mgr;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(SourceManagerTests.class);
	}

	/**
	 * Default constructor.
	 */
	public SourceManagerTests() {
		super();
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.mgr = SourceManager.getInstance();
	}

	/**
	 * Test case for loading sources from sources.xml
	 */
	public void testInitSource() {
		try {
			for (Iterator iter = this.mgr.getSources().iterator(); iter.hasNext();) {
				BaseSourceAdapter source = (BaseSourceAdapter)iter.next();
				source.init();
			}
		} catch (Exception ex) {
			fail("Failed test to init sources: " + ex.getMessage());
		}
	}

  /**
   * Test case for getting sources by SubjectType
   */
  public void testGetSourcesByType() {
    // Given that each of the current source adapters all have a fair
    // amount of overhead, external dependencies and configuration
    // requirements that might vary **widely** depending upon the
    // circumstances it seems easier to test with **no** configured
    // adapters. 
    Collection people = mgr.getSources(SubjectTypeEnum.valueOf("person"));
    Assert.assertNotNull("people !null", people);
    Assert.assertEquals("Person sources == 0", 0, people.size());
		
    Collection groups = mgr.getSources(SubjectTypeEnum.valueOf("group"));
    Assert.assertNotNull("groups !null", groups);
    Assert.assertEquals("Group sources == 0", 0, groups.size());

    Collection apps   = mgr.getSources(SubjectTypeEnum.valueOf("application"));
    Assert.assertNotNull("apps !null", apps);
    Assert.assertEquals("Application sources == 0", 0, apps.size());
  } // public void testSourcesByType()

}

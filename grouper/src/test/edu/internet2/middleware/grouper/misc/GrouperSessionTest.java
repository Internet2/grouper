/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id: GrouperSessionTest.java,v 1.3 2009-11-11 15:34:46 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import junit.textui.TestRunner;
import net.sf.ehcache.CacheManager;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
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
    
    GrouperCacheUtils.clearAllCaches();
    int cacheSize = CacheManager.ALL_CACHE_MANAGERS.size();
    Subject subject = SubjectTestHelper.SUBJ0;
    GrouperSession grouperSession = GrouperSession.start(subject);
    GroupFinder.findByName(grouperSession, "edu:aGroup", false);
    GrouperSession rootSession = grouperSession.internal_getRootSession();
    GroupFinder.findByName(rootSession, "edu:aGroup", false);
    
    //##########################################
    //NOT SURE WHY THIS FAILS SOMETIMES, JUST RUN BY ITSELF AND IT SHOULD PASS!!!!
    assertEquals( CacheManager.ALL_CACHE_MANAGERS.size(), cacheSize);
    //##########################################
    
    grouperSession.stop();
    assertEquals(CacheManager.ALL_CACHE_MANAGERS.size(), cacheSize);
    
    
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperSessionTest("testShorthandByIdNoThreadLocal"));
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
  
  /**
   * 
   */
  public void testShorthandById() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }

    try {
      SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ0_IDENTIFIER, true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }

    //now try to create a session by id
    GrouperSession.startBySubjectIdAndSource(SubjectTestHelper.SUBJ0_ID, null);
    
    SubjectFinder.findByIdentifier("edu:aGroup", true);
    
    
  }

  /**
   * 
   */
  public void testShorthandByIdNoThreadLocal() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }

    try {
      SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ0_IDENTIFIER, true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }

    //now try to create a session by id
    GrouperSession.startBySubjectIdAndSource(SubjectTestHelper.SUBJ0_ID, null, false);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Shouldnt work");
    } catch (Exception e) {
      //good
    }
    
  }

  /**
   * 
   */
  public void testShorthandByIdAndSource() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    try {
      SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ0_IDENTIFIER, true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    //now try to create a session by id
    GrouperSession.startBySubjectIdAndSource(SubjectTestHelper.SUBJ0_ID, "jdbc");
    
    SubjectFinder.findByIdentifier("edu:aGroup", true);
    
    
  }

  /**
   * 
   */
  public void testShorthandByIdentifier() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    try {
      SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ0_IDENTIFIER, true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    //now try to create a session by id
    GrouperSession.startBySubjectIdentifierAndSource(SubjectTestHelper.SUBJ0_IDENTIFIER, null);
    
    SubjectFinder.findByIdentifier("edu:aGroup", true);
    
    
  }

  /**
   * 
   */
  public void testShorthandByIdentifierAndSource() {
    
    GrouperSession.stopQuietly(this.grouperSession);
    
    try {
      SubjectFinder.findByIdentifier("edu:aGroup", true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    try {
      SubjectFinder.findByIdentifier(SubjectTestHelper.SUBJ0_IDENTIFIER, true);
      fail("Cant search for subjects without session");
    } catch (Exception e) {
      //good
    }
  
    //now try to create a session by id
    GrouperSession.startBySubjectIdentifierAndSource(SubjectTestHelper.SUBJ0_IDENTIFIER, "jdbc");
    
    SubjectFinder.findByIdentifier("edu:aGroup", true);
    
    
  }

}

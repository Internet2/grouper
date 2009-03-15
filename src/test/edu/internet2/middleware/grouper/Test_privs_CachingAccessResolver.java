/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.internal.util.Realize;
import edu.internet2.middleware.grouper.privs.AccessAdapter;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessWrapper;
import edu.internet2.middleware.grouper.privs.CachingAccessResolver;


/**
 * Test {@link CachingAccessResolver}.
 * @author  blair christensen.
 * @version $Id: Test_privs_CachingAccessResolver.java,v 1.5 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_privs_CachingAccessResolver extends GrouperTest {


  private CachingAccessResolver resolver;
  private Group                 g;



  public void setUp() {
    super.setUp();
    try {
      this.g = StemFinder.findRootStem(
                 GrouperSession.start( SubjectFinder.findRootSubject() )
               )
               .addChildStem("top", "top")
               .addChildGroup("top_group", "top_group")
               ;
      this.resolver = new CachingAccessResolver( 
                        new AccessWrapper( 
                          GrouperSession.start( SubjectFinder.findRootSubject() ),
                          (AccessAdapter) Realize.instantiate( 
                            new ApiConfig().getProperty( ApiConfig.ACCESS_PRIVILEGE_INTERFACE )
                          )
                        )
                      );
    }
    catch (Exception e) {
      throw new GrouperException( "error in setUp(): " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_constructor_nullDecoratedResolver() {
    try {
      new CachingAccessResolver(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }



  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_cacheMiss() {
    long before = resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getMisses();
    resolver.hasPrivilege( this.g, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
    assertEquals( before + 1, resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getMisses() );
  }
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_cacheHit() {
   //2007/12/03: Gary Brown
   //hasPrivilege now first calls getPrivileges. If cacheMiss is called first it sets the cache for all privs
   //so only check after 2nd explicit check
    resolver.hasPrivilege( this.g, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
    long before = resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getHits();
    resolver.hasPrivilege( this.g, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
    assertEquals( before + 1, resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getHits() );
  }
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_emptyCache() {
    assertEquals( 0, resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getSize() );
  }
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_cacheSize() {
    resolver.hasPrivilege( this.g, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN );
    //2007/12/03: Gary Brown
	//hasPrivilege calls getPrivileges which caches all 6 ACCESS privs
	assertEquals( 6, resolver.getStats(CachingAccessResolver.CACHE_HASPRIV).getSize() );
  }

}


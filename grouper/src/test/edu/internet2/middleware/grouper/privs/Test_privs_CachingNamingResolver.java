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

package edu.internet2.middleware.grouper.privs;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.Realize;


/**
 * Test {@link CachingNamingResolver}.
 * @author  blair christensen.
 * @version $Id: Test_privs_CachingNamingResolver.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_privs_CachingNamingResolver extends GrouperTest {

  /**
   * 
   */
  public Test_privs_CachingNamingResolver() {
    
  }
  
  /**
   * 
   * @param name
   */
  public Test_privs_CachingNamingResolver(String name) {
    super(name);
  }
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_privs_CachingNamingResolver("test_hasPrivilege_cacheMiss"));
  }

  private CachingNamingResolver resolver;
  private Stem                  ns;



  public void setUp() {
    super.setUp();
    try {
      this.ns = StemFinder.findRootStem(
                 GrouperSession.start( SubjectFinder.findRootSubject() )
               );
      this.resolver = new CachingNamingResolver( 
                        new NamingWrapper( 
                          GrouperSession.start( SubjectFinder.findRootSubject() ),
                          (NamingAdapter) Realize.instantiate( 
                            new ApiConfig().getProperty( ApiConfig.NAMING_PRIVILEGE_INTERFACE )
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
      new CachingNamingResolver(null);
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

    EhcacheController.ehcacheController().getCache(CachingNamingResolver.CACHE_HASPRIV).setStatisticsEnabled(true);

    long before = resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getMisses();
    resolver.hasPrivilege( this.ns, SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
    assertEquals( before + 1, resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getMisses() );
  }
  
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_cacheHit() {
    EhcacheController.ehcacheController().getCache(CachingNamingResolver.CACHE_HASPRIV).setStatisticsEnabled(true);

    long before = resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getHits();
    resolver.hasPrivilege( this.ns, SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
    assertEquals( before, resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getHits() );
    resolver.hasPrivilege( this.ns, SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
    assertEquals( before + 1, resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getHits() );
  }
  /**
   * @since   1.2.1
   */
  public void test_hasPrivilege_cacheSize() {
    long size = resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getSize();
    resolver.hasPrivilege( this.ns, SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
    assertEquals(size+ 1, resolver.getStats(CachingNamingResolver.CACHE_HASPRIV).getSize() );
  }

}


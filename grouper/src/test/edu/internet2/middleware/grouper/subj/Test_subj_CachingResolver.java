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

package edu.internet2.middleware.grouper.subj;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.subj.CachingResolver;
import edu.internet2.middleware.grouper.subj.SourcesXmlResolver;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * Test {@link CachingResolver}.
 * @author  blair christensen.
 * @version $Id: Test_subj_CachingResolver.java,v 1.2 2009-08-12 04:52:21 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_subj_CachingResolver extends GrouperTest {

  private static final  String          BAD_ID    = "subject does not exist";
  private static final  String          GOOD_ID   = GrouperConfig.ALL;



  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }



//  public void test_constructor_nullSourceManager() {
//    try {
//      new CachingResolver(null);
//      fail("failed to throw IllegalArgumentException");
//    }
//    catch (IllegalArgumentException eExpected) {
//      assertTrue("threw expected exception", true);
//    }
//  }
//
//
//  public void test_find_Id_cacheMiss() 
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    long before = CachingResolver.findCache.getStats().getCacheMisses();
//    assertNull(SubjectFinder.findById(BAD_ID, false));
//    assertEquals( before + 1, CachingResolver.findCache.getStats().getCacheMisses() );
//  }
//
//  public void test_find_Id_cacheHit()
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    long before = CachingResolver.findCache.getStats().getCacheHits();
//    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( before, CachingResolver.findCache.getStats().getCacheHits() );
//    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( before + 1, CachingResolver.findCache.getStats().getCacheHits() );
//  }
//
//  public void test_find_Id_emptyCache() {
//    assertEquals( 0, CachingResolver.findCache.getStats().getObjectCount() );
//  }
//
//  public void test_find_Id_cacheSize()
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( 3, CachingResolver.findCache.getStats().getObjectCount());
//  }
//
//
//  public void test_findAll_Query_cacheMiss() {
//    long before = CachingResolver.findAllCache.getStats().getCacheMisses();
//    resolver.findAll(BAD_ID);
//    assertEquals( before + 1, resolver.getStats( CachingResolver.CACHE_FINDALL).getMisses() );
//  }
//
//  public void test_findAll_Query_cacheHit() {
//    long before = CachingResolver.findAllCache.getStats().getCacheHits();
//    resolver.findAll(GOOD_ID);
//    assertEquals( before, CachingResolver.findAllCache.getStats().getCacheHits());
//    resolver.findAll(GOOD_ID);
//    assertEquals( before + 1, CachingResolver.findAllCache.getStats().getCacheHits() );
//  }
//
//  public void test_findAll_Query_emptyCache() {
//    assertEquals( 0, CachingResolver.findAllCache.getStats().getObjectCount() );
//  }
//
//  public void test_findAll_Query_cacheSize() {
//    resolver.findAll(GOOD_ID); 
//    assertEquals( 1, CachingResolver.findAllCache.getStats().getObjectCount() );
//  }
//
//
//  public void test_findByIdentifier_Id_cacheMiss() 
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    long before = CachingResolver.findByIdentifierCache.getStats().getCacheMisses();
//    try {
//      resolver.findByIdentifier(BAD_ID);
//    }
//    catch (SubjectNotFoundException eExpected) {
//      // ignore
//    }
//    assertEquals( before + 1, CachingResolver.findByIdentifierCache.getStats().getCacheMisses() );
//  }
//
//  public void test_findByIdentifier_Id_cacheHit()
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    long before = CachingResolver.findByIdentifierCache.getStats().getCacheHits();
//    resolver.findByIdentifier(GOOD_ID);
//    assertEquals( before, CachingResolver.findByIdentifierCache.getStats().getCacheHits() );
//    resolver.findByIdentifier(GOOD_ID);
//    assertEquals( before + 1, CachingResolver.findByIdentifierCache.getStats().getCacheHits() );
//  }
//
//  public void test_findByIdentifier_Id_emptyCache() {
//    assertEquals( 0, CachingResolver.findByIdentifierCache.getStats().getObjectCount() );
//  }
//
//  public void test_findByIdentifier_Id_cacheSize()
//    throws  SubjectNotFoundException,
//            SubjectNotUniqueException
//  {
//    resolver.findByIdentifier(GOOD_ID); // this add 3 items to the cache
//    assertEquals( 3, CachingResolver.findByIdentifierCache.getStats().getObjectCount() );
//  }

}


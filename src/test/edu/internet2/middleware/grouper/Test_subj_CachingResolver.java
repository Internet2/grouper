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
import  edu.internet2.middleware.grouper.subj.CachingResolver;
import  edu.internet2.middleware.grouper.subj.SourcesXmlResolver;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;
import  edu.internet2.middleware.subject.provider.SourceManager;


/**
 * Test {@link CachingResolver}.
 * @author  blair christensen.
 * @version $Id: Test_subj_CachingResolver.java,v 1.3 2007-08-27 15:53:53 blair Exp $
 * @since   1.2.1
 */
public class Test_subj_CachingResolver extends GrouperTest {

  private static final  String          BAD_ID    = "subject does not exist";
  private static final  String          GOOD_ID   = GrouperConfig.ALL;
  private               CachingResolver resolver;



  public void setUp() {
    super.setUp();
    try {
      this.resolver = new CachingResolver( new SourcesXmlResolver( SourceManager.getInstance() ) );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "error in setUp(): " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }



  public void test_constructor_nullSourceManager() {
    try {
      new CachingResolver(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_find_Id_cacheMiss() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    long before = resolver.getStats(CachingResolver.CACHE_FIND).getMisses();
    try {
      resolver.find(BAD_ID);
    }
    catch (SubjectNotFoundException eExpected) {
      // ignore
    }
    assertEquals( before + 1, resolver.getStats(CachingResolver.CACHE_FIND).getMisses() );
  }

  public void test_find_Id_cacheHit()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    long before = resolver.getStats(CachingResolver.CACHE_FIND).getHits();
    resolver.find(GOOD_ID);
    assertEquals( before, resolver.getStats(CachingResolver.CACHE_FIND).getHits() );
    resolver.find(GOOD_ID);
    assertEquals( before + 1, resolver.getStats(CachingResolver.CACHE_FIND).getHits() );
  }

  public void test_find_Id_emptyCache() {
    assertEquals( 0, resolver.getStats(CachingResolver.CACHE_FIND).getSize() );
  }

  public void test_find_Id_cacheSize()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    resolver.find(GOOD_ID); // this add 3 items to the cache
    assertEquals( 3, resolver.getStats(CachingResolver.CACHE_FIND).getSize() );
  }


  public void test_findAll_Query_cacheMiss() {
    long before = resolver.getStats( CachingResolver.CACHE_FINDALL ).getMisses();
    resolver.findAll(BAD_ID);
    assertEquals( before + 1, resolver.getStats( CachingResolver.CACHE_FINDALL).getMisses() );
  }

  public void test_findAll_Query_cacheHit() {
    long before = resolver.getStats( CachingResolver.CACHE_FINDALL ).getHits();
    resolver.findAll(GOOD_ID);
    assertEquals( before, resolver.getStats( CachingResolver.CACHE_FINDALL).getHits() );
    resolver.findAll(GOOD_ID);
    assertEquals( before + 1, resolver.getStats( CachingResolver.CACHE_FINDALL).getMisses() );
  }

  public void test_findAll_Query_emptyCache() {
    assertEquals( 0, resolver.getStats(CachingResolver.CACHE_FINDALL).getSize() );
  }

  public void test_findAll_Query_cacheSize() {
    resolver.findAll(GOOD_ID); 
    assertEquals( 1, resolver.getStats(CachingResolver.CACHE_FINDALL).getSize() );
  }


  public void test_findByIdentifier_Id_cacheMiss() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    long before = resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getMisses();
    try {
      resolver.findByIdentifier(BAD_ID);
    }
    catch (SubjectNotFoundException eExpected) {
      // ignore
    }
    assertEquals( before + 1, resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getMisses() );
  }

  public void test_findByIdentifier_Id_cacheHit()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    long before = resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getHits();
    resolver.findByIdentifier(GOOD_ID);
    assertEquals( before, resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getHits() );
    resolver.findByIdentifier(GOOD_ID);
    assertEquals( before + 1, resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getHits() );
  }

  public void test_findByIdentifier_Id_emptyCache() {
    assertEquals( 0, resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getSize() );
  }

  public void test_findByIdentifier_Id_cacheSize()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    resolver.findByIdentifier(GOOD_ID); // this add 3 items to the cache
    assertEquals( 3, resolver.getStats(CachingResolver.CACHE_FINDBYIDENTIFIER).getSize() );
  }

}


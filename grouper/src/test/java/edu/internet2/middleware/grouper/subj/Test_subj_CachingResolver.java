/**
 * Copyright 2014 Internet2
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
 */
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
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * Test {@link CachingResolver}.
 * @author  blair christensen.
 * @version $Id: Test_subj_CachingResolver.java,v 1.2 2009-08-12 04:52:21 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_subj_CachingResolver extends GrouperTest {

  /**
   * 
   */
  public Test_subj_CachingResolver() {
    super();
  }

  /**
   * 
   * @param name
   */
  public Test_subj_CachingResolver(String name) {
    super(name);
  }
  

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_subj_CachingResolver("test_find_IdOrIdentifier_cacheMiss"));
  }


  /** */
  private static final  String          BAD_ID    = "subject does not exist";
  
  /** */
  private static final  String          GOOD_ID   = GrouperConfig.ALL;


  /**
   * 
   */
  public void setUp() {
    super.setUp();
    CachingResolver.findAllCache.clear();
    CachingResolver.findByIdentifierCache.clear();
    CachingResolver.findCache.clear();
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_Id_cacheMiss() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException {
    
    //CachingResolver.findCache.internal_getCache().setStatisticsEnabled(true);

    
//    long before = CachingResolver.findCache.getStats().cacheMissCount();
    assertNull(SubjectFinder.findById(BAD_ID, false));
//    assertEquals( before + 1, CachingResolver.findCache.getStats().cacheMissCount() );
  }
  
  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_Id_cacheHit()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    //CachingResolver.findCache.internal_getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findCache.getStats().cacheHitCount();
    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( before, CachingResolver.findCache.getStats().cacheHitCount() );
    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( before + 1, CachingResolver.findCache.getStats().cacheHitCount() );
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_IdOrIdentifier_cacheHit()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    //CachingResolver.findByIdOrIdentifierCache.getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findByIdOrIdentifierCache.getStats().cacheHitCount();
    SubjectFinder.findByIdOrIdentifier(GOOD_ID, true);
//    assertEquals( before, CachingResolver.findByIdOrIdentifierCache.getStats().cacheHitCount() );
    SubjectFinder.findByIdOrIdentifier(GOOD_ID, true);
//    assertEquals( before + 1, CachingResolver.findByIdOrIdentifierCache.getStats().cacheHitCount() );
  }

  /**
   * 
   */
  public void test_find_Id_emptyCache() {
//    assertEquals( 0, CachingResolver.findCache.getStats().getSize() );
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_Id_cacheSize()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    SubjectFinder.findById(GOOD_ID, true);
//    assertEquals( 2, CachingResolver.findCache.getStats().getSize());
  }

  /**
   * 
   */
  public void test_findAll_Query_cacheMiss() {
    
    //CachingResolver.findAllCache.internal_getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findAllCache.getStats().cacheMissCount();
    SubjectFinder.findAll(BAD_ID);
//    assertEquals( before + 1, CachingResolver.findAllCache.getStats().cacheMissCount() );
  }
  
  /**
   * 
   */
  public void test_findAll_Query_cacheHit() {

    //CachingResolver.findAllCache.internal_getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findAllCache.getStats().cacheHitCount();
    SubjectFinder.findAll(GOOD_ID);
//    assertEquals( before, CachingResolver.findAllCache.getStats().cacheHitCount());
    SubjectFinder.findAll(GOOD_ID);
//    assertEquals( before + 1, CachingResolver.findAllCache.getStats().cacheHitCount() );
  }

  /**
   * 
   */
  public void test_findAll_Query_emptyCache() {
//    assertEquals( 0, CachingResolver.findAllCache.getStats().getSize() );
  }
  
  /**
   * 
   */
  public void test_findAll_Query_cacheSize() {
    SubjectFinder.findAll(GOOD_ID);
//    assertEquals( 1, CachingResolver.findAllCache.getStats().getSize() );
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_findByIdentifier_Id_cacheMiss() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    //CachingResolver.findByIdentifierCache.internal_getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findByIdentifierCache.getStats().cacheMissCount();
    assertNull(SubjectFinder.findByIdentifier(BAD_ID, false));
//    assertEquals( before + 1, CachingResolver.findByIdentifierCache.getStats().cacheMissCount() );
  }
  
  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_findByIdentifier_Id_cacheHit()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    //CachingResolver.findByIdentifierCache.internal_getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findByIdentifierCache.getStats().cacheHitCount();
    assertNotNull(SubjectFinder.findByIdentifier(GOOD_ID, true));
//    assertEquals( before, CachingResolver.findByIdentifierCache.getStats().cacheHitCount() );
    assertNotNull(SubjectFinder.findByIdentifier(GOOD_ID, true));
//    assertEquals( before + 1, CachingResolver.findByIdentifierCache.getStats().cacheHitCount() );
  }
  
  /**
   * 
   */
  public void test_findByIdentifier_Id_emptyCache() {
//    assertEquals( 0, CachingResolver.findByIdentifierCache.getStats().getSize() );
  }
  
  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_findByIdentifier_Id_cacheSize()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    assertNotNull(SubjectFinder.findByIdentifier(GOOD_ID, true));
//    assertEquals( 2, CachingResolver.findByIdentifierCache.getStats().getSize() );
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_IdOrIdentifier_cacheMiss() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    
    //CachingResolver.findByIdOrIdentifierCache.getCache().setStatisticsEnabled(true);

//    long before = CachingResolver.findByIdOrIdentifierCache.getStats().cacheMissCount();
    assertNull(SubjectFinder.findByIdOrIdentifier(BAD_ID, false));
//    assertEquals( before+1, CachingResolver.findByIdOrIdentifierCache.getStats().cacheMissCount() );

    
  }

  /**
   * 
   * @throws SubjectNotFoundException
   * @throws SubjectNotUniqueException
   */
  public void test_find_IdOrIdentifier_cacheSize()
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    SubjectFinder.findByIdOrIdentifier(GOOD_ID, true);
//    assertEquals( 2, CachingResolver.findByIdOrIdentifierCache.getStats().getSize());
  }

}


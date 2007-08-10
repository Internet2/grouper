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
import  edu.internet2.middleware.grouper.subj.SubjectResolver;
import  edu.internet2.middleware.grouper.subj.SubjectResolverFactory;
import  edu.internet2.middleware.subject.SourceUnavailableException;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 * Test {@link SubjecResolver} implementation chain.
 * @author  blair christensen.
 * @version $Id: Test_subj_SubjectResolver.java,v 1.2 2007-08-10 13:19:14 blair Exp $
 * @since   @HEAD@
 */
public class Test_subj_SubjectResolver extends GrouperTest {


  private SubjectResolver resolver;


  public void setUp() {
    super.setUp();
    try {
      this.resolver = SubjectResolverFactory.getInstance();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new GrouperRuntimeException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }


  public void test_find_SubjectId_nullId() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_find_SubjectIdAndType_nullId() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_find_SubjectIdAndType_nullType() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find("GrouperSystem", null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_find_SubjectIdAndTypeAndSource_nullId() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find(null, null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_find_SubjectIdAndTypeAndSource_nullType() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find("GrouperSystem", null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_find_SubjectIdAndTypeAndSource_nullSource() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.find("GrouperSystem", "application", null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_findAll_Query_nullQuery() {
    try {
      this.resolver.findAll(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_findAll_QueryAndSource_nullQuery() 
    throws  SourceUnavailableException
  {
    try {
      this.resolver.findAll(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findAll_QueryAndSource_nullSource() 
    throws  SourceUnavailableException
  {
    try {
      this.resolver.findAll("%", null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_findByIdentifier_SubjectId_nullId() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_findByIdentifier_SubjectIdAndType_nullId() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier(null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findByIdentifier_SubjectIdAndType_nullType() 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier("GrouperSystem", null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_findByIdentifier_SubjectIdAndTypeAndSource_nullId() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier(null, null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findByIdentifier_SubjectIdAndTypeAndSource_nullType() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier("GrouperSystem", null, null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_findByIdentifier_SubjectIdAndTypeAndSource_nullSource() 
    throws  SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    try {
      this.resolver.findByIdentifier("GrouperSystem", "application", null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }


  public void test_getSource_nullSource() 
    throws  SourceUnavailableException
  {
    try {
      this.resolver.getSource(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getSource_invalidSource() {
    try {
      this.resolver.getSource("invalid source id");
      fail("failed to throw SourceUnavailableException");
    }
    catch (SourceUnavailableException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getSource_getGrouperSourceAdapter() 
    throws  SourceUnavailableException
  {
    assertEquals( "g:gsa", this.resolver.getSource("g:gsa").getId() );
  }

  public void test_getSource_getJDBCSourceAdapter() 
    throws  SourceUnavailableException
  {
    assertEquals( "jdbc", this.resolver.getSource("jdbc").getId() );
  }


  public void test_getSources_defaultNumberOfSources() {
    // TODO 20070803 this should be 2 but my other code currently breaks this. 
    assertEquals( 3, this.resolver.getSources().size() );
  }
 
 
  public void test_getSources_Type_nullType() {
    try {
      this.resolver.getSources(null);
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }

  public void test_getSources_Type_invalidType() {
    try {
      this.resolver.getSources("invalid type").size();
      fail("failed to throw IllegalArgumentException");
    }
    catch (IllegalArgumentException eExpected) {
      assertTrue("threw expected exception", true);
    }
  }
  
  public void test_getSources_Type_applicationType() {
    assertEquals( 1, this.resolver.getSources("application").size() );
  }
  
  public void test_getSources_Type_groupType() {
    assertEquals( 1, this.resolver.getSources("group").size() );
  }
  
  public void test_getSources_Type_personType() {
    assertEquals( 1, this.resolver.getSources("person").size() );
  }

/*
  public void testFindGrouperAllByIdentifier() {
    LOG.info("testFindGrouperAllByIdentifier");
    try {
      Subject subj = SubjectFinder.findByIdentifier(GrouperConfig.ALL);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifier()
*/
  
}


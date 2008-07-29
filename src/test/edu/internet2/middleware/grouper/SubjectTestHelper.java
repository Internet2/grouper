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
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;

/**
 * {@link Subject} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: SubjectTestHelper.java,v 1.11 2008-07-29 07:05:20 mchyzer Exp $
 */
 public class SubjectTestHelper {

  // Protected Class Constants
  public static final Subject  SUBJA;
  public static final Subject  SUBJR;
  public static final Subject  SUBJ0;
  public static final Subject  SUBJ1;
  public static final Subject  SUBJ2;
  public static final Subject  SUBJ3;
  public static final Subject  SUBJ4;
  public static final Subject  SUBJ5;
  public static final Subject  SUBJ6;
  public static final Subject  SUBJ7;
  public static final Subject  SUBJ8;
  public static final Subject  SUBJ9;
  public static final String   SUBJ0_ID    = "test.subject.0";
  public static final String   SUBJ0_NAME  = "my name is test.subject.0";
  public static final String   SUBJ0_TYPE  = "person";
  public static final String   SUBJ1_ID    = "test.subject.1";
  public static final String   SUBJ1_NAME  = "my name is test.subject.1";
  public static final String   SUBJ1_TYPE  = "person";
  public static final String   SUBJ2_ID    = "test.subject.2";
  public static final String   SUBJ2_NAME  = "my name is test.subject.2";
  public static final String   SUBJ2_TYPE  = "person";
  public static final String   SUBJ3_ID    = "test.subject.3";
  public static final String   SUBJ3_NAME  = "my name is test.subject.3";
  public static final String   SUBJ3_TYPE  = "person";
  public static final String   SUBJ4_ID    = "test.subject.4";
  public static final String   SUBJ4_NAME  = "my name is test.subject.4";
  public static final String   SUBJ4_TYPE  = "person";
  public static final String   SUBJ5_ID    = "test.subject.5";
  public static final String   SUBJ5_NAME  = "my name is test.subject.5";
  public static final String   SUBJ5_TYPE  = "person";
  public static final String   SUBJ6_ID    = "test.subject.6";
  public static final String   SUBJ6_NAME  = "my name is test.subject.6";
  public static final String   SUBJ6_TYPE  = "person";
  public static final String   SUBJ7_ID    = "test.subject.7";
  public static final String   SUBJ7_NAME  = "my name is test.subject.7";
  public static final String   SUBJ7_TYPE  = "person";
  public static final String   SUBJ8_ID    = "test.subject.8";
  public static final String   SUBJ8_NAME  = "my name is test.subject.8";
  public static final String   SUBJ8_TYPE  = "person";
  public static final String   SUBJ9_ID    = "test.subject.9";
  public static final String   SUBJ9_NAME  = "my name is test.subject.9";
  public static final String   SUBJ9_TYPE  = "person";
  public static final String   SUBJ_ALL    = "GrouperAll";
  public static final String   SUBJ_ROOT   = "GrouperSystem";

  static {
    try {
      SUBJ0 = SubjectFinder.findById(SUBJ0_ID);
      SUBJ1 = SubjectFinder.findById(SUBJ1_ID);
      SUBJ2 = SubjectFinder.findById(SUBJ2_ID);
      SUBJ3 = SubjectFinder.findById(SUBJ3_ID);
      SUBJ4 = SubjectFinder.findById(SUBJ4_ID);
      SUBJ5 = SubjectFinder.findById(SUBJ5_ID);
      SUBJ6 = SubjectFinder.findById(SUBJ6_ID);
      SUBJ7 = SubjectFinder.findById(SUBJ7_ID);
      SUBJ8 = SubjectFinder.findById(SUBJ8_ID);
      SUBJ9 = SubjectFinder.findById(SUBJ9_ID);
      SUBJA = SubjectFinder.findById(SUBJ_ALL);
      SUBJR = SubjectFinder.findById(SUBJ_ROOT);
    }
    catch (Exception e) {
      throw new RuntimeException(
        "unable to run tests without subjects: " + e.getMessage(), e
      );
    }
  } // static


  // Protected Class Methods //

  // Don't get a subject by bad id
  protected static void getSubjectByBadId(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id);
      Assert.fail("found bad subject '" + id + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // protected static void getSubjectByBadId(id)

  // Don't get a subject by bad id and type
  protected static void getSubjectByBadIdType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findById(id, type);
      Assert.fail("found bad subject '" + id + "'/'" + type + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // protected static void getSubjectByBadIdType(id, type)

  // Don't get a subject by bad identifier and type
  protected static void getSubjectByBadIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.fail("found bad subject '" + id + "'/'" + type + "': " + subj);
    }
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
  } // protected static void getSubjectByBadIdentifierType(id, type)

  // Get a subject by id
  // @return  A {@link Subject}
  protected static Subject getSubjectById(String id) { 
    try {
      Subject subj = SubjectFinder.findById(id);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject '" + id + "': " + e.getMessage());
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperRuntimeException();
  } // protected static Subject getSubjectById(id)

  // Get a subject by identifier
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdentifier(String id) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj name", subj.getName().equals(id));
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject '" + id + "': " + e.getMessage());
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperRuntimeException();
  } // protected static Subject getSubjectByIdentifier(id)

  // Get a subject by id and type
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findById(id, type);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      Assert.assertTrue(
        "subj type",subj.getType().getName().equals(type)
      );
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail(
        "failed to find subject '" + id + "'/'" + type + "': " + e.getMessage()
      );
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperRuntimeException();
  } // protected static Subject getSubjectByIdType(id, type)

  // Get a subject by identifier and type
  // @return  A {@link Subject}
  protected static Subject getSubjectByIdentifierType(String id, String type) { 
    try {
      Subject subj = SubjectFinder.findByIdentifier(id, type);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject", subj instanceof Subject
      );
      Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",subj.getType().getName().equals(type)
      );
      return subj;
    }
    catch (SubjectNotFoundException e) {
      Assert.fail(
        "failed to find subject '" + id + "'/'" + type + "': " + e.getMessage()
      );
    }
    catch (SubjectNotUniqueException eSNU) {
      T.e(eSNU);
    }
    throw new GrouperRuntimeException();
  } // protected static Subject getSubjectByIdentifierType(id, type)

} // class SubjectTestHelper


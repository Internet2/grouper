/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link GrouperSourceAdapter} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperSourceAdapter.java,v 1.2 2005-11-11 18:39:35 blair Exp $
 */
public class TestGrouperSourceAdapter extends TestCase {

  private static final  String ID   = "gsa";
  private static final  String NAME = "Grouper Source Adapter";
  private               Source sa;
  
  public TestGrouperSourceAdapter(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
    sa = new GrouperSourceAdapter(ID, NAME);
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests
 
  public void testAdapter() { 
    Assert.assertNotNull("sa !null", sa);
    Assert.assertTrue("sa.id", sa.getId().equals(ID));
    Assert.assertTrue("sa.name", sa.getName().equals(NAME));
  } // public void testAdapter()

  public void testAdapterTypes() {
    Object[]    types = sa.getSubjectTypes().toArray();
    Assert.assertTrue("1 type", types.length == 1);
    SubjectType type  = (SubjectType) types[0];
    Assert.assertNotNull("type !null", type);
    Assert.assertTrue(
      "type instanceof SubjectType",
      type instanceof SubjectType
    );
    Assert.assertTrue(
      "type == group", type.getName().equals("group")
    );
  } // public void testAdapterTypes()

  public void testAdapterBadSubject() {
    try { 
      Subject subj = sa.getSubject(Helper.BAD_SUBJ_ID);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testAdapterBadSubject()

  public void testAdapterBadSubjectByIdentifier() {
    try { 
      Subject subj = sa.getSubjectByIdentifier(Helper.BAD_SUBJ_ID);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
  } // public void testAdapterBadSubjectByIdentifer() 

}


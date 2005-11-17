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
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link GrouperNamingPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperNamingSTEM.java,v 1.7 2005-11-17 15:40:59 blair Exp $
 */
public class TestGrouperNamingSTEM extends TestCase {

  // Private Class Constants
  private static final Privilege PRIV = NamingPrivilege.STEM;


  // Private Class Variables
  Stem            edu;
  Stem            root;
  GrouperSession  s;
  Set             subjs = new HashSet();


  public TestGrouperNamingSTEM(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.getRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testDefaultPrivs() {
    PrivHelper.getPrivs(s, edu, s.getSubject(),       0, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0,  0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1,  0, false, false);
    // TODO PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
  } // public void testDefaultPrivs()

  public void testGrantPrivs() {
    PrivHelper.grantPriv( s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv( s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(  s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ0 , 1, false, true);
    PrivHelper.getPrivs(  s, edu, SubjectHelper.SUBJ1 , 0, false, false);
/* TODO 
    subjs.add(s.getSubject());
    subjs.add(SubjectHelper.SUBJ0);
    PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
*/
  } // public void testGrantPrivs()

  public void testRevokePrivs() {
    PrivHelper.grantPriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 1, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 1, false, true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    PrivHelper.revokePriv(s, edu, s.getSubject()      , PRIV);      
    PrivHelper.revokePriv(s, edu, SubjectHelper.SUBJ0 , PRIV);      
    PrivHelper.getPrivs(s, edu, s.getSubject()      , 0, true,  true);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ0 , 0, false, false);
    PrivHelper.getPrivs(s, edu, SubjectHelper.SUBJ1 , 0, false, false);
    // TODO PrivHelper.getSubjsWithPriv(edu, subjs, PRIV);
  } // public void testRevokePrivs()

}


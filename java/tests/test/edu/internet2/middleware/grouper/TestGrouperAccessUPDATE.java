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
 * Test {@link GrouperAccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperAccessUPDATE.java,v 1.7 2005-11-29 19:39:40 blair Exp $
 */
public class TestGrouperAccessUPDATE extends TestCase {

  // Private Class Constants
  private static final Privilege PRIV = AccessPrivilege.UPDATE;


  // Private Class Variables
  Stem            edu;
  Group           i2;
  Stem            root;
  GrouperSession  s;
  Set             groups  = new HashSet();
  Set             subjs   = new HashSet();


  public TestGrouperAccessUPDATE(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
    s       = SessionHelper.getRootSession();
    root    = StemHelper.findRootStem(s);
    edu     = StemHelper.addChildStem(root, "edu", "education");
    i2      = StemHelper.addChildGroup(edu, "i2", "internet2");
    groups  = new HashSet();
    subjs   = new HashSet();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testDefaultPrivs() {
    PrivHelper.getPrivs(
      s, i2, s.getSubject(),       0, true,  true, true, true, true, true
    );
    PrivHelper.getPrivs(
      s, i2, SubjectHelper.SUBJ0,  0, false, false, false, false, false, false
    );
    PrivHelper.getPrivs(
      s, i2, SubjectHelper.SUBJ1,  0, false, false, false, false, false, false
    );
    PrivHelper.getSubjsWithPriv(i2, subjs, PRIV);
    PrivHelper.subjInGroups(s, s.getSubject(), groups, PRIV);
  } // public void testDefaultPrivs()

  public void testGrantPrivs() {
    PrivHelper.grantPriv( s, i2,  s.getSubject()      , PRIV);      
    PrivHelper.grantPriv( s, i2,  SubjectHelper.SUBJ0 , PRIV);    
    PrivHelper.getPrivs(
      s, i2,  s.getSubject()      , 1, true,  true,   true,   true,   true,   true
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ0 , 1, false, false,  false,  false,  true,   false
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ1 , 0, false, false,  false,  false,  false,  false
    );
    subjs.add(s.getSubject());
    subjs.add(SubjectHelper.SUBJ0);
    PrivHelper.getSubjsWithPriv(i2, subjs, PRIV);
    groups.add(i2);
    PrivHelper.subjInGroups(s, s.getSubject(), groups, PRIV);
    PrivHelper.subjInGroups(s, SubjectHelper.SUBJ0, groups, PRIV);
  } // public void testGrantPrivs()

  public void testRevokePrivs() {
    PrivHelper.grantPriv(s, i2,  s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, i2,  SubjectHelper.SUBJ0 , PRIV);    
    PrivHelper.getPrivs(
      s, i2,  s.getSubject()      , 1, true,  true,   true,   true,   true,   true
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ0 , 1, false, false,  false,  false,  true,   false
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ1 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.revokePriv(s, i2,  s.getSubject()      , PRIV);      
    PrivHelper.revokePriv(s, i2,  SubjectHelper.SUBJ0 , PRIV);    
    PrivHelper.getPrivs(
      s, i2,  s.getSubject()      , 0, true,  true,   true,   true,   true,   true
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ0 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ1 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.getSubjsWithPriv(i2, subjs, PRIV);
    PrivHelper.subjInGroups(s, s.getSubject(), groups, PRIV);
  } // public void testRevokePrivs()

  public void testRevokeAllPrivs() {
    PrivHelper.grantPriv(s, i2,  s.getSubject()      , PRIV);      
    PrivHelper.grantPriv(s, i2,  SubjectHelper.SUBJ0 , PRIV);    
    PrivHelper.getPrivs(
      s, i2,  s.getSubject()      , 1, true,  true,   true,   true,   true,   true
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ0 , 1, false, false,  false,  false,  true,   false
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ1 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.revokePriv(s, i2, PRIV);
    PrivHelper.getPrivs(
      s, i2,  s.getSubject()      , 0, true,  true,   true,   true,   true,   true
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ0 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.getPrivs(
      s, i2,  SubjectHelper.SUBJ1 , 0, false, false,  false,  false,  false,  false
    );
    PrivHelper.getSubjsWithPriv(i2, subjs, PRIV);
    PrivHelper.subjInGroups(s, s.getSubject(), groups, PRIV);
  } // public void testRevokeAllPrivs()

}


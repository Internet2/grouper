/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: R.java,v 1.4 2006-03-24 19:38:12 blair Exp $
 */
class R {

/*
 * TODO
 * * Setup subjects?
 */

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(R.class);

  // Protected Class Variables
  protected GrouperSession  rs    = null;
  protected Stem            root  = null;
  protected Stem            edu   = null;
  protected Group           i2    = null;
  protected Group           ua    = null;
  protected Group           ub    = null;
  protected Group           uc    = null;
  protected Group           ud    = null;
  protected Group           ue    = null;
  protected Group           uw    = null;
  protected Subject         subj0 = null;
  protected Subject         subj1 = null;
  protected Subject         subj2 = null;
  protected Member          m0    = null;
  protected Member          m1    = null;
  protected Member          m2    = null;


  // Constructors
  private R() {
    super();
  } // R()


  // Protected Class Methods
  protected static R createOneStemAndTwoGroups() 
    throws  Exception
  {
    LOG.info("createOneStemAndTwoGroups");
    R r = new R();
    r.rs    = SessionHelper.getRootSession();
    r.root  = StemFinder.findRootStem(r.rs);
    r.edu   = r.root.addChildStem("edu" , "education" );
    r.i2    = r.edu.addChildGroup("i2"  , "internet2" );    
    r.uc    = r.edu.addChildGroup("uc"  , "u of c"    );    
    r._findSubjectsAndMembers();
    GrouperSession.waitForAllTx();
    return r;
  } // protected static R createOneStemTwoGroups()
  
  protected static R createOneStemAndFourGroups() 
    throws  Exception
  {
    LOG.info("createOneStemAndFourGroups");
    R r = new R();
    r.rs    = SessionHelper.getRootSession();
    r.root  = StemFinder.findRootStem(r.rs);
    r.edu   = r.root.addChildStem("edu" , "education" );
    r.i2    = r.edu.addChildGroup("i2"  , "internet2" );    
    r.ub    = r.edu.addChildGroup("ub"  , "u of b"    );    
    r.uc    = r.edu.addChildGroup("uc"  , "u of c"    );    
    r.uw    = r.edu.addChildGroup("uw"  , "u of w"    );
    r._findSubjectsAndMembers();
    GrouperSession.waitForAllTx();
    return r;
  } // protected static R createOneStemFourGroups()

  protected static R createOneStemAndEightGroups() 
    throws  Exception
  {
    LOG.info("createOneStemAndEightGroups");
    R r = new R();
    r.rs    = SessionHelper.getRootSession();
    r.root  = StemFinder.findRootStem(r.rs);
    r.edu   = r.root.addChildStem("edu" , "education" );
    r.i2    = r.edu.addChildGroup("i2"  , "internet2" );    
    r.ua    = r.edu.addChildGroup("ua"  , "u of a"    );    
    r.ub    = r.edu.addChildGroup("ub"  , "u of b"    );    
    r.uc    = r.edu.addChildGroup("uc"  , "u of c"    );    
    r.ud    = r.edu.addChildGroup("ud"  , "u of d"    );    
    r.ue    = r.edu.addChildGroup("ue"  , "u of e"    );    
    r.uw    = r.edu.addChildGroup("uw"  , "u of w"    );
    r._findSubjectsAndMembers();
    GrouperSession.waitForAllTx();
    return r;
  } // protected static R createOneStemEightGroups()
  
  // Private Instance Methods
  private void _findSubjectsAndMembers() 
    throws  Exception
  {
    this.subj0  = SubjectFinder.findById("test.subject.0");
    this.subj1  = SubjectFinder.findById("test.subject.1");
    this.subj2  = SubjectFinder.findById("test.subject.2");
    this.m0     = MemberFinder.findBySubject(this.rs, this.subj0);
    this.m1     = MemberFinder.findBySubject(this.rs, this.subj1);
    this.m2     = MemberFinder.findBySubject(this.rs, this.subj2);
  } // private void _findSubjectsAndMembers()

}


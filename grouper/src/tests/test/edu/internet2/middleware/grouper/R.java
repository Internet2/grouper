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
 * @version $Id: R.java,v 1.1 2006-03-21 18:36:45 blair Exp $
 */
class R {

/*
 * TODO
 * * Setup subjects?
 */

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(R.class);

  // Protected Class Variables
  protected Stem  root  = null;
  protected Stem  edu   = null;
  protected Group i2    = null;
  protected Group uc    = null;


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
    GrouperSession  s = SessionHelper.getRootSession();
    r.root  = StemFinder.findRootStem(s);
    r.edu   = r.root.addChildStem("edu" , "education" );
    r.i2    = r.edu.addChildGroup("i2"  , "internet2" );    
    r.uc    = r.edu.addChildGroup("uc"  , "uchicago"  );    
    s.waitForTx();
    s.stop();
    return r;
  } // protected static R createOneStemTwoGroups()
  
}


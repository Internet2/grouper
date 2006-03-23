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
 * @version $Id: T.java,v 1.1 2006-03-23 18:36:31 blair Exp $
 */
public class T {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(T.class);

  
  // Protected Class Methods
  protected static void getMembers(Group g, int exp) {
    LOG.debug("getMembers()");
    if (g.getMembers().size() == exp) {
      Assert.assertTrue(g.getName() + " members == " + exp, true);
    }
    else {
      Assert.fail(g.getName() + " members != " + exp + " [" + g.getMembers().size() + "]");
    }
  } // protected static void getMembers(g, exp)

}


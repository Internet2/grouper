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
 * @version $Id: T.java,v 1.4 2006-04-06 15:01:55 blair Exp $
 */
public class T {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(T.class);

  
  // Protected Class Methods
  protected static void amount(String msg, int exp, int got) { 
    LOG.debug("amount()");
    Assert.assertTrue(msg + " exp[" + exp + "] got[" + got + "]", exp == got);
  } // protected static void amount(msg, exp, got)

  protected static void getEffectiveMembers(Group g, int exp) {
    LOG.debug("getEffectiveMembers()");
    T.amount(g.getName() + " eff members", exp, g.getEffectiveMembers().size());
  } // protected static void getEffectiveMembers(g, exp)

  protected static void getImmediateMembers(Group g, int exp) {
    LOG.debug("getImmediateMembers()");
    T.amount(g.getName() + " imm members", exp, g.getImmediateMembers().size());
  } // protected static void getImmediateMembers(g, exp)

  protected static void getMembers(Group g, int exp) {
    LOG.debug("getMembers()");
    T.amount(g.getName() + " members", exp, g.getMembers().size());
  } // protected static void getMembers(g, exp)

}


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

package edu.internet2.middleware.grouper.stress;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.logging.*;


/** 
 * Add stems to the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: AddStems.java,v 1.1 2005-12-11 21:56:01 blair Exp $
 */
public class AddStems {

  /*
   * TODO
   * * Add a config file for stress testing since command line
   *   arguments don't seem to be working
   */

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(AddStems.class);


  // Private Class Variables
  private static GrouperSession s;
  private static int            max_depth = 5;


  // Main
  public static void main(String[] args) {
    _setUp();
    _grouperSetUp();
    _run();
    _grouperTearDown();
  } // public static void main(args)


  // Private Class Methods

  private static void _abort(String err) {
    LOG.fatal(err);
    if (s != null) { 
      _grouperTearDown();
    }
    throw new RuntimeException(err);
  } // private static void _abort(err)

  private static Set _addStems(int depth, int per, Set parents) {
    String msg = "depth " + depth + ": ";
    LOG.info(msg + "adding per parent " + per);
    Set stems = new LinkedHashSet();
    Iterator iter = parents.iterator();
    while (iter.hasNext()) {
      Stem parent = (Stem) iter.next();
      LOG.info(msg + "adding " + per + " child stems to " + parent.getName());
      for (int i=0; i <= per; i++) {
        try {
          String  extn    = "d/" + depth + "/stem/" + i;
          String  display = "depth " + depth + " stem " + i;
          Stem    child   = parent.addChildStem(extn, display);
          stems.add(child);
        }
        catch (Exception e) {
          _abort(e.getMessage());
        }
      }
    }
    LOG.info("added " + stems.size());
    return stems;
  } // private static Set _addStems(depth, per, parents)

  private static void _grouperSetUp() {
    try {
      RegistryReset.reset();
      LOG.info("registry reset");
      s = GrouperSession.start(
        SubjectFinder.findById("GrouperSystem", "application")
      );
      LOG.info("session: started " + s);
    }
    catch (Exception e) {
      _abort(e.getMessage());
    }
  } // private static void _grouperSetUp()

  private static void _grouperTearDown() {
    try {
      s.stop();
      LOG.info("session: stopped");
    }
    catch (SessionException eS) {
      s = null;
      _abort(eS.getMessage());
    }
  } // private static void _grouperSetUp()

  private static void _run() {
    LOG.info("adding stems: max depth=" + max_depth);
    Set   stems = new LinkedHashSet();
    Set   last  = new LinkedHashSet();
    Stem  root  = StemFinder.findRootStem(s);
    last.add(root);
    for (int i=1; i <= max_depth; i++) {
      last = _addStems(i, (i * 2), last);
      stems.addAll(last);
    }
    LOG.info("stems added: " + stems.size());
  } // private static void _run()

  private static void _setUp() {
    // FIXME Why don't you work?
    if (System.getProperty("depth") != null) {
      max_depth = Integer.parseInt(System.getProperty("depth"));
    }
  } // private static void _setUp()

}


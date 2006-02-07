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

package edu.internet2.middleware.grouper;


import  java.util.*;
import  org.apache.commons.logging.*;


/** 
 * Persistent Grouper Process.
 * <p />
 * @author  blair christensen.
 * @version $Id: grouperd.java,v 1.1 2006-02-07 20:46:44 blair Exp $    
 */
public class grouperd {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(grouperd.class);


  // Public Class Methods
  public static void main(String[] args) {
    if ( (args.length > 0) && (args[0].equals("stop")) ) {
      // TODO This doesn't work
      GrouperdTxRunnerThread.sendStopSignal();
    }
    else {
      // Create the threads.  Right now this is an extremely simplistic
      // model but at some point I'll probably move to pools, etc.
      GrouperdShutdownThread  shutdown  = new GrouperdShutdownThread();
      GrouperdTxRunnerThread  txRunner  = new GrouperdTxRunnerThread();

      // Register a shutdown hook that currently does absolutely nothing
      Runtime rt = Runtime.getRuntime();
      rt.addShutdownHook(shutdown);

      // And then start the appropriate threads
      txRunner.start();
    }
  } // public static void main(args)

}


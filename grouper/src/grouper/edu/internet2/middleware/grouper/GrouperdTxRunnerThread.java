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
 * {@link grouperd} thread for processing the transaction queue.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperdTxRunnerThread.java,v 1.1 2006-02-07 20:46:44 blair Exp $    
 */
public class GrouperdTxRunnerThread extends Thread {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(GrouperdTxRunnerThread.class);

  // Private Class Variables
  private static boolean  stop  = false;


  // Constructors
  protected GrouperdTxRunnerThread() {
    super();
    System.err.println(new Date().toString() + " TXRUNNER: NEW()");
  } // protected GrouperShutdownThread()



  // Protected Class Methods
  protected static void sendStopSignal() {
    stop = true; // TODO This doesn't work
  } // protected static void sendStopSignal()


  // Public Instance Methods

  public void run( ) {
    System.err.println(new Date().toString() + " TXRUNNER: RUN() - ");
    while (true) {
      if (stop == true) { break; }
      try {
        System.err.println(new Date().toString() + " TXRUNNER: RUN() SLEEP - ");
        Thread.sleep( (long) (Math.random() * 1000) );
      } catch (InterruptedException e) {
        System.err.println(new Date().toString() + " TXRUNNER: RUN() RETURN - ");
      }
    }
  } // public void run()

}


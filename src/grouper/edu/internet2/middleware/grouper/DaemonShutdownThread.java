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
 * Thread run when shutting down {@link grouperd}.
 * <p />
 * @author  blair christensen.
 * @version $Id: DaemonShutdownThread.java,v 1.2 2006-02-14 18:34:29 blair Exp $    
 */
public class DaemonShutdownThread extends Thread {

  // Private Class Constants
  private static final DaemonLog  DL  = new DaemonLog();

  // Private Instance Variables
  private GrouperDaemon gd  = null;


  // Constructors
  protected DaemonShutdownThread(GrouperDaemon gd) {
    super();
    this.gd = gd;
  } // protected GrouperShutdownThread()


  // Public Instance Methods
  public void run( ) {
    DL.shutdownThread();
  } // public void run()

}


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
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/** 
 * {@link grouperd} thread for processing the transaction queue.
 * <p />
 * @author  blair christensen.
 * @version $Id: DaemonTxRunnerThread.java,v 1.3 2006-02-15 23:06:49 blair Exp $    
 */
public class DaemonTxRunnerThread extends Thread {

  // Private Class Constants
  private static final DaemonLog  DL  = new DaemonLog();

  // Private Instance Variables
  private GrouperDaemon gd  = null;


  // Constructors
  protected DaemonTxRunnerThread(GrouperDaemon gd) {
    super();
    this.gd = gd;
  } // protected GrouperShutdownThread()


  // Public Instance Methods

  public void run( ) {
    while (true) {
      if (this.gd.isStopped()) {
        break; 
      }
      Set queue = TxQueueFinder.findByStatus("wait");
      if (queue.size() > 0) {
        DL.itemsInQueue(queue); 
        Iterator iter = queue.iterator();
        while (iter.hasNext()) {
          TxQueue tx = (TxQueue) iter.next();
          if (tx.apply(this.gd)) {
            DL.appliedTx(tx);
          } 
          else {
            DL.failedToApplyTx(tx);
            if (!tx.setFailed(this.gd)) {
              DL.failedToSetFailed(tx);
            }
          }
        }
      } 
/*
          TxQueue tx  = (TxQueue) iter.next();
          if (tx.getClass() == TxStop.class) {
            try {
              HibernateHelper.delete(tx);
              DL.deleteTx(tx);
              this.gd.stopDaemon();
            }
            catch (HibernateException eH) {
              String  msg = eH.getMessage();
              DL.failToDeleteTx(tx, msg);
              break;
            }
          }
*/
      try {
        Thread.sleep( (long) (Math.random() * 1000) );
      } catch (InterruptedException e) {
        // Nothing
      }
    }
  } // public void run()

}


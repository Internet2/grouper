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


import  java.io.Serializable;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Transaction Queue <b>STOP</b> command.
 * @author blair christensen.
 *     
*/
class TxStop extends TxQueue implements Serializable, Tx {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TxStop.class);

  
  // Constructors

  // For Hibernate
  public TxStop() { 
    super();
  } // TxStop()


  // Public Instance Methods
  public void apply(GrouperDaemon gd) 
    throws  TxException
  {
    try {
      HibernateHelper.delete(this);
      gd.getLog().deleteTx(this);
      try {
        // Sleep for a moment
        Thread.sleep(500); 
      } catch (InterruptedException e) {
        // Nothing
      }
      gd.stopDaemon();
    }
    catch (HibernateException eH) {
      String msg = "unable to stop GrouperDaemon: " + eH.getMessage();
      gd.getLog().failToDeleteTx(this, msg);
      throw new TxException(msg);
    }
  } // public void apply()


  // Protected Instance Methods
  protected void delete(GrouperDaemon gd) {
    // Handled in apply()
  } // protected void delete(gd)

}


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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;


/** 
 * {@link GrouperDaemon} logging.
 * <p />
 * @author  blair christensen.
 * @version $Id: DaemonLog.java,v 1.2 2006-02-15 23:06:49 blair Exp $
 *     
*/
class DaemonLog implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(DaemonLog.class);

  // daemon errors
  private static final String ERR_GD_STOP   = "failed to signal that GrouperDaemon should stop: ";
  private static final String ERR_HS_STOP   = "failed to stop HSQLDB server: ";
  private static final String ERR_TXQ_ATX   = "failed to apply tx: ";
  private static final String ERR_TXQ_DTX   = "failed to delete tx: ";
  private static final String ERR_TXQ_SF    = "failed to mark failed tx: ";

  // daemon messages
  private static final String MSG_GD_STOP   = "stopping GrouperDaemon";
  private static final String MSG_GD_START  = "starting GrouperDaemon";
  private static final String MSG_HS_CFG    = "HSQLDB server config: ";
  private static final String MSG_HS_STOP   = "stopping embedded HSQLDB server";
  private static final String MSG_HS_START  = "starting embedded HSQLDB server";
  private static final String MSG_ST        = "finalizing shutdown";
  private static final String MSG_TXQ_ATX   = "applied tx: ";
  private static final String MSG_TXQ_DTX   = "deleted tx: ";
  private static final String MSG_TXQ_ITEMS = "items in tx queue: ";


  // Constructors

  protected DaemonLog() {
    super();
  } // protected DaemonLog()


  // Protected Instance Methods

  protected void appliedTx(TxQueue tx) {
    LOG.info(MSG_TXQ_ATX + tx);
  } // protected void appliedTx(tx)

  protected void deleteTx(TxQueue tx) {
    LOG.info(MSG_TXQ_DTX + tx);
  } // protected void deleteTx(tx)

  protected void error(String msg) {
    LOG.error(msg);
  } // protected void err(msg)

  protected void failedToApplyTx(TxQueue tx) {
    LOG.error(ERR_TXQ_ATX + tx);
  } // protected void failedToApplyTx(tx)

  protected void failedToSetFailed(TxQueue tx) {
    LOG.error(ERR_TXQ_SF + tx);
  } // protected void failedToSetFailed(tx)

  protected void failToDeleteTx(TxQueue tx, String msg)  {
    LOG.fatal(ERR_TXQ_DTX + tx + ": " + msg);
  } // protected void failToDeleteTx(tx, msg)

  protected void failToStopGrouperDaemon(String msg) {
    LOG.fatal(ERR_GD_STOP + msg);
  } // protected void failToStopGrouperDaemon(msg)

  protected void failToStopHsqldbServer(String msg) {
    LOG.fatal(ERR_HS_STOP + msg);
  } // protected void failToStopHsqldbServer(msg)

  protected void hsqldbConfig(String key, String val) {
    LOG.info(MSG_HS_CFG + key + "=" + val);
  } // protected void hsqldbConfig(key, val)

  protected void itemsInQueue(Set queue) {
    LOG.info(MSG_TXQ_ITEMS + queue.size());
  } // protected void itemsInQueue(queue)

  protected void shutdownThread() {
    LOG.info(MSG_ST);
  } // protected void shutdownThread();
  
  protected void stopGrouperDaemon() {
    LOG.info(MSG_GD_STOP);
  } // protected void stopGrouperDaemon()

  protected void startGrouperDaemon() {
    LOG.info(MSG_GD_START);
  } // protected void startGrouperDaemon()

  protected void stopHsqldbServer() {
    LOG.info(MSG_HS_STOP);
  } // protected void stopHsqldbServer()

  protected void startHsqldbServer() {
    LOG.info(MSG_HS_START);
  } // protected void startHsqldbServer()

}


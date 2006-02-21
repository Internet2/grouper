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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Transaction Queue Query Class.
 * @author blair christensen.
 *     
*/
class TxQueueFinder implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(TxQueueFinder.class);


  // Protected Class Methods
  protected static Set findAll() {
    Set results = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from TxQueue");
      qry.setCacheable(GrouperConfig.QRY_TQF_FA);
      qry.setCacheRegion(GrouperConfig.QCR_TQF_FA);
      results.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
    return results;
  } // protected static findAll()

  // How many transactions in "wait" status does this session have?
  protected static Set findBySession(String session) {
    Set results = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      if (hs.isConnected()) { 
        Query   qry = hs.createQuery(
          "from TxQueue as q where            "
          + "         q.sessionId = :session  "
          + "and      q.status    = :status   "
          + "order by q.queueTime asc"
        );
        qry.setCacheable(   GrouperConfig.QRY_TQF_FBGS  );
        qry.setCacheRegion( GrouperConfig.QCR_TQF_FBGS  );
        qry.setString("session" , session );
        qry.setString("status"  , "wait"  );
        results.addAll( qry.list() );
      }
      else {
        EL.error("NOT CONNECTED!"); 
      }
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      LOG.error(msg);
    }
    return results;
  } // protected static findByStatus(status)
  
  protected static Set findByStatus(String status) {
    Set results = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      if (hs.isConnected()) { 
        Query   qry = hs.createQuery(
          "from TxQueue as q where q.status = :status order by q.queueTime asc"
        );
        qry.setCacheable(GrouperConfig.QRY_TQF_FBS);
        qry.setCacheRegion(GrouperConfig.QCR_TQF_FBS);
        qry.setString("status", status);
        results.addAll( qry.list() );
      }
      else {
        EL.error("NOT CONNECTED!");
      }
      hs.close();
    }
    catch (HibernateException eH) {
      LOG.error(eH.getMessage());
    }
    return results;
  } // protected static findByStatus(status)
  
}


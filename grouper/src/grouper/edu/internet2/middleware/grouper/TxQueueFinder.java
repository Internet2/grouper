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
  private static final Log LOG = LogFactory.getLog(TxQueueFinder.class);


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

  protected static Set findByStatus(String status) {
    Set results = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      if (hs.isConnected()) { 
        Query   qry = hs.createQuery(
          "from TxQueue as q where q.status = :status"
        );
        qry.setCacheable(GrouperConfig.QRY_TQF_FBS);
        qry.setCacheRegion(GrouperConfig.QCR_TQF_FBS);
        qry.setString("status", status);
        results.addAll( qry.list() );
      }
      else {
        System.err.println("NOT CONNECTED!");
      }
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      LOG.error(msg);
    }
    return results;
  } // protected static findByStatus(status)
  
}


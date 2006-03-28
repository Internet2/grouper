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
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: FactorFinder.java,v 1.5 2006-03-28 19:06:20 blair Exp $
 */
class FactorFinder implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(FactorFinder.class);


  // Protected Class Methods
  protected static Set findIsFactor(GrouperSession s, TxQueue tx) 
    throws  HibernateException
  {
    Owner   o       = tx.getOwner();
    Set     results = new LinkedHashSet();
    Session hs      = HibernateHelper.getSession();
    Query   qry     = hs.createQuery(
      "from Factor as f where (f.left = :left or f.right = :right)"
    );
    qry.setCacheable(false); // FIXME ???
    //qry.setCacheRegion(GrouperConfig.QCR_FF_FIF);
    qry.setParameter( "left"  , o );
    qry.setParameter( "right" , o );
    Iterator iter = qry.iterate();
    while (iter.hasNext()) {
      Factor f = (Factor) iter.next();
      results.add(f);
    }
    return results;
  } // protected static Set findIsFactor(s, tx)  

}


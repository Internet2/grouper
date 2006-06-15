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
import  net.sf.hibernate.type.*;

/**
 * @author  blair christensen.
 * @version $Id: CompositeFinder.java,v 1.4 2006-06-15 00:07:02 blair Exp $
 * @since   1.0
 */
class CompositeFinder {

  // PROTECTED CLASS METHODS //
  // @since 1.0
  protected static Set isFactor(Owner o) {
    Set composites = new LinkedHashSet();
    try {
      Session   hs  = HibernateHelper.getSession();
      Query     qry = hs.createQuery(
          "from Composite as c where (" 
        + " c.left = :left or c.right = :right "
        + ")"
      );
      qry.setCacheable(   GrouperConfig.QRY_CF_IF ); 
      qry.setCacheRegion( GrouperConfig.QCR_CF_IF );
      qry.setParameter( "left"  , o );
      qry.setParameter( "right" , o );
      Iterator iter = qry.list().iterator();
      while (iter.hasNext()) {
        Composite c = (Composite) iter.next();
        c.setSession(o.getSession());
        composites.add(c);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(CompositeFinder.class, E.COMPF_ISFACTOR + eH.getMessage());
    }
    return composites;
  } // protected static Set isFactor(o)

  // @since 1.0
  protected static Composite isOwner(Owner o) 
    throws  CompositeNotFoundException
  {
    try {
      Session   hs  = HibernateHelper.getSession();
      Query     qry = hs.createQuery(
        "from Composite as c where c.owner = :owner"
      );
      qry.setCacheable(   GrouperConfig.QRY_CF_IO ); 
      qry.setCacheRegion( GrouperConfig.QCR_CF_IO );
      qry.setParameter( "owner" , o );
      Composite c   = (Composite) qry.uniqueResult();
      hs.close();
      if (c == null) {
        throw new CompositeNotFoundException(E.COMP_NOTOWNER);
      }
      c.setSession(o.getSession());
      return c;
    }
    catch (HibernateException eH) {
      throw new CompositeNotFoundException(eH.getMessage(), eH);
    }
  } // protected static Composite isOwner(o)

}


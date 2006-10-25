/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/reconcile/Reconciler.java,v 1.1 2006-10-25 00:09:40 ddonn Exp $

Copyright (c) 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

	@author ddonn
*/
package edu.internet2.middleware.signet.reconcile;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;


/**
 * Class to provide reconciling of proxies and grantables
 */
public class Reconciler
{
	// Local copy of the Signet that created me
	protected Signet		signet;

	// The Persisted Store Manager
	protected HibernateDB	persistMgr;


	public Reconciler(Signet signetInstance, HibernateDB persistMgr)
	{
		this.signet = signetInstance;
		this.persistMgr = persistMgr;
	}


  /**
   * Evaluate the conditions and pre-requisites associated with all Grantable
   * entities (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of those entities.
   * <p />
   * Please note that this method, unlike most other methods that modify
   * Signet objects, will  have its changes persisted without having to call the
   * <code>save()</code> method on each of the modified Grantable
   * entities. 
   * 
   * @return a <code>Set</code> of all Grantable entities whose
   * <code>Status</code> values were changed by this method.
   */
//  public Set reconcile()
//  {
//    Date now = new Date();
//    return this.reconcile(now);
//  }
 
  /**
   * Evaluate the conditions and pre-requisites associated with all Grantable
   * entities (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of those entities.
   * <p />
   * Please note that this method, unlike most other methods that modify
   * Signet objects, will  have its changes persisted without having to call the
   * <code>save()</code> method on each of the modified Grantable
   * entities. 
   * 
   * @param date the <code>Date</code> value to use as the current date and time
   * when evaluating effectiveDate and expirationDate.
   * 
   * @return a <code>Set</code> of all Grantable entities whose
   * <code>Status</code> values were changed by this method.
   */
  public Set reconcile(Date date)
  {
    // We don't have to evaluate every single Grantable. We can exclude these:
    //
    //  a) Those whose Status values are INACTIVE. We won't be bringing anything
    //     back from the dead.
    //
    //  b) Those whose effective-dates are later than the current Date.
   
    List  assignmentResultList;
    try
    {
      Query assignmentQuery = persistMgr.createQuery(
    		  "from edu.internet2.middleware.signet.AssignmentImpl" 
                 + " as assignment" + " where status != :inactiveStatus"  
                 + " and effectiveDate <= :currentDate"); 

      assignmentQuery.setParameter("inactiveStatus", Status.INACTIVE); 
      assignmentQuery.setParameter("currentDate", date); 

      assignmentResultList = assignmentQuery.list();
    }
    catch (HibernateException e)
    {
      throw new SignetRuntimeException(e);
    }
    
    Set changedGrantables = reconcileGrantables(assignmentResultList, date);
   
    List  proxyResultList;
    try
    {
      Query proxyQuery = persistMgr.createQuery(
    		  "from edu.internet2.middleware.signet.ProxyImpl" 
                 + " as proxy" + " where status != :inactiveStatus"  
                 + " and effectiveDate <= :currentDate"); 

      proxyQuery.setParameter("inactiveStatus", Status.INACTIVE); 
      proxyQuery.setParameter("currentDate", date); 

      proxyResultList = proxyQuery.list();
    }
    catch (HibernateException e)
    {
      throw new SignetRuntimeException(e);
    }

    changedGrantables.addAll(reconcileGrantables(proxyResultList, date));
    
    return (changedGrantables);
  }


	public Set reconcileGrantables(Collection grantables, Date date)
	{
		HashSet retval = new HashSet();

		if (null != grantables)
		{
			for (Iterator grantablesIterator = grantables.iterator(); grantablesIterator.hasNext();)
			{
				Grantable grantable = (Grantable)(grantablesIterator.next());
				// We got these Grantables from a query, so they may not have their Signet members set yet.
//TODO Is this really necessary???
//				((GrantableImpl)grantable).setSignet(signet);

				if (grantable.evaluate(date))
				{
					grantable.save();
					retval.add(grantable);
				}
			}
		}

		return (retval);
	}


}

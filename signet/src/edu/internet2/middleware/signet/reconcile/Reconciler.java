/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/reconcile/Reconciler.java,v 1.5 2007-05-06 07:13:15 ddonn Exp $

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
import java.util.Vector;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.GrantableImpl;
import edu.internet2.middleware.signet.History;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;


/**
 * Class to provide reconciling of proxies and grantables
 */
public class Reconciler
{
	public static final String Qry_assign_P1 = "inactiveStatus";
	public static final String Qry_assign_P2 = "currentDate";
	public static final String Qry_assignment =
			"from " +
			AssignmentImpl.class.getName() +
			" as assignment" +
			" where status != :" + Qry_assign_P1 +
			" and effectiveDate <= :" + Qry_assign_P2;

	public static final String Qry_proxy_P1 = "inactiveStatus";
	public static final String Qry_proxy_P2 = "currentDate";
	public static final String Qry_proxy =
			"from " +
			ProxyImpl.class.getName() +
			" as proxy " +
			" where status != :" + Qry_proxy_P1 +
			" and effectiveDate <= :" + Qry_proxy_P2; 

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
	Session hs = persistMgr.openSession();

    try
    {
      Query assignmentQuery = persistMgr.createQuery(hs, Qry_assignment);
      assignmentQuery.setParameter(Qry_assign_P1, Status.INACTIVE); 
      assignmentQuery.setParameter(Qry_assign_P2, date); 

      assignmentResultList = assignmentQuery.list();
    }
    catch (HibernateException e)
    {
    	hs.close();
      throw new SignetRuntimeException(e);
    }
    
    Set changedGrantables = reconcileGrantables(assignmentResultList, date);
   
    List  proxyResultList;
    try
    {
      Query proxyQuery = persistMgr.createQuery(hs, Qry_proxy);
      proxyQuery.setParameter(Qry_proxy_P1, Status.INACTIVE); 
      proxyQuery.setParameter(Qry_proxy_P2, date); 

      proxyResultList = proxyQuery.list();
    }
    catch (HibernateException e)
    {
    	hs.close();
      throw new SignetRuntimeException(e);
    }

    changedGrantables.addAll(reconcileGrantables(proxyResultList, date));

	persistMgr.closeSession(hs);

    return (changedGrantables);
  }


	/**
	 * Evaluate the collection of Grantables for reconciliation with supplied Date.
	 * Each reconciled Grantable creates its own History record and then both
	 * get persisted.
	 * @param grantables The collection of Grantables to reconcile
	 * @param date The Date to reconcile against
	 * @return A HashSet of reconciled (only) Grantables
	 */
	public Set reconcileGrantables(Collection grantables, Date date)
	{
		List grantList = new Vector();

		if (null != grantables)
		{
			for (Iterator grantablesIterator = grantables.iterator(); grantablesIterator.hasNext();)
			{
				Grantable grantable = (Grantable)(grantablesIterator.next());
				// We got these Grantables from a query, so they may not have their Signet members set yet.
				((GrantableImpl)grantable).setSignet(signet);

				if (grantable.evaluate(date))
				{
					History histRecord = grantable.createHistoryRecord();
					grantable.addHistoryRecord(histRecord);
//					grantable.save();
					grantList.add(grantable);
				}
			}

			if (0 < grantList.size())
			{
				Session hs = persistMgr.openSession();
				Transaction tx = hs.beginTransaction();
				persistMgr.save(hs, grantList);
				tx.commit();
				persistMgr.closeSession(hs);
			}
		}

		return (new HashSet(grantList));
	}


}

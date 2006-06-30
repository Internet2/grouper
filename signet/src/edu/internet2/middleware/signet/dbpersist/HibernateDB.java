/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/dbpersist/HibernateDB.java,v 1.1 2006-06-30 02:04:41 ddonn Exp $

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
package edu.internet2.middleware.signet.dbpersist;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ChoiceSetImpl;
import edu.internet2.middleware.signet.EntityImpl;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.PrivilegedSubjectImpl;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeAdapterImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.TreeNodeRelationship;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

/** Singleton wrapper for the Hibernate DB persistence layer */
public class HibernateDB
{
	protected static HibernateDB	hInstance;
	protected static Signet			signet;

	protected Log log = LogFactory.getLog(HibernateDB.class);

	protected Configuration		cfg;
	protected Session			session;
	protected Transaction		tx;
	protected int				xactNestingLevel = 0;
	protected HousekeepingInterceptor interceptor;


	////////////////////////////////////
	// static methods
	////////////////////////////////////

	public static HibernateDB getInstance()
	{
		if (null == hInstance)
			hInstance = new HibernateDB();
		return (hInstance);
	}


	public static void setSignet(Signet _signet) { signet = _signet; }

	public static Signet getSignet() { return (signet); }


	///////////////////////////////////
	// class methods
	///////////////////////////////////

	/**
	 * private because it's a singleton. Use getInstance().
	 */
	private HibernateDB()
	{
		super();

		cfg = new Configuration();
		try
		{
			// Read the "hibernate.cfg.xml" file. It is expected to be in a root directory of the classpath
			cfg.configure();

			String dbAccount = cfg.getProperty("hibernate.connection.username"); //$NON-NLS-1$
			interceptor = new HousekeepingInterceptor(dbAccount);
			cfg.setInterceptor(interceptor);
			SessionFactory sessionFactory = cfg.buildSessionFactory();
			interceptor.setSessionFactory(sessionFactory);

			session = sessionFactory.openSession();
			interceptor.setConnection(session.connection());
		}
		catch (HibernateException he)
		{
			log.error("HibernateDB.HibernateDB: hibernate error");
			log.error(he.toString());
			throw new SignetRuntimeException(he);
		}
	}


	public Configuration getConfiguration()
	{
		return (cfg);
	}

	public Session getSession()
	{
		return (session);
	}


	/** wrapper method for session */
	public Object load(Class loadClass, String id) throws ObjectNotFoundException
	{
		Object retval = null;
		try { retval = session.load(loadClass, id); }
		catch (net.sf.hibernate.ObjectNotFoundException onfe)
		{
			throw new ObjectNotFoundException(onfe);
		}
		catch (HibernateException he)
		{
			throw new ObjectNotFoundException(he);
		}
		return (retval);
	}

	/** wrapper method for session */
	public Object load(Class loadClass, Integer id) throws ObjectNotFoundException
	{
		Object retval = null;
		try { retval = session.load(loadClass, id); }
		catch (net.sf.hibernate.ObjectNotFoundException onfe)
		{
			throw new ObjectNotFoundException(onfe);
		}
		catch (HibernateException he)
		{
			throw new SignetRuntimeException(he);
		}
		return (retval);
	}


	/** wrapper method for session */
	public Query createQuery(String queryString) throws SignetRuntimeException
	{
		Query retval = null;
		try { retval = session.createQuery(queryString); }
		catch (HibernateException he)
		{
			throw new SignetRuntimeException(he);
		}
		return (retval);
	}


	/** wrapper method for session */
	public List find(String searchString) throws SignetRuntimeException
	{
		List retval = null;
		try { retval = session.find(searchString); }
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		return (retval);
	}

	/**
	 * Begins a Signet transaction.
	 *  
	 */
	public final void beginTransaction()
	{
		if (xactNestingLevel == 0)
		{
			try
			{
				tx = session.beginTransaction();
			}
			catch (HibernateException e)
			{
				throw new SignetRuntimeException(e);
			}
		}
		xactNestingLevel++;
	}

	/**
	 * commit a Signet database transaction.
	 */
	public void commit()
	{
		if (tx == null)
		{
			throw new IllegalStateException(ResLoaderApp.getString("Signet.msg.exc.sigTrans")); //$NON-NLS-1$
		}
		if (xactNestingLevel < 1)
		{
			throw new SignetRuntimeException(ResLoaderApp.getString("Signet.msg.exc.transNest_1") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.transNest_2")); //$NON-NLS-1$
		}
		xactNestingLevel--;
		if (xactNestingLevel == 0)
		{
			try
			{
				tx.commit();
				tx = null;
			}
			catch (HibernateException e)
			{
				throw new SignetRuntimeException(e);
			}
		}
	}

	/**
	 * Closes a Signet session.
	 */
	public void close()
	{
		try
		{
			session.close();
			session = null;
			// We leave the SessionFactory open, in case the application
			// wants to create a subsequent Signet object.
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}

	/**
	 * Saves a new Signet object, and any Signet objects it refers to.
	 * 
	 * @param o
	 */
	public final void save(Object o)
	{
		try
		{
			session.save(o);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}


	/**
	 * Gets a single Tree by ID.
	 * 
	 * @param id
	 * @return the specified Tree
	 * @throws ObjectNotFoundException
	 */
	public final Tree getTree(String id) throws ObjectNotFoundException
	{
		TreeImpl tree = (TreeImpl)(load(TreeImpl.class, id));
		tree.setSignet(signet);
		return tree;
	}


	/**
	 * Gets all of the Subsystems in the Signet database.
	 * 
	 * @return an unmodifiable Set of all of the {@link Subsystem}s in the Signet
	 *         database. Never returns null: in the case of zero {@link Subsystem}
	 *         s, this method will return an empty Set.
	 */
	public Set getSubsystems()
	{
		List resultList = find("from edu.internet2.middleware.signet.SubsystemImpl as subsystem");

		Set resultSet = new HashSet(resultList);
		for (Iterator i = resultSet.iterator(); i.hasNext();)
			((EntityImpl)i.next()).setSignet(signet);

		return UnmodifiableSet.decorate(resultSet);
	}


	/**
	 * Gets a single Subsystem by ID.
	 * 
	 * @param id
	 * @return the specified Subsystem
	 */
	public Subsystem getSubsystem(String id) throws ObjectNotFoundException
	{
		SubsystemImpl subsystemImpl;
		if (id == null)
		{
			throw new IllegalArgumentException(ResLoaderApp.getString("Signet.msg.exc.subsysIds")); //$NON-NLS-1$
		}
		if (id.length() == 0)
		{
			throw new IllegalArgumentException(ResLoaderApp.getString("Signet.msg.exc.subsysIdIs0")); //$NON-NLS-1$
		}
		try
		{
			subsystemImpl = (SubsystemImpl)(load(SubsystemImpl.class, id));
		}
		catch (ObjectNotFoundException onfe)
		{
			Object[] msgData = new Object[] { id };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.subsysNotFound")); //$NON-NLS-1$
			throw new ObjectNotFoundException(msg.format(msgData), onfe);
		}
		subsystemImpl.setSignet(signet);
		return subsystemImpl;
	}


	public Set findDuplicates(Assignment assignment)
	{
		Query query;
		List resultList;
		try
		{
			query = createQuery("from edu.internet2.middleware.signet.AssignmentImpl" + " as assignment"
					+ " where granteeKey  = :granteeKey" + " and functionKey   = :functionKey" + " and scopeID       = :scopeId"
					+ " and scopeNodeID   = :scopeNodeId" + " and assignmentID != :assignmentId");
			query.setParameter("granteeKey", assignment.getGrantee().getId(), Hibernate.INTEGER);
			query.setParameter("functionKey", ((FunctionImpl)(assignment.getFunction())).getKey(), Hibernate.INTEGER);
			query.setString("scopeId", assignment.getScope().getTree().getId());
			query.setString("scopeNodeId", assignment.getScope().getId());
			query.setParameter("assignmentId", assignment.getId(), Hibernate.INTEGER);
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		Set resultSet = new HashSet(resultList);
		Set editedSet = new HashSet();
		editedSet.addAll(resultSet);
		Iterator resultSetIterator = resultSet.iterator();
		while (resultSetIterator.hasNext())
		{
			Assignment matchedAssignment = (Assignment)(resultSetIterator.next());
			if (matchedAssignment.getStatus() == Status.INACTIVE)
			{
				// We don't consider inactive Assignments when hunting for duplicates.
				editedSet.remove(matchedAssignment);
			}
			else
			{
				((AssignmentImpl)matchedAssignment).setSignet(signet);
				// Now, let's trim this set of Assignments down further, keeping only
				// those Assignments whose LimitValues actually match.
				if (!(assignment.getLimitValues().equals(matchedAssignment.getLimitValues())))
				{
					editedSet.remove(matchedAssignment);
				}
			}
		}
		return editedSet;
	}


	public Set findDuplicates(Proxy proxy)
	{
		Set candidates = proxy.getGrantee().getProxiesReceived();
		Set duplicates = new HashSet();
		Iterator candidatesIterator = candidates.iterator();
		while (candidatesIterator.hasNext())
		{
			Proxy candidate = (Proxy)(candidatesIterator.next());
			boolean subsystemsMatch = false;
			if (candidate.getSubsystem() == null)
			{
				if (proxy.getSubsystem() == null)
				{
					subsystemsMatch = true;
				}
			}
			else
			{
				if (candidate.getSubsystem().equals(proxy.getSubsystem()))
				{
					subsystemsMatch = true;
				}
			}
			if (subsystemsMatch && !(candidate.getStatus().equals(Status.INACTIVE))
					&& candidate.getGrantor().equals(proxy.getGrantor()) && (candidate.getId() != null)
					&& !(candidate.getId().equals(proxy.getId())))
			{
				duplicates.add(candidate);
			}
		}
		return duplicates;
	}


	// I really want to do away with this method, having the
	// Tree pick up its parent-child relationships via Hibernate
	// object-mapping. I just haven't figured out how to do that yet.
	public Set getParents(TreeNode childNode)
	{
		Query query;
		List resultList;
		Tree tree = childNode.getTree();
		try
		{
			query = createQuery("from edu.internet2.middleware.signet.TreeNodeRelationship" +
					" as treeNodeRelationship" +
					" where treeID = :treeId" +
					" and nodeID = :childNodeId");
			query.setString("treeId", tree.getId());
			query.setString("childNodeId", childNode.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		Set resultSet = new HashSet(resultList);
		Set parents = new HashSet();
		Iterator resultSetIterator = resultSet.iterator();
		while (resultSetIterator.hasNext())
		{
			TreeNodeRelationship tnr = (TreeNodeRelationship)(resultSetIterator.next());
			parents.add(tree.getNode(tnr.getParentNodeId()));
		}
		return parents;
	}


	// I really want to do away with this method, having the
	// Tree pick up its parent-child relationships via Hibernate
	// object-mapping. I just haven't figured out how to do that yet.
	public Set getChildren(TreeNode parentNode)
	{
		Query query;
		List resultList;
		Tree tree = parentNode.getTree();
		try
		{
			query = createQuery("from edu.internet2.middleware.signet.TreeNodeRelationship" + " as treeNodeRelationship"
					+ " where treeID = :treeId" + " and parentNodeID = :parentNodeId");
			query.setString("treeId", tree.getId());
			query.setString("parentNodeId", parentNode.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		Set resultSet = new HashSet(resultList);
		Set children = new HashSet();
		Iterator resultSetIterator = resultSet.iterator();
		while (resultSetIterator.hasNext())
		{
			TreeNodeRelationship tnr = (TreeNodeRelationship)(resultSetIterator.next());
			children.add(tree.getNode(tnr.getChildNodeId()));
		}
		return children;
	}


	// I really want to do away with this method, having the Subsystem
	// pick up its associated Limits via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	public Map getLimitsBySubsystem(Subsystem subsystem)
	{
		List resultList;
		try
		{
			Query query = createQuery("from edu.internet2.middleware.signet.LimitImpl" +
					" as limit where subsystemID = :id");
			query.setString("id", subsystem.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		Map limits = new HashMap(resultList.size());
		Iterator resultListIterator = resultList.iterator();
		while (resultListIterator.hasNext())
		{
			Limit limit = (Limit)(resultListIterator.next());
			((LimitImpl)limit).setSignet(signet);
			limits.put(limit.getId(), limit);
		}
		return limits;
	}


	// I really want to do away with this method, having the Subsystem
	// pick up its associated Permissions via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	public Map getPermissionsBySubsystem(Subsystem subsystem)
	{
		Query query;
		List resultList;
		try
		{
			query = createQuery("from edu.internet2.middleware.signet.PermissionImpl"
					+ " as limit where subsystemID = :id");
			query.setString("id", subsystem.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		Map permissions = new HashMap(resultList.size());
		Iterator resultListIterator = resultList.iterator();
		while (resultListIterator.hasNext())
		{
			Permission permission = (Permission)(resultListIterator.next());
			((PermissionImpl)permission).setSignet(signet);
			permissions.put(permission.getId(), permission);
		}
		return permissions;
	}


	public PrivilegedSubject fetchPrivilegedSubject(String subjectTypeId, String subjectId) throws ObjectNotFoundException
	{
		PrivilegedSubjectImpl pSubject;
		Query query;
		List resultList;
		try
		{
			query = createQuery("from edu.internet2.middleware.signet.PrivilegedSubjectImpl" + " as privilegedSubject"
					+ " where subjectID = :id" + " and subjectTypeID = :type");
			query.setString("id", subjectId);
			query.setString("type", subjectTypeId);
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		if (resultList.size() > 1)
		{
			Object[] msgData = new Object[] { new Integer(resultList.size()), subjectId, subjectTypeId };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.multiPrivSubj_1") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.multiPrivSubj_2") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.multiPrivSubj_3")); //$NON-NLS-1$
			throw new SignetRuntimeException(msg.format(msgData));
		}
		if (resultList.size() == 0)
		{
			Object[] msgData = new Object[] { subjectTypeId, subjectId };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.privSubjNotFound")); //$NON-NLS-1$
			throw new ObjectNotFoundException(msg.format(msgData));
		}
		// If we've gotten this far, then resultList.size() must be equal to 1.
		Set resultSet = new HashSet(resultList);
		Iterator resultSetIterator = resultSet.iterator();
		pSubject = (PrivilegedSubjectImpl)(resultSetIterator.next());
		pSubject.setSignet(signet);
		return pSubject;
	}


	/**
	 * This method is only for use by the native Signet TreeAdapter.
	 * 
	 * @return null if the Tree is not found.
	 * @throws SignetRuntimeException
	 *           if more than one Tree is found.
	 */
	public Tree getNativeSignetTree(String id) throws ObjectNotFoundException
	{
		TreeImpl treeImpl = (TreeImpl)(load(TreeImpl.class, id));
		treeImpl.setAdapter(new TreeAdapterImpl(signet));
		treeImpl.setSignet(signet);
		return treeImpl;
	}


	/**
	 * Gets a single Assignment by ID.
	 * 
	 * @param id
	 * @return the fetched Assignment object
	 * @throws ObjectNotFoundException
	 */
	public Assignment getAssignment(int id) throws ObjectNotFoundException
	{
		Assignment assignment = (Assignment)(load(AssignmentImpl.class, new Integer(id)));
		return assignment;
	}

	/**
	 * Gets a single Proxy by ID.
	 * 
	 * @param id
	 * @return the fetched Proxy object
	 * @throws ObjectNotFoundException
	 */
	public Proxy getProxy(int id) throws ObjectNotFoundException
	{
		Proxy proxy = (Proxy)(load(ProxyImpl.class, new Integer(id)));
		return proxy;
	}


	///////////////////////////////////
	// unit test support
	///////////////////////////////////

 /** for testing only */
 public final ChoiceSet getChoiceSet(String choiceSetId) throws ObjectNotFoundException
 {
   List resultList;

   try
   {
     Query query = getSession().createQuery(
    		 "from edu.internet2.middleware.signet.ChoiceSetImpl" 
              + " as choiceSet"  
              + " where choiceSetID = :id"); 

      query.setString("id", choiceSetId); 

      resultList = query.list();
    }
    catch (HibernateException e)
    {
      throw new SignetRuntimeException(e);
    }
    
    if (resultList.size() > 1)
    {
    	Object[] formData = new Object[] {
    			new Integer(resultList.size()),
    			choiceSetId
    	};
    	MessageFormat form = new MessageFormat(
    			ResLoaderApp.getString("Signet.msg.exc.choiceSetId_1") + //$NON-NLS-1$
    			ResLoaderApp.getString("Signet.msg.exc.choiceSetId_2") + //$NON-NLS-1$
    			ResLoaderApp.getString("Signet.msg.exc.choiceSetId_3")); //$NON-NLS-1$
  
    	throw new SignetRuntimeException(form.format(formData));
    }
    
    else if (resultList.size() < 1)
    {
    	Object[] formData = new Object[] { choiceSetId };
    	throw new ObjectNotFoundException(
    			MessageFormat.format(
    					ResLoaderApp.getString("Signet.msg.exc.choiceSetFetch"), //$NON-NLS-1$
    					formData));
    } 

	else // If we've gotten this far, then resultList.size() must be equal to 1.
	{
		ChoiceSetImpl choiceSet = (ChoiceSetImpl)resultList.get(0);
		choiceSet.setSignet(signet);
		return (choiceSet);
	}
 }

}

/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/dbpersist/HibernateDB.java,v 1.8 2007-03-05 18:10:51 ddonn Exp $

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
*/
package edu.internet2.middleware.signet.dbpersist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetApiUtil;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeAdapterImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.TreeNodeRelationship;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.subjsrc.SignetSubjectAttr;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * Wrapper for the Hibernate DB persistence layer. Each Signet instance has a
 * reference to it's own HibernateDB object. Each HibernateDB object has it's
 * own, always-open, Session, which gets re-used each time the beginTransaction-
 * "some action"-commit cycle occurs. Nested transactions are prevented using the
 * "push counter" called transactDepth.
 * @version $Revision: 1.8 $
 * @author $Author: ddonn $
 */
public class HibernateDB
{
	/** logging */
	protected Log				log;
	/** Reference to the Signet instance. */
	protected Signet			signet;
	/** The Hibernate Configuration */
	protected Configuration		cfg;
	/** The Hibernate SessionFactory */
	protected SessionFactory	sessionFactory;

	protected static final String	Qry_subjectByPK =
			"from " + SignetSubject.class.getName() + //$NON-NLS-1$
			" as subject " + //$NON-NLS-1$
			" where subjectkey = :subjectkey "; //$NON-NLS-1$

	protected static final String	Qry_proxiesGranted =
			"from " + ProxyImpl.class.getName() + //$NON-NLS-1$
			" as proxy " +  //$NON-NLS-1$
			" where grantorKey = :grantorKey " + //$NON-NLS-1$
			" and " + //$NON-NLS-1$
			" status = :status "; //$NON-NLS-1$

	protected static final String	Qry_proxiesReceived =
			"from " + ProxyImpl.class.getName() + //$NON-NLS-1$
			" as proxy " +  //$NON-NLS-1$
			" where granteeKey = :granteeKey " + //$NON-NLS-1$
			" and " + //$NON-NLS-1$
			" status = :status "; //$NON-NLS-1$

	protected static final String	Qry_assignmentsGranted =
			"from " + AssignmentImpl.class.getName() + //$NON-NLS-1$
			" as assignment " +  //$NON-NLS-1$
			" where grantorKey = :grantorKey " + //$NON-NLS-1$
			" and " + //$NON-NLS-1$
			" status = :status "; //$NON-NLS-1$

	protected static final String	Qry_assignmentsReceived =
			"from " + AssignmentImpl.class.getName() + //$NON-NLS-1$
			" as assignment " +  //$NON-NLS-1$
			" where granteeKey = :granteeKey " + //$NON-NLS-1$
			" and " + //$NON-NLS-1$
			" status = :status "; //$NON-NLS-1$

	protected static final String Qry_subjByIdSrc =
			"from " + SignetSubject.class.getName() + //$NON-NLS-1$
			" as signetSubject " + //$NON-NLS-1$
			"where " + //$NON-NLS-1$
			"sourceID = :source_id " + //$NON-NLS-1$
			"and " + //$NON-NLS-1$
			"subjectID = :subject_id "; //$NON-NLS-1$

	protected static final String QRY_SUBJECTBYID =
			"from " + //$NON-NLS-1$
				SignetSubject.class.getName() + " subj, " +		//$NON-NLS-1$
				SignetSubjectAttr.class.getName() + " attr " +	//$NON-NLS-1$
			" where " + 										//$NON-NLS-1$
				" attr.attrValue = :subjIdentifier " +			//$NON-NLS-1$
				" and " + 										//$NON-NLS-1$
				" attr.mappedName = 'subjectAuthId' " +			//$NON-NLS-1$
				" and " + 										//$NON-NLS-1$
				" attr.parent.subject_PK = subj.subject_PK " +	//$NON-NLS-1$
			" order by " + 										//$NON-NLS-1$
				" attr.parent.subject_PK, " + 					//$NON-NLS-1$
				" attr.mappedName, " +							//$NON-NLS-1$
				" attr.sequence ";								//$NON-NLS-1$


	///////////////////////////////////
	// class methods
	///////////////////////////////////

	/**
	 * constructor
	 * @param signetInstance The Signet instance
	 */
	public HibernateDB(Signet signetInstance)
	{
		log = LogFactory.getLog(HibernateDB.class);

		try
		{
			// Read the "hibernate.cfg.xml" file. It is expected to be in a root directory of the classpath
			cfg = new Configuration();
			cfg.configure();

			// create a SessionFactory
			sessionFactory = cfg.buildSessionFactory();
		}
		catch (HibernateException he)
		{
			log.error("HibernateDB.HibernateDB: hibernate error"); //$NON-NLS-1$
			log.error(he.toString());
			throw new SignetRuntimeException(he);
		}

		signet = signetInstance;
	}


	public Configuration getConfiguration()
	{
		return (cfg);
	}


	/**
	 * Load a DB object when the ID is known using the caller-supplied Session.
	 * @param hs The Hibernate Session used to load loadClass
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class or null
	 * @throws ObjectNotFoundException
	 */
	public Object load(Session hs, Class loadClass, String id) throws ObjectNotFoundException
	{
		Object retval = null;

		if (null == hs)
			throw new ObjectNotFoundException("No 'session' provided for load()");
		else if (null == id)
			throw new ObjectNotFoundException("No 'id' provided for load()");
		else if (null == loadClass)
			throw new ObjectNotFoundException("No 'loadClass' provided for load()");

		try { retval = hs.load(loadClass, id); }
		catch (HibernateException he)
		{
			throw new ObjectNotFoundException(he);
		}

		return (retval);
	}

	/**
	 * Creates a temporary Session and loads a DB object when the ID is known.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class
	 * @throws ObjectNotFoundException
	 */
	public Object load(Class loadClass, String id) throws ObjectNotFoundException
	{
		Object retval = null;

		if (null != id)
		{
			try 
			{
				Session s = openSession();
				retval = load(s, loadClass, id);
				closeSession(s);
			}
			catch (ObjectNotFoundException onfe)
			{
				throw onfe;
			}
		}

		return (retval);
	}

	/**
	 * Returns an object of the given Class matching the given primary key
	 * @param hs
	 * @param loadClass
	 * @param id
	 * @return Returns an object of the given Class matching the given primary key
	 * @throws ObjectNotFoundException
	 */
	public Object load(Session hs, Class loadClass, Integer id) throws ObjectNotFoundException
	{
		Object retval = null;

		if (null == hs)
			throw new ObjectNotFoundException("No 'session' provided for load()");
		else if (null == id)
			throw new ObjectNotFoundException("No 'id' provided for load()");
		else if (null == loadClass)
			throw new ObjectNotFoundException("No 'loadClass' provided for load()");

		try { retval = hs.load(loadClass, id); }
		catch (HibernateException he)
		{
			throw new ObjectNotFoundException(he);
		}

		return (retval);
	}

	/**
	 * Load a DB object when the ID is known. Wrapper method for session.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class matching the id
	 * @throws ObjectNotFoundException
	 */
	public Object load(Class loadClass, int id) throws ObjectNotFoundException
	{
		Object retval = null;

		try
		{
			Session s = openSession();
			retval = load(s, loadClass, new Integer(id));
			closeSession(s);
		}
		catch (ObjectNotFoundException e)
		{
			throw e;
		}

		return (retval);
	}


	/**
	 * Returns an object of the given Class matching the given primary key
	 * @param hs
	 * @param loadClass
	 * @param id
	 * @return Returns an object of the given Class matching the given primary key
	 * @throws ObjectNotFoundException
	 */
	public Object load(Session hs, Class loadClass, Long id) throws ObjectNotFoundException
	{
		Object retval = null;

		if (null == hs)
			throw new ObjectNotFoundException("No 'session' provided for load()");
		else if (null == id)
			throw new ObjectNotFoundException("No 'id' provided for load()");
		else if (null == loadClass)
			throw new ObjectNotFoundException("No 'loadClass' provided for load()");

		try { retval = hs.load(loadClass, id); }
		catch (HibernateException he)
		{
			throw new ObjectNotFoundException(he);
		}

		return (retval);
	}

	/**
	 * Load a DB object when the ID is known. Wrapper method for session.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class
	 * @throws ObjectNotFoundException
	 */
	public Object load(Class loadClass, long id) throws ObjectNotFoundException
	{
		Object retval = null;

		try
		{
			Session hs = openSession();
			retval = load(hs, loadClass, new Long(id));
			closeSession(hs);
		}
		catch (ObjectNotFoundException e)
		{
			throw e;
		}

		return (retval);
	}


	/**
	 * Create a Hibernate Query (wrapper method for Session)
	 * @param hibrQuery
	 * @return A SQL query
	 * @throws SignetRuntimeException
	 */
	public Query createQuery(Session session, String hibrQuery) throws SignetRuntimeException
	{
		Query retval = null;

		try
		{
			retval = session.createQuery(hibrQuery);
		}
		catch (HibernateException he)
		{
			throw new SignetRuntimeException(he);
		}

		return (retval);
	}


	/**
	 * Run the Hibernate Query Language query and return the results (wrapper method for session)
	 * @param hibrQuery HQL query string
	 * @return List of matching records or empty List (never null)
	 * @throws SignetRuntimeException
	 */
	public List find(Session session, String hibrQuery) throws SignetRuntimeException
	{
		List retval = null;

		if ((null == hibrQuery) || (0 >= hibrQuery.length()))
			retval = new ArrayList();
		else
		{
			Query query = createQuery(session, hibrQuery);
			try { retval = query.list(); }
			catch (HibernateException e)
			{
				throw new SignetRuntimeException(e);
			}
		}

		return (retval);
	}

	/**
	 * Open a new Session, it's the caller's responsibility to call closeSession()
	 * @return Returns a new Session
	 */
	public Session openSession()
	{
		Session session = null;
		try
		{
			session = sessionFactory.openSession();
		}
		catch (HibernateException he)
		{
			log.error("HibernateDB.HibernateDB: hibernate error");
			log.error(he.toString());
			throw new SignetRuntimeException(he);
		}
		return (session);
	}

	/**
	 * Closes a Hibernate session.
	 */
	public void closeSession(Session session)
	{
		try
		{
			if (null != session)
			{
				session.close();
			}
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}

	/**
	 * Saves an object, and any signet objects it refers to. Caller is responsible
	 * for 1) creating/opening a Session, 2) beginTransaction, 3) commit or rollback
	 * Fascade for Hibernate Session.
	 * @param o The Object to save. Must have been previously defined in a
	 * object.hbm.xml configuration file.
	 */
	public void save(Session session, Object o)
	{
		if ((null == o) || (null == session))
			return;
		try
		{
			session.saveOrUpdate(o);
		}
		catch (HibernateException e)
		{
			log.error("HibernateDB.save(Session, Object): Error occurred processing Object " + o.toString());
			throw new SignetRuntimeException(e);
		}
	}

	public void save(Session session, List list)
	{
		if ((null == list) || (null == session))
			return;

		for (Iterator objs = list.iterator(); objs.hasNext(); )
		{
			Object o = objs.next();
			try { session.saveOrUpdate(o); }
			catch (HibernateException e)
			{
				log.error("HibernateDB.save(Session, List): Error occurred processing Object " + o.toString());
				throw new SignetRuntimeException(e);
			}
		}
	}


	/**
	 * Updates an existing persisted signet object, and any signet objects it refers to.
	 * Fascade for Hibernate Session.
	 * @param o The Object to update. Must have been previously defined in a
	 * object.hbm.xml configuration file.
	 */
	public void update(Session session, Object o)
	{
		try
		{
			session.update(o);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}


	public void delete(Session session, Object o)
	{
		try
		{
			session.delete(o);
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
	public Tree getTree(Session hs, String id) throws ObjectNotFoundException
	{
		TreeImpl tree = (TreeImpl)(load(hs, TreeImpl.class, id));
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
		Session session = openSession();
		List resultList = find(session, "from edu.internet2.middleware.signet.SubsystemImpl as subsystem");

		Set resultSet = new HashSet(resultList);
		for (Iterator i = resultSet.iterator(); i.hasNext();)
			((EntityImpl)i.next()).setSignet(signet);
		Set retval = UnmodifiableSet.decorate(resultSet);
		closeSession(session);

		return (retval);
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

		Session hs = openSession();
		try
		{
			subsystemImpl = (SubsystemImpl)(load(hs, SubsystemImpl.class, id));
		}
		catch (ObjectNotFoundException onfe)
		{
			closeSession(hs);
			Object[] msgData = new Object[] { id };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.subsysNotFound")); //$NON-NLS-1$
			throw new ObjectNotFoundException(msg.format(msgData), onfe);
		}
		subsystemImpl.setSignet(signet);
		closeSession(hs);
		return subsystemImpl;
	}


	public Set findDuplicates(Assignment assignment)
	{
		Query query;
		List resultList;
		Session session = openSession();
		try
		{
			query = createQuery(session,
					"from " +
					AssignmentImpl.class.getName() +
					" as assignment " + 
					" where granteeKey = :granteeKey " + 
					" and functionKey = :functionKey " + 
					" and scopeID = :scopeId " + 
					" and scopeNodeID = :scopeNodeId " +
					" and assignmentID != :assignmentId ");
			query.setParameter("granteeKey", assignment.getGrantee().getSubject_PK(), Hibernate.LONG);
			query.setParameter("functionKey", ((FunctionImpl)(assignment.getFunction())).getKey(), Hibernate.INTEGER);
			query.setString("scopeId", assignment.getScope().getTree().getId());
			query.setString("scopeNodeId", assignment.getScope().getId());
			query.setParameter("assignmentId", assignment.getId(), Hibernate.INTEGER);
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
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

		closeSession(session);
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
		Session session = openSession();
		try
		{
			query = createQuery(session,
					"from " +
					TreeNodeRelationship.class.getName() +
					" as treeNodeRelationship" +
					" where treeID = :treeId" +
					" and nodeID = :childNodeId");
			query.setString("treeId", tree.getId());
			query.setString("childNodeId", childNode.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
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

		closeSession(session);
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
		Session session = openSession();
		try
		{
			query = createQuery(session,
					"from " +
					TreeNodeRelationship.class.getName() +
					" as treeNodeRelationship"
					+ " where treeID = :treeId" +
					" and parentNodeID = :parentNodeId");
			query.setString("treeId", tree.getId());
			query.setString("parentNodeId", parentNode.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
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

		closeSession(session);
		return children;
	}


	// I really want to do away with this method, having the Subsystem
	// pick up its associated Limits via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	public Map getLimitsBySubsystem(Subsystem subsystem)
	{
		List resultList;
		Session session = openSession();
		try
		{
			Query query = createQuery(session,
					"from " +
					LimitImpl.class.getName() +
					" as limit where subsystemID = :id");
			query.setString("id", subsystem.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
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

		closeSession(session);
		return limits;
	}


	// I really want to do away with this method, having the Subsystem
	// pick up its associated Permissions via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	public Map getPermissionsBySubsystem(Subsystem subsystem)
	{
		Query query;
		List resultList;
		Session session = openSession();
		try
		{
			query = createQuery(session,
					"from " +
					PermissionImpl.class.getName() +
					" as limit where subsystemID = :id");
			query.setString("id", subsystem.getId());
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
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

		closeSession(session);
		return permissions;
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
		AssignmentImpl assignment = (AssignmentImpl)(load(AssignmentImpl.class, id));
		assignment.setSignet(signet);
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
		ProxyImpl proxy = (ProxyImpl)(load(ProxyImpl.class, id));
		proxy.setSignet(signet);
		return proxy;
	}


	/**
	 * Get the set of Proxies granted by the grantor.
	 * @param grantorId The primary key of the proxy grantor
	 * @param status The status of the proxy
	 * @return A Set of ProxyImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getProxiesGranted(long grantorId, String status)
	{
		Set retval = new HashSet();

		Session session = openSession();
		Query qry = createQuery(session, Qry_proxiesGranted);
		qry.setLong("grantorKey", grantorId);
		qry.setString("status", status);
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			session.close();
			log.error("Unable to obtain iterator for results");
		}
		closeSession(session);

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Proxies granted to the grantee.
	 * @param granteeId The primary key of the proxy grantee
	 * @param status The status of the proxy
	 * @return A Set of ProxyImpl objects that have been granted to grantee.
	 * May be an empty set but never null.
	 */
	public Set getProxiesReceived(long granteeId, String status)
	{
		Set retval = new HashSet();

		Session session = openSession();
		Query qry = createQuery(session, Qry_proxiesReceived);
		qry.setLong("granteeKey", granteeId);
		qry.setString("status", status);
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			session.close();
			log.error("Unable to obtain iterator for results");
		}
		closeSession(session);

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}


	/**
	 * Get the set of Assignments granted by the grantor.
	 * @param grantorId The primary key of the assignment grantor
	 * @param status The status of the assignment
	 * @return A Set of AssignmentImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getAssignmentsGranted(long grantorId, String status)
	{
		Set retval = new HashSet();

		Session session = openSession();
		Query qry = createQuery(session, Qry_assignmentsGranted);
		qry.setLong("grantorKey", grantorId);
		qry.setString("status", status);
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			session.close();
			log.error("Unable to obtain iterator for results");
		}
		closeSession(session);

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Assignments granted to the grantee.
	 * @param granteeId The primary key of the assignment grantee
	 * @param status The status of the assignment
	 * @return A Set of AssignmentImpl objects that have been granted to grantee.
	 * May be an empty set but never null.
	 */
	public Set getAssignmentsReceived(long granteeId, String status)
	{
		Set retval = new HashSet();

		Session session = openSession();
		Query qry = createQuery(session, Qry_assignmentsReceived);
		qry.setLong("granteeKey", granteeId);
		qry.setString("status", status);
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			session.close();
			log.error("Unable to obtain iterator for results");
		}
		closeSession(session);

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}


	///////////////////////////////////
	// unit test support
	///////////////////////////////////

 /** for testing only */
 public final ChoiceSet getChoiceSet(String choiceSetId) throws ObjectNotFoundException
 {
   List resultList;

   Session session = openSession();
   try
   {
     Query query = session.createQuery(
    		 "from edu.internet2.middleware.signet.ChoiceSetImpl" 
              + " as choiceSet"  
              + " where choiceSetID = :id"); 

      query.setString("id", choiceSetId); 

      resultList = query.list();
    }
    catch (HibernateException e)
    {
      session.close();
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

    	session.close();

    	throw new SignetRuntimeException(form.format(formData));
    }
    
    else if (resultList.size() < 1)
    {
    	session.close();
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
		session.close();
		return (choiceSet);
	}
 }


/////////////////////////////////////////////////////////////////////////////
//
//		Re-architecture
//
/////////////////////////////////////////////////////////////////////////////

	/**
	 * Get the SignetSubject that matches the DB primary key from Persisted Store.
	 * @param subject_pk The primary key
	 * @return The matching SignetSubject or null
	 */
	public SignetSubject getSubject(long subject_pk)
	{
		SignetSubject subject;

		try
		{
			subject = (SignetSubject)load(SignetSubject.class, subject_pk);
		}
		catch (ObjectNotFoundException e)
		{
			log.error("No SignetSubject found with subjectkey=" + subject_pk);
			subject = null;
		}

		return (subject);
	}


	/**
	 * Query for a signet_subject that matches the given sourceId/subjectId
	 * @param sourceId The original Source id of the Subject (from SubjectAPI)
	 * @param subjectId The original Subject id of the Subject (from SubjectAPI)
	 * @return A SignetSubject that matches the given sourceId/subjectId or null if not found
	 */
	public SignetSubject getSubject(String sourceId, String subjectId)
			throws ObjectNotFoundException
	{
		List resultList;
		Session session = openSession();
		try
		{
//System.out.println("HibernateDB.getSubject: sourceId=" + sourceId + " subjectId=" + subjectId);
			Query query = createQuery(session, Qry_subjByIdSrc);
			query.setString("subject_id", subjectId); //$NON-NLS-1$
			query.setString("source_id", sourceId); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			session.close();
			throw new SignetRuntimeException(e);
		}

		SignetSubject retval = null;

		Object[] msgData;
		MessageFormat msgFmt;
		switch (resultList.size())
		{
			case (0):
				msgData = new Object[] { sourceId, subjectId };
				String msgTemplate = ResLoaderApp.getString("HibernateDb.msg.exc.SubjNotFound");  //$NON-NLS-1$
				msgFmt = new MessageFormat(msgTemplate);
				String msg = msgFmt.format(msgData);
				session.close();
				throw new ObjectNotFoundException(msg);
			case (1):
				retval = (SignetSubject)resultList.iterator().next();
				break;
			default:
				msgData = new Object[] { new Integer(resultList.size()), subjectId, sourceId };
				msgFmt = new MessageFormat(ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_1") + //$NON-NLS-1$
						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_2") + //$NON-NLS-1$
						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_3")); //$NON-NLS-1$
				session.close();
				throw new SignetRuntimeException(msgFmt.format(msgData));
		}

		closeSession(session);

		return (retval);
	}


	public SignetSubject getSubjectByIdentifier(String identifier)
			throws ObjectNotFoundException
	{
		List resultList;
		Session session = openSession();
		try
		{
			Query query = createQuery(session, QRY_SUBJECTBYID);
			query.setString("subjIdentifier", identifier); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException he)
		{
			session.close();
			throw new SignetRuntimeException(he);
		}

		SignetSubject retval = null;

		switch (resultList.size())
		{
			case (0):
				session.close();
				throw new ObjectNotFoundException("object not found");
//				msgData = new Object[] { identifier };
//				String msgTemplate = ResLoaderApp.getString("HibernateDb.msg.exc.SubjNotFound");  //$NON-NLS-1$
//				msgFmt = new MessageFormat(msgTemplate);
//				String msg = msgFmt.format(msgData);
//				throw new ObjectNotFoundException(msg);
			case (1):
// returns an array of arrays!
// The sub-array contains a SignetSubject and SignetSubjectAttr
Object obj = resultList.iterator().next();
if (obj instanceof Object[])
	retval = (SignetSubject)((Object[])obj)[0];
else
	retval = (SignetSubject)obj;
//				retval = (SignetSubject)resultList.iterator().next();
				break;
			default:
				session.close();
				throw new SignetRuntimeException(new Integer(resultList.size()).toString());
//				msgData = new Object[] { new Integer(resultList.size()), identifier };
//				msgFmt = new MessageFormat(ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_1") + //$NON-NLS-1$
//						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_2") + //$NON-NLS-1$
//						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_3")); //$NON-NLS-1$
//				throw new SignetRuntimeException(msgFmt.format(msgData));
		}

		closeSession(session);
		return (retval);
	}

}

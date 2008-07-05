/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/dbpersist/HibernateDB.java,v 1.20 2008-07-05 23:48:30 ddonn Exp $

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

import java.io.Serializable;
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
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
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
import edu.internet2.middleware.signet.TreeNodeImpl;
import edu.internet2.middleware.signet.TreeNodeRelationship;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * Wrapper for the Hibernate DB persistence layer. Each Signet instance has a
 * reference to it's own HibernateDB object. Each HibernateDB object has it's
 * own, always-open, Session, which gets re-used each time the beginTransaction-
 * "some action"-commit cycle occurs. Nested transactions are prevented using the
 * "push counter" called transactDepth.
 * @version $Revision: 1.20 $
 * @author $Author: ddonn $
 */
public class HibernateDB implements Serializable
{
	/** logging */
	protected Log				log;
	/** Reference to the Signet instance. */
	protected Signet			signet;
	/** The Hibernate Configuration */
	protected Configuration		cfg;
	/** The Hibernate SessionFactory */
	protected SessionFactory	sessionFactory;

	/** Cache the Tree for performance */
	protected TreeImpl cachedTree = null;


	///////////////////////////////////
	// class methods
	///////////////////////////////////

	/** Private constructor for Serializable */
	private HibernateDB() {  }

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


	/**
	 * Get the Hibernate Configuration
	 * @return The Hibernate Configuration object
	 */
	public Configuration getConfiguration()
	{
		return (cfg);
	}


	/**
	 * Load a DB object when the ID is known using the caller-supplied Session.
	 * @param hs The Hibernate Session used to load loadClass
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Session hs, Class loadClass, String id)
	{
		Object						retval = null;
		IllegalArgumentException	iae = null;

		if (null == hs)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoSession")); //$NON-NLS-1$
		else if ((null == id) || (0 >= id.length()))
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoId")); //$NON-NLS-1$

		else if (null == loadClass)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoClass")); //$NON-NLS-1$

		if (null != iae)
		{
			log.error(iae);
			throw iae; 
		}

		try
		{
			retval = hs.get(loadClass, id);
		}
		catch (HibernateException he)
		{
			log.error(he);
		}

		return (retval);
	}

	/**
	 * Creates a temporary Session and loads a DB object when the ID is known.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Class loadClass, String id)
	{
		Object retval = null;

		Session hs = openSession();
		retval = load(hs, loadClass, id);
		closeSession(hs);

		return (retval);
	}

	/**
	 * Returns an object of the given Class matching the given primary key
	 * @param hs
	 * @param loadClass
	 * @param id
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Session hs, Class loadClass, Integer id)
	{
		Object						retval = null;
		IllegalArgumentException	iae = null;

		if (null == hs)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoSession")); //$NON-NLS-1$
		else if ((null == id) || (0 >= id.intValue()))
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoId")); //$NON-NLS-1$

		else if (null == loadClass)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoClass")); //$NON-NLS-1$

		if (null != iae)
		{
			log.error(iae);
			throw iae; 
		}


		try { retval = hs.get(loadClass, id); }
		catch (HibernateException he)
		{
			log.error(he);
		}

		return (retval);
	}

	/**
	 * Load a DB object when the ID is known. Wrapper method for session.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Class loadClass, int id)
	{
		Object retval = null;

		Session hs = openSession();
		retval = load(hs, loadClass, new Integer(id));
		closeSession(hs);

		return (retval);
	}


	/**
	 * Returns an object of the given Class matching the given primary key
	 * @param hs
	 * @param loadClass
	 * @param id
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Session hs, Class loadClass, Long id)
	{
		Object						retval = null;
		IllegalArgumentException	iae = null;

		if (null == hs)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoSession")); //$NON-NLS-1$
		else if ((null == id) || (0L >= id.longValue()))
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoId")); //$NON-NLS-1$

		else if (null == loadClass)
			iae = new IllegalArgumentException(
					ResLoaderApp.getString(
							"HibernateDb.msg.load.NoClass")); //$NON-NLS-1$

		if (null != iae)
		{
			log.error(iae);
			throw iae; 
		}

		try { retval = hs.get(loadClass, id); }
		catch (HibernateException he)
		{
			log.error(he);
		}

		return (retval);
	}

	/**
	 * Load a DB object when the ID is known. Wrapper method for session.
	 * @param loadClass Class to load
	 * @param id Primary key for DB-mapped class
	 * @return Instance of the requested Class, or null if no record of
	 * class-type with the given ID is found.
	 */
	public Object load(Class loadClass, long id)
	{
		Object retval = null;

		Session hs = openSession();
		retval = load(hs, loadClass, new Long(id));
		closeSession(hs);

		return (retval);
	}


	/**
	 * Create a Hibernate Query (wrapper method for Session)
	 * @param hibrQuery
	 * @return A SQL query
	 * @throws SignetRuntimeException
	 */
	public Query createQuery(Session hs, String hibrQuery) throws SignetRuntimeException
	{
		Query retval = null;

		try
		{
			retval = hs.createQuery(hibrQuery);
		}
		catch (HibernateException he)
		{
			throw new SignetRuntimeException(he);
		}

		return (retval);
	}


	/**
	 * Run the arbitrary Hibernate Query Language query and return the results
	 * @param hs A Hibernate Session
	 * @param hibrQuery HQL query string
	 * @return List of matching records or empty List (never null)
	 * @throws SignetRuntimeException
	 */
	public List find(Session hs, String hibrQuery) throws SignetRuntimeException
	{
		List retval = null;

		if ((null == hibrQuery) || (0 >= hibrQuery.length()))
			retval = new ArrayList();
		else
		{
			Query query = createQuery(hs, hibrQuery);
			try { retval = query.list(); }
			catch (HibernateException e)
			{
				throw new SignetRuntimeException(e);
			}
		}

		return (retval);
	}

protected Session stdSession = null;

	/**
	 * Open a new Session, it's the caller's responsibility to call closeSession()
	 * @return Returns a new Session
	 */
	public Session openSession()
	{
		if (null == stdSession)
		{
			try
			{
				stdSession = sessionFactory.openSession();
// The following line causes problems with Sybase. According to Hibernate 3.2
// documentation, AutoCommit is 'off' by default, so manually setting it is
// not necessary.
//stdSession.connection().setAutoCommit(false);
			}
			catch (HibernateException he)
			{
				log.error("HibernateDB.HibernateDB: hibernate error"); //$NON-NLS-1$
				log.error(he.toString());
				throw new SignetRuntimeException(he);
			}
//			catch (SQLException sqle)
//			{
//				log.error("HibernateDB.openSession: SQL exception: " + sqle.toString());
//				throw new SignetRuntimeException(sqle);
//			}
		}
		return (stdSession);
	}

	/**
	 * Closes a Hibernate session.
	 */
	public void closeSession(Session hs)
	{
		if (hs != stdSession)
		{
			try
			{
				if (null != hs)
				{
					hs.close();
				}
			}
			catch (HibernateException e)
			{
				throw new SignetRuntimeException(e);
			}
		}
	}

	public void reset()
	{
		try
		{
			if (null != stdSession)
			{
				if (stdSession.isDirty())
					log.warn("HibernateDB.reset: Outstanding transactions have been lost"); //$NON-NLS-1$
				stdSession.close();
				stdSession = null;
			}
			openSession();
			
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}

		cachedTree = null;
	}


	/**
	 * Saves an object, and any signet objects it refers to. Caller is responsible
	 * for 1) creating/opening a Session, 2) beginTransaction, 3) commit or rollback
	 * Fascade for Hibernate Session.
	 * @param o The Object to save. Must have been previously defined in a
	 * object.hbm.xml configuration file.
	 */
	public void save(Session hs, Object o)
	{
		if ((null == o) || (null == hs))
			return;
		try
		{
			hs.saveOrUpdate(o);
		}
		catch (HibernateException e)
		{
			log.error("HibernateDB.save(Session, Object): Error occurred processing Object " + o.toString()); //$NON-NLS-1$
			throw new SignetRuntimeException(e);
		}
	}

	public void save(Session hs, List list)
	{
		if ((null == list) || (null == hs))
			return;

		for (Iterator objs = list.iterator(); objs.hasNext(); )
		{
			Object o = objs.next();
			try { hs.saveOrUpdate(o); }
			catch (HibernateException e)
			{
				log.error("HibernateDB.save(Session, List): Error occurred processing Object " + o.toString()); //$NON-NLS-1$
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
	public void update(Session hs, Object o)
	{
		try
		{
			hs.update(o);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}


	public void delete(Session hs, Object o)
	{
		try
		{
			hs.delete(o);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
	}


	/**
	 * Proxy for getTreeById(Session hs, String id)
	 * @param hs The Hibernate Session to use
	 * @param id The ID of the Tree
	 * @return the specified Tree
	 */
	public Tree getTree(Session hs, String id)
	{
		return (getTreeById(hs, id));
	}

	/**
	 * Gets a single Tree by ID. Opens a Hibernate Session for the duration of
	 * this call. Note that this class caches the previously selected Tree
	 * @param id The ID of the Tree
	 * @return the specified Tree
	 * @throws ObjectNotFoundException
	 */
	public Tree getTreeById(String id) throws ObjectNotFoundException
	{
		return (getTreeById(null, id));
	}

	/**
	 * Gets a single Tree by ID using the supplied Hibernate Session. If
	 * hs is null a new Hibernate Session is opened and closed for the
	 * duration of this call. NOTE: HibernateDB caches the most recently
	 * fetched ScopeTree. If the incoming ID matches that of the cached tree,
	 * the cached tree is returned to the caller.
	 * @param hs The Hibernate Session to use
	 * @param id The ID of the Tree
	 * @return The specified Tree, or null if no ScopeTree is found for the
	 * given ID.
	 */
	public Tree getTreeById(Session hs, String id)
	{
		if ((null == cachedTree) || ( !cachedTree.getId().equals(id)))
		{
			Session session = (null != hs) ? hs : openSession();

			if (null != (cachedTree = (TreeImpl)(load(session, TreeImpl.class, id))))
				cachedTree.setSignet(signet);

			if (null == hs)
					closeSession(session);
		}

		return (cachedTree);
	}

	/**
	 * Return the single Tree for the given SubsystemId
	 * @param hs A Hibernate Session
	 * @param subsysId The Subsystem Id
	 * @return The Tree for the given Subsystem or null if not found
	 */
	public Tree getTreeBySubsystemId(Session hs, String subsysId)
	{
		Tree retval = null;

		Session session = (null != hs) ? hs : openSession();

		try
		{
			Query query = createQuery(session, HibernateQry.Qry_treeBySubsysId);
			query.setString("subsystemID", subsysId);
			List resultList = query.list();
			// should be 1-and-only-1 Tree for a given Subsystem
			switch (resultList.size())
			{
				case 0:
					log.error("No Tree found for Subsystem Id = " + subsysId);
					break;
				case 1:
					retval = (Tree)resultList.get(0);
					break;
				default:
					log.error(resultList.size() + " Trees found for Subsystem Id = " + subsysId);
					break;
			}
		}
		catch (HibernateException e)
		{
			log.error(e);
		}
		finally
		{
			if (null == hs)
				closeSession(session);
		}

		return (retval);
	}

	/**
	 * Return a Set of all Scope Trees defined in Signet's persisted store
	 * @param hs A Hibernate Session to use. If null, a Session is opened
	 * then closed for this call.
	 * @return Set of all Scope Trees defined in Signet's persisted store.
	 * May be an empty set, but never null.
	 */
	public Set getTrees(Session hs)
	{
		Set retval = new HashSet();

		Session session = (null != hs) ? hs : openSession();

		try
		{
			Query query = createQuery(session, HibernateQry.Qry_treesAll);
			retval.addAll(query.list());

			for (Iterator iter = retval.iterator(); iter.hasNext(); )
				((TreeImpl)iter.next()).setSignet(this.signet);
		}
		catch (HibernateException e)
		{
			log.error(e);
		}
		finally
		{
			if (null == hs)
				closeSession(session);
		}

		return (retval);
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
		Session hs = openSession();
		List resultList = find(hs, "from edu.internet2.middleware.signet.SubsystemImpl as subsystem");

		Set resultSet = new HashSet(resultList);
		for (Iterator i = resultSet.iterator(); i.hasNext();)
			((EntityImpl)i.next()).setSignet(signet);
		Set retval = UnmodifiableSet.decorate(resultSet);
		closeSession(hs);

		return (retval);
	}

	/**
	 * Get all Subsystems, with optional Status filter
	 * @param status Optional
	 * @return All Subsystems, optionally filtered by Status
	 */
	public Set getSubsystems(Status status)
	{
		Set retval = new HashSet();

		Session tmp_hs = openSession();

		StringBuilder queryBuf = new StringBuilder(HibernateQry.Qry_subsystemsAll);
		if (null != status)
			queryBuf.append(HibernateQry.Qry_subsystemByStatus_STUB);

		Query query = tmp_hs.createQuery(queryBuf.toString());
		if (null != status)
			query.setString("status", status.getName());
		retval.addAll(query.list());

		closeSession(tmp_hs);

		for (Iterator i = retval.iterator(); i.hasNext();)
			((EntityImpl)i.next()).setSignet(signet);

		return (retval);
	}

	/**
	 * Gets a single Subsystem by ID.
	 * 
	 * @param id
	 * @return the specified Subsystem
	 */
	public Subsystem getSubsystem(String id)
	{
		SubsystemImpl subsystemImpl;

		Session hs = openSession();

		if (null != (subsystemImpl = (SubsystemImpl)(load(hs, SubsystemImpl.class, id))))
			subsystemImpl.setSignet(signet);

		closeSession(hs);

		return (subsystemImpl);
	}

	/**
	 * Get all Subsystems matching the subsysName and optional status
	 * @param hs A Hibernate Session. If null, this method will open (and close)
	 * a new Session for the duration of the call
	 * @param subsysName The name (not ID) of the Subsystem to find
	 * @param status Optional parameter to filter on the Status value
	 * @return A Set of Subsystems matching the subsysName and optional status,
	 * may be an empty Set (never null)
	 */
	public Set getSubsystemsByName(Session hs, String subsysName, Status status)
	{
		Set retval = new HashSet();

		Session session = (null != hs) ? hs : openSession();

		StringBuilder queryBuf = new StringBuilder(HibernateQry.Qry_subsystemByName);
		if (null != status)
			queryBuf.append(HibernateQry.Qry_subsystemByStatus_STUB);

		Query query = session.createQuery(queryBuf.toString());
		query.setString("name", subsysName);
		if (null != status)
			query.setString("status", status.getName());
		retval.addAll(query.list());

		if (null == hs)
			closeSession(session);

		return (retval);
	}

	/**
	 * Get all Subsystems matching the scopeTree and optional status
	 * @param hs A Hibernate Session. If null, this method will open (and close)
	 * a new Session for the duration of the call
	 * @param scopeTreeId The scope Tree to use
	 * @param status Optional parameter to filter on the Status value
	 * @return A Set of Subsystems matching the scopeTree and optional status,
	 * may be an empty Set (never null)
	 */
	public Set getSubsystemsByScopeTree(Session hs, String scopeTreeId, Status status)
	{
		Set retval = new HashSet();

		Session session = (null != hs) ? hs : openSession();

		StringBuilder queryBuf = new StringBuilder(HibernateQry.Qry_subsystemByScopeTreeId);
		if (null != status)
			queryBuf.append(HibernateQry.Qry_subsystemByStatus_STUB);

		Query query = session.createQuery(queryBuf.toString());
		query.setString("scopetreeid", scopeTreeId);
		if (null != status)
			query.setString("status", status.getName());
		retval.addAll(query.list());

		if (null == hs)
			closeSession(session);

		return (retval);
	}


	public Set findDuplicates(Assignment assignment)
	{
		Query query;
		List resultList;
		Session hs = openSession();
		try
		{
			query = createQuery(hs, HibernateQry.Qry_assignmentDuplicates);
			query.setParameter("granteeKey", assignment.getGrantee().getSubject_PK(), Hibernate.LONG); //$NON-NLS-1$
			query.setParameter("functionKey", ((FunctionImpl)(assignment.getFunction())).getKey(), Hibernate.INTEGER); //$NON-NLS-1$
			query.setString("scopeId", assignment.getScope().getTree().getId()); //$NON-NLS-1$
			query.setString("scopeNodeId", assignment.getScope().getId()); //$NON-NLS-1$
			query.setParameter("assignmentId", assignment.getId(), Hibernate.INTEGER); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			closeSession(hs);
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

		closeSession(hs);
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
			if (subsystemsMatch &&
					!(candidate.getStatus().equals(Status.INACTIVE)) && 
					candidate.getGrantor().equals(proxy.getGrantor()) && 
					(candidate.getId() != null)	&& 
					!(candidate.getId().equals(proxy.getId())))
			{
				duplicates.add(candidate);
			}
		}
		return duplicates;
	}


	/**
	 * Get the Set of parent TreeNodes for the given child
	 * @param hs A Hibernate Session
	 * @param childNode The child TreeNode
	 * @return A Set of parents, may be empty (never null)
	 */
	public Set getParents(Session hs, TreeNode childNode)
	{
		Set parents = new HashSet();
		if (null == childNode)
			return (parents);

		Session session = (null != hs) ? hs : openSession();

		String treeId = ((TreeNodeImpl)childNode).getTreeId();

		List resultList;
		try
		{
			Query query = createQuery(session, HibernateQry.Qry_treeNodeParents);
			query.setString("treeId", treeId); //$NON-NLS-1$
			query.setString("childNodeId", childNode.getId()); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			if (null == hs) // don't put into a 'finally' because hs is used below
				closeSession(session);
			throw new SignetRuntimeException(e);
		}

		Tree tree = getTreeById(session, treeId);
		for (Iterator iter = resultList.iterator(); iter.hasNext(); )
		{
			TreeNodeRelationship tnr = (TreeNodeRelationship)(iter.next());
			parents.add(tree.getNode(tnr.getParentNodeId()));
		}

		if (null == hs)
			closeSession(session);

		return (parents);
	}

	// I really want to do away with this method, having the
	// Tree pick up its parent-child relationships via Hibernate
	// object-mapping. I just haven't figured out how to do that yet.
	public Set getParents(TreeNode childNode)
	{
		Set parents = new HashSet();
		if (null == childNode)
			return (parents);

		Session hs = openSession();
		parents = getParents(hs, childNode);

		closeSession(hs);

		return (parents);
	}


	// I really want to do away with this method, having the
	// Tree pick up its parent-child relationships via Hibernate
	// object-mapping. I just haven't figured out how to do that yet.

	/**
	 * Manually fetch the children TreeNodes of the given parentNode
	 * @param hs A Hibernate Session, for improved performance
	 * @param parentNode the prospective parent
	 * @return A Set of child nodes, may be empty but never null.
	 * @throws SignetRuntimeException If a HibernateException occurs or if the
	 * parent Tree is not found.
	 */
	public Set getChildren(Session hs, TreeNode parentNode)
	{
		Set children = new HashSet();
		if (null == parentNode)
			return (children);

		Session session = (null != hs) ? hs : openSession();

		String treeId = ((TreeNodeImpl)parentNode).getTreeId();

		try
		{
			Query query = createQuery(session, HibernateQry.Qry_treeNodeChildren);
			query.setString("treeId", treeId); //$NON-NLS-1$
			query.setString("parentNodeId", parentNode.getId()); //$NON-NLS-1$
			List<TreeNodeRelationship> result = query.list();

			Tree tree = getTreeById(session, treeId);

			for (TreeNodeRelationship tnr : result)
				children.add(tree.getNode(tnr.getChildNodeId()));
		}
		catch (Exception e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			if (null == hs)
				closeSession(session);
		}

		return children;
	}


	/**
	 * Get a LimitImpl by primary key
	 * @param limit_pk DB primary key
	 * @return The LimitImpl or null
	 */
	public LimitImpl getLimit(int limit_pk)
	{
		LimitImpl retval = null;

		Session hs = openSession();
		Query qry = createQuery(hs, HibernateQry.Qry_limitByPK);
		qry.setInteger("limit_key", limit_pk); //$NON-NLS-1$

		try
		{
			retval = (LimitImpl)qry.list().get(0);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		if (null != retval)
			retval.setSignet(signet);

		return (retval);
	}

	/**
	 * Get a LimitImpl by SubsystemId and LimitId
	 * @param subsysId The Subsystem Id
	 * @param limitId The Limit Id
	 * @return The LimitImpl or null
	 */
	public LimitImpl getLimit(String subsysId, String limitId)
	{
		LimitImpl retval = null;

		Session hs = openSession();
		Query qry = createQuery(hs, HibernateQry.Qry_limitBySubsysAndId);
		qry.setString("subsystemId", subsysId);	//$NON-NLS-1$
		qry.setString("limitId", limitId);		//$NON-NLS-1$

		try
		{
			retval = (LimitImpl)qry.list().get(0);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		if (null != retval)
			retval.setSignet(signet);

		return (retval);
	}

	// I really want to do away with this method, having the Subsystem
	// pick up its associated Limits via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	/**
	 * Return a Map of LimitImpl, keyed by LimitImpl.id, for a given Subsystem
	 * @param subsystem The Subsystem filter
	 * @return A Map of LimitImpl, keyed by LimitImpl.id
	 * @throws SignetRuntimeException If a HibernateException occurs
	 */
	public Map getLimitsBySubsystem(Subsystem subsystem)
	{
		List<LimitImpl> resultList;
		Session hs = openSession();
		try
		{
			Query query = createQuery(hs, HibernateQry.Qry_limitBySubsys);
			query.setString("subsystemId", subsystem.getId()); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		Map limits = new HashMap(resultList.size());
		for (LimitImpl limit : resultList)
		{
			//LimitImpl does not extend EntityImpl, so can't use
			// SignetApiUtil.setEntitysSignetValue()
			limit.setSignet(signet);
			limits.put(limit.getId(), limit);
		}

		return limits;
	}


	/**
	 * Return the Permission matching the given primary key
	 * @param permKey primary key
	 * @return A PermissionImpl or null
	 */
	public PermissionImpl getPermissionById(int permKey)
	{
		return ((PermissionImpl)load(PermissionImpl.class, permKey));
	}

	// I really want to do away with this method, having the Subsystem
	// pick up its associated Permissions via Hibernate object-mapping.
	// I just haven't figured out how to do that yet.
	/**
	 * Return a Map of PermissionImpl, keyed by PermissionImpl.id, for a given Subsystem
	 * @param subsystem The Subsystem filter
	 * @return A Map of PermissionImpl, keyed by PermissionImpl.id
	 * @throws SignetRuntimeException If a HibernateException occurs
	 */
	public Map getPermissionsBySubsystem(Subsystem subsystem)
	{
		List<PermissionImpl> resultList;
		Session hs = openSession();
		try
		{
			Query query = createQuery(hs, HibernateQry.Qry_permissionBySubsys);
			query.setString("subsystemId", subsystem.getId()); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		Map permissions = new HashMap(resultList.size());
		for (PermissionImpl permission : resultList)
			permissions.put(permission.getId(), permission);
		SignetApiUtil.setEntitysSignetValue(permissions.values(), signet);

		return permissions;
	}


	/**
	 * This method is only for use by the native Signet TreeAdapter.
	 * 
	 * @return null if the Tree is not found.
	 * @throws SignetRuntimeException
	 *           if more than one Tree is found.
	 */
	public TreeImpl getNativeSignetTree(String id) throws ObjectNotFoundException
	{
		TreeImpl treeImpl;

		if ((null != cachedTree) && (cachedTree.getId().equals(id)))
			treeImpl = cachedTree;
		else
		{
			Session hs = openSession();
			treeImpl = (TreeImpl)getTreeById(hs, id);
			closeSession(hs);
		}

		if (null == treeImpl.getAdapter())
			treeImpl.setAdapter(new TreeAdapterImpl(signet));

		return treeImpl;
	}


	/**
	 * Gets a single Assignment by ID (DB primary key).
	 * @param id The DB primary key
	 * @return the fetched Assignment object
	 * @throws ObjectNotFoundException
	 */
	public Assignment getAssignment(int id) throws ObjectNotFoundException
	{
		AssignmentImpl assignment = null;
		try
		{
			if (null != (assignment = (AssignmentImpl)(load(AssignmentImpl.class, id))))
				assignment.setSignet(signet);
		}
		catch (org.hibernate.ObjectNotFoundException honfe)
		{
			throw new edu.internet2.middleware.signet.ObjectNotFoundException(honfe);
		}

		return (assignment);
	}

	/**
	 * Get all assignments with optional Status filter
	 * @param status One of: null, active, inactive, pending
	 * @return A Set of AssignmentImpl. If 'status' is null, all Assignments
	 * will be returned. Otherwise, only assignments matching 'status' are
	 * returned. If no matching Assignments are found, the an empty Set is
	 * returned (never null).
	 */
	public Set getAssignments(String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsAllByStatus);
			qry.setString("status", status); //$NON-NLS-1$
		}

		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Assignments granted by the grantor.
	 * @param grantorId The primary key of the assignment grantor
	 * @param status The status of the assignment, if null or empty string, selects _all_
	 * @return A Set of AssignmentImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getAssignmentsGranted(long grantorId, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsGrantedAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsGranted);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setLong("grantorKey", grantorId); //$NON-NLS-1$

		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Assignments granted to the grantee.
	 * @param granteeId The primary key of the assignment grantee
	 * @param status The status of the assignment, if null or empty string, selects _all_
	 * @return A Set of AssignmentImpl objects that have been granted to grantee.
	 * May be an empty set but never null.
	 * @see AssignmentImpl
	 * @see Status
	 */
	public Set getAssignmentsReceived(long granteeId, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsReceivedAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsReceived);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setLong("granteeKey", granteeId); //$NON-NLS-1$

		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Assignments for a given Subsystem and optional Status
	 * @param subsysId The Subsystem primary DB key
	 * @param status Optional Status (e.g. active, inactive, pending). If null,
	 * then all records are returned.
	 * @return A Set of Assignments granted within the given subsystem, or an
	 * empty Set (never null).
	 * @see AssignmentImpl
	 * @see SubsystemImpl
	 * @see Status
	 */
	public Set getAssignmentsBySubsystem(String subsysId, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsBySubsystemAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsBySubsystem);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setString("subsysId", subsysId); //$NON-NLS-1$

		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Assignments for a given Subsystem/Function and optional Status
	 * @param subfunc A pair of Strings representing SubsystemId and FunctionId
	 * @param status Optional Status (e.g. active, inactive, pending). If null,
	 * then all records are returned.
	 * @return A Set of Assignments granted with the given subsystem/function,
	 * or an empty Set (never null).
	 * @see AssignmentImpl
	 * @see FunctionImpl
	 * @see Status
	 */
	public Set getAssignmentsByFunction(String[] subfunc, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsByFunctionAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsByFunction);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setString("functionId", subfunc[1]); //$NON-NLS-1$
		qry.setString("subsysId", subfunc[0]); //$NON-NLS-1$
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	public Set getAssignmentsByScope(String[] scopeAndNodeIds, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_assignmentsByScopeAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_assignmentsByScope);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setString("scopeId", scopeAndNodeIds[0]); //$NON-NLS-1$
		qry.setString("nodeId", scopeAndNodeIds[1]); //$NON-NLS-1$
		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
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
		ProxyImpl proxy;

		if (null != (proxy = (ProxyImpl)(load(ProxyImpl.class, id))))
			proxy.setSignet(signet);

		return (proxy);
	}


	/**
	 * Get the set of all Proxies optionally filtered by status.
	 * @param status The status of the proxy, if null or empty string, selects _all_
	 * @return A Set of ProxyImpl objects.
	 * May be an empty set but never null.
	 */
	public Set getProxies(String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_proxiesAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_proxiesByStatus);
			qry.setString("status", status); //$NON-NLS-1$
		}

		try
		{
			List list = qry.list();
			retval.addAll(list);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Proxies granted by the grantor.
	 * @param grantorId The primary key of the proxy grantor
	 * @param status The status of the proxy, if null or empty string, selects _all_
	 * @return A Set of ProxyImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getProxiesGranted(long grantorId, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_proxiesGrantedAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_proxiesGranted);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setLong("grantorKey", grantorId); //$NON-NLS-1$

		try
		{
			List list = qry.list();
			retval.addAll(list);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}

	/**
	 * Get the set of Proxies granted to the grantee.
	 * @param granteeId The primary key of the proxy grantee
	 * @param status The status of the proxy, if null or empty string, selects _all_
	 * @return A Set of ProxyImpl objects that have been granted to grantee.
	 * May be an empty set but never null.
	 */
	public Set getProxiesReceived(long granteeId, String status)
	{
		Set retval = new HashSet();

		Session hs = openSession();
		Query qry;
		if ((null == status) || (0 >= status.length()))
			qry = createQuery(hs, HibernateQry.Qry_proxiesReceivedAll);
		else
		{
			qry = createQuery(hs, HibernateQry.Qry_proxiesReceived);
			qry.setString("status", status); //$NON-NLS-1$
		}
		qry.setLong("granteeKey", granteeId); //$NON-NLS-1$

		try
		{
			retval.addAll(qry.list());
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			closeSession(hs);
		}

		SignetApiUtil.setEntitysSignetValue(retval, signet);

		return (retval);
	}


	/**
	 * Gets a single Function by primary key.
	 * @param pkey The DB primary key
	 * @return The Function object matching the primary key
	 * @throws ObjectNotFoundException
	 */
	public Function getFunction(int pkey) throws ObjectNotFoundException
	{
		FunctionImpl function;

		if (null != (function = (FunctionImpl)(load(FunctionImpl.class, pkey))))
			function.setSignet(signet);

		return (function);
	}

	/**
	 * Gets a single Function by Function Id and Subsystem Id.
	 * @param functionId The functionId field value to match
	 * @param subsystemId The subsystemId field value to match
	 * @return The Function matching the unique id/subsystem, or null
	 * @throws ObjectNotFoundException
	 */
	public Function getFunction(String functionId, String subsystemId)
			throws ObjectNotFoundException
	{
		Session hs = openSession();

		FunctionImpl retval = (FunctionImpl)getFunction(hs, functionId, subsystemId);

		closeSession(hs);

		return (retval);
	}

	/**
	 * Gets a single Function by Function Id and Subsystem Id.
	 * @param functionId The functionId field value to match
	 * @param subsystemId The subsystemId field value to match
	 * @return The Function matching the unique id/subsystem, or null
	 * @throws ObjectNotFoundException
	 */
	public Function getFunction(Session hs, String functionId, String subsystemId)
			throws ObjectNotFoundException
	{
		FunctionImpl retval = null;

		Session session = (null != hs) ? hs : openSession();

		Query qry = createQuery(session, HibernateQry.Qry_functionByIdAndSubsys);
		qry.setString("function_id", functionId); //$NON-NLS-1$
		qry.setString("subsys_id", subsystemId); //$NON-NLS-1$

		try
		{
			retval = (FunctionImpl)qry.list().get(0);
		}
		catch (HibernateException e)
		{
			throw new SignetRuntimeException(e);
		}
		finally
		{
			if (null == hs)
				closeSession(session);
		}

		if (null != retval)
			retval.setSignet(signet);

		return (retval);
	}

	///////////////////////////////////
	// unit test support
	///////////////////////////////////

 /** for testing only */
 public final ChoiceSet getChoiceSet(String choiceSetId) throws ObjectNotFoundException
 {
   List resultList;

   Session hs = openSession();
   try
   {
     Query query = hs.createQuery(
    		 "from edu.internet2.middleware.signet.ChoiceSetImpl"  //$NON-NLS-1$
              + " as choiceSet"   //$NON-NLS-1$
              + " where choiceSetID = :id");  //$NON-NLS-1$

      query.setString("id", choiceSetId);  //$NON-NLS-1$

      resultList = query.list();
    }
    catch (HibernateException e)
    {
      closeSession(hs);
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

		closeSession(hs);

    	throw new SignetRuntimeException(form.format(formData));
    }
    
    else if (resultList.size() < 1)
    {
		closeSession(hs);
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
		closeSession(hs);
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
		return ((SignetSubject)load(SignetSubject.class, subject_pk));
	}


	/**
	 * Query for a signet_subject that matches the given sourceId/subjectId
	 * @param sourceId The original Source id of the Subject (from SubjectAPI)
	 * @param subjectId The original Subject id of the Subject (from SubjectAPI)
	 * @return A SignetSubject that matches the given sourceId/subjectId or null if not found
	 */
	public SignetSubject getSubject(String sourceId, String subjectId)
	{
		List resultList;
		Session hs = openSession();
		try
		{
//System.out.println("HibernateDB.getSubject: sourceId=" + sourceId + " subjectId=" + subjectId); //$NON-NLS-1$
			Query query = createQuery(hs, HibernateQry.Qry_subjByIdSrc);
			query.setString("subject_id", subjectId); //$NON-NLS-1$
			query.setString("source_id", sourceId); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException e)
		{
			closeSession(hs);
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
				log.warn(msg);
				break;
			case (1):
				retval = (SignetSubject)resultList.iterator().next();
				break;
			default:
				msgData = new Object[] { new Integer(resultList.size()), subjectId, sourceId };
				msgFmt = new MessageFormat(ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_1") + //$NON-NLS-1$
						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_2") + //$NON-NLS-1$
						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_3")); //$NON-NLS-1$
				closeSession(hs);
				throw new SignetRuntimeException(msgFmt.format(msgData));
		}

		closeSession(hs);

		return (retval);
	}


	/**
	 * Get the Subject that matches the identifier (SubjectAuthId DB field)
	 * @param identifier
	 * @return The Signet Subject or null.
	 * @throws ObjectNotFoundException
	 */
	public SignetSubject getSubjectByIdentifier(String identifier)
			throws ObjectNotFoundException
	{
		List resultList;
		Session hs = openSession();
		try
		{
			Query query = createQuery(hs, HibernateQry.Qry_subjectById);
			query.setString("subjIdentifier", identifier); //$NON-NLS-1$
			resultList = query.list();
		}
		catch (HibernateException he)
		{
			closeSession(hs);
			throw new SignetRuntimeException(he);
		}

		SignetSubject retval = null;

		switch (resultList.size())
		{
			case (0):
				closeSession(hs);
				throw new ObjectNotFoundException("object not found"); //$NON-NLS-1$
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
				closeSession(hs);
				throw new SignetRuntimeException(new Integer(resultList.size()).toString());
//				msgData = new Object[] { new Integer(resultList.size()), identifier };
//				msgFmt = new MessageFormat(ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_1") + //$NON-NLS-1$
//						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_2") + //$NON-NLS-1$
//						ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_3")); //$NON-NLS-1$
//				throw new SignetRuntimeException(msgFmt.format(msgData));
		}

		closeSession(hs);
		return (retval);
	}

	/**
	 * Get all Subjects that have the given attribute and the given attribute
	 * value
	 * @param attrName The name of the attribute
	 * @param attrValue The value to match
	 * @return All Subjects that have the given attribute and the given
	 * attribute value, may be an empty Set (never null)
	 */
	public Set getSubjectsByAttributeValue(String attrName, String attrValue)
	{
		HashSet<SignetSubject> retval = new HashSet<SignetSubject>();
		
		Session hs = openSession();
		Query query = createQuery(hs, HibernateQry.Qry_subjectByAttrValue);
		query.setString("attrName", attrName);
		query.setString("attrValue", attrValue);
		List<SignetSubject> resultList = query.list(); //(List<SignetSubject>)query.list();
// returns a List of arrays!
for (Iterator iter = resultList.iterator(); iter.hasNext(); )
{
	Object obj = iter.next();
	if (obj instanceof Object[])
		retval.add((SignetSubject)((Object[])obj)[0]);
	else
		retval.add((SignetSubject)obj);
}
//		retval.addAll(resultList);

		closeSession(hs);

		return (retval);
	}

	/**
	 * Return a Set of all SignetSubjects in the Signet DB, may be an empty
	 * Set but never null
	 * @return A Set of all SignetSubjects in the Signet DB, may be an empty
	 * Set but never null
	 */
	public Set getSubjects()
	{
		HashSet retval = new HashSet();

		Session hs = openSession();
		Query query = createQuery(hs, HibernateQry.Qry_subjectAll);
		List resultList = query.list();

		retval.addAll(resultList);

		closeSession(hs);

		return (retval);
	}

	/**
	 * Get all Persisted Signet Subjects having a matching sourceId.
	 * @param sourceId The SourceId to match
	 * @return A Set of SignetSubjects, may be empty but never null
	 */
	public Set getSubjectsBySourceId(String sourceId)
	{
		HashSet retval = new HashSet();

		Session hs = openSession();
		Query query = createQuery(hs, HibernateQry.Qry_subjBySrc);
		query.setString("source_id", sourceId);
		List resultList = query.list();

		retval.addAll(resultList);

		closeSession(hs);

		return (retval);
	}

}

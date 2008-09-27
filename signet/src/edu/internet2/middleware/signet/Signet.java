/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/Signet.java,v 1.70 2008-09-27 01:02:09 ddonn Exp $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.reconcile.Reconciler;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.subjsrc.PersistedSignetSource;
import edu.internet2.middleware.signet.subjsrc.SignetAppSource;
import edu.internet2.middleware.signet.subjsrc.SignetSource;
import edu.internet2.middleware.signet.subjsrc.SignetSources;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
* This is the factory class for all Signet entities.
*  
*/
public final class Signet implements Serializable
{
	// Signet version
	private static final String version = ResLoaderApp.getString("signet_version"); //$NON-NLS-1$
	 
	// Signet application name
	private static final String appName = Signet.class.getSimpleName();


 /**
  * This constant denotes the default subject-type ID, as it is
  * defined and used by Signet.
  */
 public static final String DEFAULT_SUBJECT_TYPE_ID = "person"; 

 // If I could, I would, by default, set Signet's log to be the same as
 // whatever log was configured for Signet's underlying Hibernate instance.
 // Unfortunately, I can't find a way to get Hibernate to cough up its
 // Log instance. I could conceivably read Hibernate's config file to
 // determine its logging configuration, but I don't want to add that
 // dependency.
 //
 // Likewise, if I could, I would re-set Hibernate's log to be the same as
 // Signet's log whenever Signet.setLog() was called. Unfortunately, I
 // can't find a way to change Hibernate's Log instance at runtime.
 //
 // So, what to do, what to do?
 //
 // By default, Signet will log to stdout. If Signet.setLog() is called,
 // Signet will use that supplied Log instance. This arrangement has 3
 // benefits:
 //
 //   1) This allows simple, naive, command-line-oriented Signet test
 //      programs and other applications to see logging output without
 //      doing anything special, beyond the configuration setup that
 //      Hibernate requires.
 //
 //   2) This allows more sophisticated Signet applications to set up
 //      whatever logging they want.
 //
 //   3) This avoids (so far, at least) the necessity for a Signet-specific
 //      configuration file, by fobbing that responsibility off onto the
 //      enclosing application.
	private static Log		log = LogFactory.getLog(Signet.class);

	/** @return The version of this Signet runtime. */
	public static String getVersion() { return (version); }

	/** @return The application name */
	public static String getAppName() { return (appName); }


// /**
//  * Sets the Log associated with this Signet instance.
//  * 
//  * @param logger
//  */
// public final void setLogger(Log logger)
// {
//   log = logger;
// }
//
// /**
//  * Gets the Log associated with this Signet instance.
//  * 
//  * @return the Log.
//  */
// public final Log getLogger()
// {
//   return (log);
// }

	/**
	 * Formats a scope-tree for display. This method should probably be moved to some new, display-oriented class.
	 * 
	 * @param treeNode The TreeNode whose ancestry is to be displayed.
	 * @param childSeparatorPrefix A String which is to appear before every instance of a child-node (that is, before every TreeNode
	 * except the root).
	 * @param levelPrefix A String which is to appear before every instance of TreeNode which is a child of its immediate
	 * predecessor (that is, between a parent-node and its first child-node).
	 * @param levelSuffix A String which is to appear after every instance of TreeNode which is a parent of its immediate successor
	 * (that is, between a parent-node and its first child-node).
	 * @param childSeparatorSuffix A String which is to appear after every instance of a child-node (that is, after every TreeNode
	 * except the root).
	 * 
	 * @return A String representation of the specified node and its ancestors.
	 * 
	 * @throws ObjectNotFoundException
	 */
	public String displayAncestry(TreeNode treeNode,
			String childSeparatorPrefix, String levelPrefix,
			String levelSuffix, String childSeparatorSuffix)
	{
		StringBuffer display = new StringBuffer();
		Set roots = new HashSet();

		HibernateDB hibr = getPersistentDB();
		Session hs = hibr.openSession();

		buildAncestry(hs, display, (TreeNodeImpl)treeNode,
				childSeparatorPrefix + levelPrefix,
				levelSuffix + childSeparatorSuffix,
				roots);

		hibr.closeSession(hs);

		for (Iterator iter = roots.iterator(); iter.hasNext(); )
		{
			TreeNode root = (TreeNode)(iter.next());
			display.insert(0, root.getName());
		}

		display.insert(0, levelPrefix);
		display.append(levelSuffix);

		return (display.toString());
	}

	/**
	 * This method does not display the roots of the Tree. That is handled by this method's caller.
	 * @param display
	 * @param node
	 * @param prefix
	 * @param suffix
	 * @return a Set of roots of the specified TreeNode.
	 */
	private void buildAncestry(Session hs, StringBuffer display, TreeNodeImpl node,
			String prefix, String suffix, Set roots)
	{
		if (null == node)
			return;

		Set parents = node.getParents(hs);
		if (0 < parents.size())
		{
			display.insert(0, node.getName());
			display.insert(0, prefix);
			display.append(suffix);
			for (Iterator iter = parents.iterator(); iter.hasNext(); )
				buildAncestry(hs, display, (TreeNodeImpl)(iter.next()), 
						prefix, suffix, roots); // recurse
		}
		else
			roots.add(node);
	}


 private Set getRootsOfContainingTrees(Set treeNodes)
 {
   Set roots = new HashSet();

   Iterator treeNodesIterator = treeNodes.iterator();
   while (treeNodesIterator.hasNext())
   {
     TreeNode treeNode = (TreeNode) (treeNodesIterator.next());
     Tree tree = treeNode.getTree();
     ((TreeImpl)tree).setSignet(this);
     roots.addAll(tree.getRoots());
   }

   return roots;
 }

 /**
  * Formats a Tree for display, with special handling of specified nodes, the
  * ancestors of those noes, and the descendants of those nodes.
  * 
  * @param ancestorPrefix The string used to indicate that a node is disabled
  * @param selfPrefix The string used to indicate that a node is enabled
  * @param descendantPrefix The string used to indicate that a parent of the node is enabled
  * @param prefixIncrement 
  * @param infix Terminator of the xxxPrefix
  * @param infixIncrement
  * @param suffix Terminator of the the node
  * @param treeNodesOfInterest The list of enabled nodes
  * @return a String representation of the Tree
  * @throws TreeNotFoundException
  */
 public String printTreeNodesInContext
   (String ancestorPrefix,
    String selfPrefix,
    String descendantPrefix,
    String prefixIncrement,
    String infix,
    String infixIncrement,
    String suffix,
    Set    treeNodesOfInterest)
 {
   StringBuffer scopesDisplay = new StringBuffer();
   Set roots = getRootsOfContainingTrees(treeNodesOfInterest);

   Iterator rootsIterator = roots.iterator();
   while (rootsIterator.hasNext())
   {
     TreeNode root = (TreeNode) (rootsIterator.next());
     printTreeNode(scopesDisplay, ancestorPrefix, selfPrefix,
         descendantPrefix, prefixIncrement, infix, infixIncrement, suffix,
         treeNodesOfInterest, root);
   }

   return scopesDisplay.toString();
 }


 private void printTreeNode(StringBuffer scopesDisplay, String ancestorPrefix,
     String selfPrefix, String descendantPrefix, String prefixIncrement,
     String infix, String infixIncrement, String suffix,
     Set allGrantableScopes, TreeNode treeNode)
 {
   if (treeNode == null)
   {
     return;
   }
   if (allGrantableScopes.contains(treeNode))
     scopesDisplay.append(selfPrefix);
   else if (treeNode.isDescendantOfAny(allGrantableScopes))
		scopesDisplay.append(descendantPrefix);
   else
		scopesDisplay.append(ancestorPrefix);
   
   scopesDisplay.append(treeNode.getScopePath());
   scopesDisplay.append(infix);
   scopesDisplay.append(treeNode.getName());
   scopesDisplay.append(suffix);

   Set children = treeNode.getChildren();
   SortedSet sortedChildren = new TreeSet(children);
   Iterator sortedChildrenIterator = sortedChildren.iterator();
   while (sortedChildrenIterator.hasNext())
   {
     printTreeNode(scopesDisplay, prefixIncrement + ancestorPrefix,
         prefixIncrement + selfPrefix, prefixIncrement + descendantPrefix,
         prefixIncrement, infix + infixIncrement, infixIncrement, suffix,
         allGrantableScopes, (TreeNode) (sortedChildrenIterator.next()));
   }
 }


///////////////////////////////////////////////////////////////////////
//
//			Re-architecture
//
///////////////////////////////////////////////////////////////////////

	protected transient SignetSources signetSources;


	/**
	 * This constructor builds the fundamental Signet factory object. It opens a
	 * Hibernate session, and stores some Signet-specific metadata in that
	 * database if it is not already present.
	 *  
	 */
	public Signet()
	{
		String subjSrcFile = ResLoaderApp.getString("signet.subject.sources");
		signetSources = new SignetSources(subjSrcFile, this);
	}


	/** Get the super Subject (Signet Application Subject) */
	public SignetSubject getSignetSubject()
	{
		SignetSubject retval = getSubject(SignetAppSource.SIGNET_SOURCE_ID,
				SignetAppSource.SIGNET_SUBJECT_ID);

		return (retval);
	}


	/**
	 * Get the SignetSubject that matches the DB primary key from Persisted Store.
	 * @param subject_pk The primary key
	 * @return The matching SignetSubject or null
	 */
	public SignetSubject getSubject(long subject_pk)
	{
		if (null == signetSources)
			return (null);

		SignetSubject retval = signetSources.getSubject(subject_pk);

		return (retval);
	}


	/**
	 * Get a Subject that matches the given subjectId.
	 * This method scans all known SignetSource objects, in order, until a match is found.
	 * Facade method for SignetSource.
	 * @param sourceId The source ID to match
	 * @param subjectId The subject ID to match
	 * @return The matching SignetSubject or null
	 * @see edu.internet2.middleware.subject.Subject#getId()
	 */
	public SignetSubject getSubject(String sourceId, String subjectId)
	{
		if (null == signetSources)
			return (null);

		SignetSubject retval = signetSources.getSubject(sourceId, subjectId);

		return (retval);
	}


	/**
	 * Get a Subject that matches the given identifier.
	 * This method scans all known SignetSource objects, in order, until a match is found.
	 * Facade method for SignetSource.
	 * @param identifier A String that is compared to various fields/attributes (e.g. sunetId)
	 * @return A matching SignetSubject or null
	 */
	public SignetSubject getSubjectByIdentifier(String identifier)
	{
		if (null == signetSources)
			return (null);

		SignetSubject retval = signetSources.getSubjectByIdentifier(identifier);

		return (retval);
	}


	/**
	 * Performs a Search for all Subjects that match the given identifier.
	 * Facade method for SignetSources.
	 * @param identifier A String that is compared to various fields/attributes (e.g. sunetId)
	 * @return A Set of SignetSubjects (never null!)
	 */
	public Set getSubjectsByIdentifier(String identifier)
	{
		if (null == signetSources)
			return (new HashSet());

		Set retval = signetSources.getSubjectsByIdentifier(identifier);

		return (retval);
	}


	/**
	 * Get all Subjects for a given Source.
	 * This method scans the SignetSource matching sourceId, if found.
	 * Facade method for SignetSource.getSubjects().
	 * @param sourceId A String representing the Source to use.
	 * @return A Vector of SignetSubjects, or empty Vector (not null!)
	 * @see SignetSource#getSubjects()
	 *
	 */
	public Vector getSubjectsBySource(String sourceId)
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSubjectsBySource(sourceId);

		return (retval);
	}


	/**
	 * Get all Persisted Signet Subjects having a matching sourceId.
	 * @param sourceId The SourceId to match. Facade method for
	 * HibernateDb.getSubjectsBySourceId()
	 * @return A Set of SignetSubjects, may be empty but never null
	 * @see HibernateDB#getSubjectsBySourceId(String)
	 */
	public Set getSubjectsBySourceId(String sourceId)
	{
		HibernateDB hibr = getPersistentDB();

		Set retval = hibr.getSubjectsBySourceId(sourceId);
		if (null == retval)
			retval = new HashSet();

		return (retval);
	}


	/**
	 * Get all Subjects that match the given Type.
	 * This method scans all known SignetSource objects that are of the given Type.
	 * Facade method for SignetSources.
	 * @param type A String representing the Source type attribute.
	 * @return A Vector of SignetSubjects, or empty Vector (not null!)
	 */
	public Vector getSubjectsByType(String type)
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSubjectsByType(type);

		return (retval);
	}


	/**
	 * Get a Subject, by identifier that matches the given usage.
	 * This method scans all known SignetSource objects that are members of the given usage.
	 * Facade method for SignetSources.
	 * @param usage A String representing the Source usage attribute.
	 * @param identifier A String representing the Subject
	 * @return A SignetSubject, or null
	 */
	public SignetSubject getSubjectByUsage(String usage, String identifier)
	{
		SignetSubject retval;

		if (null == signetSources)
			retval = null;
		else
			retval = signetSources.getSubjectByUsage(usage, identifier);

		return (retval);
	}


	/**
	 * Get all Subjects that match the given usage.
	 * This method scans all known SignetSource objects that are members of the given usage.
	 * Facade method for SignetSources.
	 * @param usage A String representing the Source usage attribute.
	 * @return A Vector of SignetSubjects, or empty Vector (not null!)
	 */
	public Vector getSubjectsByUsage(String usage)
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSubjectsByUsage(usage);

		return (retval);
	}


	/**
	 * Returns a Vector of all known SignetSource objects.
	 * Facade method for SignetSources.getSources().
	 * @return A Vector of known SignetSource objects, or an empty Vector (not null!).
	 * @see SignetSources#getSources()
	 */
	public Vector getSources()
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSources();

		return (retval);
	}


	/**
	 * Returns a Vector of all SignetSource objects that match the given usage.
	 * Facade method for SignetSources
	 * @return Vector of SignetSource objects, or empty Vector (not null!)
	 */
	public Vector getSourcesByUsage(String usage)
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSourcesByUsage(usage);

		return (retval);
	}


	/**
	 * Returns a Vector of all SignetSource objects that match the given Type.
	 * Facade method for SignetSources
	 * @return Vector of SignetSource objects, or empty Vector (not null!)
	 */
	public Vector getSourcesByType(String type)
	{
		Vector retval;

		if (null == signetSources)
			retval = new Vector();
		else
			retval = signetSources.getSourcesByType(type);

		return (retval);
	}


	/**
	 * Get a Source that matches the given sourceId.
	 * Facade method for SignetSources.
	 * @param sourceId The source ID to match
	 * @return The matching SignetSubject or null
	 * @see edu.internet2.middleware.subject.Source#getId()
	 */
	public SignetSource getSource(String sourceId)
	{
		if (null == signetSources)
			return (null);

		SignetSource retval = signetSources.getSource(sourceId);

		return (retval);
	}


	/**
	 * This is considered a low-level method that most applications won't need to deal with.
	 * Facade method for SignetSources.
	 * @return the SourceManager (singleton) associated with this instance of Signet.
	 */
	public SourceManager getSourceManager()
	{
		if (null == signetSources)
			return (null);

		SourceManager retval = signetSources.getSourceManager();

		return (retval);
	}


	/**
	 * This is considered a low-level method that most applications won't need to deal with.
	 * Facade method for SignetSources.
	 * @return the PersistedSignetSource associated with this instance of Signet.
	 */
	public PersistedSignetSource getPersistedSource()
	{
		if (null == signetSources)
			return (null);

		PersistedSignetSource retval = signetSources.getPersistedSource();

		return (retval);
	}


	/**
	 * Get a reference to the persisted store manager to allow direct access to DB
	 * @return The persisted store manager
	 */
	public HibernateDB getPersistentDB()
	{
		return (signetSources.getPersistedSource().getPersistedStoreMgr());
	}


	/** Wrapper for persisted store method */
	public Set getSubsystems()
	{
		return (getPersistentDB().getSubsystems());
	}


	/**
	 * Facade method for Reconciler.reconcile()
	 */
	public Set reconcile(Date date)
	{
//TODO may want to create a new instance of HibernateDB instead of using the one belonging to PersistedSignetSource
		Reconciler recon = new Reconciler(this, getPersistentDB());
		Set retval = recon.reconcile(date);
		return (retval);
	}

}

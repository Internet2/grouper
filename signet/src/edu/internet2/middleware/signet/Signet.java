/*--
$Id: Signet.java,v 1.60 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.sources.SignetSubjectSources;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
* This is the factory class for all Signet entities.
*  
*/
public final class Signet
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

 // This constant should probably end up in some sort of
 // Tree-specific presentation class adapter, and should probably
 // be private when it moves there.
 static final String           SCOPE_PART_DELIMITER = ":"; 

 private static final String   DEFAULT_TREE_ADAPTER_NAME =
	 	ResLoaderApp.getString("Signet.app.defaultTreeAdapterName"); //$NON-NLS-1$

 private static final String    DEFAULT_CHOICE_SET_ADAPTER_NAME =
	 	ResLoaderApp.getString("Signet.app.defaultChoiceSetAdapterName"); //$NON-NLS-1$

 /**
  * This constant denotes the "first name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
// not used
// public static final String    ATTR_FIRSTNAME = "~firstname"; 

 /**
  * This constant denotes the "middle name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
// not used
// public static final String    ATTR_MIDDLENAME = "~middlename"; 

 /**
  * This constant denotes the "last name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
// not used
// public static final String    ATTR_LASTNAME = "~lastname"; 

 /**
  * This constant denotes the "display ID" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
// not used
// public static final String    ATTR_DISPLAYID = "~displayid"; 


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

 private Logger                logger;

 private PrivilegedSubject signetSubject;

	/** maps Signet Attribute names to those defined in the Source/Subject adapter */
	protected SignetSubjectSources signetSubjectSources;

	/** reference to DB persistence */
	protected HibernateDB persistDB;

	public static String getVersion() { return (version); }
	public static String getAppName() { return (appName); }


 /**
  * This constructor builds the fundamental Signet factory object. It opens a
  * Hibernate session, and stores some Signet-specific metadata in that
  * database if it is not already present.
  *  
  */
 public Signet()
 {
   super();

   HibernateDB.setSignet(this);
   persistDB = HibernateDB.getInstance();

   String subjSrcFile = ResLoaderApp.getString("signet.subject.sources");
   signetSubjectSources = new SignetSubjectSources(this, subjSrcFile);

   logger = Logger.getLogger(this.toString());
 }
 

 /**
  * Creates a new Category.
  * 
  * @param subsystem
  *          The {@link Subsystem}which contains this {@link Category}.
  * @param id
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Category}.
  */
 public final Category newCategory(Subsystem subsystem, String id,
     String name, Status status)
 {
   Category category = new CategoryImpl((SubsystemImpl) subsystem, id, name,
       status);
   subsystem.add(category);

   return category;
 }

 /**
  * Sets the Log associated with this Signet instance.
  * 
  * @param logger
  */
 public final void setLogger(Logger logger)
 {
   this.logger = logger;
 }

 /**
  * Gets the Log associated with this Signet instance.
  * 
  * @return the Log.
  */
 public final Logger getLogger()
 {
   return this.logger;
 }

 /**
  * Creates a new Function.
  * 
  * @param category
  *          The {@link Category}which contains this {@link Function}.
  * @param id
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Category}.
  * @param helpText
  *          A prose description which will appear in help-text and other
  *          explanatory materials.
  */
 public Function newFunction(Category category, String id, String name,
     Status status, String helpText)
 {
   Function newFunction = new FunctionImpl(this, category, id, name, helpText,
       status);

   ((SubsystemImpl) (category.getSubsystem())).add(newFunction);

   ((CategoryImpl) category).add(newFunction);

   return newFunction;
 }

 /**
  * Creates a new Subsystem.
  * 
  * @param id
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param helpText
  *          A prose description which will appear in help-text and other
  *          explanatory materials.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Subsystem}.
  */
 public final Subsystem newSubsystem(String id, String name, String helpText,
     Status status)
 {
   return new SubsystemImpl(this, id, name, helpText, status);
 }


// not used
// /**
//  * Creates a new Tree, using the default Signet TreeAdapter.
//  * 
//  * @param treeId
//  * @param treeName
//  * @return the new Tree
//  */
// public final Tree newTree(String treeId, String treeName)
// {
//   TreeAdapter defaultTreeAdapter
//    = getTreeAdapter(DEFAULT_TREE_ADAPTER_NAME);
//   Tree newTree
//    = new TreeImpl(this, defaultTreeAdapter, treeId, treeName);
//   return newTree;
// }

// not used
// /**
//  * Creates a new Tree.
//  * 
//  * @param adapter
//  * @param treeId
//  * @param treeName
//  * @return the new Tree
//  */
// public final Tree newTree(TreeAdapter adapter, String treeId,
//     String treeName)
// {
//   Tree newTree = new TreeImpl(this, adapter, treeId, treeName);
//   return newTree;
// }


// not used
// /**
//  * Gets all of the Trees in the Signet database. Should probably be changed to
//  * return a type-safe Collection.
//  * 
//  * @return an array of all of the {@link Tree}s in the Signet database. Never
//  *         returns null: in the case of zero {@link Tree}s, this method will
//  *         return a zero-length array.
//  */
// public Tree[] getTrees()
// {
//   List resultList = persistDB.find("from edu.internet2.middleware.signet.TreeImpl as tree"); 
//
//   Object[] objectArray = resultList.toArray();
//
//   TreeImpl[] treeImplArray = new TreeImpl[objectArray.length];
//
//   for (int i = 0; i < objectArray.length; i++)
//   {
//     treeImplArray[i] = (TreeImpl) (objectArray[i]);
//   }
//
//   return treeImplArray;
// }


// not used
// /**
//  * Gets all PrivilegedSubjects. Should probably be changed to return a
//  * type-safe Collection.
//  * 
//  * @return a List of all of the {@link PrivilegedSubject}s accessible to
//  *         Signet, including those who have no privileges. Never returns null:
//  *         in the case of zero {@link PrivilegedSubject}s, this method will
//  *         return an empty List.
//  */
// public List getPrivilegedSubjects()
// {
//   try
//   {
//    List privilegedSubjects = persistDB.find("from edu.internet2.middleware.signet.PrivilegedSubject"); 
//    return privilegedSubjects;
//   }
//   catch (SignetRuntimeException e)
//   {
//     throw new SignetRuntimeException(
//         ResLoaderApp.getString("Signet.msg.exc.privSubj"), e); //$NON-NLS-1$
//   }
//
// }


// not used
// // I really want to do away with this method, having the Subsystem
// // pick up its associated Functions via Hibernate object-mapping. I just
// // haven't figured out how to do that yet.
// Set getFunctionsBySubsystem(Subsystem subsystem)
// {
//   Query query;
//   List resultList;
//
//   try
//   {
//     query = persistDB.createQuery(
//    		 "from edu.internet2.middleware.signet.FunctionImpl" 
//             + " as function" + " where subsystemID = :id");  
//
//     query.setString("id", subsystem.getId()); 
//
//     resultList = query.list();
//   }
//   catch (HibernateException e)
//   {
//     throw new SignetRuntimeException(e);
//   }
//
//   Set resultSet = new HashSet(resultList);
//
//   Iterator resultSetIterator = resultSet.iterator();
//   while (resultSetIterator.hasNext())
//   {
//     Function function = (Function) (resultSetIterator.next());
//     ((FunctionImpl) function).setSignet(this);
//   }
//
//   return resultSet;
// }


// not used
// // I really want to do away with this method, having the Subsystem
// // pick up its associated ChoiceSets via Hibernate object-mapping.
// // I just haven't figured out how to do that yet.
// Set getChoiceSetsBySubsystem(Subsystem subsystem)
// {
//   Query query;
//   List resultList;
//
//   try
//   {
//     query = persistDB.createQuery(
//    		 "from edu.internet2.middleware.signet.ChoiceSetImpl" 
//            + " as choiceSet where subsystemID = :id"); 
//
//     query.setString("id", subsystem.getId()); 
//
//     resultList = query.list();
//   }
//   catch (HibernateException e)
//   {
//     throw new SignetRuntimeException(e);
//   }
//
//   Set choiceSets = new HashSet(resultList.size());
//
//   Iterator resultListIterator = resultList.iterator();
//   while (resultListIterator.hasNext())
//   {
//     ChoiceSet choiceSet = (ChoiceSet)(resultListIterator.next());
//     ((ChoiceSetImpl)choiceSet).setSignet(this);
//     choiceSets.add(choiceSet);
//   }
//
//   return choiceSets;
// }


 /**
  * Creates a new Subsystem.
  * 
  * @return the new Subsystem
  */
 public Subsystem newSubsystem(String id, String name, String helpText)
 {
   return new SubsystemImpl(this, id, name, helpText, Status.PENDING);
 }

 
 /**
  * Creates a new Permission.
  * 
  * @param subsystem the Subsystem which will contain the new Permission.
  * @param id the ID of the new Permission.
  * @param status the Status of the new Permission.
  * @return the new Permission
  */
 public Permission newPermission
  (Subsystem subsystem, String id, Status status)
 {
   Permission newPermission
    = new PermissionImpl
        ((SubsystemImpl) subsystem, id, status);
   
   ((SubsystemImpl)subsystem).add(newPermission);

   return newPermission;
 }


  public PrivilegedSubject getSignetSubject()
  {
    if (null == signetSubject)
    {
      SubjectImpl underlyingSubject = new SubjectImpl();

      try
      {
        signetSubject = persistDB.fetchPrivilegedSubject(SubjectTypeEnum.APPLICATION.getName(),
        		underlyingSubject.getId());
        ((PrivilegedSubjectImpl)signetSubject).setSubject(underlyingSubject);
      }
      catch (ObjectNotFoundException onfe)
      {
        this.signetSubject = new PrivilegedSubjectImpl(this, underlyingSubject);
      }
    }

    return (signetSubject);
  }


 /**
  * Gets a single TreeNode by treeID and nodeID, using the default Signet
  * TreeAdapter.
  * 
  * @param treeId
  * @param treeNodeId
  * @return the specified TreeNode
  * @throws ObjectNotFoundException
  */

 public TreeNode getTreeNode(String treeId, String treeNodeId)
     throws ObjectNotFoundException
 {
   TreeAdapter adapter = this
       .getTreeAdapter(DEFAULT_TREE_ADAPTER_NAME);
   return getTreeNode(adapter, treeId, treeNodeId);
 }

 /**
  * Gets a single TreeNode by adapter, treeID and nodeID.
  * 
  * @param adapter
  * @param treeId
  * @param treeNodeId
  * @return the specified TreeNode
  * @throws ObjectNotFoundException
  */
 public TreeNode getTreeNode(TreeAdapter adapter, String treeId,
     String treeNodeId) throws ObjectNotFoundException
 {
   Tree tree = null;

   try
   {
     tree = adapter.getTree(treeId);
   }
   catch (TreeNotFoundException tnfe)
   {
     throw new ObjectNotFoundException(tnfe);
   }

   TreeNode treeNode = tree.getNode(treeNodeId);

   return treeNode;
 }

 /**
  * Gets a single TreeNode identified by a scope-string. The format of that
  * scopeString is currently subject to change. and will be documented after it
  * is finalized.
  * 
  * @param scopeString
  * @return the specified TreeNode
  * @throws ObjectNotFoundException
  */
 public TreeNode getTreeNode(String scopeString)
     throws ObjectNotFoundException
 {
   int firstDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER);
   int secondDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER,
       firstDelimIndex + SCOPE_PART_DELIMITER.length());

   String treeAdapterName = scopeString.substring(0, firstDelimIndex);
   String treeId = scopeString.substring(firstDelimIndex
       + SCOPE_PART_DELIMITER.length(), secondDelimIndex);
   String treeNodeId = (scopeString.substring(secondDelimIndex
       + SCOPE_PART_DELIMITER.length()));

   TreeAdapter adapter = getTreeAdapter(treeAdapterName);
   return getTreeNode(adapter, treeId, treeNodeId);
 }

 /**
  * This method loads the named ChoiceSetAdapter class, instantiates
  * it using its parameterless constructor, and passes back the new
  * instance.
  * 
  * @param adapterName The fully-qualified class-name of the
  *     ChoiceSetAdapter.
  * @return the new ChoiceSetAdapter.
  */
 public ChoiceSetAdapter getChoiceSetAdapter(String adapterName)
 {
   ChoiceSetAdapter adapter
    = (ChoiceSetAdapter)
        (loadAndCheckAdapter
            (adapterName, ChoiceSetAdapter.class, "ChoiceSet")); 
  
   if (adapter instanceof ChoiceSetAdapterImpl)
   {
     ((ChoiceSetAdapterImpl) (adapter)).setSignet(this);
   }
   
   return adapter;
 }
 
 /**
  * @param className
  * @param requiredInterface
  * @param adapterTargetName e,g. "Tree" or "Limit"
  * @return
  */
 private Object loadAndCheckAdapter
  (String className,
   Class  requiredInterface,
   String adapterTargetName)
 {
   Object adapter;
   Class  actualClass = null;

   try
   {
     actualClass = Class.forName(className);
   }
   catch (ClassNotFoundException cnfe)
   {
	   Object[] msgData = new Object[] {
			   adapterTargetName,
			   className
	   };
	   MessageFormat msg = new MessageFormat(
			   ResLoaderApp.getString("Signet.msg.exc.noAdapter_1") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.noAdapter_2") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.noAdapter_3")); //$NON-NLS-1$
     throw new SignetRuntimeException(msg.format(msgData), cnfe);
   }
   
   if (!classImplementsInterface(actualClass, requiredInterface))
   {
	   Object[] msgData = new Object[] {
			   adapterTargetName,
			   className,
			   requiredInterface.getName()
	   };
	   MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.adaptNotImpl_1") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptNotImpl_2") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptNotImpl_3") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptNotImpl_4")); //$NON-NLS-1$
	   
     throw new SignetRuntimeException(msg.format(msgData));
   }
   
   try
   {
     adapter = actualClass.newInstance();
   }
   catch (Exception e)
   {
	   Object[] msgData = new Object[] {
			   adapterTargetName,
			   className
	   };
	   MessageFormat msg = new MessageFormat(
			   ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_1") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_2") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_3") + //$NON-NLS-1$
			   ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_4")); //$NON-NLS-1$
	   
     throw new SignetRuntimeException(msg.format(msgData), e);
   }
   
   return adapter;
 }

 /**
  * @param actualClass
  * @param requiredInterface
  * @return
  */
 private boolean classImplementsInterface
  (Class actualClass,
   Class requiredInterface)
 {
   Class[] implementedInterfaces = actualClass.getInterfaces();
   for (int i = 0; i < implementedInterfaces.length; i++)
   {
     if (implementedInterfaces[i].equals(requiredInterface))
     {
       return true;
     }
   }

   // If we've gotten this far, the actualClass does not
   // implement the requiredInterface.
   return false;
 }

 /**
  * This method loads the named TreeAdapter class, instantiates it
  * using its parameterless constructor, and passes back the new
  * instance.
  * 
  * @param adapterName The fully-qualified class-name of the
  *     TreeAdapter.
  * @return the new TreeAdapter.
  */
 public TreeAdapter getTreeAdapter(String adapterName)
 {
   TreeAdapter adapter
    = (TreeAdapter)
        (loadAndCheckAdapter
          (adapterName, TreeAdapter.class, "Tree")); 

   if (adapter instanceof TreeAdapterImpl)
   {
     ((TreeAdapterImpl) (adapter)).setSignet(this);
   }

   return adapter;
 }


// not used
// /**
//  * This method loads the named HTMLLimitRenderer class, instantiates
//  * it using its parameterless constructor, and passes back the new
//  * instance.
//  * 
//  * @param rendererName The fully-qualified class-name of the
//  *     HTMLLimitRenderer.
//  * @return the new HTMLLimitRenderer.
//  */
// HTMLLimitRenderer getHTMLLimitRenderer(String rendererName)
// {
//   HTMLLimitRenderer renderer
//    = (HTMLLimitRenderer)
//        (loadAndCheckAdapter
//          (rendererName,
//           HTMLLimitRenderer.class,
//           "HTMLLimitRenderer")); 
//
//   if (renderer instanceof HTMLLimitRendererImpl)
//   {
//     ((HTMLLimitRendererImpl) (renderer)).setSignet(this);
//   }
//
//   return renderer;
// }

 /**
  * Formats a scope-tree for display. This method should probably be
  * moved to some new, display-oriented class.
  * 
  * @param treeNode
  *     The TreeNode whose ancestry is to be displayed.
  * @param childSeparatorPrefix
  *     A String which is to appear before every instance of a
  *     child-node (that is, before every TreeNode except the root).
  * @param levelPrefix
  *     A String which is to appear before every instance of TreeNode
  *    which is a child of its immediate predecessor (that is,
  *    between a parent-node and its first child-node).
  * @param levelSuffix
  *    A String which is to appear after every instance of TreeNode
  *    which is a parent of its immediate successor (that is,
  *    between a parent-node and its first child-node).
  * @param childSeparatorSuffix
  *    A String which is to appear after every instance of a
  *    child-node (that is, after every TreeNode except the root).
  * 
  * @return
  *   A String representation of the specified node and its
  *  ancestors.
  * 
  * @throws ObjectNotFoundException
  */
 public String displayAncestry
  (TreeNode treeNode,
   String   childSeparatorPrefix,
    String  levelPrefix,
    String  levelSuffix,
    String  childSeparatorSuffix)
 {
   StringBuffer display = new StringBuffer();
   Set roots = new HashSet();

   buildAncestry
    (display,
     treeNode,
     childSeparatorPrefix + levelPrefix,
     levelSuffix + childSeparatorSuffix,
     roots);
   
   Iterator rootsIterator = roots.iterator();
   while (rootsIterator.hasNext())
   {
     TreeNode root = (TreeNode)(rootsIterator.next());
     display.insert(0, root.getName());  
   }  
   
   display.insert(0, levelPrefix);
   display.append(levelSuffix);
   
   return display.toString();
 }

 /**
  * 
  * @param display
  * @param node
  * @param prefix
  * @param suffix
  * @return a Set of roots of the specified TreeNode.
  */
 private void buildAncestry
   (StringBuffer  display,
    TreeNode      node,
    String        prefix,
    String        suffix,
    Set         roots)
 {
   if (node.getParents().size() == 0)
   {
     // This method does not display the roots of the Tree.
     // That is handled by this method's caller.
     roots.add(node);
     return;
   }

   display.insert(0, node.getName());
   display.insert(0, prefix);
   display.append(suffix);

   Set parents = node.getParents();
   Iterator parentsIterator = parents.iterator();
   while (parentsIterator.hasNext())
   {
     buildAncestry
      (display,
       (TreeNode)(parentsIterator.next()),
       prefix,
        suffix,
        roots);
   }
 }

// /**
//  * Gets a single Subsystem by ID.
//  * 
//  * @param id
//  * @return the specified Subsystem
//  */
// public Subsystem getSubsystem(String id) throws ObjectNotFoundException
// {
//   SubsystemImpl subsystemImpl;
//   
//   if (id == null)
//   {
//     throw new IllegalArgumentException(ResLoaderApp.getString("Signet.msg.exc.subsysIds")); //$NON-NLS-1$
//   }
//   
//   if (id.length() == 0)
//   {
//     throw new IllegalArgumentException(ResLoaderApp.getString("Signet.msg.exc.subsysIdIs0")); //$NON-NLS-1$
//   }
//
//   try
//   {
//     subsystemImpl = (SubsystemImpl) (persistDB.load(SubsystemImpl.class, id));
//   }
//   catch (ObjectNotFoundException onfe)
//   {
//	   Object[] msgData = new Object[] { id };
//	   MessageFormat msg = new MessageFormat(
//			   ResLoaderApp.getString("Signet.msg.exc.subsysNotFound")); //$NON-NLS-1$
//     throw new ObjectNotFoundException(msg.format(msgData), onfe);
//   }
//
//   subsystemImpl.setSignet(this);
//   return subsystemImpl;
// }


// not used
// /**
//  * Normalizes a Subject attribute-value. Should this method be public?
//  * 
//  * @param value
//  *          The Value of a Signet Subject attribute.
//  * 
//  * @return the normalized version of the attribute value. This is the original
//  *         value shifted to all lower-case, and with all punctuation marks
//  *         removed.
//  */
// public String normalizeSubjectAttributeValue(String value)
// {
//   int valueLen = value.length();
//   StringBuffer normalized = new StringBuffer(valueLen);
//
//   for (int i = 0; i < valueLen; i++)
//   {
//     char currentChar = value.charAt(i);
//     if (Character.isLetterOrDigit(currentChar))
//     {
//       normalized.append(currentChar);
//     }
//     else
//       if (Character.isWhitespace(currentChar))
//       {
//         normalized.append(currentChar);
//       }
//   }
//
//   return new String(normalized);
// }

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
  * @param ancestorPrefix
  * @param selfPrefix
  * @param descendantPrefix
  * @param prefixIncrement
  * @param infix
  * @param infixIncrement
  * @param suffix
  * @param treeNodesOfInterest
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


// not used
// /**
//  * Formats a Tree for display. This method should probably be moved to some
//  * new, display-oriented class.
//  * 
//  * @param ancestorPrefix
//  * @param selfPrefix
//  * @param descendantPrefix
//  * @param prefixIncrement
//  * @param infix
//  * @param infixIncrement
//  * @param followingLine
//  * @param tree
//  * @return a String representation of the Tree
//  * @throws TreeNotFoundException
//  */
// public String printTree(String ancestorPrefix, String selfPrefix,
//     String descendantPrefix, String prefixIncrement, // gets PREpended to
//     // prefix
//     String infix, String infixIncrement, // gets APpended to infix
//     String followingLine, Tree tree)
// {
//   StringBuffer treeDisplay = new StringBuffer();
//
//   if (tree != null)
//   {
//     Set roots = tree.getRoots();
//     Iterator rootsIterator = roots.iterator();
//     while (rootsIterator.hasNext())
//     {
//       TreeNode root = (TreeNode) (rootsIterator.next());
//       Set allTreeNodes = root.getTree().getTreeNodes();
//
//       printTreeNode(treeDisplay, ancestorPrefix, selfPrefix,
//           descendantPrefix, prefixIncrement, infix, infixIncrement,
//           followingLine, allTreeNodes, root);
//     }
//   }
//
//   return treeDisplay.toString();
// }

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
   {
     scopesDisplay.append(selfPrefix);
   }
   else
     if (treeNode.isAncestorOfAll(allGrantableScopes))
     {
       scopesDisplay.append(ancestorPrefix);
     }
     else
     {
       scopesDisplay.append(descendantPrefix);
     }

   scopesDisplay.append(treeNode);
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


 /**
  * @param helpText
  * @return the new Limit
  */
 public Limit newLimit
  (Subsystem  subsystem,
   String     id,
   DataType   dataType,
   ChoiceSet  choiceSet,
   String     name,
   int        displayOrder,
   String     helpText,
   Status     status,
   String     renderer)
 {
   Limit limit
    = new LimitImpl
        (this,
         subsystem,
         id,
         dataType,
         choiceSet,
         name,
         displayOrder,
         helpText,
         status,
         renderer);
   
   subsystem.add(limit);
   
   return limit;
 }

 /**
  * Creates a new ChoiceSet, using the default Signet
  * ChoiceSetAdapter.
  * 
  * @param subsystem
  * @param id
  * @return the new ChoiceSet
  */
 public final ChoiceSet newChoiceSet
  (Subsystem  subsystem,
   String     id)
 {
   ChoiceSetAdapter defaultChoiceSetAdapter
    = getChoiceSetAdapter(DEFAULT_CHOICE_SET_ADAPTER_NAME);
   ChoiceSet newChoiceSet
    = new ChoiceSetImpl
        (this, subsystem, defaultChoiceSetAdapter, id);
   
   return newChoiceSet;
 }


// not used
// /**
//  * @return the new ChoiceSet
//  */
// public ChoiceSet newChoiceSet
//  (Subsystem        subsystem,
//   ChoiceSetAdapter choiceSetAdapter,
//   String           id)
// {
//   ChoiceSet choiceSet
//    = new ChoiceSetImpl(this, subsystem, choiceSetAdapter, id);
//   
//   return choiceSet;
// }
 


// not used
// Set getExtensibleProxies
//   (PrivilegedSubject proxyGrantor,
//    PrivilegedSubject proxyGrantee)
// {
//   Set proxies = proxyGrantee.getProxiesReceived();
//   proxies = PrivilegedSubjectImpl.filterProxies(proxies, Status.ACTIVE);
//   proxies
//     = PrivilegedSubjectImpl.filterProxiesByGrantor(proxies, proxyGrantor);
//   
//   Set extensibleProxies = new HashSet();
//   
//   Iterator proxiesIterator = proxies.iterator();
//   while (proxiesIterator.hasNext())
//   {
//     Proxy proxy = (Proxy)(proxiesIterator.next());
//     if (proxy.canExtend())
//     {
//       extensibleProxies.add(proxy);
//     }
//   }
//   
//   return extensibleProxies;
// }


// not used
// boolean encompassesSubsystem(Set proxies, Subsystem subsystem)
// {
//   Iterator proxiesIterator = proxies.iterator();
//   while (proxiesIterator.hasNext())
//   {
//     Proxy proxy = (Proxy)(proxiesIterator.next());
//     
//     // If any of our candidate Proxies has a NULL Subsystem, than it
//     // encompasses every possible Subsystem.
//     if (proxy.getSubsystem() == null)
//     {
//       return true;
//     }
//
//     if (proxy.getSubsystem().equals(subsystem))
//     {
//       return true;
//     }
//   }
//   
//   // If we've gotten this far, none of our Proxies can do the job.
//   return false;
// }
 
 
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
  public Set reconcile()
  {
    Date now = new Date();
    return this.reconcile(now);
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
      Query assignmentQuery = persistDB.createQuery(
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
    
    Set   changedGrantables = new HashSet();
    reconcileGrantables(assignmentResultList, date, changedGrantables);
   
    List  proxyResultList;
    try
    {
      Query proxyQuery = persistDB.createQuery(
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
    
    reconcileGrantables(proxyResultList, date, changedGrantables);
    
    return changedGrantables;
  }
  
  void reconcileGrantables
    (Collection grantables,
     Date       date,
     Set        changedGrantables)
  {
    Iterator grantablesIterator = grantables.iterator();
    while (grantablesIterator.hasNext())
    {
      Grantable grantable = (Grantable)(grantablesIterator.next());
      
      // We got these Grantables from a query, so they may not have their
      // Signet members set yet.
      ((GrantableImpl)grantable).setSignet(this);
      
      if (grantable.evaluate(date))
      {
        grantable.save();
        changedGrantables.add(grantable);
      }
    }    
  }


	public HibernateDB getPersistentDB()
	{
		return (persistDB);
	}


	public SignetSubjectSources getSubjectSources()
	{
		return (signetSubjectSources);
	}


}

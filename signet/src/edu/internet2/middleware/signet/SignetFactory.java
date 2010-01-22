/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/SignetFactory.java,v 1.1 2006-10-25 00:08:28 ddonn Exp $

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
package edu.internet2.middleware.signet;

import java.text.MessageFormat;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;

/**
 * Factory class for performing various Signet-related tasks
 */
public class SignetFactory
{
	public static final String		DEFAULT_CHOICE_SET_ADAPTER_NAME =
	 	ResLoaderApp.getString("Signet.app.defaultChoiceSetAdapterName"); //$NON-NLS-1$

	public static final String		DEFAULT_TREE_ADAPTER_NAME =
		ResLoaderApp.getString("Signet.app.defaultTreeAdapterName"); //$NON-NLS-1$

	// This constant should probably end up in some sort of
	// Tree-specific presentation class adapter, and should probably
	// be private when it moves there.
	public static final String	SCOPE_PART_DELIMITER = ":"; 


	/**
	 * Creates a new Category.
	 * @param subsystem The {@link Subsystem}which contains this {@link Category}.
	 * @param id A short mnemonic code which will appear in XML documents and other documents used by analysts.
	 * @param name A descriptive name which will appear in UIs and documents exposed to users.
	 * @param status The {@link Status}that should be initially assigned to this {@link Category}.
	 */
	public static Category newCategory(Subsystem subsystem, String id, String name, Status status)
	{
		Category category = new CategoryImpl((SubsystemImpl)subsystem, id, name, status);
		subsystem.add(category);

		return (category);
	}

	/**
	 * Creates a new Function.
	 * @param signet A refernce to Signet
	 * @param category The {@link Category}which contains this {@link Function}.
	 * @param id A short mnemonic code which will appear in XML documents and other
	 *          documents used by analysts.
	 * @param name A descriptive name which will appear in UIs and documents exposed
	 *          to users.
	 * @param status The {@link Status}that should be initially assigned to this
	 *          {@link Category}.
	 * @param helpText A prose description which will appear in help-text and other
	 *          explanatory materials.
	 */
	public static Function newFunction(
			Signet signet,
			Category category,
			String id,
			String name,
			Status status,
			String helpText)
	{
		Function newFunction = new FunctionImpl(signet, category, id, name, helpText, status);
		((SubsystemImpl)(category.getSubsystem())).add(newFunction);
		((CategoryImpl)category).add(newFunction);

		return (newFunction);
	}


	/**
	 * Creates a new Subsystem.
	 * @param signet A refernce to Signet
	 * @param id A short mnemonic code which will appear in XML documents and other
	 *          documents used by analysts.
	 * @param name A descriptive name which will appear in UIs and documents exposed
	 *          to users.
	 * @param helpText A prose description which will appear in help-text and other
	 *          explanatory materials.
	 * @param status The {@link Status}that should be initially assigned to this
	 *          {@link Subsystem}.
	 */
	public static Subsystem newSubsystem(
			Signet signet,
			String id,
			String name,
			String helpText,
			Status status)
	{
		return new SubsystemImpl(signet, id, name, helpText, status);
	}


	/**
	 * Creates a new Subsystem.
	 * @param signet A refernce to Signet
	 * @param id A short mnemonic code which will appear in XML documents and other
	 *          documents used by analysts.
	 * @param name A descriptive name which will appear in UIs and documents exposed
	 *          to users.
	 * @param helpText A prose description which will appear in help-text and other
	 *          explanatory materials.
	 */
	public static Subsystem newSubsystem(Signet signet, String id, String name, String helpText)
	{
		return newSubsystem(signet, id, name, helpText, Status.PENDING);
	}

 
	/**
	 * Creates a new Permission.
	 * @param subsystem the Subsystem which will contain the new Permission.
	 * @param id the ID of the new Permission.
	 * @param status the Status of the new Permission.
	 * @return the new Permission
	 */
	public static Permission newPermission(Subsystem subsystem, String id, Status status)
	{
		Permission newPermission = new PermissionImpl((SubsystemImpl)subsystem, id, status);
		((SubsystemImpl)subsystem).add(newPermission);
		return newPermission;
	}


	/**
	 * Create a new Limit
	 * @param signet Refernce to Signet
	 * @param subsystem The subsystem
	 * @param id The ID
	 * @param dataType The data type
	 * @param choiceSet The ChoiceSet
	 * @param name The name
	 * @param displayOrder The display order
	 * @param helpText The help text
	 * @param status The status
	 * @param renderer The renderer
	 * @return The new Limit
	 */
	public static Limit newLimit(Signet signet, Subsystem subsystem, String id,
			DataType dataType, ChoiceSet choiceSet, String name, int displayOrder,
			String helpText, Status status, String renderer)
	{
		Limit limit = new LimitImpl(signet, subsystem, id, dataType, choiceSet,
				name, displayOrder, helpText, status, renderer);
		subsystem.add(limit);
		return limit;
	}

	/**
	 * Creates a new ChoiceSet, using the given ChoiceSetAdapter.
	 * @param signet Reference to Signet
	 * @param choiceSetAdapter The ChoiceSetAdapter
	 * @param subsystem
	 * @param id
	 * @return the new ChoiceSet
	 */
	public static ChoiceSet newChoiceSet(
			Signet signet,
			ChoiceSetAdapter choiceSetAdapter,
			Subsystem subsystem,
			String id)
	{
		return new ChoiceSetImpl(signet, subsystem, choiceSetAdapter, id);
	}


	/**
	 * Gets a single TreeNode by treeID and nodeID, using the default Signet TreeAdapter.
	 * @param treeId
	 * @param treeNodeId
	 * @return the specified TreeNode
	 * @throws ObjectNotFoundException
	 */
	public static TreeNode getTreeNode(Signet signet, String treeId, String treeNodeId)
			throws ObjectNotFoundException
	{
		TreeAdapter adapter = getTreeAdapter(signet, DEFAULT_TREE_ADAPTER_NAME);
		return getTreeNode(signet, adapter, treeId, treeNodeId);
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
	public static TreeNode getTreeNode(Signet signet,
				TreeAdapter adapter, String treeId, String treeNodeId)
			throws ObjectNotFoundException
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
	public static TreeNode getTreeNode(Signet signet, String scopeString)
			throws ObjectNotFoundException
	{
		int firstDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER);
		int secondDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER, firstDelimIndex + SCOPE_PART_DELIMITER.length());
		String treeAdapterName = scopeString.substring(0, firstDelimIndex);
		String treeId = scopeString.substring(firstDelimIndex + SCOPE_PART_DELIMITER.length(), secondDelimIndex);
		String treeNodeId = (scopeString.substring(secondDelimIndex + SCOPE_PART_DELIMITER.length()));
		TreeAdapter adapter = getTreeAdapter(signet, treeAdapterName);
		return getTreeNode(signet, adapter, treeId, treeNodeId);
	}

	/**
	 * This method loads the named TreeAdapter class, instantiates it using its
	 * parameterless constructor, and passes back the new instance.
	 * @param adapterName The fully-qualified class-name of the TreeAdapter.
	 * @return the new TreeAdapter.
	 */
	public static TreeAdapter getTreeAdapter(Signet signet, String adapterName)
	{
		TreeAdapter adapter = (TreeAdapter)(loadAndCheckAdapter(adapterName, TreeAdapter.class, "Tree"));
		if (adapter instanceof TreeAdapterImpl)
		{
			((TreeAdapterImpl)(adapter)).setSignet(signet);
		}
		return adapter;
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
	public static ChoiceSetAdapter getChoiceSetAdapter(Signet signet, String adapterName)
	{
		ChoiceSetAdapter adapter =
			(ChoiceSetAdapter)(loadAndCheckAdapter(adapterName, ChoiceSetAdapter.class, "ChoiceSet"));
		if (adapter instanceof ChoiceSetAdapterImpl)
		{
			((ChoiceSetAdapterImpl)(adapter)).setSignet(signet);
		}
		return adapter;
	}

	/**
	 * @param className
	 * @param requiredInterface
	 * @param adapterTargetName e,g. "Tree" or "Limit"
	 * @return
	 */
	private static Object loadAndCheckAdapter(String className, Class requiredInterface, String adapterTargetName)
	{
		Object adapter;
		Class actualClass = null;
		try
		{
			actualClass = Class.forName(className);
		}
		catch (ClassNotFoundException cnfe)
		{
			Object[] msgData = new Object[] { adapterTargetName, className };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.noAdapter_1") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.noAdapter_2") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.noAdapter_3")); //$NON-NLS-1$
			throw new SignetRuntimeException(msg.format(msgData), cnfe);
		}
		//   if (!classImplementsInterface(actualClass, requiredInterface))
		if (!(requiredInterface.isAssignableFrom(actualClass)))
		{
			Object[] msgData = new Object[] { adapterTargetName, className, requiredInterface.getName() };
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
			Object[] msgData = new Object[] { adapterTargetName, className };
			MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_1") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_2") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_3") + //$NON-NLS-1$
					ResLoaderApp.getString("Signet.msg.exc.adaptConstructor_4")); //$NON-NLS-1$
			throw new SignetRuntimeException(msg.format(msgData), e);
		}
		return adapter;
	}
}

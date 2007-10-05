/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/ScopeTreeXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

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
package edu.internet2.middleware.signet.util.xml.adapter;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetFactory;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.TreeNodeImpl;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeXb;
import edu.internet2.middleware.signet.util.xml.binder.TreeNodeImplXb;

/**
 * ScopeTreeXa 
 * 
 */
public class ScopeTreeXa extends EntityImplXa
{

	/**
	 * default constructor
	 */
	public ScopeTreeXa()
	{
	}

	/**
	 * constructor
	 * @param signet the Signet instance
	 */
	public ScopeTreeXa(Signet signet)
	{
		super(signet);
	}

	/**
	 * Create a Scope Tree Adapter given the Signet TreeImpl
	 * @param signetScopeTree the signet TreeImpl
	 * @param signet an instance of Signet
	 */
	public ScopeTreeXa(TreeImpl signetScopeTree, Signet signet)
	{
		signetEntity = signetScopeTree;
		xmlEntity = new ObjectFactory().createScopeTreeXb();
		setValues(signetScopeTree, signet);
	}

	/**
	 * Create a Scope Tree Adapter given the ScopeTreeXb XML binder
	 * @param xmlScopeTree The XML binder
	 * @param signet An instance of Signet
	 */
	public ScopeTreeXa(ScopeTreeXb xmlScopeTree, Signet signet)
	{
		xmlEntity = xmlScopeTree;
		signetEntity = new TreeImpl();
		setValues(xmlScopeTree, signet);
	}

	/**
	 * Return the Signet TreeImpl for this adapter
	 * @return The Signet TreeImpl for this adapter
	 */
	public TreeImpl getSignetScopeTree()
	{
		return ((TreeImpl)signetEntity);
	}

	/**
	 * Return the XML binder for this adapter
	 * @return The XML binder for this adapter
	 */
	public ScopeTreeXb getXmlScopeTree()
	{
		return ((ScopeTreeXb)xmlEntity);
	}


	/**
	 * Set the values in the XML binder from the TreeImpl
	 * @param signetScopeTree The Signet TreeImpl
	 * @param signet An instance of Signet
	 */
	public void setValues(TreeImpl signetScopeTree, Signet signet)
	{
		this.signet = signet;
		setValues(signetScopeTree);
	}

	/**
	 * Set the values in the XML binder from the TreeImpl
	 * @param signetScopeTree The Signet TreeImpl
	 */
	public void setValues(TreeImpl signetScopeTree)
	{
		super.setValues(signetScopeTree);

		ScopeTreeXb xmlScopeTree = (ScopeTreeXb)xmlEntity;

//	private TreeAdapter adapter;
		TreeAdapter ta = signetScopeTree.getAdapter();
		if (null != ta)
			xmlScopeTree.setAdapterClassName(ta.getClass().getName());

//	private Set nodes;
		List<TreeNodeImplXb> xmlNodes = xmlScopeTree.getRootNodes();
		for (TreeNodeImpl root : (Set<TreeNodeImpl>)signetScopeTree.getRoots())
			addXmlNodes(root, xmlNodes);
	}

	/**
	 * Recursively traverse the TreeNodeImpl hierarchy, creating XML binders for each
	 * @param signetTreeNode A Signet TreeNodeImpl
	 * @param parentXmlNodes The List of TreeNodeImplXb children for the parent
	 */
	protected void addXmlNodes(TreeNodeImpl signetTreeNode, List<TreeNodeImplXb> parentXmlNodes)
	{
		// add the XML node for the incoming signet node
		TreeNodeImplXb xmlTreeNode = new TreeNodeImplXa(signetTreeNode, signet).getXmlTreeNodeImpl();
		parentXmlNodes.add(xmlTreeNode);

		// recursively add its children
		List<TreeNodeImplXb> xmlChildren = xmlTreeNode.getChildren();
		for (TreeNodeImpl signetChild : (Set<TreeNodeImpl>)signetTreeNode.getChildren())
			addXmlNodes(signetChild, xmlChildren);
	}


	/**
	 * Set the values in the TreeImpl from the XML binder
	 * @param xmlScopeTree The XML binder ScopeTree
	 * @param signet An instance of Signet
	 */
	public void setValues(ScopeTreeXb xmlScopeTree, Signet signet)
	{
		this.signet = signet;
		setValues(xmlScopeTree);
	}

	/**
	 * Set the values in the TreeImpl from the XML binder
	 * @param xmlScopeTree The XML binder ScopeTree
	 */
	public void setValues(ScopeTreeXb xmlScopeTree)
	{
		super.setValues(xmlScopeTree);

		TreeImpl signetTree = (TreeImpl)signetEntity;

//	private TreeAdapter adapter;
		String taName = xmlScopeTree.getAdapterClassName();
		TreeAdapter ta = SignetFactory.getTreeAdapter(signet, taName);
		signetTree.setAdapter(ta);
		
//	private Set nodes;
		// get all nodes in hierarchy, may contain duplicate nodes
		Set<TreeNodeImpl> tmpNodeSet = new HashSet<TreeNodeImpl>();
		for (TreeNodeImplXb xmlNode : xmlScopeTree.getRootNodes())
			addSignetNodes(xmlNode, tmpNodeSet);

		// create a Hashtable to eliminate duplicate nodes, based on node id
		Hashtable<String, TreeNodeImpl> tmpHashtable = new Hashtable<String, TreeNodeImpl>();
		for (TreeNodeImpl signetNode : tmpNodeSet)
			tmpHashtable.put(signetNode.getId(), signetNode);

		// Note: Signet's TreeImpl keeps a flat list of all TreeNodeImpls, so
		// the same list may be added to, unlike when creating the hierarchy
		// of TreeNodeImplXb objects during XML export
		Set<TreeNodeImpl> signetNodes = signetTree.getNodes();
		signetNodes.addAll(tmpHashtable.values());
	}

	/**
	 * Recursively traverse the TreeNodeImplXb hierarchy, creating Signet TreeNodeImpls for each
	 * @param xmlNode An XML binder TreeNodeImplXb
	 * @param parentSignetNodes The Set of TreeNodeImpls for the Signet TreeImpl (a flat list, not a hierarchy!)
	 */
	protected void addSignetNodes(TreeNodeImplXb xmlNode, Set<TreeNodeImpl> parentSignetNodes)
	{
		// add the Signet node for the incoming XML node
		TreeNodeImpl signetNode = new TreeNodeImplXa(xmlNode, signet).getSignetTreeNodeImpl();
		parentSignetNodes.add(signetNode);

		// recursively add its children
		for (TreeNodeImplXb xmlChild : xmlNode.getChildren())
			addSignetNodes(xmlChild, parentSignetNodes);
	}

}

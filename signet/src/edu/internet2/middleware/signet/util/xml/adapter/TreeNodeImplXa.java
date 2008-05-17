/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/TreeNodeImplXa.java,v 1.3 2008-05-17 20:54:09 ddonn Exp $

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

import java.util.List;
import java.util.Set;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.TreeNodeImpl;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.TreeNodeImplXb;

/**
 * TreeNodeImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a TreeNodeImpl and a TreeNodeImplXb.
 * @see TreeNodeImpl
 * @see TreeNodeImplXb
 */
public class TreeNodeImplXa extends EntityImplXa
{
	/**
	 * Default constructor
	 */
	public TreeNodeImplXa()
	{
		super();
	}

	/**
	 * Simple constructor that only initializes the Signet reference
	 * @param signet An instance of Signet
	 */
	public TreeNodeImplXa(Signet signet)
	{
		super(signet);
	}

	/**
	 * Constructor - Intialize this adapter for a given TreeNodeImpl, and
	 * initialize an XML binder for it.
	 * NOTE that this will recurse down the hierarchy of all child TreeNodeImpls
	 * for the given TreeNodeImpl. The most effective use would be to pass a
	 * root TreeNodeImpl.
	 * @param signetTreeNode The Signet TreeNodeImpl
	 * @param signet An instance of Signet
	 */
	public TreeNodeImplXa(TreeNodeImpl signetTreeNode, Signet signet)
	{
		this(signet);
		signetEntity = signetTreeNode;
		xmlEntity = new ObjectFactory().createTreeNodeImplXb();
		setValues(signetTreeNode, signet);
	}

	/**
	 * Constructor - Initialize this adapter for a given TreeNodeImplXb, and
	 * initialize a Signet TreeNode for it.
	 * NOTE that this will recurse down the hierarchy of all child
	 * TreeNodeImplXbs for the given TreeNodeImplXb. The most effective use
	 * would be to pass a root TreeNodeImplXb.
	 * @param xmlTreeNode The XML binder
	 * @param signet An instance of Signet
	 */
	public TreeNodeImplXa(TreeNodeImplXb xmlTreeNode, Signet signet)
	{
		this(signet);
		xmlEntity = xmlTreeNode;
		signetEntity = new TreeNodeImpl();
		setValues(xmlTreeNode, signet);
	}

	/**
	 * @return The Signet TreeNodeImpl
	 */
	public TreeNodeImpl getSignetTreeNodeImpl()
	{
		return ((TreeNodeImpl)signetEntity);
	}

	/**
	 * Initialize the XML binder (previously created) from the Signet
	 * TreeNodeImpl
	 * @param signetTreeNodeImpl The Signet TreeNodeImpl
	 * @param signet An instance of Signet
	 */
	public void setValues(TreeNodeImpl signetTreeNodeImpl, Signet signet)
	{
		this.signet = signet;
		setValues(signetTreeNodeImpl);
	}

	/**
	 * Initialize the XML binder (previously created) from the Signet
	 * TreeNodeImpl.
	 * NOTE that this will recurse down the hierarchy of all child TreeNodeImpls
	 * for the given TreeNodeImpl. The most effective use would be to pass a
	 * root TreeNodeImpl.
	 * @param signetTreeNodeImpl The Signet TreeNodeImpl
	 */
	public void setValues(TreeNodeImpl signetTreeNodeImpl)
	{
		super.setValues(signetTreeNodeImpl);

		TreeNodeImplXb xmlTreeNodeImpl = (TreeNodeImplXb)xmlEntity;
		// get the (empty) list of xml child nodes
		List<TreeNodeImplXb> xmlChildren = xmlTreeNodeImpl.getOrganization();

		// set the child nodes
		for (TreeNodeImpl child : (Set<TreeNodeImpl>)signetTreeNodeImpl.getChildren())
			xmlChildren.add(new TreeNodeImplXa(child, signet).getXmlTreeNodeImpl());

		// set the nodeType
		xmlTreeNodeImpl.setType(signetTreeNodeImpl.getNodeType());
	}


	/**
	 * @return The XML binder
	 */
	public TreeNodeImplXb getXmlTreeNodeImpl()
	{
		return ((TreeNodeImplXb)xmlEntity);
	}

	/**
	 * Initialize the Signet TreeNodeImpl (previously created) from the XML
	 * binder.
	 * @param xmlTreeNodeImpl The XML binder TreeNodeImplXb
	 * @param signet An instance of Signet
	 */
	public void setValues(TreeNodeImplXb xmlTreeNodeImpl, Signet signet)
	{
		this.signet = signet;
		setValues(xmlTreeNodeImpl);
	}

	/**
	 * Initialize the Signet TreeNodeImpl (previously created) from the XML
	 * binder.
	 * NOTE that this will recurse down the hierarchy of all child
	 * TreeNodeImplXbs for the given TreeNodeImplXb. The most effective use
	 * would be to pass a root TreeNodeImplXb.
	 * @param xmlTreeNodeImpl The XML binder TreeNodeImplXb
	 */
	public void setValues(TreeNodeImplXb xmlTreeNodeImpl)
	{
		super.setValues(xmlTreeNodeImpl);

		TreeNodeImpl signetTreeNode = (TreeNodeImpl)signetEntity;

		// iterate through the list of xml child nodes
		for (TreeNodeImplXb xmlChild : xmlTreeNodeImpl.getOrganization())
			signetTreeNode.addChild(new TreeNodeImplXa(xmlChild, signet).getSignetTreeNodeImpl());

		// set the nodeType
		signetTreeNode.setNodeType(xmlTreeNodeImpl.getType());
	}


}

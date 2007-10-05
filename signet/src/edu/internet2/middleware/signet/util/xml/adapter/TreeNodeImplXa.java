/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/TreeNodeImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.TreeNodeImpl;
import edu.internet2.middleware.signet.util.xml.binder.TreeNodeImplXb;

/**
 * TreeNodeImplXa 
 * 
 */
public class TreeNodeImplXa extends EntityImplXa
{
	public TreeNodeImplXa()
	{
	}

	public TreeNodeImplXa(Signet signet)
	{
	}

	public TreeNodeImplXa(TreeNodeImpl signetTreeNode, Signet signet)
	{
	}

	public TreeNodeImplXa(TreeNodeImplXb xmlTreeNode, Signet signet)
	{
	}

	public TreeNodeImpl getSignetTreeNodeImpl()
	{
		return ((TreeNodeImpl)signetEntity);
	}

	public TreeNodeImplXb getXmlTreeNodeImpl()
	{
		return ((TreeNodeImplXb)xmlEntity);
	}

	public void setValues(TreeNodeImpl signetTreeNodeImpl, Signet signet)
	{
		this.signet = signet;
		setValues(signetTreeNodeImpl);
	}

	public void setValues(TreeNodeImpl signetTreeNodeImpl)
	{
		super.setValues(signetTreeNodeImpl);
//TODO implement setValues
	}


	public void setValues(TreeNodeImplXb xmlTreeNodeImpl, Signet signet)
	{
		this.signet = signet;
		setValues(xmlTreeNodeImpl);
	}

	public void setValues(TreeNodeImplXb xmlTreeNodeImpl)
	{
		super.setValues(xmlTreeNodeImpl);
//TODO implement setValues
	}


}

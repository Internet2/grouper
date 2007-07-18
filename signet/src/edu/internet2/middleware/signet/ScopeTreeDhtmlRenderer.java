/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/ScopeTreeDhtmlRenderer.java,v 1.1 2007-07-18 17:24:39 ddonn Exp $

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
package edu.internet2.middleware.signet;

/**
 * ScopeTreeDhtmlRenderer 
 * 
 * @version $Revision: 1.1 $
 * @author $Author: ddonn $
 */
public class ScopeTreeDhtmlRenderer
{
	protected ScopeTreeModel model;


	public ScopeTreeDhtmlRenderer(ScopeTreeModel model, TreeNodeImpl enabledNode)
	{
		setModel(model);
	}


	/**
	 * @return the model
	 */
	public ScopeTreeModel getModel()
	{
		return (model);
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(ScopeTreeModel model)
	{
		this.model = model;
	}


	//////////////////////////////
	// overrides Object
	//////////////////////////////


	public String toString()
	{
		return new String();
	}

}

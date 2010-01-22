/*
$Id: ITypeSafeEnum.java,v 1.1 2006-04-04 23:32:58 ddonn Exp $
$Date: 2006-04-04 23:32:58 $

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
package edu.internet2.middleware.signet;

import java.io.Serializable;

public interface ITypeSafeEnum extends Serializable
{
	/**
	 * Return the external name associated with this instance.
	 * @return the name by which this instance is identified in code.
	 */
	public String getName();

	/**
	 * Return the description associated with this instance.
	 * @return the human-readable description by which this instance is
	 * 	identified in the user interface.
	 */
	public String getHelpText();

}

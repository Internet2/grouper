/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/SignetApiUtil.java,v 1.2 2007-12-06 01:18:32 ddonn Exp $

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

import java.util.Collection;

/**
 * SignetApiUtil - Utilities for the Signet API
 */
public class SignetApiUtil
{

	/**
	 * Assuming each item in the Set extends EntityImpl, set its Signet value. 
	 * @param entities A Set of Entity objects
	 */
	public static void setEntitysSignetValue(Collection<EntityImpl> entities, Signet signet)
	{
		if (null == entities)
			return;

		// make sure each entity knows its Signet instance
		for (EntityImpl entity : entities)
			entity.setSignet(signet);
	}



}

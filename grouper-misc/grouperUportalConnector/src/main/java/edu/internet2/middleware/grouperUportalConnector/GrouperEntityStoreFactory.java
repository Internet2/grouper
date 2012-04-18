/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * Copyright 2010 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperUportalConnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IEntityStoreFactory;

/**
 * Factory for the EntityStore.
 * 
 * @author Bill Brown
 * 
 */
public class GrouperEntityStoreFactory implements IEntityStoreFactory {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(IEntityStoreFactory.class);

	/**
	 * Creates an instance.
	 * 
	 * @return The instance.
	 * @see org.jasig.portal.groups.IEntityStoreFactory#newEntityStore()
	 */
	public IEntityStore newEntityStore() throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating New Grouper GrouperEntityGroupStoreFactory");
		}
		return (IEntityStore) new GrouperEntityGroupStoreFactory()
				.newGroupStore();
	}
}

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
import org.jasig.portal.groups.ComponentGroupServiceDescriptor;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityGroupStoreFactory;

/**
 * Factory for the GrouperEntityGroupStore.
 * 
 * @author Bill Brown
 * 
 */
public class GrouperEntityGroupStoreFactory implements IEntityGroupStoreFactory {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(GrouperEntityGroupStoreFactory.class);

	private static IEntityGroupStore groupStore;

	/**
	 * returns the instance of GrouperEntityGroupStore.
	 * 
	 * @return The instance.
	 * @throws GroupsException
	 *             if there is an error
	 * @see org.jasig.portal.groups.IEntityGroupStoreFactory #newGroupStore()
	 */
	public static synchronized IEntityGroupStore getGroupStore() {
		if (groupStore == null) {
			groupStore = new GrouperEntityGroupStore();
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("returning IEntityGroupStore: " + groupStore);
		}
		return groupStore;
	}

	/**
	 * returns the instance of GrouperEntityGroupStore.
	 * 
	 * @return The instance.
	 * @throws GroupsException
	 *             if there is an error
	 * @see org.jasig.portal.groups.IEntityGroupStoreFactory #newGroupStore()
	 */
	public IEntityGroupStore newGroupStore() throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating New Grouper IEntityGroupStore");
		}
		return getGroupStore();
	}

	/**
	 * Construction with parameters.
	 * 
	 * @param svcDescriptor
	 *            The parameters.
	 * @return The instance.
	 * @throws GroupsException
	 *             if there is an error
	 * @see org.jasig.portal.groups.IEntityGroupStoreFactory
	 *      #newGroupStore(org.jasig.portal.groups.ComponentGroupServiceDescriptor)
	 */
	public IEntityGroupStore newGroupStore(
			ComponentGroupServiceDescriptor svcDescriptor)
			throws GroupsException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating New Grouper IEntityGroupStore");
		}
		return getGroupStore();
	}

}

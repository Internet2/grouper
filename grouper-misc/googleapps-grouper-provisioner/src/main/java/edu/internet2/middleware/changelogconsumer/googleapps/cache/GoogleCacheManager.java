/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.changelogconsumer.googleapps.cache;

import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.User;

/**
 * ObjectCache stores objects retrieved from Google to save on the number of API round trips required.
 * Each object type has its own cache and expiration interval. The ObjectCache object (static) to maintain the cache
 * between the ChangeLogConsumer object's (prototype) life cycling.
 *
 * @author John Gasper, Unicon
 */
public class GoogleCacheManager {
    private static Cache<User> googleUsers = new Cache<User>();
    private static Cache<Group> googleGroups = new Cache<Group>();

    private static final Object usersLock = new Object();
    private static final Object groupsLock = new Object();

    /**
     *
     * @return a Google User cache
     */
    public static Cache<User> googleUsers() {
        synchronized (usersLock) {
            return googleUsers;
        }
    }

    /**
     *
     * @return a Google Group cache
     */
    public static Cache<Group> googleGroups() {
        synchronized (groupsLock) {
            return googleGroups;
        }
    }

}

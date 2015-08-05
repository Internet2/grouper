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

package edu.internet2.middleware.changelogconsumer.googleapps.utils;

import edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsFullSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RecentlyManipulatedObjectsList tracks objects that have been recently manipulated on Google so that a delay can be
 * introduced if that object is manipulated again in the near tear.
 *
 * .add(item) should be called immediately after an object is manipulated (created, deleted, etc)
 * .delayIfNeeded(item) should be called immediately before an object is called.
 */
public class RecentlyManipulatedObjectsList {
    private static final Logger LOG = LoggerFactory.getLogger(RecentlyManipulatedObjectsList.class);

    private LinkedHashMap<String, String> queue;
    private final int queueSize;
    private long delay;

    public RecentlyManipulatedObjectsList(int size, int delay) {
        this.queueSize = size;
        this.delay = delay * 1000;

        queue = new LinkedHashMap<String, String>(this.queueSize, 1)
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest)
            {
                return this.size() > queueSize;
            }
        };
    }

    public void add(String item){
        queue.put(item, item);
        LOG.trace("Adding item {}", item);
    }

    public boolean delayIfNeeded(String item) {
        if (queue.containsKey(item)) {
            try {
                LOG.trace("Item {} found, sleeping for {} milliseconds then removing.", item, delay);
                Thread.sleep(delay);
            } catch (InterruptedException e) {

            }

            queue.remove(item);

            return true;

        } else {
            return false;
        }
    }

    public void clear() {
        queue.clear();
    }
}



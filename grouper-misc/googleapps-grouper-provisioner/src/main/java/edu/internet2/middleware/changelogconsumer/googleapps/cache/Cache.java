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
import edu.internet2.middleware.subject.Subject;
import org.joda.time.DateTime;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * CacheObject supports Google User, Google Group, Grouper Subject, and Grouper Group objects.
 *
 * * @author John Gasper, Unicon
 */
public class Cache<T> {
    private Hashtable<String, T> cache = new Hashtable<String, T>();
    private DateTime cachePopulatedTime;
    private int cacheValidity = 30;

    public T get(String id) {
        return cache.get(id);
    }

    public void clear() {
        cache.clear();
    }

    public void put(T item) {
        cache.put(getId(item), item);
    }

    public void remove (String id) {
        if (cache.containsKey(id)) {
            cache.remove(id);
        }
    }

    public void seed(int size) {
        cache = new Hashtable<String, T>(size);
    }

    public void seed(List<T> items) {
        cache = new Hashtable<String, T>(items.size() + 100);

        if (items == null) {
            seed(100);
        } else {
            for (T item : items) {
                cache.put(getId(item), item);
            }

            cachePopulatedTime = new DateTime();
        }
    }

    public int size() {
        return cache == null ? 0 : cache.size();
    }

    private String getId(T item) {
        if (item.getClass().equals(User.class)) {
            return ((User) item).getPrimaryEmail();
        } else if (item.getClass().equals(Group.class)) {
            return ((Group) item).getEmail();
        } else if (item.getClass().equals(Subject.class)) {
            return ((Subject) item).getSourceId() + "__" + ((Subject) item).getId();
        } else if (item.getClass().equals(edu.internet2.middleware.grouper.Group.class)) {
            return ((edu.internet2.middleware.grouper.Group) item).getName();
        } else {
            return item.toString();
        }
    }

    public void setCacheValidity(int minutes){
        cacheValidity = minutes;
    }

    public DateTime getExpiration() {
        return cachePopulatedTime != null ? cachePopulatedTime.plusMinutes(cacheValidity) : null;
    }

    public boolean isExpired() {
        return cachePopulatedTime == null || getExpiration().isBeforeNow();
    }

    public Set<String> getKeySet() {
        return cache.keySet();
    }
}
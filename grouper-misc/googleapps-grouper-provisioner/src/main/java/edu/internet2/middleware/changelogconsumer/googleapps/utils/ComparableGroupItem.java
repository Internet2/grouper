/*******************************************************************************
 * Copyright 2015 Internet2
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
package edu.internet2.middleware.changelogconsumer.googleapps.utils;

import edu.internet2.middleware.grouper.Group;

/**
 * Is a Google named index with a back link to the Grouper group object. This allows for set comparisons with Google group objects.
 *
 * @author John Gasper, Unicon
 */
public class ComparableGroupItem {
    private String name;
    private Group grouperGroup;

    public ComparableGroupItem(String name) {
        this.name = name;
    }

    public ComparableGroupItem(String name, Group group) {
        this.name = name;
        this.grouperGroup = group;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == ComparableGroupItem.class && name.hashCode() == obj.hashCode();
    }

    public Group getGrouperGroup() {
        return grouperGroup;
    }
}

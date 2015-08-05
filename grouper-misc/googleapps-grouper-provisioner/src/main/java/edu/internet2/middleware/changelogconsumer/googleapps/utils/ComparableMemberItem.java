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

import edu.internet2.middleware.grouper.Member;

/**
 * Is a Google named index with a back link to the Grouper subject object. This allows for set comparisons with Google user objects.
 *
 * @author John Gasper, Unicon
 */
public class ComparableMemberItem {
    private String email;
    private Member grouperMember;

    public ComparableMemberItem(String email) {
        this.email = email;
    }

    public ComparableMemberItem(String name, Member member) {
        this.email = name;
        this.grouperMember = member;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return email;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == ComparableMemberItem.class && email.hashCode() == obj.hashCode();
    }

    public Member getGrouperMember() {
        return grouperMember;
    }
}

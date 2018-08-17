/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.util;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Object to represent value in the map.
 */
public class EmailObject {
  
  private String groupId;
  private String groupName;
  private Set<String> ccEmails;
  
  public EmailObject(String groupId, String groupName, Set<String> ccEmails) {
    this.groupId = groupId;
    this.groupName = groupName;
    this.ccEmails = ccEmails;
  }
  
  public String getGroupId() {
    return groupId;
  }

  
  public String getGroupName() {
    return groupName;
  }
  
  public Set<String> getCcEmails() {
    return ccEmails;
  }

  @Override
  public int hashCode() {
   return new HashCodeBuilder()
   .append(groupId)
   .append(groupName)
   .append(ccEmails)
   .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EmailObject other = (EmailObject) obj;
  
    return new EqualsBuilder()
        .append(this.groupId, other.groupId)
        .append(this.groupName, other.groupName)
        .append(this.ccEmails, other.ccEmails)
        .isEquals();
  }

  @Override
  public String toString() {
    return "EmailObject [groupId=" + groupId + ", groupName=" + groupName
        + ", ccEmails=" + ccEmails + "]";
  }
  
}

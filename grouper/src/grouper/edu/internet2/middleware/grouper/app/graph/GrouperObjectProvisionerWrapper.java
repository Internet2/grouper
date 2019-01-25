/**
 * Copyright 2018 Internet2
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.app.graph;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

public class GrouperObjectProvisionerWrapper implements GrouperObject {

  private String id;

  /**
   * Creates a dummy provisioner object as a GrouperObject implementation
   * so that it can be used as the contents of a {@link GraphNode}. The
   * name, id, and displayName all return the same string.
   *
   * @param id The
   */
  public GrouperObjectProvisionerWrapper(String id) {
    this.id = id;
  }

  /**
   * see if this object matches the filter strings
   *
   * @param filterStrings
   * @return true if matches
   */
  @Override
  public boolean matchesLowerSearchStrings(Set<String> filterStrings) {
    for (String filterString : GrouperUtil.nonNull(filterStrings)) {
      //if all dont match, return false
      if (!getId().toLowerCase().contains(filterString)) {
        return false;
      }

    }
    return true;
  }

  /**
   * returns the id as a name
   *
   * @return the id of this object
   */
  @Override
  public String getName() {
    return id;
  }

  /**
   * description of object, not implemented
   *
   * @return description
   */
  @Override
  public String getDescription() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * returns the id as the displayName
   *
   * @return id of the object
   */
  @Override
  public String getDisplayName() {
    return id;
  }

  /**
   * gets stem this is in, not implemented
   *
   * @return the stem
   */
  @Override
  public Stem getParentStem() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Compares two provisioners and considers them equal if
   * they have the same id
   *
   * @param obj provisioner to compare
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GrouperObjectProvisionerWrapper other = (GrouperObjectProvisionerWrapper) obj;

    return new EqualsBuilder()
      .append(this.getId(), other.getId())
      .isEquals();
  }

  /**
   * Computes the hash code based on the id field
   *
   * @return object hash code
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getId())
      .toHashCode();
  }

  /**
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append("id", this.getId())
      .toString();
  }
}

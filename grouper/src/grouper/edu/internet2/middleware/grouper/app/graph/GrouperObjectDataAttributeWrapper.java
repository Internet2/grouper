package edu.internet2.middleware.grouper.app.graph;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

/**
 * Pseudo GrouperObject representing a data attribute reference in an ABAC/jexl script,
 * so that it can be used as the contents of a {@link GraphNode} for visualization.
 */
public class GrouperObjectDataAttributeWrapper implements GrouperObject {

  private String id;
  private String displayLabel;

  /**
   * @param id unique identifier for this attribute reference (e.g. "data_attr:org:123")
   * @param displayLabel human-readable label (e.g. "org = '123'")
   */
  public GrouperObjectDataAttributeWrapper(String id, String displayLabel) {
    this.id = id;
    this.displayLabel = displayLabel;
  }

  @Override
  public boolean matchesLowerSearchStrings(Set<String> filterStrings) {
    for (String filterString : GrouperUtil.nonNull(filterStrings)) {
      if (!displayLabel.toLowerCase().contains(filterString)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getName() {
    return displayLabel;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getDisplayName() {
    return displayLabel;
  }

  @Override
  public Stem getParentStem() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GrouperObjectDataAttributeWrapper other = (GrouperObjectDataAttributeWrapper) obj;
    return new EqualsBuilder().append(this.getId(), other.getId()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.getId()).toHashCode();
  }

  @Override
  public String toString() {
    return "GrouperObjectDataAttributeWrapper[" + displayLabel + "]";
  }
}

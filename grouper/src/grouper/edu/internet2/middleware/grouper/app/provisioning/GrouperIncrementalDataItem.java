package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GrouperIncrementalDataItem {

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
  
  public GrouperIncrementalDataItem() {
    super();
    // TODO Auto-generated constructor stub
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        // dont worry about the millisSince1970, actions should be added in order
        .append(this.item)
        .append(this.grouperIncrementalDataAction).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GrouperIncrementalDataItem)) {
      return false;
    }
    GrouperIncrementalDataItem grouperIncrementalDataItem = (GrouperIncrementalDataItem)obj;
    return new EqualsBuilder()
        // dont worry about the millisSince1970, actions should be added in order
        .append(this.item, grouperIncrementalDataItem.item)
        .append(this.grouperIncrementalDataAction, grouperIncrementalDataItem.grouperIncrementalDataAction).isEquals();
  }

  /**
   * constructor for "with recalc"
   * @param item
   */
  public GrouperIncrementalDataItem(Object item, Long millisSince1970) {
    super();
    this.item = item;
    this.millisSince1970 = millisSince1970;
  }

  public GrouperIncrementalDataItem(Object item, Long millisSince1970,
      GrouperIncrementalDataAction grouperIncrementalDataAction) {
    super();
    this.item = item;
    this.millisSince1970 = millisSince1970;
    this.grouperIncrementalDataAction = grouperIncrementalDataAction;
  }
  
  /**
   * if there is a timestamp put it here
   */
  private Long millisSince1970;

  
  public Long getMillisSince1970() {
    return millisSince1970;
  }

  
  public void setMillisSince1970(Long millisSince1970) {
    this.millisSince1970 = millisSince1970;
  }

  /**
   * item
   */
  private Object item;
  
  /**
   * insert, update, delete
   */
  private GrouperIncrementalDataAction grouperIncrementalDataAction;

  
  public Object getItem() {
    return item;
  }

  
  public void setItem(Object item) {
    this.item = item;
  }

  
  public GrouperIncrementalDataAction getGrouperIncrementalDataAction() {
    return grouperIncrementalDataAction;
  }

  
  public void setGrouperIncrementalDataAction(
      GrouperIncrementalDataAction grouperIncrementalDataAction) {
    this.grouperIncrementalDataAction = grouperIncrementalDataAction;
  }
  
  
}

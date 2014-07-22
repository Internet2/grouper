/**
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
 */
/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee.status;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * assert min size of groups from grouper-ws.properties
 * @author mchyzer
 *
 */
public class DiagnosticMinGroupSize extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagnosticMinGroupSize) {
      DiagnosticMinGroupSize other = (DiagnosticMinGroupSize)obj;
      return new EqualsBuilder().append(this.groupName, other.groupName).append(this.minSize, other.minSize).isEquals();
    }
    return false;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.groupName).append(this.minSize).toHashCode();
  }


  /** sourceId */
  private String groupName;

  /** min size for group */
  private int minSize;

  /**
   * construct with group name and size
   * @param theGroupName
   * @param theMinSize
   */
  public DiagnosticMinGroupSize(String theGroupName, int theMinSize) {
    this.groupName = theGroupName;
    this.minSize = theMinSize;
  }
  
  /**
   * cache the results for 50 hours (though only care for 1 hour)
   */
  private static GrouperCache<String, SuccessMinGroupBean> groupSizeResultsCache;
  
  /**
   * group size results cache
   * @return cache
   */
  private static GrouperCache<String, SuccessMinGroupBean> groupSizeResultsCache() {
    if (groupSizeResultsCache == null) {
      synchronized(DiagnosticMinGroupSize.class) {
        if (groupSizeResultsCache == null) {
          groupSizeResultsCache = new GrouperCache<String, SuccessMinGroupBean>(
              "groupSizeResultsDiagnostic", 10000, false, 
              minSizeMinutesSinceLastSuccess() * 60,  
              minSizeMinutesSinceLastSuccess() * 60, false);
        }
      }
    }
    return groupSizeResultsCache;
  }
  
  /**
   * cache the results for 50 hours (though only care for 1 hour)
   */
  private static GrouperCache<String, SuccessMinGroupBean> groupSizeResultsFailureCache;
  
  /**
   * group size results failure cache
   * @return cache
   */
  private static GrouperCache<String, SuccessMinGroupBean> groupSizeResultsFailureCache() {
    if (groupSizeResultsFailureCache == null) {
      synchronized(DiagnosticMinGroupSize.class) {
        if (groupSizeResultsFailureCache == null) {
          groupSizeResultsFailureCache = new GrouperCache<String, SuccessMinGroupBean>(
              "groupSizeResultsFailureDiagnostic", 10000, false, 
              minSizeMinutesSinceLastFailure() * 60,  
              minSizeMinutesSinceLastFailure() * 60, false);
        }
      }
    }
    return groupSizeResultsFailureCache;
  }
  
  /**
   * bean stored in cache holds when it was done and how many it got.
   */
  private static class SuccessMinGroupBean {

    /** when query was executed */
    private long timestamp;
    
    /** count of members */
    private int count;

    
    /**
     * when query was executed
     * @return the timestamp
     */
    public long getTimestamp() {
      return this.timestamp;
    }

    
    /**
     * when query was executed
     * @param timestamp1 the timestamp to set
     */
    public void setTimestamp(long timestamp1) {
      this.timestamp = timestamp1;
    }

    
    /**
     * count of members
     * @return the count
     */
    public int getCount() {
      return this.count;
    }

    
    /**
     * count of members
     * @param count1 the count to set
     */
    public void setCount(int count1) {
      this.count = count1;
    }
    
    
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    SuccessMinGroupBean successMinGroupBean = groupSizeResultsCache().get(this.groupName);
    
    if (successMinGroupBean != null && successMinGroupBean.getCount() > this.minSize ) {
      this.appendSuccessTextLine("Not checking, there was a success from before: " 
          + GrouperUtil.dateStringValue(successMinGroupBean.getTimestamp()) 
          + ", expecting " + this.minSize + ", but had " + successMinGroupBean.getCount() + " members.");
      
      return true;
    }
    successMinGroupBean = groupSizeResultsFailureCache().get(this.groupName);
    
    if (successMinGroupBean != null) {
      
      //we recently had a failure, dont check again
      this.appendFailureTextLine("Not checking, there was a failure from before: " 
          + GrouperUtil.dateStringValue(successMinGroupBean.getTimestamp()) 
          + ", expecting " + this.minSize + ", but had " + successMinGroupBean.getCount() + " members.");
      return false;
      
    }
    
    //select count(*) 
    //from grouper_memberships_all_v gmav, grouper_groups gg, grouper_fields gf 
    //where gmav.OWNER_GROUP_ID = gg.ID
    //and gmav.FIELD_ID = gf.ID
    //and gg.NAME = 'penn:community:employee'
    long count = HibernateSession.byHqlStatic()
      .createQuery("select count( distinct theMembershipEntry.memberUuid) " +
    		" from MembershipEntry as theMembershipEntry, Group theGroup, Field as theField " +
    		" where theMembershipEntry.ownerGroupId = theGroup.uuid " +
    		" and theMembershipEntry.fieldId = theField.uuid and theGroup.nameDb = :theName ").setString("theName", this.groupName)
    		.uniqueResult(long.class);
    
    
    successMinGroupBean = new SuccessMinGroupBean();
    successMinGroupBean.setCount((int)count);
    successMinGroupBean.setTimestamp(System.currentTimeMillis());
    
    if (count >= this.minSize ) {
      
      this.appendSuccessTextLine("Found: " + count
          + " members and expecting " + this.minSize + " members");
      
      groupSizeResultsCache().put(this.groupName, successMinGroupBean);
      
      return true;
    }
    
    this.appendFailureTextLine("Found: " + count + " members but expecting " + this.minSize + " members");
    
    groupSizeResultsFailureCache().put(this.groupName, successMinGroupBean);
    
    return false;
        
  }

  /**
   * @return min size minutes
   */
  private static int minSizeMinutesSinceLastSuccess() {
    return GrouperConfig.retrieveConfig().propertyValueInt(
        "ws.diagnostic.minSizeMinutesSinceLastSuccess", 60);
  }

  /**
   * @return min size minutes since failure
   */
  private static int minSizeMinutesSinceLastFailure() {
    return GrouperConfig.retrieveConfig().propertyValueInt(
        "ws.diagnostic.minSizeMinutesSinceLastFailure", 5);
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "minimumSize_" + this.groupName;
  }

  /**
   * @see edu.internet2.middleware.grouper.j2ee.status.ws.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Minimum size for group " + this.groupName;
  }

}

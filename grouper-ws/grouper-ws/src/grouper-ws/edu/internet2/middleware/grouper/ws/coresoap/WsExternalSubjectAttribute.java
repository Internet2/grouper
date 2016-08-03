/*******************************************************************************
 * Copyright 2016 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Result of one external subject attribute being retrieved.
 * 
 * @author mchyzer
 */
public class WsExternalSubjectAttribute implements Comparable<WsExternalSubjectAttribute> {

  /**
   * attribute value
   */
  private String attributeValue;
  
  
  /**
   * attribute value
   * @return the attributeValue
   */
  public String getAttributeValue() {
    return this.attributeValue;
  }

  
  /**
   * attribute value
   * @param attributeValue1 the attributeValue to set
   */
  public void setAttributeValue(String attributeValue1) {
    this.attributeValue = attributeValue1;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * convert a set of groups to results
   * @param externalSubjectAttributeSet
   * @param includeDetail true if detail of group should be sent
   * @return the groups (null if none or null)
   */
  public static WsExternalSubjectAttribute[] convertExternalSubjectAttributes(Set<ExternalSubjectAttribute> externalSubjectAttributeSet) {
    if (externalSubjectAttributeSet == null || externalSubjectAttributeSet.size() == 0) {
      return null;
    }
    int externalSubjectAttributeSetSize = externalSubjectAttributeSet.size();
    WsExternalSubjectAttribute[] wsExternalSubjectAttributeResults = new WsExternalSubjectAttribute[externalSubjectAttributeSetSize];
    int index = 0;
    for (ExternalSubjectAttribute externalSubjectAttribute : externalSubjectAttributeSet) {
      WsExternalSubjectAttribute wsGroup = new WsExternalSubjectAttribute(externalSubjectAttribute);
      wsExternalSubjectAttributeResults[index] = wsGroup;
      index++;
    }
    return wsExternalSubjectAttributeResults;

  }
  
  /**
   * Full name of the group (all extensions of parent stems, separated by colons,  and the extention of this group
   */
  private String attributeSystemName;

  /**
   * universally unique identifier of this group
   */
  private String uuid;

  /**
   * no arg constructor
   */
  public WsExternalSubjectAttribute() {
    //blank

  }

  /**
   * construct based on externalSubjectAttribute, assign all fields
   * @param externalSubjectAttribute 
   */
  public WsExternalSubjectAttribute(ExternalSubjectAttribute externalSubjectAttribute) {
    if (externalSubjectAttribute != null) {
      this.setUuid(externalSubjectAttribute.getUuid());
      this.setAttributeSystemName(externalSubjectAttribute.getAttributeSystemName());
      this.setAttributeValue(StringUtils.trimToNull(externalSubjectAttribute.getAttributeValue()));
    }
  }
  
  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @return the name
   */
  public String getAttributeSystemName() {
    return this.attributeSystemName;
  }

  /**
   * universally unique identifier of this group
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * Full name of the group (all extensions of parent stems, separated by colons, 
   * and the extention of this group
   * @param name1 the name to set
   */
  public void setAttributeSystemName(String name1) {
    this.attributeSystemName = name1;
  }

  /**
   * universally unique identifier of this group
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(WsExternalSubjectAttribute o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getAttributeSystemName(), o2.getAttributeSystemName());
  }
}

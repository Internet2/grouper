/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.subj;

/**
 * source and subject id
 * @author mchyzer
 *
 */
public class SubjectBean {

  /**
   * 
   */
  public SubjectBean() {
    
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((sourceId == null) ? 0 : sourceId.hashCode());
    return result;
  }

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SubjectBean other = (SubjectBean) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (sourceId == null) {
      if (other.sourceId != null)
        return false;
    } else if (!sourceId.equals(other.sourceId))
      return false;
    return true;
  }

  /**
   * 
   * @param id
   * @param sourceId
   */
  public SubjectBean(String id, String sourceId) {
    super();
    this.id = id;
    this.sourceId = sourceId;
  }

  /**
   * id
   */
  private String id;
  
  /**
   * source id
   */
  private String sourceId;

  /**
   * id
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * source id
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }
  
}

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
/**
 * @author mchyzer
 * $Id: GrouperSet.java,v 1.2 2009-09-16 08:52:22 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperSet;


/**
 * grouper set manages relationships of directed graphs
 */
public interface GrouperSet {
  
  /**
   * if of this object
   * @return id
   */
  public String __getId();
  
  /**
   * if has this element id then has another element id
   * @return id
   */
  public String __getIfHasElementId();

  /**
   * if has this element then has another element
   * @return id
   */
  public GrouperSetElement __getIfHasElement();

  /**
   * has this element if it has another
   * @return id
   */
  public GrouperSetElement __getThenHasElement();

  /**
   * has this element Id if it has another id
   * @return id
   */
  public String __getThenHasElementId();

  /**
   * depth of this relationship (0 means self, 1 means one hop, 2 means 2 hops, etc)
   * @return depth
   */
  public int __getDepth();

  /**
   * set the parent id of this set.  
   * the parent is the relationship leading up to this relationship.
   * e.g. if this is the graph: A->B->C, and the relationship is A->C, then the parent is A->B
   * @param grouperSetId 
   */
  public void __setParentGrouperSetId(String grouperSetId);
  
  /**
   * parent set id
   * @return parent set id
   */
  public String __getParentGrouperSetId();
  
  /**
   * get the parent set
   * the parent is the relationship leading up to this relationship.
   * e.g. if this is the graph: A->B->C, and the relationship is A->C, then the parent is A->B
   * @return parent
   */
  public GrouperSet __getParentGrouperSet();
  
  /**
   * insert or update this object
   */
  public void saveOrUpdate();

  /**
   * delete this object
   */
  public void delete();

}

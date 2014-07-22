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
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.exception.StemSetNotFoundException;
import edu.internet2.middleware.grouper.stem.StemSet;

/**
 * @author shilen
 * $Id$
 */
public interface StemSetDAO extends GrouperDAO {
  
  /** 
   * insert or update a stemSet
   * @param stemSet 
   */
  public void saveOrUpdate(StemSet stemSet);
  
  /** 
   * insert in batch
   * @param stemSets
   */
  public void saveBatch(Collection<StemSet> stemSets);
  
  /** 
   * delete a stemSet
   * @param stemSet 
   */
  public void delete(StemSet stemSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the stem set
   * @throws StemSetNotFoundException 
   */
  public StemSet findById(String id, boolean exceptionIfNotFound)
    throws StemSetNotFoundException;

  /**
   * find by if has stem id
   * @param id
   * @return the stem sets
   */
  public Set<StemSet> findByIfHasStemId(String id);
  
  /**
   * find by if has stem ids
   * @param ids
   * @return the stem sets
   */
  public Set<StemSet> findByIfHasStemIds(Collection ids);

  /**
   * find by then has stem id
   * @param id
   * @return the stem sets
   */
  public Set<StemSet> findByThenHasStemId(String id);
  
  /**
   * find by if has stem id (exclude self stem)
   * @param id
   * @return the stem sets
   */
  public Set<StemSet> findNonSelfByIfHasStemId(String id);
  
  /**
   * delete all stem sets with the given if has stem id
   * @param id
   */
  public void deleteByIfHasStemId(String id);

  /**
   * @param stemSets 
   * @param queryOptions 
   * @return children
   */
  public Set<StemSet> findAllChildren(Collection<StemSet> stemSets, QueryOptions queryOptions);
  
  /**
   * @param stemId
   * @param minDepth
   * @param queryOptions
   * @return stemSets
   */
  public Set<StemSet> findByIfHasStemOfStemChildrenAndMinDepth(String stemId, int minDepth, QueryOptions queryOptions);
  
  /**
   * find by if and then (not same) with depth of 1 (immediate)
   * @param stemIdIf
   * @param stemIdThen
   * @param exceptionIfNotFound 
   * @return the stemSet
   * @throws StemSetNotFoundException 
   */
  public StemSet findByIfThenImmediate(String stemIdIf, 
      String stemIdThen, boolean exceptionIfNotFound) throws StemSetNotFoundException;
  
  /**
   * @return stems that are missing self stem sets.  First element of the array is the stemId, second is the parentStemId
   */
  public Set<Object[]> findMissingSelfStemSets();
}

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
/*
 * @author mchyzer
 * $Id: QueryPagingTest.java,v 1.3 2009-04-13 20:24:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.List;

import junit.textui.TestRunner;

import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.ByCriteriaStatic;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 *
 */
public class QueryPagingTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new QueryPagingTest("testPaging"));
  }
  
  /**
   * @param name
   */
  public QueryPagingTest(String name) {
    super(name);
  }

  /**
   * Method testCalculateIndexes.
   */
  public void testCalculateIndexes() {

    QueryPaging queryPaging = new QueryPaging();
    //make sure it throws exception on no size
    boolean foundError = false;
    try {
      queryPaging.calculateIndexes();
    } catch (Exception e) {
      foundError = true;
    }

    assertEquals(true, foundError);

    //now see check the calcs
    queryPaging.setPageSize(10);
    queryPaging.setTotalRecordCount(30);
    queryPaging.setPageNumber(1);
    queryPaging.calculateIndexes();
    assertEquals(3, queryPaging.getNumberOfPages());
    assertEquals(1, queryPaging.getPageStartIndex());
    assertEquals(10, queryPaging.getPageEndIndex());

    queryPaging.setTotalRecordCount(31);
    queryPaging.setPageNumber(2);
    queryPaging.calculateIndexes();
    assertEquals(4, queryPaging.getNumberOfPages());
    assertEquals(11, queryPaging.getPageStartIndex());
    assertEquals(20, queryPaging.getPageEndIndex());

    queryPaging.setTotalRecordCount(29);
    queryPaging.setPageNumber(3);
    queryPaging.calculateIndexes();
    assertEquals(3, queryPaging.getNumberOfPages());
    assertEquals(21, queryPaging.getPageStartIndex());
    assertEquals(29, queryPaging.getPageEndIndex());

    //make sure page number is reset if too large
    queryPaging.setTotalRecordCount(9);
    queryPaging.setPageNumber(5);
    queryPaging.calculateIndexes();
    assertEquals(1, queryPaging.getPageNumber());
    assertEquals(1, queryPaging.getPageStartIndex());
    assertEquals(9, queryPaging.getPageEndIndex());

  }

  /**
   * 
   */
  public void testPaging() {
    //admins, optins, optouts, readers, updaters, viewers
    //viewers, updaters, readers, optouts, optins, admins
    List<Field> fields = HibernateSession.byHqlStatic().createQuery(
        "from Field where typeString = 'access'").options(new QueryOptions().sortAsc("name").paging(2,1, false)).list(Field.class);
    assertEquals("admins, groupAttrReaders", Field.fieldNames(fields));
    
    fields = HibernateSession.byHqlStatic().createQuery(
      "from Field where typeString = 'access'").options(new QueryOptions().sortAsc("name").paging(2,2, false)).list(Field.class);
    assertEquals("groupAttrUpdaters, optins", Field.fieldNames(fields));
    
    fields = HibernateSession.byHqlStatic().createQuery(
      "from Field where typeString = 'access'").options(new QueryOptions().sortDesc("name").paging(3,2, false)).list(Field.class);
    assertEquals("optouts, optins, groupAttrUpdaters", Field.fieldNames(fields));

    //try with criteria
    fields = HibernateSession.byCriteriaStatic().options(new QueryOptions().sortAsc("name").paging(2,1, false))
        .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    assertEquals("admins, groupAttrReaders", 
        Field.fieldNames(fields));

    fields = HibernateSession.byCriteriaStatic().options(new QueryOptions().sortDesc("name").paging(2,2, false))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    assertEquals("readers, optouts", 
        Field.fieldNames(fields));

    //page size of 2, first page, and get the total record count retrieval.  
    QueryPaging queryPaging = new QueryPaging(2, 1, true);
    fields = HibernateSession.byHqlStatic().createQuery(
        "from Field where typeString = 'access'").options(new QueryOptions().sortAsc("name").paging(queryPaging)).list(Field.class);
    assertEquals("admins, groupAttrReaders", Field.fieldNames(fields));
    
    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(4, queryPaging.getNumberOfPages());
    
    //try it with criteria
    queryPaging = new QueryPaging(2, 1, true);
    fields = HibernateSession.byCriteriaStatic().options(new QueryOptions().sortAsc("name").paging(queryPaging))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    assertEquals("admins, groupAttrReaders", Field.fieldNames(fields));
    
    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(4, queryPaging.getNumberOfPages());

    int queryCount = ByHql.queryCountQueries;
    
    //try it again with a different type of query (one with a select)
    queryPaging = new QueryPaging(2, 1, true);
    fields = HibernateSession.byHqlStatic().createQuery(
        "select field\n from Field field where field.typeString = 'access'")
        .options(new QueryOptions().sortAsc("name").paging(queryPaging))
        .list(Field.class);
    assertEquals("admins, groupAttrReaders", Field.fieldNames(fields));
    
    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(4, queryPaging.getNumberOfPages());
    assertEquals(queryCount+1, ByHql.queryCountQueries);

    queryCount = ByHql.queryCountQueries;

    queryPaging = new QueryPaging(2, 1, false);
    fields = HibernateSession.byHqlStatic().createQuery(
        "select field\n from Field field where field.typeString = 'access'")
        .options(new QueryOptions().sortAsc("name").paging(queryPaging))
        .list(Field.class);

    assertEquals(-1, queryPaging.getTotalRecordCount());
    assertEquals(queryCount, ByHql.queryCountQueries);

    queryPaging = new QueryPaging(20, 1, true);
    fields = HibernateSession.byHqlStatic().createQuery(
        "select field\n from Field field where field.typeString = 'access'")
        .options(new QueryOptions().sortAsc("name").paging(queryPaging)).list(Field.class);

    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(1, queryPaging.getNumberOfPages());
    //this should not cause another query
    assertEquals(queryCount, ByHql.queryCountQueries);

    //try it with criteria
    queryCount = ByCriteriaStatic.queryCountQueries;
    queryPaging = new QueryPaging(2, 1, true);
    fields = HibernateSession.byCriteriaStatic()
      .options(new QueryOptions().sortAsc("name").paging(queryPaging))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    assertEquals("admins, groupAttrReaders", Field.fieldNames(fields));
    
    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(4, queryPaging.getNumberOfPages());
    assertEquals(queryCount+1, ByCriteriaStatic.queryCountQueries);

    queryCount = ByCriteriaStatic.queryCountQueries;
    queryPaging = new QueryPaging(2, 1, false);
    fields = HibernateSession.byCriteriaStatic()
      .options(new QueryOptions().sortAsc("name").paging(queryPaging))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    
    assertEquals(-1, queryPaging.getTotalRecordCount());
    //this should not cause another query
    assertEquals(queryCount, ByCriteriaStatic.queryCountQueries);

    queryCount = ByCriteriaStatic.queryCountQueries;
    queryPaging = new QueryPaging(20, 1, true);
    fields = HibernateSession.byCriteriaStatic()
    .options(new QueryOptions().sortAsc("name").paging(queryPaging))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    
    assertEquals(8, queryPaging.getTotalRecordCount());
    assertEquals(1, queryPaging.getNumberOfPages());
    //this should not cause another query
    assertEquals(queryCount, ByCriteriaStatic.queryCountQueries);

    
  }
}

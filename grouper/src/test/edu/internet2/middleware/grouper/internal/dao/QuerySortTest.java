/*
 * @author mchyzer
 * $Id: QuerySortTest.java,v 1.3 2009-04-13 20:24:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.List;

import junit.textui.TestRunner;

import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 *
 */
public class QuerySortTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(QuerySortTest.class);
  }
  
  /**
   * @param name
   */
  public QuerySortTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testQuerySort() {
    QuerySort querySort = new QuerySort("col1", true);
    assertEquals("col1 asc", querySort.sortString(false));

    querySort = new QuerySort("col1", false);
    assertEquals(" col1 desc", querySort.sortString(true));

    querySort.setMaxCols(2);
    querySort.insertSortToBeginning("col2", true);
    assertEquals("col2 asc, col1 desc", querySort.sortString(false));
    
    querySort.insertSortToBeginning("col2", false);
    assertEquals("col2 desc, col1 desc", querySort.sortString(false));
    
    querySort.insertSortToBeginning("col3", true);
    assertEquals("col3 asc, col2 desc", querySort.sortString(false));
    
    querySort.assignSort("col4", false);
    assertEquals("col4 desc", querySort.sortString(false));
    
    //viewers, updaters, readers, optouts, optins, admins
    List<Field> fields = HibernateSession.byHqlStatic().createQuery(
        "from Field where type = 'access'")
        .options(new QueryOptions().sortAsc("name")).list(Field.class);
    assertEquals("admins, optins, optouts, readers, updaters, viewers", 
        Field.fieldNames(fields));
    
    fields = HibernateSession.byHqlStatic().createQuery(
      "from Field where type = 'access'")
      .options(new QueryOptions().sortDesc("name")).list(Field.class);
    assertEquals("viewers, updaters, readers, optouts, optins, admins", 
        Field.fieldNames(fields));

    //try with criteria
    fields = HibernateSession.byCriteriaStatic()
      .options(new QueryOptions().sortAsc("name"))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
    assertEquals("admins, optins, optouts, readers, updaters, viewers", 
        Field.fieldNames(fields));

    fields = HibernateSession.byCriteriaStatic()
      .options(new QueryOptions().sortDesc("name"))
      .list(Field.class, HibUtils.listCrit(Restrictions.eq("typeString", "access")));
      assertEquals("viewers, updaters, readers, optouts, optins, admins", 
        Field.fieldNames(fields));
    
  }
  
}

/*
 * @author mchyzer
 * $Id: HibernateSessionTest.java,v 1.1.2.1 2009-03-27 21:23:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperTest;

/**
 *
 */
public class HibernateSessionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HibernateSessionTest("testPagingSorting"));
  }
  

  /**
   * @param name
   */
  public HibernateSessionTest(String name) {
    super(name);
  }

  /**
   * test paging/sorting
   */
  public void testPagingSorting() {
    List<Field> fields = HibernateSession.byHqlStatic().createQuery(
        "from Field where type = 'access' order by name desc").list(Field.class);
    for (Field field : fields) {
      System.out.println(field);
    }
  }


}

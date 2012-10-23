/**
 * 
 */
package edu.internet2.middleware.grouper.tableIndex;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * @author mchyzer
 *
 */
public class TableIndexTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TableIndexTest("testHibernate"));
  }

  /**
   * 
   */
  public TableIndexTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public TableIndexTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testHibernate() {
    
    TableIndex tableIndex = GrouperDAOFactory.getFactory().getTableIndex().findByType(TableIndexType.group);
    
    if (tableIndex != null) {
      GrouperDAOFactory.getFactory().getTableIndex().delete(tableIndex);
    }
    
    tableIndex = new TableIndex();
    tableIndex.setType(TableIndexType.group);
    
    
    
  }
  
}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;


/**
 *
 */
public class GrouperTableSync {

  /**
   * 
   */
  public GrouperTableSync() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    GcTableSyncOutput gcTableSyncOutput = new GcTableSync().sync(args[0], GcTableSyncSubtype.valueOfIgnoreCase(args[1], true));
    System.out.println(gcTableSyncOutput.toString());
  }

}

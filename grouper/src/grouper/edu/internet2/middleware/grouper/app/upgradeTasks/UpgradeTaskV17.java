package edu.internet2.middleware.grouper.app.upgradeTasks;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UpgradeTaskV17 implements UpgradeTasksInterface {
  
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("5.8.2");
  }

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    
    Integer nullValues = new GcDbAccess().sql("select count(1) from grouper_pit_groups pg where pg.source_internal_id is null and pg.active='T'").select(int.class);
    if (nullValues != null && nullValues > 0) {
      return true;
    }
    
    nullValues = new GcDbAccess().sql("select count(1) from grouper_pit_fields pf where pf.source_internal_id is null and pf.active='T'").select(int.class);
    if (nullValues != null && nullValues > 0) {
      return true;
    }
    
    nullValues = new GcDbAccess().sql("select count(1) from grouper_pit_members pm where pm.source_internal_id is null and pm.active='T'").select(int.class);
    if (nullValues != null && nullValues > 0) {
      return true;
    }
    
    return false;
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }
  

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    new GcDbAccess().sql("update grouper_pit_groups  pg set source_internal_id = (select g.internal_id from grouper_groups  g where pg.source_id = g.id) where pg.source_internal_id is null and pg.active='T'").executeSql();
    new GcDbAccess().sql("update grouper_pit_fields  pf set source_internal_id = (select f.internal_id from grouper_fields  f where pf.source_id = f.id) where pf.source_internal_id is null and pf.active='T'").executeSql();
    new GcDbAccess().sql("update grouper_pit_members pm set source_internal_id = (select m.internal_id from grouper_members m where pm.source_id = m.id) where pm.source_internal_id is null and pm.active='T'").executeSql();
  }

}

package edu.internet2.middleware.grouper.misc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class SyncAllPitTablesDaemon extends OtherJobBase {

  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    SyncPITTables syncPITTables = new SyncPITTables();
    
    syncPITTables.syncAllPITTables();
    
    LocalDateTime ldt = LocalDateTime.now();
    ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
    ZonedDateTime gmt = zdt.withZoneSameInstant(ZoneId.of("GMT"));
    Timestamp timestamp = Timestamp.valueOf(gmt.toLocalDateTime());

    long millisSince1970 = System.currentTimeMillis();
    
    int rows = new GcDbAccess()
      .sql("update grouper_time set the_utc_timestamp = ?, this_tz_timestamp = ?,  utc_millis_since_1970 = ?, utc_micros_since_1970 = ? where time_label = 'now'")
      .addBindVar(timestamp).addBindVar(new Timestamp(millisSince1970)).addBindVar(millisSince1970).addBindVar(millisSince1970*1000).executeSql();
    
    if (rows == 0) {
      rows = new GcDbAccess()
        .sql("insert into grouper_time (time_label, the_utc_timestamp, this_tz_timestamp, utc_millis_since_1970, utc_micros_since_1970) values ('now', ?, ?, ?, ?)")
        .addBindVar(timestamp).addBindVar(new Timestamp(millisSince1970)).addBindVar(millisSince1970).addBindVar(millisSince1970*1000).executeSql();
      if (rows != 1) {
        throw new RuntimeException("Cannot insert into grouper_time! " + rows);
      }
      otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
    } else if (rows == 1) {
      otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
    } else {
      throw new RuntimeException("Cannot update one row of grouper_time! " + rows);
    }
    
    return null;
  }

}

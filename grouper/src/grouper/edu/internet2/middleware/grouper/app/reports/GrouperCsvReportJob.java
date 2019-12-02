/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.file.GrouperSftp;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperCsvReportJob extends OtherJobBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperCsvReportJob.class);

  /**
   * 
   */
  public GrouperCsvReportJob() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName("bplogixFeed");
    new GrouperCsvReportJob().run(otherJobInput);

  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    String jobName = otherJobInput.getJobName();
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap();
    
    long now = System.nanoTime();
    
    try {
      String database = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".csvReport.database");
      
      debugMap.put("job", "csv");
      
      if (StringUtils.isBlank(database)) {
        database = "grouper";
      }

      debugMap.put("database", database);

      String query = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".csvReport.query");

      if (LOG.isDebugEnabled()) {
        debugMap.put("query", query);
      } else {
        debugMap.put("query", StringUtils.abbreviate(query, 25));
      }
      
      boolean removeUnderscoresAndCapitalizeHeaders = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".csvReport.removeUnderscoresAndCapitalizeHeaders", false);
  
      List<String> headers = retrieveHeaders(query, removeUnderscoresAndCapitalizeHeaders);
      debugMap.put("columnsSize", GrouperUtil.length(headers));
      
      List<String[]> data = retrieveData(database, query);
      debugMap.put("rowsSize", GrouperUtil.length(data));
      hib3GrouperLoaderLog.setTotalCount(GrouperUtil.length(data));
      
      String fileName = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".csvReport.fileName");
  
      if (StringUtils.isBlank(fileName)) {
        fileName = GrouperUtil.tmpDir(true) + "grouperReport_" + jobName + "_" + GrouperUtil.timestampToFileString(new Date()) + ".csv";
      } else if (!fileName.contains("/") && !fileName.contains("\\")) {
        // if it has no dir info, then use the name of the file
        fileName = GrouperUtil.tmpDir(true) + fileName;
      }

      File file = createCsv(fileName, headers, data);
      debugMap.put("file", file.getAbsoluteFile());
      debugMap.put("fileSizeBytes", file.length());
      
      String sftpConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + jobName + ".csvReport.sftp.configId");
      debugMap.put("sftpConfigId", sftpConfigId);
      
      if (!StringUtils.isBlank(sftpConfigId)) {
        
        String fileNameRemote = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".csvReport.sftp.fileNameRemote");
        debugMap.put("fileNameRemote", fileNameRemote);
  
        GrouperSftp.sendFile(sftpConfigId, file, fileNameRemote);
        
      }
  
      boolean deleteFile = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".csvReport.deleteFile", true);
      
      if (deleteFile) {
        GrouperUtil.deleteFile(file);
      }
    } catch (Exception re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      debugMap.put("tookMillis", ((System.nanoTime()-now)/1000000));
      String debugMessage = GrouperUtil.mapToString(debugMap);
      hib3GrouperLoaderLog.setJobMessage(debugMessage);
      if (LOG.isDebugEnabled()) {
        LOG.debug(debugMessage);
      }
    }

    return null;
  }

  /**
   * take some data and create a csv
   * @param fileName if blank just create a file
   * @param headers
   * @param data
   * @return the file
   */
  public static File createCsv(String fileName, List<String> headers, List<String[]> data) {
    //convert the report to CSV
    //Delimiter used in CSV file
    String NEW_LINE_SEPARATOR = "\n";
    
    FileWriter fileWriter = null;
    CSVPrinter csvFilePrinter = null;

    //Create the CSVFormat object with "\n" as a record delimiter
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

    File file = new File(fileName);
 
    GrouperUtil.mkdirs(file.getParentFile());

    // if exists, delete and create.  if not exist thats ok
    GrouperUtil.deleteFile(file);
    GrouperUtil.fileCreateNewFile(file);
    
    try {

      //initialize FileWriter object
      fileWriter = new FileWriter(file);

      //initialize CSVPrinter object
      csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

      //Create CSV file header
      csvFilePrinter.printRecord(headers);

      //Write a new student object list to the CSV file
      for (String[] row : data) {
        
        // check daypass
        csvFilePrinter.printRecord((Object[])(Object)row);
      }

    } catch (Exception e) {

      throw new RuntimeException("Error in CsvFileWriter !!! " + file.getName(), e);
    
    } finally {

      GrouperUtil.closeQuietly(fileWriter);
      GrouperUtil.closeQuietly(csvFilePrinter);
    }
    return file;
  }
  
  /**
   * get headers for query
   * @param query
   * @param removeUnderscoresAndCapitalizeHeaders 
   * @return the headers
   */
  public static List<String> retrieveHeaders(String query, boolean removeUnderscoresAndCapitalizeHeaders) {
    //lets parse the query:
    String headersString = query;
    headersString = headersString.substring("select ".length());
    
    headersString = headersString.substring(0, headersString.toLowerCase().indexOf(" from "));
    
    List<String> originalHeaders = GrouperUtil.splitTrimToList(headersString, ",");
    
    
    // lets convert headers from USER_NAME to UserName
    List<String> headers = new ArrayList<String>();
    for (String originalHeader : originalHeaders) {
      StringBuilder headerBuilder = new StringBuilder();
      for (String headerPart : StringUtils.split(originalHeader, "_")) {
        headerBuilder.append(StringUtils.capitalize(headerPart));
      }
      headers.add(headerBuilder.toString());
    }
    return headers;
  }
  
  /**
   * 
   * @param database 
   * @param query 
   * @return the data
   */
  public static List<String[]> retrieveData(String database, String query) {
    
    GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(database);
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<String[]> rows = new ArrayList<String[]>();
    try {

      connection = grouperLoaderDb.connection(); 
      // select from the database
      preparedStatement = connection.prepareStatement(query);
  
      resultSet = preparedStatement.executeQuery();
                        
      while (resultSet.next()) {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        String row[] = new String[resultSetMetaData.getColumnCount()];
        for (int i=0; i < row.length;i++) {
          row[i] = resultSet.getString(i+1);
        }
        rows.add(row);
      }
    } catch (SQLException sqle) {
      throw new RuntimeException("Error in query: " + query, sqle);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(preparedStatement);
      GrouperUtil.closeQuietly(connection);
    }

    return rows;
  }
  
}

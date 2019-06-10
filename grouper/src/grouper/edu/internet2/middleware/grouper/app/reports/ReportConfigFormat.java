/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.io.FileWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * report format e.g. CSV
 */
public enum ReportConfigFormat {

  /** format of report in comma separated values */
  CSV {

    @Override
    public void formatReport(GrouperReportData grouperReportData, GrouperReportInstance grouperReportInstance) {

      //convert the report to CSV
      //Delimiter used in CSV file
      String NEW_LINE_SEPARATOR = "\n";
      
      FileWriter fileWriter = null;
      CSVPrinter csvFilePrinter = null;

      //Create the CSVFormat object with "\n" as a record delimiter
      CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

      try {

        grouperReportInstance.createEmptyReportFile();
        
        //initialize FileWriter object
        fileWriter = new FileWriter(grouperReportInstance.getReportFileUnencrypted());

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        //Create CSV file header
        csvFilePrinter.printRecord(grouperReportData.getHeaders());

        //Write a new student object list to the CSV file
        for (String[] row : grouperReportData.getData()) {
          csvFilePrinter.printRecord((Object[])(Object)row);
        }
        
      } catch (Exception e) {

        throw new RuntimeException("Error in CsvFileWriter !!! " 
            + ((grouperReportInstance != null && grouperReportInstance.getReportFileUnencrypted() != null) 
                ? grouperReportInstance.getReportFileUnencrypted().getAbsolutePath() : "no file yet"), e);
      
      } finally {

        GrouperUtil.closeQuietly(fileWriter);
        GrouperUtil.closeQuietly(csvFilePrinter);
        
      }
      if (grouperReportInstance != null && grouperReportInstance.getReportFileUnencrypted() != null) {
        grouperReportInstance.setReportInstanceSizeBytes(grouperReportInstance.getReportFileUnencrypted().length());
      }
    }

  };
  
  /**
   * format report data into a file
   * @param grouperReportData
   * @param grouperReportInstance
   */
  public abstract void formatReport(GrouperReportData grouperReportData, GrouperReportInstance grouperReportInstance);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionIfBlank 
   * @return the enum or null or exception if not found
   */
  public static ReportConfigFormat valueOfIgnoreCase(String string, boolean exceptionIfBlank) {
    return GrouperUtil.enumValueOfIgnoreCase(ReportConfigFormat.class,string, exceptionIfBlank, true );
  }

  
}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.reports;

import java.io.File;
import java.util.List;


/**
 * data from a report
 */
public class GrouperReportData {

  /**
   * 
   */
  public GrouperReportData() {
  }

  /**
   * @param headers1
   * @param data1
   */
  public GrouperReportData(List<String> headers1, List<String[]> data1) {
    this.headers = headers1;
    this.data = data1;
  }

  /**
   * file of data if its not a normal grid
   */
  private File file = null;
  
  
  
  /**
   * file of data if its not a normal grid
   * @return the file
   */
  public File getFile() {
    return this.file;
  }

  /**
   * file of data if its not a normal grid
   * @param file1
   */
  public void setFile(File file1) {
    this.file = file1;
  }

  /**
   * report headers
   */
  private List<String> headers = null;
  
  /**
   * report data
   */
  private List<String[]> data = null;

  
  /**
   * @return the headers
   */
  public List<String> getHeaders() {
    return this.headers;
  }

  
  /**
   * @param headers1 the headers to set
   */
  public void setHeaders(List<String> headers1) {
    this.headers = headers1;
  }

  
  /**
   * @return the data
   */
  public List<String[]> getData() {
    return this.data;
  }

  
  /**
   * @param data1 the data to set
   */
  public void setData(List<String[]> data1) {
    this.data = data1;
  }

  
  
}

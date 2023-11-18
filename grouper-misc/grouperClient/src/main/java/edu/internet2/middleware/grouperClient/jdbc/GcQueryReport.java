package edu.internet2.middleware.grouperClient.jdbc;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Structure to encapsulate data about a query being executed.
 * @author harveycg
 */
public class GcQueryReport {


  /**
   * Query that was executed.
   */
  private String query;


  /**
   * Total time spent, will include any nested queries and code.
   */
  private long cumulativeTimeMillis;


  /**
   * How many times the query was executed.
   */
  private int numberOfExecutions;


  /**
   * The shortest execution time.
   */
  private Long shortestExecutionTimeMillis;


  /**
   * The longest execution time.
   */
  private Long longestExecutionTimeMillis;



  /**
   * Write out a csv report to the given file path.
   * @param fileLocation is the loaction of the file to write.
   * @param queryReports is the map of reports.
   */
  public static void reportToFile(String fileLocation, Map<String, GcQueryReport> queryReports){
    StringBuilder contents = new StringBuilder("QUERY,CUMULATIVE_TIME_MILLIS,MM:SS:MI,NUM_EXECUTIONS,AVG_EXECUTION_MILLIS,SHORT_MILLIS,LONG_MILLIS,CONNECTION\n");
    for (GcQueryReport queryReport : queryReports.values()){
      contents.append("\"" + queryReport.getQuery() + "\"," + queryReport.getCumulativeTimeMillis() + "," 
          + queryReport.cumulativeTimeMillisHumanReadable() + "," 
          + queryReport.getNumberOfExecutions() + "," + (queryReport.getCumulativeTimeMillis() / queryReport.getNumberOfExecutions()) + "," 
          + queryReport.getShortestExecutionTimeMillis() + "\n");
    }  
    
    File file = new File(fileLocation);
    GrouperClientUtils.saveStringIntoFile(file, contents.toString());
  }


  /**
   * Add an execution time.
   * @param millis is the millis to add.
   */
  public void addExecutionTime(long millis){
    this.numberOfExecutions++;
    this.cumulativeTimeMillis += millis;

    if (this.getShortestExecutionTimeMillis() == null || millis < this.getShortestExecutionTimeMillis()){
      this.setShortestExecutionTimeMillis(millis);
    }

    if (this.getLongestExecutionTimeMillis() == null || millis > this.getLongestExecutionTimeMillis()){
      this.setLongestExecutionTimeMillis(millis);
    }

  }


  /**
   * Query that was executed.
   * @return the query
   */
  public String getQuery() {
    return this.query;
  }


  /**
   * Query that was executed.
   * @param _query the query to set
   */
  public void setQuery(String _query) {
    this.query = _query;
  }


  /**
   * Total time spent, will include any nested queries and code.
   * @return the cumulativeTime
   */
  public Long getCumulativeTimeMillis() {
    return this.cumulativeTimeMillis;
  }


  /**
   * Total time spent, will include any nested queries and code.
   * @param _cumulativeTimeMillis the cumulativeTime to set
   */
  public void setCumulativeTime(Long _cumulativeTimeMillis) {
    this.cumulativeTimeMillis = _cumulativeTimeMillis;
  }


  /**
   * How many times the query was executed.
   * @return the numberOfExecutions
   */
  public int getNumberOfExecutions() {
    return this.numberOfExecutions;
  }


  /**
   * How many times the query was executed.
   * @param _numberOfExecutions the numberOfExecutions to set
   */
  public void setNumberOfExecutions(int _numberOfExecutions) {
    this.numberOfExecutions = _numberOfExecutions;
  }


  /**
   * The shortest execution time.
   * @return the shortestExecutionTimeMillis
   */
  private Long getShortestExecutionTimeMillis() {
    return this.shortestExecutionTimeMillis;
  }


  /**
   * The shortest execution time.
   * @param _shortestExecutionTimeMillis the shortestExecutionTimeMillis to set
   */
  private void setShortestExecutionTimeMillis(Long _shortestExecutionTimeMillis) {
    this.shortestExecutionTimeMillis = _shortestExecutionTimeMillis;
  }


  /**
   * The longest execution time.
   * @return the longestExecutionTimeMillis
   */
  private Long getLongestExecutionTimeMillis() {
    return this.longestExecutionTimeMillis;
  }


  /**
   * The longest execution time.
   * @param _longestExecutionTimeMillis the longestExecutionTimeMillis to set
   */
  private void setLongestExecutionTimeMillis(Long _longestExecutionTimeMillis) {
    this.longestExecutionTimeMillis = _longestExecutionTimeMillis;
  }


  /**
   * Get human readable time spent querying.
   * @return the time.
   */
  private String cumulativeTimeMillisHumanReadable(){
    Long millis = this.getCumulativeTimeMillis();
    
    if (this.getCumulativeTimeMillis() < 1000){
      return "00:00." + this.getCumulativeTimeMillis();
    } else if (this.getCumulativeTimeMillis() < (1000*60)){
      return "00:" + (this.getCumulativeTimeMillis() / 60) +".00";
    }
    
    return String.format("%d:%d.00", 
        TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis) - 
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    );
  }


}

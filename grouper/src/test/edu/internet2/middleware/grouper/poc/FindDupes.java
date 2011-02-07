package edu.internet2.middleware.grouper.poc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class FindDupes {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    findDupes("c:/temp/grouper/sql.txt");

  }
  
  /**
   * 
   */
  private static class ResultBean implements Comparable {
    /** */
    int count = 0;
    
    /** */
    int firstLineNumber;
    
    /** */
    String line;

    /** */
    String previousLine;

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
      if (((Integer)this.count).equals(((ResultBean)o).count)) {
        return ((Integer)this.firstLineNumber).compareTo(((ResultBean)o).firstLineNumber);
      }
      return ((Integer)this.count).compareTo(((ResultBean)o).count) ;
    }
    
    
  }

  /**
   * 
   * @param filename
   */
  public static void findDupes(String filename) {
    File file = new File(filename);
    String contents = GrouperUtil.readFileIntoString(file);
    
    Map<String, ResultBean> results = new LinkedHashMap<String, ResultBean>();
    
    String[] contentsLines = GrouperUtil.splitTrim(contents, "\n");
    
    int lineNumber = 1;
    String previousLine = null;
    for (String contentLine : contentsLines) {
      
      if (StringUtils.isBlank(contentLine)) {
        continue;
      }
      
      if (contentLine.toLowerCase().startsWith("select")) {
        
        ResultBean resultBean = results.get(contentLine);
        if (resultBean == null) {
          resultBean = new ResultBean();
          resultBean.count = 1;
          resultBean.firstLineNumber = lineNumber;
          resultBean.line = contentLine; 
          resultBean.previousLine = previousLine;
          results.put(contentLine, resultBean);
        } else {
          resultBean.count++;
        }
        
      }
      previousLine = contentLine;
      lineNumber++;
    }
    
    List<ResultBean> resultList = new ArrayList<ResultBean>(results.values());
    Collections.sort(resultList);
    for (ResultBean resultBean : resultList) {
      System.out.println("\n" + resultBean.previousLine + "\nCount: " + resultBean.count + ", firstLine: " + resultBean.firstLineNumber + ": " + resultBean.line);
      
    }
    
  }
 
}

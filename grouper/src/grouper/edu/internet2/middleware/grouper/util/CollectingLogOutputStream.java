package edu.internet2.middleware.grouper.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.exec.LogOutputStream;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class CollectingLogOutputStream extends LogOutputStream {
    private final List<String> lines = new LinkedList<String>();
    @Override protected void processLine(String line, int level) {
        lines.add(line);
    }   
    public List<String> getLines() {
        return lines;
    }
    public String getAllLines() {
      
      if (lines.size() == 0) {
        return "";
      }
      return GrouperUtil.join(lines.iterator(), '\n');
      
  }

}
package edu.internet2.middleware.grouper.app.gsh;

import javax.script.CompiledScript;

public class GrouperGroovyScript {

  public GrouperGroovyScript() {
    
    for (int i=0;i<executionTimesMillis.length;i++) {
      executionTimesMillis[i] = -1;
    }
    
  }

  private String script;
  
  private CompiledScript compiledScript;
  
  private int[] executionTimesMillis = new int[100];

  private int executionTimeIndex = 0;
  

  public String getScript() {
    return script;
  }

  
  public void setScript(String script) {
    this.script = script;
  }

  
  public CompiledScript getCompiledScript() {
    return compiledScript;
  }

  
  public void setCompiledScript(CompiledScript compiledScript) {
    this.compiledScript = compiledScript;
  }

  public void registerExecutionTimeMillis(long millis) {
    executionTimesMillis[this.executionTimeIndex] = (int)millis;
    this.executionTimeIndex++;
    if (this.executionTimeIndex >= this.executionTimesMillis.length) {
      this.executionTimeIndex = 0;
    }
  }

  /**
   * get the average execution time, -1 means dont know
   * @return
   */
  public int getAverageExecutionTimeMillis() {

    int sumExecution = 0;
    int countExecution = 0;
    
    for (int i=0;i<executionTimesMillis.length;i++) {
      if (executionTimesMillis[i] != -1) {
        sumExecution += executionTimesMillis[i];
        countExecution++;
      }
    }
    
    // first run?
    if (sumExecution == 0) {
      return -1;
    }

    int averageExecutionTime = sumExecution / countExecution;
    return averageExecutionTime;
  }
  
}

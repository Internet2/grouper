/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.gsh;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Interpreter;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import groovy.lang.MissingPropertyException;

/**
 * @author shilen
 */
public class GrouperGroovysh extends Groovysh {

  /**
   * if running through a script, need to exit script
   * @param lineNumber
   * @param line
   * @return true if exit script
   */
  public static boolean scriptLineExit(int lineNumber, String it) {
    if (it == null) {
      return false;
    }
    it = it.trim();
    if (lineNumber == 1 && it.startsWith("#!")) {
      return true;
    }

    if (it.equals("exit") || it.startsWith("exit ") || it.startsWith("exit;") || it.equals("quit") || it.startsWith("quit ") || it.startsWith("quit;")) {
      return true;
    }
    return false;
  }
  
  /**
   * if running through a script, need to ignore line
   * @param lineNumber
   * @param line
   * @return true if skip line
   */
  public static boolean scriptLineIgnore(int lineNumber, String it) {
    if (it == null) {
      return true;
    }
    it = it.trim();
    if (it.startsWith("#") || it.startsWith("//")) {
      return true;
    }
    return false;
  }
  
  public static class GrouperGroovyResult {
    
    /**
     * debug map from script
     */
    private Map<String, Object> debugMap = null;
    
    /**
     * result
     */
    private Object result = null;
    
    /**
     * result
     * @return
     */
    public Object getResult() {
      return result;
    }

    /**
     * result
     * @param result
     */
    public void setResult(Object result) {
      this.result = result;
    }

    /**
     * debug map from script
     * @return
     */
    public Map<String, Object> getDebugMap() {
      return debugMap;
    }
    
    /**
     * debug map from script
     * @param debugMap
     */
    public void setDebugMap(Map<String, Object> debugMap) {
      this.debugMap = debugMap;
    }

    private Integer resultCode;
    
    /**
     * @return the resultCode
     */
    public Integer getResultCode() {
      return this.resultCode;
    }
    
    /**
     * @param resultCode the resultCode to set
     */
    public void setResultCode(Integer resultCode) {
      this.resultCode = resultCode;
    }
    
    /**
     * output
     */
    private StringBuilder outString = null;
    
    /**
     * @return the outString
     */
    public String getOutString() {
      return this.outString.toString();
    }
    
    /**
     * 
     * @param outString
     */
    public void setOutString(StringBuilder outString) {
      this.outString = outString;
    }

    /**
     * millis that this took
     */
    private long millis;
    
    /**
     * millis that this took
     * @return the millis
     */
    public long getMillis() {
      return this.millis;
    }
    
    /**
     * millis that this took
     * @param millis1 the millis to set
     */
    public void setMillis(long millis1) {
      this.millis = millis1;
    }

    public void setException(RuntimeException exception) {
      this.exception = exception;
    }
    
    private RuntimeException exception;

    
    public RuntimeException getException() {
      return exception;
    }

    public String fullOutput() {
      if (GrouperUtil.length(this.debugMap) > 0 || (this.outString != null && this.outString.length() > 0)) {
        StringBuilder fullOutput = new StringBuilder();
        if (GrouperUtil.length(this.debugMap) > 0) {
          fullOutput.append("Debug map: ").append(GrouperUtil.trim(GrouperUtil.mapToString(this.debugMap)));
        }
        if (this.outString != null && this.outString.length() > 0) {
          if (fullOutput.length() > 0) {
            fullOutput.append("\n");
          }
          fullOutput.append("Output: ").append(GrouperUtil.trim(this.outString.toString()));
        }
        return fullOutput.toString();
      }
      return null;
    }
  }
  
  /**
   * run a script and return the result.  Note, check for exception and rethrow.
   * Note this uses
   * @param script
   * @return the result
   */
  public static GrouperGroovyResult runScript(String script) {
    return runScript(script, false);
  }

  /**
   * run a script and return the result.  Note, check for exception and rethrow.
   * Note this uses
   * @param script
   * @param lightWeight will use an abbreviated groovysh.profile for faster speed.  built in commands
   * arent there and imports largely arent there
   * @return the result
   */
  public static GrouperGroovyResult runScript(String script, boolean lightWeight) {
    GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();
    runScript(new GrouperGroovyInput().assignScript(script).assignLightWeight(lightWeight), grouperGroovyResult);
    return grouperGroovyResult;
  }

  /**
   * run a script and return the result.  Note, check for exception and rethrow.
   * Note this uses
   * @param script
   * @param lightWeight will use an abbreviated groovysh.profile for faster speed.  built in commands
   * arent there and imports largely arent there
   * @return the result
   * @deprecated since sendErrToOut doesnt make sense
   */
  public static GrouperGroovyResult runScript(String script, boolean lightWeight, boolean sendErrToOut) {
    return runScript(script, lightWeight);
  }

  /**
   * cache 10000 scripts
   */
  private static Map<String, GrouperGroovyScript> scriptCache = Collections.synchronizedMap(new LinkedHashMap<String, GrouperGroovyScript>() {

    @Override
    protected boolean removeEldestEntry(Entry<String, GrouperGroovyScript> eldest) {
      return size() > 10000;
    }
    
    
  });
  
  
  private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
  
  private static Compilable scriptEngine = (Compilable)scriptEngineManager.getEngineByName("groovy");
  
  /**
   * run a script and return the result.  Note, check for exception and rethrow.
   * Note this uses
   * @param script
   * @param lightWeight will use an abbreviated groovysh.profile for faster speed.  built in commands
   * arent there and imports largely arent there
   * @return the result
   */
  public static void runScript(GrouperGroovyInput grouperGroovyInput, GrouperGroovyResult grouperGroovyResult) {
    
    grouperGroovyResult = grouperGroovyResult == null ? new GrouperGroovyResult() : grouperGroovyResult;
    
    long[] startNanos = new long[] {System.nanoTime()};
    
    Throwable throwable = null;

    GrouperGroovyRuntime grouperGroovyRuntime = grouperGroovyInput.getGrouperGroovyRuntime() == null ? new GrouperGroovyRuntime() : grouperGroovyInput.getGrouperGroovyRuntime();
    grouperGroovyRuntime.assignThreadLocalGrouperGroovyRuntime();
    try {    
      StringBuilder script = new StringBuilder();
      if (grouperGroovyInput.isLightWeight()) {
        script.append(GrouperUtil.readResourceIntoString("groovy_lightWeight.profile", false)).append("\n");
      } else {
        script.append(GrouperUtil.readResourceIntoString("groovy.profile", false)).append("\n");
      }

      GrouperUtil.assertion(!StringUtils.isBlank(grouperGroovyInput.getScript()), "Script is required");
      
      script.append(grouperGroovyInput.getScript());
      
      String scriptString = script.toString();
      GrouperGroovyScript grouperGroovyScript = scriptCache.get(scriptString);
      
      if (grouperGroovyScript == null) {
        grouperGroovyScript = new GrouperGroovyScript();
        grouperGroovyScript.setScript(scriptString);
        try {
          CompiledScript compiledScript = scriptEngine.compile(scriptString);
          grouperGroovyScript.setCompiledScript(compiledScript);
        } catch (ScriptException scriptException) {
          throw new RuntimeException("error compiling script", scriptException);
        }
        scriptCache.put(scriptString, grouperGroovyScript);
      }
      
      CompiledScript compiledScript = grouperGroovyScript.getCompiledScript();
      
      grouperGroovyRuntime.setInputNameToValue(grouperGroovyInput.getInputNameToValue());
      grouperGroovyRuntime.setAverageExecutionTimeMillis(grouperGroovyScript.getAverageExecutionTimeMillis());

      if (!grouperGroovyInput.isRunAsRoot()) {
        // this throw exception if not there
        GrouperSession.staticGrouperSession();
      }
      final GrouperGroovyResult GROUPER_GROOVY_RESULT = grouperGroovyResult;
      
      GrouperSession.internal_callbackRootGrouperSession(grouperGroovyInput.isRunAsRoot(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          grouperGroovyRuntime.setGrouperSession(grouperSession);
          // reset after compile so we dont affect avg speed
          grouperGroovyRuntime.resetStartMillis1970();

          // something in thread might want results while running
          GROUPER_GROOVY_RESULT.setDebugMap(grouperGroovyRuntime.getDebugMap());
          GROUPER_GROOVY_RESULT.setOutString(grouperGroovyRuntime.getOutString());
          
          startNanos[0] = System.nanoTime();
          
          // might want to run this in transaction
          HibernateSession.callbackHibernateSession(
              grouperGroovyInput.isUseTransaction() ? GrouperTransactionType.READ_WRITE_OR_USE_EXISTING: GrouperTransactionType.NONE, 
                  AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                    
                    @Override
                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      try {
                        Object result = compiledScript.eval();
                        
                        GROUPER_GROOVY_RESULT.setResult(result);
                      } catch (ScriptException scriptException) {
                        if (scriptException.getCause() instanceof GrouperGroovyExit) {
                          GrouperGroovyExit grouperGroovyExit = (GrouperGroovyExit)scriptException.getCause();
                          if (grouperGroovyExit.getExitCode() != 0 || GrouperUtil.defaultIfNull(GROUPER_GROOVY_RESULT.getResultCode(), 0) == 0) {
                            GROUPER_GROOVY_RESULT.setResultCode(grouperGroovyExit.getExitCode());
                          }
                          // this is a normal exit
                        } else {
                          throw new RuntimeException("error running script", scriptException);
                        }
                      }
                      return null;
                    }
                  });
          return null;
        }
      });

      // only register when success
      grouperGroovyScript.registerExecutionTimeMillis((System.nanoTime() - startNanos[0])/1000000);
      
    } catch (RuntimeException e) {
      throwable = e;
      if (grouperGroovyResult.getResultCode() == null || grouperGroovyResult.getResultCode() == 0) {
        grouperGroovyResult.setResultCode(1);
      }
      grouperGroovyResult.setException(e);
    } finally {
      grouperGroovyRuntime.setPercentDone(100);
      grouperGroovyResult.setMillis((System.nanoTime() - startNanos[0]) / 1000000);
      
      // capture system out
      if (grouperGroovyResult.getResultCode() == null || grouperGroovyResult.getResultCode() == 0) {
        grouperGroovyResult.setResultCode(grouperGroovyRuntime.getResultCode());
      }
      GrouperGroovyRuntime.removeThreadLocalGrouperGroovyRuntime();
    }
    if (throwable != null) {
      GrouperUtil.injectInException(throwable, "Inputs:\n" + GrouperUtil.mapToString(grouperGroovyInput.getInputNameToValue()) + "\nScript (100k max):\n" + GrouperUtil.abbreviate(grouperGroovyInput.getScript(), 100000) + "\n, Output (1000k max):\n" + GrouperUtil.abbreviate(grouperGroovyResult.fullOutput(), 1000000));
    }
  }
  
  private boolean exitOnError;
  
  /**
   * dont call this, too much of a performance penalty
   */
  public static void addImports(CompilerConfiguration compilerConfiguration) {
    ImportCustomizer defaultImports = new ImportCustomizer();
    for (String thePackage : FindImports.ALL_PACKAGES) {
      defaultImports.addStarImports(thePackage);
    }
    compilerConfiguration.addCompilationCustomizers(defaultImports);
  }
  
  /**
   * @param io
   * @param compilerConfiguration
   * @param exitOnError
   */
  public GrouperGroovysh(IO io, CompilerConfiguration compilerConfiguration, boolean exitOnError) {
    super(io, compilerConfiguration);

    this.exitOnError = exitOnError;
  }

  private Throwable throwable;
  
  /**
   * @return the throwable
   */
  public Throwable getThrowable() {
    return this.throwable;
  }

  protected void displayError(final Throwable cause) {

    this.throwable = cause;
    
    if (exitOnError) {
      if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      }
      throw new RuntimeException("error", cause);
    }

    if (cause instanceof MissingPropertyException) {
      if (((MissingPropertyException) cause).getType() != null && Interpreter.getSCRIPT_FILENAME().equals(((MissingPropertyException) cause).getType().getCanonicalName())) {
          this.getIo().err.println("@|bold,red Unknown property|@: " + ((MissingPropertyException) cause).getProperty());
          return;
      }
    }

    super.getErrorHook().call(cause);
  }
  
  /**
   * see if there's a grouper session running; return if it's there otherwise start root session and return that.
   * @return
   */
  public static GrouperSession startRootSessionIfNoSessionRunning() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    
    if (grouperSession != null) {
      return grouperSession;
    } 
    
    return GrouperSession.startRootSession();
    
  }
}

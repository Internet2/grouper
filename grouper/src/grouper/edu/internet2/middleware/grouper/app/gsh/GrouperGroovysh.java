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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import jline.TerminalFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Interpreter;
import org.codehaus.groovy.tools.shell.Main;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import groovy.lang.MissingPropertyException;

/**
 * @author shilen
 */
public class GrouperGroovysh extends Groovysh {

  public static class GrouperGroovyResult {
    
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
    private String outString;
    
    /**
     * @return the outString
     */
    public String getOutString() {
      return this.outString;
    }
    
    /**
     * @param outString the outString to set
     */
    public void setOutString(String outString) {
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
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    
    // we dont need an input stream
    ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]);
    long startNanos = System.nanoTime();
    
    Throwable throwable = null;
    GrouperGroovysh shell = null;
    try {    
      Main.setTerminalType(TerminalFactory.AUTO, false);
      IO io = new IO(inStream, outStream, errStream);
      CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
      compilerConfiguration.setTolerance(0);
//      Logger.io = io;
      compilerConfiguration.setParameters(false);
      boolean exitOnError = GrouperConfig.retrieveConfig().propertyValueBoolean("gsh.exitOnProgrammaticError", true);
      shell = new GrouperGroovysh(io, compilerConfiguration, exitOnError);
      StringBuilder body = new StringBuilder(script);
      String profile = "groovysh.profile";
      if (lightWeight) {
        if (script.contains("gshFileLoad")) {
          profile = "groovysh_lightWeightWithFile.profile";
        } else {
          profile = "groovysh_lightWeight.profile";
        }
      }
      body.insert(0, ":load '" + GrouperUtil.fileFromResourceName(profile).getAbsolutePath() + "'\n");
      body.append("\n:exit");
      int code = shell.run(body.toString());
      grouperGroovyResult.setResultCode(code);
    } catch (RuntimeException e) {
      throwable = e;
    } finally {
      grouperGroovyResult.setMillis((System.nanoTime() - startNanos) / 1000000);
      try {
        grouperGroovyResult.setOutString(new String(outStream.toByteArray()));
      } catch (Exception e) {
        
      }
      GrouperUtil.closeQuietly(outStream);
      
      // ignore err since its in the exception
      // GrouperUtil.closeQuietly(errStream);

      GrouperUtil.closeQuietly(inStream);

    }
    if (shell != null && shell.getThrowable() != null) {
      throwable = shell.getThrowable();
    }
    if (throwable != null) {
      GrouperUtil.injectInException(throwable, "Script: " + GrouperUtil.abbreviate(script, 2000) + ", Output: " + GrouperUtil.abbreviate(grouperGroovyResult.getOutString(), 10000));
      if (throwable instanceof RuntimeException) {
        throw (RuntimeException)throwable;
      }
      throw new RuntimeException("error", throwable);
    }
    return grouperGroovyResult;
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
}

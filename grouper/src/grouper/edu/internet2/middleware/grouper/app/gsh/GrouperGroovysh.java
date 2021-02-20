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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.codehaus.groovy.tools.shell.Interpreter;
import org.codehaus.groovy.tools.shell.Main;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import groovy.lang.MissingPropertyException;
import jline.TerminalFactory;

/**
 * @author shilen
 */
public class GrouperGroovysh extends Groovysh {

  /**
   * hold the script so the GSH GSHScriptLoad can get the lines
   */
  private static ThreadLocal<List<String>> threadLocalScriptLines = new InheritableThreadLocal<List<String>>();

  /**
   * print out each line number in gsh script run from inside grouper
   * @param lineNumber
   * @param line
   */
  public static void printGshScriptLine(int lineNumber, String it) {
    System.out.println("groovy:" + StringUtils.leftPad(""+lineNumber, 3, "0") + "> " + it);
  }
  
  /**
   * hold the script so the GSH GSHScriptLoad can get the lines
   * @param script
   */
  public static void threadLocalScriptAssign(String script) {
    
    List<String> lines = new ArrayList<String>();
    
    if (script != null) {
      // harmonize line endings
      script = StringUtils.replace(script, "\r\n", "\n");
      script = StringUtils.replace(script, "\r", "\n");
      lines = GrouperUtil.toList(GrouperUtil.nonNull(GrouperUtil.split(script, "\n"), String.class));
    }

    threadLocalScriptLines.set(lines);
  }
  
  /**
   * remove the script so the GSH GSHScriptLoad is done
   * @param script
   */
  public static void threadLocalScriptRemove() {
    threadLocalScriptLines.remove();
  }  
  
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
    return runScript(script, false, true);
  }

  /**
   * hold the script so the GSH GSHScriptLoad can get the lines
   * @return
   */
  public static List<String> scriptLines() {
    List<String> lines = threadLocalScriptLines.get();
    if (lines == null) {
      throw new RuntimeException("script lines not set in threadlocal!");
    }
    return lines;
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
    return runScript(script, lightWeight, true);
  }

  /**
   * run a script and return the result.  Note, check for exception and rethrow.
   * Note this uses
   * @param script
   * @param lightWeight will use an abbreviated groovysh.profile for faster speed.  built in commands
   * arent there and imports largely arent there
   * @return the result
   */
  public static GrouperGroovyResult runScript(String script, boolean lightWeight, boolean sendErrToOut) {
    
    GrouperGroovyResult grouperGroovyResult = new GrouperGroovyResult();
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errStream = sendErrToOut ? outStream : new ByteArrayOutputStream();
    
    PrintStream oldOut = System.out;
    PrintStream newOut = null;
    PrintStream oldErr = System.err;
    PrintStream newErr = null;
    
    // we dont need an input stream
    ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]);
    long startNanos = System.nanoTime();
    
    Throwable throwable = null;
    GrouperGroovysh shell = null;
    try {    
      StringBuilder body = new StringBuilder();
      if (lightWeight) {
        body.append(":load '" + GrouperUtil.fileFromResourceName("groovysh_lightWeightWithFile.profile").getAbsolutePath() + "'\n");
      } else {
        body.append(":load '" + GrouperUtil.fileFromResourceName("groovysh.profile").getAbsolutePath() + "'\n");
      }

      Main.setTerminalType(TerminalFactory.AUTO, false);

      IO io = new IO(inStream, outStream, errStream);
      CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
      compilerConfiguration.setTolerance(0);
//      Logger.io = io;
      compilerConfiguration.setParameters(false);
      shell = new GrouperGroovysh(io, compilerConfiguration, true);
      threadLocalScriptAssign(script);
      
      // this means pull the script lines from a threadlocal and push into gsh into the same shell object so conditionals work
      body.append(":gshScriptLoad\n");
      
      body.append(":exit");
      
      // capture system out
      newOut = new PrintStream(outStream);
      System.setOut(newOut);
      newErr = new PrintStream(errStream);
      System.setErr(newErr);
      
      int code = shell.run(body.toString());
      grouperGroovyResult.setResultCode(code);
    } catch (RuntimeException e) {
      throwable = e;
    } finally {
      
      // revert system back
      System.setOut(oldOut);
      System.setErr(oldErr);
      
      threadLocalScriptRemove();
      grouperGroovyResult.setMillis((System.nanoTime() - startNanos) / 1000000);
      try {
        String outString = new String(outStream.toByteArray());
        // [32mGroovy Shell[m (2.5.0-beta-2, JVM: 1.8.0_161)
        outString = GrouperUtil.replace(outString, "[32mGroovy Shell[m ", "");
        
        // Type '[1m:help[m' or '[1m:h[m' for help.
        outString = GrouperUtil.replace(outString, "[1m", "");
        outString = GrouperUtil.replace(outString, "[m", "");
        // escape char 27
        outString = GrouperUtil.replace(outString, "", "");

        //  remove unneeded output
        //  (2.5.0-beta-2, JVM: 1.8.0_265)
        //  Type ':help' or ':h' for help.
        //  -------------------------------------------------------------------------------
        //  groovy:000> :gshScriptLoad
        //  groovy:001> if (true) {
        //  groovy:002>   System.err.println('true');
        //  groovy:003> } else {
        //  groovy:004>   System.out.println('false');
        //  groovy:005> }
        //  true
        //  ===> null
        //  groovy:000> :exit

        outString = GrouperUtil.replace(outString, "\r\n", "\n");
        outString = GrouperUtil.replace(outString, "\r", "\n");
        
        List<String> outStrings = GrouperUtil.toList(GrouperUtil.nonNull(GrouperUtil.split(outString, "\n"), String.class));
        Iterator<String> outStringIterator = outStrings.iterator();
        int lineNumber = 0;
        boolean currentLineComment = false;
        boolean previousLineComment = false;
        
        while (outStringIterator.hasNext()) {
          String line = StringUtils.defaultString(outStringIterator.next());
          if (lineNumber == 0 && line.startsWith("(")) {
            outStringIterator.remove();
          } else if (lineNumber == 1 && "Type ':help' or ':h' for help.".equals(line)) {
            outStringIterator.remove();
          } else if (lineNumber == 2 && line.startsWith("----------------------")) {
            outStringIterator.remove();
          } else if (line.startsWith("groovy:000>")) {
            outStringIterator.remove();
          } else if ("===> null".equals(line)) {
            outStringIterator.remove();
          } else if (line.startsWith("groovy:") && line.matches(".*>\\s+import .*")) {
            currentLineComment = true;
            // groovy:001> import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;  
            // ===> org.codehaus.groovy.tools.shell.CommandSupport, org.codehaus.groovy.tools.shell.Groovysh, edu.internet2.middleware.grouper.*, edu.in
          } else if (line.startsWith("===> ") && previousLineComment) {
             outStringIterator.remove();
          }
          lineNumber++;
          previousLineComment = currentLineComment;
          currentLineComment = false;
        }
        outString = GrouperUtil.join(outStrings.iterator(), '\n');
        
        grouperGroovyResult.setOutString(outString);
      } catch (Exception e) {
        
      }

      GrouperUtil.closeQuietly(newOut);
      GrouperUtil.closeQuietly(outStream);

      if (!sendErrToOut) {
        GrouperUtil.closeQuietly(newErr);
        // do we need to not do this?
        GrouperUtil.closeQuietly(errStream);
      }

      GrouperUtil.closeQuietly(inStream);

    }
    if (shell != null && shell.getThrowable() != null) {
      throwable = shell.getThrowable();
    }
    if (throwable != null) {
      GrouperUtil.injectInException(throwable, "Script (8k max):\n" + GrouperUtil.abbreviate(script, 8000) + ", Output (10k max):\n" + GrouperUtil.abbreviate(grouperGroovyResult.getOutString(), 10000));
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

package edu.internet2.middleware.grouper.util;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

public class CommandLineExec {

  private String command;
  private CommandLine commandLine;
  private DefaultExecutor executor = new DefaultExecutor();
  private CollectingLogOutputStream stdout = new CollectingLogOutputStream();
  private CollectingLogOutputStream stderr = new CollectingLogOutputStream();
  private PumpStreamHandler pumpStreamHandler;
  private int exitValue;
  private boolean errorOnNonZero = false;
  private boolean waitForCompletion = true;
  
  private File workingDirectory;
  
  /**
   * if true, then just run command and wait.  if false, then run in thread
   * @param theWaitForCompletion
   * @return this for chaining
   */
  public CommandLineExec assignWaitForCompletion(boolean theWaitForCompletion) {
    this.waitForCompletion = theWaitForCompletion;
    return this;
  }
  
  /**
   * if true, then just run command and wait.  if false, then run in thread
   * @return if wait
   */
  public boolean isWaitForCompletion() {
    return waitForCompletion;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  public CommandLineExec assignWorkingDirectory(File theWorkingDirectory) {
    this.workingDirectory = theWorkingDirectory;
    return this;
  }

  public boolean isErrorOnNonZero() {
    return errorOnNonZero;
  }

  
  public CommandLineExec assignErrorOnNonZero(boolean errorOnNonZero) {
    this.errorOnNonZero = errorOnNonZero;
    return this;
  }

  public CommandLineExec() {
  }

  public CommandLineExec assignCommand(String theCommand) {
    this.command = theCommand;
    this.commandLine = CommandLine.parse(theCommand);
    return this;
  }

  public CommandLineExec execute() {
    this.pumpStreamHandler = new PumpStreamHandler(stdout, stderr);
    this.executor.setStreamHandler(this.pumpStreamHandler);
    if (this.workingDirectory != null) {
      this.executor.setWorkingDirectory(this.workingDirectory);
    }
    Runnable runnable = new Runnable() {
      
      @Override
      public void run() {
        try {
          CommandLineExec.this.exitValue = executor.execute(CommandLineExec.this.commandLine);
        } catch (Exception ioe) {
          if (ioe instanceof RuntimeException) {
            GrouperUtil.injectInException(ioe, "command: '" + CommandLineExec.this.command + "'");
            if (CommandLineExec.this.workingDirectory != null) {
              GrouperUtil.injectInException(ioe, "workingDirectory: '" + CommandLineExec.this.workingDirectory.getAbsolutePath() + "'");
            }
            throw (RuntimeException)ioe;
          }
          throw new RuntimeException("command line error in command: '" + CommandLineExec.this.command + "'" 
              + (CommandLineExec.this.workingDirectory == null ? "" : (", workingDirectory: '" + CommandLineExec.this.workingDirectory.getAbsolutePath() + "'")), ioe);
        }
        if (CommandLineExec.this.errorOnNonZero && CommandLineExec.this.exitValue != 0) {
          throw new RuntimeException("Command exit value was '" + CommandLineExec.this.exitValue + "' but expecting '0', command: '" + CommandLineExec.this.command 
              + "'" + (CommandLineExec.this.workingDirectory == null ? "" : (", workingDirectory: '" + CommandLineExec.this.workingDirectory.getAbsolutePath() + "'")) 
              + ", output: '" + CommandLineExec.this.getStdout().getAllLines() + "', error: '" + CommandLineExec.this.getStderr().getAllLines() + "'");
        }
        
      }
    };
    
    if (this.waitForCompletion) {
      runnable.run();
    } else {
      this.thread = new Thread(runnable);
      this.thread.start();
      GrouperUtil.sleep(1000);
    }
    
    return this;
  }

  private Thread thread = null;
  
  public Thread getThread() {
    return thread;
  }

  public String getCommand() {
    return command;
  }

  
  public CommandLine getCommandLine() {
    return commandLine;
  }

  
  public DefaultExecutor getExecutor() {
    return executor;
  }

  
  public CollectingLogOutputStream getStdout() {
    return stdout;
  }

  
  public CollectingLogOutputStream getStderr() {
    return stderr;
  }

  
  public PumpStreamHandler getPumpStreamHandler() {
    return pumpStreamHandler;
  }

  
  public int getExitValue() {
    return exitValue;
  }
  
}

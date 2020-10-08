package edu.internet2.middleware.grouper.util;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class CommandLineExec {

  private String command;
  private CommandLine commandLine;
  private DefaultExecutor executor = new DefaultExecutor();
  private CollectingLogOutputStream stdout = new CollectingLogOutputStream();
  private CollectingLogOutputStream stderr = new CollectingLogOutputStream();
  private PumpStreamHandler pumpStreamHandler;
  private int exitValue;
  private boolean errorOnNonZero = false;
  
  private File workingDirectory;
  
  
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
    try {
      this.exitValue = executor.execute(this.commandLine);
    } catch (Exception ioe) {
      if (ioe instanceof RuntimeException) {
        GrouperUtil.injectInException(ioe, "command: '" + this.command + "'");
        if (this.workingDirectory != null) {
          GrouperUtil.injectInException(ioe, "workingDirectory: '" + this.workingDirectory.getAbsolutePath() + "'");
        }
        throw (RuntimeException)ioe;
      }
      throw new RuntimeException("command line error in command: '" + this.command + "'" 
          + (this.workingDirectory == null ? "" : (", workingDirectory: '" + this.workingDirectory.getAbsolutePath() + "'")), ioe);
    }
    if (this.errorOnNonZero && this.exitValue != 0) {
      throw new RuntimeException("Command exit value was '" + this.exitValue + "' but expecting '0', command: '" + this.command 
          + "'" + (this.workingDirectory == null ? "" : (", workingDirectory: '" + this.workingDirectory.getAbsolutePath() + "'")) 
          + ", output: '" + this.getStdout().getAllLines() + "', error: '" + this.getStderr().getAllLines() + "'");
    }
    return this;
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

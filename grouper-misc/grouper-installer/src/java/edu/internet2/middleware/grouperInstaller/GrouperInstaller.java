/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouperInstaller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouperInstaller.util.GiDbUtils;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils.CommandResult;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.httpclient.methods.GetMethod;

/**
 * Install grouper
 * @author mchyzer
 *
 */
public class GrouperInstaller {

  /**
   * default ip address to listen for stuff
   */
  private String defaultIpAddress = null;
  

  /**
   * if we should continue or not
   * @return if should continue
   */
  private static boolean shouldContinue() {
    return shouldContinue(null, null);
  }
  /**
   * if we should continue or not
   * @param hint 
   * @param exitHint 
   * @return if should continue
   */
  private static boolean shouldContinue(String hint, String exitHint) {
    if (hint == null) {
      hint = "Do you want to continue ";
    }
    if (!hint.endsWith(" ")) {
      hint += " ";
    }
    System.out.print(hint + "(t|f)? [f] ");
    boolean shouldContinue = readFromStdInBoolean(false);
    if (!shouldContinue) {
      if (exitHint == null) {
        exitHint = "OK, will not continue, exiting...";
      }
      if (!GrouperInstallerUtils.isBlank(exitHint)) {
        System.out.println(exitHint);
      }
    }
    return shouldContinue;
  }
  
  /**
   * read a string from stdin
   * @param defaultBoolean null for none, or true of false for if the input is blank
   * @return the string
   */
  private static boolean readFromStdInBoolean(Boolean defaultBoolean) {
    
    //keep trying until we get it
    while(true) {
      String input = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(input) && defaultBoolean != null) {
        return defaultBoolean;
      }
      try {
        boolean inputBoolean = GrouperInstallerUtils.booleanValue(input);
        return inputBoolean; 
      } catch (Exception e) {
        if (defaultBoolean != null) {
          System.out.print("Expecting t or f or <blank> but received: '" + input + "', please try again: ");
        } else {
          System.out.print("Expecting t or f but received: '" + input + "', please try again: ");
        }
      }
    }
  }
  
  /**
   * read a string from stdin
   * @return the string
   */
  private static String readFromStdIn() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String str = in.readLine();
      return GrouperInstallerUtils.trim(str);
    } catch (Exception e) {
      throw new RuntimeException("Problem", e);
    }
  }
  
  /**
   * download a file, delete the local file if it exists
   * @param url
   * @param localFileName
   */
  private static void downloadFile(String url, String localFileName) {
    
    System.out.println("Downloading from URL: " + url + " to file: " + localFileName);

    boolean useLocalFile = false;

    File localFile = new File(localFileName);

    if (localFile.exists()) {
      System.out.print("File exists: " + localFile.getAbsolutePath() + ", should we use the local file (t|f)? [t]: ");
      useLocalFile = readFromStdInBoolean(true);
    }
    
    if (useLocalFile) {
      return;
    }
    
    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url);
    try {
      int result = httpClient.executeMethod(getMethod);
      
      if (result != 200) {
        throw new RuntimeException("Expecting 200 but received: " + result);
      }
      
      InputStream inputStream = getMethod.getResponseBodyAsStream();
      
      if (localFile.exists()) {
        
        System.out.println("File exists: " + localFile.getAbsolutePath() + ", deleting");
        
        if (!localFile.delete()) {
          throw new RuntimeException("Cant delete file: " + localFile.getAbsolutePath() + "!!!!!");
        }
      }
      
      FileOutputStream fileOutputStream = new FileOutputStream(localFile);

      GrouperInstallerUtils.copy(inputStream, fileOutputStream);
      
    } catch (Exception exception) {
      String errorMessage = "Error connecting to URL: " + url;
      System.out.println(errorMessage);
      throw new RuntimeException(errorMessage, exception);
    }
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {

    GrouperInstaller grouperInstaller = new GrouperInstaller();
    grouperInstaller.mainLogic(args);


    
//    GrouperInstaller grouperInstaller = new GrouperInstaller();
//
//    grouperInstaller.upgradeExistingApplicationDirectoryString = "C:\\app\\grouper_2_1_installer\\grouper.ui-2.1.5\\dist\\grouper\\";
//  
//    grouperInstaller.grouperBaseBakDir = "C:\\app\\grouperInstallerTarballDir\\bak_UI_2014_10_26_15_06_19_957\\";
//      
//    grouperInstaller.copyFiles("C:\\app\\grouperInstallerTarballDir\\grouper.ui-2.2.1\\dist\\grouper\\",
//        "C:\\app\\grouper_2_1_installer\\grouper.ui-2.1.5\\dist\\grouper\\",
//        GrouperInstallerUtils.toSet("WEB-INF/lib", "WEB-INF/web.xml", "WEB-INF/classes"));

    
   // copyFiles();
    
//    
//    
//    grouperInstaller.dbUrl = "jdbc:hsqldb:hsql://localhost:9001/grouper";
//    grouperInstaller.dbUser = "sa";
//    grouperInstaller.dbPass = "";
//    grouperInstaller.giDbUtils = new GiDbUtils(grouperInstaller.dbUrl, grouperInstaller.dbUser, grouperInstaller.dbPass);
//    grouperInstaller.untarredApiDir = new File("c:/mchyzer/grouper/trunk/grouper-installer/grouper.apiBinary-2.0.2");
//    grouperInstaller.grouperInstallDirectoryString = "C:/mchyzer/grouper/trunk/grouper-installer/";
//    
//    grouperInstaller.version = "2.0.2";
//    grouperInstaller.grouperSystemPassword = "myNewPass";
//    
//    grouperInstaller.addDriverJarToClasspath();
////      
//    grouperInstaller.startHsqlDb();
//    grouperInstaller.checkDatabaseConnection();
////      grouperInstaller.initDb();
////      
////      grouperInstaller.addQuickstartSubjects();
////      grouperInstaller.addQuickstartData();
//    
////    File uiDir = grouperInstaller.downloadUi();
////    File unzippedUiFile = unzip(uiDir.getAbsolutePath());
////    grouperInstaller.untarredUiDir = untar(unzippedUiFile.getAbsolutePath());
//
////      grouperInstaller.configureUi();
////
//      File antDir = grouperInstaller.downloadAnt();
//      File unzippedAntFile = unzip(antDir.getAbsolutePath());
//      grouperInstaller.untarredAntDir = untar(unzippedAntFile.getAbsolutePath());
////
////      grouperInstaller.buildUi();
//
//    File tomcatDir = grouperInstaller.downloadTomcat();
//    File unzippedTomcatFile = unzip(tomcatDir.getAbsolutePath());
//    grouperInstaller.untarredTomcatDir = untar(unzippedTomcatFile.getAbsolutePath());
//
//    grouperInstaller.configureTomcat();
//    
////    grouperInstaller.configureTomcatUiWebapp();
////    
//    grouperInstaller.tomcatConfigureGrouperSystem();
//    
////    grouperInstaller.tomcatBounce("restart");
//
//    File wsDir = grouperInstaller.downloadWs();
//
//    File unzippedWsFile = unzip(wsDir.getAbsolutePath());
//    grouperInstaller.untarredWsDir = untar(unzippedWsFile.getAbsolutePath());
//    grouperInstaller.configureWs();
//    grouperInstaller.buildWs();
//    
//    grouperInstaller.configureTomcatWsWebapp();
//    grouperInstaller.tomcatBounce("restart");
//
//    File clientDir = grouperInstaller.downloadClient();
//    
//    File unzippedClientFile = unzip(clientDir.getAbsolutePath());
//    grouperInstaller.untarredClientDir = untar(unzippedClientFile.getAbsolutePath());
//    grouperInstaller.configureClient();
//
//    grouperInstaller.addGrouperSystemWsGroup();
//    
//    grouperInstaller.runClientCommand();
//    
//    //CommandResult commandResult = GrouperInstallerUtils.execCommand("cmd /c cd");
//    //String result = commandResult.getOutputText();
//    //
//    //System.out.println(result);
//
//
//    //editPropertiesFile(new File("C:\\mchyzer\\grouper\\trunk\\grouper-installer\\grouper.apiBinary-2.1.0\\conf\\grouper.hibernate.properties"), 
//    //    "hibernate.connection.password", "sdf");
      
    //if started hsql, then we need to exit since that thread will not stop
    System.exit(0);
  }

  /** e.g. 2.1.0 */
  private String version;
  
  /**
   * @param isInstallNotUpgrade 
   * 
   */
  private void buildUi(boolean isInstallNotUpgrade) {
    
    File grouperUiBuildToDir = new File(this.grouperUiBuildToDirName());
    
    boolean rebuildUi = true;
    
    if (grouperUiBuildToDir.exists()) {
      boolean defaultRebuild = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.ui.rebuildIfBuilt", true, false);
      System.out.print("The Grouper UI has been built in the past, do you want it rebuilt? (t|f) [" 
          + (defaultRebuild ? "t" : "f") + "]: ");
      rebuildUi = readFromStdInBoolean(defaultRebuild);
    }
    
    if (!rebuildUi) {
      return;
    }

    if (isInstallNotUpgrade) {
      //stop tomcat
      try {
        tomcatBounce("stop");
      } catch (Exception e) {
        System.out.println("Couldnt stop tomcat, ignoring...");
      }
    }
    
    List<String> commands = new ArrayList<String>();
    
    addAntCommands(commands);
    commands.add("dist");
    
    System.out.println("\n##################################");
    System.out.println("Building UI with command:\n" + this.untarredUiDir.getAbsolutePath() + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, this.untarredUiDir, null);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }
    
    if (isInstallNotUpgrade) {
      System.out.print("Do you want to set the log dir of UI (t|f)? [t]: ");
      boolean setLogDir = readFromStdInBoolean(true);
      
      if (setLogDir) {
        
        ////set the log dir
        //C:\apps\grouperInstallerTest\grouper.ws-2.0.2\grouper-ws\build\dist\grouper-ws\WEB-INF\classes\log4j.properties
        //
        //${grouper.home}logs
  
        String defaultLogDir = this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "grouperUi";
        System.out.print("Enter the UI log dir: [" + defaultLogDir + "]: ");
        String logDir = readFromStdIn();
        logDir = GrouperInstallerUtils.defaultIfBlank(logDir, defaultLogDir);
        
        //lets replace \\ with /
        logDir = GrouperInstallerUtils.replace(logDir, "\\\\", "/");
        //lets replace \ with /
        logDir = GrouperInstallerUtils.replace(logDir, "\\", "/");
  
        File log4jFile = new File(grouperUiBuildToDirName() + File.separator + "WEB-INF" + File.separator + "classes"
            + File.separator + "log4j.properties");
  
        System.out.println("Editing file: " + log4jFile.getAbsolutePath());
  
        //log4j.appender.grouper_event.File = c:/apps/grouperInstallerTest/grouper.apiBinary-2.0.2/logs/grouper_event.log
        editFile(log4jFile, "log4j\\.\\S+\\.File\\s*=\\s*([^\\s]+logs)/grouper_[^\\s]+\\.log", null, 
            null, logDir, "UI log directory");
  
        File logDirFile = new File(defaultLogDir);
        if (!logDirFile.exists()) {
          System.out.println("Creating log directory: " + logDirFile.getAbsolutePath());
          GrouperInstallerUtils.mkdirs(logDirFile);
        }
        //test log dir
        File testLogDirFile = new File(logDirFile.getAbsolutePath() + File.separator + "testFile" + GrouperInstallerUtils.uniqueId() + ".txt");
        GrouperInstallerUtils.saveStringIntoFile(testLogDirFile, "test");
        if (!testLogDirFile.delete()) {
          throw new RuntimeException("Cant delete file: " +  testLogDirFile.getAbsolutePath());
        }
        System.out.println("Created and deleted a test file successfully in dir: " + logDirFile.getAbsolutePath());
      }
    }    

    
    System.out.println("\nEnd building UI");
    System.out.println("##################################\n");

    
  }

  /** sh command */
  private String shCommand;
  
  /**
   * 
   * @return the sh command in unix
   */
  private String shCommand() {
    if (GrouperInstallerUtils.isWindows()) {
      throw new RuntimeException("This is windows, why are you looking for sh???");
    }
    
    if (!GrouperInstallerUtils.isBlank(this.shCommand)) {
      return this.shCommand;
    }
    
    String[] attempts = new String[]{
        "bash", "/bin/bash", 
        "/sbin/bash", "/usr/local/bin/bash", 
        "/usr/bin/bash", "/usr/sbin/bash", 
        "/usr/local/sbin/bash", "sh", "/bin/sh", 
        "/sbin/sh", "/usr/local/bin/sh", 
        "/usr/bin/sh", "/usr/sbin/sh", 
        "/usr/local/sbin/sh"}; 
    
    for (String attempt : attempts) {
    
      try {
        CommandResult commandResult = GrouperInstallerUtils.execCommand(
            attempt, 
            new String[]{"-version"});
        String shResult = commandResult.getOutputText();
        if (GrouperInstallerUtils.isBlank(shResult)) {
          shResult = commandResult.getErrorText();
        }
  
        //if we get a result, thats good
        if (!GrouperInstallerUtils.isBlank(shResult)) {
          this.shCommand = attempt;
          System.out.println("Using shell command: " + attempt);
          return this.shCommand;
        }
        
      } catch (Exception e) {
        //this is ok, keep trying
      }
    }
    //ok, we couldnt find it, 
    System.out.print("Couldn't find the command 'sh'.  Enter the path of 'sh' (e.g. /bin/sh): ");
    this.shCommand = readFromStdIn();

    try {
      CommandResult commandResult = GrouperInstallerUtils.execCommand(
          this.shCommand, 
          new String[]{"-version"});
      String shResult = commandResult.getOutputText();
      if (GrouperInstallerUtils.isBlank(shResult)) {
        shResult = commandResult.getErrorText();
      }

      //if we get a result, thats good
      if (!GrouperInstallerUtils.isBlank(shResult)) {
        return this.shCommand;
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Error: couldn't run: " + this.shCommand + " -version!", e);
    }

    throw new RuntimeException("Error: couldn't run: " + this.shCommand + " -version!");
    
  }

  /**
   * 
   * @param commands
   */
  private void addGshCommands(List<String> commands) {
    if (GrouperInstallerUtils.isWindows()) {
      commands.add("cmd");
      commands.add("/c");
      commands.add(gshCommand());
    } else {
      //if you add this it messes up when args have spaces
      //commands.add(shCommand());
      commands.add(gshCommand());
    }
  }

  /**
   * 
   * @param commands
   */
  private void addAntCommands(List<String> commands) {
    if (GrouperInstallerUtils.isWindows()) {
      commands.add("cmd");
      commands.add("/c");
      commands.add(this.untarredAntDir.getAbsolutePath() + File.separator + "bin" + File.separator + "ant.bat");
    } else {
      commands.add(shCommand());
      commands.add(this.untarredAntDir.getAbsolutePath() + File.separator + "bin" + File.separator + "ant");
    }
  }

//  /**
//   * @param arg  stop, start, etc
//   * 
//   */
//  private void bounceTomcat(String arg) {
//    
//    List<String> commands = new ArrayList<String>();
//    
//    commands.add("cmd");
//    commands.add("/c");
//    commands.add("start");
//    commands.add("/b");
//    commands.add(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + "catalina.bat");
//    commands.add(arg);
//    
//    System.out.println("\n##################################");
//    System.out.println("Tomcat " + arg + " with command:"
//        + convertCommandsIntoCommand(commands) + "\n");
//    
//    DOESNT WORK WITH ENV VARS!!!!!
//    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
//        true, true, new String[]{"CATALINA_HOME=" + this.untarredTomcatDir.getAbsolutePath(), 
//        "JAVA_HOME=" + GrouperInstallerUtils.javaHome(),
//        "LOGGING_CONFIG=" + this.untarredTomcatDir + File.separator + "conf" + File.separator + "logging.properties"}, 
//        new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"));
//        
//    
//    System.out.println("stderr: " + commandResult.getErrorText());
//    System.out.println("stdout: " + commandResult.getOutputText());
//    
//    System.out.println("\nEnd tomcat " + arg );
//    System.out.println("##################################\n");
//
//    
//  }
  
  /**
   * @param arg
   * 
   */
  private void tomcatBounce(String arg) {
    
    if (!GrouperInstallerUtils.equals("start", arg) && !GrouperInstallerUtils.equals("stop", arg) && !GrouperInstallerUtils.equals("restart", arg)) {
      throw new RuntimeException("Expecting arg: start|stop|restart but received: " + arg);
    }
    
    if (GrouperInstallerUtils.equals("restart", arg)) {
      
      tomcatBounce("stop");
      tomcatBounce("start");
      return;
    }
    
    if (GrouperInstallerUtils.equals("stop", arg)) {
      
      if (GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress)) {
        System.out.println("Tomcat is supposed to be listening on port: " + this.tomcatHttpPort + ", port not listening, assuming tomcat is not running...");
        if (!shouldContinue("Should we " + arg + " tomcat anyway?", "")) {
          return;
        }
      }

      
    } else {
      if (!GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress)) {
        System.out.println("Tomcat is supposed to be listening on port: " + this.tomcatHttpPort + ", port is already listening!!!!  Why is this????");
        if (!shouldContinue("Should we " + arg + " tomcat anyway?", "")) {
          return;
        }
      }
      
    }
    
    final List<String> commands = new ArrayList<String>();
    
//    <java jar="${tomcat.home}/bin/bootstrap.jar" fork="true">
//    03          <jvmarg value="-Dcatalina.home=${tomcat.home}"/>
//    04      </java>
//    05  </target>
//    06   
//    07  <target name="tomcat-stop">
//    08      <java jar="${tomcat.home}/bin/bootstrap.jar" fork="true">
//    09          <jvmarg value="-Dcatalina.home=${tomcat.home}"/>
//    10          <arg line="stop"/>
//    11      </java>
    
    commands.add(GrouperInstallerUtils.javaCommand());
    commands.add("-XX:MaxPermSize=150m");
    commands.add("-Xmx640m");
    
    commands.add("-Dcatalina.home=" + this.untarredTomcatDir.getAbsolutePath());
    //commands.add("-Djava.util.logging.config.file=" + this.untarredTomcatDir.getAbsolutePath() + File.separator + "conf" + File.separator + "logging.properties");
    
    commands.add("-jar");
    commands.add(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + "bootstrap.jar");
    
    if (GrouperInstallerUtils.equals("stop", arg)) {
      commands.add("stop");
    }
    
    System.out.println("\n##################################");
    
    String command = "start".equals(arg) ? "startup" : "shutdown";
    
    System.out.println("Tomcat " + arg + " with command (note you need CATALINA_HOME and JAVA_HOME set):\n  "
        + this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + command
        + (GrouperInstallerUtils.isWindows() ? ".bat" : ".sh") + "\n");
    
    //dont wait
    boolean waitFor = GrouperInstallerUtils.equals("stop", arg) ? true : false;
    
    if (waitFor) {
      try {
        CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
            true, true, null, 
            new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null);
        
        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }
      } catch (Throwable e) {
        e.printStackTrace();
        if (!shouldContinue()) {
          return;
        }
      }
    } else {
      //start in new thread
      Thread thread = new Thread(new Runnable() {
        
        @Override
        public void run() {
          GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
              true, true, null, 
              new File(GrouperInstaller.this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), 
              GrouperInstaller.this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "catalina");
        }
      });
      thread.setDaemon(true);
      thread.start();

    }
    
    System.out.println("\nEnd tomcat " + arg + " (note: logs are in " + this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs)");
    System.out.println("##################################\n");

    System.out.print("Should we check ports to see if tomcat was able to " + arg + " (t|f)? [t]: ");
    
    boolean shouldCheckTomcat = readFromStdInBoolean(true);
    
    if (shouldCheckTomcat) {
      System.out.print("Waiting for tomcat to " + arg +  "...");
      boolean success = false;
      for (int i=0;i<60;i++) {
        GrouperInstallerUtils.sleep(1000);
        //check port
        boolean portAvailable = GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress);
        if (GrouperInstallerUtils.equals("start", arg)) {
          if (!portAvailable) {
            success = true;
            System.out.println("\nTomcat listening on port: " + this.tomcatHttpPort);
            break;
          }
        } else {
          if (portAvailable) {
            success = true;
            System.out.println("\nTomcat not listening on port: " + this.tomcatHttpPort);
            break;
          }
        }
        System.out.print(".");
      }
      if (!success) {
        throw new RuntimeException("Trying to " + arg + " tomcat but couldnt properly detect " + arg + " on port " + this.tomcatHttpPort);
      }
    } else {
      System.out.println("Waiting 10 seconds for tomcat to " + arg + "...");
      GrouperInstallerUtils.sleep(10000);
    }
  }
  
  /**
   * 
   */
  private void addDriverJarToClasspath() {
    String jarName = this.giDbUtils.builtinJarName();
    
    File driverJar = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "lib" + File.separator + "jdbcSamples" + File.separator + jarName);
    GrouperInstallerUtils.classpathAddFile(driverJar);
  }

  /** db url */
  private String dbUrl;

  /** db user */
  private String dbUser;

  /** db pass */
  private String dbPass;

  /** untarred dir */
  private File untarredApiDir;

  /** untarred dir */
  private File untarredUiDir;

  /** untarred dir */
  private File untarredWsDir;

  /** untarred dir */
  private File untarredAntDir;

  /** untarred dir */
  private File untarredTomcatDir;

  /** main install dir, must end in file separator */
  private String grouperTarballDirectoryString;
  
  /** base bak dir for backing up files that are upgraded, ends in File separator */
  private String grouperBaseBakDir;
  
  /** grouper system password */
  private String grouperSystemPassword;
  
  /**
   * 
   */
  private void tomcatConfigureGrouperSystem() {
    
    while (true) {
      System.out.print("Enter the GrouperSystem password: ");
      this.grouperSystemPassword = readFromStdIn();
      this.grouperSystemPassword = GrouperInstallerUtils.defaultString(this.grouperSystemPassword);
      
      if (!GrouperInstallerUtils.isBlank(this.grouperSystemPassword)) {
        break;
      }
      System.out.println("The GrouperSystem password cannot be blank!");
    }

    System.out.print("Do you want to set the GrouperSystem password in " + this.untarredTomcatDir + File.separator + "conf" + File.separator + "tomcat-users.xml? [t]: ");
    boolean setGrouperSystemPassword = readFromStdInBoolean(true);
    if (setGrouperSystemPassword) {

      //write to the tomcat_users file
      //get the password
      File tomcatUsersXmlFile = new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "conf" + File.separator + "tomcat-users.xml");
      String existingPassword = GrouperInstallerUtils.xpathEvaluateAttribute(tomcatUsersXmlFile, "tomcat-users/user[@username='GrouperSystem']", "password");
      
      System.out.println("Editing file: " + tomcatUsersXmlFile.getAbsolutePath());

      NodeList existingRole = GrouperInstallerUtils.xpathEvaluate(tomcatUsersXmlFile, "tomcat-users/role");
      
      //<role rolename="grouper_user"/>
      //<user username="GrouperSystem" password="chang3m3" roles="grouper_user"/>

      
      if (existingPassword == null) {

        addToXmlFile(tomcatUsersXmlFile, ">",  new String[]{"<tomcat-users"}, "<user username=\"GrouperSystem\" password=\"" 
            + this.grouperSystemPassword + "\" roles=\"grouper_user\"/>", "Tomcat user GrouperSystem");
         
      } else {
        
        if (GrouperInstallerUtils.equals(existingPassword, this.grouperSystemPassword)) {
          System.out.println("  - password is already set to that value, leaving file unchanged...");

        } else {
          
          editFile(tomcatUsersXmlFile, "password=\"([^\"]*)\"", new String[]{"<user", "username=\"GrouperSystem\""}, 
              null, this.grouperSystemPassword, "Tomcat password for user GrouperSystem");
          
        }
        
      }

      if (existingRole == null || existingRole.getLength() == 0) {
        
        //add the role
        addToXmlFile(tomcatUsersXmlFile, ">",  new String[]{"<tomcat-users"}, "<role rolename=\"grouper_user\"/>", "Tomcat role grouper_user");
        
      }
    }
    
  }
  
  /**
   * 
   */
  private void configureUi() {
    //build properties file
    File buildPropertiesFile = new File(this.untarredUiDir.getAbsolutePath() + File.separator + "build.properties");
    if (!buildPropertiesFile.exists()) {
      File buildPropertiesTemplateFile = new File(this.untarredUiDir.getAbsolutePath() + File.separator + "build.properties.template");
      System.out.println("Copying file: " + buildPropertiesTemplateFile.getAbsolutePath() + " to file: " + buildPropertiesFile);
      GrouperInstallerUtils.copyFile(buildPropertiesTemplateFile, buildPropertiesFile);
    }
    
    //set the grouper property
    System.out.println("Editing " + buildPropertiesFile.getAbsolutePath() + ": ");
    String apiDir = GrouperInstallerUtils.replace(this.untarredApiDir.getAbsolutePath(),"\\\\", "/");
    apiDir = GrouperInstallerUtils.replace(apiDir, "\\", "/");
    editPropertiesFile(buildPropertiesFile, "grouper.folder", apiDir);
    editPropertiesFile(buildPropertiesFile, "should.copy.context.xml.to.metainf", "false");
    
  }
  
  /**
   * 
   */
  private void configureWs() {
    //build properties file
    File buildPropertiesFile = new File(this.untarredWsDir.getAbsolutePath() + File.separator 
        + "grouper-ws" + File.separator + "build.properties");
//    if (!buildPropertiesFile.exists()) {
//      File buildPropertiesTemplateFile = new File(this.untarredUiDir.getAbsolutePath() + File.separator + "build.properties.template");
//      System.out.println("Copying file: " + buildPropertiesTemplateFile.getAbsolutePath() + " to file: " + buildPropertiesFile);
//      GrouperInstallerUtils.copyFile(buildPropertiesTemplateFile, buildPropertiesFile);
//    }
    
    //set the grouper property
    System.out.println("Editing " + buildPropertiesFile.getAbsolutePath() + ": ");
    String apiDir = GrouperInstallerUtils.replace(this.untarredApiDir.getAbsolutePath(),"\\\\", "/");
    apiDir = GrouperInstallerUtils.replace(apiDir, "\\", "/");
    editPropertiesFile(buildPropertiesFile, "grouper.dir", apiDir);
    
  }

  /**
   * 
   * @param args
   */
  private void mainLogic(String[] args) {

    boolean install = this.grouperInstallOrUpgrade();
    if (install) {
      mainInstallLogic(args);
    } else {
      mainUpgradeLogic(args);
    }
    
  }
  /**
   * 
   */
  private void reportOnConflictingJars() {
    
    System.out.println("\n##################################");
    System.out.println("Looking for conflicting jars\n");

    //look for conflicting jars
    List<File> allLibraryJars = findAllLibraryFiles();
    
    Set<String> alreadyProcessed = new HashSet<String>();
    
    for (File jarFile : allLibraryJars) {
      
      String baseName = GrouperInstallerUtils.jarFileBaseName(jarFile.getName());
      
      //dont print multiple times
      if (alreadyProcessed.contains(baseName)) {
        continue;
      }
      
      alreadyProcessed.add(baseName);
      
      List<File> relatedFiles = GrouperInstallerUtils.jarFindJar(allLibraryJars, jarFile.getName());
      
      if (GrouperInstallerUtils.length(relatedFiles) > 1) {
        System.out.println("There is a conflicting jar: " + GrouperInstallerUtils.toStringForLog(relatedFiles));
        System.out.println("You should probably delete one of these files (oldest one?) or consult the Grouper team.");
        System.out.println("Press <enter> to continue...");
        readFromStdIn();
      }
      
      if (GrouperInstallerUtils.length(relatedFiles) == 0) {
        System.out.println("Why is jar file not found??? " + jarFile.getAbsolutePath());
      }
      
    }
  }
  
  /**
   * which app is being upgraded
   */
  private AppToUpgrade appToUpgrade;
  
  /**
   * 
   * @param args
   */
  private void mainUpgradeLogic(String[] args) {

    System.out.println("You should backup your files and database before you start.  Press <enter> to continue.");
    readFromStdIn();
    
    System.out.println("\n##################################");
    System.out.println("Gather upgrade information\n");

    //####################################
    //Find out what directory to upgrade to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperUpgradeTempDirectory();
    
    //see what we are upgrading: api, ui, ws, client
    this.appToUpgrade = grouperAppToUpgrade();

    for (int i=0;i<10;i++) {
      System.out.println("Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:");
      boolean runningProcesses = readFromStdInBoolean(true);
      if (runningProcesses) {
        break;
      }
      System.out.println("Please stop any processes using this installation...");
      //lets sleep for a bit to let it start
      GrouperInstallerUtils.sleep(2000);
    }
    
    
    //get the directory where the existing installation is
    this.upgradeExistingApplicationDirectoryString = upgradeExistingDirectory();

    this.version = GrouperInstallerUtils.propertiesValue("grouper.version", true);
    System.out.println("Upgrading to grouper " + this.appToUpgrade.name() + " version: " + this.version);


    System.out.println("\n##################################");
    System.out.println("Download and build grouper packages\n");

    //download new files
    this.appToUpgrade.downloadAndBuildGrouperProjects(this);

    System.out.println("End download and build grouper packages\n");
    System.out.println("\n##################################");

    this.grouperBaseBakDir = this.grouperTarballDirectoryString + "bak_" + this.appToUpgrade + "_" 
        + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date()) + File.separator; 

    GrouperInstallerUtils.tempFilePathForJars = this.grouperBaseBakDir 
        + "jarToDelete" + File.separator;

    this.appToUpgrade.upgradeApp(this);

    this.reportOnConflictingJars();
    
    System.out.println("\nGrouper is upgraded from " + (this.originalGrouperJarVersion == null ? null : this.originalGrouperJarVersion) 
        + " to " + GrouperInstallerUtils.propertiesValue("grouper.version", true) +  "\n");

    //this file keeps track of partial upgrades
    GrouperInstallerUtils.fileDelete(this.grouperUpgradeOriginalVersionFile);
    
  }

  /**
   * upgrade the client
   */
  private void upgradeClient() {

    System.out.println("\n##################################");
    System.out.println("Upgrading grouper client\n");

    this.compareAndReplaceJar(this.grouperClientJar, new File(this.untarredClientDir + File.separator + "grouperClient.jar"), true, null);

    this.compareUpgradePropertiesFile(this.grouperClientBasePropertiesFile, 
      new File(this.untarredClientDir + File.separator + "grouper.client.base.properties"),
      this.grouperClientPropertiesFile,
      this.grouperClientExamplePropertiesFile,
      GrouperInstallerUtils.toSet("grouperClient.webService.client.version")
    );
      
  }


  /**
   * upgrade the ui
   */
  private void upgradeUi() {

    this.upgradeApi();
    
    System.out.println("\n##################################");
    System.out.println("Upgrading UI\n");
    
    //copy the jars there
    System.out.println("\n##################################");
    System.out.println("Upgrading UI jars\n");

    this.upgradeJars(new File(this.untarredUiDir + File.separator + "dist" + File.separator + "grouper" + File.separator 
        + "WEB-INF" + File.separator + "lib" + File.separator));

    System.out.println("\n##################################");
    System.out.println("Upgrading UI files\n");

    //copy files there
    this.copyFiles(this.untarredUiDir + File.separator + "dist" + File.separator + "grouper" + File.separator,
        this.upgradeExistingApplicationDirectoryString,
        GrouperInstallerUtils.toSet("WEB-INF/lib", "WEB-INF/web.xml", "WEB-INF/classes",
            "WEB-INF/bin/gsh", "WEB-INF/bin/gsh.bat", "WEB-INF/bin/gsh.sh"));

    {
      boolean hadChange = false;
      for (String gshName : new String[]{"gsh", "gsh.bat", "gsh.sh"}) {
        File newGshFile = new File(this.untarredUiDir + File.separator + "dist" 
            + File.separator + "grouper" + File.separator + "WEB-INF" + File.separator + "bin" 
            + File.separator + gshName);

        File existingGshFile = new File(this.upgradeExistingApplicationDirectoryString 
            + File.separator + "WEB-INF" + File.separator + "bin" + File.separator + gshName);

        if (!GrouperInstallerUtils.contentEquals(newGshFile, existingGshFile)) {
          this.backupAndCopyFile(newGshFile, existingGshFile, true);
          if (!GrouperInstallerUtils.equals("gsh.bat", gshName)) {
            hadChange = true;
          }
        }
        
      }
      if (hadChange) {
        //set executable and dos2unix
        gshExcutableAndDos2Unix(this.untarredUiDir + File.separator + "dist" 
            + File.separator + "grouper" + File.separator + "WEB-INF" + File.separator + "bin" 
            + File.separator);
      }
    }
    
    upgradeWebXml(new File(this.untarredUiDir + File.separator + "dist" 
            + File.separator + "grouper" + File.separator + "WEB-INF" + File.separator + "web.xml"),
            new File(this.upgradeExistingApplicationDirectoryString 
                + File.separator + "WEB-INF" + File.separator + "web.xml"));
    
    System.out.println("\n##################################");
    System.out.println("Upgrading UI config files\n");

    this.changeConfig("WEB-INF/classes/resources/grouper/nav.properties", 
        "WEB-INF/classes/grouperText/grouper.text.en.us.base.properties",
        "WEB-INF/classes/grouperText/grouper.text.en.us.properties", null, GiGrouperVersion.valueOfIgnoreCase("2.2.0"), true);

    this.changeConfig("WEB-INF/classes/resources/grouper/media.properties", 
        "WEB-INF/classes/grouper-ui.base.properties",
        "WEB-INF/classes/grouper-ui.properties", null, GiGrouperVersion.valueOfIgnoreCase("2.2.0"), false);

    {
      File newBaseOwaspFile = new File(this.untarredUiDir + File.separator + "dist" 
          + File.separator + "grouper" + File.separator + "WEB-INF" + File.separator + "classes" 
          + File.separator + "Owasp.CsrfGuard.properties");

      File newOwaspFile = new File(this.untarredUiDir + File.separator + "dist" 
          + File.separator + "grouper" + File.separator + "WEB-INF" + File.separator + "classes" 
          + File.separator + "Owasp.CsrfGuard.overlay.properties");

      if (this.owaspCsrfGuardBaseFile == null) {
        this.owaspCsrfGuardBaseFile = new File(this.upgradeExistingClassesDirectoryString + newBaseOwaspFile.getName());
      }
      
      if (this.owaspCsrfGuardFile == null) {
        this.owaspCsrfGuardFile = new File(this.upgradeExistingClassesDirectoryString + newOwaspFile.getName());
      }
      
      this.backupAndCopyFile(newBaseOwaspFile, this.owaspCsrfGuardBaseFile, true);

      boolean editedOwaspOverlay = this.owaspCsrfGuardFile != null && this.owaspCsrfGuardFile.exists();

      File bakFile = this.backupAndCopyFile(newOwaspFile, this.owaspCsrfGuardFile, true);

      if (bakFile != null && editedOwaspOverlay) {
        if (!GrouperInstallerUtils.contentEquals(this.owaspCsrfGuardFile, newOwaspFile)) {
          System.out.println("If you have edited the Owasp.CsrfGuard.overlay.properties please merge the changes to the new file");
          System.out.println("Press <enter> when done");
          readFromStdIn();
        }
      }
    }    

    
    
  }

  /**
   * upgrade the psp
   */
  private void upgradePsp() {

    this.upgradeApi();
    
    System.out.println("\n##################################");
    System.out.println("Upgrading PSP\n");
    
    //copy the jars there
    System.out.println("\n##################################");
    System.out.println("Upgrading PSP jars\n");

    this.upgradeJars(new File(this.untarredPspDir + File.separator + "lib" + File.separator + "custom" + File.separator),
        new File(new File(this.upgradeExistingLibDirectoryString).getParentFile().getAbsolutePath() + File.separator + "custom"));

    System.out.println("\n##################################");
    System.out.println("Upgrading PSP files\n");

    //copy files there (this is the conf examples)
    this.copyFiles(this.untarredPspDir + File.separator + "conf" + File.separator,
        this.upgradeExistingApplicationDirectoryString + "conf" + File.separator, null);

  }

  
  /**
   * upgrade a web.xml file
   * @param newWebXml
   * @param existingWebXml
   */
  public void upgradeWebXml(File newWebXml, File existingWebXml) {
    
    File bakFile = backupAndCopyFile(newWebXml, existingWebXml, true);
    
    if (bakFile != null) {
      //it existed
      NodeList nodeList = GrouperInstallerUtils.xpathEvaluate(bakFile, "/web-app/security-constraint");
      boolean tookOutAuthn = false;
      if (nodeList == null || nodeList.getLength() == 0) {
        //take out authn from web.xml
        String webXmlContents = GrouperInstallerUtils.readFileIntoString(existingWebXml);
        int startAuthnIndex = webXmlContents.indexOf("<security-constraint>");
        int endAuthnIndex = webXmlContents.indexOf("</security-role>");
        if (startAuthnIndex != -1 && endAuthnIndex != -1 && endAuthnIndex > startAuthnIndex) {
          endAuthnIndex = endAuthnIndex + "</security-role>".length();
          //authn is there
          webXmlContents = webXmlContents.substring(0, startAuthnIndex) + webXmlContents.substring(endAuthnIndex, webXmlContents.length());
          GrouperInstallerUtils.saveStringIntoFile(existingWebXml, webXmlContents);
          tookOutAuthn = true;
          System.out.println("Taking out basic authentication from " + existingWebXml + " since it wasnt there before");
        }
      }
      System.out.println("If you customized the web.xml please merge your changes back in "
          + (tookOutAuthn ? "\n  Note: basic authentication was removed from the new web.xml to be consistent with the old web.xml" : "")
          + "\n  New file: " + existingWebXml.getAbsolutePath() + ", bak file:" + bakFile.getAbsolutePath() );
      System.out.println("Press the <enter> key to continue");
      readFromStdIn();
      
      if (tookOutAuthn) {
        GrouperInstallerUtils.xpathEvaluate(existingWebXml, "/web-app");        
      }
    }
  }

  
  /**
   * @param legacyPropertiesFileRelativePath legacy file we are converting from
   * @param propertiesFileRelativePath 
   * @param propertiesToIgnore
   * @param basePropertiesFileRelativePath
   * @param versionMigrationHappened
   * @param removeOldCopy
   */
  @SuppressWarnings("unchecked")
  private void changeConfig(String legacyPropertiesFileRelativePath,
      String basePropertiesFileRelativePath,
      String propertiesFileRelativePath,
      Set<String> propertiesToIgnore,
      GiGrouperVersion versionMigrationHappened, boolean removeOldCopy) {

    File legacyPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + legacyPropertiesFileRelativePath);
    File newBasePropertiesFile = new File(this.untarredUiDir + File.separator + "dist" 
        + File.separator + "grouper" + File.separator + basePropertiesFileRelativePath);
    File existingBasePropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + basePropertiesFileRelativePath);
    File existingPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + propertiesFileRelativePath);

    this.compareUpgradePropertiesFile(existingBasePropertiesFile, newBasePropertiesFile, 
        existingPropertiesFile, null, propertiesToIgnore);

    //look for existing properties in legacy file, and if there are properties in the base, then remove them
    if (legacyPropertiesFile.exists()) {
      
      Properties existingBaseProperties = GrouperInstallerUtils.propertiesFromFile(existingBasePropertiesFile);
      Properties existingProperties = GrouperInstallerUtils.propertiesFromFile(existingPropertiesFile);
      Properties legacyProperties = GrouperInstallerUtils.propertiesFromFile(legacyPropertiesFile);
      Set<String> propertyNamesToRemove = new LinkedHashSet<String>();
      Set<String> propertyNamesWrongValue = new LinkedHashSet<String>();

      for (String propertyName : (Set<String>)(Object)existingBaseProperties.keySet()) {
        if (legacyProperties.containsKey(propertyName)) {
          
          Object existingValue = existingProperties.containsKey(propertyName) ?
             existingProperties.get(propertyName) : existingBaseProperties.get(propertyName);
          
          //it might be in the override, what about other overrides?  who knows
          if (!GrouperInstallerUtils.equals(existingValue, 
              legacyProperties.get(propertyName))) {

            propertyNamesWrongValue.add(propertyName);
          }
          propertyNamesToRemove.add(propertyName);
        }
      }
      
      //if we found some, see if we can remove them
      if (propertyNamesToRemove.size() > 0) {
        
        if (propertyNamesWrongValue.size() > 0) {

          //these are properties that we different in the previous legacy file
          System.out.println(legacyPropertiesFileRelativePath + " has properties that have a different value than\n  the new place they are managed: "
              + basePropertiesFileRelativePath + ",\n  and the everride(s) which could be: " + propertiesFileRelativePath);
          System.out.println("Review these properties and merge the values, this could have happened due to changes in Grouper:");
          for (String propertyName: propertyNamesWrongValue) {
            System.out.println(" - " + propertyName);
          }
          System.out.println("When you are done merging press <enter>");
          readFromStdIn();

        }

        if (removeOldCopy) {
          
          System.out.println(legacyPropertiesFileRelativePath + " is not used anymore by grouper, can it be backed up and removed (t|f)? [t]: ");
          boolean removeLegacy = readFromStdInBoolean(true);
          if (removeLegacy) {
            File backupLegacy = bakFile(legacyPropertiesFile);
            GrouperInstallerUtils.copyFile(legacyPropertiesFile, backupLegacy);
            GrouperInstallerUtils.fileDelete(legacyPropertiesFile);
            System.out.println("File as removed.  Backup path: " + backupLegacy.getAbsolutePath());
          }
          
        } else {
          System.out.println(legacyPropertiesFileRelativePath + " has properties that can be removed since they are now managed in "
              + basePropertiesFileRelativePath);
          System.out.println("Would you like to have the properties automatically removed from " 
              + legacyPropertiesFile.getName() + " (t|f)? [t]: ");
          boolean removeRedundantProperties = readFromStdInBoolean(true);
          
          if (removeRedundantProperties) {
            removeRedundantProperties(legacyPropertiesFile, propertyNamesToRemove);
          }
        }
      }
    }
  }

  
  /**
   * copy files if they are different from one place to another, print out statuses
   * @param fromDirString where to copy files from
   * @param toDirString where to copy files to
   * @param relativePathsToIgnore
   */
  public void copyFiles(String fromDirString, String toDirString, 
      Set<String> relativePathsToIgnore) {
    
    fromDirString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(fromDirString);
    toDirString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(toDirString);

    {
      //lets massage all the paths so they dont start or end with slash and
      //so they have File.separator instead of the wrong slash
      Set<String> tempRelativePathsToIgnore = new HashSet<String>();
      for (String path : GrouperInstallerUtils.nonNull(relativePathsToIgnore)) {
        path = GrouperInstallerUtils.fileMassagePathsNoLeadingOrTrailing(path);
        tempRelativePathsToIgnore.add(path);
      }
      relativePathsToIgnore = tempRelativePathsToIgnore;
    }

    int insertCount = 0;
    int updateCount = 0;
    Map<String, Boolean> relativeFilePathsChangedAndIfInsert = new LinkedHashMap<String, Boolean>();

    List<File> allFiles = GrouperInstallerUtils.fileListRecursive(new File(fromDirString));
    for (File fileToCopyFrom : allFiles) {
      String relativePath = null;
      {
        //get the relative path with no leading or trailing slash
        String path = fileToCopyFrom.getAbsolutePath();
        if (!path.startsWith(fromDirString)) {
          throw new RuntimeException("Why does path not start with fromDirString: " + path + ", " + fromDirString);
        }
        relativePath = path.substring(fromDirString.length());
        relativePath = GrouperInstallerUtils.fileMassagePathsNoLeadingOrTrailing(relativePath);
      }
      boolean ignore = false;
      
      //ignore paths passed in
      for (String pathToIgnore : relativePathsToIgnore) {
        if (relativePath.startsWith(pathToIgnore)) {
          ignore = true;
          break;
        }
      }
      
      if (!ignore) {
        
        //File to copy to
        File fileToCopyTo = new File(toDirString + relativePath);
        if (fileToCopyTo.exists()) {
          //compare contents
          if (GrouperInstallerUtils.contentEquals(fileToCopyFrom, fileToCopyTo)) {
            continue;
          }
          //not equals, make backup
          updateCount++;

          relativeFilePathsChangedAndIfInsert.put(relativePath, false);

          this.backupAndCopyFile(fileToCopyFrom, fileToCopyTo, false);

          continue;
        }
        
        //insert
        insertCount++;
        relativeFilePathsChangedAndIfInsert.put(relativePath, true);
        GrouperInstallerUtils.copyFile(fileToCopyFrom, fileToCopyTo);
        
      }
    }

    System.out.println("Upgrading files from: " + fromDirString + "\n  to: " + toDirString 
        + (GrouperInstallerUtils.length(relativePathsToIgnore) == 0 ? "" : 
        ("\n  ignoring paths: " + GrouperInstallerUtils.join(relativePathsToIgnore.iterator(), ", "))));
    System.out.println("Compared " + allFiles.size() + " files and found " 
        + insertCount + " adds and " + updateCount + " updates");

    if (insertCount > 0 || updateCount > 0) {
      
      System.out.println((insertCount + updateCount) + " files were backed up to: " + this.grouperBaseBakDir);

      boolean listFiles = insertCount + updateCount <= 10;
      if (!listFiles) {
        System.out.println("Do you want to see the list of files changed (t|f)? [f]: ");
        listFiles = readFromStdInBoolean(false);
      }

      if (listFiles) {

        for (String relativeFilePathChanged : relativeFilePathsChangedAndIfInsert.keySet()) {
          boolean isInsert = relativeFilePathsChangedAndIfInsert.get(relativeFilePathChanged);
          System.out.println(relativeFilePathChanged + " was " + (isInsert ? "added" : "updated"));
        }
      }
    }
    
  }

  /**
   * upgrade the api
   */
  private void upgradeApi() {

    this.runChangeLogTempToChangeLog();

    this.upgradeClient();

    System.out.println("\n##################################");
    System.out.println("Upgrading API\n");

    //lets get the version of the existing jar
    this.originalGrouperJarVersion();

    this.compareAndReplaceJar(this.grouperJar, 
        new File(this.untarredApiDir + File.separator + "dist" + File.separator 
            + "lib" + File.separator + "grouper.jar"), true, null);

    System.out.println("\n##################################");
    System.out.println("Upgrading API config files\n");

    this.compareUpgradePropertiesFile(this.grouperBasePropertiesFile, 
      new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper.base.properties"),
      this.grouperPropertiesFile,
      this.grouperExamplePropertiesFile, null
    );
      
    this.compareUpgradePropertiesFile(this.grouperHibernateBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper.hibernate.base.properties"),
        this.grouperHibernatePropertiesFile,
        this.grouperHibernateExamplePropertiesFile, null
      );
        
    this.compareUpgradePropertiesFile(this.grouperLoaderBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper-loader.base.properties"),
        this.grouperLoaderPropertiesFile,
        this.grouperLoaderExamplePropertiesFile, null
      );

    this.compareUpgradePropertiesFile(this.subjectBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "subject.base.properties"),
        this.subjectPropertiesFile,
        null, null
      );

    this.upgradeEhcacheXml();

    System.out.println("\nYou should compare " + this.grouperPropertiesFile.getParentFile().getAbsolutePath() + File.separator + "sources.xml"
        + "\n  with " + this.untarredApiDir + File.separator + "conf" + File.separator + "sources.xml");
    System.out.println("Press <enter> to continue after you have merged the sources.xml");
    readFromStdIn();
    
    
    System.out.println("\n##################################");
    System.out.println("Upgrading API jars\n");

    this.upgradeJars(new File(this.untarredApiDir + File.separator + "lib" 
      + File.separator + "grouper" + File.separator));
    this.upgradeJars(new File(this.untarredApiDir + File.separator + "lib" 
      + File.separator + "jdbcSamples" + File.separator));

    System.out.println("\n##################################");
    System.out.println("Upgrading DB (registry)\n");

    this.apiUpgradeDbVersion(true);

    this.apiUpgradeAdditionalGshScripts();
    
  }

  /**
   * run additional GSH scripts based on what we are upgrading from...
   */
  private void apiUpgradeAdditionalGshScripts() {
    GiGrouperVersion giGrouperVersion = this.originalGrouperJarVersion();
    if (giGrouperVersion == null) {
      System.out.println("Grouper jar file: " + (this.grouperJar == null ? null : this.grouperJar.getAbsolutePath()));
      System.out.println("ERROR, cannot find grouper version in grouper jar file, do you want to continue? (t|f)? [f]: ");
      boolean continueScript = readFromStdInBoolean(false);
      if (!continueScript) {
        System.exit(1);
      }
    }

    boolean lessThan2_0 = this.originalGrouperJarVersion.lessThanArg(new GiGrouperVersion("2.0.0"));
    {
      if (lessThan2_0) {
        System.out.println("You are upgrading from pre API version 2.0.0, do you want to run Unresolvable Subject Deletion Utility (USDU) (recommended) (t|f)? [t]: ");
      } else {
        System.out.println("You are upgrading from after API version 2.0.0, so you dont have to do this,\n  "
            + "but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: ");
      }
      boolean runScript = readFromStdInBoolean(lessThan2_0);
      
      if (runScript) {
        
        //running with command on command line doenst work on linux since the args with whitespace translate to 
        //save the commands to a file, and runt he file
        StringBuilder gshCommands = new StringBuilder();
  
        //gsh 0% GrouperSession.startRootSession()
        //edu.internet2.middleware.grouper.GrouperSession: 6f94c99d5b0948a3be96f94f00ab4d87,'GrouperSystem','application'
        //gsh 1% // run USDU to resolve all the subjects with type=person
        //gsh 3% usdu()
  
        gshCommands.append("grouperSession = GrouperSession.startRootSession();\n");
        gshCommands.append("usdu();\n");
  
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshUsdu.gsh");
        GrouperInstallerUtils.saveStringIntoFile(gshFile, gshCommands.toString());
        
        List<String> commands = new ArrayList<String>();
  
        addGshCommands(commands);
        commands.add(gshFile.getAbsolutePath());
  
        System.out.println("\n##################################");
        System.out.println("Running USDU with command:\n  " + convertCommandsIntoCommand(commands) + "\n");
  
        GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
           new File(this.gshCommand()).getParentFile(), null, true);
  
      }
    }
    
    {

      if (lessThan2_0) {
        System.out.println("You are upgrading from pre API version 2.0.0, do you want to resolve all group subjects (recommended) (t|f)? [t]: ");
      } else {
        System.out.println("You are upgrading from after API version 2.0.0, so you dont have to do this,\n  "
            + "but do you want to resolve all group subjects (not recommended) (t|f)? [f]: ");
      }
      boolean runScript = readFromStdInBoolean(lessThan2_0);
      
      if (runScript) {
        
        //running with command on command line doenst work on linux since the args with whitespace translate to 
        //save the commands to a file, and runt he file
        StringBuilder gshCommands = new StringBuilder();
  
        //gsh 5% GrouperSession.startRootSession();
        //edu.internet2.middleware.grouper.GrouperSession: 4163fb08b3b24922b55a14010d48e121,'GrouperSystem','application'
        //gsh 6% for (String g : HibernateSession.byHqlStatic().createQuery("select uuid from Group").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, "g:gsa", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true); }
  
        gshCommands.append("grouperSession = GrouperSession.startRootSession();\n");
        gshCommands.append("for (String g : HibernateSession.byHqlStatic().createQuery(\"select uuid from Group\").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, \"g:gsa\", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true);\n");
  
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshUsdu.gsh");
        GrouperInstallerUtils.saveStringIntoFile(gshFile, gshCommands.toString());
        
        List<String> commands = new ArrayList<String>();
  
        addGshCommands(commands);
        commands.add(gshFile.getAbsolutePath());
  
        System.out.println("\n##################################");
        System.out.println("Resolving group subjects with command:\n  " + convertCommandsIntoCommand(commands) + "\n");

        GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
           new File(this.gshCommand()).getParentFile(), null, true);
      }
    }

    {
      boolean lessThan2_1 = giGrouperVersion.lessThanArg(new GiGrouperVersion("2.1.0"));
      if (lessThan2_1) {
        System.out.println("You are upgrading from pre API version 2.1.0, do you want to "
            + "see if you have rules with ruleCheckType: flattenedPermission* (recommended) (t|f)? [t]: ");
      } else {
        System.out.println("You are upgrading from after API version 2.1.0, so you dont have to do this,\n  "
            + "but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: ");
      }
      boolean runScript = readFromStdInBoolean(lessThan2_1);
      
      if (runScript) {
        
        //running with command on command line doenst work on linux since the args with whitespace translate to 
        //save the commands to a file, and runt he file
        StringBuilder gshCommands = new StringBuilder();
    
        gshCommands.append("\"Count: \" + HibernateSession.bySqlStatic().select(int.class, \"SELECT count(*) FROM grouper_rules_v WHERE rule_check_type LIKE 'flattenedPermission%'\");\n");
  
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshRuleFlattenedPermissionCount.gsh");
        GrouperInstallerUtils.saveStringIntoFile(gshFile, gshCommands.toString());
        
        List<String> commands = new ArrayList<String>();
  
        addGshCommands(commands);
        commands.add(gshFile.getAbsolutePath());
  
        System.out.println("\n##################################");
        System.out.println("Counting flattenedPermission rules with command:\n  " + convertCommandsIntoCommand(commands) + "\n");

        CommandResult commandResult = GrouperInstallerUtils.execCommand(
          GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
          new File(this.gshCommand()).getParentFile(), null);

        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }

        String result = commandResult.getOutputText().trim();
        String[] lines = GrouperInstallerUtils.splitLines(result);
        {
          Pattern pattern = Pattern.compile("^Count: ([0-9]+)$");
          int count = -1;
          for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
              count = GrouperInstallerUtils.intValue(matcher.group(1));
              break;
            }
          }
          if (count == -1) {
            System.out.println("Error getting count of rules, would you like to continue (t|f)? [t]:");
            if (!readFromStdInBoolean(true)) {
              System.exit(1);
            }
          } else {
            if (count > 0) {
              System.out.println("You have " + count + " flattenedPermission rules that need to be removed.  You need to look in the view grouper_rules_v and notify the owners and remove these rules.  Do you want to continue (t|f)? [t]: ");
              
              if (!readFromStdInBoolean(true)) {
                System.exit(1);
              }
            }
          }
        }
      }
    }

    {
      boolean lessThan2_2_0 = giGrouperVersion.lessThanArg(new GiGrouperVersion("2.2.0"));
      if (lessThan2_2_0) {
        System.out.println("You are upgrading from pre API version 2.2.0, "
            + "do you want to run the 2.2 upgrade GSH script (recommended) (t|f)? [t]: ");
      } else {
        System.out.println("You are upgrading from after API version 2.2.0, so you dont have to do this,\n  "
            + "but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: ");
      }
      boolean runScript = readFromStdInBoolean(lessThan2_2_0);
      
      if (runScript) {
        
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "misc" + File.separator + "postGrouper2_2Upgrade.gsh");
        
        List<String> commands = new ArrayList<String>();
  
        addGshCommands(commands);
        commands.add(gshFile.getAbsolutePath());
  
        System.out.println("\n##################################");
        System.out.println("Running 2.2 upgrade GSH with command:\n  " + convertCommandsIntoCommand(commands) + "\n");
  
        GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
           new File(this.gshCommand()).getParentFile(), null, true);
  
      }
      
    }

    {
      boolean lessThan2_2_1 = giGrouperVersion.lessThanArg(new GiGrouperVersion("2.2.1"));
      if (lessThan2_2_1) {
        System.out.println("You are upgrading from pre API version 2.2.1, do you want to "
            + "run the 2.2.1 upgrade GSH script (recommended) (t|f)? [t]: ");
      } else {
        System.out.println("You are upgrading from after API version 2.2.1, so you dont have to do this,\n  "
            + "but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: ");
      }
      boolean runScript = readFromStdInBoolean(lessThan2_2_1);
      
      if (runScript) {
        
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "misc" + File.separator + "postGrouper2_2_1Upgrade.gsh");
        
        List<String> commands = new ArrayList<String>();

        addGshCommands(commands);
        commands.add(gshFile.getAbsolutePath());

        System.out.println("\n##################################");
        System.out.println("Running 2.2.1 upgrade GSH with command:\n  " + convertCommandsIntoCommand(commands) + "\n");

        GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
           new File(this.gshCommand()).getParentFile(), null, true);

      }
    }

  }
  
  /**
   * grouper jar version e.g. 2.1.5
   */
  private GiGrouperVersion originalGrouperJarVersion = null;
  
  /**
   * keep trac if we have found it or not
   */
  private boolean originalGrouperJarVersionRetrieved = false;
  
  /**
   * get the version of the grouper jar
   * @return the version or null if couldnt be found
   */
  private GiGrouperVersion originalGrouperJarVersion() {

    if (!this.originalGrouperJarVersionRetrieved) {

      this.originalGrouperJarVersionRetrieved = true;
      
      //lets see if an upgrade went halfway through
      this.grouperUpgradeOriginalVersionFile = new File(this.upgradeExistingApplicationDirectoryString + "grouperUpgradeOriginalVersion.txt");

      if (this.grouperJar != null && this.grouperJar.exists()) {
        String grouperJarVersionString = GrouperInstallerUtils.jarVersion(this.grouperJar);
        
        if (!GrouperInstallerUtils.isBlank(grouperJarVersionString)) {
          this.originalGrouperJarVersion = new GiGrouperVersion(grouperJarVersionString);
        }
      }

      
      if (this.grouperUpgradeOriginalVersionFile.exists()) {
        String grouperJarVersionString = GrouperInstallerUtils.readFileIntoString(this.grouperUpgradeOriginalVersionFile);
        GiGrouperVersion fileGrouperJarVersion = new GiGrouperVersion(grouperJarVersionString);
        
        if (fileGrouperJarVersion != this.originalGrouperJarVersion) {
          
          System.out.println("It is detected that an upgrade did not complete from version " + fileGrouperJarVersion);
          this.originalGrouperJarVersion = fileGrouperJarVersion;
        }
      } else {
        GrouperInstallerUtils.writeStringToFile(this.grouperUpgradeOriginalVersionFile, this.originalGrouperJarVersion.toString());
      }
    }
    
    return this.originalGrouperJarVersion;
  }
  
  /**
   * file where version is kept for partial upgrades
   */
  private File grouperUpgradeOriginalVersionFile;
  
  /**
   * @param firstTime if first time
   */
  private void apiUpgradeDbVersion(boolean firstTime) {
    
    if (!GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.api.checkDdlVersion", true, false)) {
      System.out.println("Not checking DDL version since grouper.installer.properties: grouperInstaller.default.api.checkDdlVersion = false");
      return;
    }

    List<String> commands = new ArrayList<String>();

    addGshCommands(commands);
    commands.add("-registry");
    commands.add("-check");
    commands.add("-noprompt");

    System.out.println("\n##################################");
    System.out.println("Checking API database version with command: " + convertCommandsIntoCommand(commands) + "\n");

    CommandResult commandResult = GrouperInstallerUtils.execCommand(
        GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
        new File(this.gshCommand()).getParentFile(), null);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }

    String result = commandResult.getErrorText().trim();
    
    // Grouper ddl object type 'Grouper' has dbVersion: 27 and java version: 28
    // NOTE: Grouper database schema DDL may require updates, but the temp change log must 
    // be empty to perform an upgrade.  To process the temp change log, start up your current 
    // version of GSH and run: loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog")
    if (result != null && result.contains("CHANGE_LOG_changeLogTempToChangeLog")) {
      System.out.println("You must run the change log temp to change log before upgrading.  You can start the upgrader again and run it.");
      System.exit(1);
    }
    
    String[] lines = GrouperInstallerUtils.splitLines(result);
    {
      boolean okWithVersion = false;
      boolean notOkWithVersion = false;
      for (String line : lines) {
        line = line.toLowerCase();
        //expecting stderr: NOTE: database table/object structure (ddl) is up to date
        if (line.contains("ddl") && line.contains("up to date") && line.contains("note:")) {
          okWithVersion = true;
        }
        //cant have this line
        if (line.contains("requires updates")) {
          notOkWithVersion = true;
        }
      }
      if (okWithVersion && !notOkWithVersion) {
        return;
      }
    }

    if (!firstTime) {
      System.out.println("Error: we tried to upgrade the database but it didnt work, would you like to continue skipping DDL (t|f)? ");
      boolean continueOn = readFromStdInBoolean(null);
      if (continueOn) {
        return;
      }
    }
    
    //we need to upgrade the DDL
    //Grouper ddl object type 'Grouper' has dbVersion: 26 and java version: 28
    //Grouper database schema DDL requires updates
    //(should run script manually and carefully, in sections, verify data before drop statements, backup/export important data before starting, follow change log on confluence, dont run exact same script in multiple envs - generate a new one for each env),
    //script file is:
    //C:\app\grouper_2_2_0_installer\grouper.apiBinary-2.2.0\ddlScripts\grouperDdl_20141014_10_17_12_577.sql
    //Note: this script was not executed due to option passed in
    //To run script via gsh, carefully review it, then run this:
    //gsh -registry -runsqlfile C:\\app\\grouper_2_2_0_installer\\grouper.apiBinary-2.2.0\\ddlScripts\\grouperDdl_20141014_10_17_12_577.sql

    System.out.println("Review the script(s) above if there are any, do you want the upgrader to run it to upgrade the DDL for you (t|f)? [t]: ");
    boolean runIt = readFromStdInBoolean(true);
    
    if (runIt) {

      boolean foundScript = false;

      for (String line : lines) {
        if (line.contains("-registry -runsqlfile")) {
          
          String regexPattern = "^[^\\s]+\\s+-registry -runsqlfile (.*)$";
          Pattern pattern = Pattern.compile(regexPattern);
          
          Matcher matcher = pattern.matcher(line);
          
          if (!matcher.matches()) {
            throw new RuntimeException("Expected " + regexPattern + " but received: " + line);
          }

          String fileName = matcher.group(1);
          
          commands = new ArrayList<String>();
          
          addGshCommands(commands);
          commands.add("-registry");
          commands.add("-noprompt");
          commands.add("-runsqlfile");
          commands.add(fileName);
          
          foundScript = true;
          
          System.out.println("\n##################################");
          System.out.println("Upgrading database with command: " + convertCommandsIntoCommand(commands) + "\n");

          commandResult = GrouperInstallerUtils.execCommand(
              GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
             new File(this.gshCommand()).getParentFile(), null, true);

          //no out/err since printing as we go
          System.out.println("\nDone upgrading database");
          System.out.println("\n##################################\n");
        }
      }
      //cant find script, thats ok, just check and go
      if (!foundScript) {
        throw new RuntimeException("didnt find script to to run: " + result);
      }

      //check again to make sure ok
      apiUpgradeDbVersion(false);
    }
  }

  /**
   * upgrade jars from a directory
   * @param fromDir jars from this directory
   */
  private void upgradeJars(File fromDir) {
    this.upgradeJars(fromDir, new File(this.upgradeExistingLibDirectoryString));
  }
  
  /**
   * upgrade jars from a directory
   * @param fromDir jars from this directory
   * @param toDir is where jars go if not there
   */
  private void upgradeJars(File fromDir, File toDir) {

    //for each jar in the directory
    if (!fromDir.exists() || !fromDir.isDirectory()) {
      throw new RuntimeException("Why does jar directory not exist? " + fromDir);
    }
    
    File jarDir = null;
    
    int changes = 0;
    
    for (File jarFile : fromDir.listFiles()) {
      
      //only do jar files
      if (!jarFile.getName().endsWith(".jar")) {
        continue;
      }
      
      File existingJar = this.findLibraryFile(jarFile.getName(), false);

      List<File> relatedJars = null;
      
      for (int i=0;i<10;i++) {
        
        if (i==9) {
          throw new RuntimeException("Why didnt you clear out the jars??? " + GrouperInstallerUtils.toStringForLog(relatedJars));
        }

        relatedJars = GrouperInstallerUtils.jarFindJar(toDir, jarFile.getName());
        
        if (GrouperInstallerUtils.length(relatedJars) > 1) {
          
          System.out.println("There are multiple related jars for " + jarFile.getName() + ": " + GrouperInstallerUtils.toStringForLog(relatedJars));
          System.out.println("There should be only one, remove the others, if this is a mistake, then you need to\n  rename the prefix so they are different, and report to the Grouper team");
          System.out.println("Press <enter> to continue...");
          readFromStdIn();

        } else {
        
          break;

        }
          
      }
      
      
      if (existingJar == null) {
        //see if one exists by another version
        if (GrouperInstallerUtils.length(relatedJars) == 1) {
          existingJar = relatedJars.get(0);
        }
      }
      
      if (existingJar != null) {
        try {
          //case difference
          if (!GrouperInstallerUtils.equals(jarFile.getCanonicalFile().getName(), existingJar.getCanonicalFile().getName())) {
            File tmpFile = new File(existingJar.getParentFile().getAbsoluteFile() + File.separator + existingJar.getName()+ ".tmp");
            GrouperInstallerUtils.fileMove(existingJar, tmpFile);
            existingJar = new File(existingJar.getParentFile().getAbsoluteFile() + File.separator + jarFile.getName());
            GrouperInstallerUtils.fileMove(tmpFile, existingJar);
          }
        } catch (Exception e) {
          throw new RuntimeException("Problem with file: " + jarFile, e);
        }
      }
      changes += this.compareAndReplaceJar(existingJar, jarFile, false, toDir) ? 1 : 0;

      if (jarDir == null) {
        jarDir = existingJar.getParentFile();
      }
      
    }

    System.out.println("Upgraded " + changes + " jar files from: " + fromDir.getAbsolutePath()
        + "\n  to: " + jarDir.getAbsolutePath());
    
  }
  
  /**
   * 
   */
  private void upgradeEhcacheXml() {

    //ehcache, prompt to see if do it (if difference than example, and if old example different than new example?
    File newEhcacheExample = new File(this.untarredApiDir + File.separator + "conf" + File.separator + "ehcache.xml");
    
    //lets see if different
    String existingEhcacheContents = GrouperInstallerUtils.readFileIntoString(this.ehcacheFile);
    String existingExampleEhcacheContents = GrouperInstallerUtils.readFileIntoString(this.ehcacheExampleFile);
    String newEhcacheContents = GrouperInstallerUtils.readFileIntoString(newEhcacheExample);
    
    //if existing is the same as new...
    if (GrouperInstallerUtils.equals(existingEhcacheContents, newEhcacheContents)) {
      //make sure example is up to date
      if (this.ehcacheExampleFile != null && !GrouperInstallerUtils.equals(existingExampleEhcacheContents, newEhcacheContents)) {
        this.backupAndCopyFile(newEhcacheExample, this.ehcacheExampleFile, true);
      }

      //we are all good
      return;
    }

    //lets backup the example and regular file
    File ehcacheBakFile = bakFile(this.ehcacheFile);
    GrouperInstallerUtils.copyFile(this.ehcacheFile, ehcacheBakFile);

    boolean mergeFiles = true;
    
    if (this.ehcacheExampleFile != null) {
      File ehcacheExampleBakFile = bakFile(this.ehcacheExampleFile);
  
      GrouperInstallerUtils.copyFile(this.ehcacheExampleFile, ehcacheExampleBakFile);
    } else {
      GrouperInstallerUtils.copyFile(newEhcacheExample, this.ehcacheFile);
      mergeFiles = false;
    }

    if (mergeFiles) {
      //if the ehcache is the same as the example, lets just copy
      if (GrouperInstallerUtils.equals(existingEhcacheContents, existingExampleEhcacheContents)) {
        this.backupAndCopyFile(newEhcacheExample, this.ehcacheFile, false);
        if (this.ehcacheExampleFile != null) {
          this.backupAndCopyFile(newEhcacheExample, this.ehcacheExampleFile, false);
        }
        return;
      }

      //the ehcache file is different from the example and different from the new one, so merge it in
      mergeEhcacheXmlFiles(newEhcacheExample, this.ehcacheExampleFile, this.ehcacheFile);
    }

    System.out.println("Compare you old ehcache.xml with the new ehcache.xml file: " 
        + "\n  Old file: "
        + ehcacheBakFile.getAbsolutePath()
        + "\n  New file: " + this.ehcacheFile.getAbsolutePath()
        + "\n  Press <enter> when done");
    readFromStdIn();

  }

  /**
   * 
   * @param newFile
   * @param existingFile
   * @param printDetails
   * @return the bakFile
   */
  public File backupAndCopyFile(File newFile, File existingFile, boolean printDetails) {
    
    if (!GrouperInstallerUtils.contentEquals(newFile, existingFile)) {
      
      File bakFile = null;
          
      boolean fileExists = existingFile.exists();
      if (fileExists) {
        bakFile = bakFile(existingFile);
        GrouperInstallerUtils.copyFile(existingFile, bakFile);
        if (printDetails) {
          System.out.println("Backing up: " + existingFile.getAbsolutePath() + " to: " + bakFile.getAbsolutePath());
        }
      }
      if (printDetails) {
        System.out.println("Copying " + (fileExists ? "new file" : "upgraded file") + ": " + newFile.getAbsolutePath() + " to: " + existingFile.getAbsolutePath());
      }
      GrouperInstallerUtils.copyFile(newFile, existingFile);
      return bakFile;
      
    }

    if (printDetails) {
      System.out.println(existingFile.getAbsolutePath() + " has not been updated so it was not changed");
    }
    
    return null;
  }

  /**
   * 
   * @param existingFile
   * @return the bak file
   */
  public File bakFile(File existingFile) {
    String existingFilePath = existingFile.getAbsolutePath();
    if (!existingFilePath.startsWith(this.upgradeExistingApplicationDirectoryString)) {
      throw new RuntimeException("Why does existing path not start with upgrade path??? " 
          + existingFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
    }
    
    String bakString = this.grouperBaseBakDir 
        + existingFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());

    File bakFile = new File(bakString);
    return bakFile;
  }
  
  /**
   * @param existingBasePropertiesFile 
   * @param newBasePropertiesFile 
   * @param existingPropertiesFile 
   * @param existingExamplePropertiesFile 
   * @param propertiesToIgnore
   */
  private void compareUpgradePropertiesFile(File existingBasePropertiesFile, 
      File newBasePropertiesFile,
      File existingPropertiesFile,
      File existingExamplePropertiesFile,
      Set<String> propertiesToIgnore) {

    boolean hadChange = false;
    
    if (!newBasePropertiesFile.exists() || !newBasePropertiesFile.isFile()) {
      throw new RuntimeException("Why does this file not exist? " + newBasePropertiesFile.getAbsolutePath());
    }
    
    //if there is an existing base properties file, compare and replace and done
    if (existingBasePropertiesFile != null && existingBasePropertiesFile.exists() && existingBasePropertiesFile.isFile()) {
      
      String existingBaseContents = GrouperInstallerUtils.readFileIntoString(existingBasePropertiesFile);
      String newBaseContents = GrouperInstallerUtils.readFileIntoString(newBasePropertiesFile);
      
      if (!GrouperInstallerUtils.equals(existingBaseContents, newBaseContents)) {
        
        String existingBasePropertiesFilePath = existingBasePropertiesFile.getAbsolutePath();
        if (!existingBasePropertiesFilePath.startsWith(this.upgradeExistingApplicationDirectoryString)) {
          throw new RuntimeException("Why does existing path not start with upgrade path??? " 
              + existingBasePropertiesFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
        }
        
        String bakBasePropertiesString = this.grouperBaseBakDir + existingBasePropertiesFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());

        File bakBasePropertiesFile = new File(bakBasePropertiesString);
        
        //make sure parents exist
        GrouperInstallerUtils.createParentDirectories(bakBasePropertiesFile);

        System.out.println(existingBasePropertiesFile.getName() + " has changes and was upgraded.\n  It is backed up to " 
            + bakBasePropertiesFile.getAbsolutePath());
        
        hadChange = true;
        
        GrouperInstallerUtils.fileMove(existingBasePropertiesFile, bakBasePropertiesFile);
        
        GrouperInstallerUtils.copyFile(newBasePropertiesFile, existingBasePropertiesFile);

      }
      
    } else {
      
      hadChange = true;
      
      System.out.println(newBasePropertiesFile.getName() + " didn't exist and was installed.");
      
      //its null, but we dont have the path...
      if (existingBasePropertiesFile == null) {
        existingBasePropertiesFile = new File(this.upgradeExistingClassesDirectoryString + newBasePropertiesFile.getName());
      }
      GrouperInstallerUtils.copyFile(newBasePropertiesFile, existingBasePropertiesFile);
    }
    
    // if there is an example there, it can be removed
    if (existingExamplePropertiesFile != null && existingExamplePropertiesFile.exists() && existingExamplePropertiesFile.isFile()) {

      String existingExamplePropertiesFilePath = existingExamplePropertiesFile.getAbsolutePath();
      if (!existingExamplePropertiesFilePath.startsWith(this.upgradeExistingApplicationDirectoryString)) {
        throw new RuntimeException("Why does existing path not start with upgrade path??? " 
            + existingExamplePropertiesFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
      }
      
      String bakExamplePropertiesString = this.grouperBaseBakDir 
          + existingExamplePropertiesFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());

      File bakExamplePropertiesFile = new File(bakExamplePropertiesString);
      
      //make sure parents exist
      GrouperInstallerUtils.createParentDirectories(bakExamplePropertiesFile);

      System.out.println(existingExamplePropertiesFile.getName() + " is not needed and was deleted.\n  It is backed up to " 
          + bakExamplePropertiesFile.getAbsolutePath());

      GrouperInstallerUtils.fileMove(existingExamplePropertiesFile, bakExamplePropertiesFile);
    
    }

    if (existingPropertiesFile != null && existingPropertiesFile.exists() && existingPropertiesFile.isFile()) {
      
      // now then, if there is a properties file, we can look for duplicate configs, and remove them...
      Set<String> duplicateConfigPropertyNames = configPropertyDuplicates(newBasePropertiesFile, existingPropertiesFile);

      if (GrouperInstallerUtils.length(propertiesToIgnore) > 0 && GrouperInstallerUtils.length(duplicateConfigPropertyNames) > 0) {
        duplicateConfigPropertyNames.addAll(propertiesToIgnore);
      }
      
      if (GrouperInstallerUtils.length(duplicateConfigPropertyNames) > 0) {

        hadChange = true;
        
        System.out.println(existingPropertiesFile.getName() + " has " + duplicateConfigPropertyNames.size() 
            + " properties that can be removed since the values are the same in "
            + newBasePropertiesFile.getName());

        System.out.println("Would you like to have the " + duplicateConfigPropertyNames.size() 
            + " redundant properties automatically removed from " 
            + existingPropertiesFile.getName() + " (t|f)? [t]: ");
        boolean removeRedundantProperties = readFromStdInBoolean(true);
        
        if (removeRedundantProperties) {

          String existingPropertiesFilePath = existingPropertiesFile.getAbsolutePath();
          if (!existingPropertiesFilePath.startsWith(this.upgradeExistingApplicationDirectoryString)) {
            throw new RuntimeException("Why does existing path not start with upgrade path??? " 
                + existingPropertiesFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
          }
          
          String bakPropertiesString = this.grouperBaseBakDir 
              + existingPropertiesFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());

          File bakPropertiesFile = new File(bakPropertiesString);
          
          //make sure parents exist
          GrouperInstallerUtils.createParentDirectories(bakPropertiesFile);

          System.out.println(existingPropertiesFile.getName() + " had redundant properties removed after being backed up to " 
              + bakPropertiesFile.getAbsolutePath());

          GrouperInstallerUtils.copyFile(existingPropertiesFile, bakPropertiesFile);
          
          removeRedundantProperties(existingPropertiesFile, duplicateConfigPropertyNames);
          
        }
      }
    } else {
      
      hadChange = true;
      
      //if we didnt have a properties file, create one
      //file is null...
      String contents = "\n# The " + newBasePropertiesFile.getName().replace(".base", "") 
          + " file uses Grouper Configuration Overlays (documented on wiki)\n"
          + "# By default the configuration is read from " + newBasePropertiesFile.getName() + "\n"
          + "# (which should not be edited), and the " +newBasePropertiesFile.getName().replace(".base", "") + " overlays\n"
          + "# the base settings.  See the " + newBasePropertiesFile.getName() + " for the possible\n"
          + "# settings that can be applied to the " + newBasePropertiesFile.getName().replace(".base", "") + "\n\n";

      File file = null;
      
      if (existingPropertiesFile != null) {
        file = existingPropertiesFile;
      } else if (existingBasePropertiesFile != null) {
        file = new File(existingBasePropertiesFile.getAbsolutePath().replace(".base", ""));
      } else {
        String fileName =  existingPropertiesFile != null ? existingPropertiesFile.getAbsolutePath() : this.upgradeExistingClassesDirectoryString + newBasePropertiesFile.getName().replace(".base", "");
        file = new File(fileName);
      }
      
      System.out.println("Created overlay config file: " + file.getAbsolutePath());
      
      GrouperInstallerUtils.saveStringIntoFile(file, contents);
    }
    
    if (!hadChange) {
      System.out.println("Found no changes in " + existingBasePropertiesFile.getAbsolutePath());
    }
    
  }

  /**
   * remove duplicate properties
   * @param propertiesFile
   * @param duplicatePropertyNames
   */
  private static void removeRedundantProperties(File propertiesFile, Set<String> duplicatePropertyNames) {
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(propertiesFile);
    
    String newline = GrouperInstallerUtils.newlineFromFile(fileContents);

    StringBuilder newContents = new StringBuilder();

    String[] lines = GrouperInstallerUtils.splitLines(fileContents);
    
    boolean inStartComments = true;
    boolean inHeaderComments = false;

    StringBuilder captureHeader = new StringBuilder();
    StringBuilder propertyAndComments = new StringBuilder();
    
    for (String line: lines) {
      
      line = line.trim();
      
      boolean isBlank = GrouperInstallerUtils.isBlank(line);
      boolean isComment = line.startsWith("#");
      boolean isSingleComment = line.startsWith("#") && !line.startsWith("##");
      boolean isHeaderComment = line.contains("#####");
      boolean isProperty = !isBlank && !isComment;
      
      //if in header then we are done with the start comments
      if (isHeaderComment) {
        inStartComments = false;
      }

      //we want to keep the start comments
      if (inStartComments) {
        
        if (isBlank || isComment) {
          newContents.append(line).append(newline);
          continue;
        }
        inStartComments = false;
      }

      //we are done with headers
      if (isProperty || isBlank || isSingleComment) {
        inHeaderComments = false;
      }

      if (isHeaderComment) {
        //if header and in headers, then we arent in headers
        if (inHeaderComments) {
          inHeaderComments = false;
        } else {
          //if this is a header, and we arent in headers, then we are in headers
          inHeaderComments = true;          
          captureHeader.setLength(0);
        }
      }

      if (isHeaderComment || inHeaderComments) {
        propertyAndComments.setLength(0);
        captureHeader.append(line).append(newline);
        continue;
      }
      
      if (isProperty) {
        
        //get the property
        int equalsIndex = line.indexOf('=');
        if (equalsIndex == -1) {
          //uh... ignore this... 
          System.out.println("Invalid line removed from properties file: " + propertiesFile.getAbsolutePath() + ":\n  " + line);
          continue;
        }
        
        String propertyName = line.substring(0, equalsIndex).trim();
        //unescape colons...
        if (duplicatePropertyNames.contains(propertyName) || duplicatePropertyNames.contains(propertyName.replace("\\:", ":"))) {
          propertyAndComments.setLength(0);
          //remove it!
          continue;
        }

        //keep it
        propertyAndComments.append(line).append(newline);

        //we need a header if there is one
        if (captureHeader.length() > 0) {
          newContents.append(newline);
          newContents.append(captureHeader);
          captureHeader.setLength(0);
        }

        //append the property and contents
        newContents.append(propertyAndComments);

        propertyAndComments.setLength(0);
        continue;
      }
      
      //must be whitespace or comment...
      propertyAndComments.append(line).append(newline);
    }
    
    GrouperInstallerUtils.saveStringIntoFile(propertiesFile, newContents.toString());
    
  }
  
  /**
   * 
   * @param file1
   * @param file2
   * @return the property names which are the same
   */
  @SuppressWarnings("unchecked")
  public static Set<String> configPropertyDuplicates(File file1, File file2) {
    Properties file1properties = GrouperInstallerUtils.propertiesFromFile(file1);
    Properties file2properties = GrouperInstallerUtils.propertiesFromFile(file2);
    
    Set<String> duplicatePropertyNames = new LinkedHashSet<String>();
    
    for (String propertyName : (Set<String>)(Object)file2properties.keySet()) {
      
      String file1Value = GrouperInstallerUtils.trimToEmpty(file1properties.getProperty(propertyName));
      String file2Value = GrouperInstallerUtils.trimToEmpty(file2properties.getProperty(propertyName));
      
      if (GrouperInstallerUtils.equals(file1Value, file2Value)) {
        duplicatePropertyNames.add(propertyName);
      }
      
    }
    return duplicatePropertyNames;
  }
  
  
  /**
   * the location of the existing installation, must end in file separator
   */
  private String upgradeExistingApplicationDirectoryString;
  
  /**
   * 
   */
  private static enum AppToUpgrade {
    
    /**
     * upgrading the UI
     */
    UI {

      @Override
      public boolean validateExistingDirectory(GrouperInstaller grouperInstaller) {
        //API and client are in the UI
        if (!API.validateExistingDirectory(grouperInstaller)) {
          return false;
        }
        
        //no need to check if it exists... its new in 2.2

        //grouperInstaller.mediaPropertiesFile = grouperInstaller.findClasspathFile("media.properties", false);

        //media should be there, but not forever
        
        return true;
      }

      @Override
      public void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller) {
        API.downloadAndBuildGrouperProjects(grouperInstaller);
        
        //####################################
        //download and configure ui
        grouperInstaller.downloadAndConfigureUi();

        //####################################
        //get ant
        grouperInstaller.downloadAndUnzipAnt();

        //####################################
        //build UI
        grouperInstaller.buildUi(false);

        File serverXml = null;
        for (int i=0;i<10;i++) {
          String defaultServerXml = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.ui.server.xml", false);
          System.out.println("What is the location of your tomcat server.xml for the UI?  "
              + "Note, if you dont use tomcat just leave it blank or type 'blank': " 
              + (GrouperInstallerUtils.isBlank(defaultServerXml) ? "" : ("[" + defaultServerXml + "]: ")));
          String serverXmlLocation = readFromStdIn();
          
          if (GrouperInstallerUtils.equals(defaultServerXml, "blank")) {
            defaultServerXml = null;
            break;
          }
          
          if (GrouperInstallerUtils.isBlank(serverXmlLocation)) {
            if (GrouperInstallerUtils.isNotBlank(defaultServerXml)) {
              serverXmlLocation = defaultServerXml;
            } else {
              break;
            }
          }
          serverXml = new File(serverXmlLocation);
          if (serverXml.exists() && serverXml.isFile()) {
            break;
          }
          if (i != 9) {
            System.out.println("Error: server.xml cant be found, try again.");
          }
        }
        if (serverXml != null && serverXml.exists() && serverXml.isFile()) {
          grouperInstaller.configureTomcatUriEncoding(serverXml);
        }
                
      }

      @Override
      public void upgradeApp(GrouperInstaller grouperInstaller) {
        grouperInstaller.upgradeUi();
      }
    },
    
    /**
     * upgrading the API
     */
    API {

      @Override
      public boolean validateExistingDirectory(GrouperInstaller grouperInstaller) {

        //client is in the API
        if (!CLIENT.validateExistingDirectory(grouperInstaller)) {
          return false;
        }
        
        grouperInstaller.subjectPropertiesFile = grouperInstaller.findClasspathFile("subject.properties", false);
        grouperInstaller.subjectBasePropertiesFile = grouperInstaller.findClasspathFile("subject.base.properties", false);
        
        //no need to check if it exists... its new in 2.2
        
        grouperInstaller.grouperPropertiesFile = grouperInstaller.findClasspathFile("grouper.properties", false);
        grouperInstaller.grouperBasePropertiesFile = grouperInstaller.findClasspathFile("grouper.base.properties", false);
        grouperInstaller.grouperExamplePropertiesFile = grouperInstaller.findClasspathFile("grouper.example.properties", false);

        if (grouperInstaller.grouperBasePropertiesFile == null 
            && grouperInstaller.grouperPropertiesFile == null 
            && grouperInstaller.grouperExamplePropertiesFile == null) {
          return false;
        }
        
        grouperInstaller.grouperHibernatePropertiesFile = grouperInstaller.findClasspathFile("grouper.hibernate.properties", false);
        grouperInstaller.grouperHibernateBasePropertiesFile = grouperInstaller.findClasspathFile("grouper.hibernate.base.properties", false);
        grouperInstaller.grouperHibernateExamplePropertiesFile = grouperInstaller.findClasspathFile("grouper.hibernate.example.properties", false);

        if (grouperInstaller.grouperHibernateBasePropertiesFile == null 
            && grouperInstaller.grouperHibernatePropertiesFile == null 
            && grouperInstaller.grouperHibernateExamplePropertiesFile == null) {
          return false;
        }
        
        grouperInstaller.grouperLoaderPropertiesFile = grouperInstaller.findClasspathFile("grouper-loader.properties", false);
        grouperInstaller.grouperLoaderBasePropertiesFile = grouperInstaller.findClasspathFile("grouper-loader.base.properties", false);
        grouperInstaller.grouperLoaderExamplePropertiesFile = grouperInstaller.findClasspathFile("grouper-loader.example.properties", false);

        if (grouperInstaller.grouperLoaderBasePropertiesFile == null 
            && grouperInstaller.grouperLoaderPropertiesFile == null 
            && grouperInstaller.grouperLoaderExamplePropertiesFile == null) {
          return false;
        }
        
        //this must exist
        grouperInstaller.grouperJar = grouperInstaller.findLibraryFile("grouper.jar", false);
        if (grouperInstaller.grouperJar == null) {
          return false;
        }

        grouperInstaller.ehcacheFile = grouperInstaller.findClasspathFile("ehcache.xml", false);
        grouperInstaller.ehcacheExampleFile = grouperInstaller.findClasspathFile("ehcache.example.xml", false);        
        
        //all good
        return true;
      }

      @Override
      public void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller) {
        CLIENT.downloadAndBuildGrouperProjects(grouperInstaller);
        
        //download api and set executable and dos2unix etc
        grouperInstaller.downloadAndConfigureApi();

      }

      @Override
      public void upgradeApp(GrouperInstaller grouperInstaller) {
        grouperInstaller.upgradeApi();
      }
    },
    
    /**
     * upgrading the client
     */
    CLIENT {

      @Override
      public boolean validateExistingDirectory(GrouperInstaller grouperInstaller) {
        
        grouperInstaller.grouperClientPropertiesFile = grouperInstaller.findClasspathFile("grouper.client.properties", false);
        grouperInstaller.grouperClientBasePropertiesFile = grouperInstaller.findClasspathFile("grouper.client.base.properties", false);
        grouperInstaller.grouperClientExamplePropertiesFile = grouperInstaller.findClasspathFile("grouper.client.example.properties", false);

        if (grouperInstaller.grouperClientBasePropertiesFile == null 
            && grouperInstaller.grouperClientPropertiesFile == null 
            && grouperInstaller.grouperClientExamplePropertiesFile == null) {
          if (grouperInstaller.appToUpgrade == CLIENT) {
            return false;
          }
        }
        
        //this must exist
        grouperInstaller.grouperClientJar = grouperInstaller.findLibraryFile("grouperClient.jar", false);
        if (grouperInstaller.grouperClientJar == null) {
          if (grouperInstaller.appToUpgrade == CLIENT) {
            return false;
          }
        }
        
        //all good
        return true;
      }

      @Override
      public void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller) {
        grouperInstaller.downloadAndBuildClient();
      }

      @Override
      public void upgradeApp(GrouperInstaller grouperInstaller) {
        grouperInstaller.upgradeClient();
      }
    },

    /**
     * upgrading the WS
     */
    WS {

      @Override
      public boolean validateExistingDirectory(GrouperInstaller grouperInstaller) {
        //API and client are in the UI
        if (!API.validateExistingDirectory(grouperInstaller)) {
          return false;
        }
        
        grouperInstaller.grouperWsPropertiesFile = grouperInstaller.findClasspathFile("grouper-ws.properties", false);
        grouperInstaller.grouperWsBasePropertiesFile = grouperInstaller.findClasspathFile("grouper-ws.base.properties", false);
        grouperInstaller.grouperWsExamplePropertiesFile = grouperInstaller.findClasspathFile("grouper-ws.example.properties", false);

        if (grouperInstaller.grouperWsBasePropertiesFile == null 
            && grouperInstaller.grouperWsPropertiesFile == null 
            && grouperInstaller.grouperWsExamplePropertiesFile == null) {
          return false;
        }

        return true;
      }

      @Override
      public void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller) {
        API.downloadAndBuildGrouperProjects(grouperInstaller);
        
        //####################################
        //download and configure ws
        grouperInstaller.downloadAndConfigureWs();

        //####################################
        //get ant
        grouperInstaller.downloadAndUnzipAnt();

        //####################################
        //build Ws
        grouperInstaller.buildWs(false);

      }

      @Override
      public void upgradeApp(GrouperInstaller grouperInstaller) {
        grouperInstaller.upgradeWs();
      }
    }, 
    
    /**
     * upgrading the UI
     */
    PSP {
    
      @Override
      public boolean validateExistingDirectory(GrouperInstaller grouperInstaller) {
        //API and client are in the UI
        if (!API.validateExistingDirectory(grouperInstaller)) {
          return false;
        }
        
        File customLibDir = new File(grouperInstaller.upgradeExistingApplicationDirectoryString + "lib" + File.separator + "custom");
        if (!customLibDir.exists()) {
          return false;
        }

        //see if psp jar is there
        List<File> files = GrouperInstallerUtils.jarFindJar(customLibDir, "psp.jar");
                
        if (GrouperInstallerUtils.length(files) == 0) {
          return false;
        }

        return true;
      }
    
      @Override
      public void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller) {
        API.downloadAndBuildGrouperProjects(grouperInstaller);
        
        //####################################
        //download and configure psp
        grouperInstaller.downloadAndBuildPsp();
    
      }
    
      @Override
      public void upgradeApp(GrouperInstaller grouperInstaller) {
        grouperInstaller.upgradePsp();
      }
    };

    /**
     * validate that the existing directory is valid, and find all the file paths
     * @param grouperInstaller 
     * @return true if valid, false if not
     */
    public abstract boolean validateExistingDirectory(GrouperInstaller grouperInstaller);
    
    /**
     * based on what is being upgraded, download and build the grouper projects
     * @param grouperInstaller
     */
    public abstract void downloadAndBuildGrouperProjects(GrouperInstaller grouperInstaller);
    
    /**
     * upgrade this app
     * @param grouperInstaller
     */
    public abstract void upgradeApp(GrouperInstaller grouperInstaller);
    
    /**
     * 
     * @param string
     * @param exceptionOnNotFound
     * @return the enum of what to upgrade
     */
    public static AppToUpgrade valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
      return GrouperInstallerUtils.enumValueOfIgnoreCase(AppToUpgrade.class, string, exceptionOnNotFound);
    }
    
  }


  /**
   * owasp csrf guard file
   */
  private File owaspCsrfGuardFile;
  
  /**
   * owasp csrf guard base file
   */
  private File owaspCsrfGuardBaseFile;
  
  /**
   * on an upgrade, compare a new jar and an existing jar and see if needs to be updated, and if so, update it
   * @param existingJarFile
   * @param newJarFile
   * @param printResultIfNotUpgrade
   * @param toDir if file not there, copy here.  If null, then go to upgrade existing lib directory string
   * @return true if upgraded, false if not
   */
  private boolean compareAndReplaceJar(File existingJarFile, File newJarFile, boolean printResultIfNotUpgrade, File toDir) {
    
    if (toDir == null) {
      toDir = new File(this.upgradeExistingLibDirectoryString);
    }
    
    if (existingJarFile == null || !existingJarFile.exists()) {
      System.out.println(newJarFile.getName() + " is a new file and is being copied to the application lib dir");
      existingJarFile = new File(toDir.getAbsoluteFile() + File.separator + newJarFile.getName());
      GrouperInstallerUtils.copyFile(newJarFile, existingJarFile);
      return true;
    }

    String existingJarFilePath = existingJarFile.getAbsolutePath();
    if (!existingJarFilePath.startsWith(this.upgradeExistingApplicationDirectoryString)) {
      throw new RuntimeException("Why does existing path not start with upgrade path??? " + existingJarFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
    }
    
    String bakJarFileString = this.grouperBaseBakDir + existingJarFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());
    File bakJarFile = new File(bakJarFileString);
    
    String existingVersion = GrouperInstallerUtils.jarVersion(existingJarFile);
    String newVersion = GrouperInstallerUtils.jarVersion(newJarFile);
    
    long existingSize = existingJarFile.length();
    long newSize = newJarFile.length();
    
    if (!GrouperInstallerUtils.equals(existingVersion, newVersion) || existingSize != newSize) {

      //make sure parents exist
      GrouperInstallerUtils.createParentDirectories(bakJarFile);
      
      System.out.println(existingJarFile.getName() + " had version " + existingVersion + " and size " + existingSize + " bytes and is being upgraded to version "
          + newVersion + " and size " + newSize + " bytes.\n  It is backed up to " + bakJarFile);

      GrouperInstallerUtils.fileMove(existingJarFile, bakJarFile);
      
      GrouperInstallerUtils.copyFile(newJarFile, existingJarFile);
      
      return true;
    }
    
    if (printResultIfNotUpgrade) {
      System.out.println(existingJarFile.getName() + " is up to date");
    }
    return false;
  }

  /**
   * grouper.client.properties
   */
  private File grouperClientPropertiesFile;
  
  /**
   * grouper.client.base.properties
   */
  private File grouperClientBasePropertiesFile;
  
  /**
   * grouper.client.example.properties
   */
  private File grouperClientExamplePropertiesFile;

  /**
   * grouperClient.jar
   */
  private File grouperClientJar;

  /**
   * grouper.properties
   */
  private File grouperPropertiesFile;
  
  /**
   * grouper.base.properties
   */
  private File grouperBasePropertiesFile;
  
  /**
   * subject.properties
   */
  private File subjectPropertiesFile;
  
  /**
   * subject.base.properties
   */
  private File subjectBasePropertiesFile;
  
  /**
   * grouper.example.properties
   */
  private File grouperExamplePropertiesFile;

  /**
   * grouper.hibernate.properties
   */
  private File grouperHibernatePropertiesFile;
  
  /**
   * grouper.hibernate.base.properties
   */
  private File grouperHibernateBasePropertiesFile;
  
  /**
   * grouper.hibernate.example.properties
   */
  private File grouperHibernateExamplePropertiesFile;

  /**
   * grouper-ws.properties
   */
  private File grouperWsPropertiesFile;
  
  /**
   * grouper-ws.example.properties
   */
  private File grouperWsBasePropertiesFile;

  /**
   * grouper-ws.base.properties
   */
  private File grouperWsExamplePropertiesFile;
  
  /**
   * ehcache.xml
   */
  private File ehcacheFile;

  /**
   * ehcache.example.xml
   */
  private File ehcacheExampleFile;
  
  /**
   * grouper-loader.properties
   */
  private File grouperLoaderPropertiesFile;
  
  /**
   * grouper-loader.base.properties
   */
  private File grouperLoaderBasePropertiesFile;
  
  /**
   * grouper-loader.example.properties
   */
  private File grouperLoaderExamplePropertiesFile;

  /**
   * grouper.jar
   */
  private File grouperJar;

  
  /**
   * find a classpath file on classpath by resourceName
   * @param resourceName resource name of file
   * @param exceptionIfNotFound 
   * @return the file or null if not exception if not found
   */
  private File findClasspathFile(String resourceName, boolean exceptionIfNotFound) {
    
    Set<String> fileNamesTried = new LinkedHashSet<String>();
    
    File file = new File(this.upgradeExistingApplicationDirectoryString + "classes" + File.separator + resourceName);
    if (file.exists()) {
      return file;
    }
    
    fileNamesTried.add(file.getAbsolutePath());
    
    file = new File(this.upgradeExistingApplicationDirectoryString + "conf" + File.separator + resourceName);
    if (file.exists()) {
      return file;
    }

    fileNamesTried.add(file.getAbsolutePath());

    //these could be in this location
    if (GrouperInstallerUtils.equals("nav.properties", resourceName) 
        || GrouperInstallerUtils.equals("media.properties", resourceName)) {
      file = new File(this.upgradeExistingApplicationDirectoryString + "WEB-INF" + File.separator 
          + "classes" + File.separator + "resources" + File.separator + "grouper" + File.separator + resourceName);
      if (file.exists()) {
        return file;
      }
      
      fileNamesTried.add(file.getAbsolutePath());
    }
    
    file = new File(this.upgradeExistingApplicationDirectoryString + "WEB-INF" + File.separator + "classes" 
        + File.separator + resourceName);
    if (file.exists()) {
      return file;
    }

    fileNamesTried.add(file.getAbsolutePath());
    
    file = new File(this.upgradeExistingApplicationDirectoryString + resourceName);
    if (file.exists()) {
      return file;
    }

    fileNamesTried.add(file.getAbsolutePath());
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find file, looked in: " + GrouperInstallerUtils.join(fileNamesTried.iterator(), ", "));
    }
    
    return null;
  }

  /**
   * lib dirs where libs might be in this.upgradeExistingApplicationDirectoryString
   */
  private static List<String> libDirs = GrouperInstallerUtils.toList(
      "lib" + File.separator, 
      "WEB-INF" + File.separator + "lib" + File.separator,
      "lib" + File.separator + "grouper" + File.separator,
      "lib" + File.separator + "custom" + File.separator,
      "lib" + File.separator + "jdbcSamples" + File.separator,
      "dist" + File.separator + "lib" + File.separator,
      "");

  /**
   * get all library files
   * @return the list of files
   */
  private List<File> findAllLibraryFiles() {
    List<File> result = new ArrayList<File>();
    for (String libDir : libDirs) {

      File dir = new File(this.upgradeExistingApplicationDirectoryString + libDir);
      if (dir.exists() && dir.isDirectory()) {
        for (File file : dir.listFiles()) {
          if (file.getName().endsWith(".jar")) {
            result.add(file);
          }
        }
      }
      
    }
    return result;
  }
  
  /**
   * find a library file on lib dir by libName
   * @param libName lib name of file
   * @param exceptionIfNotFound 
   * @return the file or null if not exception if not found
   */
  private File findLibraryFile(String libName, boolean exceptionIfNotFound) {
    
    Set<String> fileNamesTried = new LinkedHashSet<String>();

    for (String libDir : libDirs) {

      File file = new File(this.upgradeExistingApplicationDirectoryString + libDir + libName);
      if (file.exists()) {
        return file;
      }
      
      fileNamesTried.add(file.getAbsolutePath());
      
    }
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find file, looked in: " + GrouperInstallerUtils.join(fileNamesTried.iterator(), ", "));
    }
    
    return null;
  }
  
  /**
   * 
   * @param args
   */
  private void mainInstallLogic(String[] args) {
    
    //####################################
    //Find out what directory to install to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperInstallDirectory();

    //####################################
    //get default ip address
    System.out.print("Enter the default IP address for checking ports (just hit enter to accept the default unless on a machine with no network, might want to change to 127.0.0.1): [0.0.0.0]: ");
    this.defaultIpAddress = readFromStdIn();
    
    if (GrouperInstallerUtils.isBlank(this.defaultIpAddress)) {
      this.defaultIpAddress = "0.0.0.0";
    }

    if (!GrouperInstallerUtils.equals("0.0.0.0", this.defaultIpAddress)) {
      System.out.println("Note, you will probably need to change the hsql IP address, and tomcat server.xml IP addresses...");
    }
    
    //####################################
    //System.out.println("Grouper install directory is: " + grouperInstallDirectoryFile.getAbsolutePath());

    this.version = GrouperInstallerUtils.propertiesValue("grouper.version", true);
    System.out.println("Installing grouper version: " + this.version);
    //see if it is already downloaded
    
    //download api and set executable and dos2unix etc
    downloadAndConfigureApi();

    //####################################
    //ask about database

    File localGrouperHibernatePropertiesFile = new File(this.untarredApiDir.getAbsoluteFile() + File.separator + "conf" 
        + File.separator + "grouper.hibernate.properties");

    Properties grouperHibernateProperties = GrouperInstallerUtils.propertiesFromFile(localGrouperHibernatePropertiesFile);

    this.dbUrl = GrouperInstallerUtils.defaultString(grouperHibernateProperties.getProperty("hibernate.connection.url"), "jdbc:hsqldb:hsql://localhost:9001/grouper");
    this.dbUser = GrouperInstallerUtils.defaultString(grouperHibernateProperties.getProperty("hibernate.connection.username"));
    this.dbPass = GrouperInstallerUtils.defaultString(grouperHibernateProperties.getProperty("hibernate.connection.password"));

    boolean useHsqldb = false;

    if (this.dbUrl.contains(":hsqldb:")) {
      System.out.print("Do you want to use the default and included hsqldb database (t|f)? [t]: ");
      useHsqldb = readFromStdInBoolean(true);
    }

    if (!useHsqldb) {

      System.out.println("\n##################################\n");
      System.out.println("Example mysql URL: jdbc:mysql://localhost:3306/grouper");
      System.out.println("Example oracle URL: jdbc:oracle:thin:@server.school.edu:1521:sid");
      System.out.println("Example hsqldb URL: jdbc:hsqldb:hsql://localhost:9001/grouper");
      System.out.println("Example postgres URL: jdbc:postgresql://localhost:5432/database");
      System.out.println("Example mssql URL: jdbc:sqlserver://localhost:3280;databaseName=grouper");
      System.out.print("\nEnter the database URL [" + this.dbUrl + "]: ");
      String newDbUrl = readFromStdIn();
      if (!GrouperInstallerUtils.isBlank(newDbUrl)) {
        this.dbUrl = newDbUrl;
        if (newDbUrl.contains("postgresql") || newDbUrl.contains("sqlserver")) {
          System.out.println("Note: you need to change the search sql in the jdbc source in the grouperApi/conf/sources.xml... the change is in the comments in that file");
          for (int i=0;i<3;i++) {
            System.out.print("Ready to continue? (t|f)? [t] ");
            boolean shouldContinue = readFromStdInBoolean(true);
            if (shouldContinue) {
              break;
            }
          }
        }
      }
      System.out.print("Database user [" + this.dbUser + "]: ");
      String newDbUser = readFromStdIn();
      if (!GrouperInstallerUtils.isBlank(newDbUser)) {
        this.dbUser = newDbUser;
      }
      System.out.print("Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [" 
          + GrouperInstallerUtils.defaultIfEmpty(this.dbPass, "<blank>") + "]: ");
      String newDbPass = readFromStdIn();
      if (!GrouperInstallerUtils.isBlank(newDbPass)) {
        this.dbPass = newDbPass;
      }
    }

    this.giDbUtils = new GiDbUtils(this.dbUrl, this.dbUser, this.dbPass);

    //####################################
    //change the config file
    //get the config file

    //lets edit the three properties:
    System.out.println("Editing " + localGrouperHibernatePropertiesFile.getAbsolutePath() + ": ");
    editPropertiesFile(localGrouperHibernatePropertiesFile, "hibernate.connection.url", this.dbUrl);
    editPropertiesFile(localGrouperHibernatePropertiesFile, "hibernate.connection.username", this.dbUser);
    editPropertiesFile(localGrouperHibernatePropertiesFile, "hibernate.connection.password", this.dbPass);

    //####################################
    //check to see if listening on port?

    //####################################
    //lets get the java command

    validateJavaVersion();

    //#####################################
    //add driver to classpath
    this.addDriverJarToClasspath();

    //####################################
    //start database if needed (check on port?  ask to change port?)
    if (this.dbUrl.contains("hsqldb")) {
      //C:\mchyzer\grouper\trunk\grouper-installer\grouper.apiBinary-2.1.0
      startHsqlDb();
    }
    
    //####################################
    //check connection to database
    checkDatabaseConnection();
    
    //####################################
    //ask then init the DB
    initDb();
    addQuickstartSubjects();
    addQuickstartData();
    
    //#####################################
    //start the loader
    startLoader();
    
    //####################################
    //download and configure ui
    downloadAndConfigureUi();

    //####################################
    //get ant
    downloadAndUnzipAnt();
    
    //####################################
    //look for or ask or download tomcat
    File tomcatDir = downloadTomcat();
    File unzippedTomcatFile = unzip(tomcatDir.getAbsolutePath());
    this.untarredTomcatDir = untar(unzippedTomcatFile.getAbsolutePath());
    
    //####################################
    //ask for tomcat port
    configureTomcat();
    
    //####################################
    //build UI
    buildUi(true);

    //####################################
    //configureTomcatUiWebapp
    configureTomcatUiWebapp();
    
    //####################################
    //set the GrouperSystem password
    tomcatConfigureGrouperSystem();
    
    //####################################
    //bounce tomcat
    tomcatBounce("restart");
    
    //####################################
    //tell user to go to url
    System.out.println("##################################\n");
    System.out.println("Go here for the Grouper UI (change hostname if on different host): http://localhost:" + this.tomcatHttpPort + "/" + this.tomcatUiPath + "/");
    System.out.println("\n##################################\n");

    this.downloadAndConfigureWs();
    
    //####################################
    //build WS
    buildWs(true);
    
    //####################################
    //copy to tomcat
    configureTomcatWsWebapp();
    
    //####################################
    //bounce tomcat
    tomcatBounce("restart");

    //####################################
    //tell user to go to url
    System.out.println("This is the Grouper WS URL (change hostname if on different host): http://localhost:" + this.tomcatHttpPort + "/" + this.tomcatWsPath + "/");

    //download and build client
    this.downloadAndBuildClient();

    //####################################
    //configure where WS is
    this.configureClient();

    //####################################
    //add grouper system to WS group
    this.addGrouperSystemWsGroup();
    
    //####################################
    //run a client command
    this.runClientCommand();
    
    //####################################
    //install psp
    System.out.print("Do you want to install the provisioning service provider (t|f)? [t]: ");
    if (readFromStdInBoolean(true)) {
    	downloadAndBuildPsp();              
      GrouperInstallerUtils.copyDirectory(this.untarredPspDir, this.untarredApiDir);
    }    

    reportOnConflictingJars();

    //####################################
    //success
    System.out.println("\nInstallation success!");
    System.out.println("\nGo here for the Grouper UI (change hostname if on different host): http://localhost:" + this.tomcatHttpPort + "/" + this.tomcatUiPath + "/");
    System.out.println("\nThis is the Grouper WS URL (change hostname if on different host): http://localhost:" + this.tomcatHttpPort + "/" + this.tomcatWsPath + "/");
    System.out.println("\n##################################\n");
    
  }
  /**
   * 
   */
  private void downloadAndBuildPsp() {
    File pspDir = downloadPsp();
    File unzippedPspFile = unzip(pspDir.getAbsolutePath());
    this.untarredPspDir = untar(unzippedPspFile.getAbsolutePath());
  }
  
  /** untarred psp dir file */
  private File untarredPspDir;
  
  /**
   * 
   */
  public void downloadAndUnzipAnt() {
    File antDir = downloadAnt();
    File unzippedAntFile = unzip(antDir.getAbsolutePath());
    this.untarredAntDir = untar(unzippedAntFile.getAbsolutePath());
  }

  /**
   * 
   */
  public void downloadAndConfigureWs() {

    //####################################
    //download the ws
    File wsDir = downloadWs();

    //####################################
    //unzip/untar the ws file
    File unzippedWsFile = unzip(wsDir.getAbsolutePath());
    this.untarredWsDir = untar(unzippedWsFile.getAbsolutePath());

    //####################################
    //configure where api is
    this.configureWs();

  }

  /**
   * 
   */
  public void downloadAndConfigureUi() {
    //####################################
    //get UI
    File uiDir = downloadUi();
    
    //####################################
    //unzip/untar the ui file
    File unzippedUiFile = unzip(uiDir.getAbsolutePath());
    this.untarredUiDir = untar(unzippedUiFile.getAbsolutePath());

    //####################################
    //configure UI
    configureUi();
  }

  /**
   * 
   */
  public void downloadAndConfigureApi() {
    File apiFile = downloadApi();
    
    //####################################
    //unzip/untar the api file
    
    File unzippedApiFile = unzip(apiFile.getAbsolutePath());
    this.untarredApiDir = untar(unzippedApiFile.getAbsolutePath());
    
    gshExcutableAndDos2Unix(this.untarredApiDir.getAbsolutePath() + File.separator + "bin" + File.separator);
  }

  /**
   * @param binDirLocation which includes trailing slash
   */
  public void gshExcutableAndDos2Unix(String binDirLocation) {
    //lts make sure gsh is executable and in unix format

    if (!GrouperInstallerUtils.isWindows()) {

      System.out.print("Do you want to set gsh script to executable (t|f)? [t]: ");
      boolean setGshFile = readFromStdInBoolean(true);
      
      if (setGshFile) {
      
        List<String> commands = GrouperInstallerUtils.toList("chmod", "+x", 
            binDirLocation + "gsh.sh");
  
        System.out.println("Making sure gsh.sh is executable with command: " + convertCommandsIntoCommand(commands) + "\n");
  
        CommandResult commandResult = GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
            new File(binDirLocation), null);
        
        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }

        commands = GrouperInstallerUtils.toList("chmod", "+x", 
            binDirLocation + "gsh");
  
        System.out.println("Making sure gsh is executable with command: " + convertCommandsIntoCommand(commands) + "\n");
  
        commandResult = GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
            new File(binDirLocation), null);
        
        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }

        System.out.print("Do you want to run dos2unix on gsh.sh (t|f)? [t]: ");
        setGshFile = readFromStdInBoolean(true);
        
        if (setGshFile) {
          
          commands = GrouperInstallerUtils.toList("dos2unix", 
              binDirLocation + "gsh.sh");
    
          System.out.println("Making sure gsh.sh is in unix format: " + convertCommandsIntoCommand(commands) + "\n");
          String error = null;
          try {
            commandResult = GrouperInstallerUtils.execCommand(
                GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                new File(binDirLocation), null);

            if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
              System.out.println("stderr: " + commandResult.getErrorText());
            }
            if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
              System.out.println("stdout: " + commandResult.getOutputText());
            }

            commands = GrouperInstallerUtils.toList("dos2unix", 
                binDirLocation + "gsh");
      
            System.out.println("Making sure gsh is in unix format: " + convertCommandsIntoCommand(commands) + "\n");
            commandResult = GrouperInstallerUtils.execCommand(
                GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                new File(binDirLocation), null);

          } catch (Throwable t) {
            error = t.getMessage();
          }
          if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
            System.out.println("stderr: " + commandResult.getErrorText());
          }
          if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
            System.out.println("stdout: " + commandResult.getOutputText());
          }
          if (!GrouperInstallerUtils.isBlank(error)) {
            System.out.println("Error: " + error);
            System.out.println("NOTE: you might need to run this to convert newline characters to mac/unix:\n\n" +
            		"cat " + binDirLocation + "gsh.sh" 
            		+ " | col -b > " + binDirLocation + "gsh.sh\n");
            System.out.println("\n" +
                "cat " + binDirLocation + "gsh" 
                + " | col -b > " + binDirLocation + "gsh\n");
          }
        }
      }
      
    }
  }
  
  /**
   * 
   */
  public void downloadAndBuildClient() {
    //####################################
    //download the client
    File clientDir = downloadClient();

    //####################################
    //unzip/untar the client file
    File unzippedClientFile = unzip(clientDir.getAbsolutePath());
    this.untarredClientDir = untar(unzippedClientFile.getAbsolutePath());
  }

  /**
   * 
   */
  private int tomcatHttpPort = -1;
  
  
  /**
   * 
   */
  private void configureTomcat() {
    
    System.out.print("Do you want to set the tomcat memory limit (t|f)? [t]: ");
    boolean setTomcatMemory = readFromStdInBoolean(true);
    
    if (setTomcatMemory) {
      
      {
        File catalinaBatFile = new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + "catalina.bat");
        
        System.out.println("Editing file: " + catalinaBatFile.getAbsolutePath());
        
        Boolean edited = editFile(catalinaBatFile, "^\\s*set\\s+\"JAVA_OPTS\\s*=.*-Xmx([0-9mMgG]+)", null, null, "512M", "max memory");
        if (edited == null) {
          addToFile(catalinaBatFile, "\nset \"JAVA_OPTS=-server -Xmx512M -XX:MaxPermSize=256M\"\n", 65, "max memory");
        }
        if (null == editFile(catalinaBatFile, "^\\s*set\\s+\"JAVA_OPTS\\s*=.*-XX:MaxPermSize=([0-9mMgG]+)", null, null, "256M", "permgen memory")) {
          throw new RuntimeException("Why not edit permgen in file " + catalinaBatFile);
        }
      }
      
      {
        File catalinaShFile = new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + "catalina.sh");
        
        System.out.println("Editing file: " + catalinaShFile.getAbsolutePath());

        Boolean edited = editFile(catalinaShFile, "^\\s*JAVA_OPTS\\s*=\".*-Xmx([0-9mMgG]+)", null, null, "512M", "max memory");
        if (edited == null) {
          addToFile(catalinaShFile, "\nJAVA_OPTS=\"-server -Xmx512M -XX:MaxPermSize=256M\"\n", 65, "max memory");
        }
        if (null == editFile(catalinaShFile, "^\\s*JAVA_OPTS\\s*=\".*-XX:MaxPermSize=([0-9mMgG]+)", null, null, "256M", "permgen memory")) {
          throw new RuntimeException("Why not edit permgen in file " + catalinaShFile);
        }
      }
    }      
    
    
    if (!GrouperInstallerUtils.isWindows()) {

      System.out.print("Do you want to set tomcat scripts to executable (t|f)? [t]: ");
      boolean setTomcatFiles = readFromStdInBoolean(true);
      
      //GrouperInstallerUtils.toSet("catalina.sh", "startup.sh", "shutdown.sh");
      Set<String> shFileNames = new HashSet<String>();

      File binDir = new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin");

      //get all sh files, doing wildcards doesnt work
      for (File file : binDir.listFiles()) {
        String fileName = GrouperInstallerUtils.defaultString(file.getName());
        if (file.isFile() && fileName.endsWith(".sh")) {
          shFileNames.add(fileName);
        }
      }

      if (setTomcatFiles) {
      
        for (String command : shFileNames) {
          List<String> commands = new ArrayList<String>();
          
          commands.add("chmod");
          commands.add("+x");
          //have to do * since all the  sh files need chmod
          commands.add(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + command);
    
          System.out.println("Making tomcat file executable with command: " + convertCommandsIntoCommand(commands) + "\n");
    
          CommandResult commandResult = GrouperInstallerUtils.execCommand(
              GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
              new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null);
          
          if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
            System.out.println("stderr: " + commandResult.getErrorText());
          }
          if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
            System.out.println("stdout: " + commandResult.getOutputText());
          }
        }
      }
      
      System.out.print("Do you want to run dos2unix on tomcat sh files (t|f)? [t]: ");
      boolean dos2unix = readFromStdInBoolean(true);
      
      if (dos2unix) {
        
        try {
          
          for (String command : shFileNames) {
            
            List<String> commands = new ArrayList<String>();
            
            commands.add("dos2unix");
            commands.add(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + command);
      
            System.out.println("Making tomcat file in unix format: " + convertCommandsIntoCommand(commands) + "\n");
      
            CommandResult commandResult = GrouperInstallerUtils.execCommand(
                GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null);
            
            if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
              System.out.println("stderr: " + commandResult.getErrorText());
            }
            if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
              System.out.println("stdout: " + commandResult.getOutputText());
            }
          }

        } catch (Throwable t) {
          String error = t.getMessage();
          System.out.println("Error: " + error);
          System.out.println("NOTE: you might need to run these to convert newline characters to mac/unix:\n");
          for (String command : shFileNames) {
            String fullPath = this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + command;
            System.out.println("cat " + fullPath 
            + " | col -b > " + fullPath + "\n");
          }
        }
      }
    }
      
    //see what the current ports are
    this.tomcatHttpPort = -1;
    
    File serverXmlFile = new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "conf" + File.separator + "server.xml");
    
    int shutdownPort = GrouperInstallerUtils.xpathEvaluateAttributeInt(serverXmlFile, "/Server", "port", -1);
    
    //  /Server/Service/Connector <Connector port="8080" protocol="HTTP/1.1" 
    this.tomcatHttpPort = GrouperInstallerUtils.xpathEvaluateAttributeInt(serverXmlFile, "/Server/Service/Connector[@protocol='HTTP/1.1']", "port", -1);
    // <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
    int jkPort = GrouperInstallerUtils.xpathEvaluateAttributeInt(serverXmlFile, "/Server/Service/Connector[@protocol='AJP/1.3']", "port", -1);
    
    while(true) {
      System.out.print("What ports do you want tomcat to run on (HTTP, JK, shutdown): [" + this.tomcatHttpPort + ", " + jkPort + ", " + shutdownPort + "]: ");
      
      String ports = readFromStdIn();
      
      if (GrouperInstallerUtils.isBlank(ports)) {
        break;
      } 
      String[] portsArray = GrouperInstallerUtils.splitTrim(ports, ",");
      if (GrouperInstallerUtils.length(portsArray) == 3) {
        for (String portString : portsArray) {
          try {
            GrouperInstallerUtils.intValue(portString);
          } catch (Exception e) {
            continue;
          }
        }
      } else {
        continue;
      }
      //ok, we have three integer entries
      this.tomcatHttpPort = GrouperInstallerUtils.intValue(portsArray[0]);
      jkPort = GrouperInstallerUtils.intValue(portsArray[1]);
      shutdownPort = GrouperInstallerUtils.intValue(portsArray[2]);
      
      if (!GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress)) {
        System.out.print("The tomcat HTTP port is in use or unavailable: " + this.tomcatHttpPort + ", do you want to pick different ports? (t|f): ");
        boolean pickDifferentPorts = readFromStdInBoolean(null);
        if (pickDifferentPorts) {
          continue;
        }
      }
      if (!GrouperInstallerUtils.portAvailable(jkPort, this.defaultIpAddress)) {
        System.out.print("The tomcat JK port is in use or unavailable: " + this.tomcatHttpPort + ", do you want to pick different ports? (t|f): ");
        boolean pickDifferentPorts = readFromStdInBoolean(null);
        if (pickDifferentPorts) {
          continue;
        }
      }
      
      System.out.println("Editing tomcat config file: " + serverXmlFile.getAbsolutePath());
      //lets edit the file
      //<Connector port="8080" protocol="HTTP/1.1" 
      editFile(serverXmlFile, "port=\"([\\d]+)\"", new String[]{"<Connector", "protocol=\"HTTP/1.1\""}, 
          new String[]{"SSLEnabled=\"true\""}, Integer.toString(this.tomcatHttpPort), "tomcat HTTP port");
      //<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
      editFile(serverXmlFile, "port=\"([\\d]+)\"", new String[]{"<Connector", "protocol=\"AJP/1.3\""}, null, Integer.toString(jkPort), "tomcat JK port");
      //<Server port="8005" shutdown="SHUTDOWN">
      editFile(serverXmlFile, "port=\"([\\d]+)\"", new String[]{"<Server", "shutdown=\"SHUTDOWN\""}, null, Integer.toString(shutdownPort), "tomcat shutdown port");
      break;
    }

    configureTomcatUriEncoding(serverXmlFile);
    
  }
  /**
   * @param serverXmlFile
   */
  public void configureTomcatUriEncoding(File serverXmlFile) {
    //set encoding for connectors
    //  /Server/Service/Connector <Connector port="8080" protocol="HTTP/1.1" 
    String uriEncodingHttp = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "/Server/Service/Connector[@protocol='HTTP/1.1']", "URIEncoding");
    
    // <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
    String uriEncodingAjp = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "/Server/Service/Connector[@protocol='AJP/1.3']", "URIEncoding");

    if (!GrouperInstallerUtils.equals(uriEncodingAjp, "UTF-8") || !GrouperInstallerUtils.equals(uriEncodingHttp, "UTF-8")) {

      boolean defaultSetUriEncoding = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.ui.setTomcatUriEncoding", true, false);
      System.out.print("Do you want to set URIEncoding to UTF-8 in tomcat server.xml <Connector> elements (t|f)? [" 
          + (defaultSetUriEncoding ? "t" : "f") + "]: ");
      boolean assignUriEncoding = readFromStdInBoolean(defaultSetUriEncoding);

      if (assignUriEncoding) {
        
        if (!GrouperInstallerUtils.equals(uriEncodingAjp, "UTF-8")) {
          editFile(serverXmlFile, "URIEncoding=\"([^\"]+)\"", new String[]{"<Connector", "protocol=\"AJP/1.3\""}, 
              new String[]{"SSLEnabled=\"true\""}, "UTF-8", "tomcat URIEncoding attribute for element <Connector AJP", true, "URIEncoding");
          
        }
        
        if (!GrouperInstallerUtils.equals(uriEncodingHttp, "UTF-8")) {
          editFile(serverXmlFile, "URIEncoding=\"([^\"]+)\"", new String[]{"<Connector", "protocol=\"HTTP/1.1\""}, 
              new String[]{"SSLEnabled=\"true\""}, "UTF-8", "tomcat URIEncoding attribute for element <Connector HTTP", true, "URIEncoding");
          
        }
      }

    }
  }

  /**
   * 
   * @param newEhcacheExampleFile
   * @param existingEhcacheExampleFile
   * @param existingEhcacheFile
   */
  public static void mergeEhcacheXmlFiles(File newEhcacheExampleFile, File existingEhcacheExampleFile, File existingEhcacheFile) {
    
    try {
      //lets get the differences of the existing ehcache file and the existing ehcache example file
      DocumentBuilderFactory domFactory = GrouperInstallerUtils.xmlDocumentBuilderFactory();
      DocumentBuilder builder = domFactory.newDocumentBuilder();
      Document existingEhcacheDoc = builder.parse(existingEhcacheFile);
      Document existingEhcacheExampleDoc = builder.parse(existingEhcacheExampleFile);

      Element existingDocumentElement = existingEhcacheDoc.getDocumentElement();
      Element existingExampleDocumentElement = existingEhcacheExampleDoc.getDocumentElement();

      Map<String, String> diskStoreDifferences = null;
      
      {
        Element existingDiskStoreElement = (Element)existingDocumentElement.getElementsByTagName("diskStore").item(0);
        Element existingExampleDiskStoreElement = (Element)existingExampleDocumentElement.getElementsByTagName("diskStore").item(0);
        diskStoreDifferences = xmlNodeAttributeDifferences(existingExampleDiskStoreElement, existingDiskStoreElement);
      }

      Map<String, String> defaultCacheDifferences = null;

      {
        Element existingDefaultCacheElement = (Element)existingDocumentElement.getElementsByTagName("defaultCache").item(0);
        Element existingExampleDefaultCacheElement = (Element)existingExampleDocumentElement.getElementsByTagName("defaultCache").item(0);
        defaultCacheDifferences = xmlNodeAttributeDifferences(existingExampleDefaultCacheElement, existingDefaultCacheElement);
        
      }

      XPath xpath = XPathFactory.newInstance().newXPath();
      
      //map of cache name to the differences in the attributes of the cache
      Map<String, Map<String, String>> cacheDifferencesByCacheName = new LinkedHashMap<String, Map<String, String>>();
      
      {
        NodeList existingCacheNodeList = existingDocumentElement.getElementsByTagName("cache");
        
        //loop through all the caches
        for (int i=0;i<existingCacheNodeList.getLength(); i++) {
          
          Element existingCacheElement = (Element)existingCacheNodeList.item(i);

          String cacheName = existingCacheElement.getAttribute("name");

          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");
          Element existingExampleCacheElement = (Element)expr.evaluate(existingExampleDocumentElement, XPathConstants.NODE);

          //see if they differ
          Map<String, String> differences = xmlNodeAttributeDifferences(existingExampleCacheElement, existingCacheElement);
          
          if (differences != null) {
            cacheDifferencesByCacheName.put(cacheName, differences);
          }
        }
        
        //note, dont worry if there were caches in the example that werent in the configured one
        
      }      
      
      //lets see if there are any other nodes
      Set<Element> otherNodes = new LinkedHashSet<Element>();
      {
        NodeList nodeList = existingDocumentElement.getChildNodes();
        
        for (int i=0;i<nodeList.getLength();i++) {
          Node node = nodeList.item(i);
          if (node instanceof Element) {
            Element nodeElement = (Element)node;
            String nodeName = nodeElement.getNodeName();
            if (!GrouperInstallerUtils.equals(nodeName, "cache")
                && !GrouperInstallerUtils.equals(nodeName, "defaultCache")
                && !GrouperInstallerUtils.equals(nodeName, "diskStore")) {
              otherNodes.add(nodeElement);
            }
          }
        }
      }
      
      //lets copy the new example to both the example and the configured ehcache file
      //assume this is already backed up
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheExampleFile);
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheFile);

      //now lets do our edits
      if (GrouperInstallerUtils.length(diskStoreDifferences) > 0) {
        
        for (String attributeName : diskStoreDifferences.keySet()) {

          String attributeValue = diskStoreDifferences.get(attributeName);

          editXmlFileAttribute(existingEhcacheFile, "diskStore", null, attributeName, attributeValue, 
              "ehcache diskStore attribute '" + attributeName + "'");
          
        }
      }
      
      if (GrouperInstallerUtils.length(defaultCacheDifferences) > 0) {
        
        for (String attributeName : defaultCacheDifferences.keySet()) {
          
          String attributeValue = defaultCacheDifferences.get(attributeName);

          editXmlFileAttribute(existingEhcacheFile, "defaultCache", null, attributeName, attributeValue, 
              "ehcache defaultCache attribute '" + attributeName + "'");

        }
      }

      if (GrouperInstallerUtils.length(cacheDifferencesByCacheName) > 0) {

        existingEhcacheDoc = builder.parse(existingEhcacheFile);
        existingDocumentElement = existingEhcacheDoc.getDocumentElement();
        
        for (String cacheName : cacheDifferencesByCacheName.keySet()) {

          //see if the name exists
          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");

          Element existingCacheElement = (Element)expr.evaluate(existingDocumentElement, XPathConstants.NODE);

          Map<String, String> attributeMap = cacheDifferencesByCacheName.get(cacheName);

          //it exists
          if (existingCacheElement != null) {

            Map<String, String> expectedAttribute = new HashMap<String, String>();

            expectedAttribute.put("name", cacheName);
            
            for (String attributeName : attributeMap.keySet()) {

              String attributeValue = attributeMap.get(attributeName);

              editXmlFileAttribute(existingEhcacheFile, "cache", expectedAttribute, attributeName, attributeValue, 
                  "ehcache cache name=" + cacheName + " attribute '" + attributeName + "'");
            }
          } else {

              String fileContents = GrouperInstallerUtils.readFileIntoString(existingEhcacheFile);

              String newline = GrouperInstallerUtils.newlineFromFile(fileContents);

              int lastTagStart = fileContents.lastIndexOf("</ehcache>");
              
              if (lastTagStart == -1) {
                throw new RuntimeException("Why is </ehcache> not found???? " + fileContents);
              }

              String tag = GrouperInstallerUtils.xmlElementToXml("cache", null, attributeMap);
//              sdf
              String newFileContents = fileContents.substring(0, lastTagStart) + tag + newline 
                  + fileContents.substring(lastTagStart, fileContents.length());

              System.out.println(" - adding ehcache cache " + cacheName);

              GrouperInstallerUtils.saveStringIntoFile(existingEhcacheFile, newFileContents);

          }

        }
      }

      if (GrouperInstallerUtils.length(otherNodes) > 0) {
        String fileContents = GrouperInstallerUtils.readFileIntoString(existingEhcacheFile);
        
        String newline = GrouperInstallerUtils.newlineFromFile(fileContents);

        StringBuilder otherNodesStringBuilder = new StringBuilder();
        for (Element element : otherNodes) {
          String elementString = GrouperInstallerUtils.xmlToString(element);
          // take out the xml header: <?xml version="1.0" encoding="UTF-8"?>
          
          int elementStart = elementString.indexOf("<" + element.getNodeName());
          
          elementString = elementString.substring(elementStart);
          
          otherNodesStringBuilder.append(elementString).append(newline);
          System.out.println(" - adding element " + element.getTagName());
        }

        int lastTagStart = fileContents.lastIndexOf("</ehcache>");
        
        if (lastTagStart == -1) {
          throw new RuntimeException("Why is </ehcache> not found???? " + fileContents);
        }

        String newFileContents = fileContents.substring(0, lastTagStart) + otherNodesStringBuilder.toString()
            + fileContents.substring(lastTagStart, fileContents.length());

        GrouperInstallerUtils.saveStringIntoFile(existingEhcacheFile, newFileContents);

      }


      // test the new file, look for things
      existingEhcacheDoc = builder.parse(existingEhcacheFile);
      existingDocumentElement = existingEhcacheDoc.getDocumentElement();

      if (GrouperInstallerUtils.length(diskStoreDifferences) > 0) {
        Element existingDiskStoreElement = (Element)existingDocumentElement.getElementsByTagName("diskStore").item(0);
        for (String attributeName : diskStoreDifferences.keySet()) {
          String attributeValue = diskStoreDifferences.get(attributeName);
          if (!GrouperInstallerUtils.equals(attributeValue, existingDiskStoreElement.getAttribute(attributeName))) {
            throw new RuntimeException("Why is diskStore attribute " + attributeName + " not '" + attributeValue + "'" 
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }
      
      if (GrouperInstallerUtils.length(defaultCacheDifferences) > 0) {
        Element existingDefaultCacheElement = (Element)existingDocumentElement.getElementsByTagName("defaultCache").item(0);
        for (String attributeName : defaultCacheDifferences.keySet()) {
          String attributeValue = defaultCacheDifferences.get(attributeName);
          if (!GrouperInstallerUtils.equals(attributeValue, existingDefaultCacheElement.getAttribute(attributeName))) {
            throw new RuntimeException("Why is defaultCache attribute " + attributeName + " not '" + attributeValue + "'" 
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }

      if (GrouperInstallerUtils.length(cacheDifferencesByCacheName) > 0) {
        for (String cacheName : cacheDifferencesByCacheName.keySet()) {

          //see if the name exists
          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");
          Element existingCacheElement = (Element)expr.evaluate(existingDocumentElement, XPathConstants.NODE);

          Map<String, String> attributeMap = cacheDifferencesByCacheName.get(cacheName);
          
          for (String attributeName : attributeMap.keySet()) {
            
            String attributeValue = attributeMap.get(attributeName);

            if (!GrouperInstallerUtils.equals(attributeValue, existingCacheElement.getAttribute(attributeName))) {
              throw new RuntimeException("Why is cache " + cacheName + " attribute " + attributeName + " not '" + attributeValue + "'" 
                  + existingEhcacheFile.getAbsolutePath());
            }
            
          }
        }
      }

      if (GrouperInstallerUtils.length(otherNodes) > 0) {
        for (Element element : otherNodes) {
          
          NodeList nodeList = existingDocumentElement.getElementsByTagName(element.getNodeName());
          if (nodeList == null || nodeList.getLength() == 0 ) {
            throw new RuntimeException("Why is new element not there? " + element.getTagName() + ", "
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * 
   * @param newEhcacheExampleFile
   * @param existingEhcacheExampleFile
   * @param existingEhcacheFile
   * @return hasMerging
   */
  @SuppressWarnings("unused")
  private static boolean mergeEhcacheXmlFiles_XML_NOT_USED(File newEhcacheExampleFile, File existingEhcacheExampleFile, 
      File existingEhcacheFile) {
    
    boolean hasMerging = false;
    
    try {
      //lets get the differences of the existing ehcache file and the existing ehcache example file
      DocumentBuilderFactory domFactory = GrouperInstallerUtils.xmlDocumentBuilderFactory();
      DocumentBuilder builder = domFactory.newDocumentBuilder();
      Document existingEhcacheDoc = builder.parse(existingEhcacheFile);
      Document existingEhcacheExampleDoc = builder.parse(existingEhcacheExampleFile);

      Element existingDocumentElement = existingEhcacheDoc.getDocumentElement();
      Element existingExampleDocumentElement = existingEhcacheExampleDoc.getDocumentElement();

      Map<String, String> diskStoreDifferences = null;
      
      {
        Element existingDiskStoreElement = (Element)existingDocumentElement.getElementsByTagName("diskStore").item(0);
        Element existingExampleDiskStoreElement = (Element)existingExampleDocumentElement.getElementsByTagName("diskStore").item(0);
        diskStoreDifferences = xmlNodeAttributeDifferences(existingExampleDiskStoreElement, existingDiskStoreElement);
      }

      Map<String, String> defaultCacheDifferences = null;

      {
        Element existingDefaultCacheElement = (Element)existingDocumentElement.getElementsByTagName("defaultCache").item(0);
        Element existingExampleDefaultCacheElement = (Element)existingExampleDocumentElement.getElementsByTagName("defaultCache").item(0);
        defaultCacheDifferences = xmlNodeAttributeDifferences(existingExampleDefaultCacheElement, existingDefaultCacheElement);
        
      }

      XPath xpath = XPathFactory.newInstance().newXPath();
      
      //map of cache name to the differences in the attributes of the cache
      Map<String, Map<String, String>> cacheDifferencesByCacheName = new LinkedHashMap<String, Map<String, String>>();
      
      {
        NodeList existingCacheNodeList = existingDocumentElement.getElementsByTagName("cache");
        
        //loop through all the caches
        for (int i=0;i<existingCacheNodeList.getLength(); i++) {
          
          Element existingCacheElement = (Element)existingCacheNodeList.item(i);

          String cacheName = existingCacheElement.getAttribute("name");

          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");
          Element existingExampleCacheElement = (Element)expr.evaluate(existingExampleDocumentElement, XPathConstants.NODE);

          //see if they differ
          Map<String, String> differences = xmlNodeAttributeDifferences(existingExampleCacheElement, existingCacheElement);
          
          if (differences != null) {
            cacheDifferencesByCacheName.put(cacheName, differences);
          }
        }
        
        //note, dont worry if there were caches in the example that werent in the configured one
        
      }      
      
      //lets see if there are any other nodes
      Set<Element> otherNodes = new LinkedHashSet<Element>();
      {
        NodeList nodeList = existingDocumentElement.getChildNodes();
        
        for (int i=0;i<nodeList.getLength();i++) {
          Node node = nodeList.item(i);
          if (node instanceof Element) {
            Element nodeElement = (Element)node;
            String nodeName = nodeElement.getNodeName();
            if (!GrouperInstallerUtils.equals(nodeName, "cache")
                && !GrouperInstallerUtils.equals(nodeName, "defaultCache")
                && !GrouperInstallerUtils.equals(nodeName, "diskStore")) {
              otherNodes.add(nodeElement);
            }
          }
        }
      }
      
      //lets copy the new example to both the example and the configured ehcache file
      //assume this is already backed up
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheExampleFile);

      //this is the new existing ehcache file
      existingEhcacheExampleDoc = builder.parse(existingEhcacheExampleFile);
      existingExampleDocumentElement = existingEhcacheExampleDoc.getDocumentElement();

      //now lets do our edits
      if (GrouperInstallerUtils.length(diskStoreDifferences) > 0) {
        
        hasMerging = true;

        Element existingExampleDiskStoreElement = (Element)existingExampleDocumentElement.getElementsByTagName("diskStore").item(0);

        for (String attributeName : diskStoreDifferences.keySet()) {

          String attributeValue = diskStoreDifferences.get(attributeName);

          existingExampleDiskStoreElement.setAttribute(attributeName, attributeValue);
        }
      }
      
      if (GrouperInstallerUtils.length(defaultCacheDifferences) > 0) {

        hasMerging = true;

        Element existingExampleDefaultCacheElement = (Element)existingExampleDocumentElement.getElementsByTagName("defaultCache").item(0);
        
        for (String attributeName : defaultCacheDifferences.keySet()) {
          
          String attributeValue = defaultCacheDifferences.get(attributeName);

          existingExampleDefaultCacheElement.setAttribute(attributeName, attributeValue);
          
        }
      }

      if (GrouperInstallerUtils.length(cacheDifferencesByCacheName) > 0) {
        hasMerging = true;
        for (String cacheName : cacheDifferencesByCacheName.keySet()) {

          //see if the name exists
          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");
          Element existingExampleCacheElement = (Element)expr.evaluate(existingExampleDocumentElement, XPathConstants.NODE);

          Map<String, String> attributeMap = cacheDifferencesByCacheName.get(cacheName);
          
          //it exists
          if (existingExampleCacheElement != null) {
            
            for (String attributeName : attributeMap.keySet()) {
              
              String attributeValue = attributeMap.get(attributeName);
              existingExampleCacheElement.setAttribute(attributeName, attributeValue);
              
            }
          } else {
            
            Element existingCacheElement = (Element)expr.evaluate(existingDocumentElement, XPathConstants.NODE);
            //move a cache from one document to another
            existingExampleDocumentElement.appendChild(existingCacheElement.cloneNode(true));
            
          }
          
        }
      }

      if (GrouperInstallerUtils.length(otherNodes) > 0) {
        hasMerging = true;
        for (Element element : otherNodes) {
          
          //move a cache from one document to another
          existingExampleDocumentElement.appendChild(element.cloneNode(true));
        }
      }

//      System.out.println("Compare you old ehcache.xml with your new ehcache.xml file: " 
//          + "\n  Old file: "
//          + backedUpEhcacheFile.getAbsolutePath()
//          + "\n  New file: " + existingEhcacheFile.getAbsolutePath());

      // save to file
      String xml = GrouperInstallerUtils.xmlToString(existingEhcacheExampleDoc);
      GrouperInstallerUtils.saveStringIntoFile(existingEhcacheFile, xml);
      
      // test the new file, look for things
      existingEhcacheDoc = builder.parse(existingEhcacheFile);
      existingDocumentElement = existingEhcacheDoc.getDocumentElement();

      if (GrouperInstallerUtils.length(diskStoreDifferences) > 0) {
        Element existingDiskStoreElement = (Element)existingDocumentElement.getElementsByTagName("diskStore").item(0);
        for (String attributeName : diskStoreDifferences.keySet()) {
          String attributeValue = diskStoreDifferences.get(attributeName);
          if (!GrouperInstallerUtils.equals(attributeValue, existingDiskStoreElement.getAttribute(attributeName))) {
            throw new RuntimeException("Why is diskStore attribute " + attributeName + " not '" + attributeValue + "'" 
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }
      
      if (GrouperInstallerUtils.length(defaultCacheDifferences) > 0) {
        Element existingDefaultCacheElement = (Element)existingDocumentElement.getElementsByTagName("defaultCache").item(0);
        for (String attributeName : defaultCacheDifferences.keySet()) {
          String attributeValue = defaultCacheDifferences.get(attributeName);
          if (!GrouperInstallerUtils.equals(attributeValue, existingDefaultCacheElement.getAttribute(attributeName))) {
            throw new RuntimeException("Why is defaultCache attribute " + attributeName + " not '" + attributeValue + "'" 
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }

      if (GrouperInstallerUtils.length(cacheDifferencesByCacheName) > 0) {
        for (String cacheName : cacheDifferencesByCacheName.keySet()) {

          //see if the name exists
          //find the example cache
          XPathExpression expr = xpath.compile("cache[@name='" + cacheName + "']");
          Element existingCacheElement = (Element)expr.evaluate(existingDocumentElement, XPathConstants.NODE);

          Map<String, String> attributeMap = cacheDifferencesByCacheName.get(cacheName);
          
          for (String attributeName : attributeMap.keySet()) {
            
            String attributeValue = attributeMap.get(attributeName);

            if (!GrouperInstallerUtils.equals(attributeValue, existingCacheElement.getAttribute(attributeName))) {
              throw new RuntimeException("Why is cache " + cacheName + " attribute " + attributeName + " not '" + attributeValue + "'" 
                  + existingEhcacheFile.getAbsolutePath());
            }
            
          }
        }
      }

      if (GrouperInstallerUtils.length(otherNodes) > 0) {
        for (Element element : otherNodes) {
          
          NodeList nodeList = existingDocumentElement.getElementsByTagName(element.getNodeName());
          if (nodeList == null || nodeList.getLength() == 0 ) {
            throw new RuntimeException("Why is new element not there? " + element.getTagName() 
                + existingEhcacheFile.getAbsolutePath());
          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return hasMerging;
  }
  /**
   * 
   * @param baseElement
   * @param configuredElement
   * @return the map of differences
   */
  public static Map<String, String> xmlNodeAttributeDifferences(Element baseElement, Element configuredElement) {
    NamedNodeMap configuredNamedNodeMap = configuredElement.getAttributes();
    
    Map<String, String> result = null;
    
    //see which attributes are new or changed
    for (int i=0;i<configuredNamedNodeMap.getLength();i++) {
      Node configuredAttribute = configuredNamedNodeMap.item(i);
      Node baseAttribute = baseElement == null ? null : baseElement.getAttributeNode(configuredAttribute.getNodeName());

      String configuredValue = configuredAttribute.getNodeValue();
      String baseValue = baseAttribute == null ? null : baseAttribute.getNodeValue();
      
      if (!GrouperInstallerUtils.equals(configuredValue, baseValue)) {
        if (result == null) {
          result = new LinkedHashMap<String, String>();
        }
        result.put(configuredAttribute.getNodeName(), configuredValue);
      }
    }
    
    //see which ones are missing
    NamedNodeMap baseNamedNodeMap = baseElement == null ? null : baseElement.getAttributes();
    
    //see which attributes are new or changed
    for (int i=0;i<(baseNamedNodeMap == null ? 0 : baseNamedNodeMap.getLength());i++) {
      
      Node baseAttribute = configuredNamedNodeMap.item(0);
      Node configuredAttribute = configuredElement.getAttributeNode(baseAttribute.getNodeName());

      String baseValue = baseAttribute.getNodeValue();
      String configuredValue = configuredAttribute == null ? null : configuredAttribute.getNodeValue();
      
      if (configuredValue == null && !GrouperInstallerUtils.equals(configuredValue, baseValue)) {
        if (result == null) {
          result = new LinkedHashMap<String, String>();
        }
        result.put(baseAttribute.getNodeName(), configuredValue);
      }
    }
    
    return result;
  }
  
  /**
   * 
   * @return the file of the directory of API
   */
  private File downloadApi() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";
    String apiFileName = "grouper.apiBinary-" + this.version + ".tar.gz";
    urlToDownload += this.version + "/" + apiFileName;

    File apiFile = new File(this.grouperTarballDirectoryString + apiFileName);
    
    downloadFile(urlToDownload, apiFile.getAbsolutePath());

    return apiFile;
  }

  /**
   * 
   * @return the file of the directory of UI
   */
  private File downloadUi() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";

    String uiFileName = "grouper.ui-" + this.version + ".tar.gz";
    urlToDownload += this.version + "/" + uiFileName;

    File uiFile = new File(this.grouperTarballDirectoryString + uiFileName);
    
    downloadFile(urlToDownload, uiFile.getAbsolutePath());

    return uiFile;
  }

  /**
   * 
   * @return the file of the directory of WS
   */
  private File downloadWs() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";

    String wsFileName = "grouper.ws-" + this.version + ".tar.gz";
    urlToDownload += this.version + "/" + wsFileName;

    File wsFile = new File(this.grouperTarballDirectoryString + wsFileName);
    
    downloadFile(urlToDownload, wsFile.getAbsolutePath());

    return wsFile;
  }

  /**
   * 
   * @return the file of the directory of ant
   */
  private File downloadAnt() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }

    urlToDownload += "downloads/tools/apache-ant-1.8.2-bin.tar.gz";
    
    File antFile = new File(this.grouperTarballDirectoryString + "apache-ant-1.8.2-bin.tar.gz");
    
    downloadFile(urlToDownload, antFile.getAbsolutePath());

    return antFile;
  }

  /**
   * 
   * @return the file of the directory of tomcat
   */
  private File downloadTomcat() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }

    urlToDownload += "downloads/tools/apache-tomcat-6.0.35.tar.gz";
    
    File tomcatFile = new File(this.grouperTarballDirectoryString + "apache-tomcat-6.0.35.tar.gz");
    
    downloadFile(urlToDownload, tomcatFile.getAbsolutePath());

    return tomcatFile;
  }

  /**
   * add quick start subjects
   */
  private void addQuickstartSubjects() {
    
    System.out.print("Do you want to add quickstart subjects to DB (t|f)? [t]: ");
    boolean addQuickstartSubjects = readFromStdInBoolean(true);
    
    if (addQuickstartSubjects) {

      String url = GrouperInstallerUtils.propertiesValue("download.server.url", true);
      
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += "release/" + this.version + "/subjects.sql";

      String subjectsSqlFileName = this.untarredApiDir.getParent() + File.separator + "subjects.sql";
      File subjectsSqlFile = new File(subjectsSqlFileName);
      downloadFile(url, subjectsSqlFileName);

      List<String> commands = new ArrayList<String>();
      
      addGshCommands(commands);
      commands.add("-registry");
      commands.add("-runsqlfile");
      commands.add(subjectsSqlFile.getAbsolutePath());
      commands.add("-noprompt");
      
      System.out.println("\n##################################");
      System.out.println("Adding sample subjects with command: " + convertCommandsIntoCommand(commands) + "\n");
      
      CommandResult commandResult = GrouperInstallerUtils.execCommand(
          GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
          this.untarredApiDir, null);
      
      if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
        System.out.println("stderr: " + commandResult.getErrorText());
      }
      if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
        System.out.println("stdout: " + commandResult.getOutputText());
      }
      
      System.out.println("\nEnd adding sample subjects");
      System.out.println("##################################\n");

    }
  }
  
  /**
   * add quick start subjects
   */
  private void addQuickstartData() {
    
    System.out.print("Do you want to add quickstart data to registry (t|f)? [t] ");
    boolean addQuickstartData = readFromStdInBoolean(true);
    
    if (addQuickstartData) {
      String url = GrouperInstallerUtils.propertiesValue("download.server.url", true);
      
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += "release/" + this.version + "/quickstart.xml";
      String quickstartFileName = this.untarredApiDir.getParent() + File.separator + "quickstart.xml";
      
      File quickstartFile = new File(quickstartFileName);
      downloadFile(url, quickstartFileName);

      List<String> commands = new ArrayList<String>();
      
      addGshCommands(commands);
      commands.add("-xmlimportold");
      commands.add("GrouperSystem");
      commands.add(quickstartFile.getAbsolutePath());
      commands.add("-noprompt");
      
      System.out.println("\n##################################");
      System.out.println("Adding quickstart data with command: " + convertCommandsIntoCommand(commands) + "\n");
      
      CommandResult commandResult = GrouperInstallerUtils.execCommand(
          GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
          this.untarredApiDir, null);
      
      if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
        System.out.println("stderr: " + commandResult.getErrorText());
      }
      if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {

        System.out.println("stdout: " + commandResult.getOutputText());
      }
      System.out.println("\nEnd adding quickstart data");
      System.out.println("##################################\n");

    }
  }
  
  /**
   * if commands have spaces, put quotes around...
   * @param commands
   * @return the command
   */
  private static String convertCommandsIntoCommand(List<String> commands) {
    StringBuilder result = new StringBuilder();
    for (int i=0;i<GrouperInstallerUtils.length(commands); i++) {
      String command = GrouperInstallerUtils.defaultString(commands.get(i));
      
      //if there is a space, put quotes around command
      if (command.contains(" ")) {
        result.append("\"").append(command).append("\"");
      } else {
        result.append(command);
      }
      if (i != GrouperInstallerUtils.length(commands)-1) {
        result.append(" ");
      }
    }
    return result.toString();
  }
  
  /**
   * 
   */
  private void initDb() {
    System.out.print("Do you want to init the database (delete all existing grouper tables, add new ones) (t|f)? ");
    boolean initdb = readFromStdInBoolean(null);
    
    if (initdb) {
      List<String> commands = new ArrayList<String>();
      
      addGshCommands(commands);
      commands.add("-registry");
      commands.add("-drop");
      commands.add("-runscript");
      commands.add("-noprompt");
      
      System.out.println("\n##################################");
      System.out.println("Initting DB with command: " + convertCommandsIntoCommand(commands) + "\n");
      
      CommandResult commandResult = GrouperInstallerUtils.execCommand(
          GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
          this.untarredApiDir, null);
      
      if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
        System.out.println("stderr: " + commandResult.getErrorText());
      }
      if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {

        System.out.println("stdout: " + commandResult.getOutputText());
      }
      System.out.println("\nEnd Initting DB");
      System.out.println("##################################\n");
      
      
    }

  }
  
  /**
   * 
   */
  private void startLoader() {
    System.out.print("Do you want to start the Grouper loader (daemons)?\n  (note, if it is already running, you need to stop it now, check " 
        + (GrouperInstallerUtils.isWindows() ? "the task manager for java.exe" : "ps -ef | grep gsh | grep loader") + ") (t|f)? [f]: ");
    boolean startLoader = readFromStdInBoolean(false);
    
    if (startLoader) {
      final List<String> commands = new ArrayList<String>();
      
      addGshCommands(commands);
      commands.add("-loader");

      System.out.println("\n##################################");
      System.out.println("Starting the Grouper loader (daemons): " + convertCommandsIntoCommand(commands) + "\n");

      //start in new thread
      Thread thread = new Thread(new Runnable() {
        
        @Override
        public void run() {
          GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
              true, true, null, GrouperInstaller.this.untarredApiDir, 
              GrouperInstaller.this.grouperTarballDirectoryString + "grouperLoader");
        }
      });
      thread.setDaemon(true);
      thread.start();
      
      System.out.println("\nEnd starting the Grouper loader (daemons)");
      System.out.println("##################################\n");
      
    }

  }

  /**
   * gsh command fully qualified
   */
  private String gshCommand;
  
  /**
   * 
   * @return the gsh command
   */
  private String gshCommand() {

    if (this.gshCommand == null) {

      String gshDir = GrouperInstallerUtils.defaultIfBlank(this.upgradeExistingApplicationDirectoryString, 
          this.untarredApiDir.getAbsolutePath() + File.separator);
      
      String gsh = gshDir + "bin" + File.separator 
          + (GrouperInstallerUtils.isWindows() ? "gsh.bat" : "gsh");
      
      if (new File(gsh).exists()) {
        this.gshCommand = gsh;
        return gsh;
      }

      gsh = gshDir + "WEB-INF" + File.separator + "bin" + File.separator 
          + (GrouperInstallerUtils.isWindows() ? "gsh.bat" : "gsh");

      if (new File(gsh).exists()) {
        this.gshCommand = gsh;
        return gsh;
      }
      
      throw new RuntimeException("Cant find gsh: " + gshDir);
    }

    return this.gshCommand;
  }

  /**
   * 
   */
  private void checkDatabaseConnection() {
    System.out.println("Checking database with query: " + this.giDbUtils.checkConnectionQuery());
    Exception exception = this.giDbUtils.checkConnection();
    if (exception == null) {
      System.out.println("Successfully tested database connection");
    } else {
      System.out.println("Error: could not connect to the database: ");
      exception.printStackTrace();
      
    }
  }

  /**
   * get hsql port
   * @return port
   */
  private int hsqlPort() {
    //get right port
    int port = 9001;
    
    //match this, get the port: jdbc:hsqldb:hsql://localhost:9001/grouper
    Pattern pattern = Pattern.compile("jdbc:hsqldb:.*:(\\d+)/.*");
    Matcher matcher = pattern.matcher(this.dbUrl);
    if (matcher.matches()) {
      port = GrouperInstallerUtils.intValue(matcher.group(1));
    }
    return port;
  }
  
  /**
   * 
   */
  private void startHsqlDb() {
    System.out.print("Do you want this script to start the hsqldb database (note, it must not be running in able to start) (t|f)? [t]: ");
    boolean startdb = readFromStdInBoolean(true);
    if (startdb) {
      
      shutdownHsql();

      //get right port
      int port = hsqlPort();
      
      if (!GrouperInstallerUtils.portAvailable(port, this.defaultIpAddress)) {
        System.out.println("This port does not seem available, even after trying to stop the DB! " + port + "...");
        if (!shouldContinue()) {
          throw new RuntimeException("This port is not available, even after trying to stop the DB! " + port);
        }
      }
      
      final List<String> command = new ArrayList<String>();

      command.add(GrouperInstallerUtils.javaCommand());
      command.add("-cp");
      command.add(this.untarredApiDir + File.separator + "lib" + File.separator + "jdbcSamples" + File.separator 
          + "hsqldb.jar");
      //-cp lib\jdbcSamples\hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper -port 9001
      command.addAll(GrouperInstallerUtils.splitTrimToList("org.hsqldb.Server -database.0 file:" 
          + this.untarredApiDir + File.separator + "grouper -dbname.0 grouper -port " + port, " "));

//        System.out.println("Starting DB with command: java -cp grouper.apiBinary-" + this.version + File.separator 
//            + "lib" + File.separator + "jdbcSamples" + File.separator 
//            + "hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper");

      System.out.println("Starting DB with command: " + GrouperInstallerUtils.join(command.iterator(), " "));

      //start in new thread
      Thread thread = new Thread(new Runnable() {
        
        @Override
        public void run() {
          GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(command, String.class),
              true, true, null, null, 
              GrouperInstaller.this.grouperTarballDirectoryString + "hsqlDb");
        }
      });
      thread.setDaemon(true);
      thread.start();
      
    }
    
    //lets sleep for a bit to let it start
    GrouperInstallerUtils.sleep(2000);
    
  }

  /** gi db utils */
  private GiDbUtils giDbUtils = null;
  
  /**
   * 
   */
  private void shutdownHsql() {
    
    try {
      this.giDbUtils.executeUpdate("SHUTDOWN", null, false);
      System.out.println("Shutting down HSQL before starting it by sending the SQL: SHUTDOWN");
    } catch (Exception e) {
      //e.printStackTrace();
      System.out.println("HSQL was not detected to be running (did not successfully stop it)");
    }
  }

  /**
   * 
   */
  private void configureTomcatUiWebapp() {
    
    File serverXmlFile = new File(this.untarredTomcatDir.getAbsolutePath() 
        + File.separator + "conf" + File.separator + "server.xml");
    
    //C:\mchyzer\grouper\trunk\grouper-installer\grouper.ui-2.0.2\dist\grouper
    //
    //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
    //Server
    //Service
    //Engine
    //Host

    System.out.print("Enter the URL path for the UI [grouper]: ");
    this.tomcatUiPath = readFromStdIn();
    
    if (GrouperInstallerUtils.isBlank(this.tomcatUiPath)) {
      this.tomcatUiPath = "grouper";
    }

    if (this.tomcatUiPath.endsWith("/") || this.tomcatUiPath.endsWith("\\")) {
      this.tomcatUiPath = this.tomcatUiPath.substring(0, this.tomcatUiPath.length()-1);
    }
    if (this.tomcatUiPath.startsWith("/") || this.tomcatUiPath.startsWith("\\")) {
      this.tomcatUiPath = this.tomcatUiPath.substring(1, this.tomcatUiPath.length());
    }
    
    String currentDocBase = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "Server/Service/Engine/Host/Context[@path='/" + this.tomcatUiPath + "']", "docBase");

    String shouldBeDocBase = grouperUiBuildToDirName();

    System.out.println("Editing tomcat config file: " + serverXmlFile.getAbsolutePath());
    
    if (GrouperInstallerUtils.isBlank(currentDocBase)) {

      //need to add it
      //<Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true" xmlNamespaceAware="false" xmlValidation="false">
      //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
      addToXmlFile(serverXmlFile, ">", new String[]{"<Host "}, 
          "<Context docBase=\"" + shouldBeDocBase + "\" path=\"/" + this.tomcatUiPath + "\" reloadable=\"false\"/>", "tomcat context for UI");

    } else {

      if (!GrouperInstallerUtils.equals(currentDocBase, shouldBeDocBase)) {
        
        //lets edit the file
        //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
        editFile(serverXmlFile, "docBase=\"([^\"]+)\"", new String[]{"<Context", "path=\"/" + this.tomcatUiPath + "\""}, 
            null, shouldBeDocBase, "tomcat context for UI");

      } else {
        
        System.out.println("  - Context is already set for Grouper UI");
        
      }
      
      
    }
    
    currentDocBase = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "Server/Service/Engine/Host/Context[@path='/" + this.tomcatUiPath + "']", "docBase");
    
    if (!GrouperInstallerUtils.equals(currentDocBase, shouldBeDocBase)) {
      System.out.println("Tried to edit server.xml but it didnt work, should have context of: '" 
          + shouldBeDocBase + "', but was: '" + currentDocBase + "'");
    }
    
  }

  /**
   * 
   * @return grouper ui build to name
   */
  private String grouperUiBuildToDirName() {
    return this.untarredUiDir.getAbsolutePath() + File.separator + "dist" + File.separator + "grouper";
  }

  /**
   * @param isInstallNotUpgrade
   */
  private void buildWs(boolean isInstallNotUpgrade) {
    
    boolean rebuildWs = true;

    boolean defaultRebuild = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.ws.rebuildIfBuilt", true, false);
    System.out.print("The Grouper WS has been built in the past, do you want it rebuilt? (t|f) [" 
        + (defaultRebuild ? "t" : "f") + "]: ");
    rebuildWs = readFromStdInBoolean(defaultRebuild);

    if (!rebuildWs) {
      return;
    }

    if (isInstallNotUpgrade) {
      //stop tomcat
      try {
        tomcatBounce("stop");
      } catch (Throwable e) {
        System.out.println("Couldnt stop tomcat, ignoring...");
      }
    }
    
    List<String> commands = new ArrayList<String>();
    
    addAntCommands(commands);
    commands.add("dist");
    
    System.out.println("\n##################################");
    System.out.println("Building WS with command:\n" + this.untarredWsDir.getAbsolutePath() + File.separator + "grouper-ws" + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, new File(this.untarredWsDir.getAbsolutePath() + File.separator + "grouper-ws"), null);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    if (isInstallNotUpgrade) {
      System.out.print("Do you want to set the log dir of WS (t|f)? [t]: ");
      boolean setLogDir = readFromStdInBoolean(true);
      
      if (setLogDir) {
        
        ////set the log dir
        //C:\apps\grouperInstallerTest\grouper.ws-2.0.2\grouper-ws\build\dist\grouper-ws\WEB-INF\classes\log4j.properties
        //
        //${grouper.home}logs
  
        String defaultLogDir = this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "grouperWs";
        System.out.print("Enter the WS log dir: [" + defaultLogDir + "]: ");
        String logDir = readFromStdIn();
        logDir = GrouperInstallerUtils.defaultIfBlank(logDir, defaultLogDir);
        
        //lets replace \\ with /
        logDir = GrouperInstallerUtils.replace(logDir, "\\\\", "/");
        //lets replace \ with /
        logDir = GrouperInstallerUtils.replace(logDir, "\\", "/");
  
        File log4jFile = new File(grouperWsBuildToDirName() + File.separator + "WEB-INF" + File.separator + "classes"
            + File.separator + "log4j.properties");
        
        System.out.println("Editing file: " + log4jFile.getAbsolutePath());
        
        editFile(log4jFile, "log4j\\.\\S+\\.File\\s*=\\s*([^\\s]+logs)/grouper_[^\\s]+\\.log", null, 
            null, logDir, "WS log directory");
        
        File logDirFile = new File(defaultLogDir);
        if (!logDirFile.exists()) {
          System.out.println("Creating log directory: " + logDirFile.getAbsolutePath());
          GrouperInstallerUtils.mkdirs(logDirFile);
        }
        //test log dir
        File testLogDirFile = new File(logDirFile.getAbsolutePath() + File.separator + "testFile" + GrouperInstallerUtils.uniqueId() + ".txt");
        GrouperInstallerUtils.saveStringIntoFile(testLogDirFile, "test");
        if (!testLogDirFile.delete()) {
          throw new RuntimeException("Cant delete file: " +  testLogDirFile.getAbsolutePath());
        }
        System.out.println("Created and deleted a test file successfully in dir: " + logDirFile.getAbsolutePath());
      }
    }
    
    System.out.println("\nEnd building Ws");
    System.out.println("##################################\n");
  
    
  }

  /**
   * 
   */
  private void configureTomcatWsWebapp() {
    
    File serverXmlFile = new File(this.untarredTomcatDir.getAbsolutePath() 
        + File.separator + "conf" + File.separator + "server.xml");
    
    //C:\mchyzer\grouper\trunk\grouper-installer\grouper.ui-2.0.2\dist\grouper
    //
    //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
    //Server
    //Service
    //Engine
    //Host
  
    System.out.print("Enter the URL path for the WS [grouper-ws]: ");
    this.tomcatWsPath = readFromStdIn();
    
    if (GrouperInstallerUtils.isBlank(this.tomcatWsPath)) {
      this.tomcatWsPath = "grouper-ws";
    }
  
    if (this.tomcatWsPath.endsWith("/") || this.tomcatWsPath.endsWith("\\")) {
      this.tomcatWsPath = this.tomcatWsPath.substring(0, this.tomcatWsPath.length()-1);
    }
    if (this.tomcatWsPath.startsWith("/") || this.tomcatWsPath.startsWith("\\")) {
      this.tomcatWsPath = this.tomcatWsPath.substring(1);
    }
    
    String currentDocBase = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "Server/Service/Engine/Host/Context[@path='/" + this.tomcatWsPath + "']", "docBase");
  
    //grouper.ws-2.0.2\grouper-ws\build\dist\grouper-ws
    
    String shouldBeDocBase = grouperWsBuildToDirName();
  
    System.out.println("Editing tomcat config file: " + serverXmlFile.getAbsolutePath());
    
    if (GrouperInstallerUtils.isBlank(currentDocBase)) {
  
      //need to add it
      //<Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true" xmlNamespaceAware="false" xmlValidation="false">
      //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
      addToXmlFile(serverXmlFile, ">", new String[]{"<Host "}, 
          "<Context docBase=\"" + shouldBeDocBase + "\" path=\"/" + this.tomcatWsPath + "\" reloadable=\"false\"/>", "tomcat context for WS");
  
    } else {
  
      if (!GrouperInstallerUtils.equals(currentDocBase, shouldBeDocBase)) {
        
        //lets edit the file
        //<Context docBase="C:\mchyzer\grouper\trunk\grouper-ws_trunk\webapp" path="/grouper-ws" reloadable="false"/>
        editFile(serverXmlFile, "docBase=\"([^\"]+)\"", new String[]{"<Context", "path=\"/" + this.tomcatWsPath + "\""}, 
            null, shouldBeDocBase, "tomcat context for WS");
  
      } else {
        
        System.out.println("  - Context is already set for Grouper WS");
        
      }
      
      
    }
    
    currentDocBase = GrouperInstallerUtils.xpathEvaluateAttribute(serverXmlFile, 
        "Server/Service/Engine/Host/Context[@path='/" + this.tomcatWsPath + "']", "docBase");
    
    if (!GrouperInstallerUtils.equals(currentDocBase, shouldBeDocBase)) {
      System.out.println("Tried to edit server.xml but it didnt work, should have context of: '" 
          + shouldBeDocBase + "', but was: '" + currentDocBase + "'");
    }
    
  }

  /**
   * @return grouper ws build to dir name
   */
  private String grouperWsBuildToDirName() {
    return this.untarredWsDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator 
      + "build" + File.separator + "dist" + File.separator + "grouper-ws";
  }

  /**
     * 
     */
    private void configureClient() {
      //properties file
      File localGrouperClientPropertiesFile = new File(this.untarredClientDir.getAbsolutePath() + File.separator 
          + "grouper.client.properties");
      
      //set the grouper property
      System.out.println("Editing " + localGrouperClientPropertiesFile.getAbsolutePath() + ": ");
      editPropertiesFile(localGrouperClientPropertiesFile, "grouperClient.webService.url", "http://localhost:" 
          + this.tomcatHttpPort + "/" + this.tomcatWsPath + "/servicesRest");
      editPropertiesFile(localGrouperClientPropertiesFile, "grouperClient.webService.login", "GrouperSystem");
      editPropertiesFile(localGrouperClientPropertiesFile, "grouperClient.webService.password", this.grouperSystemPassword);
      
      
//      grouperClient.webService.url = http://localhost:8200/grouper-ws/servicesRest
//
//      # kerberos principal used to connect to web service
//      grouperClient.webService.login = GrouperSystem
//
//      # password for shared secret authentication to web service
//      # or you can put a filename with an encrypted password
//      grouperClient.webService.password = myNewPass

      
    }

  /**
   * 
   * @return the file of the directory of WS
   */
  private File downloadClient() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";
  
    String clientFileName = "grouper.clientBinary-" + this.version + ".tar.gz";
    urlToDownload += this.version + "/" + clientFileName;
  
    File clientFile = new File(this.grouperTarballDirectoryString + clientFileName);
    
    downloadFile(urlToDownload, clientFile.getAbsolutePath());

    return clientFile;
  }

  /**
   * 
   */
  private void addGrouperSystemWsGroup() {

    //C:\mchyzer\grouper\trunk\grouper-installer\grouper.apiBinary-2.0.2\bin>gsh -runarg "grouperSession = GrouperSession.startRootSession();\nwsGroup = new GroupSave(grouperSession).assignName(\"etc:webServiceClientUsers\").assignCreateParentStemsIfNotExist(true).save();\nwsGroup.addMember(SubjectFinder.findRootSubject(), false);"

    //running with command on command line doenst work on linux since the args with whitespace translate to 
    //save the commands to a file, and runt he file
    StringBuilder gshCommands = new StringBuilder();
    gshCommands.append("grouperSession = GrouperSession.startRootSession();\n");
    gshCommands.append("wsGroup = new GroupSave(grouperSession).assignName(\"etc:webServiceClientUsers\").assignCreateParentStemsIfNotExist(true).save();\n");
    gshCommands.append("wsGroup.addMember(SubjectFinder.findRootSubject(), false);\n");
    
    File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshAddGrouperSystemWsGroup.gsh");
    GrouperInstallerUtils.saveStringIntoFile(gshFile, gshCommands.toString());
    
    List<String> commands = new ArrayList<String>();

    addGshCommands(commands);
    commands.add(gshFile.getAbsolutePath());

    System.out.println("\n##################################");
    System.out.println("Adding user GrouperSystem to grouper-ws users group with command:\n  " + convertCommandsIntoCommand(commands) + "\n");

    CommandResult commandResult = GrouperInstallerUtils.execCommand(
        GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
        this.untarredApiDir, null);

    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }


  }

  /**
   * 
   */
  private void runChangeLogTempToChangeLog() {

    boolean defaultBoolean = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.api.runChangeLogToChangeLogTemp", true, false);
    System.out.print("Is it ok to run a script that copies change log temp records to the change log (recommended) (t|f)? [" 
        + (defaultBoolean ? "t" : "f") + "]: ");
    boolean runScript = readFromStdInBoolean(defaultBoolean);

    
    if (!runScript) {
      return;
    }
    
    //running with command on command line doenst work on linux since the args with whitespace translate to 
    //save the commands to a file, and runt he file
    StringBuilder gshCommands = new StringBuilder();
    gshCommands.append("grouperSession = GrouperSession.startRootSession();\n");
    gshCommands.append("loaderRunOneJob(\"CHANGE_LOG_changeLogTempToChangeLog\");\n");
    
    File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshChangeLogTempToChangeLog.gsh");
    GrouperInstallerUtils.saveStringIntoFile(gshFile, gshCommands.toString());
    
    List<String> commands = new ArrayList<String>();

    addGshCommands(commands);
    commands.add(gshFile.getAbsolutePath());

    System.out.println("\n##################################");
    System.out.println("Copying records from change log temp to change log with command:\n  " + convertCommandsIntoCommand(commands) + "\n");

    CommandResult commandResult = GrouperInstallerUtils.execCommand(
        GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
       new File(this.gshCommand()).getParentFile(), null);

    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }


  }

  /**
   * 
   */
  private void runClientCommand() {
    System.out.println("##################################");
    System.out.println("Running client command:");
    System.out.println(this.untarredClientDir.getAbsolutePath() + "> " + GrouperInstallerUtils.javaCommand() 
        + " -jar grouperClient.jar --operation=getMembersWs --groupNames=etc:webServiceClientUsers");
    
    final List<String> command = new ArrayList<String>();

    command.add(GrouperInstallerUtils.javaCommand());
    command.add("-jar");
    command.addAll(GrouperInstallerUtils.splitTrimToList(
        "grouperClient.jar --operation=getMembersWs --groupNames=etc:webServiceClientUsers", " "));
            
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(command, String.class), true, true, null, this.untarredClientDir, null);

    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }
    System.out.println("Success running client command:");
    System.out.println("##################################");

  }

  /**
   * edit a property in a property file
   * @param file
   * @param valueRegex 
   * @param lineMustHaveRegexes 
   * @param lineCantHaveRegexes 
   * @param newValue 
   * @param description of change for sys out print
   * @return true if edited file, or false if not but didnt need to, null if not found
   */
  public static Boolean editFile(File file, String valueRegex, String[] lineMustHaveRegexes, 
      String[] lineCantHaveRegexes, String newValue, String description) {
    return editFile(file, valueRegex, lineMustHaveRegexes, lineCantHaveRegexes, newValue, description, false, null);
  }

  /**
   * edit a property in a property file
   * @param file
   * @param valueRegex 
   * @param lineMustHaveRegexes 
   * @param lineCantHaveRegexes 
   * @param newValue 
   * @param description of change for sys out print
   * @param addAttributeIfNotExists if attribute isnt there, then if true, then add the attribute
   * @param newAttributeName if adding new attribute, this is the name
   * @return true if edited file, or false if not but didnt need to, null if not found
   */
  public static Boolean editFile(File file, String valueRegex, String[] lineMustHaveRegexes, 
      String[] lineCantHaveRegexes, String newValue, String description, boolean addAttributeIfNotExists, String newAttributeName) {
    
    if (!GrouperInstallerUtils.isBlank(newAttributeName) != addAttributeIfNotExists) {
      throw new RuntimeException("newAttributeName cant be null if addAttributeIfNotExists, and must be null if not addAttributeIfNotExists");
    }
    
    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Why does " + file.getName() + " not exist and have contents? " 
          + file.getAbsolutePath());
    }
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    String newline = GrouperInstallerUtils.newlineFromFile(fileContents);
    
    String[] lines = GrouperInstallerUtils.splitLines(fileContents);
    
    Pattern pattern = Pattern.compile(valueRegex);
    
    Pattern[] lineMustHavePatterns = new Pattern[GrouperInstallerUtils.length(lineMustHaveRegexes)];
    
    {
      int index = 0;
      for (String lineMustHaveRegex : GrouperInstallerUtils.nonNull(lineMustHaveRegexes, String.class)) {
        Pattern lineMustHavePattern = Pattern.compile(lineMustHaveRegex);
        lineMustHavePatterns[index] = lineMustHavePattern;
        
        index++;
      }
    }    
  
    Pattern[] lineCantHavePatterns = new Pattern[GrouperInstallerUtils.length(lineCantHaveRegexes)];
    
    {
      int index = 0;
      for (String lineCantHaveRegex : GrouperInstallerUtils.nonNull(lineCantHaveRegexes, String.class)) {
        Pattern lineCantHavePattern = Pattern.compile(lineCantHaveRegex);
        lineCantHavePatterns[index] = lineCantHavePattern;
        
        index++;
      }
    }    
  
    StringBuilder newfile = new StringBuilder();
    
    boolean madeChange = false;
    boolean noChangeNeeded = false;
    
    OUTER: for (String line : lines) {
      line = GrouperInstallerUtils.defaultString(line);
      
      //lets see if it satisfies all
      for (Pattern lineMustHavePattern : lineMustHavePatterns) {
        if (!lineMustHavePattern.matcher(line).find()) {
          newfile.append(line).append(newline);
          continue OUTER;
        }
      }
      
      //lets see if it doesnt have these
      for (Pattern lineCantHavePattern : lineCantHavePatterns) {
        if (lineCantHavePattern.matcher(line).find()) {
          newfile.append(line).append(newline);
          continue OUTER;
        }
      }
      
      //see if satisfies current
      Matcher matcher = pattern.matcher(line);
      if (!matcher.find()) {
        
        if (addAttributeIfNotExists) {
          
          System.out.println(" - adding " + description + " with value: '" + newValue + "'");
          
          line = GrouperInstallerUtils.trimEnd(line);
          
          boolean endsWithCloseTag = false;
          boolean endElement = false;
          
          if (line.endsWith("/>")) {
            line = line.substring(0, line.length()-2);
            line = GrouperInstallerUtils.trimEnd(line);
            endsWithCloseTag = true;
          } else if (line.endsWith(">")) {
            line = line.substring(0, line.length()-1);
            line = GrouperInstallerUtils.trimEnd(line);
            endElement = true;
          }
          
          newfile.append(line).append(" ").append(newAttributeName).append("=\"").append(newValue).append("\"");
          
          if (endsWithCloseTag) {
            newfile.append(" />");
          } else if (endElement) {
            newfile.append(" >");
          }
          
          newfile.append(newline);
          madeChange = true;
          
        } else {
        
          newfile.append(line).append(newline);
        }
        continue;
      }
      
      String oldValue = matcher.group(1);
      if (GrouperInstallerUtils.equals(newValue, oldValue)) {
        System.out.println(" - old " + description + " value is same as new value: " + newValue);
        noChangeNeeded = true;
        newfile.append(line).append(newline);
        continue;
      }
      
      //we need to change the value
      System.out.println(" - changing " + description + " from: '" + oldValue + "' to: '" + newValue + "'");
      newfile.append(line.substring(0, matcher.start(1)));
      newfile.append(newValue);
      newfile.append(line.substring(matcher.end(1), line.length()));
      newfile.append(newline);
      madeChange = true;
      continue;
    }
    
    if (!madeChange) {
      //true if edited file, or false if not but didnt need to, null if not found
      if (noChangeNeeded) {
        return false;
      }
      return null;
    }
    
    GrouperInstallerUtils.writeStringToFile(file, newfile.toString());
    
    return true;
  }

  /**
   * add a line to a file.  will replace \n with whatever newline is
   * @param file
   * @param line (not ending in newline)
   * @param lineNumber 1 indexed.  If not exist, add to end of file
   * @param description is a description of what was just changed
   */
  public static void addToFile(File file, String line, int lineNumber, String description) {
    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Why does " + file.getName() + " not exist and have contents? " 
          + file.getAbsolutePath());
    }
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    String newline = GrouperInstallerUtils.newlineFromFile(fileContents);
    
    String[] lines = GrouperInstallerUtils.splitLines(fileContents);

    line = GrouperInstallerUtils.replace(line, "\n", newline);
    
    line += newline;
    
    StringBuilder newfile = new StringBuilder();
    
    boolean madeChange = false;
    
    int index = 0;
    
    for (String fileLine : lines) {
      fileLine = GrouperInstallerUtils.defaultString(fileLine);
      newfile.append(fileLine).append(newline);
      index++;
      
      if (index >= lineNumber  && !madeChange) {

        System.out.println("Adding " + description + " to file at line number: " + lineNumber);        
        
        newfile.append(line);
        madeChange = true;
      }
    }
    
    if (!madeChange) {
      System.out.println("Appending " + description + " to end of file");        
      newfile.append(line);
    }
    
    GrouperInstallerUtils.writeStringToFile(file, newfile.toString());
    
  }

  /** tomcat ui path */
  private String tomcatUiPath = null;

  /** tomcat ws path */
  private String tomcatWsPath = null;

  /** untarred dir, this does NOT end in file.separator */
  private File untarredClientDir;
  
  /**
   * 
   */
  private static void validateJavaVersion() {
    CommandResult commandResult = GrouperInstallerUtils.execCommand(
        GrouperInstallerUtils.javaCommand(), 
        new String[]{"-version"});
    String javaResult = commandResult.getOutputText();
    if (GrouperInstallerUtils.isBlank(javaResult)) {
      javaResult = commandResult.getErrorText();
    }
    if (!validJava(javaResult)) {
      throw new RuntimeException("Expecting Java 6+, but received: " + javaResult + ", run the installer jar with a jdk v6");
    }
    
    //try javac
    try {
      commandResult = GrouperInstallerUtils.execCommand(
          GrouperInstallerUtils.javaCommand() + "c", 
          new String[]{"-version"});
      javaResult = commandResult.getOutputText();
      if (GrouperInstallerUtils.isBlank(javaResult)) {
        javaResult = commandResult.getErrorText();
      }
      if (!validJava(javaResult)) {
        throw new RuntimeException("Expecting Java 6+, but received: " + javaResult + ", run the installer jar with a jdk v6");
      }
    } catch (Exception e) {
      throw new RuntimeException("This needs to be run from a jdk, but it is detected to be running from a JRE...  run the installed from a JDK!");
    }
  }

  /**
   * see if install or upgrade
   * @return true if install, or false if upgrade
   */
  private boolean grouperInstallOrUpgrade() {
    String input = null;
    String defaultAction = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.installOrUpgrade", false);
    defaultAction = GrouperInstallerUtils.defaultIfBlank(defaultAction, "install");
    for (int i=0;i<10;i++) {
      System.out.print("Do you want to 'install' a new installation of grouper, or 'upgrade' an existing installation\n(enter: 'install' or 'upgrade' or blank for the default) ["
          + defaultAction + "]: ");
      input = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(input)) {
        input = defaultAction;
      }
      if (GrouperInstallerUtils.equalsIgnoreCase("install", input)) {
        return true;
      }
      if (GrouperInstallerUtils.equalsIgnoreCase("upgrade", input)) {
        return false;
      }
      System.out.println("Please enter 'install' or 'upgrade' or blank for default (which is " + defaultAction + ")");
    }
    throw new RuntimeException("Expecting 'install' or 'upgrade' but was: '" + input + "'");
  }


  
  /**
   * 
   * @return install directory
   */
  private static String grouperInstallDirectory() {
    String grouperInstallDirectoryString;
    {
      File grouperInstallDirectoryFile = new File("");
      System.out.print("Enter in the Grouper install directory (note: better if no spaces or special chars) [" 
          + grouperInstallDirectoryFile.getAbsolutePath() + "]: ");
      String input = readFromStdIn();
      if (!GrouperInstallerUtils.isBlank(input)) {
        grouperInstallDirectoryFile = new File(input);
        if (!grouperInstallDirectoryFile.exists() || !grouperInstallDirectoryFile.isDirectory()) {
          System.out.println("Error: cant find directory: '" + input + "'");
          System.exit(1);
        }
      }
      grouperInstallDirectoryString = grouperInstallDirectoryFile.getAbsolutePath();
      if (!grouperInstallDirectoryString.endsWith(File.separator)) {
        grouperInstallDirectoryString += File.separator;
      }
    }
    return grouperInstallDirectoryString;
  }

  /**
   * 
   * @return upgrade directory
   */
  private static String grouperUpgradeTempDirectory() {
    String grouperInstallDirectoryString = null;
    {
      File grouperInstallDirectoryFile = new File("");
      String defaultDirectory = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.tarballDirectory", false);
      if (GrouperInstallerUtils.isBlank(defaultDirectory)) {
        defaultDirectory = grouperInstallDirectoryFile.getAbsolutePath();
      }
      System.out.print("Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [" 
          + defaultDirectory + "]: ");
      grouperInstallDirectoryString = readFromStdIn();
      if (!GrouperInstallerUtils.isBlank(grouperInstallDirectoryString)) {
        grouperInstallDirectoryFile = new File(grouperInstallDirectoryString);
        if (!grouperInstallDirectoryFile.exists() || !grouperInstallDirectoryFile.isDirectory()) {
          System.out.println("Error: cant find directory: '" + grouperInstallDirectoryFile.getAbsolutePath() + "'");
          System.exit(1);
        }
      } else {
        grouperInstallDirectoryString = defaultDirectory;
      }
      if (!grouperInstallDirectoryString.endsWith(File.separator)) {
        grouperInstallDirectoryString += File.separator;
      }
    }
    return grouperInstallDirectoryString;
  }

  /**
   * 
   * @return directory where existing installation exists
   */
  private String upgradeExistingDirectory() {

    //get the directory where the existing installation is
    String tempUpgradeExistingApplicationDirectoryString = this.upgradeExistingApplicationDirectoryString;

    String errorMessage = "Cant find Grouper " + this.appToUpgrade.name() + " properties files or libs, looked in the directory, "
        + "/classes/ , /conf/ , /WEB-INF/classes/ , /lib/ , /WEB-INF/lib/ , /lib/grouper/ , /dist/lib/ ";

    try {
      String upgradeExistingDirectoryString = null;
      for (int i=0;i<10;i++) {
        File grouperInstallDirectoryFile = new File("");
        String defaultDirectory = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.existingInstalledDirectory", false);
        System.out.print("Where is the grouper " + this.appToUpgrade.name() + " installed? " +
          (GrouperInstallerUtils.isBlank(defaultDirectory) ? "" : ("[" + defaultDirectory + "]: ")));
        upgradeExistingDirectoryString = readFromStdIn();
        if (!GrouperInstallerUtils.isBlank(upgradeExistingDirectoryString)) {
          grouperInstallDirectoryFile = new File(upgradeExistingDirectoryString);
          if (!grouperInstallDirectoryFile.exists() || !grouperInstallDirectoryFile.isDirectory()) {
            System.out.println("Error: cant find directory: '" + grouperInstallDirectoryFile.getAbsolutePath() + "'");
            continue;
          }
        } else {
          upgradeExistingDirectoryString = defaultDirectory;
        }
        upgradeExistingDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(upgradeExistingDirectoryString);

        this.upgradeExistingApplicationDirectoryString = upgradeExistingDirectoryString;

        //make sure directory is where the app is
        if (!this.appToUpgrade.validateExistingDirectory(this)) {
          System.out.println(errorMessage);
          continue;
        }
        //find the resources dir
        {
          File resourcesDirFile = new File(this.upgradeExistingApplicationDirectoryString + "classes" + File.separator);
          if (resourcesDirFile.exists()) {
            this.upgradeExistingClassesDirectoryString = resourcesDirFile.getAbsolutePath();
          } else {
            resourcesDirFile = new File(this.upgradeExistingApplicationDirectoryString + "conf" + File.separator);
            if (resourcesDirFile.exists()) {
              this.upgradeExistingClassesDirectoryString = resourcesDirFile.getAbsolutePath();
            } else {
              resourcesDirFile = new File(this.upgradeExistingApplicationDirectoryString + "WEB-INF" + File.separator + "classes" + File.separator);
              if (resourcesDirFile.exists()) {
                this.upgradeExistingClassesDirectoryString = resourcesDirFile.getAbsolutePath();
              } else {
                this.upgradeExistingClassesDirectoryString = this.upgradeExistingApplicationDirectoryString;
              }            
            }
          }
          this.upgradeExistingClassesDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.upgradeExistingClassesDirectoryString);
        }
        
        //find the lib dir
        {
          File libDirFile = new File(this.upgradeExistingApplicationDirectoryString + "lib" + File.separator + "grouper" + File.separator);
          if (libDirFile.exists()) {
            this.upgradeExistingLibDirectoryString = libDirFile.getAbsolutePath();
          } else {
            libDirFile = new File(this.upgradeExistingApplicationDirectoryString + "WEB-INF" + File.separator + "lib" + File.separator);
            if (libDirFile.exists()) {
              this.upgradeExistingLibDirectoryString = libDirFile.getAbsolutePath();
            } else {
              libDirFile = new File(this.upgradeExistingApplicationDirectoryString + "lib" + File.separator);
              if (libDirFile.exists()) {
                this.upgradeExistingLibDirectoryString = libDirFile.getAbsolutePath();
              } else {
                this.upgradeExistingLibDirectoryString = this.upgradeExistingApplicationDirectoryString;
              }            
            }
          }
          this.upgradeExistingLibDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.upgradeExistingLibDirectoryString);
        }        
                
        return upgradeExistingDirectoryString;
      }
      
      throw new RuntimeException(errorMessage);
      
    } finally {
      //set this back
      this.upgradeExistingApplicationDirectoryString = tempUpgradeExistingApplicationDirectoryString;
    }
  }

  /**
   * where classes are in the upgrade directory, ends in file separator
   */
  private String upgradeExistingClassesDirectoryString;
  
  /**
   * where jars are in the upgrade directory, ends in file separator
   */
  private String upgradeExistingLibDirectoryString;
  
  /**
   * 
   * @return what we are upgrading
   */
  private AppToUpgrade grouperAppToUpgrade() {

    String appToUpgradeString = null;
    String defaultAppToUpgrade = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.appToUpgrade", false);
    defaultAppToUpgrade = GrouperInstallerUtils.defaultIfBlank(defaultAppToUpgrade, AppToUpgrade.API.name().toLowerCase());
    
    for (int i=0;i<10;i++) {
      System.out.print("What do you want to upgrade?  api, ui, ws, client, or psp? [" + defaultAppToUpgrade + "]: ");
      appToUpgradeString = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(appToUpgradeString)) {
        appToUpgradeString = defaultAppToUpgrade;
      }
      try {
        return AppToUpgrade.valueOfIgnoreCase(appToUpgradeString, true);
      } catch (Exception e) {
        System.out.print("Error: please enter: 'api', 'ui', 'ws', 'client', 'psp', or blank for default [" + defaultAppToUpgrade + "]");
      }
    }
    throw new RuntimeException("Expecting api, ui, ws, client, or psp but was: '" + appToUpgradeString + "'");
  }

  /**
   * 
   * @param javaResult
   * @return if valid
   */
  private static boolean validJava(String javaResult) {
    //C:\mchyzer\grouper\trunk\grouper\bin>java -version
    //java version "1.6.0_21"
    //Java(TM) SE Runtime Environment (build 1.6.0_21-b07)
    //Java HotSpot(TM) Client VM (build 17.0-b17, mixed mode, sharing)
    Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.\\d+_?\\d*");
    Matcher matcher = pattern.matcher(javaResult);
    if (matcher.find()) {
      int majorVersion = GrouperInstallerUtils.intValue(matcher.group(1));
      int minorVersion = GrouperInstallerUtils.intValue(matcher.group(2));
      if (majorVersion == 1 && minorVersion >= 6) {
        return true;
      }
      return majorVersion >= 6;
    }
    return false;
  }
  
  /**
   * add something to an xml file
   * @param file
   * @param addAfterThisRegex 
   * @param mustPassTheseRegexes 
   * @param newValue 
   * @param description of change for sys out print
   */
  public static void addToXmlFile(File file, String addAfterThisRegex, String[] mustPassTheseRegexes, String newValue, String description) {
    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Why does " + file.getName() + " not exist and have contents? " 
          + file.getAbsolutePath());
    }
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    String newline = GrouperInstallerUtils.newlineFromFile(fileContents);
    
    Pattern pattern = Pattern.compile(addAfterThisRegex);

    String[] lines = GrouperInstallerUtils.splitLines(fileContents);
    
    Pattern[] lineMustPassThesePatterns = new Pattern[GrouperInstallerUtils.length(mustPassTheseRegexes)];
    
    boolean[] hasPassedTheseRegexes = new boolean[lineMustPassThesePatterns.length];
    
    for (int i=0;i<hasPassedTheseRegexes.length;i++) {
      hasPassedTheseRegexes[i] = false;
    }
    
    {
      int index = 0;
      for (String lineMustHaveRegex : GrouperInstallerUtils.nonNull(mustPassTheseRegexes, String.class)) {
        Pattern lineMustHavePattern = Pattern.compile(lineMustHaveRegex);
        lineMustPassThesePatterns[index] = lineMustHavePattern;
        
        index++;
      }
    }    

    StringBuilder newfile = new StringBuilder();

    boolean madeChange = false;

    OUTER: for (String line : lines) {
      line = GrouperInstallerUtils.defaultString(line);
      
      //lets see if it satisfies all
      for (int i=0;i<lineMustPassThesePatterns.length;i++) {
        Pattern lineMustHavePattern = lineMustPassThesePatterns[i];
        if (lineMustHavePattern.matcher(line).find()) {
          hasPassedTheseRegexes[i] = true;
        }
      }
      
      //see if we have passed all the prefixes
      for (int i=0;i<hasPassedTheseRegexes.length;i++) {
        if (!hasPassedTheseRegexes[i]) {
          newfile.append(line).append(newline);
          continue OUTER;
        }
      }
      
      //see if satisfies current, and only add once
      Matcher matcher = pattern.matcher(line);
      if (!matcher.find() || madeChange) {
        newfile.append(line).append(newline);
        continue;
      }

      //we need to change the value
      System.out.println(" - adding " + description + " line: '" + newValue + "'");
      newfile.append(line);
      newfile.append(newline);
      newfile.append(newValue);
      newfile.append(newline);
      madeChange = true;
    }
    if (!madeChange) {
      throw new RuntimeException("Couldnt find place to add to server.xml!  Are there newlines that werent there before or something?");
    }
    
    GrouperInstallerUtils.writeStringToFile(file, newfile.toString());
    
  }

  
  /**
   * edit a property in a property file
   * @param file
   * @param propertyName
   * @param propertyValue
   */
  public static void editPropertiesFile(File file, String propertyName, String propertyValue) {
    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Why does " + file.getName() + " not exist and have contents? " 
          + file.getAbsolutePath());
    }
    
    propertyValue = GrouperInstallerUtils.defaultString(propertyValue);
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    String newline = GrouperInstallerUtils.newlineFromFile(fileContents);
    
    //if it starts with it, add a newline to start so the regexes work
    if (!fileContents.startsWith(newline)) {
      fileContents = newline + fileContents;
    }
    
    //lets look for property in file
    //this is a newline or form feed then some optional whitespace, and the property name
    //then some optional whitespace then an equals, then optional whitespace, then some text
    String propertyNameRegex = GrouperInstallerUtils.replace(propertyName, ".", "\\.");
    String regex = "[\\n\\r][ \\t]*" + propertyNameRegex + "[ \\t]*=[ \\t]*([^\\n\\r]*)";
    Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(fileContents);

    if (matcher.find()) {
      String previousValue = matcher.group(1);
      
      if (GrouperInstallerUtils.trimToEmpty(previousValue).equals(GrouperInstallerUtils.trim(propertyValue))) {
        System.out.println(" - property " + propertyName + " already was set to: " + propertyValue + ", not changing file");
        return;
      }
      
      int startIndex = matcher.start(1);
      
      int endIndex = matcher.end(1);
      
      String newContents = fileContents.substring(0, startIndex) + propertyValue;
      
      //if not the last char
      if (endIndex < fileContents.length()-1) {
        newContents += fileContents.substring(endIndex, fileContents.length());
      }

      //if there is another match, there is a problem
      if (matcher.find()) {
        throw new RuntimeException("Why are there multiple matches for " + propertyName + " in propertyFile: " + file.getAbsolutePath() + "??????");
      }

      System.out.println(" - set property: " 
          + propertyName + " from: " + previousValue + " to: " + propertyValue);
      GrouperInstallerUtils.writeStringToFile(file, newContents);
      return;
    }
    
    //lets see if it is in a comment
    //this is a newline or form feed then some optional whitespace, hash, optional whitespace, and the property name
    //then some optional whitespace then an equals, then optional whitespace, then some text
    regex = ".*[\\n\\r]([ \\t]*#[ \\t]*)" + propertyNameRegex + "[ \\t]*=[ \\t]*([^\\n\\r]*).*";
    pattern = Pattern.compile(regex, Pattern.DOTALL);
    matcher = pattern.matcher(fileContents);

    if (matcher.matches()) {
      String previousValue = matcher.group(2);
      
      int startIndexHash = matcher.start(1);
      
      int endIndexHash = matcher.end(1);

      int startIndex = matcher.start(2);
      
      int endIndex = matcher.end(2);
      
      String newContents = fileContents.substring(0, startIndexHash) + fileContents.substring(endIndexHash, startIndex)
        + propertyValue;
      
      //if not the last char
      if (endIndex < fileContents.length()-1) {
        newContents += fileContents.substring(endIndex, fileContents.length());
      }
      System.out.println(" - uncommented property: " 
          + propertyName + " from: " + previousValue + " to: " + propertyValue);
      GrouperInstallerUtils.writeStringToFile(file, newContents);
      
      return;
    }
    
    //it must have not existed
    //add a newline..
    //add to end in case it was already there, now it will be overwritten
    String newContents = fileContents + newline + "# added by grouper-installer" + newline + propertyName + " = " + propertyValue + newline;
    GrouperInstallerUtils.writeStringToFile(file, newContents);

    System.out.println(" - added to end of property file: " + propertyName + " = " + propertyValue);
    
  }

  /**
   * untar a file to a dir
   * @param fileName
   * @return the directory where the files are (assuming has a single dir the same name as the archive)
   */
  private static File untar(String fileName) {
    if (!fileName.endsWith(".tar")) {
      throw new RuntimeException("File doesnt end in .tar: " + fileName);
    }
    String untarredFileName = fileName.substring(0, fileName.length() - ".tar".length());
    
    //ant has -bin which is annoying
    if (untarredFileName.endsWith("-bin")) {
      untarredFileName = untarredFileName.substring(0, untarredFileName.length() - "-bin".length());
    }
    
    File untarredFile = new File(untarredFileName);
    if (untarredFile.exists()) {
      
      System.out.print("Untarred dir exists: " + untarredFileName + ", use untarred dir (t|f)? [t]: ");
      boolean useUnzippedFile = readFromStdInBoolean(true);
      if (useUnzippedFile) {
        return untarredFile;
      }
      
      System.out.println("Deleting: " + untarredFileName);
      GrouperInstallerUtils.deleteRecursiveDirectory(untarredFileName);
    }
    
    System.out.println("Expanding: " + fileName);
    
    String unzippedParent = untarredFile.getParentFile().getAbsolutePath();
    if (unzippedParent.endsWith(File.separator)) {
      unzippedParent = unzippedParent.substring(0, unzippedParent.length()-1);
    }
    
    TarArchiveInputStream tarArchiveInputStream = null;
    
    try {
      tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(fileName));
      
      while (true) {
  
        TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
        if (tarArchiveEntry == null) {
          break;
        }
        
        //System.out.println("Entry: " + tarArchiveEntry.getName()
        //    + ", isDirectory: " + tarArchiveEntry.isDirectory()
        //    + ", isFile: " + tarArchiveEntry.isFile());
        String fileEntryName = unzippedParent + File.separator + tarArchiveEntry.getName();
        File tarEntryFile = new File(fileEntryName);
        
        if (tarArchiveEntry.isDirectory()) {
          if (!tarEntryFile.exists() && !tarEntryFile.mkdirs()) {
            throw new RuntimeException("Cant create dirs: " + tarEntryFile.getAbsolutePath());
          }
          continue;
        }
        
        byte[] content = new byte[(int)tarArchiveEntry.getSize()];

        int size = tarArchiveInputStream.read(content, 0, content.length);
        
        //for some reason we get an error when 0 bytes...
        if (size != content.length && (!(size == -1 && content.length == 0))) {
          throw new RuntimeException("Didnt read the right amount of bytes: " + size 
              + ", should have been: " + content.length + " on entry: " + tarArchiveEntry.getName());
        }
        
        ByteArrayInputStream byteArrayInputStream = null;
        FileOutputStream fileOutputStream = null;
        
        try {
          
          //create parent directories
          if (!tarEntryFile.getParentFile().exists() && !tarEntryFile.getParentFile().mkdirs()) {
            throw new RuntimeException("Cant create dirs: " + tarEntryFile.getParentFile().getAbsolutePath());
          }

          fileOutputStream = new FileOutputStream(tarEntryFile);
          byteArrayInputStream = new ByteArrayInputStream(content);
          GrouperInstallerUtils.copy(byteArrayInputStream, fileOutputStream);
          
        } catch (Exception e) {
          throw new RuntimeException("Probem with entry: " + tarArchiveEntry.getName(), e);
        } finally {
          GrouperInstallerUtils.closeQuietly(byteArrayInputStream);
          GrouperInstallerUtils.closeQuietly(fileOutputStream);
        }
        
      }
    } catch (Exception e) {
      throw new RuntimeException("Error untarring: " + fileName, e);
    } finally {
      GrouperInstallerUtils.closeQuietly(tarArchiveInputStream);
    }
    return untarredFile;
  }
  
  /**
   * unzip a file to another file
   * @param fileName
   * @return the unzipped file
   */
  private static File unzip(String fileName) {
    if (!fileName.endsWith(".gz")) {
      throw new RuntimeException("File doesnt end in .gz: " + fileName);
    }
    String unzippedFileName = fileName.substring(0, fileName.length() - ".gz".length());
    
    File unzippedFile = new File(unzippedFileName);
    if (unzippedFile.exists()) {
      
      System.out.print("Unzipped file exists: " + unzippedFileName + ", use unzipped file (t|f)? [t]: ");
      boolean useUnzippedFile = readFromStdInBoolean(true);
      if (useUnzippedFile) {
        return unzippedFile;
      }
      System.out.println("Deleting: " + unzippedFileName);
      if (!unzippedFile.delete()) {
        throw new RuntimeException("Cant delete file: " + unzippedFileName);
      }
    }

    System.out.println("Unzipping: " + fileName);
    
    GzipCompressorInputStream gzipCompressorInputStream = null;
    FileOutputStream fileOutputStream = null;
    try {
      gzipCompressorInputStream = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))));
      fileOutputStream = new FileOutputStream(unzippedFile);
      GrouperInstallerUtils.copy(gzipCompressorInputStream, fileOutputStream);
    } catch (Exception e) {
      throw new RuntimeException("Cant unzip file: " + fileName, e);
    } finally {
      GrouperInstallerUtils.closeQuietly(gzipCompressorInputStream);
      GrouperInstallerUtils.closeQuietly(fileOutputStream);
    }
    return unzippedFile;
  }

  /**
   * 
   * @return the file of the directory of the psp
   */
  private File downloadPsp() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";

    String pspFileName = "grouper.psp-" + this.version + ".tar.gz";
    urlToDownload += this.version + "/" + pspFileName;

    File pspFile = new File(this.grouperTarballDirectoryString + pspFileName);
    
    downloadFile(urlToDownload, pspFile.getAbsolutePath());

    return pspFile;
  }
  
  /**
   * upgrade the ws
   */
  private void upgradeWs() {
  
    this.upgradeApi();
    
    System.out.println("\n##################################");
    System.out.println("Upgrading WS\n");
    
    //copy the jars there
    System.out.println("\n##################################");
    System.out.println("Upgrading WS jars\n");
  
    this.upgradeJars(new File(this.untarredWsDir + File.separator + 
        "grouper-ws" + File.separator + "build" + File.separator + "dist" + File.separator + "grouper-ws"
        + File.separator + "WEB-INF" + File.separator + "lib" + File.separator));
  
    System.out.println("\n##################################");
    System.out.println("Upgrading WS files\n");
  
    //copy files there
    this.copyFiles(this.untarredWsDir + File.separator + 
        "grouper-ws" + File.separator + "build" + File.separator + "dist" + File.separator + "grouper-ws"
        + File.separator,
        this.upgradeExistingApplicationDirectoryString,
        GrouperInstallerUtils.toSet("WEB-INF/lib", "WEB-INF/web.xml", "WEB-INF/classes",
            "WEB-INF/bin/gsh", "WEB-INF/bin/gsh.bat", "WEB-INF/bin/gsh.sh"));

    {
      boolean hadChange = false;
      for (String gshName : new String[]{"gsh", "gsh.bat", "gsh.sh"}) {
        File newGshFile = new File(this.untarredWsDir + File.separator + "grouper-ws" + File.separator 
            + "build" + File.separator + "dist" + File.separator + "grouper-ws"
            + File.separator + "WEB-INF" + File.separator + "bin" 
            + File.separator + gshName);
  
        File existingGshFile = new File(this.upgradeExistingApplicationDirectoryString 
            + File.separator + "WEB-INF" + File.separator + "bin" + File.separator + gshName);
  
        if (!GrouperInstallerUtils.contentEquals(newGshFile, existingGshFile)) {
          this.backupAndCopyFile(newGshFile, existingGshFile, true);
          if (!GrouperInstallerUtils.equals("gsh.bat", gshName)) {
            hadChange = true;
          }
        }
        
      }
      if (hadChange) {
        //set executable and dos2unix
        gshExcutableAndDos2Unix(this.upgradeExistingApplicationDirectoryString + "WEB-INF" 
            + File.separator + "bin" 
            + File.separator);
      }
    }
    
    upgradeWebXml(new File(this.untarredWsDir + File.separator + "grouper-ws" 
        + File.separator + "build" + File.separator + "dist" + File.separator + "grouper-ws"
        + File.separator + "WEB-INF" + File.separator + "web.xml"),
            new File(this.upgradeExistingApplicationDirectoryString 
                + File.separator + "WEB-INF" + File.separator + "web.xml"));
    
    System.out.println("\n##################################");
    System.out.println("Upgrading WS config files\n");

    this.compareUpgradePropertiesFile(this.grouperWsBasePropertiesFile, 
        new File(this.untarredWsDir + File.separator + 
            "grouper-ws" + File.separator + "build" + File.separator + "dist" + File.separator + "grouper-ws"
            + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "grouper-ws.base.properties"),
        this.grouperWsPropertiesFile,
        this.grouperWsExamplePropertiesFile, null
      );


  }
  /**
   * edit an xml file attribute in a xml file
   * @param file
   * @param elementName
   * @param elementMustHaveAttributeAndValue
   * @param newValue
   * @param description of change for sys out print
   * @param newAttributeName if adding new attribute, this is the name
   * @return true if edited file, or false if not but didnt need to, null if not found
   */
  public static Boolean editXmlFileAttribute(File file, String elementName, Map<String, String> elementMustHaveAttributeAndValue, 
      String newAttributeName, String newValue, String description) {

    if (!file.exists() || file.length() == 0) {
      throw new RuntimeException("Why does " + file.getName() + " not exist and have contents? " 
          + file.getAbsolutePath());
    }
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    boolean inComment = false;
    
    //lets parse the file and get to the element
    OUTER: for (int i=0;i<fileContents.length();i++) {
      
      //look for start element
      char curChar = fileContents.charAt(i);

      Character nextChar = (i+1) < fileContents.length() ? fileContents.charAt(i+1) : null;
      Character nextNextChar = (i+2) < fileContents.length() ? fileContents.charAt(i+2) : null;
      Character nextNextNextChar = (i+3) < fileContents.length() ? fileContents.charAt(i+3) : null;
      
      //if we are in comment, see when we are out of comment
      if (inComment) {
        if (curChar == '-' && nextChar != null && nextChar == '-' && nextNextChar != null && nextNextChar == '>') {
          inComment = false;
        }
        continue;
        
      }

      //look for a tag or comment
      if (curChar != '<') {
        continue;
      }
      
      //see if this is a comment
      if (nextChar != null && nextChar == '!' && nextNextChar != null && nextNextChar == '-' && nextNextNextChar != null && nextNextNextChar == '-') {
        inComment = true;
        continue;
      }

      //get tagName
      String currentElementName = _internalXmlTagName(fileContents, i+1);
      
      //not the right tag
      if (!GrouperInstallerUtils.equals(currentElementName, elementName)) {
        continue;
      }
      
      int tagNameStart = fileContents.indexOf(currentElementName, i+1);
      
      //get the attributes
      int tagAttributesStart = tagNameStart + currentElementName.length();
      XmlParseAttributesResult xmlParseAttributesResult = _internalXmlParseAttributes(fileContents, tagAttributesStart);
      Map<String, String> currentAttributes = xmlParseAttributesResult.getAttributes();
      
      if (GrouperInstallerUtils.length(elementMustHaveAttributeAndValue) > 0) {
        for (String attributeName : elementMustHaveAttributeAndValue.keySet()) {
          String expectedValue = elementMustHaveAttributeAndValue.get(attributeName);
          String hasValue = currentAttributes.get(attributeName);

          //if we dont have that value, then keep going
          if (!GrouperInstallerUtils.equals(expectedValue, hasValue)) {
            continue OUTER;
          }
        }
      }
      
      //we have the tag and it has the expected attributes

      //see if the attribute is even there...
      if (!currentAttributes.containsKey(newAttributeName)) {
        System.out.println(" - adding " + description + " with value: '" + newValue + "'");
        String newFileContents = fileContents.substring(0, tagAttributesStart) + " " + newAttributeName + "=\"" + newValue + 
            "\" " + fileContents.substring(tagAttributesStart, fileContents.length());
        GrouperInstallerUtils.writeStringToFile(file, newFileContents);
        return true;
      }

      //does it already have the value?
      String currentValue = currentAttributes.get(newAttributeName);
      
      //value is already there
      if (GrouperInstallerUtils.equals(currentValue, newValue)) {
        return false;
      }

      //it has the wrong value
      int startQuote = xmlParseAttributesResult.getAttributeStartIndex().get(newAttributeName);
      int endQuote = xmlParseAttributesResult.getAttributeEndIndex().get(newAttributeName);

      System.out.println(" - changing " + description + " from old value: '" + currentValue 
          + "' to new value: '" + newValue + "'");

      String newFileContents = fileContents.substring(0, startQuote+1)  + newValue + 
          fileContents.substring(endQuote, fileContents.length());
      GrouperInstallerUtils.writeStringToFile(file, newFileContents);
      return true;

    }

    return null;

  }

  /**
   * 
   * @param fileContents
   * @param tagIndexStart
   * @return the tag name
   */
  private static String _internalXmlTagName(String fileContents, int tagIndexStart) {
    StringBuilder tagName = new StringBuilder();
    for (int i=tagIndexStart; i<fileContents.length(); i++) {
      char curChar = fileContents.charAt(i);
      if (tagName.length() == 0 && Character.isWhitespace(curChar)) {
        continue;
      }
      if (Character.isWhitespace(curChar) || '/' == curChar || '>' == curChar) {
        return tagName.toString();
      }
      tagName.append(curChar);
    }
    throw new RuntimeException("How did I get here???? '" + tagName.toString() + "'");
  }
  
  /**
   * xml parse attribute result
   */
  private static class XmlParseAttributesResult {

    /**
     * attributes name to value
     */
    private Map<String, String> attributes;
    
    /**
     * attribute name to startIndex (of quote)
     */
    private Map<String, Integer> attributeStartIndex;

    /**
     * attribute name to endIndex (of quote)
     */
    private Map<String, Integer> attributeEndIndex;

    
    /**
     * attributes name to value
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
      return this.attributes;
    }

    
    /**
     * attributes name to value
     * @param attributes1 the attributes to set
     */
    public void setAttributes(Map<String, String> attributes1) {
      this.attributes = attributes1;
    }

    
    /**
     * attribute name to startIndex (of quote)
     * @return the attributeStartIndex
     */
    public Map<String, Integer> getAttributeStartIndex() {
      return this.attributeStartIndex;
    }
    
    /**
     * attribute name to startIndex (of quote)
     * @param attributeStartIndex1 the attributeStartIndex to set
     */
    public void setAttributeStartIndex(Map<String, Integer> attributeStartIndex1) {
      this.attributeStartIndex = attributeStartIndex1;
    }
    
    /**
     * attribute name to endIndex (of quote)
     * @return the attributeEndIndex
     */
    public Map<String, Integer> getAttributeEndIndex() {
      return this.attributeEndIndex;
    }
    
    /**
     * attribute name to endIndex (of quote)
     * @param attributeEndIndex1 the attributeEndIndex to set
     */
    public void setAttributeEndIndex(Map<String, Integer> attributeEndIndex1) {
      this.attributeEndIndex = attributeEndIndex1;
    }
    
  }
  
  /**
   * parse attributes
   * @param fileContents
   * @param tagAttributesStart is the index where the attributes start
   * @return the map of attribute names and values
   */
  private static XmlParseAttributesResult _internalXmlParseAttributes(String fileContents, int tagAttributesStart) {

    XmlParseAttributesResult xmlParseAttributesResult = new XmlParseAttributesResult();

    Map<String, String> attributes = new LinkedHashMap<String, String>();
    Map<String, Integer> attributeStartIndex = new LinkedHashMap<String, Integer>();
    Map<String, Integer> attributeEndIndex = new LinkedHashMap<String, Integer>();

    xmlParseAttributesResult.setAttributes(attributes);
    xmlParseAttributesResult.setAttributeStartIndex(attributeStartIndex);
    xmlParseAttributesResult.setAttributeEndIndex(attributeEndIndex);
    
    boolean inAttributeStartValue = false;
    boolean inAttributeStartName = true;
    boolean inAttributeName = false;
    boolean inAttributeValue = false;
    
    StringBuilder attributeName = null;
    StringBuilder attributeValue = null;
    
    for (int i=tagAttributesStart; i<fileContents.length(); i++) {
      char curChar = fileContents.charAt(i);
      boolean isWhitespace = Character.isWhitespace(curChar);

      //waiting for the attribute
      if ((inAttributeStartValue || inAttributeStartName) && isWhitespace) {
        continue;
      }

      //if waiting for value and equals, keep looking
      if (inAttributeStartValue && curChar == '=') {
        continue;
      }

      //waiting to start an attribute name, its not whitespace so do it
      if (inAttributeStartName) {
        
        //we done if we got to this character
        if (curChar == '/' || curChar == '>') {
          return xmlParseAttributesResult;
        }
        
        inAttributeStartName = false;
        inAttributeName = true;
        attributeName = new StringBuilder();
      }

      //if in an attribute name and whitespace or equals, then we are done
      if (inAttributeName && (isWhitespace || curChar == '=' )) {
        inAttributeName = false;
        inAttributeStartValue = true;
        continue;
      }

      //getting the attribute name
      if (inAttributeName) {
        attributeName.append(curChar);
        continue;
      }

      //if waiting for start value and found quote
      if (inAttributeStartValue && curChar == '"') {
        inAttributeStartValue = false;
        inAttributeValue = true;
        attributeValue = new StringBuilder();
        attributeStartIndex.put(attributeName.toString(), i);
        continue;
      }

      //if in attribute value and not quote, append the char
      if (inAttributeValue && curChar != '"') {
        attributeValue.append(curChar);
        continue;
      }

      //done with attribute value
      if (inAttributeValue && curChar == '"') {
        inAttributeValue = false;
        inAttributeStartName = true;
        if (attributes.containsKey(attributeName.toString())) {
          throw new RuntimeException("Duplicate attribute: " + attributeName.toString());
        }
        attributes.put(attributeName.toString(), attributeValue.toString());
        attributeEndIndex.put(attributeName.toString(), i);
        continue;
      }

      throw new RuntimeException("Why are we here? " + i + ", " + fileContents);
    }
    return xmlParseAttributesResult;
  }
  
}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

import edu.internet2.middleware.grouperInstaller.GrouperInstallerIndexFile.PatchFileType;
import edu.internet2.middleware.grouperInstaller.util.GiDbUtils;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils;
import edu.internet2.middleware.grouperInstaller.util.GrouperInstallerUtils.CommandResult;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import edu.internet2.middleware.grouperInstallerExt.org.apache.commons.compress.utils.IOUtils;
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
   * @param autorunPropertiesKey key in properties file to automatically fill in a value
   */
  private static boolean shouldContinue(String autorunPropertiesKey) {
    return shouldContinue(null, null, autorunPropertiesKey);
  }
  
  /**
   * if we should continue or not
   * @param hint 
   * @param exitHint 
   * @param autorunPropertiesKey key in properties file to automatically fill in a value
   * @return if should continue
   */
  private static boolean shouldContinue(String hint, String exitHint, String autorunPropertiesKey) {
    if (hint == null) {
      hint = "Do you want to continue ";
    }
    if (!hint.endsWith(" ")) {
      hint += " ";
    }
    System.out.print(hint + "(t|f)? [f] ");
    boolean shouldContinue = readFromStdInBoolean(false, autorunPropertiesKey);
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
   * @param autorunPropertiesKey key in properties file to automatically fill in a value
   * @return the string
   */
  private static boolean readFromStdInBoolean(Boolean defaultBoolean, String autorunPropertiesKey) {
    int i=100;
    //keep trying until we get it
    while(true) {
      String input = readFromStdIn(autorunPropertiesKey);
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
      if (i-- < 0) {
        throw new RuntimeException("Too many tries for finding a boolean!");
      }
    }
  }
  
  /**
   * read a string from stdin
   * @param autorunPropertiesKey key in properties file to automatically fill in a value
   * @return the string
   */
  private static String readFromStdIn(String autorunPropertiesKey) {
    
    String str = null;

    if (GrouperInstallerUtils.propertiesContainsKey(autorunPropertiesKey)) {

      str = GrouperInstallerUtils.propertiesValue(autorunPropertiesKey, false);
      
      System.out.println("<using autorun property " + autorunPropertiesKey + ">: '" + str + "'");
      
    } else if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.autorun.useDefaultsAsMuchAsAvailable", false, false)) {
      
      System.out.println("<using default which is blank due to grouperInstaller.autorun.useDefaultsAsMuchAsAvailable>: ");
      
    } else {

      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        str = in.readLine();
      } catch (Exception e) {
        throw new RuntimeException("Problem", e);
      }

    }
    
    return GrouperInstallerUtils.trim(str);
    
  }

  /**
   * download a file, delete the local file if it exists
   * @param url
   * @param localFileName
   * @param autorunUseLocalFilePropertiesKey key in properties file to automatically fill in a value if download exists, default true
   */
  private static void downloadFile(String url, String localFileName, String autorunUseLocalFilePropertiesKey) {
    downloadFile(url, localFileName, false, null, autorunUseLocalFilePropertiesKey);
  }

  /**
   * download a file, delete the local file if it exists
   * @param url
   * @param localFileName
   * @param allow404
   * @param prefixFor404 print message prefix if 404
   * @param autorunUseLocalFilePropertiesKey key in properties file to automatically fill in a value if download exists, default true
   * @return true if downloaded, false if not
   */
  private static boolean downloadFile(final String url, final String localFileName, final boolean allow404, 
      final String prefixFor404, final String autorunUseLocalFilePropertiesKey) {

    boolean useLocalFile = false;

    final File localFile = new File(localFileName);

    if (localFile.exists()) {
      System.out.print("File exists: " + localFile.getAbsolutePath() + ", should we use the local file (t|f)? [t]: ");
      useLocalFile = readFromStdInBoolean(true, autorunUseLocalFilePropertiesKey);
    }
    
    if (useLocalFile) {
      return true;
    }

    if (allow404 && GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.useLocalFilesOnlyForDevelopment", false, false)) {
      return false;
    }

    final boolean[] result = new boolean[1];
    
    Runnable runnable = new Runnable() {
      
      public void run() {
        result[0] = downloadFileHelper(url, localFileName, allow404, prefixFor404, autorunUseLocalFilePropertiesKey);
      }
    };
    
    GrouperInstallerUtils.threadRunWithStatusDots(runnable, true);
    
    return result[0];
  }
  
  /**
   * download a file, delete the local file if it exists
   * @param url
   * @param localFileName
   * @param allow404
   * @param prefixFor404 print message prefix if 404
   * @param autorunUseLocalFilePropertiesKey key in properties file to automatically fill in a value if download exists, default true
   * @return true if downloaded, false if not
   */
  private static boolean downloadFileHelper(String url, String localFileName, boolean allow404, 
      String prefixFor404, String autorunUseLocalFilePropertiesKey) {

    final File localFile = new File(localFileName);

    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url);
    try {
      int result = httpClient.executeMethod(getMethod);
      
      if (allow404 && result == 404) {
        if (GrouperInstallerUtils.isBlank(prefixFor404)) {
          prefixFor404 = "File not found: ";
        }
        System.out.println(prefixFor404 + url);
        return false;
      }
      
      System.out.println("Downloading from URL: " + url + " to file: " + localFileName);

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

      return true;

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
    grouperInstaller.mainLogic();

//    GrouperInstaller.downloadFile("https://github.com/Internet2/grouper/archive/GROUPER_2_2_BRANCH.zip",
//        "C:\\app\\grouperInstallerTarballDir\\GROUPER_2_2_BRANCH.zip", false, null, 
//        "grouperInstaller.autorun.createPatchDownloadSourceUseLocalIfExist", true
//        );

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
   * @param isInstallNotUpgrade install will bounce tomcat, configure, etc
   * 
   */
  private void buildUi(boolean isInstallNotUpgrade) {
    
    File grouperUiBuildToDir = new File(this.grouperUiBuildToDirName());
    
    boolean rebuildUi = true;
    
    if (grouperUiBuildToDir.exists()) {
      boolean defaultRebuild = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.ui.rebuildIfBuilt", true, false);
      System.out.print("The Grouper UI has been built in the past, do you want it rebuilt? (t|f) [" 
          + (defaultRebuild ? "t" : "f") + "]: ");
      rebuildUi = readFromStdInBoolean(defaultRebuild, "grouperInstaller.autorun.rebuildUiAfterHavingBeenBuilt");
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
        true, true, null, this.untarredUiDir, null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }
    
    if (isInstallNotUpgrade) {
      System.out.print("Do you want to set the log dir of UI (t|f)? [t]: ");
      boolean setLogDir = readFromStdInBoolean(true, "grouperInstaller.autorun.setLogDirOfUi");
      
      if (setLogDir) {
        
        ////set the log dir
        //C:\apps\grouperInstallerTest\grouper.ws-2.0.2\grouper-ws\build\dist\grouper-ws\WEB-INF\classes\log4j.properties
        //
        //${grouper.home}logs
  
        String defaultLogDir = this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "grouperUi";
        System.out.print("Enter the UI log dir: [" + defaultLogDir + "]: ");
        String logDir = readFromStdIn("grouperInstaller.autorun.uiLogDir");
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
            new String[]{"-version"}, true);
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
    this.shCommand = readFromStdIn("grouperInstaller.autorun.pathOfShCommandIfNotFound");

    try {
      CommandResult commandResult = GrouperInstallerUtils.execCommand(
          this.shCommand, 
          new String[]{"-version"}, true);
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

  /**
   * 
   * @param commands
   */
  private void addMavenCommands(List<String> commands) {
    if (GrouperInstallerUtils.isWindows()) {
      commands.add("cmd");
      commands.add("/c");
      commands.add(this.untarredMavenDir.getAbsolutePath() + File.separator + "bin" + File.separator + "mvn.bat");
    } else {
      commands.add(shCommand());
      commands.add(this.untarredMavenDir.getAbsolutePath() + File.separator + "bin" + File.separator + "mvn");
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
        if (!shouldContinue("Should we " + arg + " tomcat anyway?", "", "grouperInstaller.autorun." + arg + "TomcatAnyway")) {
          return;
        }
      }

      
    } else {
      if (!GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress)) {
        System.out.println("Tomcat is supposed to be listening on port: " + this.tomcatHttpPort + ", port is already listening!!!!  Why is this????");
        if (!shouldContinue("Should we " + arg + " tomcat anyway?", "", "grouperInstaller.autorun." + arg + "TomcatAnyway")) {
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
            new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null, true);
        
        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }
      } catch (Throwable e) {
        e.printStackTrace();
        if (!shouldContinue("grouperInstaller.autorun.continueAfterTomcatError")) {
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
              GrouperInstaller.this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "catalina", false);
        }
      });
      thread.setDaemon(true);
      thread.start();

    }
    
    System.out.println("\nEnd tomcat " + arg + " (note: logs are in " + this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs)");
    System.out.println("##################################\n");

    System.out.print("Should we check ports to see if tomcat was able to " + arg + " (t|f)? [t]: ");
    
    boolean shouldCheckTomcat = readFromStdInBoolean(true, "grouperInstaller.autorun." + arg + "TomcatCheckPorts");
    
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
  private File untarredMavenDir;

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
      this.grouperSystemPassword = readFromStdIn("grouperInstaller.autorun.grouperSystemPassword");
      this.grouperSystemPassword = GrouperInstallerUtils.defaultString(this.grouperSystemPassword);
      
      if (!GrouperInstallerUtils.isBlank(this.grouperSystemPassword)) {
        break;
      }
      System.out.println("The GrouperSystem password cannot be blank!");
    }

    System.out.print("Do you want to set the GrouperSystem password in " + this.untarredTomcatDir + File.separator + "conf" + File.separator + "tomcat-users.xml? [t]: ");
    boolean setGrouperSystemPassword = readFromStdInBoolean(true, "grouperInstaller.autorun.setGrouperSystemPasswordInTomcatUsers");
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
      GrouperInstallerUtils.copyFile(buildPropertiesTemplateFile, buildPropertiesFile, true);
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
    if (!buildPropertiesFile.exists()) {
      File buildPropertiesTemplateFile = new File(this.untarredWsDir.getAbsolutePath() 
          + File.separator + "grouper-ws" + File.separator + "build.example.properties");
      System.out.println("Copying file: " + buildPropertiesTemplateFile.getAbsolutePath() + " to file: " + buildPropertiesFile);
      GrouperInstallerUtils.copyFile(buildPropertiesTemplateFile, buildPropertiesFile);
    }
    
    //set the grouper property
    System.out.println("Editing " + buildPropertiesFile.getAbsolutePath() + ": ");
    String apiDir = GrouperInstallerUtils.replace(this.untarredApiDir.getAbsolutePath(),"\\\\", "/");
    apiDir = GrouperInstallerUtils.replace(apiDir, "\\", "/");
    editPropertiesFile(buildPropertiesFile, "grouper.dir", apiDir);
    
  }

  /**
   * main function of grouper installer
   */
  public static enum GrouperInstallerMainFunction {
    
    /** install grouper */
    install {

      @Override
      public void logic(GrouperInstaller grouperInstaller) {
        
        grouperInstaller.mainInstallLogic();

      }
    },
    
    /** upgrade grouper */
    upgrade {

      @Override
      public void logic(GrouperInstaller grouperInstaller) {
        
        grouperInstaller.mainUpgradeLogic();

      }
    },
    
    /** create patch */
    createPatch {

      @Override
      public void logic(GrouperInstaller grouperInstaller) {
        
        grouperInstaller.mainCreatePatchLogic();

      }
    },
    
    /** see if there are patches available for grouper */
    patch {

      @Override
      public void logic(GrouperInstaller grouperInstaller) {
        
        grouperInstaller.mainPatchLogic();

      }
    };

    /**
     * run the logic for the installer function
     * @param grouperInstaller
     */
    public abstract void logic(GrouperInstaller grouperInstaller);
    
    /**
     * convert a string to the enum
     * @param theString
     * @param exceptionOnInvalid
     * @return the enum
     */
    public static GrouperInstallerMainFunction valueOfIgnoreCase(String theString, boolean exceptionOnInvalid) {
      return GrouperInstallerUtils.enumValueOfIgnoreCase(GrouperInstallerMainFunction.class, theString, false, exceptionOnInvalid);
    }
  }

  /**
   * 
   */
  private void mainLogic() {

    this.grouperInstallerMainFunction = this.grouperInstallerMainFunction();
    
    this.grouperInstallerMainFunction.logic(this);
    
  }
  
  /**
   * what are we doing
   */
  private GrouperInstallerMainFunction grouperInstallerMainFunction;
  
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
        readFromStdIn("grouperInstaller.autorun.conflictingJarContinue");
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
   * patch grouper
   */
  private void mainCreatePatchLogic() {

    //####################################
    //Find out what directory to upgrade to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperUpgradeTempDirectory();

    //see what we are upgrading: api, ui, ws, client
    this.appToUpgrade = grouperAppToUpgradeOrPatch("create a patch for");
    
    if (this.appToUpgrade == AppToUpgrade.CLIENT) {
      throw new RuntimeException("Cant create patches for client, just put the client patch files in an API patch");
    }
    
    String branchToCreatePatchFor = null;
    {
      String defaultBranchToCreatePatchFor = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.branchToCreatePatchFor", false);
      
      if (GrouperInstallerUtils.isBlank(defaultBranchToCreatePatchFor)) {
        //grouper.version = 2.2.1
        // convert to GROUPER_2_2_BRANCH
        String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

        grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

        Pattern pattern = Pattern.compile("(\\d+_\\d+_)\\d+");
        Matcher matcher = pattern.matcher(grouperVersion);
        if (matcher.matches()) {
          String majorMinor = matcher.group(1);
          defaultBranchToCreatePatchFor = "GROUPER_" + majorMinor + "BRANCH";
        }

        
      }
      
      System.out.print("What branch do you want to create a patch for (e.g. GROUPER_2_2_BRANCH)? [" + defaultBranchToCreatePatchFor + "]: ");
      branchToCreatePatchFor = readFromStdIn("grouperInstaller.autorun.branchToCreatePatchFor");
      if (GrouperInstallerUtils.isBlank(branchToCreatePatchFor)) {
        branchToCreatePatchFor = defaultBranchToCreatePatchFor;
      }
    }

    String branchForPspToCreatePatchFor = null;

    if (this.appToUpgrade == AppToUpgrade.PSP) {
      String defaultBranchForPspToCreatePatchFor = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.branchForPspToCreatePatchFor", false);
      
      if (GrouperInstallerUtils.isBlank(defaultBranchForPspToCreatePatchFor)) {
        //grouper.version = 2.2.1
        // convert to PSP_2_2_BRANCH
        String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

        grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

        Pattern pattern = Pattern.compile("(\\d+_\\d+_)\\d+");
        Matcher matcher = pattern.matcher(grouperVersion);
        if (matcher.matches()) {
          String majorMinor = matcher.group(1);
          defaultBranchForPspToCreatePatchFor = "PSP_" + majorMinor + "BRANCH";
        }
      }
      
      System.out.print("What PSP branch do you want to create a patch for (e.g. GROUPER_2_2_BRANCH)? [" + defaultBranchForPspToCreatePatchFor + "]: ");
      branchForPspToCreatePatchFor = readFromStdIn("grouperInstaller.autorun.branchForPspToCreatePatchFor");
      if (GrouperInstallerUtils.isBlank(branchForPspToCreatePatchFor)) {
        branchForPspToCreatePatchFor = defaultBranchForPspToCreatePatchFor;
      }
      
    }
    
    int nextPatchIndex = this.downloadPatches(this.appToUpgrade);

    //see if dir is there: e.g. grouper_v2_2_1_ui_patch_0
    String patchName = null;
    
    {
      String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

      grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

      patchName = "grouper_v" + grouperVersion + "_" + this.appToUpgrade.name().toLowerCase() + "_patch_" + nextPatchIndex;
    }
 
    {
      System.out.println("Next patch index for " + this.appToUpgrade + " is " + nextPatchIndex + ". ok (" + patchName + ")? (t|f)? [t]:");
      boolean continueOn = readFromStdInBoolean(true, "grouperInstaller.autorun.patchIndexIsOk");
      if (!continueOn) {
        System.out.println("Patch index is not ok");
        throw new RuntimeException("Patch index is not ok");
      }
    }
    
    downloadAndUnzipGrouperSource(branchToCreatePatchFor);

    File sourceTagDir = null;

    {
      String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);
      String grouperTag = GrouperInstallerUtils.replace(grouperVersion, ".", "_");
      System.out.println("Using Grouper tag: " + grouperTag);
      downloadAndUnzipGrouperSource("GROUPER_" + grouperTag);
      
      sourceTagDir = new File(this.grouperTarballDirectoryString + "GROUPER_" + grouperTag
          + File.separator + "grouper-GROUPER_" + grouperTag);
      
    }
    
    //grouper is in downloadDir/GROUPER_2_2_BRANCH/grouper-GROUPER_2_2_BRANCH
    File sourceDir = new File(this.grouperTarballDirectoryString + branchToCreatePatchFor
        + File.separator + "grouper-" + branchToCreatePatchFor);

    if (!sourceDir.exists()) {
      throw new RuntimeException("Why does source dir not exist??? " + sourceDir);
    }

    //grouper is in downloadDir/GROUPER_2_2_BRANCH/grouper-GROUPER_2_2_BRANCH
    File pspSourceDir = null;
    File pspSourceTagDir = null;

    if (this.appToUpgrade == AppToUpgrade.PSP) {
      downloadAndUnzipPspSource(branchForPspToCreatePatchFor);

      pspSourceDir = new File(this.grouperTarballDirectoryString + branchForPspToCreatePatchFor
          + File.separator + "grouper-psp-" + branchForPspToCreatePatchFor);

      if (!pspSourceDir.exists()) {
        throw new RuntimeException("Why does PSP source dir not exist??? " + pspSourceDir);
      }

      String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);
      System.out.println("Using PSP tag: " + grouperVersion);
      downloadAndUnzipPspSource(grouperVersion);

      pspSourceTagDir = new File(this.grouperTarballDirectoryString + grouperVersion
          + File.separator + "grouper-psp-" + grouperVersion);

      if (!pspSourceTagDir.exists()) {
        throw new RuntimeException("Why does PSP source tag dir not exist??? " + pspSourceTagDir);
      }
    }
    
    //get ant and maven
    this.downloadAndUnzipAnt();
    this.downloadAndUnzipMaven();

    if (this.appToUpgrade == AppToUpgrade.API) {
      //have to build client first
      this.buildClient(new File(sourceDir + File.separator + "grouper-misc" + File.separator + "grouperClient"));
      this.buildClient(new File(sourceTagDir + File.separator + "grouper-misc" + File.separator + "grouperClient"));
      
      //build subject api
      this.buildSubjectApi(new File(sourceDir + File.separator + "subject"));
      this.buildSubjectApi(new File(sourceTagDir + File.separator + "subject"));
    }
    
    //lets build grouper (always)
    this.untarredApiDir = new File(sourceDir + File.separator + "grouper");
    this.buildGrouperApi(new File(sourceDir + File.separator + "grouper"));
    this.untarredApiDir = new File(sourceTagDir + File.separator + "grouper");
    this.buildGrouperApi(new File(sourceTagDir + File.separator + "grouper"));
    
    if (this.appToUpgrade == AppToUpgrade.UI) {
      //lets build the UI
      this.untarredApiDir = new File(sourceDir + File.separator + "grouper");
      this.untarredUiDir = new File(sourceDir + File.separator + "grouper-ui");
      this.configureUi();
      this.buildUi(false);

      this.untarredApiDir = new File(sourceTagDir + File.separator + "grouper");
      this.untarredUiDir = new File(sourceTagDir + File.separator + "grouper-ui");
      this.configureUi();
      this.buildUi(false);

    }
    
    if (this.appToUpgrade == AppToUpgrade.WS) {
      //lets build the WS
      this.untarredApiDir = new File(sourceDir + File.separator + "grouper");
      this.untarredWsDir = new File(sourceDir + File.separator + "grouper-ws");
      this.configureWs();
      this.buildWs(false);

      this.untarredApiDir = new File(sourceTagDir + File.separator + "grouper");
      this.untarredWsDir = new File(sourceTagDir + File.separator + "grouper-ws");
      this.configureWs();
      this.buildWs(false);
    }    
    
    if (this.appToUpgrade == AppToUpgrade.PSP) {
      this.untarredApiDir = new File(sourceDir + File.separator + "grouper");
      this.buildPsp(pspSourceDir);

      this.untarredApiDir = new File(sourceTagDir + File.separator + "grouper");
      this.buildPsp(pspSourceTagDir);
    }    

    //lets index files
    Map<String, GrouperInstallerIndexFile> indexOfFiles = new TreeMap<String, GrouperInstallerIndexFile>();
    Map<String, GrouperInstallerIndexFile> indexOfTagFiles = new TreeMap<String, GrouperInstallerIndexFile>();

    patchCreateIndexFiles(indexOfFiles, sourceDir, pspSourceDir);
    patchCreateIndexFiles(indexOfTagFiles, sourceTagDir, pspSourceTagDir);

    Set<GrouperInstallerIndexFile> grouperInstallerIndexFilesToAddToPatch = new HashSet<GrouperInstallerIndexFile>();
    
    //lets get the files from the user
    OUTER: for (int i=0;i<10;i++) {
      
      if (i==9) {
        throw new RuntimeException("You need to enter valid files!");
      }

      //if subsequent pass, then start fresh
      grouperInstallerIndexFilesToAddToPatch.clear();
      
      System.out.println("Enter the comma separated list of files (dont use .class, use .java) to make a patch from: [required]\n");
      String filesToMakePatchFromCommaSeparated = readFromStdIn("grouperInstaller.autorun.patchFilesCommaSeparated");
      if (GrouperInstallerUtils.isBlank(filesToMakePatchFromCommaSeparated)) {
        System.out.println("This is a required field!");
        continue;
      }
      
      Set<String> fileKeys = new HashSet<String>(GrouperInstallerUtils.nonNull(
          GrouperInstallerUtils.splitTrimToList(filesToMakePatchFromCommaSeparated, ",")));

      for (String fileKey : fileKeys) {
        
        if (fileKey.endsWith(".class")) {
          System.out.println("Do not specify .class files, only .java files (will be compiled): '" + fileKey + "'!!!  please re-enter the list");
          continue OUTER;
        }
        
        GrouperInstallerIndexFile grouperInstallerIndexFile = indexOfFiles.get(fileKey);
        if (grouperInstallerIndexFile == null) {
          System.out.println("Cant find file: '" + fileKey + "'!!!  please re-enter the list");
          continue OUTER;
        }
        
        if (grouperInstallerIndexFile.isHasMultipleFilesBySimpleName()
            && GrouperInstallerUtils.equals(fileKey, grouperInstallerIndexFile.getSimpleName())) {
          System.out.println("This name is in the index multiple times, please be more specific: '" 
              + fileKey + "'");
          continue OUTER;
        }
        
        if (grouperInstallerIndexFile.isHasMultipleFilesByRelativePath()
            && GrouperInstallerUtils.equals(fileKey, grouperInstallerIndexFile.getRelativePath())) {
          System.out.println("This relative path is in the index multiple times, please be more specific: '" 
              + fileKey + "'");
          continue OUTER;
        }

        if (grouperInstallerIndexFile.isHasMultipleFilesByPath()
            && GrouperInstallerUtils.equals(fileKey, grouperInstallerIndexFile.getPath())) {
          System.out.println("This path is in the index multiple times, please be more specific: '" 
              + fileKey + "'");
          continue OUTER;
        }
        
        grouperInstallerIndexFilesToAddToPatch.add(grouperInstallerIndexFile);
      }
      break OUTER;
    }
    
    //ok, we have our list of files
    //lets go from java to class
    for (GrouperInstallerIndexFile grouperInstallerIndexFile : new HashSet<GrouperInstallerIndexFile>(grouperInstallerIndexFilesToAddToPatch)) {

      if (grouperInstallerIndexFile.getSimpleName().endsWith(".java")) {
        
        String relativePathJava = grouperInstallerIndexFile.getRelativePath();
        String relativePathPrefix = GrouperInstallerUtils.substringBeforeLast(relativePathJava, ".");
        String relativePathClass = relativePathPrefix + ".class";

        GrouperInstallerIndexFile grouperInstallerIndexFileClassFile = indexOfFiles.get(relativePathClass);
        
        //this shouldnt happen
        if (grouperInstallerIndexFileClassFile == null) {
          throw new RuntimeException("Cant find class file! " + relativePathClass);
        }
        
        //this shouldnt happen
        if (grouperInstallerIndexFileClassFile.isHasMultipleFilesByRelativePath()) {
          throw new RuntimeException("Class file has multiple files by relative path???? " + relativePathClass);
        }

        //found class file
        grouperInstallerIndexFilesToAddToPatch.add(grouperInstallerIndexFileClassFile);
        
        //lets get all the inner classes
        File parentFile = grouperInstallerIndexFileClassFile.getFile().getParentFile();
        
        //with slash if needed, not sure why a class wouldnt have a package, but handle the case anyways
        String parentRelativePathWithSlash = GrouperInstallerUtils.substringBeforeLast(grouperInstallerIndexFileClassFile.getRelativePath(), "/") + "/";
        if (!grouperInstallerIndexFileClassFile.getRelativePath().contains("/")) {
          parentRelativePathWithSlash = "";
        }
        String fileNameInnerClassPrefix = GrouperInstallerUtils.substringBeforeLast(
            grouperInstallerIndexFileClassFile.getFile().getName(), ".") + "$";
        for (File siblingFile : parentFile.listFiles()) {
          if (siblingFile.getName().endsWith(".class") && GrouperInstallerUtils.filePathStartsWith(siblingFile.getName(),fileNameInnerClassPrefix)) {
            //this is an inner class
            String innerClassRelativePath = parentRelativePathWithSlash + siblingFile.getName();
            GrouperInstallerIndexFile innerClassIndexFile = indexOfFiles.get(innerClassRelativePath);
            if (innerClassIndexFile == null) {
              throw new RuntimeException("Cant find inner class index file??? " + innerClassRelativePath);
            }
            if (innerClassIndexFile.isHasMultipleFilesByRelativePath()) {
              throw new RuntimeException("Inner class file has multiple files by relative path??? " + innerClassRelativePath);
            }
            //found class file
            grouperInstallerIndexFilesToAddToPatch.add(innerClassIndexFile);
          }
        }
      }
    }

    File patchDir = new File(this.grouperTarballDirectoryString + patchName);
    
    if (patchDir.exists()) {
      if (patchDir.isFile()) {
        throw new RuntimeException("Why is patch directory a file???? " + patchDir.getAbsolutePath());
      }
      
      System.out.println("Local patch dir exists, is it ok to be automatically deleted? (t|f)? [t]:");
      boolean continueOn = readFromStdInBoolean(true, "grouperInstaller.autorun.deleteLocalPatchFile");
      if (!continueOn) {
        System.out.println("Cant continue if not deleting patch dir: " + patchDir.getAbsolutePath());
        throw new RuntimeException("Cant continue if not deleting patch dir: " + patchDir.getAbsolutePath());
      }
      
      //delete this dir
      GrouperInstallerUtils.deleteRecursiveDirectory(patchDir.getAbsolutePath());
      
    }

    
    //lets look for dependencies
    Set<String> dependencyPatchNames = new TreeSet<String>();
    
    //keep track of files to put in the "old" dir
    Map<GrouperInstallerIndexFile, File> indexFileToOldFile = new HashMap<GrouperInstallerIndexFile, File>();
    
    //go from most recent to oldest
    for (int i=nextPatchIndex-1;i>=0;i--) {
      
      //lets find the patch dir
      String currentPatchName = GrouperInstallerUtils.substringBeforeLast(patchName, "_") + "_" + i;
      
      File currentPatchDir = new File(this.grouperTarballDirectoryString + currentPatchName);

      Iterator<GrouperInstallerIndexFile> iterator = grouperInstallerIndexFilesToAddToPatch.iterator();
      
      while (iterator.hasNext()) {
        
        GrouperInstallerIndexFile indexFileToAdd = iterator.next();
        
        //dont check twice
        if (indexFileToOldFile.containsKey(indexFileToAdd)) {
          continue;
        }
        
        //note the old file will be in the patch's new directory
        File oldFile = new File(currentPatchDir.getAbsolutePath() + File.separator 
            + "new" + File.separator + indexFileToAdd.getPatchFileType().getDirName()
            + File.separator + GrouperInstallerUtils.replace(indexFileToAdd.getRelativePath(), "/", File.separator));
        if (oldFile.exists() && oldFile.isFile()) {

          if (GrouperInstallerUtils.contentEquals(indexFileToAdd.getFile(), oldFile)) {
            System.out.println("New file is same as old file: " + indexFileToAdd.getFile().getAbsolutePath() + ", " 
                + oldFile.getAbsolutePath());
            System.out.println("This file will not be included in patch");
            //remove from patch
            iterator.remove();
          } else {

            //this is now a dependency
            dependencyPatchNames.add(currentPatchName);
            
            //link this with the installer index file
            indexFileToOldFile.put(indexFileToAdd, oldFile);
          }          
        }
        
      }
      
    }
    
    {
      String patchNameDependenciesString = null;
      
      OUTER: for (int i=0;i<10;i++) {
        if (i==9) {
          throw new RuntimeException("Invalid patch names!");
        }
        if (dependencyPatchNames.size() == 0) {
          
          System.out.println("No dependency patches are detected, enter any patch names that are "
              + "dependencies that you know of (comma separated), or blank for none:\n");
          patchNameDependenciesString = readFromStdIn("grouperInstaller.autorun.patchNameDependenciesCommaSeparated");
          
        } else {
    
          System.out.println("These " + dependencyPatchNames.size() + " patches are detected: " 
              + GrouperInstallerUtils.join(dependencyPatchNames.iterator(), ", "));
          System.out.println("Enter any patch names that are dependencies that you know of (comma separated), or blank for none:\n");
          patchNameDependenciesString = readFromStdIn("grouperInstaller.autorun.patchNameDependenciesCommaSeparated");
    
        }
        if (!GrouperInstallerUtils.isBlank(patchNameDependenciesString)) {
          List<String> patchNameDependeciesFromUser = GrouperInstallerUtils.splitTrimToList(patchNameDependenciesString, ",");
          for (String currentPatchName : patchNameDependeciesFromUser) {
            if (!patchNameValid(currentPatchName)) {
              System.out.println("Invalid patch name! '" + currentPatchName + "', enter them again!");
              continue OUTER;
            }
          }
          dependencyPatchNames.addAll(patchNameDependeciesFromUser);
        }
        break;
      }
      
    }    

    //find old files from the tag
    Iterator<GrouperInstallerIndexFile> iterator = grouperInstallerIndexFilesToAddToPatch.iterator();
    
    while (iterator.hasNext()) {
      GrouperInstallerIndexFile currentIndexFile = iterator.next();
      //see if its covered in another patch
      if (indexFileToOldFile.containsKey(currentIndexFile)) {
        continue;
      }
      
      //dont have old files from java or classes, thats only for other patches to do
      if (currentIndexFile.getSimpleName().endsWith(".class") || currentIndexFile.getSimpleName().endsWith(".java")) {
        continue;
      }

      //look for the old file
      GrouperInstallerIndexFile currentIndexFileFromTag = indexOfTagFiles.get(currentIndexFile.getPath());
      if (currentIndexFileFromTag == null) {
        currentIndexFileFromTag = indexOfTagFiles.get(currentIndexFile.getRelativePath());
      }
      if (currentIndexFileFromTag != null) {
        if (currentIndexFileFromTag.isHasMultipleFilesByPath()) {
          throw new RuntimeException("Why multiple paths???? " + currentIndexFile + ", " + currentIndexFile.getPath());
        }
        if (GrouperInstallerUtils.contentEquals(currentIndexFileFromTag.getFile(), currentIndexFile.getFile())) {
          System.out.println("New file is same as old file: " + currentIndexFile.getFile().getAbsolutePath() + ", " 
              + currentIndexFileFromTag.getFile().getAbsolutePath());
          System.out.println("This file will not be included in patch");
          //remove from patch
          iterator.remove();
        } else {
          //add this as an old file
          indexFileToOldFile.put(currentIndexFile, currentIndexFileFromTag.getFile());
        }
      }
    }
    
    if (grouperInstallerIndexFilesToAddToPatch.size() == 0) {
      throw new RuntimeException("There are no files to put in patch!");
    }
    

    //# will show up on screen so user knows what it is
    //description = This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
    System.out.print("\nEnter a description for this patch, e.g. GRP-123: fixes a problem with such and such: [required]\n");
    String patchDescription = readFromStdIn("grouperInstaller.autorun.patchDescription");
    
    if (GrouperInstallerUtils.isBlank(patchDescription)) {
      throw new RuntimeException("Cant have a blank description!");
    }

    //# (note, will try to get this from patch description, if its there, this can be blank)
    Matcher patchJiraKeyMatcher = Pattern.compile(".*(GRP-\\d+).*").matcher(patchDescription);
    String defaultPatchJiraKey = "";
    if (patchJiraKeyMatcher.matches()) {
      defaultPatchJiraKey = patchJiraKeyMatcher.group(1);
    }
    System.out.print("\nEnter a Jira key (e.g. GRP-123) for this patch: [required] " 
        + (GrouperInstallerUtils.isBlank(defaultPatchJiraKey) ? "" : ("[" + defaultPatchJiraKey + "]")) + "\n");
    String patchJiraKey = readFromStdIn("grouperInstaller.autorun.patchJiraKey");
    
    if (GrouperInstallerUtils.isBlank(patchJiraKey)) {
      if (!GrouperInstallerUtils.isBlank(defaultPatchJiraKey)) {
        patchJiraKey = defaultPatchJiraKey;
      } else {
        throw new RuntimeException("Cant have a blank jira key!");
      }
    }
    
    if (!Pattern.compile("^GRP-\\d+$").matcher(patchJiraKey).matches()) {
      throw new RuntimeException("Patch jira key must be valid: '" + patchJiraKey + "'");
    }

    String patchRiskLevel = null;
    
    {
      //# low, medium, or high risk to applying the patch
      //risk = low
      System.out.println("Enter the risk level for the patch: (low|medium|high): [required] ");
      String patchRiskLevelInput = readFromStdIn("grouperInstaller.autorun.patchRiskLevel");
      
      if (GrouperInstallerUtils.equalsIgnoreCase("low", patchRiskLevelInput)) {
        patchRiskLevel = "low";
      } else if (GrouperInstallerUtils.equalsIgnoreCase("medium", patchRiskLevelInput)) {
        patchRiskLevel = "medium";
      } else if (GrouperInstallerUtils.equalsIgnoreCase("high", patchRiskLevelInput)) {
        patchRiskLevel = "high";
      } else {
        throw new RuntimeException("Invalid risk level: '" + patchRiskLevelInput + "', expecting low|medium|high");
      }
      
    }
    
    //# is this is a security patch (true or false)
    //security = false
    System.out.println("Is this a security patch? (t|f): [t] ");
    boolean securityPatch = readFromStdInBoolean(false, "grouperInstaller.autorun.patchSecurity");

    boolean requiresRestart = false;
    for (GrouperInstallerIndexFile currentIndexFile : grouperInstallerIndexFilesToAddToPatch) {
      if (currentIndexFile.getSimpleName().endsWith(".jar")
          || currentIndexFile.getSimpleName().endsWith(".java")) {
        requiresRestart = true;
      }
    }
    //# if this patch requires restart of processes (true or false)
    //requiresRestart = false
    if (requiresRestart) {
      System.out.println("It is detected that your patch requires restart");
    } else {
      System.out.println("It is NOT detected that your patch requires restart, please confirm this, does it require restart (t|f)? [f] ");
      requiresRestart = readFromStdInBoolean(false, "grouperInstaller.autorun.overrideDoesntRequireRestart");
      
      if (requiresRestart) {
        System.out.println("Perhaps the maintainer of the Grouper Installer can use this feedback to make a better guess on restart, let them know");
        GrouperInstallerUtils.sleep(2000);
      }
    }
    //at this point we can build the patch dir and file and put files in there
    
    //# patches that this patch is dependant on (comma separated)
    //dependencies = 

    //create the dir
    GrouperInstallerUtils.mkdirs(patchDir);
    
    {
      String patchPropertiesContents = "# will show up on screen so user knows what it is\n"
          + "description = " + patchDescription + "\n"
          + "\n"
          + "# patches that this patch is dependant on (comma separated)\n"
          + "dependencies = " + GrouperInstallerUtils.join(dependencyPatchNames.iterator(), ", ") + "\n"
          + "\n"
          + "# low, medium, or high risk to applying the patch\n"
          + "risk = " + patchRiskLevel + "\n"
          + "\n"
          + "# is this is a security patch (true or false)\n"
          + "security = " + securityPatch + "\n"
          + "\n"
          + "# if this patch requires restart of processes (true or false)\n"
          + "requiresRestart = " + requiresRestart + "\n";
      String patchPropertiesFileName = patchDir + File.separator + patchDir.getName() + ".properties";
      GrouperInstallerUtils.saveStringIntoFile(new File(patchPropertiesFileName), patchPropertiesContents);
    }
    
    //lets do old files
    //start with old files
    if (indexFileToOldFile.size() > 0) {
      GrouperInstallerUtils.mkdirs(new File(patchDir.getAbsolutePath() + File.separator + "old"));
      for (GrouperInstallerIndexFile currentIndexFile : indexFileToOldFile.keySet()) {

        File oldFile = new File(patchDir.getAbsolutePath() + File.separator + "old" + File.separator
            + currentIndexFile.getPatchFileType().getDirName() + File.separator
            + GrouperInstallerUtils.replace(currentIndexFile.getRelativePath(), "/", File.separator));
        
        GrouperInstallerUtils.mkdirs(oldFile.getParentFile());
        
        System.out.println("Copying old file from " + indexFileToOldFile.get(currentIndexFile).getAbsolutePath()
            + "\n   to: " + oldFile.getAbsolutePath());
        
        GrouperInstallerUtils.copyFile(indexFileToOldFile.get(currentIndexFile), oldFile);
        
      }
    }

    //now put new files in place
    {
      GrouperInstallerUtils.mkdirs(new File(patchDir.getAbsolutePath() + File.separator + "new"));
      for (GrouperInstallerIndexFile currentIndexFile : grouperInstallerIndexFilesToAddToPatch) {

        File newFile = new File(patchDir.getAbsolutePath() + File.separator + "new" + File.separator
            + currentIndexFile.getPatchFileType().getDirName() + File.separator
            + GrouperInstallerUtils.replace(currentIndexFile.getRelativePath(), "/", File.separator));
        
        GrouperInstallerUtils.mkdirs(newFile.getParentFile());
        
        System.out.println("Copying new file from " + currentIndexFile.getFile().getAbsolutePath()
            + "\n   to: " + newFile.getAbsolutePath());
        
        GrouperInstallerUtils.copyFile(currentIndexFile.getFile().getAbsoluteFile(), newFile);
        
      }
    }

    {
      //generate the wiki markup
      //    <tr>
      //      <td>
      //        <p>
      //          <a href="http://software.internet2.edu/grouper/release/2.2.1/patches/grouper_v2_2_1_api_patch_1.tar.gz">grouper_v2_2_1_api_patch_1</a>
      //        </p>
      //      </td>
      //      <td>
      //        <p>
      //          <a href="https://bugs.internet2.edu/jira/browse/GRP-1096">GRP-1096: Use threads for 2.2 upgrade to decrease time of upgrade</a>
      //        </p>
      //      </td>
      //      <td>
      //        <p>classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.java <br class="atl-forced-newline"/> classes/edu/internet2/middleware/grouper/misc/MigrateLegacyAttributes.java <br class="atl-forced-newline"/> classes/edu/internet2/middleware/grouper/misc/AddMissingGroupSets.java <br class="atl-forced-newline"/> classes/edu/internet2/middleware/grouper/misc/SyncPITTables.java <br class="atl-forced-newline"/> classes/edu/internet2/middleware/grouper/misc/SyncStemSets.java <br class="atl-forced-newline"/> classes/grouper.base.properties</p>
      //      </td>
      //    </tr>
      String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

      String wikiMarkup = "    <tr>\n"
        + "      <td>\n"
        + "        <p>\n"
        + "          <a href=\"http://software.internet2.edu/grouper/release/" + grouperVersion + "/patches/" + patchName + ".tar.gz\">" + patchName + "</a>\n"
        + "        </p>\n"
        + "      </td>\n"
        + "      <td>\n"
        + "        <p>\n"
        + "          <a href=\"https://bugs.internet2.edu/jira/browse/" + patchJiraKey + "\">" + patchDescription + "</a>\n"
        + "        </p>\n"
        + "      </td>\n"
        + "      <td>\n"
        + "        <p>";
      
      boolean isFirst = true;
      for (GrouperInstallerIndexFile currentIndexFile : grouperInstallerIndexFilesToAddToPatch) {
        
        //just do java files
        if (currentIndexFile.getSimpleName().endsWith(".class")) {
          continue;
        }
        
        //classes/edu/internet2/middleware/grouper/internal/dao/hib3/Hib3StemSetDAO.java 
        //<br class=\"atl-forced-newline\"/>

        if (!isFirst) {
          wikiMarkup += "<br class=\"atl-forced-newline\"/>";
        }
        wikiMarkup += currentIndexFile.getPatchFileType().getDirName() + "/" 
            + currentIndexFile.getRelativePath();

        isFirst = false;
        
      }
      wikiMarkup += "</p>\n      </td>\n"
        + "    </tr>\n";
    
      System.out.println("Here is the wiki markup for the release notes page, copy and paste that into confluence using the <> button:");
      System.out.println("\n" + wikiMarkup + "\n");
      System.out.println("Press <enter> to continue.");
      readFromStdIn("grouperInstaller.autorun.patchContinueAfterWikiMarkup");
    }
    
    // tar this up
    File tarfile = new File(patchDir.getParentFile() + File.separator + patchName + ".tar");
    GrouperInstallerUtils.tar(patchDir, tarfile);
    
    System.out.println("\nDo you want to name this file as a test version so you can test it without affecting other users? (t|f) [t]: ");
    boolean patchUseTestFileName = readFromStdInBoolean(true, "grouperInstaller.autorun.patchNameFileAsTestVersion");

    File gzipfile = new File(patchDir.getParentFile() + File.separator + patchName + (patchUseTestFileName ? "_test" : "") + ".tar.gz");
    GrouperInstallerUtils.gzip(tarfile, gzipfile);

    System.out.println("\nSUCCESS: your patch is here: " + gzipfile.getAbsolutePath());

  }

  /**
   * patch pattern
   */
  private static final Pattern patchNamePattern = Pattern.compile("^grouper_v(\\d+)_(\\d+)_(\\d+)_(api|ws|ui|psp)_patch_\\d+$");

  
  /**
   * see if valid patch name e.g. grouper_v2_2_1_api_patch_0
   * @param patchName
   * @return true for valid
   */
  private static boolean patchNameValid(String patchName) {
    //validate patch names
    return patchNamePattern.matcher(patchName).matches();

  }
  
  /**
   * index files from a source directory
   * @param theIndexOfFiles index of label to the index file object
   * @param theSourceDir to look for files in
   * @param thePspSourceDir is psp source dir to look for files in
   */
  private void patchCreateIndexFiles(Map<String, GrouperInstallerIndexFile> theIndexOfFiles, File theSourceDir, File thePspSourceDir) {
    System.out.println("\nCreating file index to make patches from " + theSourceDir.getAbsolutePath() + "...\n");
    
    switch(this.appToUpgrade) {
      case CLIENT:
        throw new RuntimeException("No patching client, patch API instead");
      case API:

        //index the subject API
// dont think we need lib from subject, only api
//        this.patchCreateProcessFiles(indexOfFiles, 
//            new File(sourceDir.getAbsolutePath() + File.separator + "subject"),
//            new File(sourceDir.getAbsolutePath() + File.separator + "subject" + File.separator + "lib"),
//            PatchFileType.lib);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject" + File.separator + "dist" + File.separator + "build"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject" + File.separator + "src"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "subject" + File.separator + "conf"),
            PatchFileType.clazz);

        //index the grouper client
// dont think we need lib from client, only api
//        this.patchCreateProcessFiles(indexOfFiles, 
//            new File(sourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient"),
//            new File(sourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient" + File.separator + "lib"),
//            PatchFileType.lib);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient" 
                + File.separator + "dist" + File.separator + "bin"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient" 
                + File.separator + "src" + File.separator + "java"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient" 
                + File.separator + "src" + File.separator + "ext"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-misc" + File.separator + "grouperClient" 
                + File.separator + "conf"),
            PatchFileType.clazz);

        //add grouper api files
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper" + File.separator + "lib"),
            PatchFileType.lib);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper" + File.separator + "dist" 
                + File.separator + "build" + File.separator + "grouper"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper" + File.separator + "conf"),
            PatchFileType.clazz);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper" + File.separator + "src" 
                + File.separator + "grouper"),
            PatchFileType.clazz);

        break;
      case UI:
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui" 
                + File.separator + "java" + File.separator + "lib"),
            PatchFileType.lib);
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui" 
                + File.separator + "java" + File.separator + "src"),
            PatchFileType.clazz);
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui" 
                + File.separator + "conf"),
            PatchFileType.clazz);
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui" 
                + File.separator + "temp" + File.separator + "jarBin"),
            PatchFileType.clazz);
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ui" 
                + File.separator + "webapp"),
            PatchFileType.file);
        
        break;
      case WS:
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                + File.separator + "lib" + File.separator + "grouper-ws"),
            PatchFileType.lib);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                + File.separator + "lib" + File.separator + "rampart"),
            PatchFileType.lib);

        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                + File.separator + "build" + File.separator + "grouper-ws"),
            PatchFileType.clazz);
        
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                + File.separator + "conf"),
            PatchFileType.clazz);
        
        // we need to get all the source folders except test, note, each release adds another
        File parentSourceDir = new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"
            + File.separator + "src");

        for (File wsSourceDir : parentSourceDir.listFiles()) {
          if (wsSourceDir.isFile() || !wsSourceDir.getName().startsWith("grouper")) {
            continue;
          }
          this.patchCreateProcessFiles(theIndexOfFiles, 
              new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
              new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                  + File.separator + "src" + File.separator + wsSourceDir.getName()),
              PatchFileType.clazz);
        }

        //files
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws"),
            new File(theSourceDir.getAbsolutePath() + File.separator + "grouper-ws" + File.separator + "grouper-ws" 
                + File.separator + "webapp"),
            PatchFileType.file);
        
        break;
        
      case PSP:
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp"),
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp" + File.separator + "target" 
                + File.separator + "dependency"),
            PatchFileType.lib);
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp"),
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp" + File.separator + "src" 
                + File.separator + "main" + File.separator + "java"),
            PatchFileType.clazz);
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp"),
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp" + File.separator + "src" 
                + File.separator + "main" + File.separator + "resources"),
            PatchFileType.clazz);
        this.patchCreateProcessFiles(theIndexOfFiles, 
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp"),
            new File(thePspSourceDir.getAbsolutePath() + File.separator + "psp" + File.separator + "target" 
                + File.separator + "classes"),
            PatchFileType.clazz);

        break;
    }
    
    //print out files for debugging
    //for (String key : theIndexOfFiles.keySet()) {
    //  if (key.toLowerCase().contains("mygroupsmemberships")) {
    //    System.out.println(key + " -> " + theIndexOfFiles.get(key));
    //  }
    //}

    System.out.println("\nDone creating file index to make patches from " + theSourceDir.getAbsolutePath() + "... found " + theIndexOfFiles.size() + " files\n");

  }
  
  /**
   * @param directory to look in
   * @param projectDirectory is the directory where the project is for the files
   * @param indexOfFiles the index
   * @param patchFileType
   */
  private void patchCreateProcessFiles(Map<String, GrouperInstallerIndexFile> indexOfFiles, File projectDirectory, File directory, 
      PatchFileType patchFileType) {
    
    this.patchCreateProcessFilesHelper(indexOfFiles, projectDirectory, directory, patchFileType, "");

  }

  /**
   * @param directory to look in
   * @param projectDirectory is the directory where the project is for the files
   * @param indexOfFiles
   * @param relativePath in the main path to look in, helps with restrictions
   * @param patchFileType
   */
  private void patchCreateProcessFilesHelper(Map<String, GrouperInstallerIndexFile> indexOfFiles, 
      File projectDirectory, File directory, 
      PatchFileType patchFileType, String relativePath) {

    try {
      //lets spider through directory and add files to index
      //get the files into a vector
      File[] allFiles = directory.listFiles();
  
      //loop through the array
      for (int i = 0; i < allFiles.length; i++) {
  
        File currentFileOrDirectory = allFiles[i];
        
        if (-1 < currentFileOrDirectory.getName().indexOf("..")) {
          continue; //dont go to the parent directory
        }
  
        //go to sub directory
        String newRelativePath = GrouperInstallerUtils.isBlank(relativePath) ? currentFileOrDirectory.getName() 
            : (relativePath + "/" + currentFileOrDirectory.getName());
  
        if (currentFileOrDirectory.isFile()) {
  
          boolean addFile = false;
          
          String fileRelativePath = GrouperInstallerUtils.fileRelativePath(projectDirectory, currentFileOrDirectory);
  
          switch(patchFileType) {
  
            case lib:
              
              if (currentFileOrDirectory.getName().endsWith(".jar")) {
                addFile = true;
              }
  
              break;
            case file:
              addFile = true;
              
              if (currentFileOrDirectory.getName().endsWith(".jar")) {
                addFile = false;
              }
  
              if (currentFileOrDirectory.getName().endsWith(".class")) {
                addFile = false;
              }
  
              if (currentFileOrDirectory.getName().endsWith(".java")) {
                addFile = false;
              }
  
              //these are classes not files
              if (GrouperInstallerUtils.filePathStartsWith(fileRelativePath,"WEB-INF/classes")) {
                addFile = false;
              }
  
              //these are libs not files
              if (GrouperInstallerUtils.filePathStartsWith(fileRelativePath,"WEB-INF/lib")) {
                addFile = false;
              }
  
              break;
            default: 
              addFile = true;
          }
  
          if (addFile) {
            GrouperInstallerIndexFile grouperInstallerIndexFile = new GrouperInstallerIndexFile();
            grouperInstallerIndexFile.setSimpleName(currentFileOrDirectory.getName());
            grouperInstallerIndexFile.setRelativePath(newRelativePath);
            grouperInstallerIndexFile.setFile(currentFileOrDirectory);
            grouperInstallerIndexFile.setPatchFileType(patchFileType);
            grouperInstallerIndexFile.setPath(fileRelativePath);
            
            //add by name
            if (patchCreateAddFileToIndex(indexOfFiles, grouperInstallerIndexFile, currentFileOrDirectory.getName())) {
              //different file
              indexOfFiles.get(currentFileOrDirectory.getName()).setHasMultipleFilesBySimpleName(true);
              System.out.println("Note: duplicate file by name: " + currentFileOrDirectory.getAbsolutePath());
            }
            
            //add by relative path
            if (patchCreateAddFileToIndex(indexOfFiles, grouperInstallerIndexFile, newRelativePath)) {
              //different file
              indexOfFiles.get(currentFileOrDirectory.getName()).setHasMultipleFilesByRelativePath(true);
              System.out.println("Note: duplicate file by relative path: " + currentFileOrDirectory.getAbsolutePath());
            }
  
            //add by path
            if (patchCreateAddFileToIndex(indexOfFiles, grouperInstallerIndexFile, grouperInstallerIndexFile.getPath())) {
              //different file
              indexOfFiles.get(currentFileOrDirectory.getName()).setHasMultipleFilesByPath(true);
              System.out.println("Note: duplicate file by path: " + currentFileOrDirectory.getAbsolutePath());
            }
          }
          
        } else {
                  
          patchCreateProcessFilesHelper(indexOfFiles, projectDirectory, currentFileOrDirectory, patchFileType, newRelativePath);
          
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Problem with directory: " + directory.getAbsolutePath(), e);
    }
    
  }

  /**
   * @param indexOfFiles database of files by various lookup names
   * @param grouperInstallerIndexFile file to add
   * @param key add by this key
   * @return true if file already there and different
   */
  private boolean patchCreateAddFileToIndex(Map<String, GrouperInstallerIndexFile> indexOfFiles, 
      GrouperInstallerIndexFile grouperInstallerIndexFile, String key) {
    
    //convert slashes on key
    key = key.replace('\\', '/');
    
    GrouperInstallerIndexFile currentFileInIndex = indexOfFiles.get(key);
    if (currentFileInIndex == null) {
      indexOfFiles.put(key, grouperInstallerIndexFile);
    } else {
      //skip these, who cares, too many dupes
      if (!GrouperInstallerUtils.equals(grouperInstallerIndexFile.getSimpleName(), "package-info.java")
          && !GrouperInstallerUtils.equals(grouperInstallerIndexFile.getSimpleName(), "package.html")) {
        if (!GrouperInstallerUtils.equals(grouperInstallerIndexFile.computeSha1(), currentFileInIndex.computeSha1())) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * build PSP
   * @param pspDir
   */
  private void buildPsp(File pspDir) {
    if (!pspDir.exists() || pspDir.isFile()) {
      throw new RuntimeException("Cant find psp: " + pspDir.getAbsolutePath());
    }
    
    File pspBuildToDir = new File(pspDir.getAbsolutePath() + File.separator + "psp" 
        + File.separator + "target" + File.separator + "classes");
    
    boolean rebuildPsp = true;
    
    if (pspBuildToDir.exists()) {
      System.out.print("The PSP has been built in the past, do you want it rebuilt? (t|f) [t]: ");
      rebuildPsp = readFromStdInBoolean(true, "grouperInstaller.autorun.rebuildPspAfterHavingBeenBuilt");
    }
    
    if (!rebuildPsp) {
      return;
    }
    
    List<String> commands = new ArrayList<String>();
    
//    \bin\mvn compile -DskipTests
    addMavenCommands(commands);

    //put 'compile -DskipTests' in there so it wont run tests which we dont want to do
    // dependency:copy-dependencies package -DskipTests
    //not compile
    commands.add("dependency:copy-dependencies");
    commands.add("package");
    commands.add("-DskipTests");
    
    System.out.println("\n##################################");
    System.out.println("Building PSP with command:\n" + pspDir.getAbsolutePath() + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, new File(pspDir.getAbsolutePath() + File.separator + "psp-parent"), null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    System.out.println("\nEnd building PSP");
    System.out.println("##################################\n");
    
  }

  /**
   * build grouper API
   * @param grouperApiDir
   */
  private void buildGrouperApi(File grouperApiDir) {

    if (!grouperApiDir.exists() || grouperApiDir.isFile()) {
      throw new RuntimeException("Cant find grouper api: " + grouperApiDir.getAbsolutePath());
    }
    
    File grouperBuildToDir = new File(grouperApiDir.getAbsolutePath() + File.separator + "dist" + File.separator + "build" 
        + File.separator + "grouper");
    
    boolean rebuildGrouperApi = true;
    
    if (grouperBuildToDir.exists()) {
      System.out.print("The Grouper API has been built in the past, do you want it rebuilt? (t|f) [t]: ");
      rebuildGrouperApi = readFromStdInBoolean(true, "grouperInstaller.autorun.rebuildGrouperApiAfterHavingBeenBuilt");
    }
    
    if (!rebuildGrouperApi) {
      return;
    }
    
    List<String> commands = new ArrayList<String>();
    
    addAntCommands(commands);

    //this will run tests which we dont want to do
    commands.add("dist");
    
    System.out.println("\n##################################");
    System.out.println("Building grouper API with command:\n" + grouperApiDir.getAbsolutePath() + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, grouperApiDir, null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    System.out.println("\nEnd building grouper API");
    System.out.println("##################################\n");
    
  }
  


  /**
   * build subject API
   * @param subjectApiDir
   */
  private void buildSubjectApi(File subjectApiDir) {
    if (!subjectApiDir.exists() || subjectApiDir.isFile()) {
      throw new RuntimeException("Cant find subject api: " + subjectApiDir.getAbsolutePath());
    }
    
    File subjectBuildToDir = new File(subjectApiDir.getAbsolutePath() + File.separator + "dist" + File.separator + "build");
    
    boolean rebuildSubject = true;
    
    if (subjectBuildToDir.exists()) {
      System.out.print("The Grouper subject API has been built in the past, do you want it rebuilt? (t|f) [t]: ");
      rebuildSubject = readFromStdInBoolean(true, "grouperInstaller.autorun.rebuildSubjectApiAfterHavingBeenBuilt");
    }
    
    if (!rebuildSubject) {
      return;
    }
    
    List<String> commands = new ArrayList<String>();
    
    addAntCommands(commands);

    //put 'compile' in there so it wont run tests which we dont want to do
    commands.add("compile");
    
    System.out.println("\n##################################");
    System.out.println("Building subject API with command:\n" + subjectApiDir.getAbsolutePath() + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, subjectApiDir, null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    System.out.println("\nEnd building subject API");
    System.out.println("##################################\n");
    
    
    
  }
  

  /**
   * build client API
   * @param clientDir
   */
  private void buildClient(File clientDir) {
    if (!clientDir.exists() || clientDir.isFile()) {
      throw new RuntimeException("Cant find client: " + clientDir.getAbsolutePath());
    }
    
    File clientBuildToDir = new File(clientDir.getAbsoluteFile() + File.separator + "dist" + File.separator + "bin");
    
    boolean rebuildClient = true;
    
    if (clientBuildToDir.exists()) {
      System.out.print("The Grouper client has been built in the past, do you want it rebuilt? (t|f) [t]: ");
      rebuildClient = readFromStdInBoolean(true, "grouperInstaller.autorun.rebuildClientAfterHavingBeenBuilt");
    }
    
    if (!rebuildClient) {
      return;
    }

    List<String> commands = new ArrayList<String>();
    
    addAntCommands(commands);
    
    System.out.println("\n##################################");
    System.out.println("Building client with command:\n" + clientDir.getAbsolutePath() + "> " 
        + convertCommandsIntoCommand(commands) + "\n");
    
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(commands, String.class),
        true, true, null, clientDir, null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    System.out.println("\nEnd building client");
    System.out.println("##################################\n");
    
  }
  
  /**
   * patch grouper
   */
  private void mainPatchLogic() {
    
    //####################################
    //Find out what directory to upgrade to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperUpgradeTempDirectory();
    
    //see what we are upgrading: api, ui, ws, client
    this.appToUpgrade = grouperAppToUpgradeOrPatch("patch");

    //get the directory where the existing installation is
    this.upgradeExistingApplicationDirectoryString = upgradeExistingDirectory();

    GrouperInstallerPatchAction grouperInstallerPatchAction = null;
    
    
    for (int i=0;i<10;i++) {
      //what do we want to do, install, revert, or get status?
      System.out.print("What do you want to do with patches (install, revert, status)? [install]: ");
      String patchAction = readFromStdIn("grouperInstaller.autorun.patchAction");
      try {
        grouperInstallerPatchAction = GrouperInstallerPatchAction.valueOfIgnoreCase(patchAction, false, true);
        if (grouperInstallerPatchAction == null) {
          grouperInstallerPatchAction = GrouperInstallerPatchAction.install;
        }
        break;
      } catch (Exception e) {
        //ignore, let user try again
      }
    }
    
    switch(grouperInstallerPatchAction) {
      case install:
        
        //loop through applications, check patches
        this.appToUpgrade.patch(this);

        break;
        
      case revert:
        
        //look through applications, check for reverts
        this.appToUpgrade.revertPatch(this);
        break;
        
      case status:
        
        //print out status for applications
        this.appToUpgrade.patchStatus(this);
        break;
        
      default:
        throw new RuntimeException("Invalid patch action: " + grouperInstallerPatchAction);  
    }
    
  }

  /**
   * 
   */
  public static enum GrouperInstallerPatchAction {

    /** install patches */
    install,

    /**
     * revert patches
     */
    revert,
    
    /**
     * get status on patches
     */
    status;
    
    /**
     * 
     * @param string
     * @param exceptionIfInvalid
     * @param exceptionIfBlank
     * @return the action
     */
    public static GrouperInstallerPatchAction valueOfIgnoreCase(String string, boolean exceptionIfBlank, boolean exceptionIfInvalid) {
      return GrouperInstallerUtils.enumValueOfIgnoreCase(GrouperInstallerPatchAction.class, string, exceptionIfBlank, exceptionIfInvalid);
    }
    
  }
  
  /**
   * get the existing properties file of patches
   * @return the file for patches
   */
  private Properties patchExistingProperties() {
    File patchExistingPropertiesFile = this.patchExistingPropertiesFile();
    if (patchExistingPropertiesFile == null || !patchExistingPropertiesFile.exists()) {
      return new Properties();
    }
    return GrouperInstallerUtils.propertiesFromFile(patchExistingPropertiesFile);
   }

  /**
   * get the existing properties file of patches
   * @return the file for patches
   */
  private File patchExistingPropertiesFile() {
    
    //dont cache this in a variable since the upgrade existing application variable
    File patchExistingPropertiesFile = null;
    //if theres a web-inf, put it there, if not, put it in regular...
    if (new File(this.upgradeExistingApplicationDirectoryString + "WEB-INF").exists()) {
      patchExistingPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString 
          + "WEB-INF" + File.separator + "grouperPatchStatus.properties");
    } else {
      patchExistingPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString 
          + "grouperPatchStatus.properties");
    }
    return patchExistingPropertiesFile;
  }
  
  /**
   * 
   */
  private void mainUpgradeLogic() {

    System.out.println("You should backup your files and database before you start.  Press <enter> to continue.");
    readFromStdIn("grouperInstaller.autorun.backupFiles");
    
    System.out.println("\n##################################");
    System.out.println("Gather upgrade information\n");

    //####################################
    //Find out what directory to upgrade to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperUpgradeTempDirectory();
    
    //see what we are upgrading: api, ui, ws, client
    this.appToUpgrade = grouperAppToUpgradeOrPatch("upgrade");

    for (int i=0;i<10;i++) {
      System.out.println("Are there any running processes using this installation?  tomcats?  loader?  psp?  etc?  (t|f)? [f]:");
      boolean runningProcesses = readFromStdInBoolean(true, "grouperInstaller.autorun.runningProcesses");
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

    //when we revert patches, default should be true
    this.revertAllPatchesDefault = true;
    try {
      this.appToUpgrade.upgradeApp(this);
    } finally {
      this.revertAllPatchesDefault = false;
    }

    this.reportOnConflictingJars();
    
    System.out.println("\nGrouper is upgraded from " + (this.originalGrouperJarVersion == null ? null : this.originalGrouperJarVersion) 
        + " to " + GrouperInstallerUtils.propertiesValue("grouper.version", true) +  "\n");

    //this file keeps track of partial upgrades
    GrouperInstallerUtils.fileDelete(this.grouperUpgradeOriginalVersionFile);

    //reset this so that patches go against new version
    this.grouperVersionOfJar = null;
    
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
      GrouperInstallerUtils.toSet("grouperClient.webService.client.version"),
      "grouperInstaller.autorun.removeRedundantPropetiesFromGrouperClient"
    );

    
  }


  /**
   * upgrade the ui
   */
  private void upgradeUi() {

    this.upgradeApiPreRevertPatch();

    System.out.println("You need to revert all patches to upgrade");
    this.patchRevertUi();
    
    this.upgradeApiPostRevertPatch();
    
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
            + "WEB-INF" + File.separator + "bin" + File.separator + gshName);

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
        "WEB-INF/classes/grouperText/grouper.text.en.us.properties", null, GiGrouperVersion.valueOfIgnoreCase("2.2.0"), true, 
        "grouperInstaller.autorun.continueAfterNavProperties",
        "grouperInstaller.autorun.removeOldKeysFromNavProperties");

    this.changeConfig("WEB-INF/classes/resources/grouper/media.properties", 
        "WEB-INF/classes/grouper-ui.base.properties",
        "WEB-INF/classes/grouper-ui.properties", null, GiGrouperVersion.valueOfIgnoreCase("2.2.0"), false,
        "grouperInstaller.autorun.continueAfterMediaProperties",
        "grouperInstaller.autorun.removeOldKeysFromMediaProperties");

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
          readFromStdIn("grouperInstaller.autorun.continueAfterEditedOwaspCsrfGuard");
        }
      }
    }    

    //patch it
    this.patchUi();
    
  }

  /**
   * upgrade the psp
   */
  private void upgradePsp() {

    this.upgradeApiPreRevertPatch();

    System.out.println("You need to revert all patches to upgrade");
    this.patchRevertPsp();
    
    this.upgradeApiPostRevertPatch();
    
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

    //patch it
    this.patchPsp();
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
      readFromStdIn("grouperInstaller.autorun.continueAfterMergeWebXml");
      
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
   * @param autorunPropertiesKey key in properties file to automatically fill in a value
   * @param autorunPropertiesKeyRemoveOldKeys key in properties file to automatically fill in a value to remove old keys
   */
  @SuppressWarnings("unchecked")
  private void changeConfig(String legacyPropertiesFileRelativePath,
      String basePropertiesFileRelativePath,
      String propertiesFileRelativePath,
      Set<String> propertiesToIgnore,
      GiGrouperVersion versionMigrationHappened, boolean removeOldCopy, 
      String autorunPropertiesKey, String autorunPropertiesKeyRemoveOldKeys) {

    File legacyPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + legacyPropertiesFileRelativePath);
    File newBasePropertiesFile = new File(this.untarredUiDir + File.separator + "dist" 
        + File.separator + "grouper" + File.separator + basePropertiesFileRelativePath);
    File existingBasePropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + basePropertiesFileRelativePath);
    File existingPropertiesFile = new File(this.upgradeExistingApplicationDirectoryString + propertiesFileRelativePath);

    this.compareUpgradePropertiesFile(existingBasePropertiesFile, newBasePropertiesFile, 
        existingPropertiesFile, null, propertiesToIgnore, autorunPropertiesKeyRemoveOldKeys);

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
          readFromStdIn(autorunPropertiesKey);

        }

        if (removeOldCopy) {
          
          System.out.println(legacyPropertiesFileRelativePath + " is not used anymore by grouper, can it be backed up and removed (t|f)? [t]: ");
          boolean removeLegacy = readFromStdInBoolean(true, autorunPropertiesKeyRemoveOldKeys);
          if (removeLegacy) {
            File backupLegacy = bakFile(legacyPropertiesFile);
            GrouperInstallerUtils.copyFile(legacyPropertiesFile, backupLegacy, true);
            GrouperInstallerUtils.fileDelete(legacyPropertiesFile);
            System.out.println("File as removed.  Backup path: " + backupLegacy.getAbsolutePath());
          }
          
        } else {
          System.out.println(legacyPropertiesFileRelativePath + " has properties that can be removed since they are now managed in "
              + basePropertiesFileRelativePath);
          System.out.println("Would you like to have the properties automatically removed from " 
              + legacyPropertiesFile.getName() + " (t|f)? [t]: ");
          boolean removeRedundantProperties = readFromStdInBoolean(true, autorunPropertiesKeyRemoveOldKeys);
          
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
        if (!GrouperInstallerUtils.filePathStartsWith(path,fromDirString)) {
          throw new RuntimeException("Why does path not start with fromDirString: " + path + ", " + fromDirString);
        }
        relativePath = path.substring(fromDirString.length());
        relativePath = GrouperInstallerUtils.fileMassagePathsNoLeadingOrTrailing(relativePath);
      }
      boolean ignore = false;
      
      //ignore paths passed in
      for (String pathToIgnore : relativePathsToIgnore) {
        if (GrouperInstallerUtils.filePathStartsWith(relativePath,pathToIgnore)) {
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
        GrouperInstallerUtils.copyFile(fileToCopyFrom, fileToCopyTo, false);
        
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
        listFiles = readFromStdInBoolean(false, "grouperInstaller.autorun.viewListOfFilesChangedInCopy");
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
    this.upgradeApiPreRevertPatch();

    System.out.println("You need to revert all patches to upgrade");
    this.patchRevertApi();
    
    this.upgradeApiPostRevertPatch();
  }


  /**
   * upgrade the api
   */
  private void upgradeApiPreRevertPatch() {

    //make sure existing gsh is executable and dos2unix
    gshExcutableAndDos2Unix(new File(gshCommand()).getParentFile().getAbsolutePath(), "existing");
    
    this.runChangeLogTempToChangeLog();
  }
  
  
  /**
   * upgrade the api
   */
  private void upgradeApiPostRevertPatch() {

    //revert patches
    this.upgradeClient();

    System.out.println("\n##################################");
    System.out.println("Upgrading API\n");

    //lets get the version of the existing jar
    this.originalGrouperJarVersionOrUpgradeFileVersion();

    this.compareAndReplaceJar(this.grouperJar, 
        new File(this.untarredApiDir + File.separator + "dist" + File.separator 
            + "lib" + File.separator + "grouper.jar"), true, null);

    System.out.println("\n##################################");
    System.out.println("Upgrading API config files\n");

    this.compareUpgradePropertiesFile(this.grouperBasePropertiesFile, 
      new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper.base.properties"),
      this.grouperPropertiesFile,
      this.grouperExamplePropertiesFile, null, 
      "grouperInstaller.autorun.removeRedundantPropetiesFromGrouperProperties"
    );
      
    this.compareUpgradePropertiesFile(this.grouperHibernateBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper.hibernate.base.properties"),
        this.grouperHibernatePropertiesFile,
        this.grouperHibernateExamplePropertiesFile, null, 
        "grouperInstaller.autorun.removeRedundantPropetiesFromGrouperHibernateProperties"
      );
        
    this.compareUpgradePropertiesFile(this.grouperLoaderBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouper-loader.base.properties"),
        this.grouperLoaderPropertiesFile,
        this.grouperLoaderExamplePropertiesFile, null,
        "grouperInstaller.autorun.removeRedundantPropetiesFromGrouperLoaderProperties"
      );

    this.compareUpgradePropertiesFile(this.subjectBasePropertiesFile, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "subject.base.properties"),
        this.subjectPropertiesFile,
        null, null,
        "grouperInstaller.autorun.removeRedundantPropetiesFromSubjectProperties"
      );

    this.upgradeEhcacheXml();

    this.compareAndCopyFile(this.grouperUtf8File, 
        new File(this.untarredApiDir + File.separator + "conf" + File.separator + "grouperUtf8.txt"),
        true,
        new File(this.upgradeExistingClassesDirectoryString)
        );
    
    System.out.println("\nYou should compare " + this.grouperPropertiesFile.getParentFile().getAbsolutePath() + File.separator + "sources.xml"
        + "\n  with " + this.untarredApiDir + File.separator + "conf" + File.separator + "sources.xml");
    System.out.println("Press <enter> to continue after you have merged the sources.xml");
    readFromStdIn("grouperInstaller.autorun.continueAfterMergingSourcesXml");
    
    
    System.out.println("\n##################################");
    System.out.println("Upgrading API jars\n");

    this.upgradeJars(new File(this.untarredApiDir + File.separator + "lib" 
      + File.separator + "grouper" + File.separator));
    this.upgradeJars(new File(this.untarredApiDir + File.separator + "lib" 
      + File.separator + "jdbcSamples" + File.separator));

    System.out.println("\n##################################");
    System.out.println("Patch API\n");

    //patch it
    this.patchApi();

    System.out.println("\n##################################");
    System.out.println("Upgrading DB (registry)\n");

    this.apiUpgradeDbVersion(true);

    this.apiUpgradeAdditionalGshScripts();

  }

  /**
   * run additional GSH scripts based on what we are upgrading from...
   */
  private void apiUpgradeAdditionalGshScripts() {
    GiGrouperVersion giGrouperVersion = this.originalGrouperJarVersionOrUpgradeFileVersion();
    if (giGrouperVersion == null) {
      System.out.println("Grouper jar file: " + (this.grouperJar == null ? null : this.grouperJar.getAbsolutePath()));
      System.out.println("ERROR, cannot find grouper version in grouper jar file, do you want to continue? (t|f)? [f]: ");
      boolean continueScript = readFromStdInBoolean(false, "grouperInstaller.autorun.shouldContinueAfterNoGrouperVersionFound");
      if (!continueScript) {
        System.exit(1);
      }
    }

    boolean lessThan2_0 = this.originalGrouperJarVersion.lessThanArg(new GiGrouperVersion("2.0.0"));
    {
      String runUsduAutorun = null;
      if (lessThan2_0) {
        System.out.println("You are upgrading from pre API version 2.0.0, do you want to run Unresolvable Subject Deletion Utility (USDU) (recommended) (t|f)? [t]: ");
        runUsduAutorun = "grouperInstaller.autorun.runUsduPre2.0.0";
      } else {
        System.out.println("You are upgrading from after API version 2.0.0, so you dont have to do this,\n  "
            + "but do you want to run Unresolvable Subject Deletion Utility (USDU) (not recommended) (t|f)? [f]: ");
        runUsduAutorun = "grouperInstaller.autorun.runUsduPost2.0.0";
      }
      boolean runScript = readFromStdInBoolean(lessThan2_0, runUsduAutorun);
      
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

      String autorunResolveGroupSubjects = null;
      if (lessThan2_0) {
        System.out.println("You are upgrading from pre API version 2.0.0, do you want to resolve all group subjects (recommended) (t|f)? [t]: ");
        autorunResolveGroupSubjects = "grouperInstaller.autorun.resolveGroupSubjectsPre2.0.0";
      } else {
        System.out.println("You are upgrading from after API version 2.0.0, so you dont have to do this,\n  "
            + "but do you want to resolve all group subjects (not recommended) (t|f)? [f]: ");
        autorunResolveGroupSubjects = "grouperInstaller.autorun.resolveGroupSubjectsPost2.0.0";
      }
      boolean runScript = readFromStdInBoolean(lessThan2_0, autorunResolveGroupSubjects);
      
      if (runScript) {
        
        //running with command on command line doenst work on linux since the args with whitespace translate to 
        //save the commands to a file, and runt he file
        StringBuilder gshCommands = new StringBuilder();
  
        //gsh 5% GrouperSession.startRootSession();
        //edu.internet2.middleware.grouper.GrouperSession: 4163fb08b3b24922b55a14010d48e121,'GrouperSystem','application'
        //gsh 6% for (String g : HibernateSession.byHqlStatic().createQuery("select uuid from Group").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, "g:gsa", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true); }
  
        gshCommands.append("grouperSession = GrouperSession.startRootSession();\n");
        gshCommands.append("for (String g : HibernateSession.byHqlStatic().createQuery(\"select uuid from Group\").listSet(String.class)) { subj = SubjectFinder.findByIdAndSource(g, \"g:gsa\", true); GrouperDAOFactory.getFactory().getMember().findBySubject(subj).updateMemberAttributes(subj, true); }\n");
  
        File gshFile = new File(this.untarredApiDir.getAbsolutePath() + File.separator + "gshGroupUsdu.gsh");
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
      String autorunSeeRuleCheckType = null;
      if (lessThan2_1) {
        System.out.println("You are upgrading from pre API version 2.1.0, do you want to "
            + "see if you have rules with ruleCheckType: flattenedPermission* (recommended) (t|f)? [t]: ");
        autorunSeeRuleCheckType = "grouperInstaller.autorun.seeRulesFlattenedPermissionsPre2.1.0";
      } else {
        System.out.println("You are upgrading from after API version 2.1.0, so you dont have to do this,\n  "
            + "but do you want to see if you have rules with ruleCheckType: flattenedPermission* (not recommended) (t|f)? [f]: ");
        autorunSeeRuleCheckType = "grouperInstaller.autorun.seeRulesFlattenedPermissionsPost2.1.0";
      }
      boolean runScript = readFromStdInBoolean(lessThan2_1, autorunSeeRuleCheckType);
      
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
          new File(this.gshCommand()).getParentFile(), null, true);

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
            if (!readFromStdInBoolean(true, "grouperInstaller.autorun.shouldContinueAfterErrorCountFlattenedRules")) {
              System.exit(1);
            }
          } else {
            if (count > 0) {
              System.out.println("You have " + count + " flattenedPermission rules that need to be removed.  You need to look in the view grouper_rules_v and notify the owners and remove these rules.  Do you want to continue (t|f)? [t]: ");
              
              if (!readFromStdInBoolean(true, "grouperInstaller.autorun.shouldContinueAfterFoundFlattenedRules")) {
                System.exit(1);
              }
            }
          }
        }
      }
    }

    {
      boolean lessThan2_2_0 = giGrouperVersion.lessThanArg(new GiGrouperVersion("2.2.0"));
      String autorunRun2_2gshScript = null;
      if (lessThan2_2_0) {
        System.out.println("You are upgrading from pre API version 2.2.0, "
            + "do you want to run the 2.2 upgrade GSH script (recommended) (t|f)? [t]: ");
        autorunRun2_2gshScript = "grouperInstaller.autorun.run2.2gshUpgradeScriptPre2.2.0";
      } else {
        System.out.println("You are upgrading from after API version 2.2.0, so you dont have to do this,\n  "
            + "but do you want to run the 2.2 upgrade GSH script (not recommended) (t|f)? [f]: ");
        autorunRun2_2gshScript = "grouperInstaller.autorun.run2.2gshUpgradeScriptPost2.2.0";
      }
      boolean runScript = readFromStdInBoolean(lessThan2_2_0, autorunRun2_2gshScript);
      
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
      String autorunRun2_2_1gshUpgradeScript = null;
      if (lessThan2_2_1) {
        System.out.println("You are upgrading from pre API version 2.2.1, do you want to "
            + "run the 2.2.1 upgrade GSH script (recommended) (t|f)? [t]: ");
        autorunRun2_2_1gshUpgradeScript = "grouperInstaller.autorun.run2.2.1gshUpgradeScriptPre2.2.1";
      } else {
        System.out.println("You are upgrading from after API version 2.2.1, so you dont have to do this,\n  "
            + "but do you want to run the 2.2.1 upgrade GSH script (not recommended) (t|f)? [f]: ");
        autorunRun2_2_1gshUpgradeScript = "grouperInstaller.autorun.run2.2.1gshUpgradeScriptPost2.2.1";
      }
      boolean runScript = readFromStdInBoolean(lessThan2_2_1, autorunRun2_2_1gshUpgradeScript);
      
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
   * grouper version of jar
   */
  private GiGrouperVersion grouperVersionOfJar = null;
  
  /**
   * 
   * @return the version
   */
  private GiGrouperVersion grouperVersionOfJar() {
    if (this.grouperVersionOfJar == null) {
      String grouperJarVersionString = null;
      if (this.grouperJar != null && this.grouperJar.exists()) {
        grouperJarVersionString = GrouperInstallerUtils.jarVersion(this.grouperJar);
        
      } else if (this.grouperClientJar != null && this.grouperClientJar.exists()) {
        grouperJarVersionString = GrouperInstallerUtils.jarVersion(this.grouperClientJar);
        
      }
      if (!GrouperInstallerUtils.isBlank(grouperJarVersionString)) {
        this.grouperVersionOfJar = new GiGrouperVersion(grouperJarVersionString);
      }
  
      if (this.grouperVersionOfJar == null) {
        throw new RuntimeException("Cant find version of grouper! " + this.grouperJar + ", " + this.grouperClientJar);
      }
    }
    
    return this.grouperVersionOfJar;
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
  private GiGrouperVersion originalGrouperJarVersionOrUpgradeFileVersion() {

    if (!this.originalGrouperJarVersionRetrieved) {

      this.originalGrouperJarVersionRetrieved = true;
      
      //lets see if an upgrade went halfway through
      this.grouperUpgradeOriginalVersionFile = new File(this.upgradeExistingApplicationDirectoryString + "grouperUpgradeOriginalVersion.txt");

      this.originalGrouperJarVersion = this.grouperVersionOfJar();
      
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
        new File(this.gshCommand()).getParentFile(), null, true);
    
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
      boolean continueOn = readFromStdInBoolean(null, "grouperInstaller.autorun.shouldContinueIfErrorUpgradingDatabase");
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
    boolean runIt = readFromStdInBoolean(true, "grouperInstaller.autorun.shouldRunDdlScript");
    
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
          readFromStdIn("grouperInstaller.autorun.continueAfterFoundMultipleJars");

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
    GrouperInstallerUtils.copyFile(this.ehcacheFile, ehcacheBakFile, true);

    boolean mergeFiles = true;
    
    if (this.ehcacheExampleFile != null) {
      File ehcacheExampleBakFile = bakFile(this.ehcacheExampleFile);
  
      GrouperInstallerUtils.copyFile(this.ehcacheExampleFile, ehcacheExampleBakFile, true);
    } else {
      GrouperInstallerUtils.copyFile(newEhcacheExample, this.ehcacheFile, true);
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
    readFromStdIn("grouperInstaller.autorun.continueAfterCompareEhcache");

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
        GrouperInstallerUtils.copyFile(existingFile, bakFile, true);
        if (printDetails) {
          System.out.println("Backing up: " + existingFile.getAbsolutePath() + " to: " + bakFile.getAbsolutePath());
        }
      }
      if (printDetails) {
        System.out.println("Copying " + (fileExists ? "new file" : "upgraded file") + ": " + newFile.getAbsolutePath() + " to: " + existingFile.getAbsolutePath());
      }
      GrouperInstallerUtils.copyFile(newFile, existingFile, false);
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
    if (!GrouperInstallerUtils.filePathStartsWith(existingFilePath, this.upgradeExistingApplicationDirectoryString)) {
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
   * @param autorunPropertiesKeyRemoveRedundantProperties key in properties file to automatically fill in a value
   */
  private void compareUpgradePropertiesFile(File existingBasePropertiesFile, 
      File newBasePropertiesFile,
      File existingPropertiesFile,
      File existingExamplePropertiesFile,
      Set<String> propertiesToIgnore, String autorunPropertiesKeyRemoveRedundantProperties) {

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
        if (!GrouperInstallerUtils.filePathStartsWith(existingBasePropertiesFilePath, this.upgradeExistingApplicationDirectoryString)) {
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
        
        GrouperInstallerUtils.copyFile(newBasePropertiesFile, existingBasePropertiesFile, true);

      }
      
    } else {
      
      hadChange = true;
      
      System.out.println(newBasePropertiesFile.getName() + " didn't exist and was installed.");
      
      //its null, but we dont have the path...
      if (existingBasePropertiesFile == null) {
        existingBasePropertiesFile = new File(this.upgradeExistingClassesDirectoryString + newBasePropertiesFile.getName());
      }
      GrouperInstallerUtils.copyFile(newBasePropertiesFile, existingBasePropertiesFile, true);
    }
    
    // if there is an example there, it can be removed
    if (existingExamplePropertiesFile != null && existingExamplePropertiesFile.exists() && existingExamplePropertiesFile.isFile()) {

      String existingExamplePropertiesFilePath = existingExamplePropertiesFile.getAbsolutePath();
      if (!GrouperInstallerUtils.filePathStartsWith(existingExamplePropertiesFilePath, this.upgradeExistingApplicationDirectoryString)) {
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
        boolean removeRedundantProperties = readFromStdInBoolean(true, autorunPropertiesKeyRemoveRedundantProperties);
        
        if (removeRedundantProperties) {

          String existingPropertiesFilePath = existingPropertiesFile.getAbsolutePath();
          if (!GrouperInstallerUtils.filePathStartsWith(existingPropertiesFilePath, this.upgradeExistingApplicationDirectoryString)) {
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

          GrouperInstallerUtils.copyFile(existingPropertiesFile, bakPropertiesFile, true);
          
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
      public void patchStatus(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchStatusUi();
      }

      @Override
      public void patch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchUi();
      }

      @Override
      public void revertPatch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchRevertUi();
      }

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
          String serverXmlLocation = readFromStdIn("grouperInstaller.autorun.locationOfTomcatServerXml");
          
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

        grouperInstaller.grouperUtf8File = grouperInstaller.findClasspathFile("grouperUtf8.txt", false);

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

      @Override
      public void patch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchApi();
      }

      @Override
      public void revertPatch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchRevertApi();
      }

      @Override
      public void patchStatus(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchStatusApi();
      }
    },

    /**
     * upgrading the client
     */
    CLIENT {

      @Override
      public void patchStatus(GrouperInstaller grouperInstaller) {
        throw new RuntimeException("Cant patch status client.  Client patches will be in the API if applicable");
      }

      @Override
      public void patch(GrouperInstaller grouperInstaller) {
        throw new RuntimeException("Cant patch client.  Client patches will be in the API if applicable");
      }

      @Override
      public void revertPatch(GrouperInstaller grouperInstaller) {
        throw new RuntimeException("Cant revert client.  Client patches will be in the API if applicable");
      }

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
      public void patchStatus(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchStatusWs();
      }

      @Override
      public void patch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchWs();
      }

      @Override
      public void revertPatch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchRevertWs();
      }

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
      public void patchStatus(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchStatusPsp();
      }

      @Override
      public void patch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchPsp();
      }

      @Override
      public void revertPatch(GrouperInstaller grouperInstaller) {
        grouperInstaller.patchRevertPsp();
      }

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
     * patch this app
     * @param grouperInstaller
     */
    public abstract void patch(GrouperInstaller grouperInstaller);
    
    /**
     * revert patch this app
     * @param grouperInstaller
     */
    public abstract void revertPatch(GrouperInstaller grouperInstaller);
    
    /**
     * patch status for this app
     * @param grouperInstaller
     */
    public abstract void patchStatus(GrouperInstaller grouperInstaller);
    
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
   * patch names installed (used for dependency checking), without the file ending e.g. grouper_v2_2_1_api_patch_0
   */
  private Set<String> patchesInstalled = new HashSet<String>();
  
  /**
   * if grouper is stopped
   */
  private boolean grouperStopped = false;

  /**
   * if should revert all
   */
  private Boolean revertAllPatches = null;
  
  /**
   * default for revert all patches
   */
  private boolean revertAllPatchesDefault = false;
  
  /**
   * if should revert all
   */
  private Boolean installAllPatches = null;
  
  /**
   * revert patches for an app
   * @param thisAppToRevert
   * @return if reverted
   */
  private boolean revertPatches(AppToUpgrade thisAppToRevert) {

    if (thisAppToRevert == AppToUpgrade.CLIENT) {
      throw new RuntimeException("Cant revert " + thisAppToRevert);
    }
    
    Properties patchesExistingProperties = patchExistingProperties();
    
    String grouperVersion = this.grouperVersionOfJar().toString();

    grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

    Map<Integer, String> patchNumberToNameBase = new LinkedHashMap<Integer, String>();
    
    boolean foundPatch = false;

    Map<String, Set<String>> installedPatchDependencies = new HashMap<String, Set<String>>();
    
    File patchExistingPropertiesFile = patchExistingPropertiesFile();
    
    for (int i=1000;i>=0;i--) {
      
      //grouper_v2_2_1_api_patch_0.state
      String keyBase = "grouper_v" + grouperVersion + "_" + thisAppToRevert.name().toLowerCase() + "_patch_" + i;
      String key = keyBase + ".state";

      patchNumberToNameBase.put(i, keyBase);
      
      String value = patchesExistingProperties.getProperty(key);

      if (!GrouperInstallerUtils.isBlank(value)) {
        
        System.out.println("\n################ Checking patch " + keyBase);

        GrouperInstallerPatchStatus grouperInstallerPatchStatus = GrouperInstallerPatchStatus.valueOfIgnoreCase(value, true, true);
        
        switch (grouperInstallerPatchStatus) {
          case skippedPermanently:
            
            System.out.println("Patch: " + keyBase + ": was skipped permanently on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            continue;

          case skippedTemporarily:

            System.out.println("Patch: " + keyBase + ": was skipped termporarily on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            continue;

          case reverted:

            System.out.println("Patch: " + keyBase + ": was removed on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            continue;

          case error:

            System.out.println("Patch: " + keyBase + ": had an error installing on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            continue;

          case applied:
            
            System.out.println("Patch: " + keyBase + ": was applied on: " + patchesExistingProperties.getProperty(keyBase + ".date")  + "\n");
            this.patchesInstalled.add(keyBase);
            break;

          default:
            throw new RuntimeException("Not expecting: " + grouperInstallerPatchStatus);
        }

      } else {
        continue;
      }

      if (!this.patchesInstalled.contains(keyBase)) {
        System.out.println("\n");
        continue;
      }

      //lets see if it exists on the server
      File patchUntarredDir = downloadAndUnzipPatch(keyBase);
      
      //if no more patches
      if (patchUntarredDir == null) {
        System.out.print("Error: cant find directory for patch: " + keyBase + ", press <enter> to continue");
        readFromStdIn("grouperInstaller.autorun.continueAfterCantFindPatchDir");
        continue;
      }

      //lets get the description:
      //  # will show up on screen so user knows what it is
      //  description = This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
      //
      //  # patches that this patch is dependant on (comma separated)
      //  dependencies = 
      //
      //  # low, medium, or high risk to applying the patch
      //  risk = low
      //
      //  # is this is a security patch (true or false)
      //  security = false
      //
      //  # if this patch requires restart of processes (true or false)
      //  requiresRestart = false
      Properties patchProperties = GrouperInstallerUtils.propertiesFromFile(new File(patchUntarredDir.getAbsoluteFile() + File.separator + keyBase + ".properties"));

      foundPatch = true;

      // check dependencies
      {
        List<String> dependencies = GrouperInstallerUtils.splitTrimToList(patchProperties.getProperty("dependencies"), ",");
        Set<String> dependenciesSet = new HashSet<String>(GrouperInstallerUtils.nonNull(dependencies));
        installedPatchDependencies.put(keyBase, dependenciesSet);
      }

      boolean securityRelated = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("security"), false);
      boolean requiresRestart = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("requiresRestart"), true);

      if (this.revertAllPatches == null) {
        System.out.println("Would you like to revert all patches (t|f)? [" + (this.revertAllPatchesDefault ? "t" : "f") + "]: ");
        this.revertAllPatches = readFromStdInBoolean(this.revertAllPatchesDefault, "grouperInstaller.autorun.revertAllPatches");
      }
      
      //print description
      System.out.println("Patch " + keyBase + " is " + patchProperties.getProperty("risk") + " risk, "
          + (securityRelated ? "is a security patch" : "is not a security patch"));
      System.out.println(patchProperties.getProperty("description"));
      if (!this.revertAllPatches) {
        System.out.print("Would you like to revert patch " + keyBase + " (t|f)? [f]: ");
        boolean revertPatch = readFromStdInBoolean(false, "grouperInstaller.autorun.revertPatch");

        if (!revertPatch) {
          System.out.println("");
          continue;
        }
      }
      //check dependencies
      for (String patchName : installedPatchDependencies.keySet()) {
        
        Set<String> dependencies = GrouperInstallerUtils.nonNull(installedPatchDependencies.get(patchName));
        
        if (dependencies.contains(keyBase)) {
          System.out.println("Error: cant revert " + keyBase + " because an installed patch is dependent on it: " + patchName);
          System.exit(1);
        }
      }

      if (requiresRestart && !this.grouperStopped) {
        System.out.println("This patch requires all processes that user Grouper to be stopped.\n  "
            + "Please stop these processes if they are running and press <enter> to continue...");
        this.grouperStopped = true;
        readFromStdIn("grouperInstaller.autorun.continueAfterStoppingGrouperProcesses");
      }
      
      Map<String, String> patchDirToApplicationPath = new LinkedHashMap<String, String>();
      patchDirToApplicationPath.put("files", this.upgradeExistingApplicationDirectoryString);
      patchDirToApplicationPath.put("classes", this.upgradeExistingClassesDirectoryString);
      patchDirToApplicationPath.put("lib", this.upgradeExistingLibDirectoryString);

      boolean patchHasProblem = false;
      
      //we are reverting this patch, lets see if the files are there...
      //this.upgradeExistingApplicationDirectoryString
      //patchUntarredDir
      File newDir = new File(patchUntarredDir.getAbsolutePath() + File.separator + "new");
      File oldDir = new File(patchUntarredDir.getAbsolutePath() + File.separator + "old");
      {

        for (String patchDir : patchDirToApplicationPath.keySet()) {

          String applicationPath = patchDirToApplicationPath.get(patchDir);

          File newDirFiles = new File(newDir.getAbsoluteFile() + File.separator + patchDir);
          File oldDirFiles = new File(oldDir.getAbsoluteFile() + File.separator + patchDir);
          
          if (newDirFiles.exists() && newDirFiles.isDirectory()) {

            // relative, e.g. WEB-INF/jsp/someFile.jsp
            Set<String> newFileRelativePaths = GrouperInstallerUtils.fileDescendantRelativePaths(newDirFiles);

            for (String newFilePath : GrouperInstallerUtils.nonNull(newFileRelativePaths)) {
              File newFileInPatch = new File(newDirFiles.getAbsolutePath() + File.separator + newFilePath);
              File oldFileInPatch = new File(oldDirFiles.getAbsolutePath() + File.separator + newFilePath);

              File newFileInGrouper = new File(applicationPath + newFilePath);

              if (!newFileInGrouper.exists() || !newFileInGrouper.isFile() 
                  || (!GrouperInstallerUtils.contentEquals(newFileInPatch, newFileInGrouper)
                      //its ok if the patch is already reverted?
                      && !GrouperInstallerUtils.contentEquals(oldFileInPatch, newFileInGrouper))) {
                System.out.println("Cannot revert patch since this patch file:\n  " + newFileInPatch.getAbsolutePath() 
                    + "\n  is not the same as what the patch expects:\n  " + newFileInGrouper.getAbsolutePath());
                patchHasProblem = true;
              }
            }
          }
        }
      }

      if (patchHasProblem) {
        System.out.println("Cannot continue since patch has problem");
        System.exit(1);
      }
      
      //so far so good, all the new files are all good, revert the patch
      for (String patchDir : patchDirToApplicationPath.keySet()) {
        
        String applicationPath = patchDirToApplicationPath.get(patchDir);

        File oldDirFiles = new File(oldDir.getAbsoluteFile() + File.separator + patchDir);
        File newDirFiles = new File(newDir.getAbsoluteFile() + File.separator + patchDir);
        
        if (newDirFiles.exists() && newDirFiles.isDirectory()) {
        
          // relative, e.g. WEB-INF/jsp/someFile.jsp
          Set<String> newFileRelativePaths = GrouperInstallerUtils.fileDescendantRelativePaths(newDirFiles);
          
          for (String newFilePath : GrouperInstallerUtils.nonNull(newFileRelativePaths)) {

            File oldFileInPatch = new File(oldDirFiles.getAbsolutePath() + File.separator + newFilePath);

            File newFileInGrouper = new File(applicationPath + newFilePath);
            
            if (oldFileInPatch.exists() && oldFileInPatch.isFile()) {
              System.out.println("Reverting file: " + newFileInGrouper.getAbsolutePath());
              GrouperInstallerUtils.copyFile(oldFileInPatch, newFileInGrouper, false);
            } else {
              System.out.println("Reverting (deleting) file: " + newFileInGrouper.getAbsolutePath());
              GrouperInstallerUtils.fileDelete(newFileInGrouper);
            }
          }
        }
      }
      
      this.patchesInstalled.remove(keyBase);
      installedPatchDependencies.remove(keyBase);
      System.out.println("Patch successfully reverted: " + keyBase);

      editPropertiesFile(patchExistingPropertiesFile, keyBase + ".date", 
          GrouperInstallerUtils.dateMinutesSecondsFormat.format(new Date()));
      editPropertiesFile(patchExistingPropertiesFile, keyBase + ".state", 
          GrouperInstallerPatchStatus.reverted.name());

      System.out.println("");
    }

    if (!foundPatch) {
      System.out.println("There are no new " + thisAppToRevert + " patches to revert\n");
      return false;
    }
    
    return true;
      
  }
  
  /**
   * get the patches available to apply that are not already applied
   * @param thisAppToUpgrade app to upgrade to check
   * @return if patches were installed
   */
  private boolean downloadAndInstallPatches(AppToUpgrade thisAppToUpgrade) {

    if (thisAppToUpgrade == AppToUpgrade.CLIENT) {
      throw new RuntimeException("Cant install patches for " + thisAppToUpgrade);
    }
    
    Properties patchesExistingProperties = patchExistingProperties();

    String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

    grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

    Map<Integer, String> patchNumberToNameBase = new LinkedHashMap<Integer, String>();
    
    boolean foundNewPatch = false;
    
    File patchExistingPropertiesFile = patchExistingPropertiesFile();
    
    OUTER: for (int i=0;i<1000;i++) {
      
      //grouper_v2_2_1_api_patch_0.state
      String keyBase = "grouper_v" + grouperVersion + "_" + thisAppToUpgrade.name().toLowerCase() + "_patch_" + i;
      System.out.println("\n################ Checking patch " + keyBase);
      String key = keyBase + ".state";

      patchNumberToNameBase.put(i, keyBase);
      
      String value = patchesExistingProperties.getProperty(key);

      if (!GrouperInstallerUtils.isBlank(value)) {
        
        GrouperInstallerPatchStatus grouperInstallerPatchStatus = GrouperInstallerPatchStatus.valueOfIgnoreCase(value, true, true);
        
        switch (grouperInstallerPatchStatus) {
          case applied:
            
            System.out.println("Patch: " + keyBase + ": was applied on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            this.patchesInstalled.add(keyBase);
            
            continue;

          case skippedPermanently:
            
            System.out.println("Patch: " + keyBase + ": was skipped permanently on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");
            continue;

          case skippedTemporarily:

            System.out.println("Patch: " + keyBase + ": was skipped termporarily on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");

            break;

          case reverted:

            System.out.println("Patch: " + keyBase + ": was reverted on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");

            break;

          case error:

            System.out.println("Patch: " + keyBase + ": had an error installing on: " + patchesExistingProperties.getProperty(keyBase + ".date") + "\n");

            break;

          default:
            throw new RuntimeException("Not expecting: " + grouperInstallerPatchStatus);
        }
        
      }

      //lets see if it exists on the server
      File patchUntarredDir = downloadAndUnzipPatch(keyBase);
      
      //if no more patches
      if (patchUntarredDir == null) {
        System.out.println("");
        break OUTER;
      }
      
      //lets get the description:
      //  # will show up on screen so user knows what it is
      //  description = This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
      //
      //  # patches that this patch is dependant on (comma separated)
      //  dependencies = 
      //
      //  # low, medium, or high risk to applying the patch
      //  risk = low
      //
      //  # is this is a security patch (true or false)
      //  security = false
      //
      //  # if this patch requires restart of processes (true or false)
      //  requiresRestart = false
      Properties patchProperties = GrouperInstallerUtils.propertiesFromFile(new File(patchUntarredDir.getAbsoluteFile() + File.separator + keyBase + ".properties"));

      foundNewPatch = true;

      // check dependencies
      {
        String[] dependencies = GrouperInstallerUtils.splitTrim(patchProperties.getProperty("dependencies"), ",");
  
        boolean invalidDependency = false;
        for (String dependency : GrouperInstallerUtils.nonNull(dependencies, String.class)) {
          if (!this.patchesInstalled.contains(dependency)) {
            System.out.println("Cannot install patch " + keyBase + " since it is dependent on a patch which is not installed: " + dependency);
            invalidDependency = true;
          }
        }
        if (invalidDependency) {
          System.out.println("Press <enter> to continue");
          readFromStdIn("grouperInstaller.autorun.continueAfterPatchDependencyFails");
          continue OUTER;
        }
      }
      
      boolean securityRelated = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("security"), false);
      boolean requiresRestart = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("requiresRestart"), true);
      
      if (this.installAllPatches == null) {
        System.out.println("Would you like to install all patches (t|f)? [t]: ");
        this.installAllPatches = readFromStdInBoolean(true, "grouperInstaller.autorun.installAllPatches");
      }
      
      //print description
      System.out.println("Patch " + keyBase + " is " + patchProperties.getProperty("risk") + " risk, "
          + (securityRelated ? "is a security patch" : "is not a security patch"));
      System.out.println(patchProperties.getProperty("description"));
      boolean installPatch = false;
      if (!this.installAllPatches) {
        System.out.println("Would you like to install patch " + keyBase + " (t|f)? [t]: ");
      }
      installPatch = this.installAllPatches || readFromStdInBoolean(true, "grouperInstaller.autorun.installPatch");

      if (!patchExistingPropertiesFile.exists()) {
        GrouperInstallerUtils.fileCreate(patchExistingPropertiesFile);
      }
      
      //keep track that we skipped this in the patch properties file
      editPropertiesFile(patchExistingPropertiesFile, keyBase + ".date", 
          GrouperInstallerUtils.dateMinutesSecondsFormat.format(new Date()));

      if (!installPatch) {

        System.out.println("Would you like to be prompted about this patch next time? (t|f)? [t]: ");

        boolean temporary = readFromStdInBoolean(true, "grouperInstaller.autorun.promptAboutPatchNextTime");

        GrouperInstallerPatchStatus grouperInstallerPatchStatus = null;

        if (temporary) {
          grouperInstallerPatchStatus = GrouperInstallerPatchStatus.skippedTemporarily;
        } else {
          grouperInstallerPatchStatus = GrouperInstallerPatchStatus.skippedPermanently;
        }

        editPropertiesFile(patchExistingPropertiesFile, keyBase + ".state", 
            grouperInstallerPatchStatus.name());
        System.out.println("");
        continue OUTER;
      }

      if (requiresRestart && !this.grouperStopped) {
        System.out.println("This patch requires all processes that user Grouper to be stopped.\n  "
            + "Please stop these processes if they are running and press <enter> to continue...");
        this.grouperStopped = true;
        readFromStdIn("grouperInstaller.autorun.continueAfterPatchStopProcesses");
      }
      
      Map<String, String> patchDirToApplicationPath = new LinkedHashMap<String, String>();
      patchDirToApplicationPath.put("files", this.upgradeExistingApplicationDirectoryString);
      patchDirToApplicationPath.put("classes", this.upgradeExistingClassesDirectoryString);
      patchDirToApplicationPath.put("lib", this.upgradeExistingLibDirectoryString);

      boolean patchHasProblem = false;
      
      //we are installing this patch, lets see if the files are there...
      //this.upgradeExistingApplicationDirectoryString
      //patchUntarredDir
      File oldDir = new File(patchUntarredDir.getAbsolutePath() + File.separator + "old");
      File newDir = new File(patchUntarredDir.getAbsolutePath() + File.separator + "new");
      {

        for (String patchDir : patchDirToApplicationPath.keySet()) {
          
          String applicationPath = patchDirToApplicationPath.get(patchDir);

          File oldDirFiles = new File(oldDir.getAbsoluteFile() + File.separator + patchDir);
          File newDirFiles = new File(newDir.getAbsoluteFile() + File.separator + patchDir);
          
          if (oldDirFiles.exists() && oldDirFiles.isDirectory()) {
          
            // relative, e.g. WEB-INF/jsp/someFile.jsp
            Set<String> oldFileRelativePaths = GrouperInstallerUtils.fileDescendantRelativePaths(oldDirFiles);
            
            for (String oldFilePath : GrouperInstallerUtils.nonNull(oldFileRelativePaths)) {
              File oldFileInPatch = new File(oldDirFiles.getAbsolutePath() + File.separator + oldFilePath);
              File newFileInPatch = new File(newDirFiles.getAbsolutePath() + File.separator + oldFilePath);
              File oldFileInGrouper = new File(applicationPath + oldFilePath);
  
              if (!oldFileInPatch.exists() || !oldFileInPatch.isFile()) {
                throw new RuntimeException("Why does file not exist or not file??? " + oldFileInPatch.getAbsolutePath());
              }
              
              if (!oldFileInGrouper.exists() || !oldFileInGrouper.isFile() 
                  || (!GrouperInstallerUtils.contentEquals(oldFileInPatch, oldFileInGrouper)
                      //patch is already applied?  thats ok i guess
                      && !GrouperInstallerUtils.contentEquals(newFileInPatch, oldFileInGrouper))) {
                System.out.println("Cannot apply patch since this patch file:\n  " + oldFileInPatch.getAbsolutePath() 
                    + "\n  is not the same as what the patch expects:\n  " + oldFileInGrouper.getAbsolutePath());
                patchHasProblem = true;
              }
              
            }
          }
        }
  
      }

      //lets make sure that files which are new which dont have an old version do not exist in the application
      for (String patchDir : patchDirToApplicationPath.keySet()) {
        
        String applicationPath = patchDirToApplicationPath.get(patchDir);

        File newDirFiles = new File(newDir.getAbsoluteFile() + File.separator + patchDir);
        File oldDirFiles = new File(oldDir.getAbsoluteFile() + File.separator + patchDir);
        
        if (newDirFiles.exists() && newDirFiles.isDirectory()) {
        
          // relative, e.g. WEB-INF/jsp/someFile.jsp
          Set<String> newFileRelativePaths = GrouperInstallerUtils.fileDescendantRelativePaths(newDirFiles);

          Set<String> oldFileRelativePaths = (oldDirFiles.exists() && oldDirFiles.isDirectory()) ? 
              GrouperInstallerUtils.fileDescendantRelativePaths(oldDirFiles) : new HashSet<String>();

          for (String newFilePath : GrouperInstallerUtils.nonNull(newFileRelativePaths)) {

            File newFileInPatch = new File(newDirFiles.getAbsoluteFile() + File.separator + newFilePath);
            File oldFileInGrouper = new File(applicationPath + newFilePath);

            if (!newFileInPatch.isFile()) {
              continue;
            }
            
            //if there wasnt a corresponding old file path
            if (!oldFileRelativePaths.contains(newFilePath) && !GrouperInstallerUtils.contentEquals(oldFileInGrouper, newFileInPatch)) {

              //then the file shouldnt exist
              if (oldFileInGrouper.exists()) {
                System.out.println("Cannot apply patch since this patch file:\n  " + newFileInPatch.getAbsolutePath() 
                    + "\n  is supposed to be new, but it already exists:\n  " + oldFileInGrouper.getAbsolutePath());
                patchHasProblem = true;
              }
            }
          }
        }
      }

      if (patchHasProblem) {
        editPropertiesFile(patchExistingPropertiesFile, keyBase + ".state", 
            GrouperInstallerPatchStatus.error.name());

        continue OUTER;
      }

      //so far so good, all the old files are all good, install the patch
      for (String patchDir : patchDirToApplicationPath.keySet()) {
        
        String applicationPath = patchDirToApplicationPath.get(patchDir);

        File newDirFiles = new File(newDir.getAbsoluteFile() + File.separator + patchDir);
        
        if (newDirFiles.exists() && newDirFiles.isDirectory()) {
        
          // relative, e.g. WEB-INF/jsp/someFile.jsp
          Set<String> newFileRelativePaths = GrouperInstallerUtils.fileDescendantRelativePaths(newDirFiles);
          
          for (String newFilePath : GrouperInstallerUtils.nonNull(newFileRelativePaths)) {
            File newFileInPatch = new File(newDirFiles.getAbsolutePath() + File.separator + newFilePath);
            if (!newFileInPatch.isFile()) {
              continue;
            }
            File oldFileInGrouper = new File(applicationPath + newFilePath);
            if (!oldFileInGrouper.exists() && !oldFileInGrouper.getParentFile().exists()) {
              GrouperInstallerUtils.mkdirs(oldFileInGrouper.getParentFile());
            }
            System.out.println("Applying file: " + oldFileInGrouper.getAbsolutePath());
            GrouperInstallerUtils.copyFile(newFileInPatch, oldFileInGrouper, false);
          }
        }
      }
      
      this.patchesInstalled.add(keyBase);
      System.out.println("Patch successfully applied: " + keyBase);
      
      editPropertiesFile(patchExistingPropertiesFile, keyBase + ".state", 
          GrouperInstallerPatchStatus.applied.name());
      System.out.println("");
    }

    if (!foundNewPatch) {
      System.out.println("There are no new " + thisAppToUpgrade + " patches to install\n");
      return false;
    } 
    return true;
  }
  
  /**
   * get all patches
   * @param thisAppToUpgrade app to upgrade to check
   * @return next patch index
   */
  private int downloadPatches(AppToUpgrade thisAppToUpgrade) {

    if (thisAppToUpgrade == AppToUpgrade.CLIENT) {
      throw new RuntimeException("Cant install patches for " + thisAppToUpgrade);
    }
    
    String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);

    grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");

    Map<Integer, String> patchNumberToNameBase = new LinkedHashMap<Integer, String>();
    
    int nextPatchIndex = 0;
    
    OUTER: for (int i=0;i<1000;i++) {
      
      
      //grouper_v2_2_1_api_patch_0.state
      String keyBase = "grouper_v" + grouperVersion + "_" + thisAppToUpgrade.name().toLowerCase() + "_patch_" + i;
      System.out.println("\n################ Checking patch " + keyBase);

      patchNumberToNameBase.put(i, keyBase);

      //lets see if it exists on the server
      File patchUntarredDir = downloadAndUnzipPatch(keyBase);
      
      //if no more patches
      if (patchUntarredDir == null) {
        System.out.println("");
        break OUTER;
      }
      
      nextPatchIndex = i+1;
    }

    return nextPatchIndex;
    
  }
  
  /**
   * 
   * @param patchName e.g. grouper_v2_2_1_api_patch_0.tar.gz
   * @return the directory of the unzipped patch
   */
  public File downloadAndUnzipPatch(String patchName) {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }
    urlToDownload += "release/";
    
    //e.g. 2.2.2
    Matcher patchNameMatcher = patchNamePattern.matcher(patchName);
    if (!patchNameMatcher.matches()) {
      throw new RuntimeException("Invalid patch name: " + patchName);
    }
    
    //String grouperVersion = GrouperInstallerUtils.propertiesValue("grouper.version", true);
    String grouperVersion = patchNameMatcher.group(1) + "." + patchNameMatcher.group(2) + "." + patchNameMatcher.group(3);
    
    urlToDownload +=  grouperVersion + "/patches/" + patchName + ".tar.gz";

    File patchFile = new File(this.grouperTarballDirectoryString + patchName + ".tar.gz");
    
    if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.downloadPatches", true, false)) {

      boolean foundFile = downloadFile(urlToDownload, patchFile.getAbsolutePath(), true, "Patch doesnt exist yet (not an error): ", 
          "grouperInstaller.autorun.useLocalPatchIfExists");
      
      if (!foundFile) {

        //if we are doing test patches
        if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.useTestPatches", false, false)) {
          String testUrlToDownload = GrouperInstallerUtils.replace(urlToDownload, ".tar.gz", "_test.tar.gz");
          
          //its a test url, but download to the same file name
          foundFile = downloadFile(testUrlToDownload, patchFile.getAbsolutePath(), true, "Patch doesnt exist yet (not an error): ", 
              "grouperInstaller.autorun.useLocalPatchIfExists");
        }

        if (!foundFile) {
          return null;
        }
      }
    } else {
      if (!patchFile.exists()) {
        return null;
      }
    }
    
    //####################################
    //unzip/untar the patch file
    
    File unzippedFile = unzip(patchFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalPatchIfExists");
    File untarredDir = untar(unzippedFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalPatchIfExists");
    return untarredDir;
  }

  /**
   * 
   * @param branchName
   * @return the directory of the unzipped source repo
   */
  public File downloadAndUnzipGrouperSource(String branchName) {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.source.url", false);
    
    if (GrouperInstallerUtils.isBlank(urlToDownload)) {
      urlToDownload = "https://github.com/Internet2/grouper/archive/$BRANCH_NAME$.zip";
    }
    
    urlToDownload = GrouperInstallerUtils.replace(urlToDownload, "$BRANCH_NAME$", branchName);

    String fileToDownload = GrouperInstallerUtils.substringAfterLast(urlToDownload, "/");
    
    File sourceFile = new File(this.grouperTarballDirectoryString + fileToDownload);
    
    if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.downloadSource", true, false)) {

      downloadFile(urlToDownload, sourceFile.getAbsolutePath(), "grouperInstaller.autorun.createPatchDownloadSourceUseLocalIfExist");
      
    } else {
      if (!sourceFile.exists()) {
        throw new RuntimeException("Cant find grouper source");
      }
    }
    
    //####################################
    //unzip/untar the source file
    File unzippedDir = unzipFromZip(sourceFile.getAbsolutePath(), "grouperInstaller.autorun.createPatchDownloadSourceUseLocalIfExist");
    return unzippedDir;
  }

  /**
   * 
   * @param branchName
   * @return the directory of the unzipped source repo
   */
  public File downloadAndUnzipPspSource(String branchName) {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.pspSource.url", false);
    
    if (GrouperInstallerUtils.isBlank(urlToDownload)) {
      urlToDownload = "https://github.com/Internet2/grouper-psp/archive/$BRANCH_NAME$.zip";
    }
    
    urlToDownload = GrouperInstallerUtils.replace(urlToDownload, "$BRANCH_NAME$", branchName);

    String fileToDownload = GrouperInstallerUtils.substringAfterLast(urlToDownload, "/");
    
    File sourceFile = new File(this.grouperTarballDirectoryString + fileToDownload);
    
    if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.downloadSource", true, false)) {

      downloadFile(urlToDownload, sourceFile.getAbsolutePath(), "grouperInstaller.autorun.createPatchDownloadSourceUseLocalIfExist");
      
    } else {
      if (!sourceFile.exists()) {
        throw new RuntimeException("Cant find grouper psp source");
      }
    }
    
    //####################################
    //unzip/untar the source file
    File unzippedDir = unzipFromZip(sourceFile.getAbsolutePath(), "grouperInstaller.autorun.createPatchDownloadSourceUseLocalIfExist");
    return unzippedDir;
  }

  /**
   * 
   */
  public static enum GrouperInstallerPatchStatus {

    /**
     * patch was applied
     */
    applied, 
    
    /**
     * patch was removed
     */
    reverted, 
    
    /**
     * patch was skipped temporarily, prompt again
     */
    skippedTemporarily, 

    /**
     * patch had an error applying
     */
    error, 

    /**
     * patch was skipped permanently, dont prompt again
     */
    skippedPermanently;

    /**
     * 
     * @param string
     * @param exceptionIfNotFound
     * @param exceptionIfInvalid
     * @return the patch status
     */
    public static GrouperInstallerPatchStatus valueOfIgnoreCase(String string, boolean exceptionIfNotFound, boolean exceptionIfInvalid) {
      return GrouperInstallerUtils.enumValueOfIgnoreCase(GrouperInstallerPatchStatus.class, string, exceptionIfNotFound, exceptionIfInvalid);
    }
    
  }

  /**
   * patch status api
   */
  private void patchStatusApi() {
    this.patchStatus(AppToUpgrade.API);
  }


  /**
   * patch the api
   */
  private void patchApi() {
    this.downloadAndInstallPatches(AppToUpgrade.API);
  }

  /**
   * patch status ui
   */
  private void patchStatusUi() {
    this.patchStatus(AppToUpgrade.API);
    this.patchStatus(AppToUpgrade.UI);
  }

  /**
   * patch status ws
   */
  private void patchStatusWs() {
    this.patchStatus(AppToUpgrade.API);
    this.patchStatus(AppToUpgrade.WS);
  }

  /**
   * patch status psp
   */
  private void patchStatusPsp() {
    this.patchStatus(AppToUpgrade.API);
    this.patchStatus(AppToUpgrade.PSP);
  }


  /**
   * patch the client
   */
  private void patchUi() {
    this.downloadAndInstallPatches(AppToUpgrade.API);
    boolean patchesApplied = this.downloadAndInstallPatches(AppToUpgrade.UI);
    if (patchesApplied && (this.grouperInstallerMainFunction == GrouperInstallerMainFunction.patch 
        || this.grouperInstallerMainFunction == GrouperInstallerMainFunction.upgrade)) {
      System.out.print("Since patches were applied, you should delete files in your app server work directory,"
          + "\n  in tomcat it is named 'work'.  Hit <enter> to continue: ");
      readFromStdIn("grouperInstaller.autorun.continueAfterDeleteUiWorkDirectory");
    }
  }
  
  /**
   * patch the client
   */
  private void patchWs() {
    this.downloadAndInstallPatches(AppToUpgrade.API);
    boolean patchesApplied = this.downloadAndInstallPatches(AppToUpgrade.WS);
    if (patchesApplied && (this.grouperInstallerMainFunction == GrouperInstallerMainFunction.patch 
        || this.grouperInstallerMainFunction == GrouperInstallerMainFunction.upgrade)) {
      System.out.print("Since patches were applied, you should delete files in your app server work directory,"
          + "\n  in tomcat it is named 'work'.  Hit <enter> to continue: ");
      readFromStdIn("grouperInstaller.autorun.continueAfterDeleteWsWorkDirectory");
    }
  }
  
  /**
   * patch the client
   */
  private void patchPsp() {
    this.downloadAndInstallPatches(AppToUpgrade.API);
    this.downloadAndInstallPatches(AppToUpgrade.PSP);
  }

  /**
   * revert patch the client
   */
  private void patchRevertApi() {
    this.revertPatches(AppToUpgrade.API);
  }

  /**
   * revert patch the client
   */
  private void patchRevertUi() {
    this.revertPatches(AppToUpgrade.UI);
    boolean patchesReverted = this.revertPatches(AppToUpgrade.API);
    if (patchesReverted && (this.grouperInstallerMainFunction == GrouperInstallerMainFunction.patch 
        || this.grouperInstallerMainFunction == GrouperInstallerMainFunction.upgrade)) {
      System.out.print("Since patches were reverted, you should delete files in your app server work directory,"
          + "\n  in tomcat it is named 'work'.  Hit <enter> to continue: ");
      readFromStdIn("grouperInstaller.autorun.continueAfterDeleteUiWorkDirectory");
    }
  }
  
  /**
   * revert patch the client
   */
  private void patchRevertWs() {
    this.revertPatches(AppToUpgrade.WS);
    boolean patchesReverted = this.revertPatches(AppToUpgrade.API);
    if (patchesReverted && (this.grouperInstallerMainFunction == GrouperInstallerMainFunction.patch 
        || this.grouperInstallerMainFunction == GrouperInstallerMainFunction.upgrade)) {
      System.out.print("Since patches were reverted, you should delete files in your app server work directory,"
          + "\n  in tomcat it is named 'work'.  Hit <enter> to continue: ");
      readFromStdIn("grouperInstaller.autorun.continueAfterDeleteWsWorkDirectory");
    }
  }
  
  /**
   * revert patch the client
   */
  private void patchRevertPsp() {
    this.revertPatches(AppToUpgrade.PSP);
    this.revertPatches(AppToUpgrade.API);
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
      GrouperInstallerUtils.copyFile(newJarFile, existingJarFile, true);
      return true;
    }

    String existingJarFilePath = existingJarFile.getAbsolutePath();
    if (!GrouperInstallerUtils.filePathStartsWith(existingJarFilePath,this.upgradeExistingApplicationDirectoryString)) {
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
      
      GrouperInstallerUtils.copyFile(newJarFile, existingJarFile, true);
      
      return true;
    }
    
    if (printResultIfNotUpgrade) {
      System.out.println(existingJarFile.getName() + " is up to date");
    }
    return false;
  }

  /**
   * on an upgrade, compare a new file and an existing file and see if needs to be updated, and if so, update it
   * @param existingFile
   * @param newFile
   * @param printResultIfNotUpgrade
   * @param toDir if file not there, copy here.  If null, then go to upgrade existing lib directory string
   * @return true if upgraded, false if not
   */
  private boolean compareAndCopyFile(File existingFile, File newFile, boolean printResultIfNotUpgrade, File toDir) {
    
    if (toDir == null) {
      throw new RuntimeException("Which dir to copy to??? " + newFile + ", " + existingFile);
    }
    
    if (existingFile == null || !existingFile.exists()) {
      System.out.println(newFile.getName() + " is a new file and is being copied to the application dir: " + toDir.getAbsolutePath());
      existingFile = new File(toDir.getAbsoluteFile() + File.separator + newFile.getName());
      GrouperInstallerUtils.copyFile(newFile, existingFile, true);
      return true;
    }

    String existingFilePath = existingFile.getAbsolutePath();
    if (!GrouperInstallerUtils.filePathStartsWith(existingFilePath,this.upgradeExistingApplicationDirectoryString)) {
      throw new RuntimeException("Why does existing path not start with upgrade path??? " + existingFilePath + ", " + this.upgradeExistingApplicationDirectoryString);
    }
    
    String bakFileString = this.grouperBaseBakDir + existingFilePath.substring(this.upgradeExistingApplicationDirectoryString.length());
    File bakFile = new File(bakFileString);
    
    String existingChecksum = GrouperInstallerUtils.fileSha1(existingFile);
    String newChecksum = GrouperInstallerUtils.fileSha1(newFile);
    
    long existingSize = existingFile.length();
    long newSize = newFile.length();
    
    if (!GrouperInstallerUtils.equals(existingChecksum, newChecksum) || existingSize != newSize) {

      //make sure parents exist
      GrouperInstallerUtils.createParentDirectories(bakFile);
      
      System.out.println(existingFile.getName() + " had checksum " + existingChecksum + " and size " + existingSize + " bytes and is being upgraded to checksum "
          + newChecksum + " and size " + newSize + " bytes.\n  It is backed up to " + bakFile);

      GrouperInstallerUtils.fileMove(existingFile, bakFile);
      
      GrouperInstallerUtils.copyFile(newFile, existingFile, true);
      
      return true;
    }
    
    if (printResultIfNotUpgrade) {
      System.out.println(existingFile.getName() + " is up to date");
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
   * grouperUtf8.txt
   */
  private File grouperUtf8File;
  
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
   */
  private void mainInstallLogic() {
    
    //####################################
    //Find out what directory to install to.  This ends in a file separator
    this.grouperTarballDirectoryString = grouperInstallDirectory();

    //####################################
    //get default ip address
    System.out.print("Enter the default IP address for checking ports (just hit enter to accept the default unless on a machine with no network, might want to change to 127.0.0.1): [0.0.0.0]: ");
    this.defaultIpAddress = readFromStdIn("grouperInstaller.autorun.defaultIpAddressForPorts");
    
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
      useHsqldb = readFromStdInBoolean(true, "grouperInstaller.autorun.useBuiltInHsql");
    }

    if (!useHsqldb) {

      System.out.println("\n##################################\n");
      System.out.println("Example mysql URL: jdbc:mysql://localhost:3306/grouper");
      System.out.println("Example oracle URL: jdbc:oracle:thin:@server.school.edu:1521:sid");
      System.out.println("Example hsqldb URL: jdbc:hsqldb:hsql://localhost:9001/grouper");
      System.out.println("Example postgres URL: jdbc:postgresql://localhost:5432/database");
      System.out.println("Example mssql URL: jdbc:sqlserver://localhost:3280;databaseName=grouper");
      System.out.print("\nEnter the database URL [" + this.dbUrl + "]: ");
      String newDbUrl = readFromStdIn("grouperInstaller.autorun.dbUrl");
      if (!GrouperInstallerUtils.isBlank(newDbUrl)) {
        this.dbUrl = newDbUrl;
        if (newDbUrl.contains("postgresql") || newDbUrl.contains("sqlserver")) {
          System.out.println("Note: you need to change the search sql in the jdbc source in the grouperApi/conf/sources.xml... the change is in the comments in that file");
          for (int i=0;i<3;i++) {
            System.out.print("Ready to continue? (t|f)? [t] ");
            boolean shouldContinue = readFromStdInBoolean(true, "grouperInstaller.autorun.dbContinueAfterChangeSourcesXmlForPostgresSqlServer");
            if (shouldContinue) {
              break;
            }
          }
        }
      }
      System.out.print("Database user [" + this.dbUser + "]: ");
      String newDbUser = readFromStdIn("grouperInstaller.autorun.dbUser");
      if (!GrouperInstallerUtils.isBlank(newDbUser)) {
        this.dbUser = newDbUser;
      }
      System.out.print("Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [" 
          + GrouperInstallerUtils.defaultIfEmpty(this.dbPass, "<blank>") + "]: ");
      String newDbPass = readFromStdIn("grouperInstaller.autorun.dbPass");
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
    // patch the API
    this.upgradeExistingApplicationDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath());
    this.upgradeExistingClassesDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath())
        + "conf" + File.separator;
    this.upgradeExistingLibDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath())
        + "lib" + File.separator + "grouper" + File.separator;
    patchApi();
    
    //####################################
    //ask then init the DB
    initDb();
    addQuickstartSubjects();
    addQuickstartData();
    
    //####################################
    //download and configure ui
    downloadAndConfigureUi();

    //####################################
    //get ant
    downloadAndUnzipAnt();
    
    //####################################
    //look for or ask or download tomcat
    File tomcatDir = downloadTomcat();
    File unzippedTomcatFile = unzip(tomcatDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
    this.untarredTomcatDir = untar(unzippedTomcatFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
    
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
    //copy api patch level to ui
    File apiPatchStatusFile = new File(GrouperInstallerUtils.fileAddLastSlashIfNotExists(
        this.untarredApiDir.getAbsolutePath()) + "grouperPatchStatus.properties");
    File uiPatchStatusFile = new File(GrouperInstallerUtils.fileAddLastSlashIfNotExists(
        this.grouperUiBuildToDirName()) + "WEB-INF" + File.separator + "grouperPatchStatus.properties");
    System.out.println("Copying applied API patch status to UI:");
    System.out.println("  - from: "  + apiPatchStatusFile.getAbsolutePath());
    System.out.println("  - to: "  + uiPatchStatusFile.getAbsolutePath());
    GrouperInstallerMergePatchFiles.mergePatchFiles(
        apiPatchStatusFile, uiPatchStatusFile, true);

    //####################################
    // patch the ui
    this.upgradeExistingApplicationDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperUiBuildToDirName());
    this.upgradeExistingClassesDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperUiBuildToDirName())
        + "WEB-INF" + File.separator + "classes" + File.separator ;
    this.upgradeExistingLibDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperUiBuildToDirName())
        + "WEB-INF" + File.separator + "lib" + File.separator;
    this.patchUi();

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
    //copy api patch level to ui
    File wsPatchStatusFile = new File(GrouperInstallerUtils.fileAddLastSlashIfNotExists(
        this.grouperWsBuildToDirName()) + "WEB-INF" + File.separator + "grouperPatchStatus.properties");
    System.out.println("Copying applied API patch status to WS:");
    System.out.println("  - from: "  + apiPatchStatusFile.getAbsolutePath());
    System.out.println("  - to: "  + wsPatchStatusFile.getAbsolutePath());
    GrouperInstallerMergePatchFiles.mergePatchFiles(
        apiPatchStatusFile, wsPatchStatusFile, true);

    //####################################
    // patch the ws
    this.upgradeExistingApplicationDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperWsBuildToDirName());
    this.upgradeExistingClassesDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperWsBuildToDirName())
        + "WEB-INF" + File.separator + "classes" + File.separator ;
    this.upgradeExistingLibDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.grouperWsBuildToDirName())
        + "WEB-INF" + File.separator + "lib" + File.separator;

    this.patchWs();

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
    if (readFromStdInBoolean(true, "grouperInstaller.autorun.installPsp")) {
    	downloadAndBuildPsp();              
      GrouperInstallerUtils.copyDirectory(this.untarredPspDir, this.untarredApiDir);

      //####################################
      // patch the PSP
      this.upgradeExistingApplicationDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath());
      this.upgradeExistingClassesDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath())
          + "conf" + File.separator;
      this.upgradeExistingLibDirectoryString = GrouperInstallerUtils.fileAddLastSlashIfNotExists(this.untarredApiDir.getAbsolutePath())
          + "lib" + File.separator + "grouper" + File.separator;
      patchPsp();
            
    }

    reportOnConflictingJars();

    //#####################################
    //start the loader
    startLoader();
    
    //#####################################
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
    File unzippedPspFile = unzip(pspDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalPspDownloadTarEtc");
    this.untarredPspDir = untar(unzippedPspFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalPspDownloadTarEtc");
  }
  
  /** untarred psp dir file */
  private File untarredPspDir;
  
  /**
   * 
   */
  public void downloadAndUnzipAnt() {
    File antDir = downloadAnt();
    File unzippedAntFile = unzip(antDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
    this.untarredAntDir = untar(unzippedAntFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
  }

  /**
   * 
   */
  public void downloadAndUnzipMaven() {
    File mavenDir = downloadMaven();
    File unzippedMavenFile = unzip(mavenDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
    this.untarredMavenDir = untar(unzippedMavenFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");
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
    File unzippedWsFile = unzip(wsDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalWsDownloadTarEtc");
    this.untarredWsDir = untar(unzippedWsFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalWsDownloadTarEtc");

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
    File unzippedUiFile = unzip(uiDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalUiDownloadTarEtc");
    this.untarredUiDir = untar(unzippedUiFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalUiDownloadTarEtc");

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
    
    File unzippedApiFile = unzip(apiFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalApiDownloadTarEtc");
    File theUntarredApiDir = untar(unzippedApiFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalApiDownloadTarEtc");

    File theGrouperJar = new File(GrouperInstallerUtils.fileAddLastSlashIfNotExists(theUntarredApiDir.getAbsolutePath())
        + "dist" + File.separator + "lib" + File.separator + "grouper.jar");
    
    gshExcutableAndDos2Unix(theUntarredApiDir.getAbsolutePath() + File.separator + "bin" + File.separator);

    //these might be set from UI or WS
    if (this.untarredApiDir == null) {
      this.untarredApiDir = theUntarredApiDir;
    }
    
    if (this.grouperJar == null) {
      this.grouperJar = theGrouperJar;
    }
  }

  /**
   * @param binDirLocation which includes trailing slash
   */
  public void gshExcutableAndDos2Unix(String binDirLocation) {
    gshExcutableAndDos2Unix(binDirLocation, null);
  }
  /**
   * @param binDirLocation which includes trailing slash
   * @param specify if specifying location
   */
  public void gshExcutableAndDos2Unix(String binDirLocation, String specify) {
    //lts make sure gsh is executable and in unix format

    if (!GrouperInstallerUtils.isWindows()) {

      specify = GrouperInstallerUtils.trimToEmpty(specify);
      
      if (specify.length() > 0) {
        specify += " ";
      }
      
      System.out.print("Do you want to set " + specify + "gsh script to executable (t|f)? [t]: ");
      boolean setGshFile = readFromStdInBoolean(true, "grouperInstaller.autorun.setGshScriptsToExecutable");
      
      if (setGshFile) {
      
        binDirLocation = GrouperInstallerUtils.fileAddLastSlashIfNotExists(binDirLocation);
        
        List<String> commands = GrouperInstallerUtils.toList("chmod", "+x", 
            binDirLocation + "gsh.sh");
  
        System.out.println("Making sure gsh.sh is executable with command: " + convertCommandsIntoCommand(commands) + "\n");
  
        CommandResult commandResult = GrouperInstallerUtils.execCommand(
            GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
            new File(binDirLocation), null, true);
        
        if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
          System.out.println("stderr: " + commandResult.getErrorText());
        }
        if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
          System.out.println("stdout: " + commandResult.getOutputText());
        }

        if (new File(binDirLocation + "gsh").exists()) {
          commands = GrouperInstallerUtils.toList("chmod", "+x", 
              binDirLocation + "gsh");
    
          System.out.println("Making sure gsh is executable with command: " + convertCommandsIntoCommand(commands) + "\n");
    
          commandResult = GrouperInstallerUtils.execCommand(
              GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
              new File(binDirLocation), null, true);
          
          if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
            System.out.println("stderr: " + commandResult.getErrorText());
          }
          if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
            System.out.println("stdout: " + commandResult.getOutputText());
          }
        }
        System.out.print("Do you want to run dos2unix on gsh.sh (t|f)? [t]: ");
        setGshFile = readFromStdInBoolean(true, "grouperInstaller.autorun.dos2unixOnGsh");
        
        if (setGshFile) {
          
          commands = GrouperInstallerUtils.toList("dos2unix", 
              binDirLocation + "gsh.sh");
    
          System.out.println("Making sure gsh.sh is in unix format: " + convertCommandsIntoCommand(commands) + "\n");
          String error = null;
          try {
            commandResult = GrouperInstallerUtils.execCommand(
                GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                new File(binDirLocation), null, true);

            if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
              System.out.println("stderr: " + commandResult.getErrorText());
            }
            if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
              System.out.println("stdout: " + commandResult.getOutputText());
            }

            if (new File(binDirLocation + "gsh").exists()) {
              commands = GrouperInstallerUtils.toList("dos2unix", 
                  binDirLocation + "gsh");
        
              System.out.println("Making sure gsh is in unix format: " + convertCommandsIntoCommand(commands) + "\n");
              commandResult = GrouperInstallerUtils.execCommand(
                  GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                  new File(binDirLocation), null, true);
            }
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
    File unzippedClientFile = unzip(clientDir.getAbsolutePath(), "grouperInstaller.autorun.useLocalClientDownloadTarEtc");
    this.untarredClientDir = untar(unzippedClientFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalClientDownloadTarEtc");
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
    boolean setTomcatMemory = readFromStdInBoolean(true, "grouperInstaller.autorun.setTomcatMemoryLimit");
    
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
      boolean setTomcatFiles = readFromStdInBoolean(true, "grouperInstaller.autorun.setTomcatScriptsToExecutable");
      
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
              new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null, true);
          
          if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
            System.out.println("stderr: " + commandResult.getErrorText());
          }
          if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
            System.out.println("stdout: " + commandResult.getOutputText());
          }
        }
      }
      
      System.out.print("Do you want to run dos2unix on tomcat sh files (t|f)? [t]: ");
      boolean dos2unix = readFromStdInBoolean(true, "grouperInstaller.autorun.runDos2unixOnTomcatFiles");
      
      if (dos2unix) {
        
        try {
          
          for (String command : shFileNames) {
            
            List<String> commands = new ArrayList<String>();
            
            commands.add("dos2unix");
            commands.add(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin" + File.separator + command);
      
            System.out.println("Making tomcat file in unix format: " + convertCommandsIntoCommand(commands) + "\n");
      
            CommandResult commandResult = GrouperInstallerUtils.execCommand(
                GrouperInstallerUtils.toArray(commands, String.class), true, true, null, 
                new File(this.untarredTomcatDir.getAbsolutePath() + File.separator + "bin"), null, true);
            
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
    
    int originalShutdownPort = shutdownPort;
    
    //  /Server/Service/Connector <Connector port="8080" protocol="HTTP/1.1" 
    this.tomcatHttpPort = GrouperInstallerUtils.xpathEvaluateAttributeInt(serverXmlFile, "/Server/Service/Connector[@protocol='HTTP/1.1']", "port", -1);

    int originalTomcatHttpPort = this.tomcatHttpPort;

    // <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
    int jkPort = GrouperInstallerUtils.xpathEvaluateAttributeInt(serverXmlFile, "/Server/Service/Connector[@protocol='AJP/1.3']", "port", -1);

    int originalJkPort = jkPort;
    
    String portsCommaSeparated = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.tomcatPorts", false);
    if (!GrouperInstallerUtils.isBlank(portsCommaSeparated)) {
      
      String[] portsStrings = GrouperInstallerUtils.splitTrim(portsCommaSeparated, ",");
      
      if (portsStrings.length != 3) {
        throw new RuntimeException("Why is grouperInstaller.default.tomcatPorts from grouper.installer.properties not 3 ints comma separated? " + portsCommaSeparated);
      }
      
      this.tomcatHttpPort = GrouperInstallerUtils.intValue(portsStrings[0]);
      jkPort = GrouperInstallerUtils.intValue(portsStrings[1]);
      shutdownPort = GrouperInstallerUtils.intValue(portsStrings[2]);
      
    }
    
    while(true) {
      System.out.print("What ports do you want tomcat to run on (HTTP, JK, shutdown): [" + this.tomcatHttpPort + ", " + jkPort + ", " + shutdownPort + "]: ");
      
      String ports = readFromStdIn("grouperInstaller.autorun.tomcatPorts");
      
      if (GrouperInstallerUtils.isBlank(ports)) {
        if (this.tomcatHttpPort == originalTomcatHttpPort && jkPort == originalJkPort && shutdownPort == originalShutdownPort) {
          break;
        }
      } else {
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
      }
      
      if (!GrouperInstallerUtils.portAvailable(this.tomcatHttpPort, this.defaultIpAddress)) {
        System.out.print("The tomcat HTTP port is in use or unavailable: " + this.tomcatHttpPort + ", do you want to pick different ports? (t|f): ");
        boolean pickDifferentPorts = readFromStdInBoolean(null, "grouperInstaller.autorun.pickDifferentPortIfInUse");
        if (pickDifferentPorts) {
          continue;
        }
      }
      if (!GrouperInstallerUtils.portAvailable(jkPort, this.defaultIpAddress)) {
        System.out.print("The tomcat JK port is in use or unavailable: " + this.tomcatHttpPort + ", do you want to pick different ports? (t|f): ");
        boolean pickDifferentPorts = readFromStdInBoolean(null, "grouperInstaller.autorun.pickDifferentPortIfInUse");
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
      boolean assignUriEncoding = readFromStdInBoolean(defaultSetUriEncoding, "grouperInstaller.autorun.setUriEncodingToUtf8inServerXml");

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
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheExampleFile, true);
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheFile, true);

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
      GrouperInstallerUtils.copyFile(newEhcacheExampleFile, existingEhcacheExampleFile, true);

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
    
    downloadFile(urlToDownload, apiFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalApiDownloadTarEtc");

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
    
    downloadFile(urlToDownload, uiFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalUiDownloadTarEtc");

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
    
    downloadFile(urlToDownload, wsFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalWsDownloadTarEtc");

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
    
    downloadFile(urlToDownload, antFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");

    return antFile;
  }

  /**
   * 
   * @return the file of the directory of maven
   */
  private File downloadMaven() {
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }

    urlToDownload += "downloads/tools/apache-maven-3.2.5-bin.tar.gz";
    
    File mavenFile = new File(this.grouperTarballDirectoryString + "apache-maven-3.2.5-bin.tar.gz");
    
    downloadFile(urlToDownload, mavenFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");

    return mavenFile;
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
    
    downloadFile(urlToDownload, tomcatFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalToolsDownloadTarEtc");

    return tomcatFile;
  }

  /**
   * add quick start subjects
   */
  private void addQuickstartSubjects() {
    
    System.out.print("Do you want to add quickstart subjects to DB (t|f)? [t]: ");
    boolean addQuickstartSubjects = readFromStdInBoolean(true, "grouperInstaller.autorun.addQuickstartSubjectsToDb");
    
    if (addQuickstartSubjects) {

      String url = GrouperInstallerUtils.propertiesValue("download.server.url", true);
      
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += "release/" + this.version + "/subjects.sql";

      String subjectsSqlFileName = this.untarredApiDir.getParent() + File.separator + "subjects.sql";
      File subjectsSqlFile = new File(subjectsSqlFileName);
      downloadFile(url, subjectsSqlFileName, "grouperInstaller.autorun.useLocalApiDownloadTarEtc");

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
          this.untarredApiDir, null, true);
      
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
    boolean addQuickstartData = readFromStdInBoolean(true, "grouperInstaller.autorun.addQuickstartData");

    if (addQuickstartData) {
      String url = GrouperInstallerUtils.propertiesValue("download.server.url", true);
      
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += "release/" + this.version + "/quickstart.xml";
      String quickstartFileName = this.untarredApiDir.getParent() + File.separator + "quickstart.xml";
      
      File quickstartFile = new File(quickstartFileName);
      downloadFile(url, quickstartFileName, "grouperInstaller.autorun.useLocalApiDownloadTarEtc");

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
          this.untarredApiDir, null, true);
      
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
    boolean initdb = readFromStdInBoolean(null, "grouperInstaller.autorun.deleteAndInitDatabase");
    
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
          this.untarredApiDir, null, true);
      
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
    boolean startLoader = readFromStdInBoolean(false, "grouperInstaller.autorun.startGrouperDaemons");
    
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
              GrouperInstaller.this.grouperTarballDirectoryString + "grouperLoader", false);
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
          + (GrouperInstallerUtils.isWindows() ? "gsh.bat" : "gsh.sh");
      
      if (new File(gsh).exists()) {
        this.gshCommand = gsh;
        return gsh;
      }

      gsh = gshDir + "WEB-INF" + File.separator + "bin" + File.separator 
          + (GrouperInstallerUtils.isWindows() ? "gsh.bat" : "gsh.sh");

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
      if (GrouperInstallerUtils.getFullStackTrace(exception).contains(ClassNotFoundException.class.getName())) {
        System.out.println("Cannot check connection since driver is not in classpath of installer, this is fine but not sure if connection details work or not");
      } else {
        System.out.println("Error: could not connect to the database: ");
        exception.printStackTrace();
      }
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
    boolean startdb = readFromStdInBoolean(true, "grouperInstaller.autorun.startHsqlDatabase");
    if (startdb) {
      
      shutdownHsql();

      //get right port
      int port = hsqlPort();
      
      if (!GrouperInstallerUtils.portAvailable(port, this.defaultIpAddress)) {
        System.out.println("This port does not seem available, even after trying to stop the DB! " + port + "...");
        if (!shouldContinue("grouperInstaller.autorun.continueAfterPortNotAvailable")) {
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
              GrouperInstaller.this.grouperTarballDirectoryString + "hsqlDb", false, false);
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
    this.tomcatUiPath = readFromStdIn("grouperInstaller.autorun.urlPathForUi");
    
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
    
    File grouperWsBuildToDir = new File(this.grouperWsBuildToDirName());
    
    if (grouperWsBuildToDir.exists()) {

      boolean rebuildWs = true;
      
      boolean defaultRebuild = GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.default.ws.rebuildIfBuilt", true, false);
      System.out.print("The Grouper WS has been built in the past, do you want it rebuilt? (t|f) [" 
          + (defaultRebuild ? "t" : "f") + "]: ");
      rebuildWs = readFromStdInBoolean(defaultRebuild, "grouperInstaller.autorun.rebuildWsIfBuiltAlready");
  
      if (!rebuildWs) {
        return;
      }
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
        true, true, null, new File(this.untarredWsDir.getAbsolutePath() + File.separator + "grouper-ws"), null, true);
    
    if (!GrouperInstallerUtils.isBlank(commandResult.getErrorText())) {
      System.out.println("stderr: " + commandResult.getErrorText());
    }
    if (!GrouperInstallerUtils.isBlank(commandResult.getOutputText())) {
      System.out.println("stdout: " + commandResult.getOutputText());
    }

    if (isInstallNotUpgrade) {
      System.out.print("Do you want to set the log dir of WS (t|f)? [t]: ");
      boolean setLogDir = readFromStdInBoolean(true, "grouperInstaller.autorun.setWsLogDir");
      
      if (setLogDir) {
        
        ////set the log dir
        //C:\apps\grouperInstallerTest\grouper.ws-2.0.2\grouper-ws\build\dist\grouper-ws\WEB-INF\classes\log4j.properties
        //
        //${grouper.home}logs
  
        String defaultLogDir = this.untarredTomcatDir.getAbsolutePath() + File.separator + "logs" + File.separator + "grouperWs";
        System.out.print("Enter the WS log dir: [" + defaultLogDir + "]: ");
        String logDir = readFromStdIn("grouperInstaller.autorun.wsLogDir");
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
    this.tomcatWsPath = readFromStdIn("grouperInstaller.autorun.wsUrlPath");
    
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
    
    downloadFile(urlToDownload, clientFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalClientDownloadTarEtc");

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
        this.untarredApiDir, null, true);

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
    boolean runScript = readFromStdInBoolean(defaultBoolean, "grouperInstaller.autorun.runChangeLogTempToChangeLog");

    
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
       new File(this.gshCommand()).getParentFile(), null, true);

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
            
    CommandResult commandResult = GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(command, String.class), 
        true, true, null, this.untarredClientDir, null, true);

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
        new String[]{"-version"}, true);
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
          new String[]{"-version"}, true);
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
   * see if install, upgrade, patch, etc
   * @return true if install, or false if upgrade
   */
  private GrouperInstallerMainFunction grouperInstallerMainFunction() {

    if (GrouperInstallerUtils.propertiesValueBoolean("grouperInstaller.checkJavaVersion", true, false)) {
      String javaVersion = System.getProperty("java.version");
      if (javaVersion != null && !javaVersion.startsWith("1.6") && !javaVersion.startsWith("1.7")) {
        System.out.println("Error: Java should be version 6 or 7 (1.6 or 1.7), but is detected as: " + javaVersion);        
        System.out.println("Press <enter> to continue...");
        readFromStdIn("grouperInstaller.autorun.wrongJavaContinue");
      }
    }

    String input = null;
    String defaultAction = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.installOrUpgrade", false);
    defaultAction = GrouperInstallerUtils.defaultIfBlank(defaultAction, "install");
    for (int i=0;i<10;i++) {
      System.out.print("Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,\n"
          + "  'patch' an existing installation, or 'createPatch' for Grouper developers\n" 
          + "  (enter: 'install', 'upgrade', 'patch', 'createPatch' or blank for the default) ["
          + defaultAction + "]: ");
      input = readFromStdIn("grouperInstaller.autorun.actionEgInstallUpgradePatch");
      if (GrouperInstallerUtils.isBlank(input)) {
        input = defaultAction;
      }
      GrouperInstallerMainFunction theGrouperInstallerMainFunction = GrouperInstallerMainFunction.valueOfIgnoreCase(input, false);
      if (theGrouperInstallerMainFunction != null) {
        return theGrouperInstallerMainFunction;
      }
      System.out.println("Please enter 'install', 'upgrade', 'patch', or blank for default (which is " + defaultAction + ")");
    }
    throw new RuntimeException("Expecting 'install', 'upgrade', or 'patch' but was: '" + input + "'");
  }


  
  /**
   * 
   * @return install directory
   */
  private static String grouperInstallDirectory() {
    String grouperInstallDirectoryString;
    {
      File grouperInstallDirectoryFile = new File("");
      String defaultDirectory = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.installDirectory", false);
      System.out.print("Enter in the Grouper install directory (note: better if no spaces or special chars) [" 
          + (GrouperInstallerUtils.isBlank(defaultDirectory) ? grouperInstallDirectoryFile.getAbsolutePath() : defaultDirectory) + "]: ");
      String input = readFromStdIn("grouperInstaller.autorun.installDirectory");
      if (!GrouperInstallerUtils.isBlank(input)) {
        grouperInstallDirectoryFile = new File(input);
        if (!grouperInstallDirectoryFile.exists() || !grouperInstallDirectoryFile.isDirectory()) {
          System.out.println("Error: cant find directory: '" + input + "'");
          System.exit(1);
        }
      } else {
        if (!GrouperInstallerUtils.isBlank(defaultDirectory)) {
          grouperInstallDirectoryFile = new File(defaultDirectory);
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
      grouperInstallDirectoryString = readFromStdIn("grouperInstaller.autorun.tarballDirectory");
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
        upgradeExistingDirectoryString = readFromStdIn("grouperInstaller.autorun.grouperWhereInstalled");
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
   * @param action upgrade or patch
   * @return what we are upgrading
   */
  private AppToUpgrade grouperAppToUpgradeOrPatch(String action) {

    String appToUpgradeString = null;
    String defaultAppToUpgrade = GrouperInstallerUtils.propertiesValue("grouperInstaller.default.appToUpgrade", false);
    defaultAppToUpgrade = GrouperInstallerUtils.defaultIfBlank(defaultAppToUpgrade, AppToUpgrade.API.name().toLowerCase());
    
    for (int i=0;i<10;i++) {
      System.out.print("What do you want to " + action + "?  api, ui, ws, or psp? [" + defaultAppToUpgrade + "]: ");
      appToUpgradeString = readFromStdIn("grouperInstaller.autorun.appToUpgrade");
      if (GrouperInstallerUtils.isBlank(appToUpgradeString)) {
        appToUpgradeString = defaultAppToUpgrade;
      }
      try {
        return AppToUpgrade.valueOfIgnoreCase(appToUpgradeString, true);
      } catch (Exception e) {
        System.out.print("Error: please enter: 'api', 'ui', 'ws', 'psp', or blank for default [" + defaultAppToUpgrade + "]");
      }
    }
    throw new RuntimeException("Expecting api, ui, ws, or psp but was: '" + appToUpgradeString + "'");
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
    if (!file.exists()) {
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
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the directory where the files are (assuming has a single dir the same name as the archive)
   */
  private static File untar(final String fileName, final String autorunPropertiesKeyIfFileExistsUseLocal) {

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
      boolean useUnzippedFile = readFromStdInBoolean(true, autorunPropertiesKeyIfFileExistsUseLocal);
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
    

    final File[] result = new File[1];
    
    Runnable runnable = new Runnable() {

      public void run() {
        result[0] = untarHelper(fileName, autorunPropertiesKeyIfFileExistsUseLocal);
      }
    };

    GrouperInstallerUtils.threadRunWithStatusDots(runnable, true);

    return result[0];

  }

  /**
   * untar a file to a dir
   * @param fileName
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the directory where the files are (assuming has a single dir the same name as the archive)
   */
  private static File untarHelper(String fileName, String autorunPropertiesKeyIfFileExistsUseLocal) {
    TarArchiveInputStream tarArchiveInputStream = null;
    
    String untarredFileName = fileName.substring(0, fileName.length() - ".tar".length());
    
    //ant has -bin which is annoying
    if (untarredFileName.endsWith("-bin")) {
      untarredFileName = untarredFileName.substring(0, untarredFileName.length() - "-bin".length());
    }

    File untarredFile = new File(untarredFileName);
    String unzippedParent = untarredFile.getParentFile().getAbsolutePath();
    if (unzippedParent.endsWith(File.separator)) {
      unzippedParent = unzippedParent.substring(0, unzippedParent.length()-1);
    }

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
   * unzip a file, this is for .zip files
   * @param fileName
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the unzipped file
   */
  private static File unzipFromZip(final String fileName, final String autorunPropertiesKeyIfFileExistsUseLocal) {

    if (!fileName.endsWith(".zip")) {
      throw new RuntimeException("File doesnt end in .zip: " + fileName);
    }
    String unzippedFileName = fileName.substring(0, fileName.length() - ".zip".length());

    File unzippedDir = new File(unzippedFileName);

    if (unzippedDir.exists()) {
      
      System.out.print("Unzipped dir exists: " + unzippedFileName + ", use unzipped dir (t|f)? [t]: ");
      boolean useUnzippedFile = readFromStdInBoolean(true, autorunPropertiesKeyIfFileExistsUseLocal);
      if (useUnzippedFile) {
        return unzippedDir;
      }
      System.out.println("Deleting: " + unzippedFileName);
      GrouperInstallerUtils.deleteRecursiveDirectory(unzippedFileName);
    } else {
      if (!unzippedDir.mkdir()) {
        throw new RuntimeException("Cant make dir: " + unzippedDir.getAbsolutePath());
      }
    }

    System.out.println("Unzipping: " + fileName);

    final File[] result = new File[1];
    
    Runnable runnable = new Runnable() {

      public void run() {
        result[0] = unzipFromZipHelper(fileName, autorunPropertiesKeyIfFileExistsUseLocal);
      }
    };

    GrouperInstallerUtils.threadRunWithStatusDots(runnable, true);

    return result[0];

  }

  /**
   * unzip a file, this is for .zip files
   * @param fileName
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the unzipped file
   */
  private static File unzipFromZipHelper(String fileName, String autorunPropertiesKeyIfFileExistsUseLocal) {

    String unzippedFileName = fileName.substring(0, fileName.length() - ".zip".length());

    File unzippedDir = new File(unzippedFileName);

    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(fileName);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File(unzippedDir, entry.getName());
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          InputStream in = zipFile.getInputStream(entry);
          OutputStream out = new FileOutputStream(entryDestination);
          try {
            IOUtils.copy(in, out);
          } finally {
            GrouperInstallerUtils.closeQuietly(in);
            GrouperInstallerUtils.closeQuietly(out);
          }
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      GrouperInstallerUtils.closeQuietly(zipFile);
    }

    return unzippedDir;

  }

  /**
   * unzip a file to another file, this is for .gz files
   * @param fileName
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the unzipped file
   */
  private static File unzip(final String fileName, final String autorunPropertiesKeyIfFileExistsUseLocal) {

    if (!fileName.endsWith(".gz")) {
      throw new RuntimeException("File doesnt end in .gz: " + fileName);
    }
    String unzippedFileName = fileName.substring(0, fileName.length() - ".gz".length());
    
    File unzippedFile = new File(unzippedFileName);
    if (unzippedFile.exists()) {
      
      System.out.print("Unzipped file exists: " + unzippedFileName + ", use unzipped file (t|f)? [t]: ");
      boolean useUnzippedFile = readFromStdInBoolean(true, autorunPropertiesKeyIfFileExistsUseLocal);
      if (useUnzippedFile) {
        return unzippedFile;
      }
      System.out.println("Deleting: " + unzippedFileName);
      if (!unzippedFile.delete()) {
        throw new RuntimeException("Cant delete file: " + unzippedFileName);
      }
    }

    System.out.println("Unzipping: " + fileName);
    
    final File[] result = new File[1];
    
    Runnable runnable = new Runnable() {

      public void run() {
        result[0] = unzipHelper(fileName, autorunPropertiesKeyIfFileExistsUseLocal);
      }
    };

    GrouperInstallerUtils.threadRunWithStatusDots(runnable, true);

    return result[0];

  }

  /**
   * unzip a file to another file, this is for .gz files
   * @param fileName
   * @param autorunPropertiesKeyIfFileExistsUseLocal key in properties file to automatically fill in a value
   * @return the unzipped file
   */
  private static File unzipHelper(String fileName, String autorunPropertiesKeyIfFileExistsUseLocal) {

    String unzippedFileName = fileName.substring(0, fileName.length() - ".gz".length());
    File unzippedFile = new File(unzippedFileName);

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
    
    downloadFile(urlToDownload, pspFile.getAbsolutePath(), "grouperInstaller.autorun.useLocalPspDownloadTarEtc");

    return pspFile;
  }
  
  /**
   * upgrade the ws
   */
  private void upgradeWs() {
  
    this.upgradeApiPreRevertPatch();

    System.out.println("You need to revert all patches to upgrade");
    this.patchRevertWs();
    
    this.upgradeApiPostRevertPatch();
    
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
        this.grouperWsExamplePropertiesFile, null, "grouperInstaller.autorun.removeRedundantPropetiesFromGrouperWsProperties"
      );

    //patch it
    this.patchWs();

  }
  /**
   * get the patches available to apply that are not already applied
   * @param thisAppToUpgrade app to upgrade to check
   * @return true if up to date, false if not
   */
  private boolean patchStatus(AppToUpgrade thisAppToUpgrade) {
  
    if (thisAppToUpgrade == AppToUpgrade.CLIENT) {
      throw new RuntimeException("Cant get status on " + thisAppToUpgrade);
    }
    
    Properties patchesExistingProperties = patchExistingProperties();
    
    String grouperVersion = this.grouperVersionOfJar().toString();
    
    grouperVersion = GrouperInstallerUtils.replace(grouperVersion, ".", "_");
    
    boolean foundNewPatch = false;
    
    OUTER: for (int i=0;i<1000;i++) {
      
      //grouper_v2_2_1_api_patch_0.state
      String keyBase = "grouper_v" + grouperVersion + "_" + thisAppToUpgrade.name().toLowerCase() + "_patch_" + i;
      System.out.println("\n################ Checking patch " + keyBase);
      String key = keyBase + ".state";
  
      String value = patchesExistingProperties.getProperty(key);

      if (!GrouperInstallerUtils.isBlank(value)) {

        GrouperInstallerPatchStatus grouperInstallerPatchStatus = GrouperInstallerPatchStatus.valueOfIgnoreCase(value, true, true);

        switch (grouperInstallerPatchStatus) {
          case applied:
            
            System.out.println("Patch: " + keyBase + ": was applied on: " + patchesExistingProperties.getProperty(keyBase + ".date"));
            break;
            
          case skippedPermanently:
            
            foundNewPatch = true;
            System.out.println("Patch: " + keyBase + ": was skipped permanently on: " + patchesExistingProperties.getProperty(keyBase + ".date"));
            break;
            
          case skippedTemporarily:
  
            foundNewPatch = true;
            System.out.println("Patch: " + keyBase + ": was skipped termporarily on: " + patchesExistingProperties.getProperty(keyBase + ".date"));
            break;
  
          case reverted:
  
            foundNewPatch = true;
            System.out.println("Patch: " + keyBase + ": was reverted on: " + patchesExistingProperties.getProperty(keyBase + ".date"));
            break;
  
          case error:
  
            foundNewPatch = true;
            System.out.println("Patch: " + keyBase + ": had an error installing on: " + patchesExistingProperties.getProperty(keyBase + ".date"));
            break;
  
          default:
            throw new RuntimeException("Not expecting: " + grouperInstallerPatchStatus);
        }
        
      }
  
      //lets see if it exists on the server
      File patchUntarredDir = downloadAndUnzipPatch(keyBase);
      
      //if no more patches
      if (patchUntarredDir == null) {
        System.out.println("");
        break OUTER;
      }
      
      //lets get the description:
      //  # will show up on screen so user knows what it is
      //  description = This patch fixes GRP-1080: browse folders refresh button only works in chrome, not other browsers
      //
      //  # patches that this patch is dependant on (comma separated)
      //  dependencies = 
      //
      //  # low, medium, or high risk to applying the patch
      //  risk = low
      //
      //  # is this is a security patch (true or false)
      //  security = false
      //
      //  # if this patch requires restart of processes (true or false)
      //  requiresRestart = false
      Properties patchProperties = GrouperInstallerUtils.propertiesFromFile(new File(patchUntarredDir.getAbsoluteFile() + File.separator + keyBase + ".properties"));
  
      foundNewPatch = true;
  
      // check dependencies
      {
        String[] dependencies = GrouperInstallerUtils.splitTrim(patchProperties.getProperty("dependencies"), ",");
  
        for (String dependency : GrouperInstallerUtils.nonNull(dependencies, String.class)) {
          if (!this.patchesInstalled.contains(dependency)) {
            System.out.println("Cannot install patch " + keyBase + " since it is dependent on a patch which is not installed: " + dependency);
          }
        }
      }
      
      boolean securityRelated = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("security"), false);
      boolean requiresRestart = GrouperInstallerUtils.booleanValue(patchProperties.getProperty("requiresRestart"), true);
      
      //print description
      System.out.println("Patch " + keyBase + " is " + patchProperties.getProperty("risk") + " risk, "
          + (securityRelated ? "is a security patch" : "is not a security patch"));
      System.out.println("Patch " + keyBase + (requiresRestart ? " requires" : " does not require") + " a restart");
      System.out.println(patchProperties.getProperty("description") + "\n");      
    }
  
    if (!foundNewPatch) {
      System.out.println("There are no new " + thisAppToUpgrade + " patches to install");
      return true;
    }
    
    return false;
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

package edu.internet2.middleware.grouperInstaller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    File localFile = new File(localFileName);
    
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
    
    try {
      //mainLogic(args);
    
      GrouperInstaller grouperInstaller = new GrouperInstaller();
      
      grouperInstaller.dbUrl = "jdbc:hsqldb:hsql://localhost:9001/grouper";
      grouperInstaller.dbUser = "sa";
      grouperInstaller.dbPass = "";
      grouperInstaller.giDbUtils = new GiDbUtils(grouperInstaller.dbUrl, grouperInstaller.dbUser, grouperInstaller.dbPass);
      grouperInstaller.untarredDir = new File("c:/mchyzer/grouper/trunk/grouper-installer/grouper.apiBinary-2.1.0");
      grouperInstaller.addDriverJarToClasspath();
      
      grouperInstaller.startHsqlDb();

      grouperInstaller.checkDatabaseConnection();
      
      //CommandResult commandResult = GrouperInstallerUtils.execCommand("cmd /c cd");
      //String result = commandResult.getOutputText();
      //
      //System.out.println(result);

  
      //editPropertiesFile(new File("C:\\mchyzer\\grouper\\trunk\\grouper-installer\\grouper.apiBinary-2.1.0\\conf\\grouper.hibernate.properties"), 
      //    "hibernate.connection.password", "sdf");
      
    } finally {
      if (!GrouperInstallerUtils.retrieveExecutorService().isShutdown()) {
        GrouperInstallerUtils.retrieveExecutorService().shutdown();
      }
    }
    //if started hsql, then we need to exit since that thread will not stop
    System.exit(0);
  }

  /**
   * 
   * @param untarredDir
   */
  private void addDriverJarToClasspath() {
    String jarName = this.giDbUtils.builtinJarName();
    
    File driverJar = new File(this.untarredDir.getAbsolutePath() + File.separator + "lib" + File.separator + "jdbcSamples" + File.separator + jarName);
    GrouperInstallerUtils.classpathAddFile(driverJar);
  }

  /** db url */
  private String dbUrl;

  /** db user */
  private String dbUser;

  /** db pass */
  private String dbPass;

  /** untarred dir */
  private File untarredDir;
  
  /**
   * 
   * @param args
   */
  private void mainLogic(String[] args) {
    //####################################
    //Find out what directory to install to.  This ends in a file separator
    String grouperInstallDirectoryString = grouperInstallDirectory();
    
    //####################################
    //System.out.println("Grouper install directory is: " + grouperInstallDirectoryFile.getAbsolutePath());

    System.out.print("Enter the Grouper version to install [2.0.2]: ");
    String grouperVersion = readFromStdIn();
    
    if (GrouperInstallerUtils.isBlank(grouperVersion)) {
      grouperVersion = GrouperInstallerUtils.propertiesValue("default.version", true);
    }
    
    String urlToDownload = GrouperInstallerUtils.propertiesValue("download.server.url", true);
    
    if (!urlToDownload.endsWith("/")) {
      urlToDownload += "/";
    }

    String apiFileName = "grouper.apiBinary-" + grouperVersion + ".tar.gz";
    urlToDownload += grouperVersion + "/" + apiFileName;

    File apiFile = new File(grouperInstallDirectoryString + apiFileName);

    downloadFile(urlToDownload, apiFile.getAbsolutePath());
    
    //####################################
    //unzip/untar the api file
    File unzippedFile = unzip(apiFile.getAbsolutePath());
    this.untarredDir = untar(unzippedFile.getAbsolutePath());
    
    //####################################
    //ask about database
    this.dbUrl = "jdbc:hsqldb:hsql://localhost:9001/grouper";
    this.dbUser = "sa";
    this.dbPass = "";
    
    System.out.print("Do you want to use the default and included hsqldb database (t|f)? [t]: ");
    boolean useHsqldb = readFromStdInBoolean(true);

    if (!useHsqldb) {
      System.out.print("Database URL [jdbc:hsqldb:hsql://localhost:9001/grouper]: ");
      dbUrl = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(dbUrl)) {
        dbUrl = "jdbc:hsqldb:hsql://localhost:9001/grouper";
      }
      System.out.print("Database user [sa]: ");
      dbUser = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(dbUser)) {
        dbUser = "sa";
      }
      System.out.print("Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [<blank>]: ");
      dbPass = readFromStdIn();
      if (GrouperInstallerUtils.isBlank(dbPass)) {
        dbPass = "";
      }
    }
    
    this.giDbUtils = new GiDbUtils(this.dbUrl, this.dbUser, this.dbPass);
    
    //####################################
    //change the config file
    //get the config file    
    File grouperHibernatePropertiesFile = new File(untarredDir.getAbsoluteFile() + File.separator + "conf" 
        + File.separator + "grouper.hibernate.properties");
    
    //lets edit the three properties:
    System.out.println("Editing " + grouperHibernatePropertiesFile.getAbsolutePath() + ": ");
    editPropertiesFile(grouperHibernatePropertiesFile, "hibernate.connection.url", dbUrl);
    editPropertiesFile(grouperHibernatePropertiesFile, "hibernate.connection.username", dbUser);
    editPropertiesFile(grouperHibernatePropertiesFile, "hibernate.connection.password", dbPass);
    
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
    if (dbUrl.contains("hsqldb")) {
      //C:\mchyzer\grouper\trunk\grouper-installer\grouper.apiBinary-2.1.0
      startHsqlDb();
    }
    
    //####################################
    //check connection to database
    checkDatabaseConnection();
    
    //####################################
    //ask then init the DB
    System.out.print("Do you want to init the database (delete all existing grouper tables, add new ones (t|f)? ");
    boolean initdb = readFromStdInBoolean(null);
    
    if (initdb) {
      
    }
    
    
    //####################################
    //look for or ask or download tomcat
    
    //####################################
    //ask for tomcat port
    
    //####################################
    //look for ant, or download it
    
    //####################################
    //get UI
    
    //####################################
    //configure where API is and other options
    
    //####################################
    //build the UI
    
    //####################################
    //copy to webapps
    
    //####################################
    //bounce tomcat
    
    //####################################
    //tell user to go to url
    
    //####################################
    //download the ws
    
    //####################################
    //configure where api is
    
    //####################################
    //build
    
    //####################################
    //copy to tomcat
    
    //####################################
    //bounce tomcat
    
    //####################################
    //download client
    
    //####################################
    //configure the url
    
    //####################################
    //add a user to tomcat users (ask for password?)
    
    //####################################
    //run a client command
    
    
    
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
   * 
   * @param untarredDir
   */
  private void startHsqlDb() {
    System.out.print("Do you want this script to start the hsqldb database (note, it must not be running in able to start) (t|f)? ");
    boolean startdb = readFromStdInBoolean(true);
    if (startdb) {
      
      shutdownHsql();

      //TODO get right port
      final List<String> command = new ArrayList<String>();
      //command.add("cmd");
      //command.add("/a");
      //command.add("start");
      //command.add("/b");
      command.add(GrouperInstallerUtils.javaCommand());
      command.add("-cp");
      command.add(untarredDir + File.separator + "lib" + File.separator + "jdbcSamples" + File.separator 
          + "hsqldb.jar");
      //-cp lib\jdbcSamples\hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper
      command.addAll(GrouperInstallerUtils.splitTrimToList("org.hsqldb.Server -database.0 file:" + untarredDir + File.separator + "grouper -dbname.0 grouper", " "));
      
//        System.out.println("Starting DB with command: java -cp grouper.apiBinary-" + grouperVersion + File.separator 
//            + "lib" + File.separator + "jdbcSamples" + File.separator 
//            + "hsqldb.jar org.hsqldb.Server -database.0 file:grouper -dbname.0 grouper");

      System.out.println("Starting DB with command: " + GrouperInstallerUtils.join(command.iterator(), " "));
      
      //start in new thread
      GrouperInstallerUtils.retrieveExecutorService().submit(new Callable<Object>() {

        /**
         * 
         */
        @Override
        public Object call() throws Exception {
          GrouperInstallerUtils.execCommand(GrouperInstallerUtils.toArray(command, String.class), true, true);
          return null;
        }
      });
      
      
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

  private static void validateJavaVersion() {
    CommandResult commandResult = GrouperInstallerUtils.execCommand(
        GrouperInstallerUtils.javaCommand(), 
        new String[]{"-version"});
    String javaResult = commandResult.getOutputText();
    if (GrouperInstallerUtils.isBlank(javaResult)) {
      javaResult = commandResult.getErrorText();
    }
    if (!validJava(javaResult)) {
      throw new RuntimeException("Expecting Java 6+, but received: " + javaResult);
    }
  }

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
    
    String fileContents = GrouperInstallerUtils.readFileIntoString(file);
    
    String newline = newlineFromFile(fileContents);
    
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
   * based on file contents see what the newline type is
   * @param fileContents
   * @return the newline
   */
  private static String newlineFromFile(String fileContents) {
    String newline = "\n";
    if (fileContents.contains("\\r\\n")) {
      newline = "\\r\\n";
    }
    if (fileContents.contains("\\n\\r")) {
      newline = "\\n\\r";
    }
    if (fileContents.contains("\\r")) {
      newline = "\\r";
    }
    return newline;
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
    String unzippedFileName = fileName.substring(0, fileName.length() - ".tar".length());
    
    File unzippedFile = new File(unzippedFileName);
    if (unzippedFile.exists()) {
      System.out.println("Deleting: " + unzippedFileName);
      GrouperInstallerUtils.deleteRecursiveDirectory(unzippedFileName);
    }
    
    System.out.println("Expanding: " + fileName);
    
    String unzippedParent = unzippedFile.getParentFile().getAbsolutePath();
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
        if (size != content.length) {
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
    return unzippedFile;
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
      System.out.println("Deleting: " + unzippedFileName);
      if (!unzippedFile.delete()) {
        throw new RuntimeException("Cant delete file: " + unzippedFileName);
      }
    }

    System.out.println("Unzipping to: " + unzippedFileName);
    
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
  
}

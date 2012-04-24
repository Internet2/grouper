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
package edu.internet2.middleware.grouperClient.discovery;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * <pre>
 * testing group client discovery client:
 * 
 * test delete old files
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class DiscoveryClientTest extends TestCase {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new DiscoveryClientTest("testDeleteOldFiles"));
  }

  /**
   * 
   * @param name
   */
  public DiscoveryClientTest(String name) {
    super(name);
  }
  
  /** logger */
  private static final Log LOG = LogFactory.getLog(DiscoveryClientTest.class);

  /**
   * test delete old files
   */
  public void testDeleteOldFiles() {

    List<File> testingFiles = new ArrayList<File>();
    
    String cacheDirectoryName = GrouperClientUtils.cacheDirectoryName();
    
    try {
      
      //lets create one file (new temp file)
      //file_20120102_132414_123_sd43sdf.ext.discoverytmp or file_20120102_132414_123_sd43sdf.ext
      File newFile = new File(cacheDirectoryName + File.separator + "file_20120102_132414_123_sd43sdf.ext.discoverytmp");
      
      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      assertFalse(newFile.exists());
      
      DateFormat dateFormat = new SimpleDateFormat(DiscoveryClient.TEMP_FILE_DATE_FORMAT);
      
      newFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date()) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext.discoverytmp");

      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      assertTrue(newFile.exists());
      
      //get some buffer in file names
      GrouperClientCommonUtils.sleep(100);
      
      //lets try a post-tmp file
      newFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date()) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      File firstFile = newFile;
      
      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      assertTrue(newFile.exists());
      
      //get some buffer in file names
      GrouperClientCommonUtils.sleep(100);
      
      //lets try a few more
      newFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date()) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      
      File secondFile = newFile;
      
      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      //not old enough
      assertTrue(newFile.exists());
      assertTrue(firstFile.exists());
      
      //get some buffer in file names
      GrouperClientCommonUtils.sleep(100);
      
      //lets try a few more
      newFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date()) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      
      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      //not old enough
      assertTrue(newFile.exists());
      assertTrue(firstFile.exists());
      assertTrue(secondFile.exists());
      
      //get some buffer in file names
      GrouperClientCommonUtils.sleep(100);
      
      //lets try a one that is 31 minutes old
      Calendar thirtyOneMinutesAgo = new GregorianCalendar();
      thirtyOneMinutesAgo.setTimeInMillis(System.currentTimeMillis());
      thirtyOneMinutesAgo.add(Calendar.MINUTE, -31);
      
      newFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date(thirtyOneMinutesAgo.getTimeInMillis())) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      
      GrouperUtil.saveStringIntoFile(newFile, "hey");
      
      testingFiles.add(newFile);
      
      DiscoveryClient.cleanoutOldFiles();
      
      //good to go
      assertTrue(newFile.exists());
      
      //lets try a one that is 31 minutes old
      Calendar thirtyTwoMinutesAgo = new GregorianCalendar();
      thirtyTwoMinutesAgo.setTimeInMillis(System.currentTimeMillis());
      thirtyTwoMinutesAgo.add(Calendar.MINUTE, -32);
      
      File thirtyTwoMinutesAgoFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date(thirtyTwoMinutesAgo.getTimeInMillis())) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      
      GrouperUtil.saveStringIntoFile(thirtyTwoMinutesAgoFile, "hey");
      
      testingFiles.add(thirtyTwoMinutesAgoFile);
      
      //lets try a one that is 33 minutes old
      Calendar thirtyThreeMinutesAgo = new GregorianCalendar();
      thirtyThreeMinutesAgo.setTimeInMillis(System.currentTimeMillis());
      thirtyThreeMinutesAgo.add(Calendar.MINUTE, -33);
      
      File thirtyThreeMinutesAgoFile = new File(cacheDirectoryName + File.separator + "file_" + dateFormat.format(new Date(thirtyThreeMinutesAgo.getTimeInMillis())) 
          + "_" + GrouperClientCommonUtils.uniqueId() + ".ext");
      
      GrouperUtil.saveStringIntoFile(thirtyThreeMinutesAgoFile, "hey");
      
      testingFiles.add(thirtyThreeMinutesAgoFile);

      //see what the newest one is...
      File mostRecentFile = DiscoveryClient.mostRecentFileFromFileSystem("file.ext");
      
      assertEquals(mostRecentFile.getAbsolutePath(), mostRecentFile.getAbsolutePath(), firstFile.getAbsolutePath());
      
      DiscoveryClient.cleanoutOldFiles();
      
      //good to go
      assertTrue(newFile.exists());
      assertFalse(thirtyTwoMinutesAgoFile.exists());
      assertFalse(thirtyThreeMinutesAgoFile.exists());
      
      
      
    } finally {
      for (File file : testingFiles) {
        try {
          GrouperClientCommonUtils.deleteFile(file);
        } catch (Exception e) {
          LOG.error("Cant delete file: " + file.getAbsolutePath(), e);
        }
      }
    }
    
  }


}

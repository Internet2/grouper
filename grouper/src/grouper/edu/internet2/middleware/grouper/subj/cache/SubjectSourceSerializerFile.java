/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.config.SubjectConfig;


/**
 * store and retrieve the 
 */
public class SubjectSourceSerializerFile extends SubjectSourceSerializer {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(SubjectSourceSerializerFile.class);

  /**
   * 
   */
  public SubjectSourceSerializerFile() {
    super();
  }

  /**
   * prefix of cache files
   */
  final static String SUBJECT_SOURCE_FILE_PREFIX = "grouperSubjectCache_";

  /**
   * get the list of cache files by date oldest to newest
   * @param debugMap
   * @return the list of files
   */
  private File[] cacheFiles(Map<String, Object> debugMap) {
    String directoryName = SubjectConfig.retrieveConfig().propertyValueString("subject.cache.serializer.directory");
    
    if (StringUtils.isBlank(directoryName)) {
      if (debugMap != null) {
        debugMap.put("serializerDirBlank", true);
      }
      return null;
    }
    if (debugMap != null) {
      debugMap.put("serializerDir", directoryName);
    }
    File directory = new File(directoryName);
    
    if (!directory.exists() || !directory.isDirectory()) {
      if (debugMap != null) {
        debugMap.put("directoryNotFound", true);
      }
      throw new RuntimeException("Cant find subject cache dir: '" + directoryName + "'");
    }
    
    //ok we have the directory
    File[] files = directory.listFiles(serializerFileFilter());

    if (debugMap != null) {
      debugMap.put("cacheFilesFound", GrouperUtil.length(files));
    }

    if (GrouperUtil.length(files) == 0) {
      
      LOG.debug("No cache files found");
      return null;
      
    }

    Arrays.sort(files, new Comparator<File>() {
      public int compare(File f1, File f2) {
          return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
      } });

    List<File> sortedFilesByDate = new ArrayList<File>();

    for (File file : files) {
      sortedFilesByDate.add(file);
    }
    return files;
  }

  /**
   * @return the filter
   */
  public static FilenameFilter serializerFileFilter() {
    return new FilenameFilter() {
      
      public boolean accept(File dir, String name) {
        return name != null && name.startsWith(SUBJECT_SOURCE_FILE_PREFIX);
      }
    };
  }
  
  /**
   * if not configured to use, or misconfigured, then return null
   * @see SubjectSourceSerializer#retrieveLatestSubjectCache(long, Map)
   */
  @Override
  public SubjectSourceCacheBean retrieveLatestSubjectCache(long newerThanMillis, Map<String, Object> debugMap) {
    
    MultiKey multiKey = retrieveLatestSubjectCacheHelper(newerThanMillis, debugMap);
    
    if (debugMap != null) {
      debugMap.put("foundFile", multiKey != null);
    }
    
    if (multiKey != null) {
      return (SubjectSourceCacheBean)multiKey.getKey(0);
    }
    
    return null;
  }

  /**
   * if not configured to use, or misconfigured, then return null
   * @see SubjectSourceSerializer#retrieveLatestSubjectCache(long, Map)
   * @param newerThanMillis
   * @param debugMap if not null add debug info
   * @return multikey of cache and file
   */
  private MultiKey retrieveLatestSubjectCacheHelper(long newerThanMillis, Map<String, Object> debugMap) {

    if (debugMap != null) {
      debugMap.put("newerThanMillis", newerThanMillis < 10 ? newerThanMillis : new Date(newerThanMillis));
    }
    

    File[] files = cacheFiles(debugMap);
    
    if (GrouperUtil.length(files) == 0) {
      return null;
    }
    
    for (int i=files.length -1; i>=0; i--) {
      File file = files[i];
      
      if (file.lastModified() < newerThanMillis) {
        if (debugMap != null) {
          debugMap.put("cacheTooOld_" + file.getAbsoluteFile(), true);
        }

        continue;
      }
      
      // read the object from file
      // save the object to file
      FileInputStream fileInputStream = null;
      ObjectInputStream objectInputStream = null;
      SubjectSourceCacheBean subjectSourceCacheBean = null;

      try {
        fileInputStream = new FileInputStream(file);
        objectInputStream = new ObjectInputStream(fileInputStream);
        subjectSourceCacheBean = (SubjectSourceCacheBean) objectInputStream.readObject();
      } catch (Exception ex) {
        final String problem = "Cant deserialize subject cache, this might be ok if the file was written during a shutdown: " + file.getAbsolutePath();
        LOG.warn(problem, ex);
        if (debugMap != null) {
          debugMap.put("problemWithFile_" + file.getAbsolutePath(), "cantDeserialize");
        }

      } finally {
        GrouperUtil.closeQuietly(objectInputStream);
        GrouperUtil.closeQuietly(fileInputStream);
      }
      if (subjectSourceCacheBean != null) {
        if (debugMap != null) {
          debugMap.put("latestCache", file.getAbsoluteFile());
        }
        return new MultiKey(subjectSourceCacheBean, file);
      }
      
    }
    
    return null;
  }

  /**
   * @see SubjectSourceSerializer#storeSubjectCache(SubjectSourceCacheBean, Map)
   */
  @Override
  public void storeSubjectCache(SubjectSourceCacheBean subjectSourceCacheBean, Map<String, Object> debugMap) {
    
    String directoryName = SubjectConfig.retrieveConfig().propertyValueString("subject.cache.serializer.directory");
    
    if (StringUtils.isBlank(directoryName)) {
      if (debugMap != null) {
        debugMap.put("blankSerializeDir", true);
      }
      return;
    }
    if (debugMap != null) {
      debugMap.put("serializeDir", directoryName);
    }
    File directory = new File(directoryName);
    
    if (!directory.exists() || !directory.isDirectory()) {
      if (debugMap != null) {
        debugMap.put("dirNotExist", true);
      }
      throw new RuntimeException("Cant find subject cache dir: '" + directoryName + "'");
    }

    String TIMESTAMP_FILE_FORMAT = "yyyy_MM_dd__HH_mm_ss_SSS";

    SimpleDateFormat timestampFileFormat = new SimpleDateFormat(TIMESTAMP_FILE_FORMAT);

    File cacheFile = null;
    
    for (int i=0;i<10;i++) {
      cacheFile = new File(GrouperUtil.stripLastSlashIfExists(directory.getAbsolutePath()) + File.separatorChar + SUBJECT_SOURCE_FILE_PREFIX + timestampFileFormat.format(new Date()));
      if (!cacheFile.exists()) {
        break;
      }
      cacheFile = null;
    }

    if (debugMap != null) {
      debugMap.put("usingCacheFile", cacheFile == null ? null : cacheFile.getAbsoluteFile());
    }

    if (cacheFile == null) {
      throw new RuntimeException("Cant find available file in " + SUBJECT_SOURCE_FILE_PREFIX);
    }
    
    FileOutputStream fileOutputStream = null;
    ObjectOutputStream objectOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(cacheFile);
      objectOutputStream = new ObjectOutputStream(fileOutputStream);
      objectOutputStream.writeObject(subjectSourceCacheBean);
    } catch (Exception ex) {
      if (debugMap != null) {
        debugMap.put("error", true);
      }
     throw new RuntimeException("Cant write subject cache to file: '" + directoryName + "'", ex);
    } finally {
      GrouperUtil.closeQuietly(objectOutputStream);
      GrouperUtil.closeQuietly(fileOutputStream);
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.subj.cache.SubjectSourceSerializer#cleanupOldSubjectCaches(long, Map)
   */
  @Override
  public int cleanupOldSubjectCaches(long keepNewestIfNewerThanMillis, Map<String, Object> debugMap) {
    File[] files = cacheFiles(debugMap);
    
    if (GrouperUtil.length(files) == 0) {
      if (debugMap != null) {
        debugMap.put("noCacheFiles", true);
      }
      return 0;
    }

    // get the latest file
    File latestCache = null;
    MultiKey multiKey = retrieveLatestSubjectCacheHelper(keepNewestIfNewerThanMillis, debugMap);
    
    if (multiKey != null) {
      latestCache = (File)multiKey.getKey(1);
    }

    int fileDeleteCount = 0;
    
    for (int i=files.length -1; i>=0; i--) {
      File file = files[i];
      
      if (latestCache == null || !file.equals(latestCache)) {
        try {
          GrouperUtil.deleteFile(file);
          if (debugMap != null) {
            debugMap.put("deletedFile_" + file.getAbsolutePath(), true);
          }
          fileDeleteCount++;
        } catch (Exception e) {
          LOG.error("Cant delete subject cache file: " + file.getAbsolutePath());
        }
      }
    }
    
    return fileDeleteCount;
  }

}

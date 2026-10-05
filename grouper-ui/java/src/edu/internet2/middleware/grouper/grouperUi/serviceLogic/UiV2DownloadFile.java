package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.file.GrouperFileDao;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * GRP-7438: let any ajax action hand the user a grouper_file row to download.
 *
 * <p>An ajax response cannot save a file by itself, so this is two steps:</p>
 * <ol>
 *   <li>The ajax action calls {@link #newDownloadAction(String)}.  That puts a random token
 *   in the HTTP session pointing at the grouper_file id and the logged in subject, and returns
 *   a screen action that sets location.href to the download url.</li>
 *   <li>The browser does a plain GET of {@link #download(HttpServletRequest, HttpServletResponse)},
 *   which streams the file with Content-Disposition: attachment.  The page does not navigate
 *   away since the response is an attachment.</li>
 * </ol>
 *
 * <p>Security: the download only serves tokens that are in the current session and were
 * registered for the same logged in subject, so a url copied to someone else is useless.  The
 * caller is responsible for only registering files the user may see.</p>
 */
public class UiV2DownloadFile {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2DownloadFile.class);

  /** session attribute holding token to download entry */
  static final String SESSION_ATTRIBUTE = "grouperUiDownloadFileTokens";

  /** keep at most this many tokens per session, oldest are dropped */
  static final int MAX_TOKENS_PER_SESSION = 100;

  /** random for tokens */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * what a token points to.  Serializable since it lives in the http session
   */
  static class DownloadEntry implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    /** grouper_file id */
    private final String grouperFileId;

    /** source id of the subject the token was registered for */
    private final String subjectSourceId;

    /** subject id of the subject the token was registered for */
    private final String subjectId;

    /**
     * @param grouperFileId
     * @param subjectSourceId
     * @param subjectId
     */
    DownloadEntry(String grouperFileId, String subjectSourceId, String subjectId) {
      this.grouperFileId = grouperFileId;
      this.subjectSourceId = subjectSourceId;
      this.subjectId = subjectId;
    }

    /**
     * @return grouper_file id
     */
    String getGrouperFileId() {
      return this.grouperFileId;
    }

    /**
     * @param subject
     * @return true if this entry was registered for this subject
     */
    boolean isForSubject(Subject subject) {
      return subject != null && StringUtils.equals(this.subjectSourceId, subject.getSourceId())
          && StringUtils.equals(this.subjectId, subject.getId());
    }
  }

  /**
   * register a grouper_file for the logged in user to download, and return a screen action
   * that starts the download.  Call from any ajax action, e.g.
   * guiResponseJs.addAction(UiV2DownloadFile.newDownloadAction(grouperFileId))
   * @param grouperFileId
   * @return the screen action
   */
  public static GuiScreenAction newDownloadAction(String grouperFileId) {
    return GuiScreenAction.newScript("location.href = '" + GrouperUiUtils.escapeJavascript(downloadUrl(grouperFileId), true) + "'");
  }

  /**
   * register a grouper_file for the logged in user to download, and return the relative url
   * to download it, e.g. for a link in a message
   * @param grouperFileId
   * @return the url, e.g. ../app/UiV2DownloadFile.download?token=abc123
   */
  public static String downloadUrl(String grouperFileId) {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    String token = registerDownload(request.getSession(), grouperFileId, loggedInSubject);
    return "../app/" + UiV2DownloadFile.class.getSimpleName() + ".download?token=" + token;
  }

  /**
   * put a token in the session for this file and subject
   * @param httpSession
   * @param grouperFileId
   * @param subject logged in subject
   * @return the token
   */
  static String registerDownload(HttpSession httpSession, String grouperFileId, Subject subject) {
    if (StringUtils.isBlank(grouperFileId)) {
      throw new RuntimeException("grouperFileId is required");
    }
    if (subject == null) {
      throw new RuntimeException("subject is required");
    }

    // 128 random bits as hex
    byte[] randomBytes = new byte[16];
    SECURE_RANDOM.nextBytes(randomBytes);
    StringBuilder token = new StringBuilder();
    for (byte randomByte : randomBytes) {
      token.append(String.format("%02x", randomByte));
    }

    Map<String, DownloadEntry> tokenToEntry = retrieveTokenMap(httpSession);
    synchronized (tokenToEntry) {
      tokenToEntry.put(token.toString(), new DownloadEntry(grouperFileId, subject.getSourceId(), subject.getId()));

      // drop the oldest so a long session does not grow without bound
      Iterator<String> iterator = tokenToEntry.keySet().iterator();
      while (tokenToEntry.size() > MAX_TOKENS_PER_SESSION && iterator.hasNext()) {
        iterator.next();
        iterator.remove();
      }
    }
    // set again so session replication sees the change
    httpSession.setAttribute(SESSION_ATTRIBUTE, tokenToEntry);
    return token.toString();
  }

  /**
   * look up the file id for a token in this session, for this subject
   * @param httpSession
   * @param token
   * @param subject logged in subject
   * @return the grouper_file id or null if the token is not in this session or is for another subject
   */
  static String retrieveGrouperFileId(HttpSession httpSession, String token, Subject subject) {
    if (httpSession == null || StringUtils.isBlank(token) || subject == null) {
      return null;
    }
    Map<String, DownloadEntry> tokenToEntry = retrieveTokenMap(httpSession);
    DownloadEntry downloadEntry = null;
    synchronized (tokenToEntry) {
      downloadEntry = tokenToEntry.get(token);
    }
    if (downloadEntry == null || !downloadEntry.isForSubject(subject)) {
      return null;
    }
    return downloadEntry.getGrouperFileId();
  }

  /**
   * get or create the token map in the session
   * @param httpSession
   * @return the map
   */
  @SuppressWarnings("unchecked")
  private static Map<String, DownloadEntry> retrieveTokenMap(HttpSession httpSession) {
    synchronized (httpSession) {
      Map<String, DownloadEntry> tokenToEntry = (Map<String, DownloadEntry>)httpSession.getAttribute(SESSION_ATTRIBUTE);
      if (tokenToEntry == null) {
        // insertion order so the oldest can be dropped
        tokenToEntry = new LinkedHashMap<String, DownloadEntry>();
        httpSession.setAttribute(SESSION_ATTRIBUTE, tokenToEntry);
      }
      return tokenToEntry;
    }
  }

  /**
   * plain GET from the browser (see newDownloadAction) to download the file for a token
   * @param request
   * @param response
   */
  public void download(HttpServletRequest request, HttpServletResponse response) {

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    String token = request.getParameter("token");

    String grouperFileId = retrieveGrouperFileId(request.getSession(false), token, loggedInSubject);

    GrouperFile grouperFile = grouperFileId == null ? null
        : GrouperFileDao.findById(grouperFileId, false);

    try {
      if (grouperFile == null) {
        // dont say whether the token or the file is the problem
        LOG.warn("Download not found for subject: " + GrouperUtil.subjectToString(loggedInSubject)
            + ", token found: " + (grouperFileId != null));
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found or download expired");
      } else {
        writeFile(grouperFile, request.getHeader("Accept-Encoding"), response);
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Error downloading file: " + grouperFileId, ioe);
    }

    // response is written, dont print the ajax json
    throw new ControllerDone();
  }

  /**
   * write the file to the response as an attachment, gzipped if the browser accepts it
   * @param grouperFile
   * @param acceptEncoding Accept-Encoding request header
   * @param response
   * @throws IOException
   */
  static void writeFile(GrouperFile grouperFile, String acceptEncoding, HttpServletResponse response) throws IOException {

    String fileName = grouperFile.getFileName();
    // GRP-7446: binary (blob) or text (as UTF-8)
    byte[] contents = grouperFile.retrieveBytes();

    response.setContentType(contentType(fileName));
    response.setHeader("Content-Disposition", contentDisposition(fileName));
    // private data, and dont let the browser guess a type that could render
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("X-Content-Type-Options", "nosniff");

    // dont gzip a file that is already compressed (zip, xlsx, png, etc), it does not get smaller
    boolean gzip = StringUtils.defaultString(acceptEncoding).toLowerCase().contains("gzip") && !isCompressed(fileName);

    OutputStream outputStream = response.getOutputStream();
    if (gzip) {
      // browser decompresses transparently and saves the plain file
      response.setHeader("Content-Encoding", "gzip");
      response.setHeader("Vary", "Accept-Encoding");
      GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
      gzipOutputStream.write(contents);
      gzipOutputStream.finish();
    } else {
      response.setContentLength(contents.length);
      outputStream.write(contents);
    }
    outputStream.flush();
  }

  /**
   * content type from the file extension.  Unknown types are octet-stream
   * @param fileName
   * @return the content type
   */
  static String contentType(String fileName) {
    String extension = StringUtils.defaultString(StringUtils.substringAfterLast(fileName, ".")).toLowerCase();
    if ("csv".equals(extension)) {
      return "text/csv; charset=UTF-8";
    }
    if ("txt".equals(extension) || "log".equals(extension)) {
      return "text/plain; charset=UTF-8";
    }
    if ("json".equals(extension)) {
      return "application/json; charset=UTF-8";
    }
    if ("xml".equals(extension)) {
      return "application/xml; charset=UTF-8";
    }
    // GRP-7446 binary types
    if ("zip".equals(extension)) {
      return "application/zip";
    }
    if ("gz".equals(extension)) {
      return "application/gzip";
    }
    if ("xlsx".equals(extension)) {
      return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
    if ("docx".equals(extension)) {
      return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
    if ("pdf".equals(extension)) {
      return "application/pdf";
    }
    if ("png".equals(extension)) {
      return "image/png";
    }
    if ("jpg".equals(extension) || "jpeg".equals(extension)) {
      return "image/jpeg";
    }
    return "application/octet-stream";
  }

  /**
   * file types that are already compressed, so gzipping the download would not help
   */
  private static final Set<String> COMPRESSED_EXTENSIONS = GrouperUtil.toSet(
      "zip", "gz", "tgz", "7z", "xlsx", "docx", "pptx", "pdf", "png", "jpg", "jpeg", "gif");

  /**
   * GRP-7446: whether the file is already compressed (by extension), so it is not gzipped on download
   * @param fileName
   * @return true if compressed
   */
  static boolean isCompressed(String fileName) {
    String extension = StringUtils.defaultString(StringUtils.substringAfterLast(fileName, ".")).toLowerCase();
    return COMPRESSED_EXTENSIONS.contains(extension);
  }

  /**
   * attachment header with an ascii filename for old browsers and a utf-8 filename* (rfc 5987)
   * @param fileName
   * @return the header value
   */
  static String contentDisposition(String fileName) {
    // ascii fallback: replace non printable ascii, quotes, and backslashes
    StringBuilder asciiFileName = new StringBuilder();
    for (char theChar : StringUtils.defaultString(fileName).toCharArray()) {
      if (theChar < 0x20 || theChar > 0x7e || theChar == '"' || theChar == '\\') {
        asciiFileName.append('_');
      } else {
        asciiFileName.append(theChar);
      }
    }
    String encodedFileName = null;
    try {
      // URLEncoder does form encoding, rfc 5987 wants %20 for space
      encodedFileName = URLEncoder.encode(StringUtils.defaultString(fileName), "UTF-8").replace("+", "%20");
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName;
  }

}

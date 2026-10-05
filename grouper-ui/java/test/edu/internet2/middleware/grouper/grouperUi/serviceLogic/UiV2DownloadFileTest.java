package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import edu.internet2.middleware.grouper.file.GrouperFile;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import junit.textui.TestRunner;

/**
 * GRP-7438: download of a grouper_file from any ajax action through a session token.
 * Uses in-memory proxies for the http session and response so no servlet container is needed.
 */
public class UiV2DownloadFileTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2DownloadFileTest("testTokenOnlyWorksInSameSessionForSameSubject"));
  }

  /**
   *
   */
  public UiV2DownloadFileTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2DownloadFileTest(String name) {
    super(name);
  }

  /**
   * @return an http session backed by a map (only attribute methods are supported)
   */
  private static HttpSession newHttpSession() {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    return (HttpSession)Proxy.newProxyInstance(UiV2DownloadFileTest.class.getClassLoader(),
        new Class<?>[] {HttpSession.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("hashCode".equals(method.getName())) {
              return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
              return proxy == args[0];
            }
            throw new UnsupportedOperationException(method.getName());
          }
        });
  }

  /**
   * response that records headers and body
   */
  private static class RecordingResponse implements InvocationHandler {

    /** headers set */
    private Map<String, String> headers = new HashMap<String, String>();

    /** content type set */
    private String contentType;

    /** body */
    private ByteArrayOutputStream body = new ByteArrayOutputStream();

    /** the proxy */
    private HttpServletResponse response = (HttpServletResponse)Proxy.newProxyInstance(
        UiV2DownloadFileTest.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class}, this);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      String methodName = method.getName();
      if ("setHeader".equals(methodName)) {
        this.headers.put((String)args[0], (String)args[1]);
        return null;
      }
      if ("setContentType".equals(methodName)) {
        this.contentType = (String)args[0];
        return null;
      }
      if ("setContentLength".equals(methodName)) {
        this.headers.put("Content-Length", String.valueOf(args[0]));
        return null;
      }
      if ("getOutputStream".equals(methodName)) {
        return new ServletOutputStream() {

          @Override
          public void write(int b) {
            RecordingResponse.this.body.write(b);
          }

          @Override
          public boolean isReady() {
            return true;
          }

          @Override
          public void setWriteListener(WriteListener writeListener) {
          }
        };
      }
      throw new UnsupportedOperationException(methodName);
    }
  }

  /**
   * A token is only good in the session it was registered in, for the subject it was
   * registered for.
   */
  public void testTokenOnlyWorksInSameSessionForSameSubject() {

    // given
    HttpSession session1 = newHttpSession();
    HttpSession session2 = newHttpSession();

    // when
    String token = UiV2DownloadFile.registerDownload(session1, "fileId1", SubjectTestHelper.SUBJ0);

    // then
    assertEquals(32, token.length());
    assertTrue(token, token.matches("^[0-9a-f]+$"));
    assertEquals("fileId1", UiV2DownloadFile.retrieveGrouperFileId(session1, token, SubjectTestHelper.SUBJ0));
    // still good on a second click of the link
    assertEquals("fileId1", UiV2DownloadFile.retrieveGrouperFileId(session1, token, SubjectTestHelper.SUBJ0));

    assertNull("other session", UiV2DownloadFile.retrieveGrouperFileId(session2, token, SubjectTestHelper.SUBJ0));
    assertNull("other subject", UiV2DownloadFile.retrieveGrouperFileId(session1, token, SubjectTestHelper.SUBJ1));
    assertNull("unknown token", UiV2DownloadFile.retrieveGrouperFileId(session1, "abc", SubjectTestHelper.SUBJ0));
    assertNull("no token", UiV2DownloadFile.retrieveGrouperFileId(session1, null, SubjectTestHelper.SUBJ0));
    assertNull("no session", UiV2DownloadFile.retrieveGrouperFileId(null, token, SubjectTestHelper.SUBJ0));
    assertNull("no subject", UiV2DownloadFile.retrieveGrouperFileId(session1, token, null));

    // tokens are random
    String token2 = UiV2DownloadFile.registerDownload(session1, "fileId1", SubjectTestHelper.SUBJ0);
    assertFalse(token.equals(token2));
  }

  /**
   * The session keeps at most MAX_TOKENS_PER_SESSION tokens, dropping the oldest.
   */
  public void testOldestTokensDropped() {

    // given
    HttpSession session = newHttpSession();
    String firstToken = UiV2DownloadFile.registerDownload(session, "fileFirst", SubjectTestHelper.SUBJ0);

    // when
    String lastToken = null;
    for (int i = 0; i < UiV2DownloadFile.MAX_TOKENS_PER_SESSION; i++) {
      lastToken = UiV2DownloadFile.registerDownload(session, "file" + i, SubjectTestHelper.SUBJ0);
    }

    // then
    assertNull(UiV2DownloadFile.retrieveGrouperFileId(session, firstToken, SubjectTestHelper.SUBJ0));
    assertEquals("file" + (UiV2DownloadFile.MAX_TOKENS_PER_SESSION - 1),
        UiV2DownloadFile.retrieveGrouperFileId(session, lastToken, SubjectTestHelper.SUBJ0));
    assertEquals(UiV2DownloadFile.MAX_TOKENS_PER_SESSION,
        ((Map<?, ?>)session.getAttribute(UiV2DownloadFile.SESSION_ATTRIBUTE)).size());
  }

  /**
   * content type comes from the extension
   */
  public void testContentType() {
    assertEquals("text/csv; charset=UTF-8", UiV2DownloadFile.contentType("report_2026-10-04.csv"));
    assertEquals("text/csv; charset=UTF-8", UiV2DownloadFile.contentType("REPORT.CSV"));
    assertEquals("text/plain; charset=UTF-8", UiV2DownloadFile.contentType("a.txt"));
    assertEquals("application/json; charset=UTF-8", UiV2DownloadFile.contentType("a.json"));
    assertEquals("application/octet-stream", UiV2DownloadFile.contentType("a.html"));
    assertEquals("application/octet-stream", UiV2DownloadFile.contentType("noExtension"));
    // GRP-7446 binary types
    assertEquals("application/zip", UiV2DownloadFile.contentType("grouperMembers_2026-10-05.zip"));
    assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", UiV2DownloadFile.contentType("a.XLSX"));
    assertEquals("application/pdf", UiV2DownloadFile.contentType("a.pdf"));
    assertTrue(UiV2DownloadFile.isCompressed("a.zip"));
    assertTrue(UiV2DownloadFile.isCompressed("a.XLSX"));
    assertFalse(UiV2DownloadFile.isCompressed("a.csv"));
    assertFalse(UiV2DownloadFile.isCompressed("noExtension"));
  }

  /**
   * attachment header has an ascii fallback and a utf-8 filename*
   */
  public void testContentDisposition() {
    assertEquals("attachment; filename=\"report_2026-10-04.csv\"; filename*=UTF-8''report_2026-10-04.csv",
        UiV2DownloadFile.contentDisposition("report_2026-10-04.csv"));
    assertEquals("attachment; filename=\"my report.csv\"; filename*=UTF-8''my%20report.csv",
        UiV2DownloadFile.contentDisposition("my report.csv"));
    // quote cant break out of the header value
    assertEquals("attachment; filename=\"a_b.csv\"; filename*=UTF-8''a%22b.csv",
        UiV2DownloadFile.contentDisposition("a\"b.csv"));
    // e with acute accent
    assertEquals("attachment; filename=\"caf_.csv\"; filename*=UTF-8''caf%C3%A9.csv",
        UiV2DownloadFile.contentDisposition("café.csv"));
  }

  /**
   * the file is written as an attachment, gzipped when the browser accepts it
   * @throws IOException
   */
  public void testWriteFileGzipAndPlain() throws IOException {

    // given
    StringBuilder csv = new StringBuilder("\"user.name\",\"factor\"\n");
    for (int i = 0; i < 2000; i++) {
      csv.append("\"user" + i + "\",\"sms_passcode\"\n");
    }
    GrouperFile grouperFile = new GrouperFile();
    grouperFile.setFileName("report.csv");
    grouperFile.setValueToSave(csv.toString());

    // when: browser accepts gzip
    RecordingResponse gzipResponse = new RecordingResponse();
    UiV2DownloadFile.writeFile(grouperFile, "gzip, deflate, br", gzipResponse.response);

    // then
    assertEquals("text/csv; charset=UTF-8", gzipResponse.contentType);
    assertEquals("gzip", gzipResponse.headers.get("Content-Encoding"));
    assertEquals("no-store", gzipResponse.headers.get("Cache-Control"));
    assertEquals("nosniff", gzipResponse.headers.get("X-Content-Type-Options"));
    assertTrue(gzipResponse.headers.get("Content-Disposition").startsWith("attachment; filename=\"report.csv\""));
    byte[] gzipped = gzipResponse.body.toByteArray();
    assertTrue("should be compressed: " + gzipped.length, gzipped.length < csv.length() / 4);
    String unzipped = new String(IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(gzipped))), StandardCharsets.UTF_8);
    assertEquals(csv.toString(), unzipped);

    // when: no gzip
    RecordingResponse plainResponse = new RecordingResponse();
    UiV2DownloadFile.writeFile(grouperFile, null, plainResponse.response);

    // then
    assertNull(plainResponse.headers.get("Content-Encoding"));
    assertEquals(String.valueOf(csv.length()), plainResponse.headers.get("Content-Length"));
    assertEquals(csv.toString(), new String(plainResponse.body.toByteArray(), StandardCharsets.UTF_8));
  }

  /**
   * GRP-7446: a binary file (zip) is written byte for byte, and not gzipped even if the browser accepts gzip
   * since it is already compressed
   * @throws IOException
   */
  public void testWriteFileZipNotGzipped() throws IOException {

    // given: bytes that are not valid utf-8, so any text conversion would change them
    byte[] zipBytes = new byte[] {'P', 'K', 3, 4, 0, (byte)0xff, (byte)0xfe, (byte)0x80, 10, 13};
    GrouperFile grouperFile = new GrouperFile();
    grouperFile.setFileName("grouperMembers_2026-10-05.zip");
    grouperFile.setBytesToSave(zipBytes);

    // when: browser accepts gzip
    RecordingResponse response = new RecordingResponse();
    UiV2DownloadFile.writeFile(grouperFile, "gzip, deflate, br", response.response);

    // then: sent as is
    assertEquals("application/zip", response.contentType);
    assertNull(response.headers.get("Content-Encoding"));
    assertEquals(String.valueOf(zipBytes.length), response.headers.get("Content-Length"));
    assertTrue(response.headers.get("Content-Disposition").startsWith("attachment; filename=\"grouperMembers_2026-10-05.zip\""));
    assertTrue(java.util.Arrays.equals(zipBytes, response.body.toByteArray()));
  }

}

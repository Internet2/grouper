---
title: "GSH template file download"
space: Grouper
pageId: 274464837
version: 2
lastUpdated: 2026-10-05T07:03:09.397Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/274464837/GSH+template+file+download
---

Starting in Grouper v7.7.0+, a GSH template can give the user a file to download, e.g. a CSV report or a zipped CSV. After the template runs in the UI, the browser downloads the file, and the template screen shows a link to it in case the browser blocks the download. This works for Groovy and compiled Java GSH templates.

## How it works

1. The template saves the file in the `grouper_file` table (text, or binary such as a zip) and marks it for download, with `gsh_builtin_gshTemplateOutput`.
2. The UI puts a random token in the user's session, and the browser downloads the file. The token only works in that session for that user, so a copied download URL is useless to anyone else.
3. Each file is stored with a date (yyyy-MM-dd) and a file name, so a template can reuse today's file instead of computing it again.
4. There is no central cleanup. Each template deletes its own old files.

## Methods

Call these on `gsh_builtin_gshTemplateOutput` (`out.getGsh_builtin_gshTemplateOutput()` in Java).

| Method | Description |
| --- | --- |
| `assignDownloadFile(date, fileName, String contents)` | Save text (e.g. CSV) for this template, date, and file name, and download it. Replaces the contents if that date and name are already there. |
| `assignDownloadFile(date, fileName, byte[] contents)` | Same, for binary contents such as a zip, xlsx, or pdf. |
| `retrieveDownloadFileId(date, fileName)` | grouper_file id of this template's file for that date and name, or null. Does not load the contents. |
| `assignDownloadGrouperFileId(grouperFileId)` | Download an existing grouper_file row, e.g. the one retrieveDownloadFileId found. |
| `deleteDownloadFilesOlderThanMinutes(minutes)` | Delete this template's files last saved more than that many minutes ago. Returns the number deleted. |

**Note:** files are saved per template, date, and file name, not per user. A file found with `retrieveDownloadFileId` is served to whoever runs the template next. Only reuse a file whose contents are the same for everyone who can run the template. If the contents depend on the user running it, put something unique to the user in the file name, or do not reuse it.

The date is yyyy-MM-dd. The file name is at most 100 characters with no slashes. Its extension sets the content type: csv, txt, json, xml, zip, gz, xlsx, docx, pdf, png, jpg. Other extensions download as application/octet-stream.

## Compute once a day

A daily report computes the file on the first run of the day, and later runs that day download the saved file.

```java
GshTemplateOutput gshTemplateOutput = out.getGsh_builtin_gshTemplateOutput();
String today = java.time.LocalDate.now().toString();
String fileName = "myReport_" + today + ".csv";

// already computed today?  then just download it
String existingFileId = gshTemplateOutput.retrieveDownloadFileId(today, fileName);
if (existingFileId != null) {
  gshTemplateOutput.assignDownloadGrouperFileId(existingFileId);
  return;
}

String csv = computeReport();
gshTemplateOutput.assignDownloadFile(today, fileName, csv);

// keep a week of files
gshTemplateOutput.deleteDownloadFilesOlderThanMinutes(7 * 24 * 60);
```

## Zipped CSV

Zip a large CSV and save the bytes. A CSV of people data typically zips to about a quarter of its size, e.g. 8.5MB to 1.9MB.

```java
ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipBytes)) {
  zipOutputStream.putNextEntry(new ZipEntry("myReport_" + today + ".csv"));
  zipOutputStream.write(csv.getBytes(StandardCharsets.UTF_8));
  zipOutputStream.closeEntry();
} catch (IOException ioe) {
  throw new RuntimeException(ioe);
}
gshTemplateOutput.assignDownloadFile(today, "myReport_" + today + ".zip", zipBytes.toByteArray());
```

The attached `DemoDownloadMembersCsvZip.java` is a complete compiled Java template: it exports grouper_members as a zipped CSV once a day and keeps a week of files.

## Storage and limits

- Files are in `grouper_file` with system_name `gshTemplateDownload` and file_path `/gshTemplateDownload/<templateConfigId>/<date>/<fileName>`.
- Text is in file_contents_varchar or file_contents_clob, binary is in file_contents_blob.
- Files are not encrypted, and a saved file is reused for everyone who runs the template. Only put data in a download file that every user who can run the template may see.
- The max size is `grouperFile.maxSizeBytes` in grouper.properties (default 52428800, i.e. 50MB, -1 for no limit). The whole file is held in memory when it is saved and downloaded.
- Text downloads are gzipped in transit if the browser accepts it. Files that are already compressed (zip, xlsx, pdf, png, etc) are sent as is.
- `deleteDownloadFilesOlderThanMinutes` only deletes the calling template's own files, by when each was last saved (grouper_file.updated_on_micros), not by the date in the path.

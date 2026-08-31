---
title: "Grouper Sftp files"
space: Grouper
pageId: 28548236
version: 11
lastUpdated: 2026-07-01T05:44:59.824Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548236/Grouper+Sftp+files
---

> Grouper can connect to SFTP sites to get and put files (for example, to ship a generated CSV to a downstream depot). Available since the Grouper `v2.4.0+` API (API patch 81), and present in all currently supported releases — the `GrouperSftp` API and these config keys are verified present.
> 
> Configuring an SFTP site means editing the Grouper configuration (`grouper.properties` / the config editor) and, for private keys and passwords, the server filesystem — so this is set up by a Grouper administrator, not an end user. The API is called from server-side Java or GSH; it is not exposed in the UI or web services.

  

## Overview

 Define one or more SFTP **sites** in the Grouper configuration. Each site has a `configId` — a short identifier (no special characters) used in code to pull up that site. For example, if your SFTP server is `depot.school.edu`, the `configId` could be `depot`. A given host and username should only appear in one `configId`, since together they act as the primary key for the site.

 

## Configuration

 

### Global settings

 These go in `grouper.properties` and apply to all SFTP sites.

 

| Key | Description | Type |
| --- | --- | --- |
| `grouperSftpBaseDirName` | Directory for the temporary files SFTP needs to connect (private key, known hosts). Keep it readable only by the tomcat user. If blank, the tmp dir configured in `grouper.properties` is used. | string |
| `grouperSftp.proxyHost` | Default proxy host for all SFTP external systems. | string |
| `grouperSftp.proxyPort` | Default proxy port for all SFTP external systems. | integer |
| `grouperSftp.proxyType` | Default proxy type for all SFTP external systems: `PROXY_HTTP`, `PROXY_SOCKS5`, or `PROXY_STREAM`. | string |

 

### Per-site settings

 Each key is of the form `grouperSftp.site.<configId>.<setting>` — replace `<configId>` with your site identifier (for example `grouperSftp.site.depot.host`).

 

| Setting | Description | Type | Required | Default |
| --- | --- | --- | --- | --- |
| `host` | SFTP host, e.g. `some.server.com` | string | yes |  |
| `user` | SFTP username, e.g. `someuser` | string | yes |  |
| `secret.privateKey_0` | Encrypted private key to connect with. If the encrypted value is more than 4k, split it into chunks suffixed `_0`, `_1`, `_2`… (they are concatenated). Replace newlines with `$newline$` so the value fits in a text field. | password (sensitive) | no |  |
| `secret.privateKeyPassphrase` | Passphrase for the private key. | password (sensitive) | no |  |
| `password` | Password, if connecting with a password instead of a private key. | password (sensitive) | no |  |
| `knownHostsEntry` | The `known_hosts` entry for the host you connect to, e.g. `host.whatever ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAIEA3B00cx5W9KPSjzik3E` | string | no |  |
| `deleteTempFilesAfterSession` | Whether temporary files (private key and known hosts) are deleted after the session. | boolean | no | `true` |
| `timeoutMillis` | Connection timeout in milliseconds. | integer | no | `10000` |
| `enabled` | Whether this SFTP connector is enabled. | boolean | no | `true` |
| `proxyHost` | Proxy host for this site, or fall back to the global `grouperSftp.proxyHost`. | string | no |  |
| `proxyPort` | Proxy port for this site, or fall back to the global `grouperSftp.proxyPort`. | integer | no |  |
| `proxyType` | Proxy type for this site: `PROXY_HTTP`, `PROXY_SOCKS5`, or `PROXY_STREAM`, or fall back to the global `grouperSftp.proxyType`. | string | no |  |

 

## Logging

 Enable debug logging for the SFTP client in `log4j.properties`:

 
```text
log4j.logger.edu.internet2.middleware.grouper.app.file.GrouperSftp = DEBUG
```

 A sample debug log message:

 
```text
2019-11-23 21:18:20,117: [main] DEBUG GrouperSftp.callback(255) -  - configId: depot, grouperSftpDirName: C:\Users\mchyzer\AppData\Local\Temp\grouperSftp\sftpSession_2019_11_23__21_18_16_795_UDMNM4A9, keyFileSize: 3246, host: depot.school.edu, knownHost: depot.school.edu ssh-rsa AA********c2E..., knownHostsContainsHost: true, user: myUser, passphrase?: <none>, password?: <none>, timeoutMillis: 10000, sendFileLocal_0: C:\Users\mchyzer\AppData\Local\Temp\MyFile.csv, sendFileRemote_0: /data01/dept/app/SomeFile.csv, deleteDir: true, tookMillis: 3321
```

 

## Using the API

 All calls take the `configId` of the site as the first argument. The available static methods on `GrouperSftp` are:

 

| Method | Purpose |
| --- | --- |
| `sendFile(configId, localFile, remotePath)` | Upload a local file to the remote path. |
| `receiveFile(configId, remotePath, localFile)` | Download a remote file to a local file. |
| `listFiles(configId, remotePath)` | List files at the remote path (returns a `List<String>`). |
| `existsFile(configId, remotePath)` | Whether the remote file exists (returns a `boolean`). |
| `copyFile(configId, remoteFrom, remoteTo)` | Copy a file on the remote server. |
| `moveFile(configId, remoteFrom, remoteTo)` | Move/rename a file on the remote server. |
| `deleteFile(configId, remotePath)` | Delete a remote file (returns a `boolean`). |
| `callback(configId, grouperSftpCallback)` | Run multiple operations in a single session (see below). |

 

### Individual calls

 Each call opens its own SFTP session.

 
```java
GrouperSftp.sendFile("depot", new File("d:/temp/temp/MyFile.csv"), "/data01/dept/app/MyFile.csv");

System.out.println(GrouperUtil.toStringForLog(GrouperSftp.listFiles("depot", "/data01/dept/app/")));

System.out.println(GrouperSftp.existsFile("depot", "/data01/dept/app/MyFile.csv"));

GrouperSftp.copyFile("depot", "/data01/dept/app/MyFile.csv", "/data01/dept/app/MyFile2.csv");

GrouperSftp.moveFile("depot", "/data01/dept/app/MyFile.csv", "/data01/dept/app/MyFile3.csv");

GrouperSftp.receiveFile("depot", "/data01/dept/app/MyFile3.csv", new File("d:/temp/temp/MyFile2.csv"));

GrouperSftp.deleteFile("depot", "/data01/dept/app/MyFile3.csv");
```

 

### Multiple calls in a callback

 Use a callback to run several operations efficiently over a single session.

 
```java
GrouperSftp.callback("depot", new GrouperSftpCallback() {

  public Object callback(GrouperSftpSession grouperSftpSession) {
    grouperSftpSession.sendFile(new File("d:/temp/temp/Users.csv"), "/data01/dept/app/Users.csv");
    grouperSftpSession.deleteFile("/data01/dept/app/whatever.txt");
    return null;
  }
});
```

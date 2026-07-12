---
title: "Release steps for new container build"
space: GrIntDev
pageId: 48794029
version: 60
lastUpdated: 2026-07-12T06:46:27.256Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794029/Release+steps+for+new+container+build
---

1. Look at maven for each project and update libraries for any vulnerabilities
  
  1. Look at owasp dependency check goal
    
    
    ```
    mvn -f grouper-parent site
    ```
  2. See the [jenkins security report](https://jenkins.testbed.tier.internet2.edu/job/docker/job/grouper/job/2.6.16.2-rocky-multi-arch-test/30/Security_20Scan/) (change version in URL)
2. Look at the jiras for the stable branch and make sure all commits are cherry picked back
3. See if there is an updated Tomcat version (ITS webprod6 NOW!)
  
  1. If so, get the tomcat tar.gz to the webprod6 server in the proper directory
    
    
    ```
    [mchyzer@webprod6 ~]$ cd /home/htdocs/software.internet2.edu/grouper/downloads/tools/
    [mchyzer@webprod6 tools]$ wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.98/bin/apache-tomcat-9.0.98.tar.gz
    ```
  2. Adjust the tomcat version in GrouperInstaller.java (v4)
    
    
    ```
      public static final String TOMCAT_VERSION = "9.0.98";
    ```
  3. Adjust the tomcat version in the Dockerfile (v5+)  
      
    
    ```
    ARG TOMCAT_VERSION=9.0.108
    ```
  4. Might need to run patches again in container (if new server.xml is different from server.xml.original)
    
    
    ```
    put server.xml in server.xml.original, adjust server.xml.grouper
    diff -u server.xml.original server.xml.grouper > server.xml.grouper.patch
    ```
4. Run "ant build" in grouper-client, make sure it compiles
5. If there are DDL changes make sure theres not an index longer than expected 768
6. Check unit tests (email with CI test results (summary))
7. Tag as GROUPER_RELEASE_x.y.z in grouper git
8. In [Internet2 build git](https://github.internet2.edu/internet2/grouper.git), branch as x.y.z.  
  
  
  1. Branch from the latest commit in the proper branch (check the [network graph](https://github.internet2.edu/internet2/grouper/network) if unclear)  
    git checkout 2.5.62  
    git checkout -b 2.5.63
  2. Check out the latest 2.5.x and 2.6.x to see if there is anything needing to be cherry-picked (Note: might need to change java version in jenkinsfile)  
    git pull  
    git diff origin/2.5.62..origin/2.6.9
  3. Create an empty commit so it triggers a build  
    git commit --allow-empty -m "build 2.5.63"
  4. Push to remote
9. Wait 15 minutes [build to finish](https://jenkins.testbed.tier.internet2.edu/job/internet2/job/grouper/) (old: [for build to finish](https://travis-ci.com/github/Internet2/grouper/builds))
10. Go to: [https://central.sonatype.com/](https://central.sonatype.com/)
  
  1. Select the x.y.z version and click "Release"  
    (ok to leave checked "automatically drop")
  2. Browse public repositories, Navigate the folder structure to /edu/internet2/middleware/grouper/grouper to make sure the new version is there
  3. Wait for a while until the installer is resolvable here: [https://oss.sonatype.org/service/local/repositories/releases/content/edu/internet2/middleware/grouper/grouper-installer/X.Y.Z/grouper-installer-](https://oss.sonatype.org/service/local/repositories/releases/content/edu/internet2/middleware/grouper/grouper-installer/5.19.1/grouper-installer-5.19.1.jar)[X.Y.Z](https://oss.sonatype.org/service/local/repositories/releases/content/edu/internet2/middleware/grouper/grouper-installer/5.19.1/grouper-installer-5.19.1.jar)[.jar](https://oss.sonatype.org/service/local/repositories/releases/content/edu/internet2/middleware/grouper/grouper-installer/5.19.1/grouper-installer-5.19.1.jar)
11. In the docker_grouper project
  
  1. remove any patches
  2. make an x.y.z branch if not already there
12. Make sure docker unit test count matches the number of changed unit tests in grouperContainerUnitTest.sh
  
  1. have there been any new tests (assert*) since the last release? If so, update grouperContainerUnitTest.sh by incrementing expectedSuccesses by the number of new tests  
    git log -p 2.5.62.. -- container_files/tier-support/test/grouperContainerUnitTestUi.sh container_files/tier-support/test/grouperContainerUnitTest.sh
13. Change the Dockerfile to x.y.z in one place (or two for 2.5), [commit and push](https://github.internet2.edu/docker/grouper/branches)
14. Wait 15 minutes
15. Wait until [build is done](https://jenkins.testbed.tier.internet2.edu/job/docker/job/grouper/)
16. Check build output for conflicting jars
17. Run the [container unit tests](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555514/Grouper+container+unit+tests). Link to [Grouper dockerhub](https://hub.docker.com/r/i2incommon/grouper/tags?page=1&ordering=last_updated)
18. Container mysql replicate (if build error)
  
  
  ```
  mchyzer@ISC20-0637-WL:~/containerTest$ cat Dockerfile
  FROM centos:centos7 as installing
  RUN yum update -y \
      && yum install -y wget tar unzip dos2unix patch \
          && yum clean all
  
  RUN yum install -y wget tar unzip dos2unix patch
  
  RUN yum install -y epel-release \
      && yum update -y \
      && yum install -y mariadb-server mariadb \
      && yum clean all \
      && rm -rf /var/cache/yum
  
  RUN mysql_install_db --force \
      && chown -R mysql:mysql /var/lib/mysql/ \
      && sed -i 's/^\(bind-address\s.*\)/# \1/' /etc/my.cnf \
      && sed -i 's/^\(log_error\s.*\)/# \1/' /etc/my.cnf \
      && sed -i 's/\[mysqld\]/\[mysqld\]\ncharacter_set_server = utf8/' /etc/my.cnf \
      && sed -i 's/\[mysqld\]/\[mysqld\]\ncollation_server = utf8_general_ci/' /etc/my.cnf \
      && sed -i 's/\[mysqld\]/\[mysqld\]\nport = 3306/' /etc/my.cnf \
      && cat  /etc/my.cnf \
      && echo "/usr/bin/mysqld_safe &" > /tmp/config \
      && echo "mysqladmin --silent --wait=30 ping || exit 1" >> /tmp/config \
      && echo "mysql -e 'GRANT ALL PRIVILEGES ON *.* TO \"root\"@\"%\" WITH GRANT OPTION;'" >> /tmp/config \
      && echo "mysql -e 'CREATE DATABASE grouper CHARACTER SET utf8 COLLATE utf8_bin;'" >> /tmp/config \
      && bash /tmp/config \
      && rm -f /tmp/config
  
  
  EXPOSE 3306
  
  CMD mysqld_safe
  mchyzer@ISC20-0637-WL:~/containerTest$ docker build -t my_mysql .
  [+] Building 26.8s (9/9) FINISHED
   => [internal] load build definition from Dockerfile                                                                                                                                                              0.0s
   => => transferring dockerfile: 1.27kB                                                                                                                                                                            0.0s
   => [internal] load .dockerignore                                                                                                                                                                                 0.0s
   => => transferring context: 2B                                                                                                                                                                                   0.0s
   => [internal] load metadata for docker.io/library/centos:centos7                                                                                                                                                 2.5s
   => [1/5] FROM docker.io/library/centos:centos7@sha256:9d4bcbbb213dfd745b58be38b13b996ebb5ac315fe75711bd618426a630e0987                                                                                           0.0s
   => CACHED [2/5] RUN yum update -y     && yum install -y wget tar unzip dos2unix patch         && yum clean all                                                                                                   0.0s
   => CACHED [3/5] RUN yum install -y wget tar unzip dos2unix patch                                                                                                                                                 0.0s
   => [4/5] RUN yum install -y epel-release     && yum update -y     && yum install -y mariadb-server mariadb     && yum clean all     && rm -rf /var/cache/yum                                                    17.2s
   => [5/5] RUN mysql_install_db --force     && chown -R mysql:mysql /var/lib/mysql/     && sed -i 's/^\(bind-address\s.*\)/# \1/' /etc/my.cnf     && sed -i 's/^\(log_error\s.*\)/# \1/' /etc/my.cnf     && sed -  6.2s
   => exporting to image                                                                                                                                                                                            0.9s
   => => exporting layers                                                                                                                                                                                           0.9s
   => => writing image sha256:21400cf1803d58e336753379217dc0539a100aa8b5e9bd8923b1b07d816db4cc                                                                                                                      0.0s
   => => naming to docker.io/library/my_mysql                                                                                                                                                                       0.0s
  
  Use 'docker scan' to run Snyk tests against images to find vulnerabilities and learn how to fix them
  mchyzer@ISC20-0637-WL:~/containerTest$ docker run --detach --name my_mysql --publish 3306:3306 my_mysql:latest
  ```
19. [Use the installer](https://repo1.maven.org/maven2/edu/internet2/middleware/grouper/grouper-installer/2.5.xx/) to install the container against a mysql from docker (case sensitive)
  
  1. jdbc:mysql://docker.for.win.localhost:3306/grouper_v2_5?useSSL=false
20. Upgrade the demo server
21. Adjust the version of apache/shib/java/tomcat in the [release notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793539/v2.5+Release+Notes)  
  v4
  
  
  ```
  docker run --rm i2incommon/grouper:2.5.xx bash -c "java -version && httpd -v && /usr/sbin/shibd -v && grep 'Apache Tomcat Version' /opt/tomcat/RELEASE-NOTES && grep PRETTY_NAME /etc/os-release"
  ```
  
  v5
  
  
  ```
  docker run --rm i2incommon/grouper:2.5.xx bash -c "java -version && grep 'Apache Tomcat Version' /opt/tomcat/RELEASE-NOTES && grep PRETTY_NAME /etc/os-release"
  ```
22. Adjust the SHA in release notes  
  
  
  1. docker image inspect i2incommon/grouper:2.5.xx --format '{{ .RepoDigests }}'

After stable

1. When the release has been used for a few days and no issues, then mark as stable
2. Update this page too: InCommon Trusted Access Platform Release

**See Also**

[Release steps](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793015/Release+steps)

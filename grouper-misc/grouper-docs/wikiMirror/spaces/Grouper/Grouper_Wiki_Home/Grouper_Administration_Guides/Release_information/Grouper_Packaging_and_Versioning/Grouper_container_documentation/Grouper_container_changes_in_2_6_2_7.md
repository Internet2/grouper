---
title: "Grouper container - changes in 2.6/2.7"
space: Grouper
pageId: 28554477
version: 9
lastUpdated: 2026-07-01T05:40:21.814Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554477/Grouper+container+-+changes+in+2.6+2.7
---

## Near term changes

1. Have a dockerfile that works with rocky linux and centos
  
  1. change sed file commands (DONE)
2. Release rocky image in 2.6 with each centos image. e.g. centos is 2.6.x, rocky is 2.6.x-rocky
3. Change user/group in image to be tomcat/root (DONE)
  
  1. This is openshift compatible
  2. Files will be group writable (DONE)
  3. Need umask for tomcat user (or env var) (DONE)
4. Make sure files/dirs are not OTHER WRITABLE (DONE)
5. Add the docker compose quickstart to the container unit tests
6. Refactor dockerfile (DONE)
  
  1. No more layers since no efficient way to do setgid without copying all files again (DONE)
7. Create the /opt/tomee/temp dir (DONE)
8. Create the /opt/tomee/work dir (DONE)
9. Only chown/chmod /opt/grouper, /opt/tomee (DONE)
10. Have all libs in WEB-INF/lib ? maybe do this later (NAH)
11. Yum updates in RUN in dockerfile (DONE)
12. Java / Grouper in shell script layer (cached for dev)
13. Rest of commands in shell script layer

## Medium term changes

1. 2.7 branched by techex
2. Single process (tomcat)
  
  1. Rewrite SCIM server to run in tomcat
3. No apache
4. No shibboleth
  
  1. Unicon authentication
  2. or lightweight OIDC
5. No supervisord
6. Support ARM and x86
  
  1. buildx
7. FROM rocky linux (only, no more centos)
  
  1. Will have an example to multi-stage build
    
    1. into centos
    2. into distroless
8. Java 11

## Reducing image size

If you do not pay attention to what commands are run in the Dockerfile or in the running container, you can cause "image bloat".

Here are some changes being made to 2.6.15+ and some best practices

1. It used to be for openshift the file owners were tomcat:root, and for non openshift: tomcat:tomcat. We will now just have the container be tomcat:root for Grouper's file
  
  1. The tomcat user can have a setgid and umask to accomodate this (GIVE EXAMPLE)
  2. Put umask in .bashrc? (GIVE EXAMPLE)
2. Reduce the number of COPY/RUN commands in Dockerfile (GIVE EXAMPLE)
3. Only change files which need to be changed, e.g. do not blanket chown/chmod files which do not need changing (GIVE EXAMPLE)
  
  1. Change permissions of directories different than files, make sure "s" bit set for setgid (GIVE EXAMPLE)
4. Do not copy jars around in container?

[https://towardsdatascience.com/slimming-down-your-docker-images-275f0ca9337e](https://towardsdatascience.com/slimming-down-your-docker-images-275f0ca9337e)

Size of container:

```
[root@i2midev6 container]# docker container ls -s | grep grouper_v2_6
1fa495d5b864   demo-grouper-2.6:latest    "/usr/local/bin/entrâ€¦"   16 hours ago   Up 16 hours                                                     grouper_v2_6              279MB (virtual 1.96GB)
4aae72d65c5b   i2incommon/grouper:2.6.9   "/usr/local/bin/entrâ€¦"   2 months ago   Up 2 days                                                       grouper_v2_6a             19.6MB (virtual 1.49GB)
```

Size of image:

```
[root@i2midev6 container]# docker image ls | grep "2\.6"
demo-grouper-2.6          latest    679073de43cb   16 hours ago   1.68GB
i2incommon/grouper        2.6.14    c634f237f9c7   22 hours ago   1.48GB
```

Layer size:

```
[root@i2midev6 container]# docker image history demo-grouper-2.6:latest 
IMAGE          CREATED        CREATED BY                                      SIZE      COMMENT
679073de43cb   16 hours ago   /bin/sh -c groupdel games   && userdel gamesâ€¦   206MB     
fb324af7ea7b   16 hours ago   /bin/sh -c #(nop) COPY dir:bcd6961c47ee7fb1eâ€¦   2.1kB     
c634f237f9c7   22 hours ago   /bin/sh -c #(nop)  ENTRYPOINT ["/usr/local/bâ€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop)  HEALTHCHECK &{["NONE"] "0â€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop)  EXPOSE 443 80                0B        
<missing>      22 hours ago   /bin/sh -c #(nop) WORKDIR /opt/grouper/groupâ€¦   0B        
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   33.1kB    
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   205MB     
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:17b17895e6c2722â€¦   7.14kB    
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:595ac4c989267fdâ€¦   250kB     
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:5273c9a19c84246â€¦   50kB      
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:3d4a46a1ad0441eâ€¦   2.12MB    
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   0B        
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:747bcf376e931b5â€¦   12.8kB    
<missing>      22 hours ago   /bin/sh -c #(nop) COPY multi:6c82eb75f5f0409â€¦   3.5kB     
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   88.8kB    
<missing>      22 hours ago   /bin/sh -c #(nop) COPY dir:27109b732a308e454â€¦   94kB      
<missing>      22 hours ago   /bin/sh -c #(nop) COPY dir:3385793edc54b3fe7â€¦   2.49MB    
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   360kB     
<missing>      22 hours ago   /bin/sh -c #(nop) COPY dir:7b25c012edbba2c06â€¦   159MB     
<missing>      22 hours ago   /bin/sh -c #(nop) COPY dir:2f156e4690f916cb6â€¦   41.8MB    
<missing>      22 hours ago   /bin/sh -c #(nop)  ENV JAVA_HOME=/usr/lib/jvâ€¦   0B        
<missing>      22 hours ago   |2 CORRETTO_RPM=amazon-corretto-8-x64-linux-â€¦   244MB     
<missing>      22 hours ago   /bin/sh -c #(nop) COPY file:05547c3356daf472â€¦   1.7kB     
<missing>      22 hours ago   /bin/sh -c #(nop)  ARG CORRETTO_RPM=amazon-câ€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop)  ARG CORRETTO_URL_PERM=httâ€¦   0B        
<missing>      22 hours ago   /bin/sh -c yum update -y     && yum install â€¦   302MB     
<missing>      22 hours ago   /bin/sh -c ln -sf /usr/share/zoneinfo/UTC /eâ€¦   23B       
<missing>      22 hours ago   /bin/sh -c #(nop)  ENV PATH=/usr/local/sbin:â€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop)  ARG GROUPER_CONTAINER_VERâ€¦   0B        
<missing>      22 hours ago   /bin/sh -c #(nop)  LABEL author=tier-packagiâ€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  CMD ["/usr/local/bin/starâ€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  HEALTHCHECK &{["CMD-SHELLâ€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  EXPOSE 443 80                0B        
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   288B      
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   231B      
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:9057ae47f069983e5â€¦   763B      
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:b96c26b5126d2bbebâ€¦   902B      
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:3482920e92a83d600â€¦   321B      
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:9c5138242404c1036â€¦   596B      
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:a7c0db718ec6242ddâ€¦   1.11kB    
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:8a704cedb59b5723dâ€¦   62B       
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   0B        
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   12kB      
<missing>      2 years ago    /bin/sh -c #(nop) ADD multi:96e8ca38ef61eb1aâ€¦   7.09kB    
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:fe3827180e53f4e67â€¦   9.41kB    
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   0B        
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   0B        
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   57.8MB    
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   259MB     
<missing>      2 years ago    |4 TIERVERSION=20200417 imagename=shibbolethâ€¦   15B       
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL Build=docker build â€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL Version=3.1.0          0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL ImageOS=centos7        0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL ImageName=shibboletâ€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL ImageType=Base         0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL Vendor=Internet2       0B        
<missing>      2 years ago    /bin/sh -c #(nop)  MAINTAINER $maintainer       0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ARG TIERVERSION=20200417     0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ARG version=3.1.0            0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ARG imagename=shibboleth_â€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop)  ARG maintainer=tier          0B        
<missing>      2 years ago    /bin/sh -c #(nop)  CMD ["/bin/bash"]            0B        
<missing>      2 years ago    /bin/sh -c #(nop)  LABEL org.label-schema.scâ€¦   0B        
<missing>      2 years ago    /bin/sh -c #(nop) ADD file:45a381049c52b5664â€¦   203MB     
[root@i2midev6 container]# 
```

Inspect image

```
[root@i2midev6 container]# docker image inspect  demo-grouper-2.6:latest
[
    {
        "Id": "sha256:679073de43cbbbf0dd599ae7b30aafc06b2bdb4c44a01896e6fee0efe6ca39f1",
        "RepoTags": [
            "demo-grouper-2.6:latest"
        ],
        "RepoDigests": [],
        "Parent": "sha256:fb324af7ea7bc20b216939bc71c7ce3b50c6355db01a2fa357d680d50571dbfb",
        "Comment": "",
        "Created": "2022-08-11T03:32:39.050452211Z",
        "Container": "0422b183a3093b785921f0a7752da78ae84332586aa80f0e35afc866817b81b3",
        "ContainerConfig": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "443/tcp": {},
                "80/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/bin",
                "GROUPER_HOME=/opt/grouper/grouperWebapp/WEB-INF",
                "GROUPER_CONTAINER_VERSION=2.6.14",
                "JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto"
            ],
            "Cmd": [
                "/bin/sh",
                "-c",
                "groupdel games   && userdel games   && groupdel users   && /usr/local/bin/changeUid.sh tomcat 1870   && /usr/local/bin/changeGid.sh tomcat 100   && chown -R 1870:100 /opt/grouper   && chown -R 1870:100 /opt/tomee   && chown 1870:100 /usr/lib/jvm/java/jre/lib/security/cacerts"
            ],
            "Healthcheck": {
                "Test": [
                    "NONE"
                ],
                "Interval": 60000000000,
                "Timeout": 30000000000
            },
            "Image": "sha256:fb324af7ea7bc20b216939bc71c7ce3b50c6355db01a2fa357d680d50571dbfb",
            "Volumes": null,
            "WorkingDir": "/opt/grouper/grouperWebapp/WEB-INF/",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": {
                "Build": "docker build --rm --tag tier/shibboleth_sp .",
                "ImageName": "",
                "ImageOS": "centos7",
                "ImageType": "Grouper",
                "Vendor": "TIER",
                "Version": "3.1.0",
                "author": "tier-packaging@example.com <tier-packaging@example.com>",
                "org.label-schema.build-date": "20191001",
                "org.label-schema.license": "GPLv2",
                "org.label-schema.name": "CentOS Base Image",
                "org.label-schema.schema-version": "1.0",
                "org.label-schema.vendor": "CentOS"
            }
        },
        "DockerVersion": "20.10.17",
        "Author": "",
        "Config": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "ExposedPorts": {
                "443/tcp": {},
                "80/tcp": {}
            },
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/bin",
                "GROUPER_HOME=/opt/grouper/grouperWebapp/WEB-INF",
                "GROUPER_CONTAINER_VERSION=2.6.14",
                "JAVA_HOME=/usr/lib/jvm/java-1.8.0-amazon-corretto"
            ],
            "Cmd": null,
            "Healthcheck": {
                "Test": [
                    "NONE"
                ],
                "Interval": 60000000000,
                "Timeout": 30000000000
            },
            "Image": "sha256:fb324af7ea7bc20b216939bc71c7ce3b50c6355db01a2fa357d680d50571dbfb",
            "Volumes": null,
            "WorkingDir": "/opt/grouper/grouperWebapp/WEB-INF/",
            "Entrypoint": [
                "/usr/local/bin/entrypoint.sh"
            ],
            "OnBuild": null,
            "Labels": {
                "Build": "docker build --rm --tag tier/shibboleth_sp .",
                "ImageName": "",
                "ImageOS": "centos7",
                "ImageType": "Grouper",
                "Vendor": "TIER",
                "Version": "3.1.0",
                "author": "tier-packaging@example.com <tier-packaging@example.com>",
                "org.label-schema.build-date": "20191001",
                "org.label-schema.license": "GPLv2",
                "org.label-schema.name": "CentOS Base Image",
                "org.label-schema.schema-version": "1.0",
                "org.label-schema.vendor": "CentOS"
            }
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 1683189780,
        "VirtualSize": 1683189780,
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/42fac9b967fd5e31f02b37f0e488ebe9999ca932a3ff9d50efba2f32a8ffe121/diff:/var/lib/docker/overlay2/1ef4abce568aad9d8dbb792989e40a54b8e9cddf44c4d81df72ac4bdfc6b0bc8/diff:/var/lib/docker/overlay2/7c6c4096cbe39df322a99d470dd37d9bfafcb6cedd4d9d06458e68a11c3c412d/diff:/var/lib/docker/overlay2/8c80b5470ac793c50cb8bc4aaefba0748d8e1ab8c35093af2920388ec106d5a8/diff:/var/lib/docker/overlay2/7772a78c2d2c285f9c97503f479c815b08d69411c939dccd3f4846f4224e4fae/diff:/var/lib/docker/overlay2/a57a9352d3697a9a9d8480f0687367bed0498c0e7ee63021dc28846a2a90a8f1/diff:/var/lib/docker/overlay2/2a8f9a2bbae4bf033d803703294ae9a1079ce797d0d6f62453822398292cb787/diff:/var/lib/docker/overlay2/b7a88f09842a27b0847dda970a3c8aa8b5ed6e828c81c0f756df798398fe27fa/diff:/var/lib/docker/overlay2/63ff14d86760353db1a669e86e31e0c35e8faf8400c7c6ac59cf9634549b3656/diff:/var/lib/docker/overlay2/9d3f8bf537c386bc04ea7439fa3bf84b73e6b24e5fa01c808b985008e301693f/diff:/var/lib/docker/overlay2/09333ca34506d5124047091d02f9321b9759d85b3808972af8a283ac219ba6c9/diff:/var/lib/docker/overlay2/03ee23fc23bfa70a52c10718baa4d331127bcea24ebfdd3c75e66deb326a4146/diff:/var/lib/docker/overlay2/ffd2608b0326e5d4ae4df582e51dde196bd4abb1313e962f975f6b2ace13b566/diff:/var/lib/docker/overlay2/64b6a28bda51088558f565878563780066e1e63ddd3ffde21fd262376f0560ac/diff:/var/lib/docker/overlay2/1f706300afe994dde4f3135fa74a2b0cecfa7d070eb8c552d558189bcb0861b7/diff:/var/lib/docker/overlay2/07f3ef394ef8d2d4a977a2a1540b09f7f01e3b3ec24059fccd0f6a08f7306421/diff:/var/lib/docker/overlay2/131634f57b6b2f77a8a672c75faf20aff914d1567ae580a176d0d960066f442c/diff:/var/lib/docker/overlay2/16654c000082cd658a2627dfa2bc831a70b9f292d60a4c21e80b8d7df478d1af/diff:/var/lib/docker/overlay2/cc6169e1ef5dee4954e5e36a2f4d5821d592c1e6f7b963d1e1a1e00432000ab8/diff:/var/lib/docker/overlay2/096deb23cc7d4638ac6da53ab3a40ef21b34b858900b54f49513848a13257e7f/diff:/var/lib/docker/overlay2/18ab68db63f5be9a52e5459aecc7abe770b9f2af927b658c3d3099397853a946/diff:/var/lib/docker/overlay2/c757dacfdbbe728340db16c7b65b325f61d032dc6e3d32596cd05975daaea6cd/diff:/var/lib/docker/overlay2/c8b39ca316fcfd4d893da728fed195b000deba6946500cbfc0805ecc3743a144/diff:/var/lib/docker/overlay2/f3b936e18affc42cc964652bde346f0daf4514d2eb140140803b70f5aadfe567/diff:/var/lib/docker/overlay2/1e44107fe16332bc3c19838858af62dd8f6fbb9e9748c8c914562b5ab26be149/diff:/var/lib/docker/overlay2/7ca8c406abb00cadca8d948b70effa51c3d97ac0d48d59b45bc1e09186c80ba8/diff:/var/lib/docker/overlay2/54a6d883618d69cbb8ea4a5bea731b5e37f3675fc09f6e0ad7c9f3894c0c61ff/diff:/var/lib/docker/overlay2/333c2e9f83c2d6574a8d722021ce51ffc214d18eb071a2ed876f6d5bdf7b26d7/diff:/var/lib/docker/overlay2/dcbbd1b4ebaccbdfbd7329536ccf75f1344fa6c0f2d7cda0845e1d2503feda4b/diff:/var/lib/docker/overlay2/7551b4350df7b4e7f2b45d2710fd295890f66122fe8319db27a7f64c5e4d6507/diff:/var/lib/docker/overlay2/b00375decd66685f05c77baac460b220c8b44110b811d7efb6932d5822e2c3ab/diff:/var/lib/docker/overlay2/d605a8654e204b8b825b7c54894e25575deefff33344e15210809d17fb0a081c/diff:/var/lib/docker/overlay2/d65ca34273b873e829fb0449e4ee921f6f249697c3a8dda1fc8848743c878e63/diff:/var/lib/docker/overlay2/3d0ee219794e42a956a1755f2e0c015a0878f715fb0078e690272c083923c605/diff:/var/lib/docker/overlay2/c4543043540c78351cb26fa731e0a0ebd6e4b942e1cdbddf30449d6218559614/diff:/var/lib/docker/overlay2/c3c273bdd83ce8ba12c9b7cea7dec67023745c870791ee27bc90390c151079e0/diff:/var/lib/docker/overlay2/31b80064082736757edda5e989b1ae52e19b19cd631a594118fa0da2d3d946f1/diff:/var/lib/docker/overlay2/992a57c9d7478a7c08e913847941899edd8db3bc6b93d6287893b8cb18f9f0c1/diff",
                "MergedDir": "/var/lib/docker/overlay2/c0645860b314b36a7f77a3bb4110af6892f6499336d77806a16649db624ab18f/merged",
                "UpperDir": "/var/lib/docker/overlay2/c0645860b314b36a7f77a3bb4110af6892f6499336d77806a16649db624ab18f/diff",
                "WorkDir": "/var/lib/docker/overlay2/c0645860b314b36a7f77a3bb4110af6892f6499336d77806a16649db624ab18f/work"
            },
            "Name": "overlay2"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:77b174a6a187b610e4699546bd973a8d1e77663796e3724318a2a4b24cb07ea0",
                "sha256:bed8257b5767b95eacdcc39fd5cdec458878fb8a60da0470fd10a150fa63710f",
                "sha256:a5b51dee02190c5a9d590ea43b4c5dc7a03179b73a2f3a29b46fc0cf7ea1aba4",
                "sha256:bea84f30e74ffc31748958d54a94ca70f3b15f81fcf2ca3b0d583057aab0a3a8",
                "sha256:7d92b8f47c1b16f3cf6a0827fec722d01d8ed2b9eed3d64b3327934f5f5cc72f",
                "sha256:99f080e35296351c83ac12a8de2658414508f8759cca28b09dbe55bc5dc0422f",
                "sha256:11de5d76ba5ba1f0c611584084021b40574bd9488aed9bda9091c37e782d2f61",
                "sha256:f57f22d83523f47f7bb41b4ede834ee8ea264ba6ecfe626679c88f36b59209d9",
                "sha256:a5fa2f76c3e56e128c78e78c6b413f2fc25cd0a8beaac3f2b3ed8f6ad2c4bf0e",
                "sha256:f684d6aa6f3fa8a2d6750b4b3ec9ffad6741a9d499d8c65cdf6d685fcb8e4112",
                "sha256:afbf79fcffbfbd2887885abcdc9a243c5aeade32111742b2cf5f61be86bfca58",
                "sha256:fc20bbfa069c0d5c5233f27e3775e2a22bd617ff0a4bc1c1040a0554b422187d",
                "sha256:7ef874a6378e96fcd14e400b5c4e2c3bcd4a1e0bb6252da52a5c17883e75398d",
                "sha256:03a7f8c8c8182ed955bd67c1c6f2d2ec44eacf8afb469755422cfbc7caeef49d",
                "sha256:e7b29b834801dcfa58b73e11735daf93a71b176ba607e44c6846238e6cc701ab",
                "sha256:bf26537e5d46bb55e73d5bfa52eab3db7e6cd01825f4d08c7fdec1f02b640f17",
                "sha256:7d7cffd21d11449191cc497d29f49930630991400ae500b1c9c0809c9501d694",
                "sha256:3938899ffb224e520de7c98260b1706ef1da48254ee99b8994615fcc32077cdc",
                "sha256:a2b8a2c065f9edf2a42f8daba6f23a5c3b802fcd5a7c8ba548b4ed2ac7768396",
                "sha256:f0e7a20892cde66c0e71a07acdc674eef64821c3f693a6f1735867aef5218068",
                "sha256:daa7fcce0a418f6a16ec726a8472df0c2ab234535b8396e1bfde138d6f0fcf94",
                "sha256:87b8d9d7803f4e0058524f7ff299df291ada917619b528549219c6c753344144",
                "sha256:c9eb97e6b605bcf8340ab7f1e29e068b8a74b78c9ee003019b42ca78734ce2e3",
                "sha256:49d468591bed7fd38034defca80fcb11d0ef2a16ffb5f249a0ad7d05aa5e126d",
                "sha256:ad618da2bd611e91ab14d839c05b41161149ff85e30d18cb6e0bf686815ac499",
                "sha256:ca5d0e50fb35f72887a9097f9a356026af9c28aa877bcd9f2b127a224a39f2e5",
                "sha256:9c0c4b90e4c0b44871c0321617daab4a2b3d22d28cd019b01291c03d76f8bc5e",
                "sha256:954a707e2af2f98c63f3f56a45ab37a702f5ee80d32d00b9b9423c8660ebcf1b",
                "sha256:e6a27a34d676cd2ea291a1234e99e27e39af151d5ad766db55287b743ee938f9",
                "sha256:1e6d36f0b3c8441d83140d468a96c42e5e31e9c019530a00644a8c2427336a54",
                "sha256:24ec0681b3d4fd3fd96af7a7b33537c1105ca4703538d37dbdc41427216c8788",
                "sha256:1cc6fe6a95407870ac0e62632e40ba0bdccadf906c2e13d0e710a2f5a953e764",
                "sha256:7c474acd1827c0f1a21b0aef656a0e70dc0e232c6b7fd4f7b99a40d767d06267",
                "sha256:9e3d5698713d8c9e585dbef76f21e877f5eebe08d5a749ba5c92a335d7770921",
                "sha256:383db0861f4009802e039aade448fb1cf3146276441eeb50f12fc0870bffceb1",
                "sha256:55e6e1661159279b6d973d27e5237878dc3f900e0de7e5da1105847174c59e92",
                "sha256:3da5a0c8da5f0c7a58429199da0d789f7c4b35a0f894d39941654db07a770c62",
                "sha256:3594b380042ea6e02e27b46097f11a466f66ebeaec7d670996ae494a8b18be90",
                "sha256:9f8603c2fb7f3e8f68c3e897beb1caf2f73092a56b65100fd2830be7098140c2"
            ]
        },
        "Metadata": {
            "LastTagTime": "2022-08-11T03:32:40.283929917Z"
        }
    }
]
[root@i2midev6 container]# 
```

Install "[dive](https://github.com/wagoodman/dive)" to insspect images

```
curl -OL https://github.com/wagoodman/dive/releases/download/v0.9.2/dive_0.9.2_linux_amd64.rpm
rpm -i dive_0.9.2_linux_amd64.rpm
```

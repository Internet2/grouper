From grouper-pspng-testing-config

COPY grouper.installer.properties /opt/grouper

RUN PATH=/usr/lib/jvm/zulu-8/bin/:$PATH && \
    mkdir /tmp/grouper-tarballs && \
    cd /opt/grouper && \
    yes “” | java -cp .:grouperInstaller.jar edu.internet2.middleware.grouperInstaller.GrouperInstaller && \
    rm -rf /tmp/grouper-tarballs

FROM unicon/grouper-daemon:2.3.0

MAINTAINER John Gasper <jgasper@unicon.net>

COPY conf/ /opt/grouper.apiBinary-$GROUPER_VERSION/conf/

WORKDIR /opt/grouper.apiBinary-$GROUPER_VERSION/

CMD ["/bin/sh", "bin/gsh"]

---
title: "Grouper v2.5 container docker-compose example"
space: Grouper
pageId: 28554995
version: 8
lastUpdated: 2026-07-01T05:39:11.072Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554995/Grouper+v2.5+container+docker-compose+example
---

The docker-compose utility offers a few improvements over using basic docker commands to start and stop containers. One is that it keeps all your container startup options in a single file. So you don't need to type or paste in a long command line when you need to bring up a container. Another advantage is that it can bring up multiple containers together as a single command. Optional dependencies can be made between two containers, so that a container will bring up any dependencies it needs without remembering to do it manually. The `docker-compose up` command brings them all up, and either `docker-compose down` or control-C will nicely stop them. Stopped containers will retain their data, until something changes in their configuration, after which they will be wiped and recreated. A third important advantage is that an internal network can be created for the container group, including dns aliases. This means you don't need to know the IP address of the container running your database. Rather, you can configure an alias for it, and can reference that dns name.

Sample docker-compose.yml file:

```yml
version: "2.4"

services:
  db:
    image: postgres
    volumes:
      - ./db-data:/var/lib/postgresql
    environment:
      - POSTGRES_USER=grouper_v2_5
      - POSTGRES_PASSWORD=abc-2.5-xyz
      - POSTGRES_DB=grouper_v2_5
    ports:
      - "5432:5432"
    networks:
      - testing_net
  daemon:
    image: i2incommon/grouper:2.5.29
    depends_on:
      - db
    environment:
      - GROUPER_LOG_TO_HOST=true
    volumes:
      - ./root:/opt/grouper/slashRoot
      - ./logs/grouper-daemon-logs:/opt/grouper/logs
    restart: unless-stopped
    command: "bash -c 'sleep 10 && daemon'"
    networks:
      - testing_net
  web:
    image: i2incommon/grouper:2.5.29
    depends_on:
      - db
    environment:
      - RUN_SHIB_SP=false
    volumes:
      - ./root:/opt/grouper/slashRoot
      - ./logs/grouper-ui-logs:/opt/grouper/logs
    ports:
      - "8080:8080"
    restart: unless-stopped
    command: "bash -c 'sleep 10 && ui'"
    networks:
      - testing_net
  ws:
    image: i2incommon/grouper:2.5.29
    depends_on:
      - db
    environment:
      - RUN_SHIB_SP=false
    volumes:
      - ./root:/opt/grouper/slashRoot
      - ./logs/grouper-ws-logs:/opt/grouper/logs
    ports:
      - "8081:8080"
    restart: unless-stopped
    command: "bash -c 'sleep 10 && ws'"
    networks:
      - testing_net
 
networks:
  testing_net:
```

A similar config for Grouper 2.4.x:

```yml
version: "3"
services:
  db:
    image: postgres
    environment:
      - POSTGRES_USER=grouper_v2_4
      - POSTGRES_PASSWORD=abc-2.4-xyz
      - POSTGRES_DB=grouper_v2_4
    ports:
      - "5432:5432"
    networks:
      - testing_net
  init:
    image: tier/grouper:2.4.0-a93-u56-w11-p12-20200310
    depends_on:
      - db
    volumes:
      - ./conf:/opt/grouper/conf
      - ./logs:/opt/grouper/logs
    command: "bash -c 'sleep 10 && gsh -registry -check -runscript -noprompt'"
    networks:
      - testing_net
  daemon:
    image: tier/grouper:2.4.0-a93-u56-w11-p12-20200310
    depends_on:
      - db
    volumes:
      - ./conf:/opt/grouper/conf
      - ./logs:/opt/grouper/logs
    restart: unless-stopped
    command: "bash -c 'sleep 10 && daemon'"
    networks:
      - testing_net
  web:
    image: my-grouper-2.4.0p93:latest
    depends_on:
      - db
    volumes:
      - ./conf:/opt/grouper/conf
      - ./logs:/opt/grouper/logs
    ports:
      - "8080:8080"
    restart: unless-stopped
    command: "bash -c 'sleep 10 && echo ok && ui'"
    networks:
      - testing_net

networks:
  testing_net:

```

Note that any container with a `networks` reference will be able to refer to other containers by their container name, without needing to know the IP address. The jdbc url used in the above example was simply jdbc:postgresql://db:5432/grouper_v2_5 .

## Initialization and setup

The following sets up a minimal container with logs for each container going to the host. It also stores the Postgres data in a local volume for persistence; without this, data inside the db container may persist after stopping and restarting, but will eventually get erased and rebuilt at some point if the docker-compose.yml file ever changes.

```bash
mkdir -p ./db-data ./logs/grouper-ws-logs ./logs/grouper-ui-logs ./logs/grouper-daemon-logs ./root ./root/opt/grouper/grouperWebapp/WEB-INF/classes
chmod a+w logs/* db-data

# Create a morphString length 16 password with possible numbers, upper and lower letters
echo encrypt.key=$(apg -a 1 -n 1 -m 16 -x 16 -M ncl) > ./root/opt/grouper/grouperWebapp/WEB-INF/classes/morphString.properties

# Create hibernate properties
cat > ./root/opt/grouper/grouperWebapp/WEB-INF/classes/grouper.hibernate.properties
  hibernate.connection.url = jdbc:postgresql://db:5432/grouper_v2_5
  hibernate.connection.username         = grouper_v2_5
  hibernate.connection.password         = abc-2.5-xyz
  registry.auto.ddl.upToVersion = 2.5.*
  grouper.is.ui.basicAuthn=true
  grouper.is.ws.basicAuthn=true

# Allow editing of the config from the UI from host container
cat > ./root/opt/grouper/grouperWebapp/WEB-INF/classes/grouper-ui.properties
  grouperUi.configurationEditor.sourceIpAddresses = 192.168.0.0/16

docker-compose up db &
docker-compose up daemon &

docker ps
docker exec -it wiki-test_daemon_1 gsh

   grouperPasswordSave = new GrouperPasswordSave();
   grouperPasswordSave.assignUsername("GrouperSystem").assignPassword("GrouperSystem").assignEntityType("username");
   grouperPasswordSave.assignApplication(GrouperPassword.Application.UI);
   new Authentication().assignUserPassword(grouperPasswordSave);

docker-compose up web &
wget --http-user=GrouperSystem --http-password=GrouperSystem  http://localhost:8080/grouper -O -
```

As an alternative to saving the db data in a volume, an existing database container outside of the scope of docker-compose can be used in a setup without being managed by it. This may be useful for a generic database that could apply to multple docker-compose setups. It will need to be started ahead of time. Then, docker-compose containers can use it via:

```yml
  web:
    image: i2incommon/grouper:2.5.27
    external_links:
      - my-db-container:db
    ...

```

When using this method, you will want to avoid the `--rm` option when creating the container, which would delete it as soon as it stopped. Without this option, the container will remain in the stopped state, and can be restarted multiple times. To view stopped containers, use the `docker ps -a` command which lists all containers, not just the running ones. The external link will refer to the container name, which will be either the name you set up with `--name` upon creation, or the randomly generated one.

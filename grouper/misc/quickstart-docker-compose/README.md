# Grouper quickstart demo

This directory contains a minimal docker-compose setup, that can run a Grouper v5 container running the UI, WS, and daemon in a single container.
It includes a PostreSQL database, an openLDAP server with about 35 demo users. The demo uses this LDAP as a subject source, so these users can be
resolved. The running container represents the default Grouper system, with the database initialized, and default objects in the etc: folder created.

## Running the demo

```
docker compose up -d
```

## Stopping the demo

```
docker compose down
```

## Accessing the UI and WS

In this quickstart configuration, the only user with access is GrouperSystem, with password `pass`. To set a UI or WS password for one of the LDAP
users, you will need GSH and the GrouperPassword API to set one.

UI: http://localhost:8080/grouper/

WS: http://localhost:8080/grouper/servicesRest/v5_20_6/{endpoint}


## Accessing the shell and GSH

Shell: `docker compose exec grouper bash`

GSH: `docker compose exec grouper bin/gsh.sh`

---
title: "Selenium testing example"
space: GrIntDev
pageId: 48792774
version: 2
lastUpdated: 2026-07-12T07:01:15.918Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792774/Selenium+testing+example
---

This could be used in Grouper at some point

```
David Shafer  12:37 PM
@mchyzer, you’d asked about our Selenium configuration-- We’re using Selenium for acceptance testing with a Ruby on Rails project. To drive Selenium, we use Capybara (which works well for Rails, but you might need an alternative). Here’s the docker-compose configuration we use, which brings up a PostgreSQL database, the Selenium server (using the Selenium standalone Firefox configuration), and the application container (configured to run the test suite): (edited) 
:+1::skin-tone-3:
1

12:40
docker-compose.yml 
version: "3"
​
networks:
  client:
  db:
​
services:
  db:
    image: postgres:9.6
    environment:
      POSTGRES_PASSWORD: "dockerci"
    networks:
      - db
    ports:
      - "5432:5432"
​
  firefox:
    image: selenium/standalone-firefox:3.141.59-20200409
    environment:
      MOZ_HEADLESS: 1
      START_XVFB: "false"
    networks:
      - client
    ports:
      - "4444:4444"
      - "5900:5900"
    volumes:
      - "/dev/shm:/dev/shm"
​
  test:
    image: siteadmin
    command: >-
      bash -c "rails db:setup && bundle exec rspec \
        --format d \
        --force-color \
        --profile"
    depends_on:
      - db
      - firefox
    environment:
      DB_CONNINFO: >-
        {
          "dbname": "fmdev",
          "engine": "postgres",
          "host": "db",
          "password": "dockerci",
          "port": 5432,
          "username": "postgres"
        }
      RAILS_LOG_TO_STDOUT: "true"
      SELENIUM_REMOTE_URL: "http://firefox:4444/wd/hub"
      # Note: If below is set to "app" (or any other entry in the HSTS preload
      # list), then Firefox will force HTTPS
      # (https://stackoverflow.com/questions/53961887/selenium-firefox-driver-forces-https)
      CAPYBARA_APP_HOST: "test"
      CAPYBARA_SERVER_HOST: "0.0.0.0"
    networks:
      - client
      - db
    ports:
      - "3000:3000"
```

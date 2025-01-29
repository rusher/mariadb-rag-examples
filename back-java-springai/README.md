## Spring AI use with MariaDB database store and openAI

These example will retrieve MariaDB documentation (4000+ pages) and using openAI Model, store it in MariaDB database.
(This requires mariadb 11.7 database).

change application.yml to corresponds to DB access

set your OPENAI key to env variable OPEN_AI_KEY
```
export OPEN_AI_KEY=...
```

set application.yml mariadb info and set initialize-store to true in first launch



run the example using
```
mvn spring-boot:run 
# or with JDBC logs 
mvn spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.mariadb=DEBUG
```

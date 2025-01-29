## langcain use with MariaDB database store and openAI

These example will retrieve MariaDB documentation (4000+ pages) and using openAI Model, store it in MariaDB database.
(This requires mariadb 11.7 database).

change application.yml to corresponds to DB access

set your OPENAI key to env variable OPEN_AI_KEY
```
export OPENAI_API_KEY=...
```

run the example using



```
# install c/c connector, then

pip install -r requirements.txt

```

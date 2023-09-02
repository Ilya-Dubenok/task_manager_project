FROM postgres:15.3-alpine3.18
COPY ./user-service/ddl/ /docker-entrypoint-initdb.d/
COPY ./user-service/dml/ /docker-entrypoint-initdb.d/
COPY ./audit-service/ddl/ /docker-entrypoint-initdb.d/
COPY ./task-service/ddl/ /docker-entrypoint-initdb.d/
COPY ./report-service/ddl/ /docker-entrypoint-initdb.d/

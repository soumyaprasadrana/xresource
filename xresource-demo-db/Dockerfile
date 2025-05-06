FROM postgres:16

ENV POSTGRES_DB=xresourcedemo
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=Full@ccess123

# Copy init script
COPY sql/schema.sql /docker-entrypoint-initdb.d/init.sql

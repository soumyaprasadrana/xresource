docker run -d \
  --name xresourcedemo-postgres \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=Full@ccess123 \
  -e POSTGRES_DB=xresourcedemo \
  -v C:\xresourcedemo-db\xresourcedemo-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  xresourcedemo-db:latest

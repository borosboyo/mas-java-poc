#!/bin/bash
set -e

# Grant CREATEDB privilege to myuser so it can create tenant databases
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    ALTER USER myuser CREATEDB;
    \du
EOSQL

echo "Database initialization complete. User 'myuser' now has CREATEDB privilege."


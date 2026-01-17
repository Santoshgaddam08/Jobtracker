#!/bin/bash
cd /app/frontend
exec /usr/local/bin/http-server -p 3000 -c-1

#!/bin/bash
cd /app/frontend
exec /usr/bin/http-server -p 3000 -c-1

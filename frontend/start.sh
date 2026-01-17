#!/bin/bash
cd /app/frontend
exec python3 -m http.server 3000

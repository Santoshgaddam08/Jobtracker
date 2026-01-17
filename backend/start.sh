#!/bin/bash
cd /app/backend
exec /usr/bin/java -jar target/job-tracker-backend-0.0.1.jar --spring.profiles.active=dev

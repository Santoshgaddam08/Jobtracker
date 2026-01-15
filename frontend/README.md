# JobTracker Frontend 

This frontend is intentionally lightweight and GitHub Pages-friendly.

## Features
- Login / Register (JWT)
- Dashboard: list + create + delete applications
- Detail: notes + reminders + status history
- Analytics summary
- Realtime events via STOMP (WebSocket)

## Configure API / WebSocket
Open the app → Settings:
- API Base URL (example): `http://localhost:8080`
- WebSocket URL (example): `ws://localhost:8080/ws`

## Run locally
You can serve `frontend/` with any static server.
Example (VS Code Live Server / Python http server).

## Deploy 
You can deploy using GitHub Pages by serving `frontend/` as the Pages root.

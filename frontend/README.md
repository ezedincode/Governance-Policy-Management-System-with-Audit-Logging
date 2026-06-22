# Governance Policy Management — Lite UI

A lightweight React + Vite single-page app for the Governance Policy Management
microservices system. All requests go through the **API Gateway** and the JWT
access token is persisted in `localStorage`.

## Features

- **Register** (`POST /auth/register`) — username, email, password, role (`USER | ADMIN | AUDITOR`)
- **Sign in** (`POST /auth/login`) — stores `accessToken` in `localStorage`
- **List all policies** (`GET /policies`)
- **Search policy by id** (`GET /policies/{id}`)
- **Create policy** (`POST /policies`) — `{ title, description, createdBy }`
- **Submit / Approve / Reject** (`POST /policies/{id}/submit | approve | reject`)
- Auto-logout on expired/invalid token (HTTP 401)

## Prerequisites

The backend services must be running and registered with Eureka so the gateway
(`http://localhost:8088`) can route requests:

- Eureka Server (8761), Gateway (8088), Auth Service, Governance Service, User Service, Audit Service, Redis

## Getting started

```bash
cd frontend
npm install
npm run dev
```

The app starts on http://localhost:5173.

## Configuration

The gateway base URL is configured in `frontend/.env`:

```
VITE_GATEWAY_URL=http://localhost:8088
```

Change it if your gateway runs elsewhere (e.g. a remote host).

## How the token is handled

- On successful login the `accessToken` returned by `/auth/login` is saved to
  `localStorage` under the key `gpm_access_token`.
- An axios request interceptor reads the token from `localStorage` and attaches
  it as `Authorization: Bearer <token>` to **every** request, so all policy
  calls satisfy the gateway's `AuthenticationFilter`.
- A response interceptor clears the token and redirects to `/login` on any
  `401 Unauthorized` response (expired or invalid token).

## Project structure

```
frontend/
├── .env                      # VITE_GATEWAY_URL
├── index.html
├── vite.config.js
├── package.json
└── src/
    ├── api.js                # axios instance + token storage + interceptors
    ├── policies.js           # policy API helpers
    ├── AuthContext.jsx       # login/register/logout + current user
    ├── App.jsx               # routes
    ├── main.jsx              # entry
    ├── styles.css
    ├── components/
    │   ├── Layout.jsx        # top nav + logout
    │   ├── ProtectedRoute.jsx
    │   ├── Alert.jsx
    │   └── StatusBadge.jsx
    └── pages/
        ├── Login.jsx
        ├── Register.jsx
        ├── PolicyList.jsx     # list + search by id
        ├── PolicyCreate.jsx
        └── PolicyDetail.jsx   # submit / approve / reject
```

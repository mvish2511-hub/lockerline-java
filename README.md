# LockerLine

A bank of 48 parcel lockers in an apartment block: couriers drop, residents collect with a code that expires.

## Run it in a container

You need Docker and nothing else: the image builds the app itself (in a first stage, on a JDK) and runs it on a JRE.
From this folder:

    docker build -t lockerline .
    docker run -p 8080:8080 lockerline

Open http://localhost:8080/ for the wall. That run keeps its database inside the container and loses it with the
container.

## Keep the parcels, and configure it from one file

Every setting can come from the environment; the table below lists the ones you may need to change. Copy
`.env.example` to `.env`, change what you need, and run with the database on a named volume at `/data`:

    cp .env.example .env
    docker run -d --name lockerline -p 8080:8080 -v lockerline-data:/data --env-file .env lockerline

| Variable | What it sets | Default |
|---|---|---|
| `DATABASE_URL` | the JDBC URL of the H2 file | `jdbc:h2:file:./data/lockerline` (`jdbc:h2:file:/data/lockerline` in `.env.example`) |
| `FREE_WINDOW_HOURS` | how long a pickup code stays valid after the drop | `48` |
| `CODE_HASH_KEY` | the key pickup codes are hashed with | `dev-only-change-me` - set your own |
| `HOST` | the address the server listens on | `127.0.0.1` (`0.0.0.0` in the image) |
| `PORT` | the port | `8080` |

To change the free window: edit the line in `.env`, then `docker rm -f lockerline` and run the same `docker run`
again. Nothing is rebuilt, and the parcels are still there, because they live on the volume.

Logs go to stdout: `docker logs lockerline` shows one line per request and one per event - never a pickup code.

### How pickup codes are stored

A code has only a million possible values, so a plain hash of a leaked table is reversed in seconds. Codes are stored
as HMAC-SHA-256 with `CODE_HASH_KEY`, which lives in the environment and never in the database: without the key the
stored hashes are useless. Because the hash is keyed but has no per-row salt, a presented code is found with one
equality lookup (bcrypt would mean checking the code against every waiting parcel), and the final comparison is
constant-time.

## One parcel, from drop to collection

With the container running on port 8080:

    curl -s http://localhost:8080/actuator/health
    # {"status":"UP"}

    curl -s "http://localhost:8080/towers/A/doors?size=M&state=free"
    # {"tower":"A","free":20,"doors":[{"no":"D21","size":"M","state":"free"}, ...]}

    curl -s -X POST http://localhost:8080/drops -H 'content-type: application/json' -d '{"sizeClass":"M","flat":"302"}'
    # {"parcelId":"P001","door":"D21","sizeClass":"M","flat":"302","droppedAt":"...","code":"482913","expiresAt":"..."}

    curl -s -X POST http://localhost:8080/pickups -H 'content-type: application/json' -d '{"code":"482913"}'
    # {"parcelId":"P001","door":"D21","openedAt":"...","dwellSeconds":95}

    curl -s http://localhost:8080/parcels/P001
    # {"parcelId":"P001","state":"collected", ... "events":[dropped, code_issued, opened]}

Use the code your own drop returned. The API docs are at http://localhost:8080/swagger-ui.html.

<!-- learntube:start -->
---

[![tests](https://github.com/mvish2511-hub/lockerline-java/actions/workflows/tests.yml/badge.svg)](https://github.com/mvish2511-hub/lockerline-java/actions/workflows/tests.yml)

### Progress

- [x] Ticket 1 · Forty-eight doors, and a wall you can look at
- [x] Ticket 2 · A courier asks for a door
- [x] Ticket 3 · The code that dies at midnight
- [x] Ticket 4 · Three ways to say no
- [x] Ticket 5 · The door opens, and the clock stops
- [x] Ticket 6 · A notebook nobody can rewrite
- [x] Ticket 7 · One file becomes an app
- [x] Ticket 8 · The rule table, and then the rule changes
- [x] Ticket 9 · It runs on a machine that isn't yours
- [x] Ticket 10 · It survives being switched off

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->

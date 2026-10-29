# LockerLine

A bank of 48 parcel lockers in an apartment block: couriers drop, residents collect with a code that expires.

## How pickup codes are stored

A code has only a million possible values, so a plain hash of a leaked table is reversed in seconds. Codes are stored
as HMAC-SHA-256 with `code-hash-key` (a setting, never in the database): without the key the stored hashes are
useless. Because the hash is keyed but has no per-row salt, a presented code is found with one equality lookup
(bcrypt would mean checking the code against every waiting parcel), and the final comparison is constant-time.

<!-- learntube:start -->
---

[![tests](https://github.com/mvish2511-hub/lockerline-java/actions/workflows/tests.yml/badge.svg)](https://github.com/mvish2511-hub/lockerline-java/actions/workflows/tests.yml)

### Progress

- [x] Ticket 1 · Forty-eight doors, and a wall you can look at
- [x] Ticket 2 · A courier asks for a door
- [x] Ticket 3 · The code that dies at midnight
- [x] Ticket 4 · Three ways to say no
- [ ] Ticket 5 · The door opens, and the clock stops
- [ ] Ticket 6 · A notebook nobody can rewrite
- [ ] Ticket 7 · One file becomes an app
- [ ] Ticket 8 · The rule table, and then the rule changes
- [ ] Ticket 9 · It runs on a machine that isn't yours
- [ ] Ticket 10 · It survives being switched off

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->

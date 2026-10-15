## Deploying the frontend and the ticket service next to the existing API (Hetzner, Caddy)

Result: `https://globoticket-tst.duckdns.org/app/catalog.html`. The existing `restful-booker-tst` site is not touched.

Two containers (compose project `globoticket`):

| Container | Role | Reachable |
|---|---|---|
| `globoticket-frontend` | nginx: serves `/app/`, proxies `/api/` to the ticket service | `127.0.0.1:5001`, public through Caddy |
| `globoticket-ticket-service` | creates the PDF tickets (Java 21, about 60 MB RAM, limited to 256 MB) | only from the frontend container, no published port |

Caddy needs no change for the ticket service: the browser and the tests call `https://<your-subdomain>/api/tickets`, nginx forwards it.

### First deployment
1. **DNS:** on duckdns.org add a new subdomain (e.g. `globoticket-tst`) with the IP of your server. A free account allows several subdomains.
2. **Check the server has room:** `free -h` (about 300 MB free for the ticket service and the first build), `df -h` (about 1 GB for the image layers), `ss -ltn | grep 5001` (port free).
3. **Copy the code to the server** (`web/`, `ticket-service/` and `deploy/` are needed):
   ```
   git clone <repo-url> playwright_introduction     # or: scp -r web ticket-service deploy user@server:~/playwright_introduction/
   cd playwright_introduction
   ```
4. **Start the containers** (the first build downloads the Maven dependencies and takes a few minutes):
   ```
   docker compose -f deploy/docker-compose.yml up -d --build
   curl -I http://127.0.0.1:5001/app/catalog.html     # expect HTTP 200
   curl http://127.0.0.1:5001/api/health              # expect {"status":"UP"}
   ```
5. **Caddy:** append `deploy/Caddyfile.snippet` to your Caddyfile (with your subdomain), then reload without downtime:
   ```
   sudo caddy reload --config /etc/caddy/Caddyfile
   ```
6. **Use it in the tests:**
   ```
   mvn test -Dapp.url=https://globoticket-tst.duckdns.org/app/
   ```
   The tests derive the ticket service URL from `app.url` (same host, path `/api/`).

### Updating an earlier deployment (frontend only, before the ticket service existed)
The compose project is now named `globoticket`, so the old container has to go once, otherwise Docker reports a name conflict. Do this on the server, in the repository directory, before the first `up`:
```
docker rm -f globoticket-frontend
git pull
docker compose -f deploy/docker-compose.yml up -d --build
```
The site is down for a few seconds. Other containers on the server are not affected.

### Later updates
```
git pull && docker compose -f deploy/docker-compose.yml up -d --build
docker image prune -f        # optional: remove old layers
```

### Switching the planted bugs
The ticket service has five planted bugs for the bug hunt (see the instructor note in the main README). Switching needs no rebuild:
```
BUGS=all docker compose -f deploy/docker-compose.yml up -d                 # all five on
BUGS=total,vat docker compose -f deploy/docker-compose.yml up -d           # a selection
docker compose -f deploy/docker-compose.yml up -d                          # all off
```
Note that `up -d` without `BUGS` always resets the service to correct behaviour; to keep a setting across updates, put it in `deploy/.env` (`BUGS=all`).

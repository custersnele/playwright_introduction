## Deploying the frontend next to the existing API (Hetzner, Caddy)

Result: `https://globoticket-tst.duckdns.org/app/catalog.html`. The existing `restful-booker-tst` site is not touched.

1. **DNS:** on duckdns.org add a new subdomain (e.g. `globoticket-tst`) with the IP of your server. A free account allows several subdomains, so there is no need to reuse or rename the API one.
2. **Copy the code to the server** (only `web/` and `deploy/` are needed):
   ```
   git clone <repo-url> playwright_introduction     # or: scp -r web deploy user@server:~/playwright_introduction/
   cd playwright_introduction
   ```
3. **Start the container:**
   ```
   docker compose -f deploy/docker-compose.yml up -d --build
   curl -I http://127.0.0.1:5001/app/catalog.html     # expect HTTP 200
   ```
   Port 5001 is bound to 127.0.0.1, so it is not exposed publicly except through Caddy. Check that 5001 is not used by something else (`ss -ltn | grep 5001`).
4. **Caddy:** append `deploy/Caddyfile.snippet` to your Caddyfile (with your subdomain), then reload without downtime:
   ```
   sudo caddy reload --config /etc/caddy/Caddyfile
   ```
   (or `sudo systemctl reload caddy`). Caddy gets the HTTPS certificate automatically; ports 80 and 443 are already open since the API works.
5. **Use it in the tests:**
   ```
   mvn test -Dapp.url=https://globoticket-tst.duckdns.org/app/
   ```
   and update the SUT URL on the slides.

Update after changing `web/`: `git pull && docker compose -f deploy/docker-compose.yml up -d --build`.

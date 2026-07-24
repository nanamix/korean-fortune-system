#!/bin/sh
set -eu

address="${OPENBAO_ADDR:-https://jyha-macbook-pro-2019.tail21796d.ts.net:8443}"
edge_auth_mode="${OPENBAO_EDGE_AUTH_MODE:-tailscale}"
bootstrap_dir="${OPENBAO_BOOTSTRAP_DIR:-/run/openbao-bootstrap}"
output_dir="${OPENBAO_OUTPUT_DIR:-/run/openbao-secrets}"
secret_path="${OPENBAO_SECRET_PATH:-secret/data/projects/korean-fortune-system/prod}"
required_keys="${OPENBAO_REQUIRED_KEYS:-MYSQL_ROOT_PASSWORD MYSQL_PASSWORD GRAFANA_PASSWORD JWT_SECRET}"

case "$address" in
  https://*) ;;
  *) echo "openbao_secret_render_failed:OPENBAO_ADDR must use https" >&2; exit 1 ;;
esac
case "$secret_path" in
  *[!a-zA-Z0-9_./-]*|'') echo "openbao_secret_render_failed:invalid secret path" >&2; exit 1 ;;
esac
case "$edge_auth_mode" in
  tailscale|cloudflare) ;;
  *) echo "openbao_secret_render_failed:invalid OPENBAO_EDGE_AUTH_MODE" >&2; exit 1 ;;
esac

mkdir -p "$output_dir"
chmod 0711 "$output_dir"

curl_config="$output_dir/.curl-config"
login_request="$output_dir/.login-request.json"
login_response="$output_dir/.login-response.json"
secret_response="$output_dir/.secret-response.json"

cleanup() {
  rm -f "$curl_config" "$login_request" "$login_response" "$secret_response"
}
trap cleanup EXIT HUP INT TERM

read_bootstrap() {
  name="$1"
  file="$bootstrap_dir/$name"
  [ -f "$file" ] || { echo "openbao_secret_render_failed:missing bootstrap $name" >&2; exit 1; }
  value="$(tr -d '\r\n' < "$file")"
  [ -n "$value" ] || { echo "openbao_secret_render_failed:empty bootstrap $name" >&2; exit 1; }
  case "$value" in
    *[\"\\]*) echo "openbao_secret_render_failed:invalid bootstrap $name" >&2; exit 1 ;;
  esac
  printf '%s' "$value"
}

cf_id=""
cf_credential=""
if [ "$edge_auth_mode" = "cloudflare" ]; then
  cf_id="$(read_bootstrap cf-access-client-id)"
  cf_credential="$(read_bootstrap cf-access-client-secret)"
fi

write_curl_config() {
  token="${1:-}"
  {
    if [ "$edge_auth_mode" = "cloudflare" ]; then
      printf 'header = "CF-Access-Client-Id: %s"\n' "$cf_id"
      printf 'header = "CF-Access-Client-Secret: %s"\n' "$cf_credential"
    fi
    printf 'header = "Content-Type: application/json"\n'
    [ -z "$token" ] || printf 'header = "X-Vault-Token: %s"\n' "$token"
  } > "$curl_config"
  chmod 0600 "$curl_config"
}

jq -n \
  --rawfile role_id "$bootstrap_dir/approle-role-id" \
  --rawfile secret_id "$bootstrap_dir/approle-secret-id" \
  '{role_id: ($role_id | gsub("[\\r\\n]+$"; "")), secret_id: ($secret_id | gsub("[\\r\\n]+$"; ""))}' \
  > "$login_request"
chmod 0600 "$login_request"

write_curl_config
curl --fail --silent --show-error \
  --config "$curl_config" \
  --request POST \
  --data-binary "@$login_request" \
  "$address/v1/auth/approle/login" \
  > "$login_response" || { echo "openbao_secret_render_failed:login request" >&2; exit 1; }

token="$(jq -er '.auth.client_token | select(type == "string" and length > 0)' "$login_response")" \
  || { echo "openbao_secret_render_failed:login token missing" >&2; exit 1; }
carriage_return="$(printf '\r')"
case "$token" in
  *\"*|*\\*|*"$carriage_return"*|*'
'*) echo "openbao_secret_render_failed:invalid login token" >&2; exit 1 ;;
esac

write_curl_config "$token"
curl --fail --silent --show-error \
  --config "$curl_config" \
  "$address/v1/$secret_path" \
  > "$secret_response" || { echo "openbao_secret_render_failed:secret request" >&2; exit 1; }

jq -e '
  .data.data
  | type == "object"
    and all(to_entries[]; (.key | test("^[A-Z][A-Z0-9_]*$")) and (.value | type == "string"))
' "$secret_response" >/dev/null \
  || { echo "openbao_secret_render_failed:invalid secret payload" >&2; exit 1; }

find "$output_dir" -maxdepth 1 -type f ! -name '.*' -delete
jq -r '.data.data | to_entries[] | [.key, (.value | @base64)] | @tsv' "$secret_response" |
while IFS="$(printf '\t')" read -r key encoded; do
  target="$output_dir/$key"
  printf '%s' "$encoded" | base64 -d > "$target"
  chmod 0444 "$target"
done

for key in $required_keys; do
  case "$key" in
    *[!A-Z0-9_]*|'') echo "openbao_secret_render_failed:invalid required key" >&2; exit 1 ;;
  esac
  [ -s "$output_dir/$key" ] || { echo "openbao_secret_render_failed:missing required key $key" >&2; exit 1; }
done

secret_count="$(find "$output_dir" -maxdepth 1 -type f ! -name '.*' | wc -l | tr -d ' ')"
echo "openbao_secret_render_complete:count=$secret_count"

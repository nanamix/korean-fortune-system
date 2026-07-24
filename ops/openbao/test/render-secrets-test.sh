#!/bin/sh
set -eu

if [ "${OPENBAO_TEST_FAKE_CURL:-}" = "1" ]; then
  config=""
  url=""
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --config)
        config="$2"
        shift 2
        ;;
      http*)
        url="$1"
        shift
        ;;
      *)
        shift
        ;;
    esac
  done

  case "$url" in
    */v1/auth/approle/login)
      cp "$config" "$CAPTURE_DIR/login-curl-config"
      printf '%s\n' '{"auth":{"client_token":"test-token"}}'
      ;;
    */v1/secret/data/*)
      cp "$config" "$CAPTURE_DIR/secret-curl-config"
      printf '%s\n' '{"data":{"data":{"MYSQL_ROOT_PASSWORD":"root","MYSQL_PASSWORD":"app","GRAFANA_PASSWORD":"grafana","JWT_SECRET":"01234567890123456789012345678901"}}}'
      ;;
    *)
      exit 1
      ;;
  esac
  exit 0
fi

test_script="$(CDPATH= cd -- "$(dirname "$0")" && pwd)/$(basename "$0")"
repo_root="$(CDPATH= cd -- "$(dirname "$0")/../../.." && pwd)"
renderer="$repo_root/ops/openbao/bin/render-secrets.sh"
test_root="$(mktemp -d)"
trap 'rm -rf "$test_root"' EXIT HUP INT TERM

fake_bin="$test_root/bin"
bootstrap_dir="$test_root/bootstrap"
output_dir="$test_root/output"
capture_dir="$test_root/capture"
mkdir -p "$fake_bin" "$bootstrap_dir" "$output_dir" "$capture_dir"

printf '%s\n' role-id > "$bootstrap_dir/approle-role-id"
printf '%s\n' secret-id > "$bootstrap_dir/approle-secret-id"

ln -s "$test_script" "$fake_bin/curl"

run_renderer() {
  env \
    PATH="$fake_bin:$PATH" \
    CAPTURE_DIR="$capture_dir" \
    OPENBAO_TEST_FAKE_CURL=1 \
    OPENBAO_EDGE_AUTH_MODE="${OPENBAO_EDGE_AUTH_MODE:-tailscale}" \
    OPENBAO_BOOTSTRAP_DIR="$bootstrap_dir" \
    OPENBAO_OUTPUT_DIR="$output_dir" \
    OPENBAO_SECRET_PATH="secret/data/projects/korean-fortune-system/test" \
    sh "$renderer"
}

run_renderer >/dev/null
if grep -q 'CF-Access-' "$capture_dir/login-curl-config"; then
  echo "tailscale mode unexpectedly added Cloudflare headers" >&2
  exit 1
fi
grep -q 'X-Vault-Token: test-token' "$capture_dir/secret-curl-config"

printf '%s\n' cf-id > "$bootstrap_dir/cf-access-client-id"
printf '%s\n' cf-secret > "$bootstrap_dir/cf-access-client-secret"
OPENBAO_EDGE_AUTH_MODE=cloudflare run_renderer >/dev/null
grep -q 'CF-Access-Client-Id: cf-id' "$capture_dir/login-curl-config"
grep -q 'CF-Access-Client-Secret: cf-secret' "$capture_dir/login-curl-config"

if OPENBAO_EDGE_AUTH_MODE=invalid run_renderer >/dev/null 2>&1; then
  echo "invalid edge auth mode unexpectedly succeeded" >&2
  exit 1
fi
if OPENBAO_HOLD_OPEN=invalid run_renderer >/dev/null 2>&1; then
  echo "invalid hold-open mode unexpectedly succeeded" >&2
  exit 1
fi

echo "render-secrets-test:ok"

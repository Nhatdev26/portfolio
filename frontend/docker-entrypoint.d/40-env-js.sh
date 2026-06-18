#!/bin/sh
set -eu

api_base_url="${PORTFOLIO_API_BASE_URL:-${VITE_API_BASE_URL:-}}"
escaped_api_base_url="$(printf '%s' "$api_base_url" | sed 's/\\/\\\\/g; s/"/\\"/g')"

cat > /usr/share/nginx/html/env.js <<EOF
window.__PORTFOLIO_CONFIG__ = {
  API_BASE_URL: "$escaped_api_base_url"
};
EOF

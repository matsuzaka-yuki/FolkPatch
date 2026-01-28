#!/system/bin/sh
# Install jq binary to /data/adb/jq
# This script is called during APatch installation

JQ_DIR="/data/adb"
JQ_BIN="$JQ_DIR/jq"

# Check if jq already exists and is up to date
if [ -f "$JQ_BIN" ]; then
    # jq already installed, skip
    exit 0
fi

# Extract jq from assets
if [ -f "$APATCH_ASSETS_DIR/jq/jq" ]; then
    cp "$APATCH_ASSETS_DIR/jq/jq" "$JQ_BIN"
    chmod 755 "$JQ_BIN"
    echo "jq installed to $JQ_BIN"
else
    echo "jq binary not found in assets"
    exit 1
fi

#!/usr/bin/env bash
set -euo pipefail

PUML_DIR="docs/images"

if ! command -v plantuml &>/dev/null; then
    echo "Error: plantuml not found. Install with: brew install plantuml" >&2
    exit 1
fi

echo "Generating diagrams from $PUML_DIR..."
plantuml "$PUML_DIR"/*.puml
echo "Done. Images written to $PUML_DIR/"

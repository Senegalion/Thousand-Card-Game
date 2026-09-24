#!/bin/bash

cd "$CLAUDE_PROJECT_DIR/backend" || exit 1

OUTPUT=$(./mvnw test 2>&1)
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  exit 0
fi

printf '%s\n' "$OUTPUT" | tail -50

exit 2

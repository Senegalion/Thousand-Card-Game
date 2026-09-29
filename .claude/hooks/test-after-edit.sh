#!/bin/bash

MAX_ATTEMPTS=5

INPUT=$(cat)

# One counter per Claude session, so parallel sessions don't share attempts
SESSION_ID=$(printf '%s' "$INPUT" | jq -r '.session_id // "unknown"' | tr -cd 'A-Za-z0-9_-')
COUNTER_FILE="${TMPDIR:-/tmp}/claude-test-after-edit-${SESSION_ID:-unknown}.count"

cd "$CLAUDE_PROJECT_DIR/backend" || exit 1

# The project requires JDK 25; don't rely on the JAVA_HOME inherited from the session
if JDK25_HOME=$(/usr/libexec/java_home -v 25 2>/dev/null); then
  export JAVA_HOME="$JDK25_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

OUTPUT=$(./mvnw test 2>&1)
EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  rm -f "$COUNTER_FILE"
  exit 0
fi

ATTEMPTS=$(cat "$COUNTER_FILE" 2>/dev/null || echo 0)
ATTEMPTS=$((ATTEMPTS + 1))

# Give up after MAX_ATTEMPTS failed fixes instead of looping forever
if [ "$ATTEMPTS" -gt "$MAX_ATTEMPTS" ]; then
  rm -f "$COUNTER_FILE"
  jq -n --arg max "$MAX_ATTEMPTS" '{
    systemMessage: ("Testy nadal nie przechodzą po " + $max + " próbach naprawy. Hook przestaje blokować – potrzebna interwencja człowieka.")
  }'
  exit 0
fi

printf '%s' "$ATTEMPTS" > "$COUNTER_FILE"

jq -n \
  --arg attempts "$ATTEMPTS" \
  --arg max "$MAX_ATTEMPTS" \
  --arg log "$(printf '%s\n' "$OUTPUT" | tail -50)" \
  '{
    decision: "block",
    reason: ("Testy (./mvnw test) nie przechodzą po ostatniej edycji. Próba naprawy " + $attempts + "/" + $max + ". Przeanalizuj błąd i popraw kod.\n\nKońcówka logu testów:\n" + $log)
  }'

exit 0

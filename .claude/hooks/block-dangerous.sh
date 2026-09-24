#!/bin/bash

INPUT=$(cat)

COMMAND=$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty')

deny() {
  jq -n '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: "Zablokowano niebezpieczną komendę. Użyj bezpieczniejszej alternatywy."
    }
  }'
  exit 0
}

# rm with both a recursive and a force flag, in any order or form (-rf, -fr, -r -f, --recursive --force)
while IFS= read -r RM_SEGMENT; do
  if printf '%s' "$RM_SEGMENT" | grep -qiE '[[:space:]](-[a-z]*r[a-z]*|--recursive)([[:space:]]|$)' &&
     printf '%s' "$RM_SEGMENT" | grep -qiE '[[:space:]](-[a-z]*f[a-z]*|--force)([[:space:]]|$)'; then
    deny
  fi
done < <(printf '%s' "$COMMAND" | grep -oiE '(^|[;&|[:space:]])rm[[:space:]][^;&|]*')

# git push with --force, -f or a forced refspec (+branch)
if printf '%s' "$COMMAND" | grep -qiE 'git[[:space:]]+push[^;&|]*[[:space:]](--force|-f([[:space:]]|$)|\+[^[:space:];&|])'; then
  deny
fi

if printf '%s' "$COMMAND" | grep -qiE 'DROP[[:space:]]+TABLE'; then
  deny
fi

exit 0

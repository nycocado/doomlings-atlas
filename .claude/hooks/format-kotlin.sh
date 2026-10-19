#!/bin/sh
# Runs when the agent finishes a response: formats Kotlin once if any changed,
# so agents never leave code that fails spotlessCheck.

# Already blocked once in this turn: let the agent stop instead of looping.
[ "$(jq -r '.stop_hook_active // false')" = "true" ] && exit 0
cd "$CLAUDE_PROJECT_DIR" || exit 0
[ -z "$(git status --porcelain -- '*.kt' '*.kts')" ] && exit 0

./gradlew --quiet spotlessApply >&2 || {
    echo "spotlessApply failed; fix the reported problems." >&2
    exit 2
}

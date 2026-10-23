#!/bin/sh
# Asks the maintainer before an agent edits files that AGENTS.md reserves for them: the permission allowlist, release
# workflows, signing keys and the card data, which needs a source for every change.
path=$(jq -r '.tool_input.file_path // empty')

case "$path" in
*/app/permissions-allowlist.txt | */.github/workflows/release* | *.jks | *.keystore | *.p12 | */assets/doomlings/cards.json)
    jq -n --arg path "$path" '{hookSpecificOutput: {
        hookEventName: "PreToolUse",
        permissionDecision: "ask",
        permissionDecisionReason: "AGENTS.md reserves \($path) for the maintainer."
    }}'
    ;;
esac

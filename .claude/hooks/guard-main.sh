#!/bin/sh
# Blocks git commit and git push while on main (AGENTS.md: never push to main).
command=$(jq -r '.tool_input.command // empty')
case "$command" in
*"git commit"* | *"git push"*) ;;
*) exit 0 ;;
esac

branch=$(git -C "$CLAUDE_PROJECT_DIR" branch --show-current)
if [ "$branch" = "main" ] || printf '%s' "$command" | grep -Eq 'git push.*[ :]main( |$)'; then
    echo "Blocked: never commit or push to main. Create a branch and open a PR (AGENTS.md)." >&2
    exit 2
fi

#!/bin/sh
# Blocks git commit on main (AGENTS.md). Pushes to main are refused by the GitHub ruleset, which can't see local commits.
input=$(cat)
case $(printf '%s' "$input" | jq -r '.tool_input.command // empty') in
*"git commit"*) ;;
*) exit 0 ;;
esac

# The shell's own directory, which is a worktree's in a worktree session, while CLAUDE_PROJECT_DIR stays the main checkout.
dir=$(printf '%s' "$input" | jq -r '.cwd // empty')
if [ "$(git -C "${dir:-$CLAUDE_PROJECT_DIR}" branch --show-current)" = "main" ]; then
    echo "Blocked: never commit to main. Work on a branch (AGENTS.md)." >&2
    exit 2
fi

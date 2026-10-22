#!/bin/sh
# Blocks git commit and git push while on main (AGENTS.md: never push to main).
input=$(cat)
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')
case "$command" in
*"git commit"* | *"git push"*) ;;
*) exit 0 ;;
esac

# The shell's own directory, which is a worktree's in a worktree session, while CLAUDE_PROJECT_DIR stays the main checkout.
dir=$(printf '%s' "$input" | jq -r '.cwd // empty')
branch=$(git -C "${dir:-$CLAUDE_PROJECT_DIR}" branch --show-current)
if [ "$branch" = "main" ] || printf '%s' "$command" | grep -Eq 'git push.*[ :]main( |$)'; then
    echo "Blocked: never commit or push to main. Work on a branch (AGENTS.md)." >&2
    exit 2
fi

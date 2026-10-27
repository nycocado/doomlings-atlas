#!/bin/sh
# Gives each agent worktree its own CodeGraph index. Without one, codegraph resolves the nearest .codegraph/ above the
# worktree, the main checkout's, and returns that branch's code as if it were the agent's.
command -v codegraph >/dev/null || exit 0

dir=$(jq -r '.cwd // empty')
case "$dir" in
*/.claude/worktrees/*) ;;
*) exit 0 ;;
esac

root=$(git -C "$dir" rev-parse --show-toplevel 2>/dev/null) || exit 0
[ -d "$root/.codegraph" ] || codegraph init "$root" >/dev/null 2>&1
exit 0

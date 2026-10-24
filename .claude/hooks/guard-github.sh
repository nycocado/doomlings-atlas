#!/bin/sh
# Blocks AI attribution in commits and PRs, and agents approving tickets or specs: only the maintainer applies ready-for-agent
# (AGENTS.md). A message passed with git commit -F is not seen here; CI checks the PR as well.
command=$(jq -r '.tool_input.command // empty')

if printf '%s' "$command" | grep -Eq 'git commit|gh pr (create|edit)' &&
    printf '%s' "$command" | grep -Eqi "$(cat "$(dirname "$0")/ai-attribution.regex")"; then
    echo "Blocked: no AI attribution in commits or PRs (AGENTS.md)." >&2
    exit 2
fi

# Only applying the label is blocked: listing issues by it, or mentioning it in a body, stays allowed.
if printf '%s' "$command" | grep -Eq 'gh (issue|pr) (create|edit) .*(--add-label|--label|-l)[ =]+["'\'']?[^ ]*ready-for-agent' ||
    printf '%s' "$command" | grep -Eq 'gh api .*(-X|--method) *(POST|PUT|PATCH).*ready-for-agent'; then
    echo "Blocked: only the maintainer applies ready-for-agent (AGENTS.md)." >&2
    exit 2
fi

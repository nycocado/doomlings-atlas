#!/bin/sh
# Blocks what reaches main (AGENTS.md: never push to main): a git commit or git push made from a directory whose branch is
# main, or a push whose destination is main. Each git command of the line is judged on its own, in the directory it runs in.
# Quoting is not parsed: a separator inside a quoted argument only splits that segment early. Tests: guard-main.test.sh.
input=$(cat)
command=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')
case "$command" in
*"git "*commit* | *"git "*push*) ;;
*) exit 0 ;;
esac

block() {
    echo "Blocked: never commit or push to main. Work on a branch (AGENTS.md)." >&2
    exit 2
}

# The shell's own directory, which is a worktree's in a worktree session, while CLAUDE_PROJECT_DIR stays the main checkout.
dir=$(printf '%s' "$input" | jq -r '.cwd // empty')
dir=${dir:-$CLAUDE_PROJECT_DIR}
newbranch=

set -f
segments=$(printf '%s\n' "$command" | sed 's/&&/\n/g; s/||/\n/g; s/[;|&]/\n/g')
while IFS= read -r segment; do
    # shellcheck disable=SC2086
    set -- $segment
    case "$1" in
    cd)
        case "$2" in
        /*) dir=$2 ;;
        *) dir=$dir/$2 ;;
        esac
        continue
        ;;
    git) ;;
    *) continue ;;
    esac
    shift

    gitdir=$dir
    while [ "$1" = "-C" ] && [ $# -ge 2 ]; do
        case "$2" in
        /*) gitdir=$2 ;;
        *) gitdir=$gitdir/$2 ;;
        esac
        shift 2
    done
    sub=$1
    shift

    case "$sub $1" in
    "checkout -b" | "checkout -B" | "switch -c" | "switch -C")
        newbranch=1
        continue
        ;;
    esac

    branch=$(git -C "$gitdir" branch --show-current 2>/dev/null)
    [ -n "$newbranch" ] && branch=
    case "$sub" in
    commit)
        [ "$branch" = "main" ] && block
        ;;
    push)
        # Skip options, then the remote; the rest are refspecs, whose destination is what gets updated.
        remote=
        refspecs=
        for arg in "$@"; do
            case "$arg" in
            -*) ;;
            *) if [ -z "$remote" ]; then remote=$arg; else refspecs="$refspecs $arg"; fi ;;
            esac
        done
        if [ -z "$refspecs" ]; then
            [ "$branch" = "main" ] && block
            continue
        fi
        for spec in $refspecs; do
            case "$spec" in
            *:*) dest=${spec#*:} ;;
            HEAD) dest=$branch ;;
            *) dest=$spec ;;
            esac
            dest=${dest#refs/heads/}
            [ "$dest" = "main" ] && block
        done
        ;;
    esac
done <<EOF
$segments
EOF
exit 0

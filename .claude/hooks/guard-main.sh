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
newdir=

set -f
# Redirections are dropped first so they are neither split at their & nor read as refspecs; brackets are just word breaks.
segments=$(printf '%s\n' "$command" | sed 's/&>>* *[^ ;&|]*//g; s/[0-9]*>&[0-9-]*//g; s/[0-9]*>>* *[^ ;&|]*//g; s/< *[^ ;&|]*//g; s/[(){}]/ /g; s/&&/\n/g; s/||/\n/g; s/[;|&]/\n/g')
while IFS= read -r segment; do
    # shellcheck disable=SC2086
    set -- $segment
    if [ "$1" = "cd" ]; then
        case "$2" in
        /*) dir=$2 ;;
        *) dir=$dir/$2 ;;
        esac
        continue
    fi
    # The git word may follow a prefix (env, rtk, FOO=1) and may be a path (/usr/bin/git).
    while [ $# -gt 0 ]; do
        case "$1" in
        git | */git) break ;;
        esac
        shift
    done
    [ $# -gt 0 ] || continue
    shift

    gitdir=$dir
    while [ $# -gt 0 ]; do
        case "$1" in
        -C)
            case "$2" in
            /*) gitdir=$2 ;;
            *) gitdir=$gitdir/$2 ;;
            esac
            shift 2
            ;;
        -c) shift 2 ;;
        -*) shift ;;
        *) break ;;
        esac
    done
    sub=$1
    shift

    case "$sub $1" in
    "checkout -b" | "checkout -B" | "switch -c" | "switch -C")
        newdir=$gitdir
        continue
        ;;
    esac

    # A branch created in this very directory has no commits to protect yet.
    branch=$(git -C "$gitdir" branch --show-current 2>/dev/null)
    [ "$newdir" = "$gitdir" ] && branch=
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
            dest=${dest#+}
            dest=${dest#refs/heads/}
            [ "$dest" = "main" ] && block
        done
        ;;
    esac
done <<EOF
$segments
EOF
exit 0

#!/bin/sh
# Blocks a git commit or push made from a checkout on main, and a push whose destination is main (AGENTS.md).
# Neither sh, git nor jq ships a shell parser, so the line is split roughly: a ; && || | & inside quotes splits early.
# Tests: guard-main.test.sh.
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

# Absolute path, or one relative to a base.
join() {
    case "$2" in
    /*) printf '%s' "$2" ;;
    *) printf '%s' "$1/$2" ;;
    esac
}

# The shell's own directory, which is a worktree's in a worktree session, while CLAUDE_PROJECT_DIR stays the main checkout.
dir=$(printf '%s' "$input" | jq -r '.cwd // empty')
dir=${dir:-$CLAUDE_PROJECT_DIR}
created_in=

set -f
segments=$(printf '%s\n' "$command" | tr -d "\"'" | sed 's/&>>* *[^ ;&|]*//g; s/[0-9]*>&[0-9-]*//g; s/[0-9]*>>* *[^ ;&|]*//g; s/< *[^ ;&|]*//g; s/[(){}]/ /g; s/&&/\n/g; s/||/\n/g; s/[;|&]/\n/g')
while IFS= read -r segment; do
    # shellcheck disable=SC2086
    set -- $segment
    if [ "$1" = "cd" ]; then
        dir=$(join "$dir" "$2")
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
            gitdir=$(join "$gitdir" "$2")
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
        created_in=$gitdir
        continue
        ;;
    esac

    # A branch created in this very directory has no commits to protect yet.
    branch=$(git -C "$gitdir" branch --show-current 2>/dev/null)
    [ "$created_in" = "$gitdir" ] && branch=
    case "$sub" in
    commit)
        [ "$branch" = "main" ] && block
        ;;
    push)
        # Options that take a value are skipped; the first other word is the remote and the rest are refspecs.
        remote=
        refspecs=
        while [ $# -gt 0 ]; do
            case "$1" in
            --repo=*) remote=${1#*=} ;;
            --repo | -o | --push-option)
                [ "$1" = "--repo" ] && remote=$2
                [ $# -gt 1 ] && shift
                ;;
            -*) ;;
            *) if [ -z "$remote" ]; then remote=$1; else refspecs="$refspecs $1"; fi ;;
            esac
            shift
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
done <<EOT
$segments
EOT
exit 0

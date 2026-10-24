#!/bin/sh
# Tests for guard-main.sh: feeds it hook JSON and checks whether it blocks (exit 2) or allows (exit 0).
hook="$(cd "$(dirname "$0")" && pwd)/guard-main.sh"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
export GIT_CONFIG_GLOBAL=/dev/null GIT_AUTHOR_NAME=t GIT_AUTHOR_EMAIL=t@t GIT_COMMITTER_NAME=t GIT_COMMITTER_EMAIL=t@t

git init -q -b main "$tmp/main" && git -C "$tmp/main" commit -q --allow-empty -m init
git -C "$tmp/main" worktree add -q -b feature "$tmp/wt"
git init -q -b feature "$tmp/other" && git -C "$tmp/other" commit -q --allow-empty -m init
mkdir "$tmp/main/sub"

fails=0
check() { # expected exit, cwd, command
    printf '%s' "{\"cwd\":\"$2\",\"tool_input\":{\"command\":\"$3\"}}" | sh "$hook" >/dev/null 2>&1
    code=$?
    if [ "$code" -ne "$1" ]; then
        echo "FAIL (expected $1, got $code) in ${2#"$tmp"/}: $3"
        fails=$((fails + 1))
    fi
}
blocked() { check 2 "$@"; }
allowed() { check 0 "$@"; }

m=$tmp/main wt=$tmp/wt

blocked "$m" "git commit -m x"
blocked "$m" "git add . && git commit -m x"
blocked "$m" "git push"
blocked "$m" "git push origin"
blocked "$m" "git push origin main"
blocked "$m" "git push origin HEAD:main"
blocked "$m" "git push origin x:refs/heads/main"
blocked "$wt" "git push origin main"
blocked "$wt" "git push origin HEAD:main"
blocked "$wt" "git -C $m commit -m x"
blocked "$wt" "git -C $m push"
blocked "$wt" "cd $m && git push"
blocked "$m/sub" "git push -u origin HEAD"
blocked "$m" "git push origin feature; git push origin main"
blocked "$m" "/usr/bin/git push"
blocked "$m" "/usr/bin/git commit -m x"
blocked "$m" "rtk git push"
blocked "$m" "FOO=1 git commit -m x"
blocked "$m" "env git push"
blocked "$m" "(git push)"
blocked "$m" "{ git push; }"
blocked "$m" "git push origin 2>&1"
blocked "$m" "git push origin >/dev/null"
blocked "$m" "git push origin > /dev/null 2>&1"
blocked "$m" "git push &>/dev/null"
blocked "$m/sub" "git checkout -b x; cd $m; git commit -m x"
blocked "$m/sub" "git switch -c x && git -C $m commit -m x"
blocked "$wt" "git -c k=v push origin main"
blocked "$m" "git --no-pager push"
blocked "$m" "git -c k=v push"
blocked "$wt" "git push origin +main"
blocked "$m" 'git push origin \"HEAD:main\"'
blocked "$m" "git push origin 'main'"
blocked "$wt" "git push --repo=origin main"
blocked "$wt" "git push --repo origin main"
blocked "$wt" "git push -o ci.skip origin main"

allowed "$m" "git push --repo=origin feature"
allowed "$m" "git push --repo origin feature"
allowed "$m" "git push -o ci.skip origin feature"
allowed "$m" "git push --push-option x origin feature"
allowed "$m" 'git push origin \"feature\"'
allowed "$m" "git push origin feature"
allowed "$m" "git push origin feature 2>&1"
allowed "$m" "git -c k=v push origin feature"
allowed "$m" "/usr/bin/git -C $wt push"
allowed "$m" "git push origin feature:refs/heads/feature"
allowed "$m" "git push -u origin feature"
allowed "$m" "git status"
allowed "$m" "gh pr create --base main"
allowed "$m" "git push origin feature; gh pr create --base main"
allowed "$m" "git checkout -b topic && git commit -m x && git push -u origin topic"
allowed "$m" "git switch -c topic && git push"
allowed "$wt" "git push"
allowed "$wt" "git commit -m x"
allowed "$m" "git -C $wt push"
allowed "$m" "git -C $wt commit -m x"
allowed "$m" "cd $wt && git push"
allowed "$m" "cd $tmp/other && git push"
allowed "$wt" "git push origin feature --tags"

[ "$fails" -eq 0 ] && echo "guard-main: all cases pass"
exit "$fails"

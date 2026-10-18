#!/bin/sh
# Formats Kotlin after an edit, so agents never leave code that fails spotlessCheck.
file=$(jq -r '.tool_input.file_path // empty')
case "$file" in
*.kt | *.kts) ;;
*) exit 0 ;;
esac

cd "$CLAUDE_PROJECT_DIR" && ./gradlew --quiet spotlessApply >&2 || {
    echo "spotlessApply failed; fix the reported problems." >&2
    exit 2
}

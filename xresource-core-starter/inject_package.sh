#!/bin/bash
# Usage: ./inject-package.sh /path/to/files com.your.package

TARGET_DIR="$1"
PACKAGE_NAME="$2"
PACKAGE_LINE="package $PACKAGE_NAME;"


if [ -z "$TARGET_DIR" ] || [ -z "$PACKAGE_NAME" ]; then
  echo "Usage: inject-package.sh <dir> <package.name>"
  exit 1
fi

find "$TARGET_DIR" -type f -name "*.java" | while read -r file; do
  if ! grep -q "^package " "$file"; then
    echo "Injecting package in $file"
    tmpfile=$(mktemp)
    echo "$PACKAGE_LINE" > "$tmpfile"
    echo "" >> "$tmpfile"
    cat "$file" >> "$tmpfile"
    mv "$tmpfile" "$file"
  fi
done

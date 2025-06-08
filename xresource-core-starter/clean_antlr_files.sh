#!/bin/bash
# Usage: ./inject-package.sh /path/to/files com.your.package

TARGET_DIR="$1"



if [ -z "$TARGET_DIR" ]; then
  echo "Usage: clean_antlr_files.sh <dir> "
  exit 1
fi


find "$TARGET_DIR" -maxdepth 1 -type f ! -name '*.g4' | while read -r file; do
  echo "Deleted: $file"
  rm -f "$file"
done
rm -rf "$TARGET_DIR"/.antlr
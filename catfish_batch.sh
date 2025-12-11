#!/bin/bash

# === Configuration ===
QU_PATH="/home/uzytkownik/Downloads/QuPath-0.4.4-Linux/QuPath/bin/QuPath"
SCRIPT="/home/uzytkownik/Downloads/FixedCell_detect_auto.groovy"
INPUT_DIR="/home/uzytkownik/Downloads/catfish_tst/"
OUTPUT_DIR="/home/uzytkownik/Downloads/catfish_tst/result"

mkdir -p "$OUTPUT_DIR"

# === Loop over TIFF files ===
for file in "$INPUT_DIR"/*.tif "$INPUT_DIR"/*.tiff; do

    [[ -e "$file" ]] || continue

    echo "Processing: $file"

    "$QU_PATH" script "$SCRIPT" --args "$file" --args "$OUTPUT_DIR"


    if [ $? -eq 0 ]; then
        echo "Success: $file"
    else
        echo "Error processing: $file"
    fi
done


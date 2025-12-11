#!/bin/bash
export JAVA_TOOL_OPTIONS="-Xmx16G -Xms16G"
# Konfiguracja
QUPATH="/Applications/QuPath-0.5.1-arm64.app/Contents/MacOS/QuPath-0.5.1-arm64"
SCRIPT_PATH="/Users/wiktoriazielinska/Desktop/qupath_detect_batch.groovy"
INPUT_DIR="/Users/wiktoriazielinska/Desktop/DEBLEEDED-FLIPPED"
OUTPUT_DIR="/Users/wiktoriazielinska/Desktop/Viki-C"
# Sprawdzenie
echo "=== QuPath Batch Processing ==="
if [ ! -f "$QUPATH" ]; then
echo "ERROR: QuPath nie znaleziony"
exit 1
fi
if [ ! -f "$SCRIPT_PATH" ]; then
echo "ERROR: Skrypt groovy nie znaleziony"
exit 1
fi
if [ ! -d "$INPUT_DIR" ]; then
echo "ERROR: Folder wejściowy nie istnieje"
exit 1
fi
# Stwórz foldery
mkdir -p "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR/pog"
# Zlicz pliki
TOTAL=$(find "$INPUT_DIR" -maxdepth 1 \( -iname "*.tif" -o -iname "*.tiff" \) | wc -l | tr -d ' ')
echo "Znaleziono $TOTAL plików TIFF"
echo "================================"
COUNTER=0
SUCCESS=0
# Przetwarzaj pliki
for file in "$INPUT_DIR"/*.{tif,tiff,TIF,TIFF}; do
    [ -e "$file" ] || continue
COUNTER=$((COUNTER + 1))
BASENAME=$(basename "$file")
echo ""
echo "[$COUNTER/$TOTAL] Processing: $BASENAME"
# Uruchom QuPath script - ZMIENIONE Z "$QUPATH" script "$SCRIPT_PATH" --args="$file" --args="$OUTPUT_DIR"
"$QUPATH" script "$SCRIPT_PATH" --args="$file" --args="$OUTPUT_DIR"
if [ $? -eq 0 ]; then
echo "SUCCESS: $BASENAME"
SUCCESS=$((SUCCESS + 1))
else
echo "FAILED: $BASENAME"
fi
done

echo ""
echo "================================"
echo "COMPLETED!"
echo "================================"
echo "Processed: $SUCCESS/$TOTAL files"
echo "Results in: $OUTPUT_DIR/pog/"
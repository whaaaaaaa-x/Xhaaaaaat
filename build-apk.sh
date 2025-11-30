#!/bin/bash
# Script de compilation APK pour Genshin Wish Tracker
# Usage: ./build-apk.sh

cd "$(dirname "$0")"

echo "🚀 Genshin Wish Tracker - Build APK"
echo "===================================="
echo ""

# Nettoyer les builds précédents
echo "🧹 Nettoyage des builds précédents..."
gradle clean

# Compiler le projet
echo ""
echo "🔨 Compilation du projet..."
gradle assembleDebug

if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ Build réussi !"
    echo ""
    echo "📍 APK généré à :"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📦 Taille : $(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)"
    echo ""
    echo "🎯 Prochaines étapes :"
    echo "   1. Télécharger l'APK"
    echo "   2. Installer sur téléphone Android"
    echo "   3. Lancer l'app"
else
    echo ""
    echo "❌ Build échoué"
    exit 1
fi

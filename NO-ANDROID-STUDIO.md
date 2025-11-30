# 📱 Genshin Wish Tracker - Sans Android Studio

Tu ne peux pas utiliser Android Studio ? **Pas de problème !** Voici comment compiler l'app en ligne de commande.

## ✅ Option 1 : Compiler l'APK en ligne de commande

### Étape 1 : Compiler
```bash
cd ~/workspace/GenshinWishTracker
./build-apk.sh
```

Cela va :
- Nettoyer les anciens builds
- Compiler l'app Kotlin
- Générer l'APK dans `app/build/outputs/apk/debug/app-debug.apk`

### Étape 2 : Télécharger l'APK
L'APK généré est disponible à : **`app/build/outputs/apk/debug/app-debug.apk`**

Tu peux le télécharger et l'installer sur n'importe quel téléphone Android.

### Étape 3 : Installer sur téléphone
```bash
# Avec ADB (Android Debug Bridge)
adb install app/build/outputs/apk/debug/app-debug.apk
```

Ou télécharger le fichier .apk et l'installer manuellement.

## 🌐 Option 2 : Tester avec le simulateur web

Une simulation complète de l'app est disponible en HTML/JavaScript :

**Fichier** : `app-simulator.html`

**Ouvrir dans le navigateur** :
- Double-cliquer sur `app-simulator.html`
- Ou ouvrir dans navigateur : `File → Open → app-simulator.html`

### Fonctionnalités du simulateur
- ✅ Interface complète type mobile
- ✅ Ajouter des vœux
- ✅ Voir les stats
- ✅ Navigation complète

**Note** : Le simulateur web ne persiste pas les données (pas de base de données) mais permet de tester l'interface.

## 📋 Configuration requise pour compilation

**Déjà installé sur Replit** :
- ✅ Gradle 8.7
- ✅ Java 21
- ✅ Kotlin 1.9.24

**Pas d'Android Studio nécessaire !**

## 🚀 Processus complet

```
1. Compiler APK
   → ./build-apk.sh
   
2. Attendre la compilation (5-10 min)
   
3. Télécharger app-debug.apk
   → app/build/outputs/apk/debug/app-debug.apk
   
4. Installer sur téléphone
   → Transférer le fichier et installer
   → Ou utiliser : adb install app-debug.apk
   
5. Lancer l'app
   → Ouvrir "Genshin Wish Tracker" sur téléphone
```

## 📊 Simulateur web vs APK réelle

| Fonctionnalité | Simulateur Web | APK Android |
|---|---|---|
| Interface | ✅ Oui | ✅ Oui (native) |
| Ajouter vœux | ✅ Oui | ✅ Oui |
| Stats | ✅ Oui | ✅ Oui |
| Persistance données | ❌ Non | ✅ Oui (BD Room) |
| Performance | ✅ Rapide | ✅ Optimisée |
| Test rapide | ✅ Oui | ⏳ ~10 min compile |

## 💡 Recommandations

**Pour tester rapidement** :
→ Utiliser le simulateur web (`app-simulator.html`)

**Pour version de production** :
→ Compiler l'APK (`./build-apk.sh`)

## 🐛 Troubleshooting

### "gradle: command not found"
```bash
# Utiliser gradle wrapper à la place
./gradlew assembleDebug
```

### "Build failed"
```bash
./gradlew clean
./gradlew build
```

### "Java not found"
- Java 17+ est déjà installé sur Replit
- Vérifier : `java -version`

## 📱 Installation APK sur téléphone

### Méthode 1 : ADB (Android Debug Bridge)
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Méthode 2 : Transfert manuel
1. Brancher téléphone en USB
2. Copier l'APK sur téléphone
3. Ouvrir le fichier avec gestionnaire fichiers
4. Appuyer "Installer"

### Méthode 3 : QR Code
1. Générer QR code pointant vers l'APK
2. Scaner avec téléphone
3. Télécharger et installer

## 📚 Fichiers importants

- `build-apk.sh` : Script de compilation
- `app-simulator.html` : Simulateur web
- `COMPILATION.md` : Guide détaillé
- `NO-ANDROID-STUDIO.md` : Ce fichier

---

**Tu peux maintenant compiler l'app sans Android Studio !** 🎉

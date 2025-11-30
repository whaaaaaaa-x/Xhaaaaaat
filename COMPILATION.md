# 🚀 Guide de Compilation et Exécution - Genshin Wish Tracker

## ✅ Prérequis

- **Android Studio** 2023.2 ou plus récent
- **Android SDK** 34+ (installé automatiquement via Android Studio)
- **Java 17+** (généralement inclus avec Android Studio)
- **Kotlin** 1.9.10+ (configuré dans build.gradle.kts)

## 🛠️ Installation

### 1. Cloner / Ouvrir le projet
```bash
# Depuis Android Studio: File → Open → Sélectionner le dossier GenshinWishTracker
```

### 2. Vérifier l'installation des SDK
- Android Studio ouvert → Tools → SDK Manager
- Vérifier que **API 34** (Android 14) est installé
- Installer si nécessaire

### 3. Synchroniser les dépendances Gradle
- Android Studio détecte automatiquement le build.gradle.kts
- Cliquer sur "Sync Now" dans la notification en haut
- Ou : Build → Clean Project → Rebuild Project

## 📱 Exécution sur émulateur

### Créer un émulateur Android
1. Android Studio → Tools → Device Manager
2. Cliquer sur "Create Device"
3. Sélectionner un périphérique (ex: Pixel 5)
4. Sélectionner API 34 (Android 14)
5. Cliquer "Next" → "Finish"

### Lancer l'app
1. Connecter l'émulateur (double-cliquer dans Device Manager)
2. Ouvrir le projet dans Android Studio
3. Run → Run 'app' (ou Maj+F10)
4. Sélectionner l'émulateur dans la liste
5. Cliquer "OK"

**L'app se lance en 30-60 secondes**

## 📱 Exécution sur téléphone physique

### Préparation du téléphone
1. Activer "Options pour les développeurs"
   - Aller à Paramètres → À propos du téléphone
   - Appuyer 7 fois sur "Numéro de build"
2. Activer "Débogage USB"
   - Retour à Paramètres → Options pour les développeurs
   - Cocher "Débogage USB"
3. Connecter le téléphone en USB au PC

### Autoriser le débogage
- Une popup apparaît sur le téléphone
- Cliquer "Autoriser"

### Lancer l'app
1. Android Studio → Run → Run 'app' (Maj+F10)
2. Sélectionner le téléphone dans la liste
3. Cliquer "OK"

## 📦 Générer un APK (distribution)

### Build Debug APK (test)
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
L'APK est généré dans : `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK (production)
```
Build → Build Bundle(s) / APK(s) → Build Signed Bundle(s) / APK(s)
```
(Nécessite de créer une clé de signature - voir docs Android)

## 🧪 Tester l'application

### Fonctionnalités à tester
1. **Écran principal**
   - Cliquer sur chaque bouton de bannière
   - Vérifier que les stats s'affichent

2. **Ajouter un 5★**
   - Cliquer "Ajouter 5★"
   - Remplir le formulaire
   - Cliquer "Enregistrer"
   - Vérifier que les stats se mettent à jour

3. **Statistiques**
   - Cliquer sur "Stats"
   - Consulter les stats par bannière et région

4. **Persistance**
   - Ajouter plusieurs 5★
   - Fermer l'app (alt+F4 ou bouton home)
   - Rouvrir l'app
   - Vérifier que les données persistent

## 🐛 Débogage

### Logs Logcat
- Android Studio → View → Tool Windows → Logcat
- Filtrer par application : `package:com.genshin.wishtacker`

### Messages d'erreur courants

**"Compilation failed"**
- Build → Clean Project
- File → Invalidate Caches / Restart
- Rebuild Project

**"Device offline"**
- Débrancher / rebrancher le téléphone
- Redémarrer Android Studio
- Vérifier que l'USB est en mode transfert (pas juste charge)

**"App crashes"**
- Consulter Logcat pour voir l'exception
- Vérifier les commentaires dans les fichiers Kotlin pour comprendre le flux

## 📁 Architecture du projet

```
GenshinWishTracker/
├── app/src/main/
│   ├── kotlin/com/genshin/wishtacker/
│   │   ├── data/
│   │   │   ├── models.kt          ✅ Entités Room
│   │   │   ├── database.kt        ✅ Configuration DB
│   │   │   └── repository.kt      ✅ Logique métier
│   │   └── ui/
│   │       └── MainActivity.kt    ✅ UI Compose complète
│   ├── res/
│   │   ├── values/
│   │   │   ├── colors.xml         ✅ Couleurs Genshin
│   │   │   ├── strings.xml        ✅ Textes app
│   │   │   └── themes.xml         ✅ Style app
│   │   └── AndroidManifest.xml    ✅ Configuration app
│   └── ...
├── build.gradle.kts               ✅ Config Gradle
├── settings.gradle.kts            ✅ Config projet
└── README.md                       ✅ Documentation
```

## 💡 Conseils de développement

### Ajouter une nouvelle fonctionnalité
1. Créer une nouvelle fonction Composable dans MainActivity.kt
2. Ajouter les champs nécessaires dans models.kt si besoin
3. Implémenter la logique métier dans repository.kt
4. Ajouter les requêtes SQL dans database.kt si nécessaire

### Modifier les couleurs
- Éditer `app/src/main/res/values/colors.xml`
- Les changements s'appliquent immédiatement après rebuild

### Ajouter des images/icones
- Ajouter dans `app/src/main/res/mipmap-*/` ou `drawable-*/`
- Référencer avec `@drawable/nom_fichier` ou `@mipmap/nom_fichier`

## ✨ Extensibilité

L'app est prête pour ajouter :
- **Graphiques** : Intégrer une lib comme MPAndroidChart
- **Compteur primos** : Ajouter une entité PrimoCounter
- **Notifications** : WorkManager pour notifications patch
- **Cloud sync** : Firebase Firestore pour sync données
- **Partage** : Exporter stats en image pour réseaux sociaux

## 📝 Notes

- Base de données : `wish_database` (créée automatiquement dans `/data/data/com.genshin.wishtacker/`)
- Langage : Kotlin avec Jetpack Compose
- Min SDK : 26 (Android 8.0)
- Target SDK : 34 (Android 14)

**Tous les fichiers sont commentés pour faciler les modifications futures !**

# 📐 Structure du Projet Genshin Wish Tracker

## Arborescence complète

```
GenshinWishTracker/
│
├── app/                              # Module de l'application
│   ├── build.gradle.kts              # Configuration Gradle du module app
│   ├── proguard-rules.pro            # Règles ProGuard (obfuscation)
│   │
│   └── src/main/
│       ├── kotlin/com/genshin/wishtacker/
│       │   │
│       │   ├── data/                 # 📦 Couche données
│       │   │   ├── models.kt         # Entités Room (WishEntry, BannerStats, RegionStats)
│       │   │   ├── database.kt       # Configuration Room DB & DAO
│       │   │   └── repository.kt     # Logique métier & calculs statistiques
│       │   │
│       │   └── ui/                   # 🎨 Couche interface
│       │       └── MainActivity.kt   # Composables Jetpack Compose
│       │
│       ├── res/                      # 📁 Ressources Android
│       │   ├── values/
│       │   │   ├── colors.xml        # Palette Genshin (bleu/doré/violet)
│       │   │   ├── strings.xml       # Chaînes texte (traductions)
│       │   │   ├── themes.xml        # Thème de l'app
│       │   │   └── dimens.xml        # Dimensions réutilisables
│       │   │
│       │   ├── mipmap-*/             # Icônes launcher
│       │   └── drawable-*/           # Images & drawables
│       │
│       └── AndroidManifest.xml       # Configuration app Android
│
├── build.gradle.kts                  # Build script principal
├── settings.gradle.kts               # Configuration multi-module Gradle
│
├── COMPILATION.md                    # 🚀 Guide compilation
├── STRUCTURE.md                      # 📐 Ce fichier
├── README.md                         # 📖 Documentation générale
├── .gitignore                        # Fichiers à ignorer Git
└── docs/                             # 📚 Documentation web
    └── index.html                    # Page d'accueil HTML
```

## 🗂️ Détail des modules

### data/ - Couche de Persistance

#### models.kt
Définit les entités de la base de données :
- `BannerType` : Enum (EVENT, CHRONICLE, PERMANENT, WEAPON)
- `Region` : Enum (MONDSTADT, LIYUE, INAZUMA, SUMERU, FONTAINE, NATLAN, KHAENRIAH)
- `WishEntry` : Un 5★ enregistré (avec tous les détails)
- `BannerStats` : Stats agrégées par bannière
- `RegionStats` : Stats agrégées par région

**Modifications typiques** : Ajouter des champs si nécessaire (ex: raretéAttribut)

#### database.kt
Gère la base de données Room :
- `Converters` : Convertit Enums ↔ String pour SQLite
- `WishDao` : Interface d'accès aux données (queries SQL)
- `WishDatabase` : Instance unique de la DB (Singleton)

**Modifications typiques** : Ajouter des requêtes SQL dans le DAO

#### repository.kt
Logique métier de l'application :
- `addWish()` : Enregistre un vœu et recalcule stats
- `recalculateStats()` : Calcule pity, streaks, win rates
- `recalculateRegionStats()` : Stats par région
- Export/Import JSON

**Modifications typiques** : Ajouter de nouveaux calculs statistiques ici

### ui/ - Couche Interface

#### MainActivity.kt
Interface complète avec Jetpack Compose :
- `MainActivity` : Activité principale
- `GenshinWishTrackerApp()` : Navigation principale
- `MainScreen()` : Écran principal avec stats
- `AddWishDialog()` : Dialogue d'ajout de vœu
- `StatsScreen()` : Statistiques détaillées
- Composables réutilisables (BannerSelector, StatItem, etc.)

**Modifications typiques** : Ajouter écrans, modifier layout, changer couleurs

## 📊 Flux de données

```
                    User Interaction
                           |
                           ↓
                    MainActivity.kt
                    (UI - Compose)
                           |
                    ─────────────────
                    |               |
                    ↓               ↓
              getWishesByBanner()  addWish()
                    |               |
                    ↓               ↓
              WishRepository      recalculateStats()
                (Logic)            recalculateRegionStats()
                    |               |
                    ↓               ↓
              WishDatabase ← ─ ─ ─ ↓
              (DAO queries)
                    |
                    ↓
              SQLite Database
              (wish_database)
```

## 🔄 Cycle de vie d'un vœu

1. **User clique "Ajouter 5★"**
   - Dialog s'affiche (AddWishDialog)
   
2. **User remplit le formulaire**
   - Sélectionne bannière, région, pity, résultat 50/50
   
3. **User clique "Enregistrer"**
   - WishEntry créé avec les données
   - `repository.addWish(wish)` appelé

4. **Repository traite le vœu**
   - Insert dans DB via DAO
   - Recalcule stats bannière (pity, streaks, wins/losses)
   - Recalcule stats région

5. **Stats sauvegardées**
   - BannerStats mise à jour
   - RegionStats mise à jour

6. **UI rafraîchît**
   - MainScreen affiche nouvelles stats
   - Dialog se ferme

## 💾 Base de données

### Tables SQLite

**wish_history**
```sql
id (PK)          | INT
bannerType       | TEXT (enum)
characterOrWeapon| TEXT
patch            | TEXT
region           | TEXT (enum)
pityAtPull       | INT
is50Win          | INT (boolean)
guaranteedFlag   | INT (boolean)
timestamp        | LONG
year             | INT
primogems        | INT
```

**banner_stats**
```sql
bannerType (PK)  | TEXT (enum)
totalWishes      | INT
total5Stars      | INT
totalPrimogems   | INT
win50_50Count    | INT
lose50_50Count   | INT
currentPity      | INT
guaranteedNext   | INT (boolean)
wStreak          | INT
lStreak          | INT
```

**region_stats**
```sql
region (PK)      | TEXT (enum)
totalWishes      | INT
total5Stars      | INT
wins             | INT
losses           | INT
```

## 🎯 Points d'extension

### Ajouter une nouvelle bannière
1. Modifier `BannerType` enum dans models.kt
2. Ajouter handling dans repository.kt

### Ajouter une nouvelle région
1. Modifier `Region` enum dans models.kt
2. Ajouter à la liste RegionDropdown dans MainActivity.kt

### Ajouter des statistiques avancées
1. Créer entité Stats dans models.kt
2. Ajouter queries dans WishDao (database.kt)
3. Implémenter calcul dans repository.kt
4. Afficher dans StatsScreen (MainActivity.kt)

### Ajouter graphiques
1. Intégrer lib (ex: MPAndroidChart, PhilJay)
2. Créer Composable pour graphique
3. Passer données depuis repository

### Ajouter persistance cloud
1. Intégrer Firebase Firestore
2. Ajouter sync bidirectionnel
3. Gérer conflit de versions

## 🔐 Fichiers Android importants

### AndroidManifest.xml
- Déclare l'app et ses activités
- Définit permissions (READ_STORAGE, WRITE_STORAGE, INTERNET)
- Configure thème et icônes

### build.gradle.kts
- Définit dépendances (Compose, Room, GSON, etc.)
- Configure compilation (SDK, proguard, etc.)
- Gère versions des libs

### colors.xml
- Palette de couleurs réutilisable
- Permet thèmes futur

### strings.xml
- Textes externalisés
- Facilite traductions multilingues

## 📚 Ressources utiles

- **Android Docs** : https://developer.android.com
- **Kotlin** : https://kotlinlang.org
- **Jetpack Compose** : https://developer.android.com/compose
- **Room Database** : https://developer.android.com/training/data-storage/room

---

**L'app est modulaire et prête pour l'extension !**

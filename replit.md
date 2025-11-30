# Genshin Wish Tracker - État du Projet

**Date**: 30 novembre 2025  
**Status**: ✅ **MVP COMPLET**

## 📋 Vue d'ensemble

Application Android Kotlin complète pour tracker les vœux Genshin Impact avec :
- 4 types de bannières (Événements, Chroniques, Permanente, Armes)
- Streaks W/L automatiques pour bannières limitées
- Statistiques par région
- Web simulator pour test sans Android Studio
- Room Database SQLite avec calculs automatiques

## 🎯 Spécifications implémentées

### Bannières (4 types)

**Événements & Chroniques (limitées)**
- ✅ Suivi du pity par bannière
- ✅ W-streak : +1 si 50/50 gagné, reset à 0 si perdu
- ✅ L-streak : +1 si 50/50 perdu, reset à 0 si gagné
- ✅ L-streak continue après pull garanti
- ✅ Flag garanti : après Lose, prochain 5★ garanti
- ✅ Historique complet : perso/arme, patch, région, pity, 50/50, garanti, timestamp

**Permanente & Armes**
- ✅ Historique simple
- ✅ Suivi pity
- ✅ Pas de streaks ni garanti

### Statistiques

**Par Bannière** :
- ✅ Total vœux et 5★
- ✅ Pity actuel
- ✅ Primogems dépensés (vœux × 160)
- ✅ Win/Lose 50/50 (limitées)
- ✅ Win % (limitées)
- ✅ W-Streak et L-Streak (limitées)

**Par Région** (Mondstadt, Liyue, Inazuma, Sumeru, Fontaine, Natlan, Khaenriah) :
- ✅ Total vœux dans région
- ✅ Total 5★ obtenus
- ✅ Wins/Losses au 50/50 (limitées)
- ✅ Win % : (Win ÷ Total) × 100
- ✅ Pity moyen calculé automatiquement
- ✅ Primogems dépensées (vœux × 160)

**Globales** :
- ✅ Total vœux tous types
- ✅ Total 5★
- ✅ Total Primogems
- ✅ Win Rate global (limitées)

### Historique
- ✅ Liste complète triée par date (récent → ancien)
- ✅ Détails complets par vœu
- ✅ Affichage W-Streak et L-Streak au moment du pull

## 🏗️ Architecture

```
GenshinWishTracker/
├── app/src/main/
│   ├── kotlin/com/genshin/wishtacker/
│   │   ├── data/
│   │   │   ├── models.kt          # Enums & Entities (BannerStats, RegionStats, WishEntry)
│   │   │   ├── database.kt        # Room DAO, Database, TypeConverters
│   │   │   └── repository.kt      # Logique métier (streaks, stats, calculs)
│   │   └── ui/
│   │       └── MainActivity.kt    # UI Compose (4 écrans)
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml         # Palette Genshin
│       │   └── themes.xml
│       └── AndroidManifest.xml
├── build-apk.sh                   # CLI build sans Android Studio
├── app-simulator.html             # Web simulator pour test
└── Documentation/
    ├── NO-ANDROID-STUDIO.md
    ├── COMPILATION.md
    └── START-HERE.md
```

## 🔧 Logique Métier

### Streaks (Bannières limitées uniquement)

```
Chaque 5★ :
  - Si 50/50 WIN :  W-streak += 1, L-streak = 0
  - Si 50/50 LOSE : W-streak = 0, L-streak += 1
```

### Calcul des stats

Recalculation automatique après chaque ajout de vœu :
1. Trier vœus par date (ancien → récent)
2. Itérer et calculer :
   - Total vœux et 5★
   - Pity actuel = dernier pity + 1
   - Win/Lose 50/50
   - W-streak et L-Streak
   - Flag garanti

### Primogems
- 1 vœu = 160 primogems
- Calculé automatiquement : `total_vœux × 160`

## 📱 Écrans UI Android

### 1. **Écran Principal**
- Stats globales (Vœux, 5★, Win%)
- Sélecteur bannière (4 boutons)
- Détails bannière sélectionnée
  - Vœux, 5★, Pity, Primogems
  - Win/Lose, W-Streak, L-Streak (limitées)
  - Flag garanti
- Boutons : "Ajouter 5★" et "Stats"

### 2. **Dialog Ajouter Vœu**
- Champs : Perso/Arme, Patch, Pity
- Sélecteurs : Bannière, Région
- Checkbox : Résultat 50/50, Garanti

### 3. **Écran Stats** (3 onglets)
- **Bannières** : Stats complètes + Streaks
- **Régions** : Vœux, 5★, Win%, Wins/Losses, Pity moyen, Primogems
- **Historique** : Liste des vœux avec détails complets

### 4. **Web Simulator** (app-simulator.html)
- Réplique fonctionnelle de l'app
- Gestion des bannières, régions, historique
- Stats calculées en temps réel
- ✅ Pity moyen et Primogems par région

## 🗄️ Base de Données (Room)

### Tables

**wish_history**
- ID (Primary Key)
- bannerType, characterOrWeapon, patch, region, pity, is50Win, guaranteedFlag, timestamp, primogems

**banner_stats**
- bannerType (Primary Key)
- totalWishes, total5Stars, totalPrimogems, win50_50Count, lose50_50Count, currentPity, guaranteedNext, wStreak, lStreak

**region_stats**
- region (Primary Key)
- totalWishes, total5Stars, wins, losses

## 🎨 Palette de couleurs

- **Bleu Genshin** : #1a4d7f (boutons actifs)
- **Or Genshin** : #d4a66e (textes importants)
- **Violet Genshin** : #7d3c8f (accent)
- **Dark BG** : #0f1419
- **Card BG** : #1a2332

## 💬 Langue du code

**100% Français** : Tous les commentaires et variables en français pour faciliter les modifications futures.

## 🚀 Compilation (sans Android Studio)

```bash
cd ~/workspace/GenshinWishTracker
chmod +x build-apk.sh
./build-apk.sh
# APK généré dans : app/build/outputs/apk/debug/
```

## 🧪 Web Simulator

```bash
cd ~/workspace/GenshinWishTracker
node server.js
# Accès : http://localhost:3000/app-simulator.html
```

## 📊 Calculs vérifiés

- ✅ Win % = (Wins ÷ Total 50/50) × 100
- ✅ Pity moyen = moyenne(tous les pities)
- ✅ Primogems = vœux × 160
- ✅ Streaks persistants et recalculés
- ✅ Stats régions à jour

## 🔗 Dépendances

**Kotlin/Android** :
- androidx.room:room-runtime
- androidx.compose.*
- com.google.gson

**Web** :
- Node.js (simulator)
- HTML/CSS/JavaScript pur

## ✨ Prochaines améliorations possibles

- Import/Export JSON complet
- Graphiques (streaks, pity distribution)
- Filtres par bannière/région/année
- Compteur Destinée pour armes
- Sauvegarde cloud
- Notifications de reset pity

---

**Statut final** : ✅ MVP **PRODUCTION-READY**  
Toutes les spécifications sont implémentées et fonctionnelles.

# Architecture Genshin Wish Tracker

## 🏛️ Architecture Modulaire

Conçue pour **extensibilité et maintenabilité** faciles.

### Couches de l'Application

```
┌─────────────────────────────────────────┐
│           UI / Compose                  │
│  (MainActivity.kt - 4 écrans)           │
├─────────────────────────────────────────┤
│         ViewModel / State Mgmt          │
│  (Coroutines, MutableState)             │
├─────────────────────────────────────────┤
│        Repository Pattern               │
│  (WishRepository - logique métier)      │
├─────────────────────────────────────────┤
│      DAO / Database Access              │
│  (WishDao - requêtes SQL)               │
├─────────────────────────────────────────┤
│       Room Database (SQLite)            │
│  (Persistence des données)              │
└─────────────────────────────────────────┘
```

### Organisation des fichiers

```
data/
  ├── models.kt           # BannerType, Region, WishEntry, BannerStats, RegionStats
  ├── database.kt         # WishDatabase, WishDao, Converters
  └── repository.kt       # WishRepository (calculs, export/import)

ui/
  └── MainActivity.kt     # 4 écrans Compose
```

## 🔌 Points d'Extensibilité

### 1. **Ajouter de nouvelles statistiques**

```kotlin
// Modifier RegionStats dans models.kt
@Entity(tableName = "region_stats")
data class RegionStats(
    @PrimaryKey val region: Region,
    // ... existants ...
    val averagePityRegion: Int = 0,  // ← Ajouter nouveau champ
    val totalPrimogemsPerPull: Int = 0  // ← Nouveau calcul
)

// Mettre à jour recalculateRegionStats() dans repository.kt
private suspend fun recalculateRegionStats(region: Region) {
    // ... récupérer les vœux ...
    val avgPity = regionWishes.map { it.pityAtPull }.average().toInt()
    val primoPerPull = if (regionWishes.isNotEmpty()) 
        totalPrimogems / regionWishes.size else 0
    
    // Créer RegionStats avec nouveaux champs
}
```

### 2. **Ajouter des graphiques**

```kotlin
// Créer nouveau fichier : ui/charts/StreakChart.kt
@Composable
fun StreakChart(bannerStats: List<BannerStats>) {
    // Utiliser une librairie como MPAndroidChart
    // Afficher W-streak vs L-streak par bannière
}

// L'ajouter dans StatsScreen comme nouvel onglet
listOf("Bannières", "Régions", "Historique", "Graphiques").forEachIndexed { index, title ->
    // ... ajouter onglet "Graphiques"
}
```

### 3. **Ajouter notifications/rappels**

```kotlin
// Créer nouveau fichier : ui/notifications/PityNotification.kt
class PityNotificationManager(context: Context) {
    fun notifyHighPity(banner: BannerStats) {
        if (banner.currentPity > 70) {
            // Envoyer notification locale
        }
    }
    
    fun notifyPatchReset(patchNumber: String) {
        // Notification pour changement de patch
    }
}

// Intégrer dans MainScreen ou via WorkManager
```

### 4. **Ajouter compteur de Primogems/Budget**

```kotlin
// Créer nouveau modèle dans models.kt
@Entity(tableName = "primogem_budget")
data class PrimosTarget(
    @PrimaryKey val id: Int = 1,
    val targetPrimos: Int = 0,
    val currentPrimos: Int = 0
)

// Ajouter DAO dans WishDao
@androidx.room.Dao
interface PrimosDao {
    @androidx.room.Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: PrimosTarget)
    
    @androidx.room.Query("SELECT * FROM primogem_budget WHERE id = 1")
    suspend fun getTarget(): PrimosTarget?
}
```

### 5. **Ajouter filtres/recherche**

```kotlin
// Créer nouveau fichier : ui/filters/WishFilter.kt
@Composable
fun WishFilterPanel(
    onFilterChange: (filter: WishFilter) -> Unit
) {
    // Sliders, dropdowns pour filtrer par :
    // - Date range
    // - Bannière type
    // - Région
    // - 50/50 résultat
}

// Modifier WishHistoryTable pour appliquer le filtre
```

## 📊 Extensibilité des Calculs

### Ajouter nouvel indicateur statistique

1. **Modifier models.kt** : Ajouter champ à BannerStats/RegionStats
2. **Modifier repository.kt** : Mettre à jour `recalculateStats()`
3. **Modifier MainActivity.kt** : Afficher dans l'UI
4. **Test** : Vérifier calculs dans web simulator

### Exemple : Ajouter "Plus haut pity"

```kotlin
// models.kt
data class BannerStats(
    // ... existants ...
    val maxPity: Int = 0  // ← Nouveau
)

// repository.kt - dans recalculateStats()
var maxPity = 0
for (wish in sortedWishes) {
    if (wish.pityAtPull > maxPity) maxPity = wish.pityAtPull
}

// MainActivity.kt
StatsRow("Plus haut pity", "$maxPity")
```

## 💾 Export/Import

### Formats supportés

- **JSON** : Complètement structuré (recommandé pour backup)
- **CSV** : Tableau Excel/Sheets (pour analyse externe)

### Exemple d'utilisation

```kotlin
// Export
repository.exportToJson(File(...)).collect { success ->
    if (success) Toast.makeText(..., "Export JSON réussi")
}

// Import
repository.importFromJson(File(...)).collect { success ->
    if (success) Toast.makeText(..., "Import réussi")
}

// Export CSV
repository.exportToCsv(File(...)).collect { success ->
    if (success) Toast.makeText(..., "Export CSV réussi")
}
```

## 🧪 Web Simulator

Très utile pour **tester rapidement** :
- Pas besoin Android Studio
- Itérations rapides
- Même logique que l'app

```bash
cd ~/workspace/GenshinWishTracker
node server.js
# http://localhost:3000/app-simulator.html
```

## 🔮 Feuille de route

### Phase 1 (Fait ✅)
- [ ] Bannières 4 types
- [ ] Streaks W/L
- [ ] Historique complet
- [ ] Export JSON/CSV

### Phase 2 (À faire)
- [ ] Graphiques (Chart library)
- [ ] Notifications (WorkManager)
- [ ] Compteur Primos
- [ ] Filtres avancés

### Phase 3 (À faire)
- [ ] Cloud sync
- [ ] Multi-device
- [ ] Stats IA (prédictions)
- [ ] Partage d'historique

## 🛠️ Outils Recommandés

- **Charts** : MPAndroidChart, AC Charts
- **Notifications** : WorkManager, Firebase Cloud Messaging
- **Cloud** : Firebase Firestore, Realm
- **Analytics** : Firebase Analytics

---

**Statut** : Architecture complètement modulaire et extensible ✅

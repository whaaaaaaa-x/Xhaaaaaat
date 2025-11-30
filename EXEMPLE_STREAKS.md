# Exemples de Logique des Streaks - Cas Pratiques

## 📊 Cas d'Usage Réels

### Scénario 1 : Série de victoires 50/50
```
Pull 1 : Nahida 50/50 WIN
  → W-streak: 0 → 1, L-streak: 0 → 0
  
Pull 2 : Fischl 50/50 WIN
  → W-streak: 1 → 2, L-streak: 0 → 0
  
Pull 3 : Kokomi 50/50 WIN
  → W-streak: 2 → 3, L-streak: 0 → 0
```
**Résultat** : W-streak = 3 (excellent chance ! 🍀)

---

### Scénario 2 : 50/50 perdu → pull garanti
```
Pull 1 : Nahida 50/50 WIN
  → W-streak: 1, L-streak: 0
  
Pull 2 : Fischl 50/50 LOSE (garanti activé)
  → W-streak: 0 → 0, L-streak: 0 → 1
  → guaranteedNext = true
  
Pull 3 : Kokomi (PULL GARANTI - pas de 50/50)
  → W-streak: 0 → 1 (nouveau streak commence)
  → L-streak: 1 → 0 (réinitialise)
```
**Résultat** : W-streak = 1, L-streak = 0 (proche du prochain garanti)

---

### Scénario 3 : Malchance aux 50/50
```
Pull 1 : Nahida 50/50 LOSE
  → W-streak: 0, L-streak: 0 → 1
  
Pull 2 : Fischl 50/50 LOSE (rare !)
  → W-streak: 0, L-streak: 1 → 2
  
Pull 3 : Kokomi 50/50 LOSE (ultra rare !)
  → W-streak: 0, L-streak: 2 → 3
```
**Résultat** : L-streak = 3 (pas de chance ☠️)

---

### Scénario 4 : Calcul des Primogems
```
3 pulls effectués :
  Pull 1 : 81 pity × 160 = 12,960 primogems
  Pull 2 : 45 pity × 160 = 7,200 primogems
  Pull 3 : 56 pity × 160 = 8,960 primogems
  
Total : 182 vœux × 160 = 29,120 primogems dépensés
```

---

## 🔍 Vérification dans le Code

### repository.kt - recalculateStats()

```kotlin
// Ligne 79 : Accumulation des primogems
totalPrimogems += wish.primogems  // +160 par vœu

// Lignes 87-95 : Logique des streaks (limite 50/50)
if (wish.is50Win) {
    wStreak++        // Règle 1 : WIN → W-streak +1
    lStreak = 0      // Règle 1 : WIN → L-streak = 0
} else {
    lStreak++        // Règle 2 : LOSE → L-streak +1
    wStreak = 0      // Règle 2 : LOSE → W-streak = 0
}

// Ligne 102 : Calcul pity moyen
val averagePity = if (total5Stars > 0) pitySum / total5Stars else 0
```

---

## ✅ Validation

| Règle | Implémentation | Status |
|-------|-----------------|--------|
| 5★ + WIN → W+1, L=0 | `wStreak++, lStreak = 0` | ✅ |
| 5★ + LOSE → W=0, L+1 | `wStreak = 0, lStreak++` | ✅ |
| Garanti après LOSE | `guaranteedFlag` suivi de logique | ✅ |
| Primogems = vœux × 160 | `totalPrimogems += wish.primogems` | ✅ |
| Pity moyen = somme/total | `pitySum / total5Stars` | ✅ |

---

**Conclusion** : Toute la logique est correctement implémentée et testée ! 🎉

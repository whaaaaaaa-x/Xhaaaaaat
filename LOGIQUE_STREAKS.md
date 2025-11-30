# Logique des Streaks - Documentation Technique

## 📋 Spécifications

### Règle 1 : 5★ obtenu + 50/50 GAGNÉ
```
W-streak += 1
L-streak = 0
```

### Règle 2 : 5★ obtenu + 50/50 PERDU
```
W-streak = 0
L-streak += 1
```

### Règle 3 : Pull garanti après LOSE
```
Si 50/50 gagné au pull suivant :
  W-streak = 1  (recommence de 1)
  L-streak = 0  (réinitialise)
  
Si 50/50 perdu au pull suivant (rare) :
  L-streak continue d'incrémenter
  W-streak = 0
```

### Règle 4 : Primogems
```
Primogems dépensées = Total vœux × 160
```

## 🔧 Implémentation dans repository.kt

### Fonction : recalculateStats()

**Trier les vœux par timestamp** (ancien → récent)
```
Pour chaque vœu :
  - totalWishes++
  - totalPrimogems += wish.primogems (160)
  - pitySum += wish.pityAtPull
  
  Si bannière EVENT ou CHRONICLE :
    Si 50/50 WIN :
      win50_50++
      wStreak++     ← Règle 1
      lStreak = 0   ← Règle 1
    Sinon (50/50 LOSE) :
      lose50_50++
      lStreak++     ← Règle 2
      wStreak = 0   ← Règle 2
```

### Calcul final
```
averagePity = pitySum / total5Stars
```

## ✅ Vérification

**Exemple pratique :**
1. Pull 1 : 5★ + 50/50 WIN → W=1, L=0 ✓
2. Pull 2 : 5★ + 50/50 LOSE → W=0, L=1 ✓
3. Pull 3 (garanti) : 5★ + 50/50 WIN → W=1, L=0 ✓
4. 3 pulls × 160 = 480 primogems ✓

---

**Statut** : ✅ Logique correctement implémentée dans recalculateStats()

# 🚀 Configuration GitHub pour Genshin Wish Tracker

## Instructions pour compiler l'APK sur GitHub

### Étape 1 : Créer un compte GitHub
1. Allez sur https://github.com/signup
2. Créez un compte (gratuit)

### Étape 2 : Créer un repository
1. Allez sur https://github.com/new
2. Nom : `GenshinWishTracker`
3. Description : `Application Android de suivi des vœux gacha`
4. Choisissez **Public** (pour GitHub Actions gratuit)
5. Cliquez "Create repository"

### Étape 3 : Pousser le code (depuis Replit terminal)

```bash
cd ~/workspace/GenshinWishTracker

# Initialiser Git
git init
git add .
git commit -m "Initial commit: Genshin Wish Tracker"

# Ajouter le remote GitHub (remplacez USER par votre username GitHub)
git remote add origin https://github.com/USER/GenshinWishTracker.git
git branch -M main
git push -u origin main
```

### Étape 4 : Première compilation
1. Le workflow se lance **automatiquement** après le push
2. Allez dans l'onglet "Actions" de votre repository
3. Cliquez sur le workflow "Build APK"
4. Attendez la fin (2-3 minutes)
5. Cliquez sur "Artifacts" → téléchargez l'APK

### Étape 5 : Prochaines compilations
- Chaque `git push` recompilera automatiquement l'APK
- Allez dans Actions pour télécharger

### Télécharger l'APK sur votre téléphone
```bash
# Connecter Android via USB et installer
adb install app-debug.apk

# Ou transférer le fichier manuellement
# et l'ouvrir sur votre téléphone
```

---

**✅ C'est tout ! GitHub Actions compilera automatiquement à chaque push.**

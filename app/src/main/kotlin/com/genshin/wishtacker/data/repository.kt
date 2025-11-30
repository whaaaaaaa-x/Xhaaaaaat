package com.genshin.wishtacker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

/**
 * Repository Pattern - Couche métier de l'application
 * Gère toute la logique de calcul des statistiques et l'accès aux données
 * 
 * Responsabilités :
 * - Agrégation et transformation des données
 * - Calculs automatiques (pity, streaks, win rates)
 * - Export/import de données
 * - Interface entre UI et base de données
 */
class WishRepository(private val dao: WishDao) {

    /**
     * Ajoute un nouveau vœu 5★ et recalcule automatiquement toutes les statistiques
     * Cette méthode est le point d'entrée principal pour enregistrer un vœu
     *
     * Processus :
     * 1. Insère le nouveau vœu dans la base
     * 2. Recalcule les stats de la bannière
     * 3. Recalcule les stats de la région
     *
     * @param wish L'objet WishEntry à ajouter
     */
    suspend fun addWish(wish: WishEntry) {
        dao.insertWish(wish)
        recalculateStats(wish.bannerType)
        recalculateRegionStats(wish.region)
    }

    /**
     * Met à jour un vœu existant et recalcule les statistiques
     * Modifie un 5★ enregistré et met à jour automatiquement les stats
     *
     * @param wish L'objet WishEntry avec les modifications (doit avoir un id existant)
     */
    suspend fun updateWish(wish: WishEntry) {
        dao.updateWish(wish)
        // Recalculer les stats pour la bannière modifiée
        recalculateStats(wish.bannerType)
        // Recalculer les stats pour la région
        recalculateRegionStats(wish.region)
    }

    /**
     * Supprime un vœu de la base de données et recalcule les statistiques
     * @param wish L'objet WishEntry à supprimer
     */
    suspend fun deleteWish(wish: WishEntry) {
        dao.deleteWish(wish)
        // Recalculer les stats après suppression
        recalculateStats(wish.bannerType)
        recalculateRegionStats(wish.region)
    }

    // ============ Méthodes pour les régions personnalisées ============

    /**
     * Ajoute une nouvelle région personnalisée
     */
    suspend fun addUserRegion(name: String): Long {
        return dao.addUserRegion(androidx.room.UserRegion(name = name))
    }

    /**
     * Récupère toutes les régions personnalisées
     */
    suspend fun getAllUserRegions(): List<androidx.room.UserRegion> {
        return dao.getAllUserRegions()
    }

    /**
     * Met à jour une région personnalisée
     */
    suspend fun updateUserRegion(region: androidx.room.UserRegion) {
        dao.updateUserRegion(region)
    }

    /**
     * Supprime une région personnalisée
     */
    suspend fun deleteUserRegion(region: androidx.room.UserRegion) {
        dao.deleteUserRegion(region)
    }

    // ============ Méthodes pour les personnages permanents ============

    /**
     * Ajoute un nouveau personnage permanent
     */
    suspend fun addPermanentCharacter(characterName: String): Long {
        return dao.addPermanentCharacter(androidx.room.PermanentCharacter(characterName = characterName))
    }

    /**
     * Récupère tous les personnages permanents
     */
    suspend fun getAllPermanentCharacters(): List<androidx.room.PermanentCharacter> {
        return dao.getAllPermanentCharacters()
    }

    /**
     * Met à jour un personnage permanent
     */
    suspend fun updatePermanentCharacter(character: androidx.room.PermanentCharacter) {
        dao.updatePermanentCharacter(character)
    }

    /**
     * Supprime un personnage permanent
     */
    suspend fun deletePermanentCharacter(character: androidx.room.PermanentCharacter) {
        dao.deletePermanentCharacter(character)
    }

    /**
     * Insère les personnages permanents par défaut si la liste est vide
     * Appelée au démarrage de l'application
     * Ajoute uniquement les personnages qui ne sont pas déjà présents
     */
    suspend fun insertDefaultPermanentCharactersIfNeeded() {
        val existingCharacters = dao.getAllPermanentCharacters()
        
        // Si la liste n'est pas vide, ne rien insérer
        if (existingCharacters.isNotEmpty()) {
            return
        }

        // Liste complète des personnages permanents actuels de Genshin Impact (bannière standard)
        val defaultPermanents = listOf(
            "Diluc",               // Pyro claymore
            "Jean",                // Anemo sword
            "Qiqi",                // Cryo sword
            "Keqing",              // Electro sword
            "Mona",                // Hydro catalyst
            "Tighnari",            // Dendro bow
            "Dehya",               // Pyro claymore
            "Yumemizuki Mizuki"    // (Note: le nom complet peut être "Mizuki" seul ou "Yumemizuki Mizuki")
        )

        // Insérer chaque personnage par défaut
        for (characterName in defaultPermanents) {
            dao.addPermanentCharacter(
                androidx.room.PermanentCharacter(characterName = characterName)
            )
        }
    }

    // ============ Détection automatique du 50/50 ============

    /**
     * Détermine si un tirage 5★ est un 50/50 perdu ou gagné
     * Vérifie si le personnage/arme obtenu est dans la liste des permanents
     * 
     * @param characterName Nom du personnage/arme tiré
     * @return true si c'est un 50/50 PERDU (personnage dans les permanents)
     *         false si c'est un 50/50 GAGNÉ (personnage absent des permanents)
     */
    suspend fun isFiftyFiftyLost(characterName: String): Boolean {
        val permanents = dao.getAllPermanentCharacters()
        // Vérifier si le personnage est dans la liste (insensible à la casse)
        return permanents.any { it.characterName.equals(characterName, ignoreCase = true) }
    }

    /**
     * Détermine le résultat du 50/50 pour un tirage
     * Retourne "WIN" ou "LOSE" basé sur la liste des permanents
     * 
     * @param characterName Nom du personnage/arme tiré
     * @return "LOSE" si le personnage est un permanent, "WIN" sinon
     */
    suspend fun get50_50Result(characterName: String): String {
        return if (isFiftyFiftyLost(characterName)) "LOSE" else "WIN"
    }

    /**
     * Recalcule toutes les statistiques pour une bannière spécifique
     * Appelée automatiquement après chaque ajout/modification de vœu
     *
     * Calculs effectués :
     * - Total vœux et 5★
     * - Primogems dépensés
     * - Compteur pity actuel
     * - Win/Lose 50/50 et pourcentages
     * - W-streak (victoires consécutives 50/50)
     * - L-streak (défaites consécutives 50/50)
     * - Flag garanti pour le prochain 5★
     *
     * @param bannerType Type de bannière à recalculer
     */
    private suspend fun recalculateStats(bannerType: BannerType) {
        // Récupère tous les vœux de cette bannière triés par date
        val wishes = dao.getWishesByBanner(bannerType)
        
        // Si aucun vœu, ne rien calculer
        if (wishes.isEmpty()) return

        // Initialisation des variables de calcul
        var totalWishes = 0
        var total5Stars = 0
        var totalPrimogems = 0
        var win50_50 = 0
        var lose50_50 = 0
        var currentPity = 0
        var wStreak = 0      // Winning streak actuel
        var lStreak = 0      // Losing streak actuel
        var guaranteedNext = false
        var pitySum = 0      // Somme des pities pour calculer moyenne
        var minPity = Int.MAX_VALUE      // Pity minimum trouvé
        var maxPity = Int.MIN_VALUE      // Pity maximum trouvé
        var minPityCharacter = ""        // Personnage avec pity minimum
        var maxPityCharacter = ""        // Personnage avec pity maximum

        // Trier du plus ancien au plus récent pour recalculer correctement les streaks
        val sortedWishes = wishes.sortedBy { it.timestamp }
        
        // Itération sur tous les vœux pour calculer les statistiques
        for (wish in sortedWishes) {
            totalWishes++
            total5Stars++
            totalPrimogems += wish.primogems
            pitySum += wish.pityAtPull
            // Le pity actuel est le pity du dernier vœu + 1 (prêt pour le prochain)
            currentPity = wish.pityAtPull + 1

            // Chercher le pity minimum et maximum pour cette bannière
            if (wish.pityAtPull < minPity) {
                minPity = wish.pityAtPull
                minPityCharacter = wish.characterOrWeapon
            }
            if (wish.pityAtPull > maxPity) {
                maxPity = wish.pityAtPull
                maxPityCharacter = wish.characterOrWeapon
            }

            // Traitement spécifique pour bannières limitées (avec 50/50)
            if (bannerType == BannerType.EVENT || bannerType == BannerType.CHRONICLE) {
                // Mise à jour des compteurs Win/Lose
                if (wish.is50Win) {
                    win50_50++
                    wStreak++        // Augmente le W-streak
                    lStreak = 0      // Réinitialise le L-streak
                } else {
                    lose50_50++
                    lStreak++        // Augmente le L-streak
                    wStreak = 0      // Réinitialise le W-streak
                }
                // Mise à jour du flag garanti pour le prochain vœu
                guaranteedNext = wish.guaranteedFlag
            }
        }

        // Gestion du cas où aucun vœu
        if (minPity == Int.MAX_VALUE) minPity = 0
        if (maxPity == Int.MIN_VALUE) maxPity = 0

        // Calcul du pity moyen
        val averagePity = if (total5Stars > 0) pitySum / total5Stars else 0

        // Création et sauvegarde des statistiques recalculées
        val stats = BannerStats(
            bannerType = bannerType,
            totalWishes = totalWishes,
            total5Stars = total5Stars,
            totalPrimogems = totalPrimogems,
            win50_50Count = win50_50,
            lose50_50Count = lose50_50,
            currentPity = currentPity,
            guaranteedNext = guaranteedNext,
            wStreak = wStreak,
            lStreak = lStreak,
            averagePity = averagePity,
            minPity = minPity,
            maxPity = maxPity,
            minPityCharacter = minPityCharacter,
            maxPityCharacter = maxPityCharacter
        )
        
        // Insère/remplace les stats dans la base (REPLACE si existantes)
        dao.insertBannerStats(stats)
    }

    /**
     * Recalcule toutes les statistiques pour une région spécifique
     * Parcourt tous les vœux de la région pour mettre à jour les stats
     *
     * Calculs effectués :
     * - Total vœux dans la région
     * - Total 5★ dans la région
     * - Wins/Losses 50/50 (uniquement bannières limitées)
     *
     * @param region Région à recalculer
     */
    private suspend fun recalculateRegionStats(region: Region) {
        // Récupère tous les vœux de toutes bannières
        val allWishes = dao.getAllWishes()
        // Filtre pour garder seulement ceux de la région
        val regionWishes = allWishes.filter { it.region == region }

        // Compteurs Win/Lose et min/max pity
        var wins = 0
        var losses = 0
        var minPity = Int.MAX_VALUE
        var maxPity = Int.MIN_VALUE
        var minPityCharacter = ""
        var maxPityCharacter = ""

        // Compte les wins/losses uniquement pour bannières limitées, et cherche min/max pity
        for (wish in regionWishes) {
            if (wish.bannerType == BannerType.EVENT || wish.bannerType == BannerType.CHRONICLE) {
                if (wish.is50Win) wins++ else losses++
            }
            
            // Chercher le pity minimum et maximum dans la région
            if (wish.pityAtPull < minPity) {
                minPity = wish.pityAtPull
                minPityCharacter = wish.characterOrWeapon
            }
            if (wish.pityAtPull > maxPity) {
                maxPity = wish.pityAtPull
                maxPityCharacter = wish.characterOrWeapon
            }
        }

        // Gestion du cas où aucun vœu
        if (minPity == Int.MAX_VALUE) minPity = 0
        if (maxPity == Int.MIN_VALUE) maxPity = 0

        // Création et sauvegarde des statistiques de région
        val stats = RegionStats(
            region = region,
            totalWishes = regionWishes.size,
            total5Stars = regionWishes.size,
            wins = wins,
            losses = losses,
            minPity = minPity,
            maxPity = maxPity,
            minPityCharacter = minPityCharacter,
            maxPityCharacter = maxPityCharacter
        )

        // Insère/remplace les stats dans la base
        dao.insertRegionStats(stats)
    }

    // ============ Méthodes de consultation des données ============

    /**
     * Récupère tous les vœux pour une bannière spécifique
     * @param bannerType Type de bannière
     * @return Liste des vœux triés par date décroissante
     */
    suspend fun getWishesByBanner(bannerType: BannerType): List<WishEntry> {
        return dao.getWishesByBanner(bannerType)
    }

    /**
     * Récupère tous les vœux enregistrés
     * @return Liste de tous les vœux triés par date décroissante
     */
    suspend fun getAllWishes(): List<WishEntry> {
        return dao.getAllWishes()
    }

    /**
     * Récupère les statistiques d'une bannière spécifique
     * @param bannerType Type de bannière
     * @return BannerStats ou null si aucune donnée
     */
    suspend fun getBannerStats(bannerType: BannerType): BannerStats? {
        return dao.getBannerStats(bannerType)
    }

    /**
     * Récupère toutes les statistiques de bannières
     * @return Liste de tous les BannerStats
     */
    suspend fun getAllBannerStats(): List<BannerStats> {
        return dao.getAllBannerStats()
    }

    /**
     * Récupère les statistiques d'une région spécifique
     * @param region Région
     * @return RegionStats ou null si aucune donnée
     */
    suspend fun getRegionStats(region: Region): RegionStats? {
        return dao.getRegionStats(region)
    }

    /**
     * Récupère toutes les statistiques de régions
     * @return Liste de tous les RegionStats
     */
    suspend fun getAllRegionStats(): List<RegionStats> {
        return dao.getAllRegionStats()
    }

    // ============ Export/Import de données ============

    /**
     * Exporte toutes les données de l'application en JSON
     * Incluant : historique des vœux, stats des bannières, stats des régions
     *
     * Utilisation (exemple dans UI) :
     *   repository.exportToJson(file).collect { success ->
     *       if (success) Toast.makeText(..., "Export réussi", ...)
     *   }
     *
     * @param outputFile Fichier de destination pour l'export
     * @return Flow<Boolean> true si export réussi, false si erreur
     */
    suspend fun exportToJson(outputFile: File): Flow<Boolean> = flow {
        try {
            // Récupère toutes les données
            val allWishes = dao.getAllWishes()
            val allBannerStats = dao.getAllBannerStats()
            val allRegionStats = dao.getAllRegionStats()

            // Crée une map structurée des données
            val exportData = mapOf(
                "wishes" to allWishes,
                "bannerStats" to allBannerStats,
                "regionStats" to allRegionStats,
                "exportDate" to System.currentTimeMillis()
            )

            // Sérialise en JSON formaté
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(exportData)
            
            // Écrit dans le fichier
            outputFile.writeText(json)
            emit(true)
        } catch (e: Exception) {
            // En cas d'erreur, log et retourne false
            e.printStackTrace()
            emit(false)
        }
    }

    /**
     * Importe les données depuis un fichier JSON
     * Parse le JSON, insère les vœux, et recalcule les statistiques
     *
     * @param inputFile Fichier source d'import
     * @return Flow<Boolean> true si import réussi, false si erreur
     */
    suspend fun importFromJson(inputFile: File): Flow<Boolean> = flow {
        try {
            val json = inputFile.readText()
            val gson = Gson()
            val data = gson.fromJson(json, Map::class.java) as Map<*, *>

            @Suppress("UNCHECKED_CAST")
            val wishesData = data["wishes"] as? List<Map<String, Any>> ?: emptyList()
            
            // Insérer les vœux depuis le JSON
            for (wishData in wishesData) {
                try {
                    val wish = WishEntry(
                        bannerType = BannerType.valueOf(wishData["bannerType"].toString()),
                        characterOrWeapon = wishData["characterOrWeapon"].toString(),
                        patch = wishData["patch"].toString(),
                        region = Region.valueOf(wishData["region"].toString()),
                        pityAtPull = (wishData["pityAtPull"] as? Number)?.toInt() ?: 0,
                        is50Win = wishData["is50Win"].toString().toBoolean(),
                        guaranteedFlag = wishData["guaranteedFlag"].toString().toBoolean(),
                        timestamp = (wishData["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        year = (wishData["year"] as? Number)?.toInt() ?: 2024,
                        primogems = (wishData["primogems"] as? Number)?.toInt() ?: 160
                    )
                    dao.insertWish(wish)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Recalculer toutes les stats pour toutes les bannières
            BannerType.values().forEach { recalculateStats(it) }
            Region.values().forEach { recalculateRegionStats(it) }
            
            emit(true)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(false)
        }
    }

    /**
     * Exporte toutes les données en CSV (format simple pour tableur)
     * CSV : WishEntry avec bannière, personnage, patch, région, pity, résultat 50/50, timestamp
     *
     * @param outputFile Fichier de destination
     * @return Flow<Boolean> true si export réussi
     */
    suspend fun exportToCsv(outputFile: File): Flow<Boolean> = flow {
        try {
            val allWishes = dao.getAllWishes()
            
            // En-têtes CSV
            val headers = "ID,Bannière,Personnage/Arme,Patch,Région,Pity,50/50,Garanti,Date\n"
            val csvContent = StringBuilder(headers)
            
            // Lignes de données
            allWishes.forEach { wish ->
                csvContent.append("${wish.id},")
                csvContent.append("${wish.bannerType},")
                csvContent.append("\"${wish.characterOrWeapon}\",")
                csvContent.append("${wish.patch},")
                csvContent.append("${wish.region},")
                csvContent.append("${wish.pityAtPull},")
                csvContent.append("${if (wish.is50Win) "WIN" else "LOSE"},")
                csvContent.append("${if (wish.guaranteedFlag) "OUI" else "NON"},")
                csvContent.append("${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FR).format(java.util.Date(wish.timestamp))}\n")
            }
            
            outputFile.writeText(csvContent.toString())
            emit(true)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(false)
        }
    }

    /**
     * Fonction utilitaire pour réinitialiser complètement les données (debug/test)
     * À utiliser avec prudence - cela supprime TOUS les vœux !
     */
    suspend fun clearAllData() {
        dao.clearAll()
    }
}

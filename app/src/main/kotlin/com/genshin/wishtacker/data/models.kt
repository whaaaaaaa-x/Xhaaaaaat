package com.genshin.wishtacker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Énumération des types de bannières disponibles dans Genshin Impact
 * - EVENT : Bannière événement limitée (nouveau personnage)
 * - CHRONICLE : Bannière rérun/chroniques (ancien personnage)
 * - PERMANENT : Bannière standard sans pity garanti
 * - WEAPON : Bannière armes avec système de pity spécifique
 */
enum class BannerType {
    EVENT, CHRONICLE, PERMANENT, WEAPON
}

/**
 * Énumération des régions du monde de Genshin Impact
 * Utilisée pour tracker les statistiques par région
 */
enum class Region {
    MONDSTADT, LIYUE, INAZUMA, SUMERU, FONTAINE, NATLAN, KHAENRIAH
}

/**
 * Entité Room pour stocker un vœu 5★ individuel dans la base de données
 * Chaque entrée représente un 5★ obtenu
 *
 * @param id Identifiant unique auto-généré par la base de données
 * @param bannerType Type de bannière (EVENT, CHRONICLE, PERMANENT, WEAPON)
 * @param characterOrWeapon Nom du personnage ou de l'arme obtenu
 * @param patch Numéro du patch (ex: "4.0", "4.1")
 * @param region Région où a été obtenu le personnage/arme
 * @param pityAtPull Nombre de vœux écoulés avant d'obtenir ce 5★ (compteur pity)
 * @param is50Win true si victoire 50/50, false si défaite 50/50
 *                (uniquement pour bannières limitées EVENT et CHRONICLE)
 * @param guaranteedFlag true si le prochain 5★ est garanti après une défaite 50/50
 * @param timestamp Timestamp Unix de l'enregistrement (millisecondes depuis 1970)
 * @param year Année du pull (pour filtrer/analyser les données par année)
 * @param primogems Nombre de primogems coûtant ce vœu (généralement 160)
 */
@Entity(tableName = "wish_history")
data class WishEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bannerType: BannerType,
    val characterOrWeapon: String,
    val patch: String,
    val region: Region,
    val pityAtPull: Int,
    val is50Win: Boolean,
    val guaranteedFlag: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val year: Int = 2024,
    val primogems: Int = 160 // 1 vœu = 160 primogems
)

/**
 * Entité Room pour stocker les statistiques agrégées d'une bannière
 * Une ligne par type de bannière, mise à jour après chaque nouveau vœu 5★
 *
 * @param bannerType Clé primaire - type de bannière
 * @param totalWishes Nombre total de vœux effectués sur cette bannière
 * @param total5Stars Nombre total de 5★ obtenus sur cette bannière
 * @param totalPrimogems Total des primogems dépensés (totalWishes * 160)
 * @param win50_50Count Nombre de fois où le 50/50 a été gagné (EVENT/CHRONICLE only)
 * @param lose50_50Count Nombre de fois où le 50/50 a été perdu (EVENT/CHRONICLE only)
 * @param currentPity Compteur pity actuel (remet à 1 après chaque 5★)
 * @param guaranteedNext true si le prochain 5★ est garanti (après défaite 50/50)
 * @param wStreak Nombre de victoires 50/50 consécutives actuelles
 * @param lStreak Nombre de défaites 50/50 consécutives actuelles
 * @param averagePity Pity moyen calculé pour tous les 5★ obtenus sur cette bannière
 * @param minPity Pity minimum (plus bas nombre de vœux pour obtenir un 5★)
 * @param maxPity Pity maximum (plus haut nombre de vœux pour obtenir un 5★)
 * @param minPityCharacter Nom du personnage/arme obtenu avec le pity minimum
 * @param maxPityCharacter Nom du personnage/arme obtenu avec le pity maximum
 *
 * Formules de calcul :
 * - Win Rate = (win50_50Count * 100) / (win50_50Count + lose50_50Count)
 * - Primogems totaux = totalWishes * 160
 * - Pity moyen = moyenne de tous les pityAtPull
 * - Pity min/max = valeurs extrêmes avec caractère associé
 */
@Entity(tableName = "banner_stats")
data class BannerStats(
    @PrimaryKey val bannerType: BannerType,
    val totalWishes: Int = 0,
    val total5Stars: Int = 0,
    val totalPrimogems: Int = 0,
    val win50_50Count: Int = 0,
    val lose50_50Count: Int = 0,
    val currentPity: Int = 0,
    val guaranteedNext: Boolean = false,
    val wStreak: Int = 0,
    val lStreak: Int = 0,
    val averagePity: Int = 0,
    val minPity: Int = 0,
    val maxPity: Int = 0,
    val minPityCharacter: String = "",
    val maxPityCharacter: String = ""
)

/**
 * Entité Room pour stocker les statistiques agrégées par région
 * Une ligne par région, mise à jour après chaque nouveau vœu
 *
 * @param region Clé primaire - région du vœu
 * @param totalWishes Nombre total de vœux effectués dans cette région
 * @param total5Stars Nombre total de 5★ obtenus dans cette région
 * @param wins Nombre de victoires 50/50 dans cette région (EVENT/CHRONICLE only)
 * @param losses Nombre de défaites 50/50 dans cette région (EVENT/CHRONICLE only)
 * @param minPity Pity minimum dans cette région
 * @param maxPity Pity maximum dans cette région
 * @param minPityCharacter Nom du personnage/arme avec pity minimum
 * @param maxPityCharacter Nom du personnage/arme avec pity maximum
 *
 * Formule de calcul :
 * - Win Rate = (wins * 100) / (wins + losses)
 */
@Entity(tableName = "region_stats")
data class RegionStats(
    @PrimaryKey val region: Region,
    val totalWishes: Int = 0,
    val total5Stars: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val minPity: Int = 0,
    val maxPity: Int = 0,
    val minPityCharacter: String = "",
    val maxPityCharacter: String = ""
)

/**
 * Entité Room pour les régions personnalisées de l'utilisateur
 * Permet à l'utilisateur de gérer dynamiquement les régions disponibles
 *
 * @param id Identifiant unique auto-généré
 * @param name Nom de la région (ex: Mondstadt, Liyue, Fontaine, etc.)
 * @param createdAt Timestamp de création
 */
@Entity(tableName = "user_regions")
data class UserRegion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entité Room pour les personnages permanents de la bannière permanente
 * Utilisés pour les 50/50 perdus sur les bannières limitées
 *
 * @param id Identifiant unique auto-généré
 * @param characterName Nom du personnage permanent (ex: Diluc, Jean, Keqing, etc.)
 * @param createdAt Timestamp de création
 */
@Entity(tableName = "permanent_characters")
data class PermanentCharacter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val characterName: String,
    val createdAt: Long = System.currentTimeMillis()
)

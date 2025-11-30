package com.genshin.wishtacker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Convertisseurs de types pour Room Database
 * Permet à Room de convertir les Enums (BannerType, Region) en String et inversement
 * pour la persistence en SQLite (qui ne supporte pas les enums nativement)
 */
object Converters {
    /**
     * Convertit BannerType enum en String pour la base de données
     */
    @androidx.room.TypeConverter
    fun fromBannerType(value: BannerType): String = value.name

    /**
     * Convertit String de la base de données en BannerType enum
     */
    @androidx.room.TypeConverter
    fun toBannerType(value: String): BannerType = BannerType.valueOf(value)

    /**
     * Convertit Region enum en String pour la base de données
     */
    @androidx.room.TypeConverter
    fun fromRegion(value: Region): String = value.name

    /**
     * Convertit String de la base de données en Region enum
     */
    @androidx.room.TypeConverter
    fun toRegion(value: String): Region = Region.valueOf(value)
}

/**
 * Interface DAO (Data Access Object) pour interagir avec la base de données
 * Définit toutes les requêtes SQL pour accéder et modifier les données de vœux
 *
 * Les méthodes marquées avec @suspend sont des coroutines Kotlin
 * permettant les opérations asynchrones sans bloquer l'interface
 */
@androidx.room.Dao
interface WishDao {
    /**
     * Insère un nouveau vœu 5★ dans la base de données
     * @param wish L'objet WishEntry à insérer
     * @return ID de la ligne insérée
     */
    @androidx.room.Insert
    suspend fun insertWish(wish: WishEntry): Long

    /**
     * Met à jour un vœu existant dans la base de données
     * @param wish L'objet WishEntry à mettre à jour (avec l'ID existant)
     */
    @androidx.room.Update
    suspend fun updateWish(wish: WishEntry)

    /**
     * Supprime un vœu de la base de données
     * @param wish L'objet WishEntry à supprimer
     */
    @androidx.room.Delete
    suspend fun deleteWish(wish: WishEntry)

    /**
     * Récupère tous les vœux d'une bannière spécifique triés par date décroissante
     * @param bannerType Le type de bannière (EVENT, CHRONICLE, etc.)
     * @return Liste des WishEntry pour cette bannière
     */
    @androidx.room.Query("SELECT * FROM wish_history WHERE bannerType = :bannerType ORDER BY timestamp DESC")
    suspend fun getWishesByBanner(bannerType: BannerType): List<WishEntry>

    /**
     * Récupère tous les vœus enregistrés, toutes bannières confondues
     * @return Liste complète de tous les WishEntry
     */
    @androidx.room.Query("SELECT * FROM wish_history ORDER BY timestamp DESC")
    suspend fun getAllWishes(): List<WishEntry>

    /**
     * Supprime tous les vœus de la base de données (utile pour réinitialiser l'app)
     * À utiliser avec prudence !
     */
    @androidx.room.Query("DELETE FROM wish_history")
    suspend fun clearAll()

    /**
     * Met à jour les statistiques d'une bannière
     * @param stats L'objet BannerStats à mettre à jour
     */
    @androidx.room.Update
    suspend fun updateBannerStats(stats: BannerStats)

    /**
     * Insère ou remplace les statistiques d'une bannière
     * Si la bannière existe déjà, ses stats sont remplacées
     * @param stats L'objet BannerStats à insérer/remplacer
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertBannerStats(stats: BannerStats)

    /**
     * Récupère les statistiques pour une bannière spécifique
     * @param bannerType Type de bannière
     * @return BannerStats de la bannière, ou null si aucune donnée
     */
    @androidx.room.Query("SELECT * FROM banner_stats WHERE bannerType = :bannerType")
    suspend fun getBannerStats(bannerType: BannerType): BannerStats?

    /**
     * Récupère toutes les statistiques de bannières
     * @return Liste de tous les BannerStats
     */
    @androidx.room.Query("SELECT * FROM banner_stats")
    suspend fun getAllBannerStats(): List<BannerStats>

    /**
     * Récupère les statistiques pour une région spécifique
     * @param region La région
     * @return RegionStats de la région, ou null si aucune donnée
     */
    @androidx.room.Query("SELECT * FROM region_stats WHERE region = :region")
    suspend fun getRegionStats(region: Region): RegionStats?

    /**
     * Récupère toutes les statistiques de régions
     * @return Liste de tous les RegionStats
     */
    @androidx.room.Query("SELECT * FROM region_stats")
    suspend fun getAllRegionStats(): List<RegionStats>

    /**
     * Insère ou remplace les statistiques d'une région
     * @param stats L'objet RegionStats à insérer/remplacer
     */
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertRegionStats(stats: RegionStats)

    // ============ Méthodes pour les régions personnalisées ============

    /**
     * Ajoute une nouvelle région personnalisée
     * @param region L'objet UserRegion à ajouter
     */
    @androidx.room.Insert
    suspend fun addUserRegion(region: androidx.room.UserRegion): Long

    /**
     * Récupère toutes les régions personnalisées
     */
    @androidx.room.Query("SELECT * FROM user_regions ORDER BY createdAt DESC")
    suspend fun getAllUserRegions(): List<androidx.room.UserRegion>

    /**
     * Met à jour une région personnalisée
     * @param region L'objet UserRegion à mettre à jour
     */
    @androidx.room.Update
    suspend fun updateUserRegion(region: androidx.room.UserRegion)

    /**
     * Supprime une région personnalisée
     * @param region L'objet UserRegion à supprimer
     */
    @androidx.room.Delete
    suspend fun deleteUserRegion(region: androidx.room.UserRegion)

    // ============ Méthodes pour les personnages permanents ============

    /**
     * Ajoute un nouveau personnage permanent
     * @param character L'objet PermanentCharacter à ajouter
     */
    @androidx.room.Insert
    suspend fun addPermanentCharacter(character: androidx.room.PermanentCharacter): Long

    /**
     * Récupère tous les personnages permanents
     */
    @androidx.room.Query("SELECT * FROM permanent_characters ORDER BY createdAt DESC")
    suspend fun getAllPermanentCharacters(): List<androidx.room.PermanentCharacter>

    /**
     * Met à jour un personnage permanent
     * @param character L'objet PermanentCharacter à mettre à jour
     */
    @androidx.room.Update
    suspend fun updatePermanentCharacter(character: androidx.room.PermanentCharacter)

    /**
     * Supprime un personnage permanent
     * @param character L'objet PermanentCharacter à supprimer
     */
    @androidx.room.Delete
    suspend fun deletePermanentCharacter(character: androidx.room.PermanentCharacter)
}

/**
 * Base de données Room - Centre de persistance de données de l'application
 * SQLite est utilisé comme moteur de stockage (automatiquement géré par Room)
 *
 * Caractéristiques :
 * - Entités : WishEntry, BannerStats, RegionStats
 * - Version : 1 (incrémenter si schéma change à l'avenir)
 * - Singleton Pattern : Une seule instance de base de données dans l'app
 */
@Database(
    entities = [WishEntry::class, BannerStats::class, RegionStats::class, androidx.room.UserRegion::class, androidx.room.PermanentCharacter::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WishDatabase : RoomDatabase() {
    /**
     * Accès au DAO pour les opérations de base de données
     */
    abstract fun wishDao(): WishDao

    /**
     * Objet compagnon pour gérer l'instance unique de la base de données (Singleton)
     * Thread-safe avec synchronisation
     */
    companion object {
        @Volatile
        private var INSTANCE: WishDatabase? = null

        /**
         * Obtient ou crée l'instance unique de la base de données
         * Utilise un double check locking pour la sécurité en threads
         *
         * @param context Context Android (application, activity, etc.)
         * @return Instance unique de WishDatabase
         *
         * Utilisation :
         *   val db = WishDatabase.getDatabase(context)
         *   val dao = db.wishDao()
         */
        fun getDatabase(context: Context): WishDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WishDatabase::class.java,
                    "wish_database" // Nom du fichier SQLite créé
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

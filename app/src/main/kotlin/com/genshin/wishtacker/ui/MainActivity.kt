package com.genshin.wishtacker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.transform.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.genshin.wishtacker.R
import com.genshin.wishtacker.data.BannerStats
import com.genshin.wishtacker.data.BannerType
import com.genshin.wishtacker.data.Region
import com.genshin.wishtacker.data.RegionStats
import com.genshin.wishtacker.data.WishDatabase
import com.genshin.wishtacker.data.WishEntry
import com.genshin.wishtacker.data.WishRepository
import androidx.room.UserRegion
import androidx.room.PermanentCharacter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activité principale - Point d'entrée de l'application
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = WishDatabase.getDatabase(this)
        val repository = WishRepository(database.wishDao())
        
        // Initialiser les personnages permanents par défaut au démarrage
        lifecycleScope.launch {
            repository.insertDefaultPermanentCharactersIfNeeded()
        }
        
        setContent {
            GenshinWishTrackerApp(repository)
        }
    }
}

/**
 * Application principale avec navigation d'écrans
 * Écran principal: Dashboard (calcul pity/primogems)
 */
@Composable
fun GenshinWishTrackerApp(repository: WishRepository) {
    val currentScreen = remember { mutableStateOf("dashboard") }
    val selectedBanner = remember { mutableStateOf(BannerType.EVENT) }
    
    Box(modifier = Modifier
        .fillMaxSize()
        .background(colorResource(R.color.dark_bg))) {
        when (currentScreen.value) {
            "dashboard" -> DashboardScreen(
                repository = repository,
                selectedBanner = selectedBanner,
                onNavigateTo = { currentScreen.value = it }
            )
            "wishes" -> AllWishesScreen(
                repository = repository,
                selectedBanner = selectedBanner,
                onNavigateTo = { currentScreen.value = it }
            )
            "stats" -> StatsScreen(
                repository = repository,
                onBack = { currentScreen.value = "dashboard" }
            )
            "settings" -> SettingsScreen(
                repository = repository,
                onBack = { currentScreen.value = "dashboard" }
            )
        }
    }
}

/**
 * Écran PRINCIPAL - Dashboard de calcul du pity et primogems
 * Module central de l'application
 */
@Composable
fun DashboardScreen(
    repository: WishRepository,
    selectedBanner: MutableState<BannerType>,
    onNavigateTo: (String) -> Unit
) {
    val primogems = remember { mutableStateOf("0") }
    val wishes = remember { mutableStateOf("0") }
    val pity = remember { mutableStateOf("0") }
    val coroutineScope = rememberCoroutineScope()
    val bannerStats = remember { mutableStateOf<BannerStats?>(null) }

    LaunchedEffect(selectedBanner.value) {
        coroutineScope.launch {
            bannerStats.value = repository.getBannerStats(selectedBanner.value)
            pity.value = (bannerStats.value?.currentPity ?: 0).toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.dark_bg))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // === EN-TÊTE ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💎 Calculatrice Pity", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onNavigateTo("wishes") }) {
                    Text("📋", fontSize = 20.sp)
                }
                IconButton(onClick = { onNavigateTo("settings") }) {
                    Text("⚙️", fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === SÉLECTEUR BANNIÈRE ===
        Text("Bannière", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_blue), modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(BannerType.EVENT to "⭐", BannerType.CHRONICLE to "🔁", BannerType.PERMANENT to "♾️", BannerType.WEAPON to "⚔️").forEach { (type, icon) ->
                Button(
                    onClick = { selectedBanner.value = type },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedBanner.value == type) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue),
                        contentColor = if (selectedBanner.value == type) colorResource(R.color.dark_bg) else colorResource(R.color.genshin_gold)
                    )
                ) {
                    Text(icon, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === SAISIES ===
        Text("Saisie", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_blue), modifier = Modifier.padding(bottom = 12.dp))

        TextField(
            value = primogems.value,
            onValueChange = { primogems.value = it },
            label = { Text("💰 Primogems actuels") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = colorResource(R.color.genshin_blue), unfocusedContainerColor = colorResource(R.color.genshin_blue))
        )
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = wishes.value,
            onValueChange = { wishes.value = it },
            label = { Text("🎁 Vœux effectués") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = colorResource(R.color.genshin_blue), unfocusedContainerColor = colorResource(R.color.genshin_blue))
        )
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = pity.value,
            onValueChange = { pity.value = it },
            label = { Text("🎯 Pity actuel (0-89)") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = colorResource(R.color.genshin_blue), unfocusedContainerColor = colorResource(R.color.genshin_blue))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // === CALCULS ===
        val currentPity = pity.value.toIntOrNull() ?: 0
        val wishesRemaining = maxOf(0, 75 - currentPity)
        val primoNeeded = wishesRemaining * 160

        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_bg))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 Calculs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pity actuel", fontSize = 11.sp, color = colorResource(R.color.genshin_blue))
                    Text("$currentPity / 75", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Vœux avant soft pity", fontSize = 11.sp, color = colorResource(R.color.genshin_blue))
                    Text("$wishesRemaining vœux", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Primogems nécessaires", fontSize = 11.sp, color = colorResource(R.color.genshin_blue))
                    Text("$primoNeeded 💎", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(colorResource(R.color.genshin_blue))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((currentPity / 75f).coerceIn(0f, 1f))
                            .background(colorResource(R.color.genshin_gold))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // === BOUTONS ACTIONS ===
        Button(
            onClick = { onNavigateTo("wishes") },
            modifier = Modifier.fillMaxWidth().height(45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))
        ) {
            Text("📋 Historique des Tirages", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onNavigateTo("stats") },
            modifier = Modifier.fillMaxWidth().height(45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_purple))
        ) {
            Text("📊 Statistiques Détaillées", fontSize = 12.sp)
        }
    }
}

/**
 * Écran secondaire - Historique complet des tirages
 */
@Composable
fun AllWishesScreen(
    repository: WishRepository,
    selectedBanner: MutableState<BannerType>,
    onNavigateTo: (String) -> Unit
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val bannerStats = remember { mutableStateOf<List<BannerStats>>(emptyList()) }
    val globalStats = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            bannerStats.value = repository.getAllBannerStats()
            loadGlobalStats(repository, globalStats)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.dark_bg))
            .verticalScroll(rememberScrollState())
    ) {
        // === EN-TÊTE avec boutons retour et paramètres ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateTo("dashboard") }) {
                Text("◀", fontSize = 20.sp)
            }
            Text("📋 Historique", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
            IconButton(
                onClick = { onNavigateTo("settings") },
                modifier = Modifier.width(40.dp)
            ) {
                Text("⚙️", fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // === STATISTIQUES GLOBALES ===
        GlobalStatsCard(globalStats.value)

        Spacer(modifier = Modifier.height(16.dp))

        // === SÉLECTEUR DE BANNIÈRE ===
        BannerSelector(selectedBanner, bannerStats.value)

        Spacer(modifier = Modifier.height(16.dp))

        // === AFFICHAGE STATS BANNIÈRE SÉLECTIONNÉE ===
        val currentStats = bannerStats.value.find { it.bannerType == selectedBanner.value }
        if (currentStats != null) {
            BannerDetailsCard(currentStats, selectedBanner.value)
        } else {
            NoDataCard("Aucune données pour cette bannière")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === BOUTONS D'ACTION ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog.value = true },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.genshin_blue),
                    contentColor = colorResource(R.color.genshin_gold)
                )
            ) {
                Text("Ajouter 5★", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onNavigateTo("stats") },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.genshin_purple),
                    contentColor = colorResource(R.color.genshin_gold)
                )
            ) {
                Text("Stats", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // === DIALOG AJOUTER 5★ ===
    if (showAddDialog.value) {
        AddWishDialog(
            repository = repository,
            onDismiss = { showAddDialog.value = false },
            onAdd = { wish ->
                coroutineScope.launch {
                    repository.addWish(wish)
                    bannerStats.value = repository.getAllBannerStats()
                    loadGlobalStats(repository, globalStats)
                }
                showAddDialog.value = false
            }
        )
    }
}

/**
 * Charge les statistiques globales
 */
private suspend fun loadGlobalStats(
    repository: WishRepository,
    globalStats: MutableState<Map<String, Int>>
) {
    val allWishes = repository.getAllWishes()
    val totalWishes = allWishes.size
    val totalPrimogems = allWishes.sumOf { it.primogems }
    val total5Stars = allWishes.size
    
    val limitedWishes = allWishes.filter { 
        it.bannerType == BannerType.EVENT || it.bannerType == BannerType.CHRONICLE 
    }
    val wins = limitedWishes.count { it.is50Win }
    val losses = limitedWishes.count { !it.is50Win }
    val winRate = if (wins + losses > 0) (wins * 100) / (wins + losses) else 0

    globalStats.value = mapOf(
        "totalWishes" to totalWishes,
        "total5Stars" to total5Stars,
        "totalPrimogems" to totalPrimogems,
        "wins" to wins,
        "losses" to losses,
        "winRate" to winRate
    )
}

/**
 * Carte des statistiques globales
 */
@Composable
fun GlobalStatsCard(stats: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.card_bg),
            contentColor = colorResource(R.color.white)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "📊 Statistiques Globales",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.genshin_gold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge("Vœux", "${stats["totalWishes"] ?: 0}")
                StatBadge("5★", "${stats["total5Stars"] ?: 0}")
                StatBadge("Win %", "${stats["winRate"] ?: 0}%")
            }
        }
    }
}

/**
 * Badge de statistique
 */
@Composable
fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = colorResource(R.color.genshin_blue))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
    }
}

/**
 * Sélecteur de bannière avec boutons
 */
@Composable
fun BannerSelector(selectedBanner: MutableState<BannerType>, stats: List<BannerStats>) {
    val banners = listOf(
        BannerType.EVENT to "Événement",
        BannerType.CHRONICLE to "Chroniques",
        BannerType.PERMANENT to "Permanente",
        BannerType.WEAPON to "Armes"
    )

    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            "Sélectionner une bannière",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.genshin_gold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            banners.forEach { (bannerType, name) ->
                Button(
                    onClick = { selectedBanner.value = bannerType },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedBanner.value == bannerType)
                            colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue),
                        contentColor = if (selectedBanner.value == bannerType)
                            colorResource(R.color.dark_bg) else colorResource(R.color.genshin_gold)
                    )
                ) {
                    Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Carte détaillée d'une bannière
 */
@Composable
fun BannerDetailsCard(stats: BannerStats, bannerType: BannerType) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.card_bg),
            contentColor = colorResource(R.color.white)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                when (bannerType) {
                    BannerType.EVENT -> "⭐ Bannière Événement"
                    BannerType.CHRONICLE -> "🔁 Bannière Chroniques"
                    BannerType.PERMANENT -> "♾️ Bannière Permanente"
                    BannerType.WEAPON -> "⚔️ Bannière Armes"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.genshin_gold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Statistiques principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Vœux", "${stats.totalWishes}")
                StatItem("5★", "${stats.total5Stars}")
                StatItem("Pity", "${stats.currentPity}")
                StatItem("Primos", "${stats.totalPrimogems}")
            }

            // Stats 50/50 pour bannières limitées
            if (bannerType == BannerType.EVENT || bannerType == BannerType.CHRONICLE) {
                Spacer(modifier = Modifier.height(12.dp))
                val winRate = if (stats.win50_50Count + stats.lose50_50Count > 0) {
                    (stats.win50_50Count * 100) / (stats.win50_50Count + stats.lose50_50Count)
                } else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Win", "${stats.win50_50Count} (${winRate}%)", colorResource(R.color.genshin_blue))
                    StatItem("W-Streak", "${stats.wStreak}", colorResource(R.color.genshin_gold))
                    StatItem("L-Streak", "${stats.lStreak}", colorResource(R.color.genshin_purple))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Prochain garanti : ${if (stats.guaranteedNext) "OUI ✓" else "NON"}",
                    fontSize = 12.sp,
                    color = if (stats.guaranteedNext) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Élément de statistique
 */
@Composable
fun StatItem(label: String, value: String, color: Color = colorResource(R.color.genshin_gold)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = colorResource(R.color.genshin_blue))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * Carte affichant "Aucune données"
 */
@Composable
fun NoDataCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.card_bg),
            contentColor = colorResource(R.color.white)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                message,
                fontSize = 14.sp,
                color = colorResource(R.color.genshin_blue),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dialogue pour ajouter un nouveau vœu 5★
 * Détecte automatiquement si c'est un 50/50 perdu ou gagné basé sur la liste des permanents
 */
@Composable
fun AddWishDialog(repository: WishRepository?, onDismiss: () -> Unit, onAdd: (WishEntry) -> Unit) {
    val characterName = remember { mutableStateOf("") }
    val patch = remember { mutableStateOf("") }
    val pity = remember { mutableStateOf("") }
    val selectedBanner = remember { mutableStateOf(BannerType.EVENT) }
    val selectedRegion = remember { mutableStateOf(Region.MONDSTADT) }
    val is50Win = remember { mutableStateOf(true) }
    val guaranteedFlag = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val fif50Status = remember { mutableStateOf("(Auto-détection)") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(colorResource(R.color.card_bg)),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_bg))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Ajouter un 5★",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.genshin_gold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Champ Personnage/Arme
                TextField(
                    value = characterName.value,
                    onValueChange = { characterName.value = it },
                    label = { Text("Personnage / Arme") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Champ Patch
                TextField(
                    value = patch.value,
                    onValueChange = { patch.value = it },
                    label = { Text("Patch (ex: 4.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Champ Pity
                TextField(
                    value = pity.value,
                    onValueChange = { pity.value = it },
                    label = { Text("Pity au moment du pull") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sélecteur Bannière
                BannerDropdown(selectedBanner)

                Spacer(modifier = Modifier.height(8.dp))

                // Sélecteur Région
                RegionDropdown(selectedRegion)

                Spacer(modifier = Modifier.height(12.dp))

                // Résultat 50/50 - Détection automatique possible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Résultat 50/50 : ${if (is50Win.value) "WIN ✅" else "LOSE ❌"}",
                            fontSize = 12.sp,
                            color = if (is50Win.value) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_purple),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            fif50Status.value,
                            fontSize = 9.sp,
                            color = colorResource(R.color.genshin_blue)
                        )
                    }
                    Button(
                        onClick = {
                            // Auto-détecter basé sur la liste des permanents
                            if (repository != null && characterName.value.isNotEmpty()) {
                                coroutineScope.launch {
                                    val isLost = repository.isFiftyFiftyLost(characterName.value)
                                    is50Win.value = !isLost  // Inverser: si perdu, alors is50Win = false
                                    fif50Status.value = if (isLost) "Perdu (permanent détecté)" else "Gagné (nouveau)"
                                }
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.genshin_blue)
                        )
                    ) {
                        Text("🔍 Auto", fontSize = 9.sp)
                    }
                }

                // Flag Garanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Prochain garanti",
                        fontSize = 12.sp,
                        color = colorResource(R.color.white)
                    )
                    Checkbox(
                        checked = guaranteedFlag.value,
                        onCheckedChange = { guaranteedFlag.value = it },
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", color = colorResource(R.color.genshin_purple))
                    }

                    Button(
                        onClick = {
                            // Avant d'enregistrer: détection automatique si repository fourni
                            if (repository != null && characterName.value.isNotEmpty()) {
                                coroutineScope.launch {
                                    val isLost = repository.isFiftyFiftyLost(characterName.value)
                                    val finalIs50Win = !isLost  // Inverser: si perdu, alors is50Win = false
                                    
                                    onAdd(
                                        WishEntry(
                                            bannerType = selectedBanner.value,
                                            characterOrWeapon = characterName.value.ifEmpty { "Inconnu" },
                                            patch = patch.value.ifEmpty { "?" },
                                            region = selectedRegion.value,
                                            pityAtPull = pity.value.toIntOrNull() ?: 0,
                                            is50Win = finalIs50Win,  // Utiliser la détection auto
                                            guaranteedFlag = guaranteedFlag.value
                                        )
                                    )
                                }
                            } else {
                                // Fallback si pas de repository
                                onAdd(
                                    WishEntry(
                                        bannerType = selectedBanner.value,
                                        characterOrWeapon = characterName.value.ifEmpty { "Inconnu" },
                                        patch = patch.value.ifEmpty { "?" },
                                        region = selectedRegion.value,
                                        pityAtPull = pity.value.toIntOrNull() ?: 0,
                                        is50Win = is50Win.value,
                                        guaranteedFlag = guaranteedFlag.value
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))
                    ) {
                        Text("Enregistrer", color = colorResource(R.color.genshin_gold))
                    }
                }
            }
        }
    }
}

/**
 * Dropdown pour sélectionner une bannière
 */
@Composable
fun BannerDropdown(selectedBanner: MutableState<BannerType>) {
    val expanded = remember { mutableStateOf(false) }
    val banners = listOf(
        BannerType.EVENT to "Événement",
        BannerType.CHRONICLE to "Chroniques",
        BannerType.PERMANENT to "Permanente",
        BannerType.WEAPON to "Armes"
    )

    Box {
        Button(
            onClick = { expanded.value = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.genshin_blue),
                contentColor = colorResource(R.color.genshin_gold)
            )
        ) {
            Text(banners.find { it.first == selectedBanner.value }?.second ?: "Sélectionner")
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            banners.forEach { (bannerType, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        selectedBanner.value = bannerType
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown pour sélectionner une région
 */
@Composable
fun RegionDropdown(selectedRegion: MutableState<Region>) {
    val expanded = remember { mutableStateOf(false) }
    val regions = Region.values()

    Box {
        Button(
            onClick = { expanded.value = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.genshin_purple),
                contentColor = colorResource(R.color.genshin_gold)
            )
        ) {
            Text(selectedRegion.value.name)
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region.name) },
                    onClick = {
                        selectedRegion.value = region
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Écran des statistiques détaillées
 */
@Composable
fun StatsScreen(repository: WishRepository, onBack: () -> Unit) {
    val selectedStatsTab = remember { mutableStateOf(0) }
    val bannerStats = remember { mutableStateOf<List<BannerStats>>(emptyList()) }
    val regionStats = remember { mutableStateOf<List<RegionStats>>(emptyList()) }
    val allWishes = remember { mutableStateOf<List<WishEntry>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            bannerStats.value = repository.getAllBannerStats()
            regionStats.value = repository.getAllRegionStats()
            allWishes.value = repository.getAllWishes()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.dark_bg))
    ) {
        // En-tête avec bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.genshin_blue))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = colorResource(R.color.genshin_gold)
                )
            }
            Text(
                "Statistiques Détaillées",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.genshin_gold),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Onglets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Bannières", "Régions", "Historique").forEachIndexed { index, title ->
                Button(
                    onClick = { selectedStatsTab.value = index },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStatsTab.value == index)
                            colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue),
                        contentColor = if (selectedStatsTab.value == index)
                            colorResource(R.color.dark_bg) else colorResource(R.color.genshin_gold)
                    )
                ) {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contenu onglets
        when (selectedStatsTab.value) {
            0 -> StatsTableBanners(bannerStats.value)
            1 -> StatsTableRegions(regionStats.value, allWishes.value)
            2 -> WishHistoryTable(
                allWishes.value,
                repository = repository,
                onUpdate = {
                    coroutineScope.launch {
                        allWishes.value = repository.getAllWishes()
                        bannerStats.value = repository.getAllBannerStats()
                    }
                }
            )
        }
    }
}

/**
 * Tableau des stats par bannière
 */
@Composable
fun StatsTableBanners(stats: List<BannerStats>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stats) { stat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.card_bg),
                    contentColor = colorResource(R.color.white)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        when (stat.bannerType) {
                            BannerType.EVENT -> "⭐ Événement"
                            BannerType.CHRONICLE -> "🔁 Chroniques"
                            BannerType.PERMANENT -> "♾️ Permanente"
                            BannerType.WEAPON -> "⚔️ Armes"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.genshin_gold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    StatsRow("Vœux", "${stat.totalWishes}")
                    StatsRow("5★ obtenus", "${stat.total5Stars}")
                    StatsRow("Pity actuel", "${stat.currentPity}")
                    StatsRow("Pity moyen", "${stat.averagePity}")
                    StatsRow("Pity min", "${stat.minPity} → ${stat.minPityCharacter}")
                    StatsRow("Pity max", "${stat.maxPity} → ${stat.maxPityCharacter}")
                    StatsRow("Primogems", "${stat.totalPrimogems}")

                    if (stat.bannerType == BannerType.EVENT || stat.bannerType == BannerType.CHRONICLE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val winRate = if (stat.win50_50Count + stat.lose50_50Count > 0) {
                            (stat.win50_50Count * 100) / (stat.win50_50Count + stat.lose50_50Count)
                        } else 0
                        StatsRow("Wins", "${stat.win50_50Count}", colorResource(R.color.genshin_gold))
                        StatsRow("Losses", "${stat.lose50_50Count}", colorResource(R.color.genshin_purple))
                        StatsRow("Win %", "$winRate%")
                        StatsRow("W-Streak", "${stat.wStreak}", if (stat.wStreak > 0) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue))
                        StatsRow("L-Streak", "${stat.lStreak}", if (stat.lStreak > 0) colorResource(R.color.genshin_purple) else colorResource(R.color.genshin_blue))
                        StatsRow("Garanti", if (stat.guaranteedNext) "OUI ✓" else "NON")
                    }
                }
            }
        }
    }
}

/**
 * Tableau des stats par région
 */
@Composable
fun StatsTableRegions(stats: List<RegionStats>, allWishes: List<WishEntry>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stats) { stat ->
            // Calculer pity moyen et primogems pour cette région
            val regionWishes = allWishes.filter { it.region == stat.region }
            val avgPity = if (regionWishes.isNotEmpty()) {
                regionWishes.map { it.pityAtPull }.average().toInt()
            } else 0
            val totalPrimogems = regionWishes.sumOf { it.primogems }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.card_bg),
                    contentColor = colorResource(R.color.white)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        stat.region.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.genshin_gold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    StatsRow("Vœux", "${stat.totalWishes}")
                    StatsRow("5★ obtenus", "${stat.total5Stars}")
                    StatsRow("Pity min", "${stat.minPity} → ${stat.minPityCharacter}")
                    StatsRow("Pity max", "${stat.maxPity} → ${stat.maxPityCharacter}")
                    val winRate = if (stat.wins + stat.losses > 0) {
                        (stat.wins * 100) / (stat.wins + stat.losses)
                    } else 0
                    StatsRow("Win Rate", "$winRate%")
                    StatsRow("Wins", "${stat.wins}", colorResource(R.color.genshin_gold))
                    StatsRow("Losses", "${stat.losses}", colorResource(R.color.genshin_purple))
                    StatsRow("Pity moyen", "$avgPity")
                    StatsRow("Primogems", "$totalPrimogems")
                }
            }
        }
    }
}

/**
 * Ligne de statistique
 */
@Composable
fun StatsRow(label: String, value: String, valueColor: Color = colorResource(R.color.genshin_gold)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = colorResource(R.color.genshin_blue))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

/**
 * Tableau d'historique des vœux avec actions (édition/suppression)
 */
@Composable
fun WishHistoryTable(wishes: List<WishEntry>, repository: WishRepository? = null, onUpdate: (() -> Unit)? = null) {
    val editingWish = remember { mutableStateOf<WishEntry?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (wishes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aucun vœu enregistré",
                fontSize = 14.sp,
                color = colorResource(R.color.genshin_blue),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(wishes) { wish ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.card_bg),
                        contentColor = colorResource(R.color.white)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Titre : Personnage et bannière
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                wish.characterOrWeapon,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.genshin_gold)
                            )
                            Text(
                                when (wish.bannerType) {
                                    BannerType.EVENT -> "⭐ Evt"
                                    BannerType.CHRONICLE -> "🔁 Chr"
                                    BannerType.PERMANENT -> "♾️ Perm"
                                    BannerType.WEAPON -> "⚔️ Arm"
                                },
                                fontSize = 10.sp,
                                color = colorResource(R.color.genshin_purple)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Informations détaillées
                        StatsRow("Patch", wish.patch)
                        StatsRow("Région", wish.region.name)
                        StatsRow("Pity", "${wish.pityAtPull}")
                        StatsRow("Primogems", "${wish.primogems}")
                        
                        // Résultat 50/50 pour bannières limitées
                        if (wish.bannerType == BannerType.EVENT || wish.bannerType == BannerType.CHRONICLE) {
                            StatsRow(
                                "50/50",
                                if (wish.is50Win) "WIN" else "LOSE",
                                if (wish.is50Win) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_purple)
                            )
                            StatsRow(
                                "Garanti",
                                if (wish.guaranteedFlag) "OUI ✓" else "NON",
                                if (wish.guaranteedFlag) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue)
                            )
                        }
                        
                        // Timestamp
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FR).format(Date(wish.timestamp)),
                            fontSize = 9.sp,
                            color = colorResource(R.color.genshin_blue)
                        )

                        // Boutons d'actions si repository fourni
                        if (repository != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { editingWish.value = wish },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(R.color.genshin_blue)
                                    )
                                ) {
                                    Text("✏️ Modifier", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.deleteWish(wish)
                                            onUpdate?.invoke()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(R.color.genshin_purple)
                                    )
                                ) {
                                    Text("🗑️ Supprimer", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog de modification
    if (editingWish.value != null && repository != null) {
        EditWishDialog(
            wish = editingWish.value!!,
            repository = repository,
            onDismiss = { editingWish.value = null },
            onSave = {
                editingWish.value = null
                onUpdate?.invoke()
            }
        )
    }
}

/**
 * Dialog pour éditer un vœu existant
 */
@Composable
fun EditWishDialog(
    wish: WishEntry,
    repository: WishRepository,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val characterName = remember { mutableStateOf(wish.characterOrWeapon) }
    val patch = remember { mutableStateOf(wish.patch) }
    val pity = remember { mutableStateOf(wish.pityAtPull.toString()) }
    val selectedBanner = remember { mutableStateOf(wish.bannerType) }
    val selectedRegion = remember { mutableStateOf(wish.region) }
    val is50Win = remember { mutableStateOf(wish.is50Win) }
    val guaranteedFlag = remember { mutableStateOf(wish.guaranteedFlag) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(colorResource(R.color.card_bg)),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_bg))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Modifier le 5★",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.genshin_gold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = characterName.value,
                    onValueChange = { characterName.value = it },
                    label = { Text("Personnage / Arme") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = patch.value,
                    onValueChange = { patch.value = it },
                    label = { Text("Patch (ex: 4.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = pity.value,
                    onValueChange = { pity.value = it },
                    label = { Text("Pity au moment du pull") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.white),
                        unfocusedTextColor = colorResource(R.color.white),
                        focusedContainerColor = colorResource(R.color.genshin_blue),
                        unfocusedContainerColor = colorResource(R.color.genshin_blue),
                        focusedLabelColor = colorResource(R.color.genshin_gold),
                        unfocusedLabelColor = colorResource(R.color.genshin_gold)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                BannerDropdown(selectedBanner)
                Spacer(modifier = Modifier.height(8.dp))
                RegionDropdown(selectedRegion)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Résultat 50/50 : ${if (is50Win.value) "WIN" else "LOSE"}", fontSize = 12.sp)
                    Switch(
                        checked = is50Win.value,
                        onCheckedChange = { is50Win.value = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Garanti prochain : ${if (guaranteedFlag.value) "OUI" else "NON"}", fontSize = 12.sp)
                    Switch(
                        checked = guaranteedFlag.value,
                        onCheckedChange = { guaranteedFlag.value = it },
                        modifier = Modifier.scale(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.genshin_blue)
                        )
                    ) {
                        Text("Annuler")
                    }

                    Button(
                        onClick = {
                            val pityInt = pity.value.toIntOrNull() ?: wish.pityAtPull
                            val updatedWish = wish.copy(
                                characterOrWeapon = characterName.value,
                                patch = patch.value,
                                pityAtPull = pityInt,
                                bannerType = selectedBanner.value,
                                region = selectedRegion.value,
                                is50Win = is50Win.value,
                                guaranteedFlag = guaranteedFlag.value
                            )
                            coroutineScope.launch {
                                repository.updateWish(updatedWish)
                                onSave()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.genshin_gold),
                            contentColor = colorResource(R.color.dark_bg)
                        )
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

/**
 * Écran Paramètres - Section 1: Données, 2: Affichage, 3: Régions, 4: Permanents, 5: Options avancées
 */
@Composable
fun SettingsScreen(repository: WishRepository, onBack: () -> Unit) {
    val tab = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val regions = remember { mutableStateOf<List<UserRegion>>(emptyList()) }
    val chars = remember { mutableStateOf<List<PermanentCharacter>>(emptyList()) }
    val autoBackup = remember { mutableStateOf(true) }
    val darkTheme = remember { mutableStateOf(true) }
    val auto50 = remember { mutableStateOf(true) }
    val wishCost = remember { mutableStateOf("160") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            regions.value = repository.getAllUserRegions()
            chars.value = repository.getAllPermanentCharacters()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colorResource(R.color.dark_bg))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Text("◀", fontSize = 20.sp) }
            Text("⚙️ Paramètres", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
            Spacer(modifier = Modifier.width(40.dp))
        }

        Row(modifier = Modifier.fillMaxWidth().background(colorResource(R.color.genshin_blue)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf("Données", "Affichage", "Régions", "Permanents", "Avancé").forEachIndexed { i, label ->
                Button(onClick = { tab.value = i }, modifier = Modifier.weight(1f).height(32.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = if (tab.value == i) colorResource(R.color.genshin_gold) else colorResource(R.color.genshin_blue),
                    contentColor = if (tab.value == i) colorResource(R.color.dark_bg) else colorResource(R.color.genshin_gold)
                )) {
                    Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(12.dp)) {
            when (tab.value) {
                0 -> {
                    item {
                        Text("📊 Données", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 8.dp))
                        Button(onClick = { /* Export */ }, modifier = Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))) { Text("📥 Exporter JSON") }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { /* Import */ }, modifier = Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))) { Text("📤 Importer JSON") }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sauvegarde auto", fontSize = 11.sp)
                            Switch(checked = autoBackup.value, onCheckedChange = { autoBackup.value = it }, modifier = Modifier.scale(0.8f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_purple))) { Text("🔄 Réinitialiser") }
                    }
                }
                1 -> {
                    item {
                        Text("🎨 Affichage", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thème sombre", fontSize = 11.sp)
                            Switch(checked = darkTheme.value, onCheckedChange = { darkTheme.value = it }, modifier = Modifier.scale(0.8f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("📊 Plus d'options bientôt...", fontSize = 10.sp, color = colorResource(R.color.genshin_blue))
                    }
                }
                2 -> {
                    item { Text("📍 Régions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 8.dp)) }
                    items(regions.value) { r ->
                        Card(modifier = Modifier.fillMaxWidth().padding(4.dp), colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_bg))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(r.name, fontSize = 11.sp, color = colorResource(R.color.genshin_gold), fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Button(onClick = { }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))) { Text("✏️", fontSize = 8.sp) }
                                    Button(onClick = { coroutineScope.launch { repository.deleteUserRegion(r); regions.value = repository.getAllUserRegions() } }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_purple))) { Text("🗑️", fontSize = 8.sp) }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { coroutineScope.launch { repository.addUserRegion("Nouvelle"); regions.value = repository.getAllUserRegions() } }, modifier = Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_gold))) { Text("➕ Ajouter", color = colorResource(R.color.dark_bg)) }
                    }
                }
                3 -> {
                    item { Text("👤 Permanents", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 8.dp)) }
                    items(chars.value) { c ->
                        Card(modifier = Modifier.fillMaxWidth().padding(4.dp), colors = CardDefaults.cardColors(containerColor = colorResource(R.color.card_bg))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(c.characterName, fontSize = 11.sp, color = colorResource(R.color.genshin_gold), fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Button(onClick = { }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_blue))) { Text("✏️", fontSize = 8.sp) }
                                    Button(onClick = { coroutineScope.launch { repository.deletePermanentCharacter(c); chars.value = repository.getAllPermanentCharacters() } }, modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_purple))) { Text("🗑️", fontSize = 8.sp) }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { coroutineScope.launch { repository.addPermanentCharacter("Nouveau"); chars.value = repository.getAllPermanentCharacters() } }, modifier = Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.genshin_gold))) { Text("➕ Ajouter", color = colorResource(R.color.dark_bg)) }
                    }
                }
                4 -> {
                    item {
                        Text("⚡ Options Avancées", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold), modifier = Modifier.padding(bottom = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Détection 50/50 auto", fontSize = 11.sp)
                            Switch(checked = auto50.value, onCheckedChange = { auto50.value = it }, modifier = Modifier.scale(0.8f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("💎 Coût d'un vœu (primogems)", fontSize = 11.sp, color = colorResource(R.color.genshin_blue))
                        TextField(value = wishCost.value, onValueChange = { wishCost.value = it }, modifier = Modifier.fillMaxWidth().height(40.dp), textStyle = TextStyle(fontSize = 12.sp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📋 Filtrage historique", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.genshin_gold))
                        Text("• Par patch\n• Par région\n• Par bannière", fontSize = 10.sp, color = colorResource(R.color.genshin_blue), modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

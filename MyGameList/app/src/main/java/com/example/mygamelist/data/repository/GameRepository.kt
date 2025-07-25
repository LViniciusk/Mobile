package com.example.mygamelist.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mygamelist.data.api.RawgService
import com.example.mygamelist.data.dao.GameDao
import com.example.mygamelist.data.model.Game
import com.example.mygamelist.data.model.GameCacheType
import com.example.mygamelist.data.model.GameDetail
import com.example.mygamelist.data.model.toDomainGame
import com.example.mygamelist.data.model.toDomainGameDetail
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

private val EXPLICIT_TAG_SLUGS_TO_EXCLUDE = setOf(
    "sex",
    "nudity",
    "sexual-content",
    "NSFW",
)

interface GameRepository {
    fun getNewReleaseGames(): Flow<List<Game>>
    fun getComingSoonGames(): Flow<List<Game>>
    suspend fun refreshNewReleaseGamesCache()
    suspend fun refreshComingSoonGamesCache()
    suspend fun getGameDetails(gameId: Int): GameDetail
    fun getGamesByCacheType(type: GameCacheType): Flow<List<Game>>
    suspend fun deleteGamesByCacheType(type: GameCacheType)

    suspend fun addGameToUserList(game: Game, userId: String)
    suspend fun removeGameFromUserList(game: Game, userId: String)
    fun getAllUserSavedGames(userId: String): Flow<List<Game>>
    suspend fun getUserGameById(gameId: Int, userId: String): Game?

    suspend fun isGameSavedLocally(gameId: Int, userId: String): Boolean

    suspend fun syncUserGamesFromFirestoreToRoom(userId: String)
    suspend fun deleteUserGamesFromFirestore(userId: String)
    suspend fun deleteUserGames(userId: String)
}

class GameRepositoryImpl @Inject constructor(
    private val rawgService: RawgService,
    private val gameDao: GameDao,
    private val apiKey: String,
    private val firestore: FirebaseFirestore
) : GameRepository {

    private fun getUserGamesFirestoreCollection(userId: String) =
        firestore.collection("users").document(userId).collection("userGames")


    override fun getNewReleaseGames(): Flow<List<Game>> {
        return gameDao.getGamesByCacheType(GameCacheType.NEW_RELEASE)
            .distinctUntilChanged()
    }

    override fun getComingSoonGames(): Flow<List<Game>> {
        return gameDao.getGamesByCacheType(GameCacheType.COMING_SOON)
            .distinctUntilChanged()
    }


    override fun getGamesByCacheType(type: GameCacheType): Flow<List<Game>> {
        return gameDao.getGamesByCacheType(type)
    }

    override suspend fun deleteGamesByCacheType(type: GameCacheType) {
        gameDao.deleteGamesByCacheType(type)
    }

    override suspend fun deleteUserGames(userId: String) {
        gameDao.deleteUserGames(userId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun refreshNewReleaseGamesCache() {
        Log.d("GameRepositoryImpl", "Iniciando refresh de Novos Lançamentos da API.")
        try {
            val today = LocalDate.now()
            val thirtyDaysAgo = today.minusDays(30)
            val dateRange = "${thirtyDaysAgo.format(DateTimeFormatter.ISO_DATE)},${today.format(DateTimeFormatter.ISO_DATE)}"

            val response = rawgService.getNewReleases(apiKey, dateRange)

            val filteredResults = response.results.filter { gameResult ->
                val hasExplicitTag = gameResult.tags?.any { tag -> EXPLICIT_TAG_SLUGS_TO_EXCLUDE.contains(tag.slug) } == true
                !(hasExplicitTag)
            }

            val gamesFromApi = filteredResults.map {
                it.toDomainGame().copy(
                    cacheType = GameCacheType.NEW_RELEASE,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            gameDao.deleteGamesByCacheType(GameCacheType.NEW_RELEASE)
            gameDao.insertAllGames(gamesFromApi)
            Log.d("GameRepositoryImpl", "Novos Lançamentos atualizados no cache: ${gamesFromApi.size} jogos (após filtro de conteúdo).")
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Erro ao refrescar cache de Novos Lançamentos: ${e.localizedMessage}", e)
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun refreshComingSoonGamesCache() {
        Log.d("GameRepositoryImpl", "Iniciando refresh de Em Breve da API.")
        try {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val threeMonthsFromNow = today.plusMonths(3)
            val dateRange = "${tomorrow.format(DateTimeFormatter.ISO_DATE)},${threeMonthsFromNow.format(DateTimeFormatter.ISO_DATE)}"

            val response = rawgService.getComingSoon(apiKey, dateRange)

            val filteredResults = response.results.filter { gameResult ->
                val hasExplicitTag = gameResult.tags?.any { tag -> EXPLICIT_TAG_SLUGS_TO_EXCLUDE.contains(tag.slug) } == true
                !(hasExplicitTag)
            }

            val gamesFromApi = filteredResults.map {
                it.toDomainGame().copy(
                    cacheType = GameCacheType.COMING_SOON,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            gameDao.deleteGamesByCacheType(GameCacheType.COMING_SOON)
            gameDao.insertAllGames(gamesFromApi)
            Log.d("GameRepositoryImpl", "Jogos Em Breve atualizados no cache: ${gamesFromApi.size} jogos (após filtro de conteúdo).")
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Erro ao refrescar cache de Em Breve: ${e.localizedMessage}", e)
            throw e
        }
    }

    override suspend fun addGameToUserList(game: Game, userId: String) {
        val gameToSave = game.copy(cacheType = GameCacheType.NONE, lastUpdated = null, userId = userId)
        gameDao.insertGame(gameToSave)
        getUserGamesFirestoreCollection(userId).document(game.id.toString()).set(gameToSave).await()
        Log.d("GameRepositoryImpl", "Jogo '${game.title}' adicionado à lista local e Firestore do usuário $userId.")
    }

    override suspend fun removeGameFromUserList(game: Game, userId: String) {
        val userSpecificGame = gameDao.getUserGameById(game.id, userId)

        if (userSpecificGame != null) {
            gameDao.deleteGame(userSpecificGame)
            getUserGamesFirestoreCollection(userId).document(game.id.toString()).delete().await()
            Log.d("GameRepositoryImpl", "Jogo '${game.title}' removido da lista local e Firestore do usuário $userId.")
        } else {
            Log.w("GameRepositoryImpl", "Tentativa de remover jogo '${game.title}' do usuário $userId, mas não encontrado localmente com cacheType NONE. Tentando remover apenas do Firestore.")
            getUserGamesFirestoreCollection(userId).document(game.id.toString()).delete().await()
        }
    }

    override fun getAllUserSavedGames(userId: String): Flow<List<Game>> {
        return gameDao.getAllUserGames(userId)
    }

    override suspend fun getUserGameById(gameId: Int, userId: String): Game? {
        return gameDao.getUserGameById(gameId, userId)
    }

    override suspend fun isGameSavedLocally(gameId: Int, userId: String): Boolean {
        return gameDao.getUserGameById(gameId, userId) != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGameDetails(gameId: Int): GameDetail {
        Log.d("GameRepositoryImpl", "Buscando detalhes do jogo ID: $gameId da API.")
        try {
            val response = rawgService.getGameDetails(gameId, apiKey)
            val hasExplicitTagInDetails = response.tags?.any { tag -> EXPLICIT_TAG_SLUGS_TO_EXCLUDE.contains(tag.slug) } ?: false
            if (hasExplicitTagInDetails) {
                Log.w("GameRepositoryImpl", "Detalhes de jogo com tags explícitas (${response.name}) solicitados.")
            }
            return response.toDomainGameDetail()
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Erro ao buscar detalhes do jogo ID: $gameId: ${e.localizedMessage}", e)
            throw e
        }
    }

    override suspend fun syncUserGamesFromFirestoreToRoom(userId: String) {
        Log.d("GameRepositoryImpl", "Iniciando sincronização de jogos do usuário $userId do Firestore para o Room.")
        try {
            val firestoreGames = getUserGamesFirestoreCollection(userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Game::class.java) }

            gameDao.deleteUserGames(userId)
            gameDao.insertAllGames(firestoreGames)
            Log.d("GameRepositoryImpl", "Sincronização de jogos do usuário $userId concluída. ${firestoreGames.size} jogos sincronizados.")
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Erro ao sincronizar jogos do usuário $userId do Firestore: ${e.localizedMessage}", e)
            throw e
        }
    }

    override suspend fun deleteUserGamesFromFirestore(userId: String) {
        Log.d("GameRepositoryImpl", "Deletando todos os jogos do usuário $userId no Firestore.")
        try {
            val batch = firestore.batch()
            val gamesSnapshot = getUserGamesFirestoreCollection(userId).get().await()
            for (document in gamesSnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
            Log.d("GameRepositoryImpl", "Todos os jogos do usuário $userId deletados do Firestore.")
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Erro ao deletar jogos do usuário $userId no Firestore: ${e.localizedMessage}", e)
            throw e
        }
    }
}
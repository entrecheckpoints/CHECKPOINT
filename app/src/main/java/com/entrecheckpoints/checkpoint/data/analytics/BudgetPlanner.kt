package com.entrecheckpoints.checkpoint.data.analytics

import com.entrecheckpoints.checkpoint.data.local.GameEntity
import com.entrecheckpoints.checkpoint.data.local.PricePointEntity
import com.entrecheckpoints.checkpoint.data.model.LibraryStatus

object BudgetPlanner {
    data class Suggestion(
        val games: List<GameEntity>,
        val totalCents: Long,
        val totalSavingsCents: Long,
    )

    fun suggest(
        games: List<GameEntity>,
        historyByGame: Map<String, List<PricePointEntity>>,
        remainingCents: Long,
        maxGames: Int = 5,
    ): Suggestion {
        if (remainingCents <= 0) return Suggestion(emptyList(), 0, 0)
        val candidates = games
            .filter { it.libraryStatus == LibraryStatus.WISHLIST.id && it.priceCents in 1..remainingCents }
            .map { game ->
                val analytics = DealAnalytics.calculate(game, historyByGame[game.id].orEmpty())
                Candidate(
                    game = game,
                    score = analytics.dealScore,
                    savings = analytics.savingsCents,
                    value = analytics.dealScore * 1000.0 + analytics.savingsCents / 100.0,
                )
            }
            .sortedWith(compareByDescending<Candidate> { it.value / it.game.priceCents.coerceAtLeast(1) }.thenByDescending { it.score })

        val selected = mutableListOf<GameEntity>()
        var total = 0L
        var savings = 0L
        candidates.forEach { candidate ->
            if (selected.size >= maxGames) return@forEach
            if (total + candidate.game.priceCents <= remainingCents) {
                selected += candidate.game
                total += candidate.game.priceCents
                savings += candidate.savings
            }
        }
        return Suggestion(selected, total, savings)
    }

    private data class Candidate(
        val game: GameEntity,
        val score: Double,
        val savings: Long,
        val value: Double,
    )
}

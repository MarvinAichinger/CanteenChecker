package com.example.canteenchecker.consumerapp.infrastructure

import com.example.canteenchecker.consumerapp.logic.Canteen
import com.example.canteenchecker.consumerapp.logic.CanteenApi
import com.example.canteenchecker.consumerapp.logic.CanteenDetails
import com.example.canteenchecker.consumerapp.logic.CanteenResult

internal class MockCanteenApi : CanteenApi {

    private val canteens = listOf(
        Canteen("1", "Foo", "Menu 1", null, null),
        Canteen("2", "Bar", "Menu 2", 1250, 3.4),
        Canteen("3", "Baz", null, 1490, 2.4),
    )

    override suspend fun getCanteens(query: String): Result<CanteenResult> = Result.success(
        canteens
            .filter { c -> c.name.contains(query, true) }
            .let { CanteenResult(it, it.size) }
    )

    override suspend fun getCanteen(canteenId: String): Result<CanteenDetails> {
        TODO("Not yet implemented")
    }

}

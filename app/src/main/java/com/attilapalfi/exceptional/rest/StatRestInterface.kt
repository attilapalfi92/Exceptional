package com.attilapalfi.exceptional.rest

import retrofit.http.GET
import java.util.*

/**
 * Created by palfi on 2015-10-23.
 */
public interface StatRestInterface {
    @GET("/stats/globalThrowCounts")
    fun getGlobalThrowCounts(): LinkedHashMap<Int, Int>
}
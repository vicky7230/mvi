package com.vicky7230.mvi.data.network

import com.google.gson.JsonElement
import com.vicky7230.mvi.data.network.api.NetworkResult
import com.vicky7230.mvi.data.network.api.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ApiHelperImp
    @Inject
    constructor(
        private val api: Api,
    ) : ApiHelper {
        override suspend fun getTodos(
        ): Flow<NetworkResult<JsonElement>> {
            return handleApi { api.getTodos() }
        }


        private fun <T : Any> handleApi(execute: suspend () -> Response<T>): Flow<NetworkResult<T>> {
            return flow {
                emit(NetworkResult.Loading)
                try {
                    val response = execute()
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        emit(NetworkResult.Success(body))
                    } else {
                        emit(
                            NetworkResult.Error(
                                code = response.code(),
                                message = "${response.message()} (${System.currentTimeMillis()})",
                            ),
                        )
                    }
                } catch (httpException: HttpException) {
                    Timber.e(httpException)
                    emit(
                        NetworkResult.Error(
                            code = httpException.code(),
                            message = "${httpException.message()} + ${System.currentTimeMillis()}",
                        ),
                    )
                } catch (throwable: Throwable) {
                    Timber.e(throwable)
                    emit(NetworkResult.Exception(Exception("${throwable.localizedMessage} (${System.currentTimeMillis()})")))
                }
            }.flowOn(Dispatchers.IO)
        }
    }

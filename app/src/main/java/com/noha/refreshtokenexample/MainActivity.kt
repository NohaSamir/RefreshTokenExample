package com.noha.refreshtokenexample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.simpleName

    // Fail in first time only
    var isFirstTimeToCallGetBooks = true
    var isFirstTimeToCallGetBooksById = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //First token
        SharedPreferenceManager.saveToken(
            TokenResponse("ONE", "Refresh_Token_One", 1630008916902)
        )

        Log.d(TAG, "Initial Value $SharedPreferenceManager.getToken()")

        testRefreshToken()
    }

    private fun testRefreshToken() {
        lifecycle.coroutineScope.launch {
            coroutineScope {
                Log.d(TAG, "Test if 2 apis called at the same time")
                Log.d(TAG, "***********************************")
                val deferred = listOf(
                    async { getBooks() },
                    async { getBookById("1") })
                deferred.awaitAll()

                Log.d(TAG, "***********************************")
                Log.d(TAG, "Test if the token already refreshed from a few min's")
                isFirstTimeToCallGetBooks = true
                getBooks()
            }
        }
    }


    private suspend fun getBooks() {
        Log.d(TAG, "Get books called " + Calendar.getInstance().timeInMillis)


        if (isFirstTimeToCallGetBooks) {
            isFirstTimeToCallGetBooks = false
            //Call API and it return 401: Token expire, the we have to refresh the token
            refreshToken {
                getBooks()
            }
        } else {
            Log.d(TAG, "Get books Success in second time")
        }
    }

    private suspend fun getBookById(id: String) {
        Log.d(TAG, "Get books by id called " + Calendar.getInstance().timeInMillis)

        if (isFirstTimeToCallGetBooksById) {
            isFirstTimeToCallGetBooksById = false
            refreshToken {
                getBookById(id)
            }
        } else {
            Log.d(TAG, "Get books by id Success in second time")
        }
    }

    companion object {
        var tokenLoading = false
        var apisStack: Queue<suspend () -> Any> = LinkedList()
    }

    @Synchronized
    private suspend fun refreshToken(apiCall: suspend () -> Any) {
        Log.d(TAG, "Refresh token called " + Calendar.getInstance().timeInMillis)

        when {
            //If the token api already in progress, and refresh token called from another API
            tokenLoading -> {
                Log.d(TAG, "Token refreshed already in progress")
                apisStack.add(apiCall)
            }

            //If the token updated from less than 1 min, try the API again
            //If your token expire after 1 day, you can increase the time as you want
            Calendar.getInstance().timeInMillis - SharedPreferenceManager.getToken().lastUpdatedTime < 60000 -> {
                Log.d(TAG, "Token refreshed from a few seconds")
                apiCall.invoke()
            }


            else -> {
                tokenLoading = true

                //Delay represent API call
                delay(3000)

                val newToken =
                    TokenResponse("TWO", "Refresh_Token_Two", Calendar.getInstance().timeInMillis)
                SharedPreferenceManager.saveToken(newToken)

                Log.d(TAG, "New token $newToken")
                tokenLoading = false

                //retry
                apiCall.invoke()

                while (apisStack.peek() != null) {
                    apisStack.poll()?.invoke()
                }
            }
        }
    }
}
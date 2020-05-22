package com.fpradipt.fokkuy.view_model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fpradipt.fokkuy.db.TimerUsageDao
import com.fpradipt.fokkuy.model.UsageFirestoreModel
import com.fpradipt.fokkuy.model.UsageModel
import com.fpradipt.fokkuy.utils.formatLog
import com.google.android.gms.tasks.OnFailureListener
import com.google.common.math.LongMath
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class UsageViewModel(
    private val database: TimerUsageDao,
    application: Application,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {
    private var firestore = Firebase.firestore
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val _histories = database.getHistory()
    private var logUsage = MutableLiveData<UsageModel?>()
    var logFirebaseLvData = MutableLiveData<List<UsageFirestoreModel>>()
    var logFirebase = initFirestore()
    val histories: LiveData<List<UsageModel>>
        get() = _histories

    val parsedHist = Transformations.map(logFirebase) { history ->
        formatLog(history, application.resources)
    }

    private val collection = "usages"


    init {
        initTimer()
        initFirestore()
    }

    private fun initFirestore(): LiveData<List<UsageFirestoreModel>> {
        val listFirestoreModel = mutableListOf<UsageFirestoreModel>()

        firestore.collection("users/${auth.currentUser!!.uid}/${collection}")
            .addSnapshotListener { docs, e ->
                if (e != null) {
                    logFirebaseLvData.value = null
                    return@addSnapshotListener
                }

                for (doc in docs!!) {
//                    Log.d("DOCS", doc.toObject(UsageFirestoreModel::class.java).toString())
//                    val docObject = doc.toObject(UsageFirestoreModel::class.java)
                    val model = UsageFirestoreModel()
                    doc.getLong("start_timer")?.let {
                        model.startTimer = it
                    }

                    doc.getLong("end_timer")?.let {
                        model.endTimer = it
                    }

                    doc.getLong("duration")?.let {
                        model.duration = it.toInt()
                    }

                    doc.getString("created_at")?.let {
                        model.createdAt = it
                    }
                    listFirestoreModel.add(
                        model
                    )
                }
                logFirebaseLvData.value = listFirestoreModel

            }
        return logFirebaseLvData
    }

    private fun initTimer() {
        uiScope.launch {
            logUsage.value = getCurrentData()
            Log.d("INIT", logUsage.value.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onClear() {
        uiScope.launch {
            clearHist()
            Log.d("Clear", logUsage.value.toString())
        }
    }

    fun onStartTimer() {
        uiScope.launch {
            val logInit = UsageModel()
            logInit.startTimer = System.currentTimeMillis()

            insertHist(logInit)
            Log.d("START", logInit.toString())
            logUsage.value = getCurrentData()
        }
    }

    fun onStopTimer() {
        uiScope.launch {
            val oldLog = logUsage.value ?: return@launch
            oldLog.endTimer = System.currentTimeMillis()
            oldLog.duration = ((oldLog.endTimer - oldLog.startTimer) / 1000).toInt()
            oldLog.createdAt = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss")
                .withZone(ZoneOffset.systemDefault())
                .format(Instant.now())

            val firestoreModel = UsageFirestoreModel()
            firestoreModel.startTimer = oldLog.startTimer
            firestoreModel.endTimer = oldLog.endTimer
            firestoreModel.duration = oldLog.duration
            firestoreModel.createdAt = oldLog.createdAt

            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid
            firestore = Firebase.firestore
            firestore.collection("users")
                .document(userId.toString())
                .collection(collection)
                .add(firestoreModel.toMap())
                .addOnSuccessListener {
                    Log.d("Firestore", "Success")
                }
                .addOnFailureListener(OnFailureListener { e ->
                    Log.d("Firestore", "Error add collection", e)
                })
            updateHist(oldLog)
            Log.d("STOP", oldLog.toString())
        }
    }

    private suspend fun getCurrentData(): UsageModel? {
        return withContext(Dispatchers.IO) {
            var log = database.getCurrent()
            Log.d("CURRENT", log.toString())
//            if (log?.endTimer != log?.startTimer) {
//                log = null
//            }

            log
        }
    }

    private suspend fun insertHist(log: UsageModel) {
        withContext(Dispatchers.IO) {
            database.insert(log)
        }
    }

    private suspend fun updateHist(log: UsageModel) {
        withContext(Dispatchers.IO) {
            database.update(log)
        }
    }

    private suspend fun clearHist() {
        withContext(Dispatchers.IO) {
            database.clearHistory()
        }
    }


}
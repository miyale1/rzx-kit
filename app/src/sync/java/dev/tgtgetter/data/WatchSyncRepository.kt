package dev.tgtgetter.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.xiaomi.xms.wearable.Wearable
import com.xiaomi.xms.wearable.auth.Permission
import org.json.JSONObject

class WatchSyncRepository(context: Context) {
    private val nodeApi = Wearable.getNodeApi(context.applicationContext)
    private val messageApi = Wearable.getMessageApi(context.applicationContext)
    private val authApi = Wearable.getAuthApi(context.applicationContext)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getConnectedDeviceName(callback: (Result<String?>) -> Unit) {
        nodeApi.connectedNodes.addOnSuccessListener { callback(Result.success(it.firstOrNull()?.name)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    fun send(tgt: String, callback: (Result<Unit>) -> Unit) {
        nodeApi.connectedNodes.addOnSuccessListener { nodes ->
            val node = nodes.firstOrNull() ?: run {
                callback(Result.failure(IllegalStateException("未发现已连接的手表")))
                return@addOnSuccessListener
            }
            nodeApi.isWearAppInstalled(node.id).addOnSuccessListener { installed ->
                if (!installed) {
                    callback(Result.failure(IllegalStateException("手表上未安装真智学")))
                    return@addOnSuccessListener
                }
                nodeApi.launchWearApp(node.id, "/home").addOnSuccessListener {
                    mainHandler.postDelayed({ authorizeAndSend(node.id, tgt, callback) }, APP_START_DELAY_MS)
                }.addOnFailureListener { callback(Result.failure(it)) }
            }.addOnFailureListener { callback(Result.failure(it)) }
        }.addOnFailureListener { callback(Result.failure(it)) }
    }

    private fun authorizeAndSend(nodeId: String, tgt: String, callback: (Result<Unit>) -> Unit) {
        authApi.checkPermission(nodeId, Permission.DEVICE_MANAGER).addOnSuccessListener { granted ->
            if (granted) sendMessage(nodeId, tgt, callback) else {
                authApi.requestPermission(nodeId, Permission.DEVICE_MANAGER).addOnSuccessListener { permissions ->
                    if (permissions.any { it.name == Permission.DEVICE_MANAGER.name }) sendMessage(nodeId, tgt, callback)
                    else callback(Result.failure(SecurityException("未授予设备管理权限")))
                }.addOnFailureListener { callback(Result.failure(it)) }
            }
        }.addOnFailureListener { callback(Result.failure(it)) }
    }

    private fun sendMessage(nodeId: String, tgt: String, callback: (Result<Unit>) -> Unit) {
        val payload = JSONObject().put("type", "zhixue_credentials").put("version", 1).put("tgt", tgt).toString()
        messageApi.sendMessage(nodeId, payload.toByteArray(Charsets.UTF_8))
            .addOnSuccessListener { callback(Result.success(Unit)) }
            .addOnFailureListener { callback(Result.failure(it)) }
    }

    private companion object { const val APP_START_DELAY_MS = 1_500L }
}

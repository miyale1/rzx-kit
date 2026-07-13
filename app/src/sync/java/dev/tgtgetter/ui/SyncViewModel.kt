package dev.tgtgetter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.tgtgetter.data.WatchSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SyncUiState(
    val tgt: String = "",
    val syncing: Boolean = false,
    val message: String? = null,
    val deviceName: String = "正在查询…",
    val deviceStatus: String = "正在检查连接状态",
    val connected: Boolean = false,
)

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WatchSyncRepository(application)
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState = _uiState.asStateFlow()

    init { refreshConnectedDevice() }

    fun setTgt(value: String) = _uiState.update { it.copy(tgt = value.trim(), message = null) }

    fun refreshConnectedDevice() {
        repository.getConnectedDeviceName { result ->
            _uiState.update {
                val name = result.getOrNull()
                it.copy(
                    connected = name != null,
                    deviceName = name ?: "暂无设备",
                    deviceStatus = when {
                        result.isFailure -> "查询失败：${result.exceptionOrNull()?.message ?: "通信服务异常"}"
                        name == null -> "未连接"
                        else -> "已连接"
                    },
                )
            }
        }
    }

    fun sync() {
        val tgt = _uiState.value.tgt
        if (!tgt.startsWith("TGT-") || _uiState.value.syncing) {
            if (!tgt.startsWith("TGT-")) _uiState.update { it.copy(message = "请输入有效的 TGT") }
            return
        }
        _uiState.update { it.copy(syncing = true, message = "正在启动手表应用…") }
        refreshConnectedDevice()
        repository.send(tgt) { result ->
            _uiState.update {
                it.copy(syncing = false, message = result.fold(
                    onSuccess = { "TGT 已同步到手表" },
                    onFailure = { error -> "同步失败：${error.message ?: "通信服务异常"}" },
                ))
            }
        }
    }
}

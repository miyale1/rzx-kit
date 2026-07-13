package dev.tgtgetter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tgtgetter.data.TgtRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TgtUiState(
    val loading: Boolean = false,
    val rootGranted: Boolean? = null,
    val appInstalled: Boolean? = null,
    val dataFileExists: Boolean? = null,
    val tgt: String? = null,
    val message: String = "点击下方按钮开始获取",
)

class TgtViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TgtRepository(application)
    private val _uiState = MutableStateFlow(TgtUiState())
    val uiState = _uiState.asStateFlow()

    fun fetch() {
        if (_uiState.value.loading) return
        _uiState.value = TgtUiState(loading = true, message = "正在申请 root 权限并读取数据…")
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching(repository::getTgt).getOrElse {
                _uiState.value = TgtUiState(message = "读取失败：${it.message ?: "root 服务异常"}")
                return@launch
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    rootGranted = result.rootGranted,
                    appInstalled = result.appInstalled,
                    dataFileExists = result.dataFileExists,
                    tgt = result.tgt,
                    message = result.error ?: "登录凭证获取成功",
                )
            }
        }
    }
}

package dev.tgtgetter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tgtgetter.ui.TgtUiState
import dev.tgtgetter.ui.TgtViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiuixTheme { TgtScreen() } }
    }
}

@Composable
private fun TgtScreen(viewModel: TgtViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Surface(color = Color(0xfff7f7f9), modifier = Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("真智学 Getter", modifier = Modifier.fillMaxWidth(), color = Color(0xff151515), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("本地登录凭证读取器", modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = Color(0xff77777d), fontSize = 15.sp)
            Spacer(Modifier.height(48.dp))
            Button(viewModel::fetch, Modifier.fillMaxWidth().height(56.dp), enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(color = Color(0xff3482ff))) {
                Text(if (state.loading) "正在获取…" else "获取登录凭证", fontSize = 17.sp, color = Color.White)
            }
            Spacer(Modifier.height(18.dp))
            ResultCard(state)
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("登录凭证", state.tgt))
                Toast.makeText(context, "登录凭证已复制", Toast.LENGTH_SHORT).show()
            }, enabled = state.tgt != null, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("复制登录凭证", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun ResultCard(state: TgtUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("检测结果", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            StatusRow("Root 权限是否可用", state.rootGranted)
            StatusRow("智学网应用是否安装", state.appInstalled)
            StatusRow("登录凭证数据是否存在", state.dataFileExists)
            Spacer(Modifier.height(12.dp))
            Text(state.message, color = if (state.tgt != null) Color(0xff18a058) else Color(0xff77777d), fontSize = 14.sp)
            state.tgt?.let {
                Spacer(Modifier.height(12.dp)); Text("登录凭证", color = Color(0xff77777d), fontSize = 13.sp); Spacer(Modifier.height(6.dp))
                Text(it, modifier = Modifier.fillMaxWidth().background(Color(0xffeeeef2), RoundedCornerShape(12.dp)).padding(14.dp),
                    color = Color(0xff202124), fontSize = 14.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: Boolean?) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xff333338), fontSize = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(when (value) { true -> Color(0xff18a058); false -> Color(0xffe64545); null -> Color(0xffb5b5ba) }, CircleShape))
            Text(when (value) { true -> "通过"; false -> "失败"; null -> "待检测" }, modifier = Modifier.padding(start = 8.dp), color = Color(0xff77777d), fontSize = 14.sp)
        }
    }
}

package dev.tgtgetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tgtgetter.ui.SyncViewModel
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiuixTheme { SyncScreen() } }
    }
}

@Composable
private fun SyncScreen(viewModel: SyncViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Surface(color = Color(0xfff7f7f9), modifier = Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text("真智学同步器", modifier = Modifier.fillMaxWidth(), color = Color(0xff151515), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("将登录凭证同步到手表端真智学", modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = Color(0xff77777d), fontSize = 15.sp)
            Spacer(Modifier.height(28.dp))
            Card(onClick = viewModel::refreshConnectedDevice, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("当前设备", color = Color(0xff77777d), fontSize = 13.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(state.deviceName, color = Color(0xff202124), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            Modifier.size(9.dp).background(
                                if (state.connected) Color(0xff3482ff) else Color(0xffb5b5ba),
                                RoundedCornerShape(5.dp),
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(state.deviceStatus, color = if (state.connected) Color(0xff3482ff) else Color(0xff77777d), fontSize = 13.sp)
                    }
                }
            }
            Text("点击卡片可刷新设备状态", modifier = Modifier.fillMaxWidth().padding(top = 7.dp), color = Color(0xff99999f), fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))
            Text("登录凭证", color = Color(0xff55555b), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = state.tgt,
                onValueChange = viewModel::setTgt,
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xff202124), fontSize = 15.sp),
                decorationBox = { inner -> Box { if (state.tgt.isEmpty()) Text("粘贴以 TGT- 开头的凭据…", color = Color(0xffaaaaaf), fontSize = 15.sp); inner() } },
            )
            Spacer(Modifier.height(18.dp))
            Button(viewModel::sync, Modifier.fillMaxWidth().height(56.dp), enabled = state.tgt.startsWith("TGT-") && !state.syncing,
                colors = ButtonDefaults.buttonColors(color = Color(0xff3482ff))) {
                Text(if (state.syncing) "正在同步…" else "同步到手表", color = Color.White, fontSize = 17.sp)
            }
            state.message?.let {
                Text(it, Modifier.fillMaxWidth().padding(top = 12.dp),
                    color = if (it.startsWith("同步失败") || it.startsWith("请输入")) Color(0xffe64545) else Color(0xff3482ff), fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("同步器不会读取智学网数据，请先使用真智学 Getter 取得并复制登录凭证。", color = Color(0xff88888e), fontSize = 13.sp)
            Text("同步过程不会联网，您的数据将通过蓝牙发送至手表，无需担心数据安全。", color = Color(0xff88888e), fontSize = 13.sp)
        }
    }
}

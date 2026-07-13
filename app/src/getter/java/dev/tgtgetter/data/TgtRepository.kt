package dev.tgtgetter.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.concurrent.TimeUnit

data class TgtResult(
    val rootGranted: Boolean? = null,
    val appInstalled: Boolean? = null,
    val dataFileExists: Boolean? = null,
    val tgt: String? = null,
    val error: String? = null,
)

class TgtRepository(private val context: Context) {
    fun getTgt(): TgtResult {
        val root = runRoot("id")
        if (!root.success || !root.output.contains("uid=0")) {
            return TgtResult(error = "未获得 root 权限，请在 root 管理器中授权")
        }

        if (!isZhixueInstalled()) {
            return TgtResult(rootGranted = true, error = "未安装智学网")
        }

        val file = runRoot("cat '$PREFS_FILE'", retryInMountMaster = true)
        if (!file.success) {
            return TgtResult(
                rootGranted = true,
                appInstalled = true,
                dataFileExists = false,
                error = "未找到或无法读取智学网登录数据${file.errorSuffix()}",
            )
        }
        if (file.output.isEmpty()) {
            return TgtResult(
                rootGranted = true,
                appInstalled = true,
                dataFileExists = true,
                error = "读取智学网登录数据失败${file.errorSuffix()}",
            )
        }

        val tgt = runCatching { parseTgt(file.output) }.getOrElse {
            return TgtResult(
                rootGranted = true,
                appInstalled = true,
                dataFileExists = true,
                error = "登录数据解析失败：${it.message ?: "数据格式异常"}",
            )
        }

        return TgtResult(
            rootGranted = true,
            appInstalled = true,
            dataFileExists = true,
            tgt = tgt,
            error = if (tgt == null) "解析成功，但未找到 TGT，请确认账号已登录" else null,
        )
    }

    private fun isZhixueInstalled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(ZHIXUE_PACKAGE, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(ZHIXUE_PACKAGE, 0)
        }
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun runRoot(command: String, retryInMountMaster: Boolean = false): RootResult {
        val regular = runSu(command, mountMaster = false)
        if (regular.success || !retryInMountMaster) return regular
        val mountMaster = runSu(command, mountMaster = true)
        return if (mountMaster.success) mountMaster else regular.withFallbackError(mountMaster)
    }

    private fun runSu(command: String, mountMaster: Boolean): RootResult {
        val arguments = buildList {
            add("su")
            if (mountMaster) add("-M")
            add("-c")
            add(command)
        }
        val process = runCatching { ProcessBuilder(arguments).redirectErrorStream(false).start() }
            .getOrElse { return RootResult(false, "", it.message.orEmpty()) }
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val outThread = Thread { process.inputStream.use { it.copyTo(stdout) } }.apply { start() }
        val errThread = Thread { process.errorStream.use { it.copyTo(stderr) } }.apply { start() }
        val finished = process.waitFor(ROOT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        val exitCode = if (finished) process.exitValue() else null
        if (!finished) {
            process.destroy()
            if (!process.waitFor(500, TimeUnit.MILLISECONDS)) process.destroyForcibly()
        }
        outThread.join(1_000)
        errThread.join(1_000)
        return RootResult(
            success = exitCode == 0,
            output = stdout.toString(Charsets.UTF_8.name()),
            error = if (finished) stderr.toString(Charsets.UTF_8.name()).trim()
                else "root 授权超时，请检查超级用户管理器是否允许本应用获取权限",
        )
    }

    companion object {
        const val ZHIXUE_PACKAGE = "com.iflytek.elpmobile.smartlearning"
        const val PREFS_FILE = "/data/user/0/$ZHIXUE_PACKAGE/shared_prefs/EDUserCenter.xml"
        private const val ROOT_TIMEOUT_SECONDS = 10L

        fun parseTgt(xml: String): String? {
            val value = Regex(
                "<string\\s+name=[\"']Key_EDUserNew[\"']>(.*?)</string>",
                setOf(RegexOption.DOT_MATCHES_ALL),
            ).find(xml.replace("&#10;", ""))?.groupValues?.get(1) ?: error("未找到 Key_EDUserNew")
            val data = Base64.getDecoder().decode(value.filterNot(Char::isWhitespace))
            var index = 0
            while (index <= data.size - 3) {
                if (data[index].toInt() and 0xff == 0x74) {
                    val length = ((data[index + 1].toInt() and 0xff) shl 8) or (data[index + 2].toInt() and 0xff)
                    if (length in 3..500 && index + 3 + length <= data.size) {
                        val candidate = data.copyOfRange(index + 3, index + 3 + length)
                            .filter { it.toInt() in 32..126 }.toByteArray().toString(Charsets.US_ASCII)
                        if (candidate.startsWith("TGT-")) return candidate
                        index += 3 + length
                        continue
                    }
                }
                index++
            }
            return null
        }
    }
}

private data class RootResult(val success: Boolean, val output: String, val error: String) {
    fun errorSuffix() = error.takeIf(String::isNotBlank)?.let { "：$it" }.orEmpty()
    fun withFallbackError(fallback: RootResult): RootResult =
        copy(error = listOf(error, fallback.error).filter(String::isNotBlank).distinct().joinToString("；"))
}

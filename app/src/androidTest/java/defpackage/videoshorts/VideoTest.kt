package defpackage.videoshorts

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import com.automate123.videshorts.extension.asFlow
import com.automate123.videshorts.service.VideoWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VideoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    @ApplicationContext
    lateinit var appContext: Context

    private val instrContext = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun concatFiles() {
        with(appContext) {
            val testDir = File(cacheDir, DIRNAME)
            testDir.deleteRecursively()
            testDir.mkdirs()
            val assets = instrContext.assets
            assets.open("1VR.mp4").copyTo(FileOutputStream(File(testDir, "1.mp4")))
            assets.open("2HL.mp4").copyTo(FileOutputStream(File(testDir, "2.mp4")))
            assets.open("2VR.mp4").copyTo(FileOutputStream(File(testDir, "3.mp4")))
            assets.open("3HB.mp4").copyTo(FileOutputStream(File(testDir, "4.mp4")))
            assets.open("4HL.mp4").copyTo(FileOutputStream(File(testDir, "5.mp4")))
            assets.open("4VR.mp4").copyTo(FileOutputStream(File(testDir, "6.mp4")))
            runBlocking {
                val start = System.currentTimeMillis()
                VideoWorker.launch(applicationContext, DIRNAME)
                    .asFlow()
                    .first { it.state in finishStates }
                println("Time: ${System.currentTimeMillis() - start} ms")
            }
        }
    }

    @After
    fun release() {
    }

    companion object {

        private const val DIRNAME = "test"

        private val finishStates = arrayOf(
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED
        )
    }
}
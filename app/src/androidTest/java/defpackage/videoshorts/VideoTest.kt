package defpackage.videoshorts

import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import com.automate123.videshorts.service.VideoWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.single
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
        with(appContext) {
            val testDir = File(cacheDir, DIRNAME)
            testDir.deleteRecursively()
            testDir.mkdirs()
        }
    }

    @Test
    fun concatFiles() {
        with(appContext) {
            val testDir = File(cacheDir, DIRNAME)
            val assets = instrContext.assets
            assets.open("1.mp4").copyTo(FileOutputStream(File(testDir, "1.mp4")))
            assets.open("2.mp4").copyTo(FileOutputStream(File(testDir, "2.mp4")))
            runBlocking {
                val info = callbackFlow<WorkInfo> {
                    val observer = Observer<WorkInfo> {
                        trySend(it)
                    }
                    VideoWorker.launch(applicationContext, DIRNAME).also {
                        it.observeForever(observer)
                        awaitClose {
                            it.removeObserver(observer)
                        }
                    }
                }.flowOn(Dispatchers.Main)
                    .single()
                assert(info.state == WorkInfo.State.SUCCEEDED)
            }
        }
    }

    @After
    fun release() {
    }

    companion object {

        private const val DIRNAME = "test"
    }
}
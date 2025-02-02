package eu.kanade.tachiyomi

import android.app.Application
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import data.Categories
import data.History
import data.Mangas
import eu.kanade.data.AndroidDatabaseHandler
import eu.kanade.data.DatabaseHandler
import eu.kanade.data.dateAdapter
import eu.kanade.data.listOfLongsAdapter
import eu.kanade.data.listOfStringsAdapter
import eu.kanade.data.listOfStringsAndAdapter
import eu.kanade.tachiyomi.data.cache.ChapterCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.database.DbOpenCallback
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.library.CustomMangaManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.saver.ImageSaver
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.job.DelayedTrackingStore
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.SourceManager
import exh.eh.EHentaiUpdateHelper
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class AppModule(val app: Application) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {
        addSingleton(app)

        // This is used to allow incremental migration from Storio
        addSingletonFactory<SupportSQLiteOpenHelper> {
            val configuration = SupportSQLiteOpenHelper.Configuration.builder(app)
                .callback(DbOpenCallback())
                .name(DbOpenCallback.DATABASE_FILENAME)
                .noBackupDirectory(false)
                .build()

            if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Support database inspector in Android Studio
                FrameworkSQLiteOpenHelperFactory().create(configuration)
            } else {
                RequerySQLiteOpenHelperFactory().create(configuration)
            }
        }

        addSingletonFactory<SqlDriver> {
            AndroidSqliteDriver(openHelper = get())
        }

        addSingletonFactory {
            Database(
                driver = get(),
                historyAdapter = History.Adapter(
                    last_readAdapter = dateAdapter,
                ),
                mangasAdapter = Mangas.Adapter(
                    genreAdapter = listOfStringsAdapter,
                    // SY -->
                    filtered_scanlatorsAdapter = listOfStringsAndAdapter,
                    // SY <--
                ),
                // SY -->
                categoriesAdapter = Categories.Adapter(
                    manga_orderAdapter = listOfLongsAdapter,
                ),
                // SY <--
            )
        }

        addSingletonFactory<DatabaseHandler> { AndroidDatabaseHandler(get(), get()) }

        addSingletonFactory { Json { ignoreUnknownKeys = true } }

        addSingletonFactory { PreferencesHelper(app) }

        addSingletonFactory { DatabaseHelper(get()) }

        addSingletonFactory { ChapterCache(app) }

        addSingletonFactory { CoverCache(app) }

        addSingletonFactory { NetworkHelper(app) }

        addSingletonFactory { SourceManager(app).also { get<ExtensionManager>().init(it) } }

        addSingletonFactory { ExtensionManager(app) }

        addSingletonFactory { DownloadManager(app) }

        addSingletonFactory { TrackManager(app) }

        addSingletonFactory { DelayedTrackingStore(app) }

        addSingletonFactory { ImageSaver(app) }

        // SY -->
        addSingletonFactory { CustomMangaManager(app) }

        addSingletonFactory { EHentaiUpdateHelper(app) }
        // SY <--

        // Asynchronously init expensive components for a faster cold start
        ContextCompat.getMainExecutor(app).execute {
            get<PreferencesHelper>()

            get<NetworkHelper>()

            get<SourceManager>()

            get<Database>()

            get<DatabaseHelper>()

            get<DownloadManager>()

            // SY -->
            get<CustomMangaManager>()
            // SY <--
        }
    }
}

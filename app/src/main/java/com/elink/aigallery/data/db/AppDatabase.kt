package com.elink.aigallery.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MediaItem::class,
        ImageTag::class,
        PersonEntity::class,
        FaceEmbedding::class,
        MediaFaceAnalysis::class,
        MediaTagAnalysis::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elink_ai_gallery.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE media_items ADD COLUMN mediaStoreId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `persons` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `embedding` BLOB NOT NULL,
                        `embeddingDim` INTEGER NOT NULL,
                        `sampleCount` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_persons_name` ON `persons` (`name`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `face_embeddings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mediaId` INTEGER NOT NULL,
                        `personId` INTEGER,
                        `embedding` BLOB NOT NULL,
                        `embeddingDim` INTEGER NOT NULL,
                        `leftPos` INTEGER NOT NULL,
                        `topPos` INTEGER NOT NULL,
                        `rightPos` INTEGER NOT NULL,
                        `bottomPos` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_face_embeddings_mediaId` ON `face_embeddings` (`mediaId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_face_embeddings_personId` ON `face_embeddings` (`personId`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_face_analysis` (
                        `mediaId` INTEGER NOT NULL,
                        `hasFace` INTEGER NOT NULL,
                        `processedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`mediaId`),
                        FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_face_analysis_mediaId` ON `media_face_analysis` (`mediaId`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_tag_analysis` (
                        `mediaId` INTEGER NOT NULL,
                        `labelCount` INTEGER NOT NULL,
                        `processedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`mediaId`),
                        FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_tag_analysis_mediaId` ON `media_tag_analysis` (`mediaId`)"
                )
            }
        }
    }
}

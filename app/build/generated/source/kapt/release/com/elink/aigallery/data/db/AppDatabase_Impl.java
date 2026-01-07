package com.elink.aigallery.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MediaDao _mediaDao;

  private volatile PersonDao _personDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `media_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `path` TEXT NOT NULL, `dateTaken` INTEGER NOT NULL, `folderName` TEXT NOT NULL, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_media_items_path` ON `media_items` (`path`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_items_folderName` ON `media_items` (`folderName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `image_tags` (`mediaId` INTEGER NOT NULL, `label` TEXT NOT NULL, `confidence` REAL NOT NULL, PRIMARY KEY(`mediaId`, `label`), FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_image_tags_mediaId` ON `image_tags` (`mediaId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_image_tags_label` ON `image_tags` (`label`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `persons` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `embedding` BLOB NOT NULL, `embeddingDim` INTEGER NOT NULL, `sampleCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_persons_name` ON `persons` (`name`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `face_embeddings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mediaId` INTEGER NOT NULL, `personId` INTEGER, `embedding` BLOB NOT NULL, `embeddingDim` INTEGER NOT NULL, `leftPos` INTEGER NOT NULL, `topPos` INTEGER NOT NULL, `rightPos` INTEGER NOT NULL, `bottomPos` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`personId`) REFERENCES `persons`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_face_embeddings_mediaId` ON `face_embeddings` (`mediaId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_face_embeddings_personId` ON `face_embeddings` (`personId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `media_face_analysis` (`mediaId` INTEGER NOT NULL, `hasFace` INTEGER NOT NULL, `processedAt` INTEGER NOT NULL, PRIMARY KEY(`mediaId`), FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_face_analysis_mediaId` ON `media_face_analysis` (`mediaId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7cdf853b9b47e6131403683779d65b8a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `media_items`");
        db.execSQL("DROP TABLE IF EXISTS `image_tags`");
        db.execSQL("DROP TABLE IF EXISTS `persons`");
        db.execSQL("DROP TABLE IF EXISTS `face_embeddings`");
        db.execSQL("DROP TABLE IF EXISTS `media_face_analysis`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMediaItems = new HashMap<String, TableInfo.Column>(6);
        _columnsMediaItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("path", new TableInfo.Column("path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("dateTaken", new TableInfo.Column("dateTaken", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("folderName", new TableInfo.Column("folderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("width", new TableInfo.Column("width", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("height", new TableInfo.Column("height", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMediaItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMediaItems = new HashSet<TableInfo.Index>(2);
        _indicesMediaItems.add(new TableInfo.Index("index_media_items_path", true, Arrays.asList("path"), Arrays.asList("ASC")));
        _indicesMediaItems.add(new TableInfo.Index("index_media_items_folderName", false, Arrays.asList("folderName"), Arrays.asList("ASC")));
        final TableInfo _infoMediaItems = new TableInfo("media_items", _columnsMediaItems, _foreignKeysMediaItems, _indicesMediaItems);
        final TableInfo _existingMediaItems = TableInfo.read(db, "media_items");
        if (!_infoMediaItems.equals(_existingMediaItems)) {
          return new RoomOpenHelper.ValidationResult(false, "media_items(com.elink.aigallery.data.db.MediaItem).\n"
                  + " Expected:\n" + _infoMediaItems + "\n"
                  + " Found:\n" + _existingMediaItems);
        }
        final HashMap<String, TableInfo.Column> _columnsImageTags = new HashMap<String, TableInfo.Column>(3);
        _columnsImageTags.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageTags.put("label", new TableInfo.Column("label", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImageTags.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImageTags = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysImageTags.add(new TableInfo.ForeignKey("media_items", "CASCADE", "NO ACTION", Arrays.asList("mediaId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesImageTags = new HashSet<TableInfo.Index>(2);
        _indicesImageTags.add(new TableInfo.Index("index_image_tags_mediaId", false, Arrays.asList("mediaId"), Arrays.asList("ASC")));
        _indicesImageTags.add(new TableInfo.Index("index_image_tags_label", false, Arrays.asList("label"), Arrays.asList("ASC")));
        final TableInfo _infoImageTags = new TableInfo("image_tags", _columnsImageTags, _foreignKeysImageTags, _indicesImageTags);
        final TableInfo _existingImageTags = TableInfo.read(db, "image_tags");
        if (!_infoImageTags.equals(_existingImageTags)) {
          return new RoomOpenHelper.ValidationResult(false, "image_tags(com.elink.aigallery.data.db.ImageTag).\n"
                  + " Expected:\n" + _infoImageTags + "\n"
                  + " Found:\n" + _existingImageTags);
        }
        final HashMap<String, TableInfo.Column> _columnsPersons = new HashMap<String, TableInfo.Column>(6);
        _columnsPersons.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("embedding", new TableInfo.Column("embedding", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("embeddingDim", new TableInfo.Column("embeddingDim", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("sampleCount", new TableInfo.Column("sampleCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPersons.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPersons = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPersons = new HashSet<TableInfo.Index>(1);
        _indicesPersons.add(new TableInfo.Index("index_persons_name", false, Arrays.asList("name"), Arrays.asList("ASC")));
        final TableInfo _infoPersons = new TableInfo("persons", _columnsPersons, _foreignKeysPersons, _indicesPersons);
        final TableInfo _existingPersons = TableInfo.read(db, "persons");
        if (!_infoPersons.equals(_existingPersons)) {
          return new RoomOpenHelper.ValidationResult(false, "persons(com.elink.aigallery.data.db.PersonEntity).\n"
                  + " Expected:\n" + _infoPersons + "\n"
                  + " Found:\n" + _existingPersons);
        }
        final HashMap<String, TableInfo.Column> _columnsFaceEmbeddings = new HashMap<String, TableInfo.Column>(10);
        _columnsFaceEmbeddings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("personId", new TableInfo.Column("personId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("embedding", new TableInfo.Column("embedding", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("embeddingDim", new TableInfo.Column("embeddingDim", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("leftPos", new TableInfo.Column("leftPos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("topPos", new TableInfo.Column("topPos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("rightPos", new TableInfo.Column("rightPos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("bottomPos", new TableInfo.Column("bottomPos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFaceEmbeddings = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysFaceEmbeddings.add(new TableInfo.ForeignKey("media_items", "CASCADE", "NO ACTION", Arrays.asList("mediaId"), Arrays.asList("id")));
        _foreignKeysFaceEmbeddings.add(new TableInfo.ForeignKey("persons", "SET NULL", "NO ACTION", Arrays.asList("personId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesFaceEmbeddings = new HashSet<TableInfo.Index>(2);
        _indicesFaceEmbeddings.add(new TableInfo.Index("index_face_embeddings_mediaId", false, Arrays.asList("mediaId"), Arrays.asList("ASC")));
        _indicesFaceEmbeddings.add(new TableInfo.Index("index_face_embeddings_personId", false, Arrays.asList("personId"), Arrays.asList("ASC")));
        final TableInfo _infoFaceEmbeddings = new TableInfo("face_embeddings", _columnsFaceEmbeddings, _foreignKeysFaceEmbeddings, _indicesFaceEmbeddings);
        final TableInfo _existingFaceEmbeddings = TableInfo.read(db, "face_embeddings");
        if (!_infoFaceEmbeddings.equals(_existingFaceEmbeddings)) {
          return new RoomOpenHelper.ValidationResult(false, "face_embeddings(com.elink.aigallery.data.db.FaceEmbedding).\n"
                  + " Expected:\n" + _infoFaceEmbeddings + "\n"
                  + " Found:\n" + _existingFaceEmbeddings);
        }
        final HashMap<String, TableInfo.Column> _columnsMediaFaceAnalysis = new HashMap<String, TableInfo.Column>(3);
        _columnsMediaFaceAnalysis.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFaceAnalysis.put("hasFace", new TableInfo.Column("hasFace", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFaceAnalysis.put("processedAt", new TableInfo.Column("processedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMediaFaceAnalysis = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMediaFaceAnalysis.add(new TableInfo.ForeignKey("media_items", "CASCADE", "NO ACTION", Arrays.asList("mediaId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMediaFaceAnalysis = new HashSet<TableInfo.Index>(1);
        _indicesMediaFaceAnalysis.add(new TableInfo.Index("index_media_face_analysis_mediaId", false, Arrays.asList("mediaId"), Arrays.asList("ASC")));
        final TableInfo _infoMediaFaceAnalysis = new TableInfo("media_face_analysis", _columnsMediaFaceAnalysis, _foreignKeysMediaFaceAnalysis, _indicesMediaFaceAnalysis);
        final TableInfo _existingMediaFaceAnalysis = TableInfo.read(db, "media_face_analysis");
        if (!_infoMediaFaceAnalysis.equals(_existingMediaFaceAnalysis)) {
          return new RoomOpenHelper.ValidationResult(false, "media_face_analysis(com.elink.aigallery.data.db.MediaFaceAnalysis).\n"
                  + " Expected:\n" + _infoMediaFaceAnalysis + "\n"
                  + " Found:\n" + _existingMediaFaceAnalysis);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7cdf853b9b47e6131403683779d65b8a", "6363c5e8b29ba32b8317e1dc09b3e0f7");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "media_items","image_tags","persons","face_embeddings","media_face_analysis");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `media_items`");
      _db.execSQL("DELETE FROM `image_tags`");
      _db.execSQL("DELETE FROM `persons`");
      _db.execSQL("DELETE FROM `face_embeddings`");
      _db.execSQL("DELETE FROM `media_face_analysis`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MediaDao.class, MediaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MediaDao mediaDao() {
    if (_mediaDao != null) {
      return _mediaDao;
    } else {
      synchronized(this) {
        if(_mediaDao == null) {
          _mediaDao = new MediaDao_Impl(this);
        }
        return _mediaDao;
      }
    }
  }

  @Override
  public PersonDao personDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }
}

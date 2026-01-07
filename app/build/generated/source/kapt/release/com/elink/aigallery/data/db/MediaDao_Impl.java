package com.elink.aigallery.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MediaDao_Impl implements MediaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MediaItem> __insertionAdapterOfMediaItem;

  private final EntityInsertionAdapter<ImageTag> __insertionAdapterOfImageTag;

  public MediaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMediaItem = new EntityInsertionAdapter<MediaItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `media_items` (`id`,`path`,`dateTaken`,`folderName`,`width`,`height`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getPath() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPath());
        }
        statement.bindLong(3, entity.getDateTaken());
        if (entity.getFolderName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getFolderName());
        }
        statement.bindLong(5, entity.getWidth());
        statement.bindLong(6, entity.getHeight());
      }
    };
    this.__insertionAdapterOfImageTag = new EntityInsertionAdapter<ImageTag>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `image_tags` (`mediaId`,`label`,`confidence`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageTag entity) {
        statement.bindLong(1, entity.getMediaId());
        if (entity.getLabel() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getLabel());
        }
        statement.bindDouble(3, entity.getConfidence());
      }
    };
  }

  @Override
  public Object insertMediaItems(final List<MediaItem> items,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfMediaItem.insertAndReturnIdsList(items);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertImageTags(final List<ImageTag> tags,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfImageTag.insert(tags);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MediaItem>> searchImages(final String query) {
    final String _sql = "\n"
            + "        SELECT DISTINCT m.* FROM media_items AS m\n"
            + "        INNER JOIN image_tags AS t ON m.id = t.mediaId\n"
            + "        WHERE t.label LIKE '%' || ? || '%'\n"
            + "        ORDER BY m.dateTaken DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"media_items",
        "image_tags"}, new Callable<List<MediaItem>>() {
      @Override
      @NonNull
      public List<MediaItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPath = CursorUtil.getColumnIndexOrThrow(_cursor, "path");
          final int _cursorIndexOfDateTaken = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTaken");
          final int _cursorIndexOfFolderName = CursorUtil.getColumnIndexOrThrow(_cursor, "folderName");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final List<MediaItem> _result = new ArrayList<MediaItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final long _tmpDateTaken;
            _tmpDateTaken = _cursor.getLong(_cursorIndexOfDateTaken);
            final String _tmpFolderName;
            if (_cursor.isNull(_cursorIndexOfFolderName)) {
              _tmpFolderName = null;
            } else {
              _tmpFolderName = _cursor.getString(_cursorIndexOfFolderName);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            _item = new MediaItem(_tmpId,_tmpPath,_tmpDateTaken,_tmpFolderName,_tmpWidth,_tmpHeight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FolderWithImages>> getImagesByFolder() {
    final String _sql = "\n"
            + "        SELECT DISTINCT folderName FROM media_items\n"
            + "        ORDER BY folderName ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"media_items"}, new Callable<List<FolderWithImages>>() {
      @Override
      @NonNull
      public List<FolderWithImages> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfFolderName = 0;
            final ArrayMap<String, ArrayList<MediaItem>> _collectionItems = new ArrayMap<String, ArrayList<MediaItem>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              if (_cursor.isNull(_cursorIndexOfFolderName)) {
                _tmpKey = null;
              } else {
                _tmpKey = _cursor.getString(_cursorIndexOfFolderName);
              }
              if (_tmpKey != null) {
                if (!_collectionItems.containsKey(_tmpKey)) {
                  _collectionItems.put(_tmpKey, new ArrayList<MediaItem>());
                }
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipmediaItemsAscomElinkAigalleryDataDbMediaItem(_collectionItems);
            final List<FolderWithImages> _result = new ArrayList<FolderWithImages>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final FolderWithImages _item;
              final String _tmpFolderName;
              if (_cursor.isNull(_cursorIndexOfFolderName)) {
                _tmpFolderName = null;
              } else {
                _tmpFolderName = _cursor.getString(_cursorIndexOfFolderName);
              }
              final ArrayList<MediaItem> _tmpItemsCollection;
              final String _tmpKey_1;
              if (_cursor.isNull(_cursorIndexOfFolderName)) {
                _tmpKey_1 = null;
              } else {
                _tmpKey_1 = _cursor.getString(_cursorIndexOfFolderName);
              }
              if (_tmpKey_1 != null) {
                _tmpItemsCollection = _collectionItems.get(_tmpKey_1);
              } else {
                _tmpItemsCollection = new ArrayList<MediaItem>();
              }
              _item = new FolderWithImages(_tmpFolderName,_tmpItemsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipmediaItemsAscomElinkAigalleryDataDbMediaItem(
      @NonNull final ArrayMap<String, ArrayList<MediaItem>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipmediaItemsAscomElinkAigalleryDataDbMediaItem(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`path`,`dateTaken`,`folderName`,`width`,`height` FROM `media_items` WHERE `folderName` IN (");
    final int _inputSize = __mapKeySet == null ? 1 : __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    if (__mapKeySet == null) {
      _stmt.bindNull(_argIndex);
    } else {
      for (String _item : __mapKeySet) {
        if (_item == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _item);
        }
        _argIndex++;
      }
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "folderName");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfPath = 1;
      final int _cursorIndexOfDateTaken = 2;
      final int _cursorIndexOfFolderName = 3;
      final int _cursorIndexOfWidth = 4;
      final int _cursorIndexOfHeight = 5;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        if (_cursor.isNull(_itemKeyIndex)) {
          _tmpKey = null;
        } else {
          _tmpKey = _cursor.getString(_itemKeyIndex);
        }
        if (_tmpKey != null) {
          final ArrayList<MediaItem> _tmpRelation = _map.get(_tmpKey);
          if (_tmpRelation != null) {
            final MediaItem _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPath;
            if (_cursor.isNull(_cursorIndexOfPath)) {
              _tmpPath = null;
            } else {
              _tmpPath = _cursor.getString(_cursorIndexOfPath);
            }
            final long _tmpDateTaken;
            _tmpDateTaken = _cursor.getLong(_cursorIndexOfDateTaken);
            final String _tmpFolderName;
            if (_cursor.isNull(_cursorIndexOfFolderName)) {
              _tmpFolderName = null;
            } else {
              _tmpFolderName = _cursor.getString(_cursorIndexOfFolderName);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            _item_1 = new MediaItem(_tmpId,_tmpPath,_tmpDateTaken,_tmpFolderName,_tmpWidth,_tmpHeight);
            _tmpRelation.add(_item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }
}

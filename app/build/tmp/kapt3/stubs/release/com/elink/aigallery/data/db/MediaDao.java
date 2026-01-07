package com.elink.aigallery.data.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J\u001c\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00040\u00032\u0006\u0010\b\u001a\u00020\tH\'J\u001c\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u00042\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u00042\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\"\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00042\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001c\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00040\u00032\u0006\u0010\u0018\u001a\u00020\tH\'\u00a8\u0006\u0019"}, d2 = {"Lcom/elink/aigallery/data/db/MediaDao;", "", "getImagesByFolder", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/elink/aigallery/data/db/FolderWithImages;", "getImagesByLabel", "Lcom/elink/aigallery/data/db/MediaItem;", "label", "", "getImagesWithoutFaceAnalysis", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUntaggedImages", "insertImageTags", "", "tags", "Lcom/elink/aigallery/data/db/ImageTag;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMediaItems", "", "items", "searchImages", "query", "app_release"})
@androidx.room.Dao()
public abstract interface MediaDao {
    
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMediaItems(@org.jetbrains.annotations.NotNull()
    java.util.List<com.elink.aigallery.data.db.MediaItem> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.Long>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertImageTags(@org.jetbrains.annotations.NotNull()
    java.util.List<com.elink.aigallery.data.db.ImageTag> tags, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        SELECT DISTINCT m.* FROM media_items AS m\n        INNER JOIN image_tags AS t ON m.id = t.mediaId\n        WHERE t.label LIKE \'%\' || :query || \'%\'\n        ORDER BY m.dateTaken DESC\n        ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.MediaItem>> searchImages(@org.jetbrains.annotations.NotNull()
    java.lang.String query);
    
    @androidx.room.Transaction()
    @androidx.room.Query(value = "\n        SELECT DISTINCT folderName FROM media_items\n        ORDER BY folderName ASC\n        ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.FolderWithImages>> getImagesByFolder();
    
    @androidx.room.Query(value = "\n        SELECT m.* FROM media_items AS m\n        LEFT JOIN image_tags AS t ON m.id = t.mediaId\n        WHERE t.mediaId IS NULL\n        ORDER BY m.dateTaken DESC\n        LIMIT :limit\n        ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUntaggedImages(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.elink.aigallery.data.db.MediaItem>> $completion);
    
    @androidx.room.Query(value = "\n        SELECT m.* FROM media_items AS m\n        LEFT JOIN media_face_analysis AS f ON m.id = f.mediaId\n        WHERE f.mediaId IS NULL\n        ORDER BY m.dateTaken DESC\n        LIMIT :limit\n        ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getImagesWithoutFaceAnalysis(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.elink.aigallery.data.db.MediaItem>> $completion);
    
    @androidx.room.Query(value = "\n        SELECT DISTINCT m.* FROM media_items AS m\n        INNER JOIN image_tags AS t ON m.id = t.mediaId\n        WHERE t.label = :label\n        ORDER BY m.dateTaken DESC\n        ")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.MediaItem>> getImagesByLabel(@org.jetbrains.annotations.NotNull()
    java.lang.String label);
}
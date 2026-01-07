package com.elink.aigallery.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eJ$\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\nH\u0086@\u00a2\u0006\u0002\u0010\u0015J\u0012\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\n0\u0017J\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u00172\u0006\u0010\u001a\u001a\u00020\u0014J\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u00172\u0006\u0010\u001c\u001a\u00020\u0014J\u000e\u0010\u001d\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u001eR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/elink/aigallery/data/repository/MediaRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "contentResolver", "Landroid/content/ContentResolver;", "mediaDao", "Lcom/elink/aigallery/data/db/MediaDao;", "getUntaggedImages", "", "Lcom/elink/aigallery/data/db/MediaItem;", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertTags", "", "mediaId", "", "labels", "", "(JLjava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeFolders", "Lkotlinx/coroutines/flow/Flow;", "Lcom/elink/aigallery/data/db/FolderWithImages;", "observeImagesByLabel", "label", "searchImages", "query", "syncImages", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class MediaRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.elink.aigallery.data.db.MediaDao mediaDao = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.ContentResolver contentResolver = null;
    
    public MediaRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.FolderWithImages>> observeFolders() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncImages(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.MediaItem>> searchImages(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.elink.aigallery.data.db.MediaItem>> observeImagesByLabel(@org.jetbrains.annotations.NotNull()
    java.lang.String label) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUntaggedImages(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.elink.aigallery.data.db.MediaItem>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertTags(long mediaId, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> labels, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}
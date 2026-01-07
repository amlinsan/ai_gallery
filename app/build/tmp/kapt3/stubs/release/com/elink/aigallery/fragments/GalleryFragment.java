package com.elink.aigallery.fragments;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 52\u00020\u0001:\u00015B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0016\u001a\u00020\u000bH\u0002J\u0018\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000fH\u0002J$\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010 2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0016J\b\u0010#\u001a\u00020$H\u0016J\u001a\u0010%\u001a\u00020$2\u0006\u0010&\u001a\u00020\u001c2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0016J\u0013\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002\u00a2\u0006\u0002\u0010(J\b\u0010)\u001a\u00020$H\u0002J \u0010*\u001a\u00020$2\u0006\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0002J$\u00101\u001a\u00020$2\u0006\u00102\u001a\u00020\u000b2\b\b\u0002\u00103\u001a\u00020\u000b2\b\b\u0002\u00104\u001a\u00020\u000bH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013\u00a8\u00066"}, d2 = {"Lcom/elink/aigallery/fragments/GalleryFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/elink/aigallery/databinding/FragmentGalleryBinding;", "binding", "getBinding", "()Lcom/elink/aigallery/databinding/FragmentGalleryBinding;", "currentTab", "", "hasLoadedOnce", "", "permissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "", "viewModel", "Lcom/elink/aigallery/ui/gallery/GalleryViewModel;", "getViewModel", "()Lcom/elink/aigallery/ui/gallery/GalleryViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "hasMediaPermissions", "isGranted", "context", "Landroid/content/Context;", "permission", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "", "onViewCreated", "view", "requiredPermissions", "()[Ljava/lang/String;", "startScan", "updateAdapters", "folderAdapter", "Lcom/elink/aigallery/ui/gallery/FolderAdapter;", "mediaAdapter", "Lcom/elink/aigallery/ui/gallery/MediaItemAdapter;", "categoryAdapter", "Lcom/elink/aigallery/ui/gallery/CategoryAdapter;", "updateContentState", "hasData", "hasSearchResults", "hasCategories", "Companion", "app_release"})
public final class GalleryFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.elink.aigallery.databinding.FragmentGalleryBinding _binding;
    private boolean hasLoadedOnce = false;
    private int currentTab = 0;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> permissionLauncher = null;
    private static final int TAB_FOLDERS = 0;
    private static final int TAB_CATEGORIES = 1;
    @org.jetbrains.annotations.NotNull()
    public static final com.elink.aigallery.fragments.GalleryFragment.Companion Companion = null;
    
    public GalleryFragment() {
        super();
    }
    
    private final com.elink.aigallery.databinding.FragmentGalleryBinding getBinding() {
        return null;
    }
    
    private final com.elink.aigallery.ui.gallery.GalleryViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    private final void startScan() {
    }
    
    private final java.lang.String[] requiredPermissions() {
        return null;
    }
    
    private final boolean hasMediaPermissions() {
        return false;
    }
    
    private final boolean isGranted(android.content.Context context, java.lang.String permission) {
        return false;
    }
    
    private final void updateContentState(boolean hasData, boolean hasSearchResults, boolean hasCategories) {
    }
    
    private final void updateAdapters(com.elink.aigallery.ui.gallery.FolderAdapter folderAdapter, com.elink.aigallery.ui.gallery.MediaItemAdapter mediaAdapter, com.elink.aigallery.ui.gallery.CategoryAdapter categoryAdapter) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/elink/aigallery/fragments/GalleryFragment$Companion;", "", "()V", "TAB_CATEGORIES", "", "TAB_FOLDERS", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}
package com.elink.aigallery.fragments;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u00182\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0002\u0018\u0019B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\t\u001a\u00020\u0005H\u0016J\u001c\u0010\n\u001a\u00020\u000b2\n\u0010\f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\r\u001a\u00020\u0005H\u0016J\u001c\u0010\u000e\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0005H\u0016J\u000e\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0013\u001a\u00020\u0005J\u0016\u0010\u0014\u001a\u00020\u000b2\u000e\u0010\u0015\u001a\n\u0012\u0004\u0012\u00020\u0017\u0018\u00010\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/elink/aigallery/fragments/ClassificationResultsAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/elink/aigallery/fragments/ClassificationResultsAdapter$ViewHolder;", "()V", "adapterSize", "", "categories", "", "Lorg/tensorflow/lite/support/label/Category;", "getItemCount", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "updateAdapterSize", "size", "updateResults", "listClassifications", "", "Lorg/tensorflow/lite/task/vision/classifier/Classifications;", "Companion", "ViewHolder", "app_release"})
public final class ClassificationResultsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.elink.aigallery.fragments.ClassificationResultsAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String NO_VALUE = "--";
    @org.jetbrains.annotations.NotNull()
    private java.util.List<org.tensorflow.lite.support.label.Category> categories;
    private int adapterSize = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.elink.aigallery.fragments.ClassificationResultsAdapter.Companion Companion = null;
    
    public ClassificationResultsAdapter() {
        super();
    }
    
    public final void updateResults(@org.jetbrains.annotations.Nullable()
    java.util.List<? extends org.tensorflow.lite.task.vision.classifier.Classifications> listClassifications) {
    }
    
    public final void updateAdapterSize(int size) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.elink.aigallery.fragments.ClassificationResultsAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.elink.aigallery.fragments.ClassificationResultsAdapter.ViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/elink/aigallery/fragments/ClassificationResultsAdapter$Companion;", "", "()V", "NO_VALUE", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001f\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\u0002\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/elink/aigallery/fragments/ClassificationResultsAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/elink/aigallery/databinding/ItemClassificationResultBinding;", "(Lcom/elink/aigallery/fragments/ClassificationResultsAdapter;Lcom/elink/aigallery/databinding/ItemClassificationResultBinding;)V", "bind", "", "label", "", "score", "", "(Ljava/lang/String;Ljava/lang/Float;)V", "app_release"})
    public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.elink.aigallery.databinding.ItemClassificationResultBinding binding = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull()
        com.elink.aigallery.databinding.ItemClassificationResultBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.Nullable()
        java.lang.String label, @org.jetbrains.annotations.Nullable()
        java.lang.Float score) {
        }
    }
}
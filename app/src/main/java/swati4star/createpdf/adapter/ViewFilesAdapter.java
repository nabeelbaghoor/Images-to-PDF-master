package swati4star.createpdf.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import swati4star.createpdf.R;
import swati4star.createpdf.database.DatabaseHelper;
import swati4star.createpdf.interfaces.EmptyStateChangeListener;
import swati4star.createpdf.util.FileUtils;

//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;

/**
 * Created by swati on 9/10/15.
 * <p>
 * An adapter to view the existing PDF files
 */

public class ViewFilesAdapter extends RecyclerView.Adapter<ViewFilesAdapter.ViewFilesHolder> {

    private final Activity mActivity;
    private final EmptyStateChangeListener mEmptyStateChangeListener;
    private final FileUtils mFileUtils;
    private final DatabaseHelper mDatabaseHelper;

    private List<File> mFileList;

    public ViewFilesAdapter(Activity activity,
                            List<File> feedItems,
                            EmptyStateChangeListener emptyStateChangeListener) {
        this.mActivity = activity;
        this.mEmptyStateChangeListener = emptyStateChangeListener;
        this.mFileList = feedItems;
        mFileUtils = new FileUtils(activity);
        mDatabaseHelper = new DatabaseHelper(mActivity);
    }

    @NonNull
    @Override
    public ViewFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewFilesHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewFilesHolder holder, final int pos) {
        final int position = holder.getAdapterPosition();
        final File pdfFile = mFileList.get(position);
        holder.fileName.setText(pdfFile.getName());
        holder.ripple.setOnClickListener(view -> {
            new MaterialDialog.Builder(mActivity)
                    .title(R.string.title)
                    .items(R.array.items)
                    .itemsIds(R.array.itemIds)
                    .itemsCallback((dialog, view1, which, text)
                            -> mFileUtils.openFile(pdfFile.getPath(), FileUtils.FileType.e_PDF))
                    .show();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<File> pdfFiles) {
        mFileList = pdfFiles;
        notifyDataSetChanged();
    }

    class ViewFilesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fileRipple)
        MaterialRippleLayout ripple;
        @BindView(R.id.fileName)
        TextView fileName;

        ViewFilesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
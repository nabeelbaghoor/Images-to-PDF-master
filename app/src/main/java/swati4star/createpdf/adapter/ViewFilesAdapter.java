package swati4star.createpdf.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import swati4star.createpdf.R;
import swati4star.createpdf.database.DatabaseHelper;
import swati4star.createpdf.interfaces.EmptyStateChangeListener;
import swati4star.createpdf.interfaces.ItemSelectedListener;
import swati4star.createpdf.model.PDFFile;
import swati4star.createpdf.util.FileUtils;

import static swati4star.createpdf.util.FileUtils.getFormattedDate;

/**
 * Created by swati on 9/10/15.
 * <p>
 * An adapter to view the existing PDF files
 */

public class ViewFilesAdapter extends RecyclerView.Adapter<ViewFilesAdapter.ViewFilesHolder> {

    private final Activity mActivity;
    private final EmptyStateChangeListener mEmptyStateChangeListener;
    private final ItemSelectedListener mItemSelectedListener;
    private final ArrayList<Integer> mSelectedFiles;
    private final FileUtils mFileUtils;
    private final DatabaseHelper mDatabaseHelper;
    private final SharedPreferences mSharedPreferences;

    private List<PDFFile> mFileList;

    /**
     * Returns adapter instance
     *
     * @param activity                 the activity calling this adapter
     * @param feedItems                list containing {@link PDFFile}
     * @param emptyStateChangeListener interface for empty state change
     * @param itemSelectedListener     interface for monitoring the status of file selection
     */
    public ViewFilesAdapter(Activity activity,
                            List<PDFFile> feedItems,
                            EmptyStateChangeListener emptyStateChangeListener,
                            ItemSelectedListener itemSelectedListener) {
        this.mActivity = activity;
        this.mEmptyStateChangeListener = emptyStateChangeListener;
        this.mItemSelectedListener = itemSelectedListener;
        this.mFileList = feedItems;
        mSelectedFiles = new ArrayList<>();
        mFileUtils = new FileUtils(activity);
        mDatabaseHelper = new DatabaseHelper(mActivity);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    @NonNull
    @Override
    public ViewFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewFilesHolder(itemView, mItemSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewFilesHolder holder, final int pos) {
        final int position = holder.getAdapterPosition();
        final PDFFile pdfFile = mFileList.get(position);

        holder.fileName.setText(pdfFile.getPdfFile().getName());
//        holder.checkBox.setChecked(mSelectedFiles.contains(position));
        holder.ripple.setOnClickListener(view -> {
            new MaterialDialog.Builder(mActivity)
                    .title(R.string.title)
                    .items(R.array.items)
                    .itemsIds(R.array.itemIds)
                    .itemsCallback((dialog, view1, which, text)
                            -> performOperation(which, position, pdfFile.getPdfFile()))
                    .show();
            notifyDataSetChanged();
        });
    }

    /**
     * Performs the required option on file
     * as per user selection
     *
     * @param index    - index of operation performed
     * @param position - position of item clicked
     * @param file     - file object clicked
     */
    private void performOperation(int index, int position, File file) {
        switch (index) {
            case 0: //Open
                mFileUtils.openFile(file.getPath(), FileUtils.FileType.e_PDF);
                break;

        }
    }

    /**
     * Sets the action bar title to app name when all files have been unchecked
     */
    private void updateActionBarTitle() {
        mActivity.setTitle(R.string.app_name);
    }

    @Override
    public int getItemCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Sets pdf files
     *
     * @param pdfFiles list containing {@link PDFFile}
     */
    public void setData(List<PDFFile> pdfFiles) {
        mFileList = pdfFiles;
        notifyDataSetChanged();
    }

    class ViewFilesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fileRipple)
        MaterialRippleLayout ripple;
        @BindView(R.id.fileName)
        TextView fileName;
//        @BindView(R.id.checkbox)
//        CheckBox checkBox;

        ViewFilesHolder(View itemView, ItemSelectedListener itemSelectedListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
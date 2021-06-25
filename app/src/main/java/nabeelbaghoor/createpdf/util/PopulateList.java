package nabeelbaghoor.createpdf.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import nabeelbaghoor.createpdf.adapter.ViewFilesAdapter;
import nabeelbaghoor.createpdf.interfaces.EmptyStateChangeListener;

public class PopulateList extends AsyncTask<Void, Void, Void> {

    private final int mCurrentSortingIndex;
    private final EmptyStateChangeListener mEmptyStateChangeListener;
    private final DirectoryUtils mDirectoryUtils;
    private final ViewFilesAdapter mAdapter;
    private final Handler mHandler;
    @Nullable
    private final String mQuery;

    public PopulateList(ViewFilesAdapter adapter,
                        EmptyStateChangeListener emptyStateChangeListener,
                        DirectoryUtils directoryUtils, int index, @Nullable String mQuery) {
        this.mAdapter = adapter;
        mCurrentSortingIndex = index;
        mEmptyStateChangeListener = emptyStateChangeListener;
        this.mQuery = mQuery;
        mHandler = new Handler(Looper.getMainLooper());
        mDirectoryUtils = directoryUtils;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        populateListView();
        return null;
    }

    private void populateListView() {
        ArrayList<File> pdfFiles;
        if (TextUtils.isEmpty(mQuery)) {
            pdfFiles = mDirectoryUtils.getPdfFromOtherDirectories();
        } else {
            pdfFiles = mDirectoryUtils.searchPDF(mQuery);
        }
        if (pdfFiles == null)
            mHandler.post(mEmptyStateChangeListener::showNoPermissionsView);
        else if (pdfFiles.size() == 0) {
            mHandler.post(mEmptyStateChangeListener::setEmptyStateVisible);
            mHandler.post(() -> mAdapter.setData(null));
        } else {
            mHandler.post(mEmptyStateChangeListener::setEmptyStateInvisible);
            mHandler.post(mEmptyStateChangeListener::hideNoPermissionsView);
            mHandler.post(() -> mAdapter.setData(pdfFiles));
            mHandler.post(mEmptyStateChangeListener::filesPopulated);
        }
    }
}
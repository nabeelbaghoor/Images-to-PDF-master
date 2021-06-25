package swati4star.createpdf.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import swati4star.createpdf.R;
import swati4star.createpdf.adapter.ViewFilesAdapter;
import swati4star.createpdf.interfaces.EmptyStateChangeListener;
import swati4star.createpdf.util.DirectoryUtils;
import swati4star.createpdf.util.PopulateList;

public class ViewFilesFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        EmptyStateChangeListener {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 10;
    @BindView(R.id.getStarted)
    public Button getStarted;
    @BindView(R.id.filesRecyclerView)
    RecyclerView mViewFilesListRecyclerView;
    @BindView(R.id.swipe)
    SwipeRefreshLayout mSwipeView;
    @BindView(R.id.emptyStatusView)
    ConstraintLayout emptyView;
    @BindView(R.id.no_permissions_view)
    RelativeLayout noPermissionsLayout;
    private Activity mActivity;
    private ViewFilesAdapter mViewFilesAdapter;
    private DirectoryUtils mDirectoryUtils;
    private int mCurrentSortingIndex;
    private boolean mIsMergeRequired = false;
    private int mCountFiles = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_view_files, container, false);
        ButterKnife.bind(this, root);
        mViewFilesAdapter = new ViewFilesAdapter(mActivity, null, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        mViewFilesListRecyclerView.setLayoutManager(mLayoutManager);
        mViewFilesListRecyclerView.setAdapter(mViewFilesAdapter);
        mSwipeView.setOnRefreshListener(this);

        checkIfListEmpty();
        return root;
    }

    private void checkIfListEmpty() {
        onRefresh();
        final File[] files = mDirectoryUtils.getOrCreatePdfDirectory().listFiles();
        int count = 0;

        if (files == null) {
            showNoPermissionsView();
            return;
        }

        for (File file : files)
            if (!file.isDirectory()) {
                count++;
                break;
            }
        if (count == 0) {
            setEmptyStateVisible();
            mCountFiles = 0;
            updateToolbar();
        }
    }

    @Override
    public void onRefresh() {
        populatePdfList(null);
        mSwipeView.setRefreshing(false);
    }

    private void populatePdfList(@Nullable String query) {
        new PopulateList(mViewFilesAdapter, this,
                new DirectoryUtils(mActivity), mCurrentSortingIndex, query).execute();
    }

    @Override
    public void setEmptyStateVisible() {
        emptyView.setVisibility(View.VISIBLE);
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void setEmptyStateInvisible() {
        emptyView.setVisibility(View.GONE);
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void showNoPermissionsView() {
        emptyView.setVisibility(View.GONE);
        noPermissionsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoPermissionsView() {
        noPermissionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void filesPopulated() {
        //refresh everything and invalidate the menu.
        if (mIsMergeRequired) {
            mIsMergeRequired = false;
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (grantResults.length < 1) {
            return;
        }
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRefresh();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        mDirectoryUtils = new DirectoryUtils(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void updateToolbar() {
        AppCompatActivity activity = ((AppCompatActivity)
                Objects.requireNonNull(mActivity));
        ActionBar toolbar = activity.getSupportActionBar();
        if (toolbar != null) {
            mActivity.setTitle(mCountFiles == 0 ?
                    mActivity.getResources().getString(R.string.viewFiles)
                    : String.valueOf(mCountFiles));
            //When one or more than one files are selected refresh
            //ActionBar: set Merge option invisible or visible
            mIsMergeRequired = mCountFiles > 1;
            activity.invalidateOptionsMenu();
        }
    }
}
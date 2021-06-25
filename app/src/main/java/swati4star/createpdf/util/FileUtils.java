package swati4star.createpdf.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import swati4star.createpdf.R;

//import android.support.v4.content.FileProvider;

public class FileUtils {

    public static final String AUTHORITY_APP = "com.swati4star.shareFile";
    private final Activity mContext;

    public FileUtils(Activity context) {
        this.mContext = context;
    }

    // GET PDF DETAILS

    public static String getFormattedDate(File file) {
        Date lastModDate = new Date(file.lastModified());
        String[] formatDate = lastModDate.toString().split(" ");
        String time = formatDate[3];
        String[] formatTime = time.split(":");
        String date = formatTime[0] + ":" + formatTime[1];

        return formatDate[0] + ", " + formatDate[1] + " " + formatDate[2] + " at " + date;
    }

    public static String getFileNameWithoutExtension(String path) {
        if (path == null || path.lastIndexOf("/") == -1)
            return path;

        String filename = path.substring(path.lastIndexOf("/") + 1);
        filename = filename.replace(".pdf", "");

        return filename;
    }

    public void openFile(String path, FileType fileType) {
        if (path == null) {
            return;
        }
        openFileInternal(path, fileType == FileType.e_PDF ?
                mContext.getString(R.string.pdf_type) : mContext.getString(R.string.txt_type));
    }

    private void openFileInternal(String path, String dataType) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            Uri uri = FileProvider.getUriForFile(mContext, AUTHORITY_APP, file);

            target.setDataAndType(uri, dataType);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            openIntent(Intent.createChooser(target, mContext.getString(R.string.open_file)));
        } catch (Exception e) {
        }
    }

    public String getDefaultStorageLocation() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/PDFfiles/";
    }

    public boolean isFileExist(String mFileName) {
        String path = getDefaultStorageLocation() + mFileName;
        File file = new File(path);
        return file.exists();
    }

    public String getLastFileName(ArrayList<String> filesPath) {
        if (filesPath.size() == 0)
            return "";

        String lastSelectedFilePath = filesPath.get(filesPath.size() - 1);
        String nameWithoutExt = stripExtension(getFileNameWithoutExtension(lastSelectedFilePath));

        return nameWithoutExt + mContext.getString(R.string.pdf_suffix);
    }

    public String stripExtension(String fileNameWithExt) {
        // Handle null case specially.
        if (fileNameWithExt == null) return null;

        // Get position of last '.'.
        int pos = fileNameWithExt.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.
        if (pos == -1) return fileNameWithExt;

        // Otherwise return the string, up to the dot.
        return fileNameWithExt.substring(0, pos);
    }

    private void openIntent(Intent intent) {
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    public enum FileType {
        e_PDF
    }
}
package nabeelbaghoor.I2PConverterApp.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nabeelbaghoor.I2PConverterApp.R;

public class DirectoryUtils {

    private final Context mContext;
    private ArrayList<String> mFilePaths;

    public DirectoryUtils(Context context) {
        mContext = context;
    }

    ArrayList<File> searchPDF(String query) {
        ArrayList<File> searchResult = new ArrayList<>();
        final File[] files = getOrCreatePdfDirectory().listFiles();
        ArrayList<File> pdfs = searchPdfsFromPdfFolder(files);
        for (File pdf : pdfs) {
            String path = pdf.getPath();
            String[] fileName = path.split("/");
            String pdfName = fileName[fileName.length - 1].replace("pdf", "");
            if (checkChar(query, pdfName) == 1) {
                searchResult.add(pdf);
            }
        }
        return searchResult;
    }

    private int checkChar(String query, String fileName) {
        query = query.toLowerCase();
        fileName = fileName.toLowerCase();
        Set<Character> q = new HashSet<>();
        Set<Character> f = new HashSet<>();
        for (char c : query.toCharArray()) {
            q.add(c);
        }
        for (char c : fileName.toCharArray()) {
            f.add(c);
        }

        if (q.containsAll(f) || f.containsAll(q))
            return 1;

        return 0;
    }

    // RETURNING LIST OF FILES OR DIRECTORIES

    ArrayList<File> getPdfsFromPdfFolder(File[] files) {
        ArrayList<File> pdfFiles = new ArrayList<>();
        if (files == null)
            return pdfFiles;
        for (File file : files) {
            if (isPDFAndNotDirectory(file))
                pdfFiles.add(file);
        }
        return pdfFiles;
    }

    private ArrayList<File> searchPdfsFromPdfFolder(File[] files) {
        ArrayList<File> pdfFiles = getPdfsFromPdfFolder(files);
        if (files == null)
            return pdfFiles;
        for (File file : files) {
            if (file.isDirectory()) {
                for (File dirFiles : file.listFiles()) {
                    if (isPDFAndNotDirectory(dirFiles))
                        pdfFiles.add(dirFiles);
                }
            }
        }
        return pdfFiles;
    }

    private boolean isPDFAndNotDirectory(File file) {
        return !file.isDirectory() &&
                file.getName().endsWith(mContext.getString(R.string.pdf_ext));
    }


    public String getDefaultStorageLocation() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/PDFfiles/";
    }

    public File getOrCreatePdfDirectory() {
        File folder = new File(getDefaultStorageLocation());
        if (!folder.exists())
            folder.mkdir();
        return folder;
    }

    public ArrayList<File> getPdfFromOtherDirectories() {
        mFilePaths = new ArrayList<>();
        walkDir(getOrCreatePdfDirectory());
        ArrayList<File> files = new ArrayList<>();
        for (String path : mFilePaths)
            files.add(new File(path));
        return files;
    }

    private void walkDir(File dir) {
        walkDir(dir, Collections.singletonList(".pdf"));
    }

    private void walkDir(File dir, List<String> extensions) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDir(aListFile, extensions);
                } else {
                    for (String extension : extensions) {
                        if (aListFile.getName().endsWith(extension)) {
                            //Do what ever u want
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
}

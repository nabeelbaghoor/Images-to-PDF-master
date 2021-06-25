package nabeelbaghoor.createpdf.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import nabeelbaghoor.createpdf.interfaces.OnPDFCreatedInterface;

public class CreatePdf extends AsyncTask<String, String, String> {

    private final String mFileName;
    private final String mQualityString;
    private final ArrayList<String> mImagesUri;
    private final int mBorderWidth;
    private final OnPDFCreatedInterface mOnPDFCreatedInterface;
    private final String mPageSize;
    private final int mMarginTop;
    private final int mMarginBottom;
    private final int mMarginRight;
    private final int mMarginLeft;
    private final String mImageScaleType;
    private final String mPageNumStyle;
    private final int mPageColor;
    private boolean mSuccess;
    private String mPath;

    public CreatePdf(ArrayList<String> mImagesUri, String filename, String parentPath,
                     OnPDFCreatedInterface onPDFCreated) {
        this.mImagesUri = mImagesUri;
        this.mFileName = filename;
        this.mQualityString = Integer.toString(30);
        this.mOnPDFCreatedInterface = onPDFCreated;
        this.mPageSize = "DefaultPageSize"; //"A4"
        this.mBorderWidth = 0;
        this.mMarginTop = 0;
        this.mMarginBottom = 0;
        this.mMarginRight = 0;
        this.mMarginLeft = 0;
        this.mImageScaleType = "maintain_aspect_ratio";
        this.mPageNumStyle = "pref_page_number_style";
        this.mPageColor = Color.WHITE;
        mPath = parentPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mSuccess = true;
        mOnPDFCreatedInterface.onPDFCreationStarted();
    }

    private void setFilePath() {
        File folder = new File(mPath);
        if (!folder.exists())
            folder.mkdir();
        mPath = mPath + mFileName + ".pdf";
    }

    public boolean isNotEmpty(CharSequence s) {
        return s != null && !s.toString().trim().equals("");
    }

    @Override
    protected String doInBackground(String... params) {

        setFilePath();

        Log.v("stage 1", "store the pdf in sd card");

        Rectangle pageSize = new Rectangle(PageSize.getRectangle("A4"));
        pageSize.setBackgroundColor(getBaseColor(mPageColor));
        Document document = new Document(pageSize,
                mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
        Log.v("stage 2", "Document Created");
        document.setMargins(mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
        Rectangle documentRect = document.getPageSize();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mPath));

            Log.v("Stage 3", "Pdf writer");

            document.open();

            Log.v("Stage 4", "Document opened");

            for (int i = 0; i < mImagesUri.size(); i++) {
                int quality;
                quality = 30;
                if (isNotEmpty(mQualityString)) {
                    quality = Integer.parseInt(mQualityString);
                }
                Image image = Image.getInstance(mImagesUri.get(i));
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                double qualityMod = quality * 0.09;
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);

                Log.v("Stage 5", "Image compressed " + qualityMod);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mImagesUri.get(i), bmOptions);

                Log.v("Stage 6", "Image path adding");

                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);
                if (mImageScaleType.equals("maintain_aspect_ratio"))
                    image.scaleToFit(pageWidth, pageHeight);
                else
                    image.scaleAbsolute(pageWidth, pageHeight);

                image.setAbsolutePosition(
                        (documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);

                Log.v("Stage 7", "Image Alignments");
                addPageNumber(documentRect, writer);
                document.add(image);

                document.newPage();
            }

            Log.v("Stage 8", "Image adding");

            document.close();

            Log.v("Stage 7", "Document Closed" + mPath);

            Log.v("Stage 8", "Record inserted in database");

        } catch (Exception e) {
            e.printStackTrace();
            mSuccess = false;
        }

        return null;
    }

    private void addPageNumber(Rectangle documentRect, PdfWriter writer) {
        if (mPageNumStyle != null) {
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_BOTTOM,
                    getPhrase(writer, mPageNumStyle, mImagesUri.size()),
                    ((documentRect.getRight() + documentRect.getLeft()) / 2),
                    documentRect.getBottom() + 25, 0);
        }
    }

    @NonNull
    private Phrase getPhrase(PdfWriter writer, String pageNumStyle, int size) {
        Phrase phrase;
        switch (pageNumStyle) {
            case "pg_num_style_page_x_of_n":
                phrase = new Phrase(String.format("Page %d of %d", writer.getPageNumber(), size));
                break;
            case "pg_num_style_x_of_n":
                phrase = new Phrase(String.format("%d of %d", writer.getPageNumber(), size));
                break;
            default:
                phrase = new Phrase(String.format("%d", writer.getPageNumber()));
                break;
        }
        return phrase;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mOnPDFCreatedInterface.onPDFCreated(mSuccess, mPath);
    }

    private BaseColor getBaseColor(int color) {
        return new BaseColor(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }
}



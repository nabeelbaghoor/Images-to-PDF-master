package nabeelbaghoor.I2PConverterApp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    private int mId;
    @ColumnInfo(name = "file_path")
    private final String mFilePath;

    @ColumnInfo(name = "date")
    private String mDate;

    @ColumnInfo(name = "operation_type")
    private final String mOperationType;

    public History(String filePath, String date, String operationType) {
        this.mFilePath = filePath;
        this.mDate = date;
        this.mOperationType = operationType;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public String getOperationType() {
        return mOperationType;
    }
}

package nabeelbaghoor.I2PConverterApp;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface HistoryDao {
    @Insert
    void insertAll(History... histories);
}
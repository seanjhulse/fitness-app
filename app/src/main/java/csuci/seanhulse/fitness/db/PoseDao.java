package csuci.seanhulse.fitness.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PoseDao {
    @Query("SELECT * FROM pose")
    List<Pose> loadAll();

    @Insert
    void insertAll(Pose... poses);

    @Insert
    long insert(Pose pose);

    @Delete
    void delete(Pose pose);
}

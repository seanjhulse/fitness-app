package csuci.seanhulse.fitness.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.UUID;

@Dao
public interface PoseDao {
    @Query("SELECT * FROM pose")
    List<Pose> loadAll();

    @Query("SELECT * FROM pose WHERE exercise_id = :exerciseId")
    List<Pose> findAll(UUID exerciseId);

    @Insert
    void insertAll(Pose... poses);

    @Insert
    long insert(Pose pose);

    @Delete
    void delete(Pose pose);
}

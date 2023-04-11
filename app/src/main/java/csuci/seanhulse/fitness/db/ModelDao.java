package csuci.seanhulse.fitness.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.UUID;

import csuci.seanhulse.fitness.api.TaskState.Status;

@Dao
public interface ModelDao {
    @Query("SELECT * FROM model")
    List<Model> loadAll();

    @Query("SELECT * FROM model WHERE exercise_id = :exerciseId AND status = 'SUCCESS' ORDER BY datetime DESC")
    List<Model> findAll(UUID exerciseId);

    @Query("UPDATE model SET status = :status WHERE id = :id")
    void updateStatus(Status status, long id);

    @Insert
    void insertAll(Model... models);

    @Insert
    long insert(Model model);

    @Delete
    void delete(Model model);
}

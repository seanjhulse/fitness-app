package csuci.seanhulse.fitness.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    List<Exercise> loadAll();

    @Query("SELECT * FROM exercise WHERE name = :name")
    List<Exercise> findAllWithName(String name);

    @Insert
    void insertAll(Exercise... exercises);

    @Insert
    void insert(Exercise exercise);

    @Delete
    void delete(Exercise exercise);
}

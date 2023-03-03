package com.example.fitnessform.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PoseDao {
    @Query("SELECT * FROM pose")
    List<Pose> loadAll();

    @Query("SELECT * FROM pose WHERE landmark_type IN (:landmarkTypes)")
    List<Pose> loadAllPosesByType(int... landmarkTypes);

    @Insert
    void insertAll(Pose... poses);

    @Delete
    void delete(Pose pose);
}

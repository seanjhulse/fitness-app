package com.example.fitnessform.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Pose.class}, version = 0)
public abstract class PoseDatabase extends RoomDatabase {
    public abstract PoseDao poseDao();
}

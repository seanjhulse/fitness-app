package csuci.seanhulse.fitness.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import csuci.seanhulse.fitness.data.IPoseDataListener;

@Database(entities = {Pose.class}, version = 0)
public abstract class PoseDatabase extends RoomDatabase {
    public abstract PoseDao poseDao();
}

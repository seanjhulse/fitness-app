package csuci.seanhulse.fitness.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Pose.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class PoseDatabase extends RoomDatabase {
    public abstract PoseDao poseDao();
}

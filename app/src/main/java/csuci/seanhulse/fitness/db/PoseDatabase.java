package csuci.seanhulse.fitness.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Pose.class, Exercise.class, Model.class}, version = 12)
@TypeConverters({Converters.class})
public abstract class PoseDatabase extends RoomDatabase {
    public abstract PoseDao poseDao();

    public abstract ExerciseDao exerciseDao();

    public abstract ModelDao modelDao();
}

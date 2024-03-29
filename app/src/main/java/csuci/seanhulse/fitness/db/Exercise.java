package csuci.seanhulse.fitness.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity
public class Exercise {
    @PrimaryKey
    @NonNull
    private UUID id;

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "reps")
    private int reps;

    @ColumnInfo(name = "level")
    private Level level;

    @ColumnInfo(name = "sessions")
    private int sessions;

    public UUID getId() {
        return id;
    }

    public Exercise(String datetime, String name, int reps, Level level, int sessions) {
        this.id = UUID.randomUUID();
        this.datetime = datetime;
        this.name = name;
        this.reps = reps;
        this.level = level;
        this.sessions = sessions;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getName() {
        return name;
    }

    public int getReps() {
        return reps;
    }

    public Level getLevel() {
        return level;
    }

    public int getSessions() {
        return sessions;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

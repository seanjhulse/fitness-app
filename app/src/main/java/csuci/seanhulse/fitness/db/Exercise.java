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

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public Exercise(String datetime, String name, int reps) {
        this.id = UUID.randomUUID();
        this.datetime = datetime;
        this.name = name;
        this.reps = reps;
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
}

package csuci.seanhulse.fitness.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Pose {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "landmarks")
    private List<Landmark> landmarks;

    @Ignore
    public Pose() {

    }

    public Pose(List<Landmark> landmarks, String datetime) {
        this.landmarks = landmarks;
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }
}

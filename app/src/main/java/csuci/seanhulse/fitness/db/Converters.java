package csuci.seanhulse.fitness.db;


import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();
    private static final Type landmarkListType = new TypeToken<List<Landmark>>(){}.getType();

    @TypeConverter
    public static String fromLandmarksToJson(List<Landmark> landmarks) {
        return gson.toJson(landmarks);
    }

    @TypeConverter
    public static List<Landmark> jsonToLandmarks(String landmarks) {
        return gson.fromJson(landmarks, landmarkListType);
    }
}

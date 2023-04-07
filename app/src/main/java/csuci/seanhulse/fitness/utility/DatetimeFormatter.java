package csuci.seanhulse.fitness.utility;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatetimeFormatter {

    /**
     * Given a UTC ISO-8601 datetime, we format the datetime into a human readable format or - if we fail - we return
     * the original datetime.
     *
     * @param datetime the ISO-8601 UTC datetime
     * @return a human readable string or the original datetime if we failed to parse the datetime
     */
    public static String datetimeFormatter(String datetime) {
        try {
            @SuppressLint("SimpleDateFormat") final DateFormat dateTimeFormatter = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mmZ");
            Date date = dateTimeFormatter.parse(datetime);
            if (date != null) {
                SimpleDateFormat humanReadableFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm a", Locale.US);
                return humanReadableFormat.format(date);
            } else {
                throw new Exception("Failed to parse datetime");
            }

        } catch (Exception e) {
            Log.w("Workouts converting exercise datetime failed", e);
            return datetime;
        }
    }
}

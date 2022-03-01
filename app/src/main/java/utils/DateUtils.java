package utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.brimbay.chat.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String TAG = DateUtils.class.getSimpleName();

    // This class should not be initialized
    private DateUtils() {

    }
    
    /**
    * Gets timestamp in millis from date e.g(2021-12-05 19:40:26)
    * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long getMillis(String d, @Nullable String zone){
        String date = d;
        try {
            if (d.contains("T")) {
                date = d.replace("T", " ");
                if (date.contains(".") && date.contains("Z")) {
                    date = date.replace(date.substring(d.indexOf(".")), "");
                }
            }
        }catch (DateTimeParseException e){
            Log.e(TAG,e.getLocalizedMessage());
            return 0;
        }
        
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") );
        return localDateTime.atZone(zone == null?ZoneId.systemDefault():ZoneId.of(zone)).toInstant().toEpochMilli();
    }
    
    public static long getMillisOld(String d) {
        try{
            if (d.contains("T")) {
                d = d.replace("T", " ");
                if (d.contains(".") && d.contains("Z")) {
                    d = d.replace(d.substring(d.indexOf(".")), "");
                }
            }
        }catch (Exception e){
            Log.e(TAG,e.getLocalizedMessage());
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        try {
            Date date = sdf.parse(d);
            return date != null?date.getTime():0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets timestamp in millis and converts it to HH:mm (e.g. 16:44).
     */
    public static String formatTime(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static String formatTimeWithMarker(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static int getHourOfDay(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("H", Locale.getDefault());
        return Integer.parseInt(dateFormat.format(timeInMillis));
    }

    public static int getMinute(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("m", Locale.getDefault());
        return Integer.parseInt(dateFormat.format(timeInMillis));
    }

    /**
     * If the given time is of a different date, display the date.
     * If it is of the same date, display the time.
     *
     * @param timeInMillis The time to convert, in milliseconds.
     * @return The time or date.
     */
    public static String formatDateTime(long timeInMillis) {
        if (isToday(timeInMillis)) {
            return formatTime(timeInMillis);
        } else {
            return formatDate(timeInMillis);
        }
    }
    
    public  static String formatPassedTimeAndDate(Context context, long time){
        if (isToday(time)){
            return context.getString(R.string.chat_time_format,context.getString(R.string.today),formatTimeWithMarker(time));
        }else{
            return context.getString(R.string.chat_time_format,formatDateFancy(time),formatTimeWithMarker(time));
        }
    }
    
    /**
     * Formats timestamp to 'date month' format (e.g. 'February 3').
     */
    public static String formatDate(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy, MMMM dd", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }
    /**
     * Formats timestamp to 'date month' format (e.g. 'February 3').
     */
    public static String formatDateFancy(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    /**
     * Returns whether the given date is today, based on the user's current locale.
     */
    public static boolean isToday(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = dateFormat.format(timeInMillis);
        return date.equals(dateFormat.format(System.currentTimeMillis()));
    }

    public static long getTimePassed(long timeMillis) {
        if (timeMillis == 0) return 0;

        long millis = System.currentTimeMillis() - timeMillis;

        return millis / 1000;
    }

    public static String getPassedTime(long timeInMillis) {
        if (timeInMillis == 0) {
            return "";
        }

        long millis = System.currentTimeMillis() - timeInMillis;
        long sec = millis / 1000;
        long minutes = sec / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = weeks / 4;
        long years = months / 12;

        if (sec < 60) {
            return "just now";
        } else if (minutes >= 1 && minutes < 60) {
            return minutes + " mins ago";
        } else if (hours >= 1 && hours < 24) {
            return hours + " hour"+ (hours > 1?"s ": " ")+"ago";
        } else if (days >= 1 && days < 7) {
            return days + " day"+ (days > 1?"s ": " ")+"ago";
        } else if (weeks >= 1 && weeks < 4) {
            return weeks + " week"+ (weeks > 1?"s ": " ")+ "ago";
        } else if (months >= 1 && months < 12) {
            return months + " month"+ (months > 1?"s ": " ")+"ago";
        } else if (years >= 1) {
            return years + " year"+ (years > 1?"s ": " ")+"ago";
        } else {
            return "";
        }
    }

    /**
     * Checks if two dates are of the same day.
     *
     * @param millisFirst  The time in milliseconds of the first date.
     * @param millisSecond The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     */
    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }
    
}

package com.fareed.bioMini;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Date time = Calendar.getInstance().getTime();
        return dateFormat.format(time);
    }
}

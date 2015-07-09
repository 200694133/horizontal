package hyn.com.lib;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hanyanan on 2015/3/3.
 */
public class TimeUtils {
    public static SimpleDateFormat sTimeFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");
    public static long getCurrentWallClockTime(){
        //TODO
        return System.currentTimeMillis();
    }

    public static String getLogTime(){
        long currentTime = System.currentTimeMillis();
        Date nowTime = new Date(currentTime);
        String retStrFormatNowDate = sTimeFormatter.format(nowTime);
        return retStrFormatNowDate+" "+currentTime%1000;
    }
}

package com.hamitao.kids.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.hamitao.framework.utils.Logger;
import com.hamitao.kids.constant.Constants;
import com.hamitao.requestframe.entity.RequestCourseScheduleInfo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * @data on 2018/4/16 18:42
 * @describe: 设置闹钟
 */
@SuppressLint("WrongConstant")
public class AlarmManagerUtil {
    private static String TAG = "AlarmManagerUtil";

    public static final String ALARM_ACTION = "com.alarm.clock.action";//闹钟
    public static final String COURSES_CHEDULE_ACTION = "com.course.schedule.action";//课程表


    public static void setAlarmTime(Context context, long timeInMillis, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, intent.getIntExtra("id", 0),
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        int interval = (int) intent.getLongExtra("intervalMillis", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setWindow(AlarmManager.RTC_WAKEUP, timeInMillis, interval, sender);
        }
    }

    /**
     * 关闭闹钟
     *
     * @param context
     * @param action
     * @param id
     */
    public static void cancelAlarm(Context context, String action, int id) {
        Intent intent = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * @param flag            周期性时间间隔的标志,flag = 0 表示一次性的闹钟, flag = 1 表示每天提醒的闹钟(1天的时间间隔),flag = 2
     *                        表示按周每周提醒的闹钟（一周的周期性时间间隔）
     * @param hour            时
     * @param minute          分
     * @param idx              闹钟的id
     * @param week            week=0表示一次性闹钟或者按天的周期性闹钟，非0 的情况下是几就代表以周为周期性的周几的闹钟
     * @param tips            闹钟提示信息
     * @param soundOrVibrator 2表示声音和震动都执行，1表示只有铃声提醒，0表示只有震动提醒
     * @param id             服务器上闹钟的id
     */
    public static void setAlarm(Context context, int flag, int hour, int minute, int idx, int
            week, String tips, int soundOrVibrator,String id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long intervalMillis = 0;
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get
                (Calendar.DAY_OF_MONTH), hour, minute, 10);
        if (flag == 0) {
            intervalMillis = 0;
        } else if (flag == 1) {
            intervalMillis = 24 * 3600 * 1000;
        } else if (flag == 2) {
            intervalMillis = 24 * 3600 * 1000 * 7;
        }
        Intent intent = new Intent(ALARM_ACTION);
        intent.putExtra(Constants.INTERVALMILLIS, intervalMillis);
        intent.putExtra(Constants.ALARM_MSG, tips);
        intent.putExtra(Constants.ALARM_ID, idx);
        intent.putExtra(Constants.ALARM_SERVICE_ID, id);
        intent.putExtra(Constants.SOUNDORVIBRATOR, soundOrVibrator);
        Logger.d(TAG, "intervalMillis=" + intervalMillis + "  tips=" + tips + "  idx=" + idx + "  soundOrVibrator=" + soundOrVibrator);
        intent.putExtra(Constants.ALARM_TIME, hour + ":" + minute);
        PendingIntent sender = PendingIntent.getBroadcast(context, idx, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setWindow(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis()),
                    0, sender);
        } else {
            if (flag == 0) {
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            } else {
                am.setRepeating(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis
                        ()), intervalMillis, sender);
            }
        }
    }

    //设置课程表
    public static void setCourseSchedule(Context context, int flag, int hour, int minute, int id, int
            week,  List<RequestCourseScheduleInfo> requestCourseScheduleInfos) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long intervalMillis = 0;
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get
                (Calendar.DAY_OF_MONTH), hour, minute, 10);
        if (flag == 0) {
            intervalMillis = 0;
        } else if (flag == 1) {
            intervalMillis = 24 * 3600 * 1000;
        } else if (flag == 2) {
            intervalMillis = 24 * 3600 * 1000 * 7;
        }
        Intent intent = new Intent(COURSES_CHEDULE_ACTION);
        intent.putExtra(Constants.INTERVALMILLIS, intervalMillis);
        intent.putExtra(Constants.COURSES_CHEDULE_INFO_DATA, (Serializable) requestCourseScheduleInfos);

        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setWindow(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis()),
                    0, sender);
        } else {
            if (flag == 0) {
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            } else {
                am.setRepeating(AlarmManager.RTC_WAKEUP, calMethod(week, calendar.getTimeInMillis
                        ()), intervalMillis, sender);
            }
        }

    }


    /**
     * @param weekflag 传入的是周几
     * @param dateTime 传入的是时间戳（设置当天的年月日+从选择框拿来的时分秒）
     * @return 返回起始闹钟时间的时间戳
     */
    private static long calMethod(int weekflag, long dateTime) {
        long time = 0;
        //weekflag == 0表示是按天为周期性的时间间隔或者是一次行的，weekfalg非0时表示每周几的闹钟并以周为时间间隔
        if (weekflag != 0) {
            Calendar c = Calendar.getInstance();
            int week = c.get(Calendar.DAY_OF_WEEK);
            if (1 == week) {
                week = 7;
            } else if (2 == week) {
                week = 1;
            } else if (3 == week) {
                week = 2;
            } else if (4 == week) {
                week = 3;
            } else if (5 == week) {
                week = 4;
            } else if (6 == week) {
                week = 5;
            } else if (7 == week) {
                week = 6;
            }

            if (weekflag == week) {
                if (dateTime > System.currentTimeMillis()) {
                    time = dateTime;
                } else {
                    time = dateTime + 7 * 24 * 3600 * 1000;
                }
            } else if (weekflag > week) {
                time = dateTime + (weekflag - week) * 24 * 3600 * 1000;
            } else if (weekflag < week) {
                time = dateTime + (weekflag - week + 7) * 24 * 3600 * 1000;
            }
        } else {
            if (dateTime > System.currentTimeMillis()) {
                time = dateTime;
            } else {
                time = dateTime + 24 * 3600 * 1000;
            }
        }
        return time;
    }


    /**
     * 获取今天是星期几
     */
    public static String getToday(){
        String day_of_weekString = "";
        Calendar cal = Calendar.getInstance();
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                day_of_weekString = "SUN";
                break;
            case 2:
                day_of_weekString = "MON";
                break;
            case 3:
                day_of_weekString = "TUE";
                break;
            case 4:
                day_of_weekString = "WED";
                break;
            case 5:
                day_of_weekString = "THU";
                break;
            case 6:
                day_of_weekString = "FRI";
                break;
            case 7:
                day_of_weekString = "SAT";
                break;
        }
        return day_of_weekString;
    }


}

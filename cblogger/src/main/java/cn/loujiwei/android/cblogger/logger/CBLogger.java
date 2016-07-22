package cn.loujiwei.android.cblogger.logger;

import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import cn.loujiwei.android.cblogger.utils.FileUtils;
import cn.loujiwei.android.cblogger.utils.ThreadManager;


/**
 * Created by LJW on 16/7/18.
 */
public class CBLogger {

    public final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public final static String fileName = format.format(new Date()) + ".log";

    private static int maxLength = 10;
    private static int deleteDay = 7;   //自动删除几天前的log
    static StringBuffer _stringBuffer = new StringBuffer(maxLength);

    private static String logPath = Environment.getExternalStorageDirectory()
            + "/cestbon/baseData/log/";


    public static void d(String d) {
        System.out.println(":" + d);
        _stringBuffer.append(d);

        System.out.println("stringBuffer = " + _stringBuffer.toString());

        testWriteBufferToDisk();
    }

    /**
     * 设置最大缓存大小
     *
     * @param length
     */
    public static void setMaxLength(int length) {
        CBLogger.maxLength = length;
        CBLogger._stringBuffer = new StringBuffer(length);
    }


    /**
     * 设置log的存放路径
     *
     * @param path
     */
    public static void setLogPath(String path) {
        logPath = path;
    }


    public static synchronized void testWriteBufferToDisk() {

        System.out.println("长度----->" + _stringBuffer.length());
        if (_stringBuffer.length() > maxLength) {
            System.out.println("超了 " + _stringBuffer.length());
            final String cache = _stringBuffer.toString();

            _stringBuffer.delete(0, _stringBuffer.length());

            Future<String> future = ThreadManager.getFixedThreadPool().submit(new Callable<String>() {
                @Override
                public String call() throws Exception {

                    if (FileUtils.getSDAvailableSize() < 10) {
                        FileUtils.deleteLogFolderBeforeDays(logPath, deleteDay);
                    }
                    return FileUtils.fileWrite(logPath, fileName, cache);

                }
            });
        }
    }


    /**
     * 获得log文件夹的大小
     *
     * @return
     */
    public static String getLogFolderSize() {
        return FileUtils.getAutoFileOrFilesSize(logPath);
    }

    /**
     * 删除log文件夹
     *
     * @return
     */
    public static boolean deleteLogFolder() {
        return FileUtils.deleteDirectory(logPath);
    }

    /**
     * 删除day天前的照片
     *
     * @param day
     * @return
     */
    public static boolean deleteLogFolderByDay(int day) {
        return FileUtils.deleteLogFolderBeforeDays(logPath, day);
    }

    public static void setDeleteDay(int deleteDay) {
        CBLogger.deleteDay = deleteDay;
    }
}

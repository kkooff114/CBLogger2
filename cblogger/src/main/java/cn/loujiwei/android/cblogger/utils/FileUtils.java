package cn.loujiwei.android.cblogger.utils;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by LJW on 16/7/19.
 */
public class FileUtils {

    public static String fileWrite(String path, String fileName, String content) {

        try {

            File file = new File(path);

            if (file.exists() && !file.isDirectory()) {
                file.delete();
            }

            if (!file.exists()) {
                file.mkdirs();
            }


            File file1 = new File(path + fileName);
            if (file1.exists() && file1.isDirectory()) {
                file1.delete();
            }

            if (!file1.exists()) {
                file1.createNewFile();
            }

            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(path + fileName, true);
            writer.write(content);
            writer.close();

            return "s";
        } catch (Exception e) {
            e.printStackTrace();
            return "e";
        }
    }


    //获取照片文件夹的内存大小
    public static String getAutoFileOrFilesSize(String path) {
        File file = new File(path);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("获取文件大小", "获取失败!");
        }
        return FormetFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     *
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        FileInputStream fis = null;
        try {
            if (file.exists()) {
                fis = new FileInputStream(file);
                size = fis.available();
            } else {
                file.createNewFile();
                Log.e("获取文件大小", "文件不存在!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获得剩余容量，即可用大小
     *
     * @return
     */
    public static long getSDAvailableSize() {
        File path = null;
        if (checkSDCardAvailable()) {//检查是否有sd卡
            path = Environment.getExternalStorageDirectory();
        } else {
            path = Environment.getDataDirectory();
        }
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    public static boolean checkSDCardAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        try {
            if (!sPath.endsWith(File.separator)) {
                sPath = sPath + File.separator;
            }
            File dirFile = new File(sPath);
            //如果dir对应的文件不存在，或者不是一个目录，则退出
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return false;
            }
            boolean flag = true;
            //删除文件夹下的所有文件(包括子目录)
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                //删除子文件
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag) break;
                } //删除子目录
                else {
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag) break;
                }
            }
            if (!flag) return false;
            //删除当前目录
            if (dirFile.delete()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        try {
            File file = new File(sPath);
            // 路径为文件且不为空则进行删除
            if (file.isFile() && file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除day天前的log文件夹
     *
     * @param path
     * @param day
     * @return
     */
    public static boolean deleteLogFolderBeforeDays(String path, int day) {

        //判断传入对象是否为一个文件夹对象
        try {
            File file = new File(path);
            if (!file.isDirectory()) {
                return false;
            } else {
                File[] t = file.listFiles();
                List<File> list = Arrays.asList(t);

                // 将文件夹按修改时间排序
                Collections.sort(list, new Comparator<File>() {
                    @Override
                    public int compare(File file, File t1) {
                        return (int) (file.lastModified() - t1.lastModified());
                    }
                });

                for (int i = 0; i < list.size() - day; i++) {
                    try {
                        list.get(i).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

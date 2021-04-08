package com.zistone.libble.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件相关操作
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyFileUtil {

    private static final String TAG = "MyFileUtil";

    /**
     * （禁止外部实例化）
     */
    private MyFileUtil() {
    }

    /**
     * 从文件路径中提取目录路径，如果文件路径不含目录返回null
     *
     * @param filePath
     * @return 目录路径，不以'/'或操作系统的文件分隔符结尾
     */
    public static String ExtractDirPath(String filePath) {
        //分隔目录和文件名的位置
        int separatePos = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return separatePos == -1 ? null : filePath.substring(0, separatePos);
    }

    /**
     * 按路径建立文件，如已有相同路径的文件则不建立
     *
     * @param filePath
     * @return
     * @throws IOException 如路径是目录或建文件时出错抛异常
     */
    public static File MakeFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile())
            return file;
        if (filePath.endsWith("/") || filePath.endsWith("\\"))
            try {
                throw new IOException(filePath + "是一个目录");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "按路径建立文件失败" + e.toString());
            }
        //文件所在目录的路径
        String dirPath = ExtractDirPath(filePath);
        //如文件所在目录不存在则先建目录
        if (dirPath != null) {
            MakeFolder(dirPath);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "文件已创建");
        return file;
    }

    /**
     * 新建目录，支持建立多级目录
     *
     * @param folderPath 新建目录的路径字符串
     * @return 创建成功返回true，否则返回false
     */
    public static boolean MakeFolder(String folderPath) {
        try {
            File file = new File(folderPath);
            if (!file.exists()) {
                file.mkdirs();
                Log.i(TAG, "新建目录为：" + folderPath);
            } else {
                Log.i(TAG, "目录已经存在：" + folderPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "新建目录操作出错" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param filePathAndName 要删除文件名及路径
     * @return 删除成功返回true，删除失败返回false
     */
    public static boolean DeleteFile(String filePathAndName) {
        try {
            File file = new File(filePathAndName);
            if (file.exists()) {
                file.delete();
                Log.i(TAG, "删除文件" + filePathAndName + "成功");
            } else {
                Log.i(TAG, "要删除的文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "删除文件" + filePathAndName + "失败" + e.toString());
            return false;
        }
        return true;
    }

    /**
     * 递归删除指定目录中所有文件和子文件夹
     *
     * @param path           目录的路径
     * @param ifDeleteFolder true：删除目录下所有文件和文件夹，false：只删除目录下所有文件,子文件夹将保留
     */
    public static void DeleteAllFile(String path, boolean ifDeleteFolder) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        String temp;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith("\\") || path.endsWith("/"))
                temp = path + tempList[i];
            else
                temp = path + File.separator + tempList[i];
            if ((new File(temp)).isFile()) {
                DeleteFile(temp);
            } else if ((new File(temp)).isDirectory() && ifDeleteFolder) {
                //先删除文件夹里面的文件
                DeleteAllFile(path + File.separator + tempList[i], true);
                //再删除空文件夹
                DeleteFolder(path + File.separator + tempList[i]);
            }
        }
    }

    /**
     * 删除文件夹，包括里面的文件
     *
     * @param folderPath 文件夹路径字符串
     */
    public static void DeleteFolder(String folderPath) {
        try {
            File myFilePath = new File(folderPath);
            if (myFilePath.exists()) {
                //删除完里面所有内容
                DeleteAllFile(folderPath, true);
                //删除空文件夹
                myFilePath.delete();
            }
            Log.i(TAG, "删除文件夹" + folderPath + "成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "删除文件夹" + folderPath + "成功" + e.toString());
        }
    }

    /**
     * 复制文件，如果目标文件的路径不存在则自动新建路径
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    public static void CopyFile(String sourcePath, String targetPath) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            int byteSum = 0;
            int byteRead;
            File sourceFile = new File(sourcePath);
            //文件存在时
            if (sourceFile.exists()) {
                //读入源文件
                inputStream = new FileInputStream(sourcePath);
                //文件所在目录的路径
                String dirPath = ExtractDirPath(targetPath);
                //如文件所在目录不存在则先建目录
                if (dirPath != null) {
                    MakeFolder(dirPath);
                }
                fileOutputStream = new FileOutputStream(targetPath);
                byte[] byteArray = new byte[1024];
                while ((byteRead = inputStream.read(byteArray)) != -1) {
                    //字节数
                    byteSum += byteRead;
                    fileOutputStream.write(byteArray, 0, byteRead);
                }
                Log.i(TAG, "源文件大小：" + byteSum);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "复制文件" + sourcePath + "失败" + e.toString());
        } finally {
            try {
                if (null != fileOutputStream)
                    fileOutputStream.close();
                if (null != fileOutputStream)
                    fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件夹，如果目标文件的路径不存在则自动新建路径
     *
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    public static void CopyFolder(String sourcePath, String targetPath) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            //如果文件夹不存在 则建立新文件夹
            MakeFolder(targetPath);
            String[] file = new File(sourcePath).list();
            File temp;
            for (int i = 0; i < file.length; i++) {
                String tempPath = MakeFilePath(sourcePath, file[i]);
                temp = new File(tempPath);
                if (temp.isFile()) {
                    fileInputStream = new FileInputStream(temp);
                    fileOutputStream = new FileOutputStream(MakeFilePath(targetPath, file[i]));
                    byte[] b = new byte[1024 * 5];
                    int len = 0;
                    int byteSum = 0;
                    while ((len = fileInputStream.read(b)) != -1) {
                        fileOutputStream.write(b, 0, len);
                        byteSum += len;
                    }
                    fileOutputStream.flush();
                    Log.i(TAG, "源文件夹大小：" + byteSum);
                    fileInputStream.close();
                    fileOutputStream.close();
                }
                //如果是子文件夹，递归复制
                else if (temp.isDirectory()) {
                    CopyFolder(sourcePath + '/' + file[i], targetPath + '/' + file[i]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "复制文件夹失败" + e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (null != fileInputStream)
                    fileInputStream.close();
                if (null != fileOutputStream)
                    fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将路径和文件名拼接起来
     *
     * @param folderPath 文件夹路径字符串
     * @param fileName   文件名字符串
     * @return 文件全路径的字符串
     */
    public static String MakeFilePath(String folderPath, String fileName) {
        return folderPath.endsWith("\\") || folderPath.endsWith("/") ? folderPath + fileName : folderPath + File.separatorChar + fileName;
    }

    /**
     * 移动文件
     *
     * @param oldFilePath 旧文件路径字符串
     * @param newFilePath 新文件路径字符串
     */
    public static void MoveFile(String oldFilePath, String newFilePath) {
        CopyFile(oldFilePath, newFilePath);
        DeleteFile(oldFilePath);
    }

    /**
     * 移动文件夹
     *
     * @param oldFolderPath 旧文件夹路径字符串
     * @param newFolderPath 新文件夹路径字符串
     */
    public static void MoveFolder(String oldFolderPath, String newFolderPath) {
        CopyFolder(oldFolderPath, newFolderPath);
        DeleteFolder(oldFolderPath);
    }

    /**
     * 获取文件夹下所有文件的路径
     *
     * @param filePath
     * @return 所有文件的路径的字符串（如果该文件是目录只获取它的目录名）
     */
    public static ArrayList<String> GetPathListFromFolder(String filePath) {
        ArrayList<String> fileNameList = new ArrayList<>();
        File file = new File(filePath);
        try {
            File[] tempFile = file.listFiles();
            for (int i = 0; i < tempFile.length; i++) {
                if (tempFile[i].isFile()) {
                    String tempFileName = tempFile[i].getName();
                    fileNameList.add(MakeFilePath(filePath, tempFileName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取文件夹下所有文件的路径失败" + e.toString());
        }
        return fileNameList;
    }

    /**
     * 递归获取文件夹下所有文件的路径
     *
     * @param filePath
     * @return 所有文件的路径的字符串（如果该文件是目录则继续往下遍历）
     */
    public static ArrayList<String> GetAllPathListFromFolder(String filePath) {
        ArrayList<String> fileNameList = new ArrayList<>();
        File file = new File(filePath);
        try {
            File[] tempFile = file.listFiles();
            for (int i = 0; i < tempFile.length; i++) {
                String tempFileName = tempFile[i].getName();
                String path = MakeFilePath(filePath, tempFileName);
                //该文件不是目录
                if (tempFile[i].isFile()) {
                    fileNameList.add(path);
                }
                //该文件是一个目录
                else {
                    ArrayList<String> tempFilePaths = GetAllPathListFromFolder(path);
                    if (tempFilePaths.size() > 0) {
                        for (String tempPath : tempFilePaths) {
                            fileNameList.add(tempPath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "递归获取文件夹下所有文件的路径失败" + e.toString());
        }
        return fileNameList;
    }

    /**
     * 获取文件夹下所有文件的总数
     *
     * @param filePath
     * @return
     */
    public static int GetFileCount(String filePath) {
        int count = 0;
        try {
            File file = new File(filePath);
            if (!FolderIsExist(filePath))
                return count;
            File[] tempFile = file.listFiles();
            for (int i = 0; i < tempFile.length; i++) {
                if (tempFile[i].isFile())
                    count++;
            }
        } catch (Exception fe) {
            count = 0;
        }
        return count;
    }

    /**
     * 判断某一文件是否为空
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static boolean FileIsNull(String filePath) throws IOException {
        boolean result = false;
        FileReader fileReader = new FileReader(filePath);
        if (fileReader.read() == -1) {
            result = true;
        }
        fileReader.close();
        return result;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean FileIsExist(String fileName) {
        // 判断文件名是否为空
        if (fileName == null || fileName.length() == 0) {
            return false;
        } else {
            File file = new File(fileName);
            if (!file.exists() || file.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断文件夹是否存在
     *
     * @param folderPath
     * @return
     */
    public static boolean FolderIsExist(String folderPath) {
        File file = new File(folderPath);
        return file.isDirectory() ? true : false;
    }

    /**
     * 获得文件的大小
     *
     * @param filePath
     * @return 单位：字节
     */
    public static Double GetFileSize(String filePath) {
        if (!FileIsExist(filePath))
            return null;
        else {
            File file = new File(filePath);
            double intNum = Math.ceil(file.length());
            return new Double(intNum);
        }
    }

    /**
     * 获取文件的最后修改时间
     *
     * @param filePath
     * @return 文件最后的修改日期
     */
    public static String GetFileModifyTime(String filePath) {
        if (!FileIsExist(filePath)) {
            return null;
        } else {
            File file = new File(filePath);
            long timeStamp = file.lastModified();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            return formatter.format(new Date(timeStamp));
        }
    }

    /**
     * 遍历某一文件夹下的所有文件的详细信息（如果该文件是目录只获取它的目录信息）
     * 每个元素是一个子ArrayList，里面包含三个字段，依次是文件的全路径(String)，文件的修改日期(String)，文件的大小(Double)
     *
     * @param folderPath
     * @returnG
     */
    public static ArrayList GetFilesInfo(String folderPath) {
        List returnList = new ArrayList();
        //获取文件夹下所有文件的路径
        List filePathList = GetPathListFromFolder(folderPath);
        for (int i = 0; i < filePathList.size(); i++) {
            List tempList = new ArrayList();
            String filePath = (String) filePathList.get(i);
            String modifyTime = GetFileModifyTime(filePath);
            Double fileSize = GetFileSize(filePath);
            tempList.add(filePath);
            tempList.add(modifyTime);
            tempList.add(fileSize);
            returnList.add(tempList);
        }
        return (ArrayList) returnList;
    }

}

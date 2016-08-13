package com.zhuyanbin.image;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class Image
{
    final private static String INPUT_DIR = "/source";
    final private static String OUTPUT_DIR = "/dest";
    final private static int CUT_HEIGHT = 475;

    public static void main(String[] argv) throws Exception
    {
        Image img = new Image();
        if (!img.initOutDir(OUTPUT_DIR)) {
            System.out.println("创建输出目录失败");
            System.exit(-1);
        }
        Vector<String> fileLists = img.getAllFiles(INPUT_DIR);
        for (String filePath : fileLists) {
            if (!img.cutImage(filePath)) {
                System.out.println("裁剪文件:" + img.getShortPath(filePath));
            }
        }
    }

    private String getShortPath(String path)
    {
        String fullPath = getClass().getResource(INPUT_DIR).getPath();
        if (path.startsWith(fullPath)) {
            return path.substring(fullPath.length() + 1);
        }

        return  path;
    }

    private String getHeadFileName(String path)
    {
        int index = path.lastIndexOf(".");
        return path.substring(0, index) + "_head" + path.substring(index);
    }

    private Vector<String> getAllFiles(String path)
    {
        Vector<String> result = new Vector<String>();

        String fullPath = getClass().getResource(path).getPath();
        File file = new File(fullPath);
        File[] fileLists = file.listFiles();
        for (File subFile : fileLists) {
            result.add(subFile.getPath());
        }

        return  result;
    }

    private boolean initOutDir(String path)
    {
        String fullPath = getClass().getResource("/").getPath() + OUTPUT_DIR;
        File file = new File(fullPath);
        if (!file.exists()) {
            return file.mkdirs();
        }

        return file.isDirectory();
    }

    private boolean cutImage(String path) throws IOException
    {
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[1];

        ImageData data = new ImageData(path);
        if (data.height <= CUT_HEIGHT) {
            throw  new RuntimeException("image height(" + data.height + ") can not be less " + CUT_HEIGHT);
        }

        ImageData headData = new ImageData(data.width, CUT_HEIGHT, data.depth, data.palette);
        ImageData tailData = new ImageData(data.width, data.height - CUT_HEIGHT, data.depth, data.palette);

        ImageData writeData;
        for (int y = 0; y < data.height; y++) {
            int py = 0;
            if (y < CUT_HEIGHT) {
                writeData = headData;
            } else {
                writeData = tailData;
                py = CUT_HEIGHT;
            }

            int pixel;
            int alpha;
            for (int x = 0; x < data.width; x++) {
                pixel = data.getPixel(x, y);
                writeData.setPixel(x, y - py, pixel);

                alpha = data.getAlpha(x, y);
                writeData.setAlpha(x, y - py, alpha);
            }
        }

        String outDirFullPath = getClass().getResource(OUTPUT_DIR).getPath() + "/";
        String fileName = outDirFullPath + getHeadFileName(getShortPath(path));
        System.out.println(fileName);
        loader.data[0] = headData;
        loader.save(fileName, data.type);


        fileName = outDirFullPath + getShortPath(path);
        System.out.println(fileName);
        loader.data[0] = tailData;
        loader.save(fileName, data.type);
        return true;
    }
}

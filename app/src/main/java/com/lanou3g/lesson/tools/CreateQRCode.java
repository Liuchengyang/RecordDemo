package com.lanou3g.lesson.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 本类由: Risky57 创建于: 16/3/9.
 */
public class CreateQRCode {

    private static final String cacheDir = "/sdcard/Download/";


    /**
     *
     * @param content 需要生成二维码的内容
     * @param bmpName 存储成图片的名字
     * @param widthPix 图片宽
     * @param heightPix 图片高
     * @param logoBm 生成的二维码中间的图片,可以为null
     * @param filePath 生成图片文件的路径
     * @param listener 回调监听
     */
    public static void createQRImage(final String content, final String bmpName, final int widthPix, final int heightPix, final Bitmap logoBm, final OnCreateQRListener listener) {
        if (content == null || "".equals(content)) {
            return;
        }
        File file = new File(cacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        final String filePath = cacheDir + bmpName + ".jpg";
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bitmap bmp = BitmapFactory.decodeFile(filePath);
                listener.onSuccess(bmp);
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                createQRImage(content, bmpName,widthPix, heightPix, logoBm, filePath, handler);

            }
        }).start();


    }

    private static void createQRImage(String content, String bmpName , int widthPix, int heightPix, Bitmap logoBm, String filePath, Handler handler) {
        try {
            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            //设置空白边距的宽度
//            hints.put(EncodeHintType.MARGIN, 2); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }


            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            boolean isSuccess = bitmap != null && bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(filePath));
            if (isSuccess) {
                handler.sendEmptyMessage(0);
            }
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createQRImage(String content, String bmpName,OnCreateQRListener listener) {
        createQRImage(content, bmpName,800, 800, null, listener);
        return false;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    public interface OnCreateQRListener {
        void onSuccess(Bitmap qrImage);
    }

    public static String defaultDir() {
        return cacheDir;
    }
}
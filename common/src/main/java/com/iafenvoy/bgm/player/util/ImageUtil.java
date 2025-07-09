package com.iafenvoy.bgm.player.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    public static InputStream convertToPng(InputStream imageStream) throws IOException {
        try {
            //Webp can also be loaded by this way.
            BufferedImage image = ImageIO.read(imageStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            System.err.println("图像处理错误: " + e.getMessage());
            throw e;
        } finally {
            if (imageStream != null)
                try {
                    imageStream.close();
                } catch (IOException e) {
                    System.err.println("关闭输入流失败: " + e.getMessage());
                }
        }
    }
}

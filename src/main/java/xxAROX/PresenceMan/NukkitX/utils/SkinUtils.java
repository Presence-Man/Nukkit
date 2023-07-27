package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.entity.data.Skin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SkinUtils {

    public static BufferedImage fromSkinToImage(Skin skin) {
        return toImage(skin.getSkinData().data, skin.getSkinData().height, skin.getSkinData().width);
    }

    public static BufferedImage toImage(byte[] data, int height, int width) {
        int[] pixelArray = new int[data.length / 4];
        for (int i = 0; i < pixelArray.length; i++) {
            int r = data[i * 4] & 0xFF;
            int g = data[i * 4 + 1] & 0xFF;
            int b = data[i * 4 + 2] & 0xFF;
            int a = data[i * 4 + 3] & 0xFF;
            pixelArray[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int position = pixelArray.length - 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixelArray[position]);
                position--;
            }
        }
        return image;
    }

    public static byte[] fromImage(BufferedImage image) throws IOException {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] bytes = new byte[width * height * 4];
        int position = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                bytes[position] = (byte) ((argb >> 16) & 0xFF); // R
                bytes[position + 1] = (byte) ((argb >> 8) & 0xFF); // G
                bytes[position + 2] = (byte) (argb & 0xFF); // B
                bytes[position + 3] = (byte) ((argb >> 24) & 0xFF); // A
                position += 4;
            }
        }

        return bytes;
    }

    public static void saveHead(String path, BufferedImage image, int height, int width) throws IOException {
        BufferedImage body = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        body.getGraphics().drawImage(image, 0, 0, height, width, null);

        int srcXY = 8;
        int srcW = 8;
        int srcH = 8;
        int hatSrcX = 40;
        int hatSrcY = 8;

        if (height == 128 && width == 128) {
            int rgb = image.getRGB(8, 8);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int a = (rgb >> 24) & 0xFF;
            if (!(r == 0 && g == 0 && b == 0 && a == 0)) {
                srcXY = 8;
                srcW = srcH = 8;
                hatSrcX = 40;
                hatSrcY = 8;
            } else {
                srcXY = 16;
                srcW = srcH = 16;
                hatSrcX = 80;
                hatSrcY = 16;
            }
        }

        BufferedImage head = body.getSubimage(srcXY, srcXY, srcW, srcH);
        body.getGraphics().drawImage(head, 0, 0, height, width, null);

        if (!(height == 32 && width == 64)) {
            BufferedImage hat = body.getSubimage(hatSrcX, hatSrcY, srcW, srcH);
            body.getGraphics().drawImage(hat, 0, 0, height, width, null);
        }

        File outputFile = new File(path);
        ImageIO.write(body, "PNG", outputFile);
    }

    public static void saveImage(String path, BufferedImage image) throws IOException {
        File outputFile = new File(path);
        ImageIO.write(image, "PNG", outputFile);
    }
}
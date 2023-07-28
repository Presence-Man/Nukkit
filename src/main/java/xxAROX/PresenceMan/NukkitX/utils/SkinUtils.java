package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.BinaryStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class SkinUtils {

    public static String getFrontFace(Player player) {
        Skin skinData = player.getSkin();
        BufferedImage faceImage = extractFrontFace(skinData);
        if (faceImage == null) {
            return null;
        }

        return encodeImageToBase64(faceImage);
    }

    private static BufferedImage extractFrontFace(Skin skin) {
        BufferedImage image = fromSkinToImage(skin);
        int width = image.getWidth();
        int height = image.getHeight();

        int xy, w, h, x, y;
        if (width == 32 && height == 32) {
            xy = 8;
            w = 8;
            h = 8;
            x = 16;
            y = 8;
        } else if (width == 64 && height == 64) {
            xy = 8;
            w = 8;
            h = 8;
            x = 48;
            y = 8;
        } else if (width == 128 && height == 128) {
            xy = 16;
            w = 16;
            h = 16;
            x = 80;
            y = 16;
        } else {
            // Unsupported skin size
            return null;
        }

        BufferedImage faceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        faceImage.getGraphics().fillRect(0, 0, width, height);
        faceImage.getGraphics().drawImage(image, 0, 0, width, height, xy, xy, xy + w, xy + h, null);
        if (!(height == 32 && width == 64)) {
            faceImage.getGraphics().drawImage(image, 0, 0, width, height, x, y, x + w, y + h, null);
        }

        return faceImage;
    }

    private static BufferedImage fromSkinToImage(Skin skin) {
        byte[] skinData = skin.getSkinData().data;
        int width, height;

        switch (skinData.length) {
            case 8192:
                width = 64;
                height = 32;
                break;
            case 16384:
                width = 64;
                height = 64;
                break;
            case 32768:
                width = 128;
                height = 64;
                break;
            case 65536:
                width = 128;
                height = 128;
                break;
            default:
                // Unsupported skin size
                return null;
        }

        BinaryStream stream = new BinaryStream(skinData);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int b = stream.getByte() & 0xFF;
                int g = stream.getByte() & 0xFF;
                int r = stream.getByte() & 0xFF;
                int a = stream.getByte() & 0xFF;

                int argb = (a << 24) | (b << 16) | (g << 8) | r;
                image.setRGB(x, y, argb);
            }
        }

        return image;
    }

    private static String encodeImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageData = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
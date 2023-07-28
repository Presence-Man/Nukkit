package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class SkinUtils {

    private static final int PIXEL_SIZE = 4;

    public static final int SINGLE_SKIN_SIZE = 64 * 32 * PIXEL_SIZE;
    public static final int DOUBLE_SKIN_SIZE = 64 * 64 * PIXEL_SIZE;
    public static final int SKIN_128_64_SIZE = 128 * 64 * PIXEL_SIZE;
    public static final int SKIN_128_128_SIZE = 128 * 128 * PIXEL_SIZE;

    public static String getSkin(Player session, Skin skinData) {
        byte[] skin = skinData.getSkinData().data;
        int width, height;
        if (skin.length == SINGLE_SKIN_SIZE) {
            width = 64;
            height = 32;
        } else if (skin.length == DOUBLE_SKIN_SIZE) {
            width = 64;
            height = 64;
        } else if (skin.length == SKIN_128_64_SIZE) {
            width = 128;
            height = 64;
        } else if (skin.length == SKIN_128_128_SIZE) {
            width = 128;
            height = 128;
        } else {
            throw new IllegalStateException("Invalid skin");
        }
        BufferedImage image = createImageFromBytes(width, height, skin);
        return getImageAsBase64(image);
    }

    private static BufferedImage createImageFromBytes(int width, int height, byte[] bytes) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        ByteArrayInputStream data = new ByteArrayInputStream(bytes);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(data.read(), data.read(), data.read(), data.read());
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    private static String getImageAsBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageData = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
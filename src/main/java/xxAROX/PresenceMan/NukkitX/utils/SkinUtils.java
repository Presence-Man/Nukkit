package xxAROX.PresenceMan.NukkitX.utils;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import xxAROX.PresenceMan.NukkitX.PresenceMan;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class SkinUtils {

    public static String getHead(Player player, Skin skin) {
        BufferedImage head = getFace(skin);
        if (head == null) return null;

        String ip = player.getAddress();
        String xuid = player.getLoginChainData().getXUID();
        String tmpFilePath = PresenceMan.getInstance().getDataFolder() + File.separator + ".cache-" + xuid + ".png";

        try {
            javax.imageio.ImageIO.write(head, "png", new File(tmpFilePath));
        } catch (IOException ignored) {}

        String data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(head, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            data = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File tmpFile = new File(tmpFilePath);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        return data;
    }

    private static BufferedImage fromSkinToImage(Skin skin) {
        return toImage(skin.getSkinData().data, getHeight(skin), getWidth(skin));
    }

    private static int getHeight(Skin skin) {
        return skin.getSkinData().height;
    }

    private static int getWidth(Skin skin) {
        return skin.getSkinData().width;
    }

    private static BufferedImage toImage(byte[] data, int height, int width) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BufferedImage image = javax.imageio.ImageIO.read(bais);
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = resizedImage.getGraphics();
            graphics.drawImage(image, 0, 0, width, height, null);
            graphics.dispose();
            return resizedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage getFace(Skin skin) {
        BufferedImage image = fromSkinToImage(skin);
        int width = image.getWidth();
        int height = image.getHeight();

        int xy, w, h, x, y;
        if (width == 32 && height == 32) {
            xy = 16;
            w = 8;
            h = 8;
            x = 40;
            y = 8;
        } else if (width == 128 && height == 128) {
            int rgb = image.getRGB(8, 8);
            int alpha = (rgb >> 24) & 0xFF;
            if (alpha != 0) {
                xy = 8;
                w = 8;
                h = 8;
                x = 40;
                y = 8;
            } else {
                xy = 16;
                w = 16;
                h = 16;
                x = 80;
                y = 16;
            }
        } else {
            xy = 8;
            w = 8;
            h = 8;
            x = 40;
            y = 8;
        }

        BufferedImage faceImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = faceImage.getGraphics();
        graphics.setColor(new java.awt.Color(0, 0, 0, 127));
        graphics.fillRect(0, 0, height, width);
        graphics.drawImage(image, 0, 0, width, height, xy, xy, xy + w, xy + h, null);
        if (!(height == 32 && width == 64)) {
            graphics.drawImage(image, 0, 0, width, height, x, y, x + w, y + h, null);
        }
        graphics.dispose();

        return faceImage;
    }
}
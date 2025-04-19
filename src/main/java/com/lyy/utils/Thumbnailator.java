package com.lyy.utils;

import java.awt.image.BufferedImage;

public class Thumbnailator {
    // 生成缩略图（固定宽度，高度按比例计算）
    public static BufferedImage createThumbnail(BufferedImage original, int targetWidth) {
        int width = original.getWidth();
        int height = original.getHeight();
        int targetHeight = (int) (height * (targetWidth / (double) width));

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(
                original.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH),
                0, 0, null
        );
        return resized;
    }
}

package com.lyy.utils;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class VideoUtils {
    public static String generateCover(MultipartFile videoFile) throws Exception {
        Path tempVideoPath = null;
        Path coverImage = null;

        try {
            // 1. 创建临时视频文件（确保唯一性）
            tempVideoPath = Files.createTempFile("video-", ".mp4");
            videoFile.transferTo(tempVideoPath);

            // 2. 提取封面帧
            coverImage = extractFirstFrame(tempVideoPath.toString()).toPath();

            // 3. 上传到OSS
            String objectKey = "covers/" + UUID.randomUUID() + ".jpg"; // 唯一文件名
            return AliOssUtil.uploadFile(objectKey, Files.newInputStream(coverImage));
        } catch (Exception e) {
            throw new RuntimeException("封面生成失败: " + e.getMessage(), e);
        } finally {
            // 4. 强制清理临时文件
            cleanupTempFiles(tempVideoPath, coverImage);
        }
    }

    private static File extractFirstFrame(String videoPath) throws IOException {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        try {
            grabber.start();

            // 跳过无效帧（黑屏/音频帧）
            Frame frame;
            int maxAttempts = 10;
            for (int i = 0; i < maxAttempts; i++) {
                frame = grabber.grabImage();
                if (frame != null && !isBlackFrame(frame)) {
                    return convertFrameToImage(frame);
                }
            }
            throw new IOException("未找到有效视频帧");
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }

    private static void cleanupTempFiles(Path... paths) {
        for (Path path : paths) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.println("临时文件清理失败: " + path);
                }
            }
        }
    }

    // 辅助方法：检测黑屏帧（示例逻辑）
    private static boolean isBlackFrame(Frame frame) {
        // 实现具体黑屏检测逻辑（如像素亮度分析）
        return false;
    }

    // 辅助方法：转换帧为图片文件
    private static File convertFrameToImage(Frame frame) throws IOException {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage image = converter.getBufferedImage(frame);
        BufferedImage thumbnail = Thumbnailator.createThumbnail(image, 640);

        File tempFile = File.createTempFile("cover-", ".jpg");
        ImageIO.write(thumbnail, "jpg", tempFile);
        return tempFile;
    }


    // 使用 FFmpeg 提取视频时长（单位：秒）
    public static int extractVideoDuration(MultipartFile videoFile) throws IOException {
        Path tempVideoPath = Files.createTempFile("video-", ".mp4");
        videoFile.transferTo(tempVideoPath);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempVideoPath.toFile())) {
            grabber.start();
            long durationMicroseconds = grabber.getLengthInTime(); // 单位：微秒
            int durationSeconds = (int) (durationMicroseconds / 1_000_000); // 转换为秒
            grabber.stop();
            return durationSeconds;
        } finally {
            Files.deleteIfExists(tempVideoPath);
        }
    }
}


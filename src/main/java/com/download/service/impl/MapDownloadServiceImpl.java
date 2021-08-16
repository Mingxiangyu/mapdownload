package com.download.service.impl;

import com.download.service.MapDownloadService;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 天级预警服务实现类
 *
 * @author mxy
 * @since 2020-09-10
 */
@Slf4j
@Service
public class MapDownloadServiceImpl implements MapDownloadService {

  @Value("${dir.outputpath}")
  private String outputPath;

  @Value("${dir.tilepath}")
  private String tilePath;

  public static void main(String[] args) {
    MapDownloadServiceImpl mapDownloadService = new MapDownloadServiceImpl();
    // 右上
    //    117.75888
    //    39.04464
    // 左下
    //    117.69156
    //    39.01317
    double minLatD = 39.01317;
    double maxLatD = 39.04464;
    double minlngD = 117.69156;
    double maxlngD = 117.75888;
    mapDownloadService.downloadMap(minLatD, maxLatD, minlngD, maxlngD, 17, null);
  }

  @Override
  public void downloadMap(
      double minLatD,
      double maxLatD,
      double minlngD,
      double maxlngD,
      int zoomLevel,
      HttpServletResponse response) {
    Pair<Integer, Integer> minPair = getTileNumber(minLatD, minlngD, zoomLevel);
    Pair<Integer, Integer> maxPair = getTileNumber(maxLatD, maxlngD, zoomLevel);
    int minCol = minPair.getLeft();
    int minRow = minPair.getRight();
    int maxCol = maxPair.getLeft();
    int maxRow = maxPair.getRight();
    if (minCol > maxCol) {
      int x = maxCol;
      maxCol = minCol;
      minCol = x;
    }
    if (minRow > maxRow) {
      int x = maxRow;
      maxRow = minRow;
      minRow = x;
    }

    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
    String fileName = sdf.format(date) + ".png";
    String outputPath = this.outputPath;
    String outPutFilePath = outputPath + File.separator + fileName;
    splicingTiles(zoomLevel, minCol, maxCol, minRow, maxRow, outPutFilePath, tilePath);
    download(outPutFilePath, response, fileName);
  }

  /**
   * 获取瓦片标号
   *
   * @param lat 纬度
   * @param lon 经度
   * @param zoom 层级
   * @return
   */
  public static Pair<Integer, Integer> getTileNumber(
      final double lat, final double lon, final int zoom) {
    //    Math.floor 返回不大于值的最大整数
    int col = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
    int row =
        (int)
            Math.floor(
                (1
                        - Math.log(
                                Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat)))
                            / Math.PI)
                    / 2
                    * (1 << zoom));
    if (col < 0) {
      col = 0;
    }
    if (col >= (1 << zoom)) {
      col = ((1 << zoom) - 1);
    }
    if (row < 0) {
      row = 0;
    }
    if (row >= (1 << zoom)) {
      row = ((1 << zoom) - 1);
    }
    return Pair.of(col, row);
  }

  /**
   * @param zoomLevel 瓦片层级
   * @param minCol 最小列
   * @param maxCol 最大列
   * @param minRow 最小行
   * @param maxRow 最大行
   * @param outPutFileName 生成图片存储路径
   * @param tilePath 瓦片文件夹
   */
  private static void splicingTiles(
      int zoomLevel,
      int minCol,
      int maxCol,
      int minRow,
      int maxRow,
      String outPutFileName,
      String tilePath) {

    File file = new File(outPutFileName);
    if (file.exists()) {
      file.delete();
    }
    // 获取拼接图片宽度
    int imageWidth = 256 * (Math.abs(maxCol - minCol) + 1);
    // 获取拼接图片高度
    int imageHeight = 256 * (Math.abs(maxRow - minRow) + 1);
    BufferedImage memoryimg =
        new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    String sourceFileName;
    File sourceFile;
    // 循环读取瓦片列文件夹（即该瓦片所在文件夹）
    for (int col = minCol; col <= maxCol; col++) {
      // 循环读取瓦片行文件（即该瓦片名称）
      for (int row = minRow; row <= maxRow; row++) {
        try {
          // 获取该文件绝对路径
          sourceFileName = tilePath + zoomLevel + "\\" + col + "\\" + row + ".png";
          sourceFile = new File(sourceFileName);
          if (sourceFile.exists()) {
            saveBitmapBuffered(
                memoryimg, new FileInputStream(sourceFile), col - minCol, row - minRow);
          } else {
            log.error("不存在：" + sourceFileName);
          }
        } catch (Exception ex) {
          log.error(ex.getMessage());
        }
      }
    }
    try {
      ImageIO.write(memoryimg, "png", file);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    log.info("拼接完成");
  }

  /**
   * 将单个切片的像素值赋值给拼接后的图片
   *
   * @param image 拼接后的图片对象
   * @param sourceInputstream 单个瓦片文件流
   * @param col 当前瓦片列
   * @param row 当前瓦片行
   */
  private static void saveBitmapBuffered(
      BufferedImage image, FileInputStream sourceInputstream, int col, int row) {
    int colPixel = col * 256;
    int rowPixel = row * 256;
    try {
      BufferedImage bufferedImage = ImageIO.read(sourceInputstream);
      for (int i = 0; i < 256; i++) {
        for (int j = 0; j < 256; j++) {
          image.setRGB(colPixel + i, rowPixel + j, bufferedImage.getRGB(i, j));
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public static void download(String downloadPath, HttpServletResponse response, String fileName) {
    fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    response.setHeader("Content-disposition", "attachment; filename=" + fileName);
    try (BufferedInputStream inputStream =
            new BufferedInputStream(new FileInputStream(downloadPath));
        ServletOutputStream outputStream = response.getOutputStream()) {

      byte[] b = new byte[2048];
      int len;
      while ((len = inputStream.read(b)) > 0) {
        outputStream.write(b, 0, len);
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }
}

package com.download.controller;

import com.download.service.MapDownloadService;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author T480S */
@RestController
public class MapDownloadController {

  @Autowired MapDownloadService service;

  @GetMapping("/map/download")
  public ResponseEntity<String> downloadMap(
      @RequestParam String minLat,
      @RequestParam String maxLat,
      @RequestParam String minlng,
      @RequestParam String maxlng,
      @RequestParam String zoom,
      HttpServletResponse response) {
    double minLatD = Double.parseDouble(minLat);
    double maxLatD = Double.parseDouble(maxLat);
    double minlngD = Double.parseDouble(minlng);
    double maxlngD = Double.parseDouble(maxlng);

    long startTime = System.currentTimeMillis();
    service.downloadMap(minLatD, maxLatD, minlngD, maxlngD, NumberUtils.toInt(zoom), response);
    long endTime = System.currentTimeMillis();

    return ResponseEntity.ok().body("耗时：" + (endTime - startTime) + "毫秒！");
  }
}

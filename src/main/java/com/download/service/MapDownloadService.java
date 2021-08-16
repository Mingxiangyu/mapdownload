package com.download.service;

import javax.servlet.http.HttpServletResponse;

/**
 * 服务类
 *
 * @author mxy
 * @since 2020-09-10
 */
public interface MapDownloadService {

  void downloadMap(double minLatD, double maxLatD, double minlngD, double maxlngD, int zoomLevel,HttpServletResponse response);
}

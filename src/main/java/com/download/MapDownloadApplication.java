package com.download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
public class MapDownloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapDownloadApplication.class, args);
	}

	/**
	 * 屏蔽swaggerUI中显示的basicerror
	 *
	 * @return 空
	 */
	@Bean
	public Docket demoApi() {
		return new Docket(DocumentationType.OAS_30)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.regex("(?!/error.*).*"))
				.build();
	}
}

package com.lyy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableConfigurationProperties
public class VideoPlatformApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(VideoPlatformApplication.class,args);
    }
}

package com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

@Component
public class TomcatListener implements ServletContextListener {

    private ServletContext servletContext;

    public static String TMP_DIR = null;

    @Value("${upload.tmp.dir}")
    private String tmp;

    @Value("${upload.dir}")
    private String upload ;

//    @Autowired
//    private Sheduler sheduler ;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
//        System.out.println("tomcat关闭了");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        File tempFile = new File(tmp);
        if(tempFile.exists()==false) {
            tempFile.mkdirs() ;
        }

        File uploadFile = new File(upload);
        if(!uploadFile.exists()) {
            uploadFile.mkdirs() ;
        }
        servletContext=arg0.getServletContext();

        ApplicationContext applicationContext= WebApplicationContextUtils.getWebApplicationContext(servletContext);
        SpringUtil.context = applicationContext ;
    }
}

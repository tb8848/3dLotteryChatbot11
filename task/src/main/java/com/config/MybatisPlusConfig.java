package com.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author miemie
 * @since 2018-08-10
 */
@Configuration
@MapperScan("com.dao")
public class MybatisPlusConfig {

    @Autowired
    private PaginationInnerInterceptor paginationInterceptor;

    @Bean
    public MybatisPlusInterceptor  mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            // 获取参数方法
            Map<String, Object> paramMap = RequestDataHelper.getRequestData();
            if (paramMap==null || !"xxx".equals(tableName))
                return  tableName;

            String qihao = (String)paramMap.get("qihao"); //期号
            if (qihao==null){
                return tableName;
            }


            return "draw_buy_record_" + qihao;
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        interceptor.addInnerInterceptor(paginationInterceptor);
        // 3.4.3.2 作废该方式
        // dynamicTableNameInnerInterceptor.setTableNameHandlerMap(map);
        return interceptor;
    }
}

package com.beans;

import lombok.Data;

import java.util.Map;

@Data
public class ResponseBean<T> {
    private Integer code ;
    private String msg ;
    private Long count ;
    private T data ;

    private boolean noTrans = false;

    //国际化内容中的占位符key和value
    private Map<String,String> holderMap;

    public final static ResponseBean OK = new ResponseBean(200 , "success" , null) ;

    public ResponseBean(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseBean(Integer code, String msg, T data,Boolean noTrans) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.noTrans = noTrans;
    }

    public ResponseBean(Integer code, Long count , T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count ;
    }

    public ResponseBean(Integer code, Integer count , T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count.longValue() ;
    }

    public ResponseBean(Integer code, Long count , String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count ;
    }

    public ResponseBean(Integer code, Integer count , String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count.longValue() ;
    }

    public ResponseBean(Integer code, Integer count , String msg, T data, Map<String,String> holderMap) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count.longValue() ;
        this.holderMap = holderMap;
    }

    public ResponseBean(Integer code, Integer count , String msg, T data, Boolean noTrans) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.count = count.longValue() ;
        this.noTrans = noTrans;
    }

    public ResponseBean(){}



    public static ResponseBean error(String msg){
        return new ResponseBean(500 , msg , null) ;
    }

    public static ResponseBean ok(String msg){
        return new ResponseBean(200 , msg , null) ;
    }
}

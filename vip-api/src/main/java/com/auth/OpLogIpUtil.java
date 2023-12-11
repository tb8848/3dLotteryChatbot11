package com.auth;

import com.google.common.collect.Maps;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OpLogIpUtil {

    DatabaseReader databaseReader;

    @PostConstruct
    public void initDatabaseReader() {
        try {
            InputStream database = this.getClass().getClassLoader()
                    .getResourceAsStream("db/GeoLite2City.mmdb");
            // 创建 DatabaseReader对象
            databaseReader = new DatabaseReader.Builder(database).build();
        } catch (Exception e) {
            log.error("启动加载IP库异常,执行退出", e);
            System.exit(0);
        }
    }

    /**
     * UserAgent转换设备
     *
     * @param userAgent
     * @return
     */
    public String userAgentToDevice(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        UserAgent agent = UserAgent.parseUserAgentString(userAgent);
        Browser browser = agent.getBrowser();
        OperatingSystem operatingSystem = agent.getOperatingSystem();
        return sb.append(browser.getName())
                .append(" ")
                .append(agent.getBrowserVersion())
                .append(" / ")
                .append(operatingSystem.getName()).toString();
    }

    /**
     * IP转地址
     *
     * @param ip IP地址
     * @return java.lang.String
     */
    private String ipToCityInfo(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = databaseReader.city(ipAddress);
            StringBuilder sb = new StringBuilder();
            List<String> parts = new ArrayList<>();

            //获取城市信息
            Country country = response.getCountry();
            if (null != country) {
                String name = country.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    parts.add(name);
                }
                //sb.append(StringUtils.hasText(name) ? name : "");
            }

            Subdivision subdivision = response.getLeastSpecificSubdivision();
            if (null != subdivision) {
                String name = subdivision.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    parts.add(name);
                }
                //sb.append(StringUtils.hasText(name) ? name : "");
            }

            City city = response.getCity();
            if (null != city) {
                String name = city.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    parts.add(name);
                }
                //sb.append(StringUtils.hasText(name) ? name : "");
            }

            Location location = response.getLocation();
            if (null != location) {
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                if (null != lat && null != lng) {
                    sb.append("(")
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude()).append(")");
                        parts.add(sb.toString());
                }
            }
            return parts.stream().collect(Collectors.joining(","));
            //return sb.toString();
        } catch (AddressNotFoundException ex) {
            return "内网IP";
        } catch (Exception e) {
            log.error("IP转换地址发生异常:{}", ip, e);
        }
        return null;
    }


    /**
     * 根据IP地址获取信息
     *
     * @param ip IP地址
     * @return java.lang.String
     */
    public String getCityInfo(String ip) {
        if (StringUtils.isEmpty(ip)) {
            log.info("IP转地址,IP为空:{}", ip);
            return null;
        }
        return ipToCityInfo(ip);
    }




    /**
     * IP转地址
     *
     * @param ip IP地址
     * @return java.lang.String
     */
    private Map<String,Object> ipToCityInfoV2(String ip) {

        Map<String,Object> addrMap = Maps.newHashMap();

        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = databaseReader.city(ipAddress);
            StringBuilder sb = new StringBuilder();
            List<String> parts = new ArrayList<>();

            //获取城市信息
            Country country = response.getCountry();
            if (null != country) {
                String name = country.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    addrMap.put("country",name);
                }
            }

            Subdivision subdivision = response.getLeastSpecificSubdivision();
            if (null != subdivision) {
                String name = subdivision.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    addrMap.put("province",name);
                    //parts.add(name);
                }
                //sb.append(StringUtils.hasText(name) ? name : "");
            }

            City city = response.getCity();
            if (null != city) {
                String name = city.getNames().get("zh-CN");
                if(StringUtils.hasText(name)){
                    addrMap.put("city",name);
                    //parts.add(name);
                }
                //sb.append(StringUtils.hasText(name) ? name : "");
            }

            Location location = response.getLocation();
            if (null != location) {
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                if (null != lat && null != lng) {

                    sb.append("(")
                            .append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude()).append(")");
                    addrMap.put("lnglat",sb.toString());
                    //parts.add(sb.toString());
                }
            }
            return addrMap;
            //return sb.toString();
        } catch (AddressNotFoundException ex) {
            addrMap.put("province","内网IP");
            return addrMap;
        } catch (Exception e) {
            log.error("IP转换地址发生异常:{}", ip, e);
        }
        return null;
    }


    /**
     * 根据IP地址获取信息
     *
     * @param ip IP地址
     * @return java.lang.String
     */
    public Map<String,Object> getCityInfoV2(String ip) {
        if (StringUtils.isEmpty(ip)) {
            log.info("IP转地址,IP为空:{}", ip);
            return null;
        }
        return ipToCityInfoV2(ip);
    }

}

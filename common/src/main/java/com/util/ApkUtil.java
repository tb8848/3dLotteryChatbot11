package com.util;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApkUtil {

    public static Map<String, Object> readAPK(String apkUrl) {
        Map<String, Object> resMap = new HashMap<String, Object>();
        try{
            ApkFile apkFile = new ApkFile(apkUrl);
            ApkMeta apkMeta = apkFile.getApkMeta();
            resMap.put("filename", apkMeta.getName());
            resMap.put("pkgname", apkMeta.getPackageName());
            resMap.put("versioncode", apkMeta.getVersionCode());
            resMap.put("versionname", apkMeta.getVersionName());
            for (UseFeature feature : apkMeta.getUsesFeatures()) {
//                System.out.println(feature.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resMap;

    }
}

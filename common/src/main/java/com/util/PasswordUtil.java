package com.util;

import java.util.Random;

public class PasswordUtil {
	
	private static String pre_md1 = "WO_1234567890!@#$%^&*()";
	private static String pre_md2 = "wo_!@#$%^&*(1234567890)";
	
	public static String jiami(String pwd)
	{
		String password = MD5Util.getMD5(pre_md2+ MD5Util.getMD5(pre_md1+pwd).toLowerCase());
		return password;
		
	}

	public static String VerifyCode(int n){
		Random r = new Random();
		StringBuffer sb =new StringBuffer();
		for(int i = 0;i < n;i ++){
			int ran1 = r.nextInt(10);
			sb.append(String.valueOf(ran1));
		}
		return sb.toString();
	}

    public static void main(String[] args) {
    	String pwd = "123456";
		String password = PasswordUtil.jiami(pwd);
		System.out.println(password);
	}
}

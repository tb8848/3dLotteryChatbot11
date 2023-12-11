package com.util;

import cn.hutool.core.util.HexUtil;
import com.aliyuncs.utils.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MD5Util {
	
	public static void main(String[] args) throws Exception {
		//String pwd = MD5Util.getMD5( MD5Util.getMD5("123456"));
//		System.out.println(pwd);

//        String password = MD5Util.getMD5(MD5Util.getMD5("123456" + "sunjob")
//                + "sunjob");
        //System.out.println(password);
//        String a = "\"\\\"\\\"\\\"\\\"\\\"\\\"\\\"[\\\"{\\\"罐\\\":\\\"大\\\"}\\\"]\\\"\\\"\\\"\\\"\\\"\\\"\\\"\"";
//        int x = a.indexOf("[");
//        int y = a.indexOf("]");
//        String aa = a.substring(x,y+1);
//        String aaa = aa.replaceFirst("\\\\","");
//        int z = aaa.indexOf("}");
//        int length = aaa.length();
//        System.out.println(aaa.substring(0,z+1));
//        System.out.println(aaa.substring(z+2,length));
//        System.out.println(aaa.substring(0,z+1)+aaa.substring(z+2,length));

        String b = "[\"0\",\"0\"]";
        int xy = b.length();
        String c = b.substring(1,xy-1).replaceAll("\"","");
        System.out.println(c);
//        System.out.println(jiami("06CEE4F200000000" , "FFFFFFFFFFFFFFFF"));
	}
	
	private static String byteHEX(byte ib) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F' };
		char[] ob = new char[2];
		ob[0] = Digit[(ib >>> 4) & 0X0F];
		ob[1] = Digit[ib & 0X0F];
		String s = new String(ob);
		return s;
	}
	
//字符串加密	
   public static String getMD5(String source){
	   MessageDigest messageDigest = null;
	try {
		messageDigest = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
	}
       messageDigest.update(source.getBytes());
       byte[] b = messageDigest.digest();
       StringBuffer sb = new StringBuffer();
       for (int i=0;i<b.length;i++){
      	 sb.append(byteHEX(b[i]));
       }
       
       //sb.setCharAt(sb.length()-1, (char)(sb.charAt(sb.length()-1)+1));
       return sb.toString();
   }
   
   public static String encodePassword(String password) {
       if (StringUtils.isEmpty(password)) {
           return password;
       }
       String str = null;
	   try {
		   str = getMD5(password.getBytes("utf-8"));
	   } catch (UnsupportedEncodingException e) {
		   e.printStackTrace();
	   }
       return str;
   }

   public static String getMD5(byte[] source) {
       String s = null;
       char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
               '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
       try {
           MessageDigest md = MessageDigest.getInstance("MD5");
           md.update(source);
           byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
           // 用字节表示就是 16 个字节
           char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
           // 所以表示成 16 进制需要 32 个字符
           int k = 0; // 表示转换结果中对应的字符位置
           for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
               // 转换成 16 进制字符的转换
               byte byte0 = tmp[i]; // 取第 i 个字节
               str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
               // >>> 为逻辑右移，将符号位一起右移
               str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
           }
           s = new String(str); // 换后的结果转换为字符串

       } catch (Exception e) {
           e.printStackTrace();
       }
       return s;
   }

   public final static String MD5(String inputStr) {
       // 用于加密的字符
       char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
       try {
           // 使用utf-8字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
           byte[] btInput = inputStr.getBytes("utf-8");

           // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
           MessageDigest mdInst = MessageDigest.getInstance("MD5");

           // MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
           mdInst.update(btInput);

           // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
           byte[] md = mdInst.digest();

           // 把密文转换成十六进制的字符串形式
           int j = md.length;
           char str[] = new char[j * 2];
           int k = 0;
           for (int i = 0; i < j; i++) { // i = 0
               byte byte0 = md[i]; // 95
               str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
               str[k++] = md5String[byte0 & 0xf]; // F
           }

           // 返回经过加密后的字符串
           return new String(str);

       } catch (Exception e) {
           return null;
       }
   }
   
	public static String encryption(String plain) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plain.getBytes("utf-8"));
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return re_md5;
	}

    // 解密
    static public String jiemi(String src) {
        // 密钥
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");
        String now = sdf.format(date);
        String Password = MD5Util.getMD5(now).substring(0,24);
        // String Password = MD5.md5_24(pwd);
        // String Password = ;
        String Algorithm = "DESede";
        byte KeyBytes[] = new byte[24];
        byte PassBytes[] = Password.getBytes();

        for (int i = 0; i < 24; i++) {
            if (i < PassBytes.length)
                KeyBytes[i] = PassBytes[i];
            else
                KeyBytes[i] = 0x30;
        }
        // 还原
        byte[] b = hexStringToBytes(src);
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        SecretKey DesKey = new SecretKeySpec(KeyBytes, Algorithm);

        try {
            Cipher C = Cipher.getInstance(Algorithm);
            C.init(Cipher.DECRYPT_MODE, DesKey);
            byte Encrypt[] = C.doFinal(b);

            return new String(Encrypt);
        } catch (Exception e) {

        }

        return null;
    }

    public static String jiami(String rand, String dESKey) throws  Exception {
//	    if(rand.length() == 12) {
//	        rand += "0000" ;
//        }
        SecureRandom sr = new SecureRandom();

        byte[] rawKeyData =HexUtil.decodeHex(dESKey);

        DESKeySpec dks = new DESKeySpec(rawKeyData);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");

        SecretKey key = keyFactory.generateSecret(dks);

        Cipher cipher = Cipher.getInstance("DESede");

        cipher.init(1, key, sr);

        byte[] data = rand.getBytes("UTF8");

        byte[] encryptedData = cipher.doFinal(data);

        String enOut = HexUtil.encodeHexStr(encryptedData);

        return enOut;
    }

    public static byte[] hexStringToBytes(String hexString) {

        if (hexString == null || hexString.equals("")) {

            return null;

        }

        hexString = hexString.toUpperCase();

        int length = hexString.length() / 2;

        char[] hexChars = hexString.toCharArray();

        byte[] d = new byte[length];

        for (int i = 0; i < length; i++) {

            int pos = i * 2;

            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }

        return d;
    }

    /**
     * MD5加密类
     *
     * @param str 要加密的字符串
     * @return 加密后的字符串
     */
    public static String code(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static byte charToByte(char c) {

        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}

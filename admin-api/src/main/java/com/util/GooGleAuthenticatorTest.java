package com.util;

public class GooGleAuthenticatorTest {
    // 当测试authTest时候，把genSecretTest生成的secret值赋值给它
    private static String secret = "R2Q3S52RNXBTFTOM";

    public static void genSecretTest() {// 生成密钥
        secret = GoogleAuthenticator.generateSecretKey();
        // 把这个qrcode生成二维码，用google身份验证器扫描二维码就能添加成功
        String qrcode = GoogleAuthenticator.getQRBarcode("133080077@qq.com", secret);
        System.out.println("qrcode:" + qrcode + ",key:" + secret);
    }

    /**
     * 对app的随机生成的code,输入并验证
     */
    public static void verifyTest() {
        long code = 454325;
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        boolean r = ga.check_code("HSCYUJ3VBPP5P754", code, t);
        System.out.println("检查code是否正确？" + r);
    }

    public static void main(String[] args) {
        genSecretTest();
        //verifyTest();
    }

}

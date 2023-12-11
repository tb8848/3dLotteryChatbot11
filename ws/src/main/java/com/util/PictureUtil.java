package com.util;

import cn.hutool.core.date.DateUtil;
import com.beans.Draw;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PictureUtil {

    public  final static PictureUtil pictureUtil = new PictureUtil();
    private  PictureUtil(){

    }
    public static  PictureUtil getInstance(){
        return  pictureUtil;
    }
    public  void  createImages(List<Draw> drawList, String filePath, String fileName){
        try {
            int rows = drawList.size();
            String[][] tableData2 = new String[rows+1][3];
            tableData2[0][0] = "集数";
            tableData2[0][1] = "时间";
            tableData2[0][2] = "结果";
            for(int i=0;i<drawList.size();i++){
                Draw one = drawList.get(i);
                tableData2[i+1][0] = String.valueOf(one.getDrawId());
                tableData2[i+1][1] = String.valueOf(DateUtil.format(one.getOpenDateTime(),"yyyy-MM-dd"));
                tableData2[i+1][2] = one.getDrawResult2T()+"\t"+one.getDrawResult3T()+"\t"+one.getDrawResult4T();
            }

            myGraphicsGeneration(tableData2,filePath,fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        public  void myGraphicsGeneration(String cellsValue[][], String path,String fileName) {
            int kuochong = 200;
            // 字体大小
            int fontTitileSize = 15;
            // 横线的行数
            int totalrow = cellsValue.length+1;
            // 竖线的行数
            int totalcol = 0;
            if (cellsValue[0]  != null) {
                totalcol = cellsValue[0].length;
            }
            // 图片宽度
            int imageWidth = 300;
            // 行高
            int rowheight = 40;
            // 图片高度
            int imageHeight = totalrow*rowheight+200;
            // 起始高度
            int startHeight = 50;
            // 起始宽度
            int startWidth = 10;
            // 单元格宽度
            int colwidth = (int)((imageWidth-20)/totalcol);
            BufferedImage image = new BufferedImage(imageWidth, imageHeight,BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0,0, imageWidth, imageHeight);
            graphics.setColor(new Color(220,240,240));

            //画横线
//            for(int j=0;j<totalrow; j++){
//                graphics.setColor(Color.black);
//                graphics.drawLine(startWidth,
//                        startHeight+(j+1)*rowheight,
//                        startWidth+colwidth*totalcol,
//                        startHeight+(j+1)*rowheight);
//            }
            //画竖线
//            for(int k=0;k<totalcol+1;k++){
//                if(k==2){
//                    graphics.setColor(Color.black);
//                    graphics.drawLine(startWidth+k*colwidth+kuochong, startHeight+rowheight, startWidth+k*colwidth+kuochong, startHeight+rowheight*totalrow);
//                }else{
//                    graphics.setColor(Color.black);
//                    graphics.drawLine(startWidth+k*colwidth, startHeight+rowheight, startWidth+k*colwidth, startHeight+rowheight*totalrow);
//                }
//            }
//            for(int k=2;k<totalcol;k++){ // 合并单元格 （其实也就是消除掉黑色的线）
//                if(k==2){
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth+kuochong, startHeight+1+rowheight, startWidth+k*colwidth+kuochong, rowheight*3+9);
//                }else{
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth, startHeight+1+rowheight, startWidth+k*colwidth, rowheight*3+9);
//                }
//            }

//            for(int k=2;k<totalcol;k++){// 合并单元格 （其实也就是消除掉黑色的线）
//                if(k==2){
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth+kuochong, startHeight+1+rowheight*5, startWidth+k*colwidth+kuochong, rowheight*7+9);
//                }else{
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth, startHeight+1+rowheight*5, startWidth+k*colwidth, rowheight*7+9);
//                }
//            }

//            for(int k=2;k<totalcol;k++){// 合并单元格 （其实也就是消除掉黑色的线）
//                if(k==2){
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth+kuochong, startHeight+1+rowheight*8, startWidth+k*colwidth+kuochong, rowheight*10+9);
//                }else{
//                    graphics.setColor(Color.white);
//                    graphics.drawLine(startWidth+k*colwidth, startHeight+1+rowheight*8, startWidth+k*colwidth, rowheight*10+9);
//                }
//            }

            graphics.setColor(Color.black);
            //画竖线
        /*for(int k=1;k<totalcol+1;k++){
           // graphics.setColor(Color.white);
            graphics.drawLine(startWidth+k*colwidth, startHeight+rowheight, startWidth+k*colwidth, rowheight);
        }*/

            //设置字体
            Font font = new Font("微软雅黑",Font.BOLD,fontTitileSize);
//            graphics.setFont(font);
//
//            //写标题
//            String title02 = "编号："+contractBh;
//            graphics.drawString(title02, startWidth, startHeight+rowheight-10);
//
//
//
//            Font fonttile = new Font("微软雅黑",Font.BOLD,18);
//            graphics.setFont(fonttile);
//
//            //写标题
//            String title03 = "测试测试测试";
//            graphics.drawString(title03, 850, 25);
//
//            //写标题
//            String title04 = "刺客伍六七哈哈哈哈哈哈";
//            graphics.drawString(title04, 790, 60);
            //写入内容
            for(int n=0;n<cellsValue.length;n++){
                for(int h=0;h<cellsValue[n].length;h++){
                    font = new Font("微软雅黑",Font.PLAIN,fontTitileSize);
                    graphics.setFont(font);
                    graphics.setColor(Color.BLACK);
                    if(h>0){
                        if(cellsValue[n][h].equals(cellsValue[n][h-1])){

                        }else{
                            if(h==2){
                                graphics.drawString(cellsValue[n][h], startWidth+colwidth*h+5+kuochong, startHeight+rowheight*(n+2)-10);
                            }else{
                                graphics.drawString(cellsValue[n][h], startWidth+colwidth*h+5, startHeight+rowheight*(n+2)-10);
                            }
                        }
                    }else{
                        graphics.drawString(cellsValue[n][h], startWidth+colwidth*h+5, startHeight+rowheight*(n+2)-10);

                    }
                }
            }
            // 保存图片
            createImage(image, path,fileName);
        }

        /**
         * 将图片保存到指定位置
         * @param image 缓冲文件类
         * @param fileLocation 文件位置
         */
        public static void createImage(BufferedImage image, String fileLocation,String fileName) {
            try {
                File file = new File(fileLocation);
                if(!file.exists()){
                    file.mkdir();
                }
                FileOutputStream fos = new FileOutputStream(fileLocation+fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ImageIO.write(image, "jpg", fos);
           /* JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
            encoder.encode(image);*/
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 图片转换为string
         * @return
         */
        public static String fileToByteArray(String filePath) throws Exception{
            BASE64Encoder encoder = new sun.misc.BASE64Encoder();
            File file = new File(filePath);
            BufferedImage bi = ImageIO.read(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return encoder.encodeBuffer(bytes).trim();
        }
}

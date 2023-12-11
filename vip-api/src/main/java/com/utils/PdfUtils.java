package com.utils;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;

import java.io.IOException;

public class PdfUtils {

    public static Paragraph createParagraph(int align,String content,int leading,int fontSize){
        Paragraph paragraph = new Paragraph();
        if(leading>0){
            paragraph.setLeading(leading);
        }
        //设置字体
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            //对齐方式     1 2 3代表中右左
            paragraph.setAlignment(align);
            //字体
            Font fontHeader = new Font(bfChinese);
            //字体大小
            fontHeader.setSize(fontSize);
            //设置段落字体
            paragraph.setFont(fontHeader);
            Chunk chunk1 = new Chunk(content);
            paragraph.add(chunk1);
            return paragraph;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

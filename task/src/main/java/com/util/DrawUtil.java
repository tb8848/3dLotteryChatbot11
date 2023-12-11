package com.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrawUtil {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println(getDrawInfo());

        /*String s = "2022-11-14 20:0:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(s);
        String str = sdf.format(date);
        System.out.println(str);*/

        /*String str = "362781";
        for (int i = 1; i < str.length(); i ++) {
            String s = str.substring(i);
            System.out.println(s);
        }*/
    }

    public static Map<String, Object> getDrawInfo() throws IOException {
        Map<String, Object> result = new HashMap<>();
        String url = "http://php.szghrj.com:7878/"; //http://caipiao.szghrj.com:7878/
        Document doc = Jsoup.connect(url).get();

        //出奖日期
        Element elementKaijiangDate =  doc.getElementsByClass("my-6").first();
        Elements elementsDaojishiDiv = elementKaijiangDate.getElementsByTag("div").first().children();

        Iterator<Element> it = elementsDaojishiDiv.iterator();

        int i = 0 ;
        int secondes = 0;
        while (it.hasNext()){
            Element e = it.next().child(0);
            int     n = Integer.parseInt(e.html());
            switch (i)
            {
                case 0:
                    secondes += 24*n*3600;
                    break;
                case 1:
                    secondes += n*3600;
                    break;
                case 2:
                    secondes += n*60;
                    break;
                case 3:
                    secondes += n;
                    break;

            }
            i++;
        }

        TimeZone timeZone = TimeZone.getTimeZone("GMT+07:00");
        Calendar c = Calendar.getInstance(timeZone);
        c.add(Calendar.SECOND,secondes);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day   = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        //int second = c.get(Calendar.SECOND);
        String chuJiangShijian = year+"-"+month+"-"+day+" "+hour+":"+minute+":00"; //有误差

        if (i>0)
        {
            System.out.println("本期开奖时间:"+chuJiangShijian);
            result.put("drawDate", chuJiangShijian);
        }
        else{
            System.out.println("本期获取开奖时间未定");
            result.put("drawDate", "本期获取开奖时间未定");
        }

        //上一期开奖时间和结果
        Element elementSanQiKaijiangDate =  doc.getElementsByClass("my-6").last();
        String sanQiRiqi = elementSanQiKaijiangDate.child(0).html();
        Matcher matcher = Pattern.compile("[0-9]{2}/[0-9]{2}/[0-9]{4}").matcher(sanQiRiqi);
        if (matcher.find()){
            String[] dateArray = matcher.group().split("/");
            String dateStr = dateArray[2]+"-"+dateArray[1]+"-"+dateArray[0];
            System.out.println("上期开奖日期:"+dateStr);
            result.put("previousDate", dateStr);
        }

        //上期开奖结果
        Elements elementsKaijiangJieGuoSvg =  elementSanQiKaijiangDate.child(1).children();
        StringBuffer  sb = new StringBuffer();
        elementsKaijiangJieGuoSvg.iterator().forEachRemaining(e -> {
            String numStr = e.getElementsByTag("text").first().html();
            sb.append(numStr);
        });
        System.out.println("上期开奖号码:"+sb);
        result.put("previousDraw", sb);

        return result;
    }
}

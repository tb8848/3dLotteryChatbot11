package com.service;

import com.beans.LotteryMethod;
import com.beans.LotterySetting;
import com.util.Code3DCreateUtils;
import com.vo.BuyRecord3DVO;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DummyService {


    private  List<String> getNums(int len,String[] numsArr){
        if(len>0){
            Random random = new Random();
            Set<String> numSet = Sets.newHashSet();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                numSet.add(num);
                if(numSet.size()>=len){
                    break;
                }
            }
            List<String> numList = numSet.stream().collect(Collectors.toList());
            Collections.sort(numList);
            return numList;
        }else{
            return Lists.newArrayList();
        }
    }


    public List<BuyRecord3DVO> randomZx(LotteryMethod lm , LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0","1","2","3","4","5","6","7","8","9"};
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"};
        if(lsType==1){
            int numCount = random.nextInt(10);
            if(numCount==0){
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(10);
            if(numCount==0){
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String shi = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(10);
            if(numCount==0){
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String ge = numList.stream().collect(Collectors.joining());

            String hzname = "";
            int sum = bai.length() * shi.length() * ge.length();
            if(sum==1){
                hzname = "直选单式";
            }else{
                hzname = "直选复式";
            }
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBai(bai);
            oneRecord.setShi(shi);
            oneRecord.setGe(ge);
            oneRecord.setBuyAmount(sum);
            oneRecord.setValue(bai+","+shi+","+ge);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);

        }else if(lsType==2){
            int numCount = random.nextInt(hzArr.length);
            if(numCount==0){
                numCount=1;
            }

            List<String> hzList = getNums(numCount,numsArr);
            List<String> codeList = Code3DCreateUtils.hezhi(hzList);
            String hzname = "直选和值";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setHzList(hzList);
            oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            //和值
        }
        return recordList;
    }


    public List<BuyRecord3DVO> randomTx(LotteryMethod lm ,LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0","1","2","3","4","5","6","7","8","9"};
        int numCount = random.nextInt(10);
        if(numCount==0){
            numCount=1;
        }
        List<String> numList = getNums(numCount,numsArr);
        //普通
        String bai = numList.stream().collect(Collectors.joining());

        numList.clear();
        numCount = random.nextInt(10);
        if(numCount==0){
            numCount=1;
        }
        numList = getNums(numCount,numsArr);
        String shi = numList.stream().collect(Collectors.joining());

        numList.clear();
        numCount = random.nextInt(10);
        if(numCount==0){
            numCount=1;
        }
        numList = getNums(numCount,numsArr);
        String ge = numList.stream().collect(Collectors.joining());

        String hzname = "";
        int sum = bai.length() * shi.length() * ge.length();
        if(sum==1){
            hzname = "通选单式";
        }else{
            hzname = "通选复式";
        }
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(hzname);
        oneRecord.setBai(bai);
        oneRecord.setShi(shi);
        oneRecord.setGe(ge);
        oneRecord.setBuyAmount(sum);
        oneRecord.setValue(bai+","+shi+","+ge);
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId(String.valueOf(lsType));
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        recordList.add(oneRecord);
        return recordList;
    }


    public List<BuyRecord3DVO> randomZ3(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //组三普通
            int numCount = random.nextInt(10);
            if (numCount < 2) {
                numCount = 2;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z3Code(bai);

            for(String code : codeList){
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组三单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            }
            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = random.nextInt(10);
            if (numCount==0) {
                numCount = 1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String dm = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(10);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!dm.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String tm = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.z3DtCode(dm,tm);

            for(String code : codeList){
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组三单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            }
        }else if(lsType==3){
            //和值
            String[] hzArr = {"1","2","3","4","5","6","7","8","9","10","11",
                    "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};

            int numCount = random.nextInt(hzArr.length);
            if(numCount==0){
                numCount=1;
            }
            List<String> hzList = getNums(numCount,hzArr);
            List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);

            String hzname = "组三和值";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setHzList(hzList);
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomZ6(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //组三普通
            int numCount = random.nextInt(10);
            if (numCount < 3) {
                numCount = 3;
            }
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.z6CodeBy(bai);
            codeList.forEach(code -> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            });

            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = random.nextInt(10);
            if (numCount<2) {
                numCount = 2;
            }
            List<String> numList = getNums(numCount,numsArr);
            String dm = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(10);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!dm.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String tm = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.z6DtCode(dm,tm);

            codeList.forEach(code-> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("组六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            });
        }else if(lsType==3){
            //和值
            String[] hzArr = {"3","4","5","6","7","8","9","10","11",
                    "12","13","14","15","16","17","18","19","20","21","22","23","24"};
            int numCount = random.nextInt(hzArr.length);
            if(numCount==0){
                numCount=1;
            }
            List<String> hzList = getNums(numCount,hzArr);
            List<String> codeList = Code3DCreateUtils.z3hezhi(hzList);
            String hzname = "组六和值";
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setHzList(hzList);
            oneRecord.setValue(hzList.stream().collect(Collectors.joining(",")));
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomBx(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //包选3
            int numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            numList.clear();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!numList.contains(num)){
                    numList.add(num);
                }
                if(numList.size()>=numCount){
                    break;
                }
            }
            String shi = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount=1;
            }
            numList.clear();
            while(true){
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !shi.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if(numList.size()>=numCount){
                    break;
                }
            }
            String ge = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.createB3Code(bai,shi,ge);
            codeList.forEach(code -> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("包选三单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            });
            return recordList;
        }else if(lsType==2){
            //胆拖
            int numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String shi = numList.stream().collect(Collectors.joining());

            numList.clear();
            numCount = random.nextInt(3);
            if (numCount==0) {
                numCount = 1;
            }
            while (true) {
                String num = numsArr[random.nextInt(numsArr.length)];
                if(!bai.contains(num) && !shi.contains(num) && !numList.contains(num)){
                    numList.add(num);
                }
                if (numList.size() >= numCount) {
                    break;
                }
            }
            String ge = numList.stream().collect(Collectors.joining());
            List<String> codeList = Code3DCreateUtils.createB6Code(bai,shi,ge);

            codeList.forEach(code-> {
                String[] arr = code.split("");
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("包选六单式");
                oneRecord.setBai(arr[0]);
                oneRecord.setShi(arr[1]);
                oneRecord.setGe(arr[2]);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setValue(Arrays.stream(arr).collect(Collectors.joining(",")));
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            });
        }
        return recordList;
    }

    public List<BuyRecord3DVO> random1D(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //1D
            int numCount = random.nextInt(10);
            List<String> numList = getNums(numCount,numsArr);
            //普通
            String bai = "";
            String shi = "";
            String ge = "";
            if(numList.size()>0){
                bai = numList.stream().collect(Collectors.joining());
            }

            numCount = random.nextInt(10);
            numList = getNums(numCount,numsArr);
            if(numList.size()>0){
                shi = numList.stream().collect(Collectors.joining());
            }

            numCount = random.nextInt(10);
            numList = getNums(numCount,numsArr);
            if(numList.size()>0){
                ge = numList.stream().collect(Collectors.joining());
            }

            if(bai.length()>0) {
                int size = bai.length();
                String hzname = size == 1 ? "1D单式": "1D复式";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(hzname);
                oneRecord.setBuyAmount(size);
                oneRecord.setValue(bai + ",-,-");
                oneRecord.setBai(bai);
                oneRecord.setShi("-");
                oneRecord.setGe("-");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            }

            if(shi.length()>0) {
                int size = shi.length();
                String hzname = size == 1 ? "1D单式": "1D复式";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(hzname);
                oneRecord.setBuyAmount(size);
                oneRecord.setValue("-,"+shi + ",-");
                oneRecord.setBai("-");
                oneRecord.setShi(shi);
                oneRecord.setGe("-");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            }

            if(ge.length()>0) {
                int size = ge.length();
                String hzname = size == 1 ? "1D单式": "1D复式";
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(hzname);
                oneRecord.setBuyAmount(size);
                oneRecord.setValue("-,-,"+ge);
                oneRecord.setBai("-");
                oneRecord.setShi("-");
                oneRecord.setGe(ge);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            }
            return recordList;
        }else if(lsType==2){
            //猜1D
            int numCount = random.nextInt(10);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            int size = numList.size();
            String hzname = size == 1 ? "猜1D单式" : "猜1D复式";
            String value = numList.stream().collect(Collectors.joining());
            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBuyAmount(size);
            oneRecord.setValue(value);
            oneRecord.setBuyCode(value);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            return recordList;
        }
        return recordList;
    }

    public List<BuyRecord3DVO> random2D(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String[] numsArr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        if (lsType == 1) {
            //2D
            int numCount = random.nextInt(10);
            if (numCount==0) {
                numCount=1;
            }
            List<String> numList = getNums(numCount,numsArr);
            String bai = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(10);
            if (numCount==0) {
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String shi = numList.stream().collect(Collectors.joining());

            numCount = random.nextInt(10);
            if (numCount==0) {
                numCount=1;
            }
            numList = getNums(numCount,numsArr);
            String ge = numList.stream().collect(Collectors.joining());

            List<String> codeList = Code3DCreateUtils.ding2Code(bai,shi,ge);
            String hzname = codeList.size() == 1 ? "2D单式" : "2D复式";

            BuyRecord3DVO oneRecord = new BuyRecord3DVO();
            oneRecord.setHuizongName(hzname);
            oneRecord.setBai(bai);
            oneRecord.setShi(shi);
            oneRecord.setGe(ge);
            oneRecord.setBuyAmount(codeList.size());
            oneRecord.setValue(bai+","+shi+","+ge);
            oneRecord.setLmId(lm.getId());
            oneRecord.setLsTypeId(String.valueOf(lsType));
            oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
            recordList.add(oneRecord);
            return recordList;
        }else if(lsType==2){
            String hzname = "猜2D单式";
            //c2d
            int numCount = random.nextInt(10);
            if (numCount<2) {
                numCount = 2;
            }
            List<String> numList = getNums(numCount,numsArr);
            List<String> codeList = Code3DCreateUtils.c2dCode(numList.stream().collect(Collectors.joining()));
            codeList.forEach(code-> {
                BuyRecord3DVO oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName(hzname);
                oneRecord.setValue(code);
                oneRecord.setBuyAmount(1);
                oneRecord.setBuyCode(code);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
            });
        }
        return recordList;
    }

    //大小，奇偶，猜三同，拖拉机
    public List<BuyRecord3DVO> randomNoCodes(LotteryMethod lm ,LotterySetting ls) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        Integer lsType = ls.getTypeId();
        String lmId = lm.getId();
        BuyRecord3DVO oneRecord = null;
        switch(lmId){
            case "12":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("拖拉机单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("拖拉机");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
            case "11":
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("猜三同单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue("三同号");
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
            case "10":
                int num = random.nextInt(10);
                String value = "奇";
                if(num%2==0){
                    value = "偶";
                }
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("奇偶单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(value);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
            case "9":
                int num1 = random.nextInt(10);
                String value1 = "大";
                if(num1<5){
                    value1 = "小";
                }
                oneRecord = new BuyRecord3DVO();
                oneRecord.setHuizongName("大小单式");
                oneRecord.setBuyAmount(1);
                oneRecord.setValue(value1);
                oneRecord.setLmId(lm.getId());
                oneRecord.setLsTypeId(String.valueOf(lsType));
                oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
                recordList.add(oneRecord);
                break;
        }
        return recordList;
    }

    public List<BuyRecord3DVO> randomHs(LotteryMethod lm) {
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        Random random = new Random();
        //和值
        String[] hzArr = {"0","1","2","3","4","5","6","7","8","9","10","11",
                "12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27"};
        Set<String> numList = Sets.newHashSet();
        int numCount = random.nextInt(hzArr.length);
        if(numCount==0){
            numCount=1;
        }
        while(true){
            numList.add(hzArr[random.nextInt(hzArr.length)]);
            if(numList.size()>=numCount){
                break;
            }
        }

        String huizongName = numList.size()==1?"和数单式":"和数复式";
        BuyRecord3DVO oneRecord = new BuyRecord3DVO();
        oneRecord.setHuizongName(huizongName);
        oneRecord.setBuyAmount(numList.size());
        oneRecord.setHzList(numList.stream().collect(Collectors.toList()));
        oneRecord.setValue(numList.stream().collect(Collectors.joining(",")));
        oneRecord.setLmId(lm.getId());
        oneRecord.setLsTypeId("1");
        oneRecord.setBuyMoney(BigDecimal.valueOf(1+random.nextInt(10)));
        recordList.add(oneRecord);

        return recordList;
    }

    public List<BuyRecord3DVO> createDummyBuyRecords(LotteryMethod lm ,LotterySetting ls){
        List<BuyRecord3DVO> recordList = Lists.newArrayList();
        String lmId = lm.getId();
        switch(lmId){
            case "1": //直选
                recordList = randomZx(lm,ls);
                break;
            case "2":
                //通选
                recordList = randomTx(lm,ls);
                break;
            case "3":
                recordList = randomZ3(lm,ls);
                break;
            case "4":
                recordList = randomZ6(lm,ls);
                break;
            case "5":
                recordList = randomHs(lm);
                break;
            case "9":
            case "10":
            case "11":
            case "12":
                recordList = randomNoCodes(lm,ls);
                break;
            case "6":
                recordList = random1D(lm,ls);
                break;
            case "7":
                recordList = random2D(lm,ls);
                break;
            case "8":
                recordList = randomBx(lm,ls);
                break;
        }
        return recordList;
    }



}

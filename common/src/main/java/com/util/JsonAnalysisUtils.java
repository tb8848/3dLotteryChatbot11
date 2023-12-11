package com.util;

import java.util.List;

/**
 * 解析生成号码的json
 */
public class JsonAnalysisUtils {

//    //解析
//    public static String analysis(CodeRules rules, LotteryMethod lotteryMethod){
//
////        System.out.println(rules);
////
////        System.out.println("-------------------------");
//
//        String result = "";
//        if (lotteryMethod == null){
//            return null;
//        }
//        result = lotteryMethod.getBettingMethod();
//
//        //定位置
//        FixCodeRule fixCodeRule = rules.getFixCode();
//        if(null !=fixCodeRule && (fixCodeRule.getIncluded()==1 || fixCodeRule.getExcluded()==1)){
//            if(StringUtil.isNotNull(fixCodeRule.getLoc1()) || StringUtil.isNotNull(fixCodeRule.getLoc2())
//                    || StringUtil.isNotNull(fixCodeRule.getLoc3()) || StringUtil.isNotNull(fixCodeRule.getLoc4())){
//                String fixCodeResult = fixCodeRule.getName();//定位置
//                if (fixCodeRule.getIncluded() == 1){
//                    fixCodeResult = fixCodeResult + ":取,";
//                }else if (fixCodeRule.getExcluded() == 1){
//                    fixCodeResult = fixCodeResult + ":除,";
//                }
//                if (StringUtil.isNotNull(fixCodeRule.getLoc1())){
//                    fixCodeResult = fixCodeResult + "仟=<span style='color:red'>" + fixCodeRule.getLoc1() +"</span>,";
//                }
//                if (StringUtil.isNotNull(fixCodeRule.getLoc2())){
//                    fixCodeResult = fixCodeResult + "佰=<span style='color:red'>" + fixCodeRule.getLoc2() +"</span>,";
//                }
//                if (StringUtil.isNotNull(fixCodeRule.getLoc3())){
//                    fixCodeResult = fixCodeResult + "拾=<span style='color:red'>" + fixCodeRule.getLoc3() +"</span>,";
//                }
//                if (StringUtil.isNotNull(fixCodeRule.getLoc4())){
//                    fixCodeResult = fixCodeResult + "个=<span style='color:red'>" + fixCodeRule.getLoc4() +"</span>,";
//                }
//                result = result + " " + fixCodeResult;
//            }
//        }
//
//        //配数
//        if(rules.getLotteryMethodId().equals("5") || rules.getLotteryMethodId().equals("6") || rules.getLotteryMethodId().equals("7")){
//            MatchCodesRule matchCodesRule = rules.getMatchCodes();
//            if(null != matchCodesRule && (matchCodesRule.getIncluded()==1 || matchCodesRule.getExcluded()==1)){
//                String matchCodesResult = matchCodesRule.getName();//配数
//                if(matchCodesRule.getIsXian()==0){
//                    matchCodesResult = "配数全转";
//                }
//                if (matchCodesRule.getIncluded() == 1){
//                    matchCodesResult = matchCodesResult + ":取,";
//                }else if (matchCodesRule.getExcluded() == 1){
//                    matchCodesResult = matchCodesResult + ":除,";
//                }
//                if (StringUtil.isNotNull(matchCodesRule.getLoc1())){
//                    matchCodesResult = matchCodesResult + "第一位:<span style='color:red'>"+matchCodesRule.getLoc1()+"</span>,";
//                }
//                if (StringUtil.isNotNull(matchCodesRule.getLoc2())){
//                    matchCodesResult = matchCodesResult + "第二位:<span style='color:red'>"+matchCodesRule.getLoc2()+"</span>,";
//                }
//                if (StringUtil.isNotNull(matchCodesRule.getLoc3())){
//                    matchCodesResult = matchCodesResult + "第三位:<span style='color:red'>"+matchCodesRule.getLoc3()+"</span>,";
//                }
//                if (StringUtil.isNotNull(matchCodesRule.getLoc4())){
//                    matchCodesResult = matchCodesResult + "第四位:<span style='color:red'>"+matchCodesRule.getLoc4()+"</span>,";
//                }
//                result = result + " " + matchCodesResult;
//            }
//        }
//
//        //双重、三重、双双重
//        RepeatRuleComp repeatRuleComp = rules.getRepeat();
//        if (repeatRuleComp != null){
//            String repeatResult = "";
//            if (repeatRuleComp.getDouble1() != null && (repeatRuleComp.getDouble1().getIncluded()==1 || repeatRuleComp.getDouble1().getExcluded()==1)){
//                repeatResult = repeatResult + repeatRuleComp.getDouble1().getName();//双重
//                if (repeatRuleComp.getDouble1().getIncluded() == 1){
//                    repeatResult = repeatResult + ":取,";
//                }else if(repeatRuleComp.getDouble1().getExcluded() == 1){
//                    repeatResult = repeatResult + ":除,";
//                }
//            }
//            if (repeatRuleComp.getDouble2() != null && (repeatRuleComp.getDouble2().getIncluded()==1 || repeatRuleComp.getDouble2().getExcluded()==1)){
//                repeatResult = repeatResult + repeatRuleComp.getDouble2().getName();//双双重
//                if (repeatRuleComp.getDouble2().getIncluded() == 1){
//                    repeatResult = repeatResult + ":取,";
//                }else if (repeatRuleComp.getDouble2().getExcluded() == 1){
//                    repeatResult = repeatResult + ":除,";
//                }
//            }
//            if (repeatRuleComp.getTriple() != null && (repeatRuleComp.getTriple().getIncluded()==1 || repeatRuleComp.getTriple().getExcluded()==1)){
//                repeatResult = repeatResult + repeatRuleComp.getTriple().getName();//三重
//                if (repeatRuleComp.getTriple().getIncluded() == 1){
//                    repeatResult = repeatResult + ":取,";
//                }else if (repeatRuleComp.getTriple().getExcluded() == 1){
//                    repeatResult = repeatResult + ":除,";
//                }
//            }
//            result = result + " " + repeatResult;
//        }
//
//        //两兄弟、三兄弟、四兄弟
//        BrotherRuleComp brotherRuleComp = rules.getBrothers();
//        if (brotherRuleComp != null){
//            String brotherResult = "";
//            if (brotherRuleComp.getBr2() != null && (brotherRuleComp.getBr2().getIncluded()==1 || brotherRuleComp.getBr2().getExcluded()==1)){
//                brotherResult = brotherResult + brotherRuleComp.getBr2().getName();//两兄弟
//                if (brotherRuleComp.getBr2().getIncluded() == 1){
//                    brotherResult = brotherResult + ":取,";
//                }else if (brotherRuleComp.getBr2().getExcluded() == 1){
//                    brotherResult = brotherResult + ":除,";
//                }
//            }
//            if (brotherRuleComp.getBr3() != null && (brotherRuleComp.getBr3().getIncluded()==1 || brotherRuleComp.getBr3().getExcluded()==1)){
//                brotherResult = brotherResult + brotherRuleComp.getBr3().getName();//三兄弟
//                if (brotherRuleComp.getBr3().getIncluded() == 1){
//                    brotherResult = brotherResult + ":取,";
//                }else if (brotherRuleComp.getBr3().getExcluded() == 1){
//                    brotherResult = brotherResult + ":除,";
//                }
//            }
//            if (brotherRuleComp.getBr4() != null && (brotherRuleComp.getBr4().getIncluded()==1 || brotherRuleComp.getBr4().getExcluded()==1)){
//                brotherResult = brotherResult + brotherRuleComp.getBr4().getName();//四兄弟
//                if (brotherRuleComp.getBr4().getIncluded() == 1){
//                    brotherResult = brotherResult + ":取,";
//                }else if (brotherRuleComp.getBr4().getExcluded() == 1){
//                    brotherResult = brotherResult + ":除,";
//                }
//            }
//            result = result + " " + brotherResult;
//        }
//
//        //对数
//        PairsRule pairsRule = rules.getPairs();
//        if(null !=pairsRule && (pairsRule.getIncluded()==1 || pairsRule.getExcluded()==1)){
//            String pairsResult = pairsRule.getName();//对数
//            if (pairsRule.getIncluded() == 1){
//                pairsResult = pairsResult + ":取,";
//            }else if (pairsRule.getExcluded() == 1){
//                pairsResult = pairsResult + ":除,";
//            }
//            if (StringUtil.isNotNull(pairsRule.getPair1())){
//                pairsResult = pairsResult + "内容1=<span style='color:red'>"+pairsRule.getPair1()+"</span>,";
//            }
//            if (StringUtil.isNotNull(pairsRule.getPair2())){
//                pairsResult = pairsResult + "内容2=<span style='color:red'>"+pairsRule.getPair2()+"</span>,";
//            }
//            if (StringUtil.isNotNull(pairsRule.getPair3())){
//                pairsResult = pairsResult + "内容3=<span style='color:red'>"+pairsRule.getPair3()+"</span>,";
//            }
//            if (StringUtil.isNotNull(pairsRule.getPair4())){
//                pairsResult = pairsResult + "内容4=<span style='color:red'>"+pairsRule.getPair4()+"</span>,";
//            }
//            if (StringUtil.isNotNull(pairsRule.getPair5())){
//                pairsResult = pairsResult + "内容5=<span style='color:red'>"+pairsRule.getPair5()+"</span>,";
//            }
//            result = result + " " + pairsResult;
//        }
//
//        //单数
//        SingleNumRule singleNumRule = rules.getSingleNum();
//        if(null !=singleNumRule && (singleNumRule.getIncluded()==1 || singleNumRule.getExcluded()==1)){
//            String singleNumResult = singleNumRule.getName();//单
//            if (singleNumRule.getIncluded() == 1){
//                singleNumResult = singleNumResult + ":取,";
//            }else if (singleNumRule.getExcluded() == 1){
//                singleNumResult = singleNumResult + ":除,";
//            }
//            List<Integer> locArr = singleNumRule.getLocArr();
//            for (int i=0;i<locArr.size();i++){
//                if (locArr.get(i) == 1){
//                    singleNumResult = singleNumResult + "选中第"+(i+1)+"位,";
//                }
//            }
//            result = result + " " + singleNumResult;
//        }
//
//        //双数
//        DoubleNumRule doubleNumRule = rules.getDoubleNum();
//        if(null !=doubleNumRule && (doubleNumRule.getIncluded()==1 || doubleNumRule.getExcluded()==1)){
//            String doubleNumResult = doubleNumRule.getName();//双
//            if (doubleNumRule.getIncluded() == 1){
//                doubleNumResult = doubleNumResult + ":取,";
//            }else if (doubleNumRule.getExcluded() == 1){
//                doubleNumResult = doubleNumResult + ":除,";
//            }
//            List<Integer> locArr = doubleNumRule.getLocArr();
//            for (int i=0;i<locArr.size();i++){
//                if (locArr.get(i) == 1){
//                    doubleNumResult = doubleNumResult + "选中第"+(i+1)+"位,";
//                }
//            }
//            result = result + " " + doubleNumResult;
//        }
//
//        //大数
//        BigNumRule bigNumRule = rules.getBigNum();
//        if(null !=bigNumRule && (bigNumRule.getIncluded()==1 || bigNumRule.getExcluded()==1)){
//            String bigNumResult = bigNumRule.getName();
//            if (bigNumRule.getIncluded() == 1){
//                bigNumResult = bigNumResult + ":取,";
//            }else if (bigNumRule.getExcluded() == 1){
//                bigNumResult = bigNumResult + ":除,";
//            }
//            List<Integer> locArr = bigNumRule.getLocArr();
//            for (int i=0;i<locArr.size();i++){
//                if (locArr.get(i) == 1){
//                    bigNumResult = bigNumResult + "选中第"+(i+1)+"位,";
//                }
//            }
//            result = result + " " + bigNumResult;
//        }
//
//        //小数
//        SmallNumRule smallNumRule = rules.getSmallNum();
//        if(null !=smallNumRule && (smallNumRule.getIncluded()==1 || smallNumRule.getExcluded()==1)){
//            String smallNumResult = smallNumRule.getName();
//            if (smallNumRule.getIncluded() == 1){
//                smallNumResult = smallNumResult + ":取,";
//            }else if (smallNumRule.getExcluded() == 1){
//                smallNumResult = smallNumResult + ":除,";
//            }
//            List<Integer> locArr = smallNumRule.getLocArr();
//            for (int i=0;i<locArr.size();i++){
//                if (locArr.get(i) == 1){
//                    smallNumResult = smallNumResult + "选中第"+(i+1)+"位,";
//                }
//            }
//            result = result + " " + smallNumResult;
//        }
//
//        //两数和
//        Sum2Rule sum2Rule = rules.getSum2();
//        if(null !=sum2Rule && (sum2Rule.getIncluded()==1) && sum2Rule.getSumType()==2){
//            String sum2Result = "不定位合分:两数和,内容=<span style='color:red'>"+sum2Rule.getSum()+"</span>";
//            result = result + " " + sum2Result;
//        }
//        if(null !=sum2Rule && (sum2Rule.getIncluded()==1) && sum2Rule.getSumType()==3){
//            String sum2Result = "不定位合分:三数和,内容=<span style='color:red'>"+sum2Rule.getSum()+"</span>";
//            result = result + " " + sum2Result;
//        }
//
//        HefenRule hefenRule = rules.getHefen();
//
//
//        OthersRule othersRule = rules.getOthers();
//        if(StringUtil.isNotNull(othersRule.getShangJiang())){
//            String str = "上奖,内容=<span style='color:red'>"+othersRule.getShangJiang()+"</span>";
//            result = result + " " + str;
//        }
//        if(StringUtil.isNotNull(othersRule.getFullChange())){
//            String str = "全转,内容=<span style='color:red'>"+othersRule.getFullChange()+"</span>";
//            result = result + " " + str;
//        }
//        if(StringUtil.isNotNull(othersRule.getExcludes())){
//            String str = "排除,内容=<span style='color:red'>"+othersRule.getExcludes()+"</span>";
//            result = result + " " + str;
//        }
//
//        if(null!=othersRule.getLocArr()){
//            boolean needCheck = othersRule.getLocArr().stream().noneMatch(item->item==0);
//            if(needCheck){
//                String str = "乘号位置,";
//                List<Integer> list = othersRule.getLocArr();
//                for (int i=0,len=list.size();i<len;i++){
//                    if (list.get(i) == 1){
//                        str = str + "选中第<span style='color:red'>"+(i+1)+"</span>位,";
//                    }
//                }
//                result = result + " " + str;
//            }
//        }
//
//        FushiRule fushiRule = rules.getFushi();
//        if(StringUtil.isNotNull(fushiRule.getContains())){
//            String str = "含,内容=<span style='color:red'>"+fushiRule.getContains()+"</span>";
//            result = result + " " + str;
//        }
//        if(StringUtil.isNotNull(fushiRule.getCombines())){
//            String str = "复式,内容=<span style='color:red'>"+fushiRule.getCombines()+"</span>";
//            result = result + " " + str;
//        }
//
//
//        return result;
//
//
//    }
//
//


}

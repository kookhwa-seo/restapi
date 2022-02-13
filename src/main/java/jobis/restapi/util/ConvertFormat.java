package jobis.restapi.util;

public class ConvertFormat {
    public static String convertMoney(int money){
        StringBuffer sb = new StringBuffer();
        int tmp;
        int mm = money/10000;
        tmp = money%10000;
        int k = tmp/1000;
        tmp = money%1000;
        int m = tmp/100;
        tmp = money%100;
        int t = tmp/10;
        int n = tmp%10;

        if (mm > 0){
            sb.append(mm).append("만");
        }
        if (k > 0){
            sb.append(" ").append(k).append("천");
        }
        if (m > 0){
            sb.append(" ").append(m).append("백");
        }
        if (t > 0){
            sb.append(" ").append(t).append("십");
        }
        if (n > 0){
            sb.append(" ").append(n).append("원");
        }else{
            sb.append("원");
        }
        return sb.toString();
    }
}

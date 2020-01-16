package miscellaneous;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

	public static void main(String[] args) {
		//毫秒变字符串
//		long ms = System.currentTimeMillis() + 100000000l;
		long ms = 1578193228516l;
		ms2String(ms);
	}

	public static void ms2String(long millisecond) {
		Date date = new Date(millisecond);
//		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss SSS a"); //a是上午下午
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss SSS"); //HH是24小时制
		System.out.println("毫秒[" + millisecond + "]对应日期时间字符串：\n" + format.format(date));
	}

}

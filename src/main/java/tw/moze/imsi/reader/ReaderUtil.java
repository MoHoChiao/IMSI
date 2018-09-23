package tw.moze.imsi.reader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.JedisCluster;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.dev.XXX;

public class ReaderUtil {
	volatile static long max = 0;
	public static synchronized void setMaxDataTime(long fileTime) {
		max = Math.max(max, fileTime);
	}

	public static synchronized long getMaxDataTime() {
		return max;
	}

	private static Pattern p = Pattern.compile("data_(\\d+)_");

	/**
	 * DMS 來源檔，其資料時間一定小於檔名的 timestamp 時間，因此可以從檔名的 timestamp 中取時間
	 * @param path
	 * @return
	 */
	public static long getDMSFileTime(String path) {
		Matcher m = p.matcher(path);
		if (m.find()) {
			String v = m.group(1);
//			System.out.println("Good");
			return Long.valueOf(v);
		}
//		System.out.println("No Good");
		return new File(path).lastModified()/1000;
	}

	/**
	 * LSR 的來源檔，其資料時間不一定會小於檔名的 timestamp 時間，因此直接取檔案最後修改時間
	 * @param path
	 * @return
	 */
	public static long getLSRFileTime(String path) {
		return new File(path).lastModified()/1000;
	}
	public static void main(String[] args) {
		String path = "/data/imsi_mapping_cluster_v1/src/dms/s1ap_1_prb_MSP6000-12251_ipx_reports_data_1526844900_60_0_7.csv";
		System.out.println(getDMSFileTime(path));
		System.out.println(getLSRFileTime(path));
	}

	private static String DMS_LAST_DATA_TIMSSTAMP = "dms_last_data";

	public static void setRedisDataTime() {
		JedisCluster jedis = null;
        boolean done = false;
        SimpleDateFormat dfToSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int count = 5;
        do {
	        try {
	        	jedis = RedisUtil.getResource();
	            Long dmsDataTime = getMaxDataTime();
	            jedis.setex(DMS_LAST_DATA_TIMSSTAMP, 60 * 60 , String.valueOf(dmsDataTime));
	            XXX.out("Set DMS Data Time to: " + dfToSec.format(new Date(dmsDataTime * 1000L)));
	            done = true;
	        }
	        catch(Error e) {
	        	if (--count == 0) {
	        		XXX.out(String.format("Redis set value error:", e.toString()));
	        		e.printStackTrace();
	        		done = true;
	        	}
	        	else {
	        		try {
						Thread.sleep(3 * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	        	}
	        }
	        finally {
//	            if (jedis != null) {
//	                try {
//						jedis.close();
//					} catch (IOException e) {}
//	            }
	        }
        } while(!done);
    }

	/**
	 * 當 DNS 還沒有設定 DataTime 或無法取回 DataTime 時，會傳回 -1；此時呼叫端要特別處理
	 */
	public static Long getRedisDataTime() {
        JedisCluster jedis = null;
        boolean done = false;
        int count = 5;
        do {
	        try {
	        	jedis = RedisUtil.getResource();
	            String v = jedis.get(DMS_LAST_DATA_TIMSSTAMP);
	            if (v == null)
	            	return -1L;
	            return Long.valueOf(v);
	        }
	        catch(Throwable e) {
	        	if (--count == 0) {
	        		XXX.out(String.format("Redis get value error:", e.toString()));
	        		e.printStackTrace();
	        		done = true;
	        	}
	        	else {
	        		try {
						Thread.sleep(3 * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	        	}
	        }
	        finally {
//	            if (jedis != null) {
//	                try {
//						jedis.close();
//					} catch (IOException e) {}
//	            }
	        }
        } while(!done);

        return -1L;
    }
}

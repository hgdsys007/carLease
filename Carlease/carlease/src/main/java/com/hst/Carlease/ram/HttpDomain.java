package com.hst.Carlease.ram;

/**
 * 保存域名信息，主要由三部份组成
 * 
 * @author lmc 版权声明：版权归作者所有，未经协议授权不得使用、转载、抄袭等，否则依法追究法律责任和经济责任。
 */
public class HttpDomain {
	private static String protocol = "http://";
	private static String host = "rent.wisdom-gps.com";

	private static int port = 80;

	public static String getProtocol() {
		return protocol;
	}

	public static String getHost() {

		host = "jx.hstgps.com:";// 测试库

		return host;
	}

	public static int getPort() {
		port = 40050;// 测试库

		return port;
	}

	public static String getUrl() {
		/**
		 * APP测试路径地址已改： 内网： http://192.168.60.211:6211/ 外网：
		 * http://jx.hstgps.com:6211
		 */
//		return "http://jx.hstgps.com:6211";// 以租代购项目的地址
//		 return "http://interface.ystcar.net:86";//正式地址
//		 return "http://interface.ystcar.net:85";//
//		return "http://erpali.ystcar.net:85";
		return "http://test2.ystcar.net:89";//测试地址
//		return "http://192.168.80.5:99";
	}


}

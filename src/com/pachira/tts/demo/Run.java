package com.pachira.tts.demo;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class Run {
	private String serverName;
	
	public Run(String serverName) {
		setServerName(serverName);
	}
	
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public abstract void run(Map<String,List<String>> map) throws Exception;
	
	public abstract String desc();
	
	
	/**
	 * 关闭资源
	 */
	public static void close(Closeable... clses) {
		for(Closeable cls:clses) {
			if(cls != null) {
				try {
					cls.close();
					cls = null;
				}catch (Exception e) {}
			}
		}
	}
	
	public static void close(AutoCloseable... clses) {
		for(AutoCloseable cls:clses) {
			if(cls != null) {
				try {
					cls.close();
				}catch (Exception e) {}
			}
		}
	}
	
	public static boolean isEmpty(Object obj) {
		if(obj == null || "".equals(obj.toString().trim())) {
			return true;
		}
		return false;
	}
	
	public static boolean hasEmpty(Object... objs) {
		if(objs == null) {
			return true;
		}
		for(Object obj:objs) {
			if(isEmpty(obj)) {
				return true;
			}
		}
		return false;
	}
	
	public static String readInputStream(InputStream in,boolean close) {
		try {
			byte[] buff = new byte[1024];
			int len = 0;
			StringBuffer sb = new StringBuffer();
			while((len=in.read(buff)) != -1) {
				sb.append(new String(buff,0,len));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			if(close) {
				close(in);
			}
		}
	}
	
	
}

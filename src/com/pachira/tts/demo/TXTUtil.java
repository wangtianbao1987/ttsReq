package com.pachira.tts.demo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class TXTUtil {
	private String txtFileName;
	private Map<String,String> txtData;
	private static Map<String,TXTUtil> tus;
	
	protected TXTUtil(String txtFileName){
		this.txtFileName = txtFileName;
		if(this.loadTxt()){
			tus.put(txtFileName, this);
		}
	}
	
	public static void removeTxt(String txt){
		tus.remove(txt);
	}
	
	public static TXTUtil newInstance(String txtFileName){
		if(tus == null){
			tus = new HashMap<String,TXTUtil>();
		}
		TXTUtil pu = tus.get(txtFileName);
		if(pu == null){
			pu = new TXTUtil(txtFileName);
		}
		return pu;
	}
	
	public String getValue(String key){
		if(txtFileName == null){
			return null;
		}else{
			try {
				return txtData.get(key);
			} catch (Throwable e) {
				return null;
			}
		}
	}
	
	public Integer getIntVal(String key){
		String str = getValue(key);
		if(str == null){
			return null;
		}
		try {
			return Integer.parseInt(str.trim());
		} catch (Throwable e) {
			return null;
		}
	}

	public Long getLongVal(String key){
		String str = getValue(key);
		if(str == null){
			return null;
		}
		try {
			return Long.parseLong(str.trim());
		} catch (Throwable e) {
			return null;
		}
	}
	public Boolean getBooleanVal(String key){
		String str = getValue(key);
		if(str==null || "".equals(str)){
			return null;
		}
		str = str.toLowerCase();
		if("false".equals(str) || "0".equals(str)){
			return false;
		}
		if("true".equals(str) || "1".equals(str)) {
			return true;
		}
		return null;
	}
	
	public boolean getboolval(String key) {
		Boolean res = getBooleanVal(key);
		return res==null?false:res;
	}
	
	private boolean loadTxt(){
		try {
			if(txtData == null){
				txtData = new HashMap<String,String>();
			}else{
				txtData.clear();
			}
			loadData();
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	public void loadData() {
		BufferedReader br = null;
		InputStreamReader isr = null;
		InputStream in = null;
		try {
			in = TXTUtil.class.getResourceAsStream("/"+txtFileName);
			isr = new InputStreamReader(in,"UTF-8");
			br = new BufferedReader(isr);
			String str = null;
			while((str=br.readLine()) != null) {
				str = str.trim();
				if(str.startsWith("#")) {
					continue;
				}
				int index = str.indexOf("=");
				if(index != -1) {
					txtData.put(str.substring(0, index).trim(), str.substring(index+1).trim());
				}
			}
		} catch (Throwable e) {
			tus.remove(txtFileName);
		} finally {
			
			SourceUtils.close(br,isr,in);
		}
	}
	
	public void writeData() {
		
	}
	
	
	
}

package com.pachira.tts.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Main {
	public static final String PRE;
	public static List<Run> serverList = new ArrayList<Run>();
	static {
		PRE = "java -jar tts.jar";
		serverList.add(new TTS("tts"));
	}
	public static void main(String[] args) {
		args = args==null?new String[] {}:args;
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		
		String preKey = null;
		for(int i = 0;i < args.length;i++) {
			if(args[i] == null) {
				continue;
			}
			if(args[i].startsWith("-")) {
				preKey = args[i];
			}else if(preKey != null){
				if(map.get(preKey)==null) {
					List<String> vals = new ArrayList<String>();
					vals.add(args[i]);
					map.put(preKey, vals);
				}else {
					List<String> vals = map.get(preKey);
					vals.add(args[i]);
				}
			}
		}
		
		if(!map.isEmpty()) {
			Iterator<String> mapIt = map.keySet().iterator();
			System.out.println("传入参数：");
			while(mapIt.hasNext()) {
				String key = mapIt.next();
				List<String> vals = map.get(key);
				System.out.print(key);
				for(String val:vals) {
					System.out.println("\t"+val);
				}
			}
			System.out.println("======================================================");
		}
		List<String> serverName = map.get("-0");
		if(serverName == null) {
			System.out.println(tip());
			return;
		}
		boolean find = false;
		for(Run run:serverList) {
			if(serverName.get(0).equals(run.getServerName())) {
				try {
					find = true;
					run.run(map);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if(!find) {
			System.out.println(tip());
		}
	}
	
	
	private static String tip() {
		StringBuffer sb = new StringBuffer();
		sb.append(PRE).append(" -0 [serverName] ...\n");
		for(Run run:serverList) {
			String serverName = run.getServerName();
			sb.append("\t").append("[serverName]：").append(serverName).append("\n\t\t").append(run.desc()).append("\n-----------------------------------");
		}
		return sb.toString();
	}
	
	
}

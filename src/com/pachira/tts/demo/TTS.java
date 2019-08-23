package com.pachira.tts.demo;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.json.JSONObject;

public class TTS extends Run {
	private static byte[] bb = new byte[] {};
	private static int maxLen = 512;
	private String charset;
	private int port;
	private String filePath;
	private String searchStart;
	private String ip;
	
	public TTS(String serverName) {
		super(serverName);
	}

	@Override
	public void run(Map<String, List<String>> map) throws Exception {
		List<String> filePaths = map.get("-f");
		if(filePaths == null) {
			System.out.println(tip());
			return;
		}
		filePath = filePaths.get(0);
		
		charset = map.get("-c")==null?"UTF-8":map.get("-c").get(0);
		searchStart = map.get("-s")==null?null:map.get("-s").get(0);
		ip = map.get("-h")==null?"192.168.128.49":map.get("-h").get(0);
		port = map.get("-p")==null?8888:Integer.parseInt(map.get("-p").get(0));
		
		String volume = map.get("-v")==null?"1":map.get("-v").get(0);
		String speed = map.get("-d")==null?"1.6":map.get("-d").get(0);
		String pitch = map.get("-t")==null?"1":map.get("-t").get(0);
		String voice_name = map.get("-n")==null?"xiaochang":map.get("-n").get(0);
		String sample_rate = map.get("-r")==null?"16000":map.get("-r").get(0);
		String tag_mode = map.get("-g")==null?"0":map.get("-g").get(0);
		String eng_mode = map.get("-e")==null?"0":map.get("-e").get(0);
		List<String> filters = map.get("-k");
		
		File file = new File(filePath);
		if(!file.exists()) {
			System.out.println("找不到文件："+file.getAbsolutePath());
			System.out.println("******************************************************");
			System.out.println(tip());
		}
		
		BufferedReader br = null;
		InputStreamReader isr = null;
		FileInputStream fis = null;
		
		Map<String,String> param = new HashMap<String,String>();
		param.put("volume", volume);
		param.put("speed", speed);
		param.put("pitch", pitch);
		param.put("voice_name", voice_name);
		param.put("sample_rate", sample_rate);
		param.put("tag_mode", tag_mode);
		param.put("eng_mode", eng_mode);
		param.put("format", "pcm");
		param.put("language", "zh-cmn");
		SourceDataLine line = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis,charset);
			br = new BufferedReader(isr);
			String lineStr = null;
			boolean start = false;
			line = getSourceDataLine(512, Integer.parseInt(sample_rate),16);
			A:while((lineStr=br.readLine()) != null) {
				lineStr = lineStr.trim();
				if("".equals(lineStr)) {
					continue;
				}
				if(searchStart == null || "".equals(searchStart)) {
					start = true;
				}else if(lineStr.indexOf(searchStart) != -1) {
					start = true;
				}
				if(!start) {
					continue;
				}
				if(filters != null) {
					for(String filter:filters) {
						if(lineStr.indexOf(filter) != -1) {
							continue A;
						}
					}
				}
				param.put("text", lineStr);
				System.out.println(lineStr);
				String json = JSONObject.valueToString(param);
				try {
					bb = new byte[] {};
					reqTTS(line,json);
				}catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(line != null) {
					line.close();
				}
			}catch(Throwable e) {
				e.printStackTrace();
			}
			close(br,isr,fis);
		}
	}

	@Override
	public String desc() {
		return "TTS阅读";
	}
	
	private String tip() {
        StringBuffer sb = new StringBuffer();
        sb.append(Main.PRE).append(" -0 ").append(getServerName())
        	.append(" -f [txt文本路径] -s [开始朗读的字符串] -c [txt文本编码] -h [TTS服务器IP] -p [TTS服务器端口] -v [音量] -d [语速] -t [音调] -n [发音人] -r [采样率] -g [SSML] -e [英文读法] -k [过滤的行]\n")
        	.append("\t-f  txt文本路径，必须设置的项\n").append("\t-s  开始朗读的位置。通过搜索字符串，在指定位置开始播放\n").append("\t-c  tts文本文件的文件编码[默认UTF-8]\n")
        	.append("\t-h  TTS服务器IP地址/域名[默认192.168.128.49]\n").append("\t-p  TTS服务器端口[默认8888]\n").append("\t-v  音量[0~2。默认1]\n").append("\t-d  语速[0~2。默认1.6]\n")
        	.append("\t-t  音调[0~2。默认1]\n").append("\t-n  发音人[默认：xiaochang]\n").append("\t-r  采样率[16000/8000。默认：16000]\n").append("\t-g  是否使用SSML标记[0/1；默认0]\n")
        	.append("\t-e  英文读法[0/1；默认：0]\n").append("\t-k  过滤的行，通过搜索字符串过滤行，可输入多条过滤");
        return sb.toString();
    }
	
	
	public SourceDataLine getSourceDataLine(int bufferSize,float sampleRate,int sampleSizeInBits) {
		try {
			int channels = 1;
			boolean signed = true;
			boolean bigEndian = false;
			AudioFormat af = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, af, bufferSize);
			SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine(info);
			sdl.open(af);
			sdl.start();
			return sdl;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void reqTTS(SourceDataLine line,String body) {
		Socket socket = null;
		OutputStreamWriter osw = null;
		InputStream in = null;
		try {
			socket = new Socket(ip,port);
			osw = new OutputStreamWriter(socket.getOutputStream(),"utf-8");
			StringBuffer sb = new StringBuffer();
			sb.append("POST /voice/tts HTTP/1.1\r\n");
			sb.append("Host: "+ip+":"+port+"\r\n");
			sb.append("Connection: keep-alive\r\n");
			sb.append("Content-Length: "+body.getBytes("UTF-8").length+"\r\n");
			sb.append("\r\n");
			sb.append(body);
			osw.write(sb.toString());
			osw.flush();
			
			in = socket.getInputStream();
			
			String len = readLine(in);
			if(!len.contains("200")) {
				System.err.println("请求失败："+len);
				return;
			}
			
			String contentType = "";
			int contentLength = 0;
			while(!"".equals(len=readLine(in))) {
				if(len.startsWith("Content-Type")) {
					contentType = len;
				}else if(len.startsWith("Content-Length")) {
					contentLength = Integer.parseInt(len.split(":")[1].trim());
				}
			}
			if(contentType.contains("audio")) {
				while(!"0".equals(len = readLine(in))){
					if("".equals(len)) {
						continue;
					}
					int dataLen = Integer.parseInt(len, 16);
					int pos = bb.length;
					int shengyu = (dataLen+pos) % maxLen;
					byte[] buff = null;
					if(dataLen+pos-shengyu - maxLen < 0) {
						buff = new byte[(dataLen+pos-shengyu)/2*2];
						shengyu = (dataLen+pos-shengyu) % 2;
					}else {
						buff = new byte[dataLen+pos-shengyu];
					}
					
					if(shengyu > 0) {
						for(int i=0;i<bb.length;i++) {
							buff[i] = bb[i];
						}
					}
					int readLen = 0;
					while(pos < buff.length && -1 != (readLen=in.read(buff, pos, buff.length-pos))) {
						pos += readLen;
					}
					play(line,buff);
					
					bb = new byte[shengyu];
					pos = 0;
					while(pos < bb.length && -1 != (readLen=in.read(bb,pos,bb.length-pos))) {
						pos += readLen;
					}
				}
			}else {
				byte[] buff = new byte[contentLength];
				int pos = 0;
				int readLen = 0;
				while(pos < contentLength && -1 != (readLen=in.read(buff, pos, contentLength-pos))) {
					pos += readLen;
				}
				String errStr = new String(buff,"UTF-8").trim();
				System.err.println(errStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(in,osw,socket);
		}
	}
	
	public void play(SourceDataLine line,byte[] buff) {
		line.write(buff, 0, buff.length);
	}
	
	public void close(Object... objs) {
		if(objs == null) {
			return;
		}
		for(Object obj : objs) {
			if(obj == null) {
				continue;
			}
			try {
				if(obj instanceof Closeable) {
					((Closeable) obj).close();
				}else if(obj instanceof AutoCloseable) {
					((AutoCloseable) obj).close();
				}
			}catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public String readLine(InputStream in) throws Exception {
		byte[] buff = new byte[200];
		int len = 0;
		while(true) {
			int val = in.read();
			if(val == '\r') {
				in.read(); // \n
				break;
			}else if(val == 0){
				continue;
			}else {
				buff[len] = (byte)val;
				len++;
			}
		}
		return new String(buff,0,len);
	}
	
}

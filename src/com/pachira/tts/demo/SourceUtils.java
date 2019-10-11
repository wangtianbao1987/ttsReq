package com.pachira.tts.demo;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public final class SourceUtils {
	private final static void runMethod(String methodName,Object... objs){
		if(objs == null){
			return;
		}
		for(Object obj:objs){
			if(obj == null){
				return;
			}
			try {
				Method m = obj.getClass().getMethod(methodName);
				m.invoke(obj);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	public final static void write(InputStream in,OutputStream out){
		try {
			byte[] buff = new byte[1024*10];
			int len = 0;
			while((len=in.read(buff)) != -1){
				try {
					out.write(buff,0,len);
					out.flush();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	public final static void close(Object... objs){
		if(objs == null){
			return;
		}
		for(Object obj:objs){
			if(obj == null){
				return;
			}
			try {
				if(obj instanceof Closeable){
					((Closeable) obj).close();
				}else if(obj instanceof AutoCloseable){
					((AutoCloseable) obj).close();
				}else{
					runMethod("close", obj);
				}
			} catch (Throwable e) {
				// logging.warn("资源关闭失败:"+obj, e);
			}
		}
	}
	
	public final<T> void closeArr(T[] ts){
		for(T t:ts){
			try{
				if(t instanceof Closeable){
					((Closeable) t).close();
				}else if(t instanceof AutoCloseable){
					((AutoCloseable) t).close();
				}else{
					runMethod("close", t);
				}
			}catch(Throwable e){
				// logging.warn("资源关闭失败:"+t, e);
			}
		}
	}
	
	public final static void flush(Object... objs){
		if(objs == null){
			return;
		}
		for(Object obj:objs){
			if(obj == null){
				return;
			}
			try {
				if(obj instanceof OutputStream){
					((OutputStream) obj).flush();
				}else{
					runMethod("flush", obj);
				}
			} catch (Throwable e) {
				// logging.warn("数据刷新失败:"+obj, e);
			}
		}
	}
	
}

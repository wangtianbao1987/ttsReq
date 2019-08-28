package com.pachira.tts.demo;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.json.JSONObject;

public class MainFrame extends Run {
	private JButton fbtn, playBtn;
	private JTextField ffile, urlF,volumeF,speedF,pitchF,tagModeF,engModeF,formatF;
	private JComboBox<String> voiceNameBox,sampleRateBox,bitBox;
	private JTextArea area1,area2,area3;
	private Container c = null;
	private static byte[] bb = new byte[] {};
	private static int maxLen = 512;
	private static boolean playing = false;
	private SourceDataLine line;
	private String[] strs = new String[30];
	private int arrIndex = 0;
	public MainFrame(String serverName) {
		super(serverName);
	}
	
	@Override
	public void run(Map<String, List<String>> map) throws Exception {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");  			
		}catch (Exception e) {
			e.printStackTrace();
		}
		JFrame jf = new JFrame("TTS");
		jf.setSize(1000, 605);
		jf.setLocationRelativeTo(null);
		jf.setLayout(null);
		c = jf.getContentPane();

		ffile = new JTextField(20);
		ffile.setEnabled(false);
		fbtn = new JButton("选择...");
		fbtn.addActionListener(new ChoseFileListener(ffile, fbtn));
		urlF = new JTextField(20);
		volumeF = new JTextField(20);
		speedF = new JTextField(20);
		pitchF = new JTextField(20);
		voiceNameBox = new JComboBox<>(new String[] { "xiaoke", "xiaochang" });
		sampleRateBox = new JComboBox<>(new String[] { "8000", "16000" });
		bitBox = new JComboBox<>(new String[] { "16", "8" });
		tagModeF = new JTextField(20);
		engModeF = new JTextField(20);
		formatF = new JTextField(20);
		
		playBtn = new JButton("播放");
		playBtn.addActionListener(new PlayListener(playBtn));
		
		urlF.setText("http://192.168.128.49:8888/voice/tts");
		volumeF.setText("1");
		speedF.setText("1.3");
		pitchF.setText("1");
		tagModeF.setText("0");
		engModeF.setText("0");
		formatF.setText("pcm");
		
		area1 = new JTextArea(3,20);
		JScrollPane jsp1 = new JScrollPane(area1);
		jsp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		area2 = new JTextArea(3,20);
		JScrollPane jsp2 = new JScrollPane(area2);
		jsp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		area3 = new JTextArea(3,20);
		area3.setEditable(false);
		JScrollPane jsp3 = new JScrollPane(area3);
		area3.setLineWrap(true);
		jsp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		addItem(
			new MyComp(new JLabel("文件："),5,5,95,30),new MyComp(ffile,105,5,300,30),new MyComp(fbtn,410,5,80,30),
			new MyComp(new JLabel("地址："),5,40,95,30),new MyComp(urlF,105,40,385,30),
			new MyComp(new JLabel("音量："),5,75,95,30),new MyComp(volumeF,105,75,385,30),
			new MyComp(new JLabel("语速："),5,110,95,30),new MyComp(speedF,105,110,385,30),
			new MyComp(new JLabel("音调："),5,145,95,30),new MyComp(pitchF,105,145,385,30),
			new MyComp(new JLabel("发音人："),5,180,95,30),new MyComp(voiceNameBox,105,180,385,30),
			new MyComp(new JLabel("采样率："),5,215,95,30),new MyComp(sampleRateBox,105,215,385,30),
			new MyComp(new JLabel("采样深度："),5,250,95,30),new MyComp(bitBox,105,250,385,30),
			new MyComp(new JLabel("SSML："),5,285,95,30),new MyComp(tagModeF,105,285,385,30),
			new MyComp(new JLabel("英文读法："),5,320,95,30),new MyComp(engModeF,105,320,385,30),
			new MyComp(new JLabel("音频格式："),5,355,95,30),new MyComp(formatF,105,355,385,30),
			new MyComp(new JLabel("开始位置："),5,390,95,50),new MyComp(jsp1,105,390,385,50),
			new MyComp(new JLabel("过滤位置："),5,445,95,50),new MyComp(jsp2,105,445,385,50),
			new MyComp(playBtn, 410, 500, 80,30),new MyComp(jsp3,500,5,485,525)
		);
		
		jf.setVisible(true);
		jf.setResizable(false);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public String desc() {
		return "TTS Frame";
	}
	
	public void addItem(MyComp... comps) {
		for(MyComp comp:comps) {
			comp.comp.setBounds(comp.x, comp.y, comp.width, comp.height);
			c.add(comp.comp);
		}
	}
	
	public static void main(String[] args) throws Exception {
		new MainFrame("ttsframe").run(null);
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
	
	public void reqTTS(String body) {
		Socket socket = null;
		OutputStreamWriter osw = null;
		InputStream in = null;
		try {
			URI uri = new URI(urlF.getText());
			socket = new Socket(uri.getHost(),uri.getPort());
			osw = new OutputStreamWriter(socket.getOutputStream(),"utf-8");
			StringBuffer sb = new StringBuffer();
			sb.append("POST /voice/tts HTTP/1.1\r\n");
			sb.append("Host: "+uri.getHost()+":"+uri.getPort()+"\r\n");
			sb.append("Connection: keep-alive\r\n");
			sb.append("Content-Length: "+body.getBytes("UTF-8").length+"\r\n");
			sb.append("\r\n");
			sb.append(body);
			osw.write(sb.toString());
			osw.flush();
			
			in = socket.getInputStream();
			
			String len = readLine(in);
			if(!len.contains("200")) {
				text("请求失败："+len);
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
					play(buff);
					
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
				text(errStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(in,osw,socket);
		}
	}
	
	public void play(byte[] buff) {
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
	
	
	@SuppressWarnings("serial")
	class MyComp extends Component{
		Component comp;
		int x,y,width,height;
		MyComp(Component comp,int x,int y,int width,int height){
			this.comp = comp;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	class ChoseFileListener implements ActionListener{
		private JFileChooser chooser = new JFileChooser();
		JButton btn;
		JTextField field;
		ChoseFileListener(JTextField field,JButton btn){
			this.field = field;
			this.btn = btn;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			File file = null;
			chooser.setFileSelectionMode(JFileChooser.APPROVE_OPTION);
			int status = chooser.showOpenDialog(null);
			if (status == 1) {
				field.setText("");
			} else {
				file = chooser.getSelectedFile();
				field.setText(file.getAbsolutePath());
			}
		}
	}

	class PlayListener implements ActionListener{
		JButton btn;
		PlayListener(JButton btn){
			this.btn = btn;
		}
		@Override
		public void actionPerformed(ActionEvent ee) {
			if(playing) {
				playing = false;
				playBtn.setText("播放");
				try {
					line.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				playing = true;
				playBtn.setText("暂停");
				Map<String,String> param = new HashMap<String,String>();
				param.put("volume", volumeF.getText());
				param.put("speed", speedF.getText());
				param.put("pitch", pitchF.getText());
				param.put("voice_name", voiceNameBox.getItemAt(voiceNameBox.getSelectedIndex()));
				param.put("sample_rate", sampleRateBox.getItemAt(sampleRateBox.getSelectedIndex()));
				param.put("bit", bitBox.getItemAt(bitBox.getSelectedIndex()));
				param.put("tag_mode", tagModeF.getText());
				param.put("eng_mode", engModeF.getText());
				param.put("format", formatF.getText());
				param.put("language", "zh-cmn");
				
				new Thread() {
					public void run() {
						BufferedReader br = null;
						InputStreamReader isr = null;
						FileInputStream fis = null;
						try {
							line = getSourceDataLine(
								512,
								Integer.parseInt(sampleRateBox.getItemAt(sampleRateBox.getSelectedIndex())),
								Integer.parseInt(bitBox.getItemAt(bitBox.getSelectedIndex()))
							);
							fis = new FileInputStream(ffile.getText());
							isr = new InputStreamReader(fis,"UTF-8");
							br = new BufferedReader(isr);
							String lineStr = null;
							boolean start = false;
							A:while((lineStr=br.readLine()) != null) {
								lineStr = lineStr.trim();
								if("".equals(lineStr)) {
									continue;
								}
								String[] searchStarts = area1.getText().split("\n");
								for(String searchStart : searchStarts) {
									if(searchStart == null || "".equals(searchStart)) {
										start = true;
									}else if(lineStr.indexOf(searchStart) != -1) {
										start = true;
									}
								}
								if(!start) {
									continue;
								}
								
								String[] filters = area1.getText().split("\n");
								for(String filter : filters) {
									if(filter != null && !"".equals(filter)) {
										if(lineStr.indexOf(filter) != -1) {
											continue A;
										}
									}
								}
								
								param.put("text", lineStr);
								text(lineStr);
								String json = JSONObject.valueToString(param);
								try {
									bb = new byte[] {};
									reqTTS(json);
								}catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}catch (Exception e) {
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
					};
				}.start();
				
				
			}
		}
	}
	
	public void text(String text) {
		StringBuffer sb = new StringBuffer();
		for(int i=arrIndex;i<arrIndex+strs.length;i++) {
			String str = strs[i%strs.length];
			if(str == null) {
				continue;
			}
			sb.append("\n"+str+"\n");
		}
		arrIndex = (arrIndex+1) % strs.length;
		strs[arrIndex] = text;
		sb.append("\n"+text+"\n");
		
		String str = sb.substring(1);
		area3.setText(str);
		area3.setCaretPosition(str.length()); 
	}
}


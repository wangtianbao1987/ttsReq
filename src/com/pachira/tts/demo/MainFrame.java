package com.pachira.tts.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.json.JSONObject;

public class MainFrame extends Run {
	private JFrame jf;
	private JButton fbtn, playBtn, clearBtn, zhidingBtn, zhanBtn;
	private JTextField ffile, charsetF, urlF,volumeF,speedF,pitchF,tagModeF,engModeF,formatF,startF;
	private JComboBox<String> voiceNameBox,sampleRateBox,bitBox;
	private JScrollPane jsp2, jsp3;
	private JLabel[] jLabels;
	private JTextArea area2;
	private JTextPane area3;
	private Container c = null;
	private static byte[] bb = new byte[] {};
	private static int maxLen = 512;
	private static boolean playing = false;
	private SourceDataLine line;
	private String curText = "";
	private String preText = "";
	private int readedLen = 0;
	private boolean top = false;
	private boolean zhan = true;
	public MainFrame(String serverName) {
		super(serverName);
	}
	
	@Override
	public void run(Map<String, List<String>> map) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  			
		}catch (Exception e) {
			e.printStackTrace();
		}
		jf = new JFrame("TTS");
		jf.setSize(1000, 605);
		jf.setLocationRelativeTo(null);
		jf.setLayout(null);
		c = jf.getContentPane();

		ffile = new JTextField(20);
		ffile.setEnabled(false);
		fbtn = new JButton("选择...");
		fbtn.addActionListener(new ChoseFileListener(ffile, fbtn));
		charsetF = new JTextField(20);
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
		startF = new JTextField(20);
		
		playBtn = new JButton("播放");
		playBtn.addActionListener(new PlayListener(playBtn));
		
		clearBtn = new JButton("清理文本");
		clearBtn.addActionListener(new ClearListener(clearBtn));
		
		zhidingBtn = new JButton("置顶");
		zhidingBtn.addActionListener(new ZhidingListener(zhidingBtn));
		
		zhanBtn = new JButton("左侧隐藏");
		zhanBtn.addActionListener(new ZhanListener(zhanBtn));
		
		charsetF.setText("UTF-8");
		urlF.setText("http://192.168.128.49:8888/voice/tts");
		volumeF.setText("1");
		speedF.setText("1.3");
		pitchF.setText("1");
		tagModeF.setText("0");
		engModeF.setText("0");
		formatF.setText("pcm");
		
		area2 = new JTextArea(3,20);
		jsp2 = new JScrollPane(area2);
		jsp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		area3 = new JTextPane();
		area3.setEditable(false);
		jsp3 = new JScrollPane(area3);
		jsp3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		jLabels = new JLabel[] {
			new JLabel("文件："),new JLabel("文件编码："),new JLabel("URL地址："),new JLabel("音量："),
			new JLabel("语速："),new JLabel("音调："),new JLabel("发音人："),new JLabel("采样率："),
			new JLabel("采样深度："),new JLabel("SSML："),new JLabel("英文读法："),new JLabel("音频格式："),
			new JLabel("开始位置："),new JLabel("过滤位置：")
		};
		showLeftItems();
		int y = 460;
		addItem(
			new MyComp(zhanBtn, 140, y+=55, 80,30),new MyComp(zhidingBtn, 230, y, 80,30),
			new MyComp(clearBtn, 320, y, 80,30),new MyComp(playBtn, 410, y, 80,30)
		);
		
		jf.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event){
				try {
					ffile.getText();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
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
			while(playing && !"".equals(len=readLine(in))) {
				if(len.startsWith("Content-Type")) {
					contentType = len;
				}else if(len.startsWith("Content-Length")) {
					contentLength = Integer.parseInt(len.split(":")[1].trim());
				}
			}
			if(contentType.contains("audio")) {
				while(playing && !"0".equals(len = readLine(in))){
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
					while(playing && pos < buff.length && -1 != (readLen=in.read(buff, pos, buff.length-pos))) {
						pos += readLen;
					}
					play(buff);
					
					bb = new byte[shengyu];
					pos = 0;
					while(playing && pos < bb.length && -1 != (readLen=in.read(bb,pos,bb.length-pos))) {
						pos += readLen;
					}
				}
			}else {
				byte[] buff = new byte[contentLength];
				int pos = 0;
				int readLen = 0;
				while(playing && pos < contentLength && -1 != (readLen=in.read(buff, pos, contentLength-pos))) {
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
		while(playing) {
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
					line.stop();
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
						String readStr = null;
						try {
							line = getSourceDataLine(
									512,
									Integer.parseInt(sampleRateBox.getItemAt(sampleRateBox.getSelectedIndex())),
									Integer.parseInt(bitBox.getItemAt(bitBox.getSelectedIndex()))
									);
							File txtFile = new File(ffile.getText());
							if (!txtFile.isFile()) {
								throw new RuntimeException("文件找不到");
							}
							fis = new FileInputStream(txtFile);
							isr = new InputStreamReader(fis,charsetF.getText());
							br = new BufferedReader(isr);
							boolean start = false;
							String lineStr = null;
							A:while((lineStr=br.readLine()) != null) {
								if(!playing) {
									break A;
								}
								lineStr = lineStr.trim();
								if("".equals(lineStr)) {
									continue;
								}
								String searchStart = startF.getText();
								if(searchStart == null || "".equals(searchStart)) {
									start = true;
								}else if(lineStr.indexOf(searchStart) != -1) {
									start = true;
								}
								if(!start) {
									continue;
								}
								
								String[] filters = area2.getText().split("\n");
								for(String filter : filters) {
									if(filter != null && !"".equals(filter)) {
										if(lineStr.indexOf(filter) != -1) {
											continue A;
										}
									}
								}
								
								param.put("text", lineStr);
								readStr = lineStr;
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
							text("[ERROR] -> "+e.getMessage());
						} finally {
							startF.setText(readStr);
							try {if(line != null) {line.drain();}}catch(Throwable e) {}
							try {if(line != null) {line.stop();}}catch(Throwable e) {}
							try {if(line != null) {line.close();}}catch(Throwable e) {}
							close(br,isr,fis);
						}
					};
				}.start();
			}
		}
	}
	
	class ClearListener implements ActionListener{
		JButton btn;
		ClearListener(JButton btn){
			this.btn = btn;
		}
		@Override
		public void actionPerformed(ActionEvent ee) {
			clearText();
		}
	}
	
	class ZhidingListener implements ActionListener{
		JButton btn;
		ZhidingListener(JButton btn){
			this.btn = btn;
		}
		@Override
		public void actionPerformed(ActionEvent ee) {
			top = !top;
			jf.setAlwaysOnTop(top);
			if (top) {
				btn.setText("取消置顶");
			}else {
				btn.setText("置顶");
			}
		}
	}
	
	class ZhanListener implements ActionListener{
		JButton btn;
		ZhanListener(JButton btn){
			this.btn = btn;
		}
		@Override
		public void actionPerformed(ActionEvent ee) {
			zhan = !zhan;
			if (zhan) {
				btn.setText("左侧隐藏");
				showLeftItems();
			}else {
				btn.setText("展开");
				removeLeftItems();
			}
		}
	}
	
	private void removeLeftItems() {
		for(int i=0;i<jLabels.length;i++) {
			c.remove(jLabels[i]);
		}
		c.remove(ffile);
		c.remove(fbtn);
		c.remove(charsetF);
		c.remove(urlF);
		c.remove(volumeF);
		c.remove(speedF);
		c.remove(pitchF);
		c.remove(voiceNameBox);
		c.remove(sampleRateBox);
		c.remove(bitBox);
		c.remove(tagModeF);
		c.remove(engModeF);
		c.remove(formatF);
		c.remove(startF);
		c.remove(jsp2);
		c.remove(sampleRateBox);
		c.remove(sampleRateBox);
		c.remove(sampleRateBox);
		c.remove(jsp3);
		addItem(new MyComp(jsp3,5,5,485,500));
		jf.setSize(500, 605);
		c.repaint();
	}
	
	private void showLeftItems() {
		int y = 5;
		c.remove(jsp3);
		jf.setSize(1000, 605);
		addItem(
			new MyComp(jLabels[0],5,y,95,30),new MyComp(ffile,105,5,300,30),new MyComp(fbtn,410,5,80,30),
			new MyComp(jLabels[1],5,y+=35,95,30),new MyComp(charsetF,105,y,385,30),
			new MyComp(jLabels[2],5,y+=35,95,30),new MyComp(urlF,105,y,385,30),
			new MyComp(jLabels[3],5,y+=35,95,30),new MyComp(volumeF,105,y,385,30),
			new MyComp(jLabels[4],5,y+=35,95,30),new MyComp(speedF,105,y,385,30),
			new MyComp(jLabels[5],5,y+=35,95,30),new MyComp(pitchF,105,y,385,30),
			new MyComp(jLabels[6],5,y+=35,95,30),new MyComp(voiceNameBox,105,y,385,30),
			new MyComp(jLabels[7],5,y+=35,95,30),new MyComp(sampleRateBox,105,y,385,30),
			new MyComp(jLabels[8],5,y+=35,95,30),new MyComp(bitBox,105,y,385,30),
			new MyComp(jLabels[9],5,y+=35,95,30),new MyComp(tagModeF,105,y,385,30),
			new MyComp(jLabels[10],5,y+=35,95,30),new MyComp(engModeF,105,y,385,30),
			new MyComp(jLabels[11],5,y+=35,95,30),new MyComp(formatF,105,y,385,30),
			new MyComp(jLabels[12],5,y+=35,95,30),new MyComp(startF,105,y,385,30),
			new MyComp(jLabels[13],5,y+=35,95,50),new MyComp(jsp2,105,y,385,50),
			new MyComp(jsp3,500,5,485,y+80)
		);
		c.repaint();
	}
	
	private void clearText() {
		try {
			if (readedLen == 0) {
				return;
			}
			Document doc = area3.getStyledDocument();
			doc.remove(0, doc.getLength());
			preText = "";
			readedLen = 0;
			text(curText);
		} catch (Exception e) {
		}
	}
	
	public void text(String text) {
		try {
			Document doc = area3.getStyledDocument();
			int preTextLen = preText.length();
			if(preTextLen != 0) {
				doc.remove(readedLen-preTextLen-2, preTextLen+2);
				SimpleAttributeSet set = new SimpleAttributeSet();
				if (preText.matches("第.+章.+")) {
					StyleConstants.setFontSize(set, 25);
					StyleConstants.setForeground(set, Color.BLUE);
				}else {
					StyleConstants.setFontSize(set, 16);
					StyleConstants.setForeground(set, Color.BLACK);
				}
				doc.insertString(doc.getLength(), preText+"\n\n", set);
			}
			preText = text;
			curText = text;
			SimpleAttributeSet set = new SimpleAttributeSet();
			StyleConstants.setFontSize(set, 20);
			StyleConstants.setForeground(set, Color.RED);
			doc.insertString(doc.getLength(), text+"\n\n", set);
			readedLen = readedLen+text.length()+2;
			area3.setCaretPosition(readedLen);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		new MainFrame("ttsframe").run(null);
	}
}


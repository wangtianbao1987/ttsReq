package com.pachira.tts.demo;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener {
	private static Map<String, String> param = new HashMap<String, String>();
	private static File file;

	private JPanel PANEL = new JPanel(new GridLayout(11, 1));
	private JButton fbtn, playBtn;
	private JTextField ffile;
	private JFileChooser chooser = new JFileChooser();
	private boolean playing = false;

	public MainFrame() {
		this.setTitle("TTS");
		this.setSize(510, 520);
		this.setLocationRelativeTo(null);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.getContentPane().add(PANEL);

		ffile = new JTextField(20);
		fbtn = new JButton("选择...");
		fbtn.addActionListener(this);
		JTextField volumeF = new JTextField(20);
		JTextField speedF = new JTextField(20);
		JTextField pitchF = new JTextField(20);
		JComboBox<String> voiceNameBox = new JComboBox<>(new String[] { "xiaoke", "xiaochang" });
		JComboBox<String> sampleRateBox = new JComboBox<>(new String[] { "8000", "16000" });
		JComboBox<String> bitBox = new JComboBox<>(new String[] { "16", "8" });
		JTextField tagModeF = new JTextField(20);
		JTextField engModeF = new JTextField(20);
		JTextField formatF = new JTextField(20);
		
		playBtn = new JButton("播放");
		playBtn.addActionListener(this);
		
		volumeF.setText("1");
		speedF.setText("1.3");
		pitchF.setText("1");
		tagModeF.setText("0");
		engModeF.setText("0");
		formatF.setText("pcm");

		addItem(new JLabel("文件："),ffile,fbtn);
		addItem(new JLabel("音量："),volumeF);
		addItem(new JLabel("语速："),speedF);
		addItem(new JLabel("音调："),pitchF);
		addItem(new JLabel("发音人："),voiceNameBox);
		addItem(new JLabel("采样率："),sampleRateBox);
		addItem(new JLabel("采样深度："),bitBox);
		addItem(new JLabel("SSML："),tagModeF);
		addItem(new JLabel("英文读法："),engModeF);
		addItem(new JLabel("音频格式："),formatF);
		
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.add(playBtn);
		PANEL.add(bottomPanel);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setResizable(false);
	}
	
	public JPanel addItem(Component... comps) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for(Component comp:comps) {
			if(comp instanceof JLabel) {
				comp.setPreferredSize(new Dimension(100, 30));
			} else if (comp instanceof JComboBox) {
				comp.setPreferredSize(new Dimension(300, 30));
			} else if(comp instanceof JTextField) {
				((JTextField) comp).setColumns(27);
				comp.setPreferredSize(new Dimension(300, 30));
			}
			panel.add(comp);
		}
		PANEL.add(panel);
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fbtn) {
			file = null;
			chooser.setFileSelectionMode(0);
			int status = chooser.showOpenDialog(null);
			if (status == 1) {
				return;
			} else {
				file = chooser.getSelectedFile();
				ffile.setText(file.getAbsolutePath());
			}
		}else if(e.getSource() == playBtn) {
			playing = !playing;
			
		}
	}

	public static void main(String[] args) {
		new MainFrame();
	}
}

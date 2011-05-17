package com.raphfrk.craftproxyliter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class CraftProxyGUI extends JFrame implements WindowListener, ActionListener {
	private static final long serialVersionUID = 1L;

	JPanel topPanel = new JPanel();
	JPanel secondPanel = new JPanel();
	JPanel combinedTop = new JPanel();
	JTextField serverName;
	JTextField portNum;
	JPanel filePanel;
	JTextField currentSize;
	JTextField desiredSize;
	JTextField latencyBox;
	JLabel localServerName;
	JTextField localServerPortnum;
	JLabel info;
	JButton connect;

	final Object statusTextSync = new Object();
	String statusText = "";

	final Object buttonTextSync = new Object();
	String buttonText = "Start";

	Thread serverMainThread = null;

	public JFrame main;

	MyPropertiesFile pf;

	public CraftProxyGUI() {

		pf = new MyPropertiesFile("CraftProxyLiteGUI.txt");

		pf.load();
		
		String defaultHostname = pf.getString("connect_hostname", "localhost");
		int defaultPort = pf.getInt("connect_port", 20000);
		int listenPort = pf.getInt("listen_port", 25565);
		int desired = pf.getInt("cache_size", 48);
		int latency = pf.getInt("buffer_latency" , 0);

		setTitle("CraftProxyLiter Local Cache Mode - v" + VersionNumbering.version);
		setSize(450,325);
		setLocation(40,150);

		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(new TitledBorder("Remote Server"));
		topPanel.setBackground(Color.WHITE);
		secondPanel.setLayout(new BorderLayout());
		secondPanel.setBorder(new TitledBorder("Local Server"));
		secondPanel.setBackground(Color.WHITE);

		serverName = new JTextField(defaultHostname, 20);
		TitledBorder border = new TitledBorder("Name");
		serverName.setBorder(border);
		serverName.addActionListener(this);

		portNum = new JTextField(Integer.toString(defaultPort) , 6);
		border = new TitledBorder("Port");
		portNum.setBorder(border);
		portNum.addActionListener(this);

		localServerName = new JLabel("localhost");
		localServerName.setBackground(Color.GRAY);
		border = new TitledBorder("Name");
		localServerName.setBorder(border);

		localServerPortnum = new JTextField(Integer.toString(listenPort), 6);
		border = new TitledBorder("Port");
		localServerPortnum.setBorder(border);
		localServerPortnum.addActionListener(this);

		topPanel.add(serverName, BorderLayout.CENTER);
		topPanel.add(portNum, BorderLayout.LINE_END);

		secondPanel.setLayout(new BorderLayout());
		secondPanel.add(localServerName, BorderLayout.CENTER);
		secondPanel.add(localServerPortnum, BorderLayout.LINE_END);

		combinedTop.setLayout(new BorderLayout());
		combinedTop.add(topPanel, BorderLayout.CENTER);
		combinedTop.add(secondPanel, BorderLayout.SOUTH);
		
		currentSize = new JTextField("Unknown");
		currentSize.setBorder(new TitledBorder("Current Size (MB)"));
		currentSize.setEditable(false);
		
		desiredSize = new JTextField(Integer.toString(desired));
		desiredSize.setBorder(new TitledBorder("Max Size (MB)"));

		latencyBox = new JTextField(Integer.toString(latency));
		latencyBox.setBorder(new TitledBorder("Buffer latency (ms)"));
		
		connect = new JButton(buttonText);
		connect.addActionListener(this);
		
		filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		JPanel fileLinePanel = new JPanel();
		fileLinePanel.setBorder(new TitledBorder("Cache Size"));
		fileLinePanel.setLayout(new GridLayout(1,3));
		fileLinePanel.add(currentSize);
		fileLinePanel.add(desiredSize);
		fileLinePanel.add(latencyBox);
		filePanel.add(fileLinePanel, BorderLayout.CENTER);
		filePanel.add(connect, BorderLayout.PAGE_END);
		
		info = new JLabel();
		border = new TitledBorder("Status");
		info.setBorder(border);

		setLayout(new BorderLayout());
		add(combinedTop, BorderLayout.PAGE_START);
		add(info, BorderLayout.CENTER);
		add(filePanel, BorderLayout.PAGE_END);

		this.setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.addWindowListener(this);

	}

	public void safeSetStatus(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				info.setText(text);
			}
		});
	}

	public void safeSetButton(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connect.setText(text);
				connect.updateUI();
			}
		});
	}
	
	public void safeSetFileSize(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				currentSize.setText(text);
				currentSize.updateUI();
			}
		});
	}

	public void windowClosing(WindowEvent paramWindowEvent) {
		if(serverMainThread != null) {
			serverMainThread.interrupt();
		}
	}

	public void windowOpened(WindowEvent paramWindowEvent) {
	}

	public void windowClosed(WindowEvent paramWindowEvent) {
	}

	public void windowIconified(WindowEvent paramWindowEvent) {

	}

	public void windowDeiconified(WindowEvent paramWindowEvent) {
	}

	public void windowActivated(WindowEvent paramWindowEvent) {
	}

	public void windowDeactivated(WindowEvent paramWindowEvent) {
	}

	public void actionPerformed(ActionEvent action) {
		if(action.getSource().equals(connect)) {

			int desired = 48;
			int latency = 0;
			try {			
				pf.setString("connect_hostname", serverName.getText());
				pf.setInt("connect_port", Integer.parseInt(portNum.getText()));
				pf.setInt("listen_port", Integer.parseInt(localServerPortnum.getText()));
				try {
					desired = Integer.parseInt(desiredSize.getText());
				} catch (NumberFormatException nfe) {
					desired = 48;
					desiredSize.setText("48");
				}
				try {
					latency = Integer.parseInt(latencyBox.getText());
				} catch (NumberFormatException nfe) {
					latency = 0;
					latencyBox.setText("0");
				}
				pf.setInt("cache_size", desired);
				pf.setInt("buffer_latency", latency);
				desiredSize.setEditable(false);
				latencyBox.setEditable(false);
				pf.save();
			} catch (NumberFormatException nfe) {
			}

			if(serverMainThread == null || !serverMainThread.isAlive()) {

				safeSetButton("Stop");

				final String distantServer = serverName.getText() + ":" + portNum.getText();
				final String localServer = localServerPortnum.getText();
				final String cacheSize = Integer.toString(desired * 1024 * 1024);
				final String latencyString = Integer.toString(latency);

				serverMainThread = new Thread(new Runnable() {

					public void run() {

						String[] args = {
								localServer,
								distantServer,
								"local_cache",
								"quiet",
								"bridge_connection",
								"auth_off",
								"cache_limit",
								cacheSize,
								"bufferlatency",
								latencyString
						};

						Main.main(args, false);
					}

				});

				serverMainThread.start();

			} else {
				safeSetButton("Stopping");
				serverMainThread.interrupt();
				desiredSize.setEditable(true);
				latencyBox.setEditable(true);
			}
		}
	}


}

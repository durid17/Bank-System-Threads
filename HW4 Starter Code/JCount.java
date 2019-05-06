import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class JCount extends JPanel{
	
	private class Worker extends Thread{
		private final int change = 10000;
		private int toGet;
		private int now;
		public Worker(int toGet) {
			this.toGet = toGet;
			this.now = 0;
		}
		@Override
		public void run() {
			while(!isInterrupted()) {
				if(now == toGet) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							label.setText("" + now);
						}
					});
					try {
						sleep(100);
					} catch (InterruptedException e) { break; }
					break;
				}else if(now % change == 0) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							label.setText("" + now);
						}
					});
					try {
						sleep(100);
					} catch (InterruptedException e) { break; }
				}
				now++;
			}
		}
	}
	
	private JTextField field;
	private JButton start;
	private JButton stop;
	private JLabel label;
	private int toGet = 100000000;
	private Worker worker;
	private static final int NUM_OF_COUNTS = 4;
	
	
	public JCount() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		field = new JTextField(10);
		add(field);
		field.setText("" + toGet);
		start = new JButton("Start");
		add(start);
		stop  = new JButton("Stop");
		add(stop);
		label = new JLabel("0");
		add(label);
		worker = new Worker(toGet);
		worker.start();
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(worker.isAlive()) worker.interrupt();
				if(field.getText().length() == 0) return;
				worker = new Worker(Integer.parseInt(field.getText()));
				worker.start();
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(worker.isAlive()) worker.interrupt();
			}
		});
	}
	
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		for(int i = 0 ; i < NUM_OF_COUNTS; i++) {
			frame.add(new JCount());
			frame.add(Box.createRigidArea(new Dimension(0,40)));
		}
		
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	
}

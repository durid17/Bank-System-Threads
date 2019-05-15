import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat.Field;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class WebFrame extends JFrame{
	
	private DefaultTableModel model;
	private JTable table;
	private JButton single , concurent , stop;
	private JTextField numOfWorkers;
	private JLabel running , completed , elapsed;
	private JProgressBar bar;
	private List<String> urls;
	private Semaphore sem;
	private Luncher luncher;
	private CountDownLatch latch;
	private long startTime;
	private long endTime;
	
	private void reset() {
		concurent.setEnabled(true);
		single.setEnabled(true);
		stop.setEnabled(false);
	}
	
	public class Luncher extends Thread{
		private Thread[] threads;
		@Override
		public void run() {
			threads = new Thread[urls.size()];
			startThread();
			for(int i = 0 ; i < urls.size() ; i++) {
				try {
					sem.acquire();
					threads[i] = new WebWorker(urls.get(i) , this , i , latch);
					threads[i].start();
					if(isInterrupted()) {
						for(int j = 0 ; j < urls.size(); j++) {
							threads[j].interrupt();
						}
						break;
					}
				} catch (InterruptedException e) {
					finishThread();
					reset();
					return;
				}
			}
			try {
				latch.await();
				endTime = System.currentTimeMillis();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						elapsed.setText("Elapsed:" + (endTime - startTime));
					}
				});
			} catch (InterruptedException e) {}
			
			finishThread();
			reset();
		}
		
		public void allDone() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					bar.setValue(bar.getValue() + 1);
				}
			});
			sem.release();
		}
		
		public void startThread() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String s = running.getText();
					int ind = s.indexOf(':');
					int numThreads = Integer.parseInt(s.substring(ind + 1)) + 1;
					running.setText("Running:" + numThreads);
				}
			});
		}
		
		public void updateRow(String info , int index) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					table.setValueAt(info, index, 1);
				}
			});
		}
		
		public void finishThread() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String s = running.getText();
					int ind = s.indexOf(':');
					int numThreads = Integer.parseInt(s.substring(ind + 1)) - 1;
					running.setText("Running:" + numThreads);
					
					s = completed.getText();
					ind = s.indexOf(':');
					numThreads = Integer.parseInt(s.substring(ind + 1)) + 1;
					completed.setText("Completed:" + numThreads);
				}
			});
		}
		
	}
	
	
	public WebFrame() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,300));
		panel.add(scrollpane);
		String filename = "/home/demetre/Desktop/eclipse-workspace/hw4Threads/HW4 Starter Code/links.txt";
		urls = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(new File(filename));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				model.addRow(new Object[] {line , ""});
				urls.add(line);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		latch = new CountDownLatch(urls.size());
		Box box = Box.createVerticalBox();
		
		single = new JButton("Single Thread Fletch");
		single.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startDownloads(1);
			}
		});
		box.add(single);
		
		
		concurent = new JButton("Concurent Fletch");
		concurent.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int numThreads = 0;
				if(numOfWorkers.getText().length() == 0) {
					numThreads = 1;
				}else numThreads = Integer.parseInt(numOfWorkers.getText());
				startDownloads(numThreads);
			}
		});
		box.add(concurent);
		
		
		numOfWorkers = new JTextField();
		numOfWorkers.setMaximumSize(new Dimension(50 , 50));
		box.add(numOfWorkers);
		running = new JLabel("Running:0");
		box.add(running);
		completed = new JLabel("Completed:0");
		box.add(completed);
		elapsed = new JLabel("Elapsed:0");
		box.add(elapsed);
		
		bar = new JProgressBar();
		box.add(bar);
		
		stop = new JButton("Stop");
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				concurent.setEnabled(true);
				single.setEnabled(true);
				stop.setEnabled(false);
				luncher.interrupt();
			}
		});
		box.add(stop);
		stop.setEnabled(false);

		panel.add(box);
		add(panel);
		
		setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	private void startDownloads(int numThreads) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for(int i = 0 ; i < model.getRowCount() ; i++) {
					model.setValueAt("", i, 1);
				}
				completed.setText("Completed:0");
				elapsed.setText("Elapsed:0");
				bar.setValue(0);
			}
		});
		concurent.setEnabled(false);
		single.setEnabled(false);
		stop.setEnabled(true);
		bar.setMaximum(urls.size());
		sem = new Semaphore(numThreads);
		startTime = System.currentTimeMillis();
		luncher = new Luncher();
		luncher.start();
	}
	
	public static void main(String[] args) {
		WebFrame frame = new WebFrame();
	}
}

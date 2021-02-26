
/***********************
************************
**   ~ NETWORKS-1 ~   **
************************
************************/		

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import ithakimodem.Modem;


class multiThreadApp extends Thread {
	
	private static final int speed = 80000;
	
	private Thread t;
	private String id;
	private String request;
	private Modem modem;
	
	multiThreadApp(String threadName, String reqCode, Modem m){
		id = threadName;
		request = reqCode;
		modem = m;
		System.out.println("Thread "+ id +" ready!");
	}
	
	
	public void run() {
		
		modem.open("ithaki");
		ArrayList<Byte> image = new ArrayList<>();
		boolean start = false;
		byte recv, prev;
		
		modem.write(request.getBytes());
		recv = (byte)modem.read();
		prev = recv;
		System.out.println("Thread "+ id +" running...\n");
		
		for(;;)
		{
			if( start ) {
				try {
					recv = (byte)modem.read();
					if(recv == -39 && prev == -1) {
						image.add(recv);
						System.out.println("ok!");
						break;
					}
					else prev = recv;
					image.add(recv);
					
				} catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			else {
				recv = (byte)modem.read();
				if(recv == -40 && prev == -1) {
					start = true;
					image.add( (byte) -1  );
					image.add( (byte) -40 );
				}
				else prev = recv;
			}
		}
		System.out.println("Thread "+ id +" done!");
	}
	
	
	public void start() {
		if (t == null) {
	         t = new Thread(this, id);
	         t.start ();
	      }
	}
}



public class userApplication {
	
	
	public static final int speed = 80000;
	
	
	public static Modem modemInit() {
			
		Modem modem = new Modem();
		modem.setSpeed(speed);
		modem.setTimeout(5000);
		modem.open("ithaki");
		return modem;
	}
	
	
	
	public static void save2file(double[] arr, String filename) {
		
		String fpath = "C:\\Users\\User\\Documents\\ElectrEng&CompSc\\Networks-1\\"
													+ filename + ".txt";
		try {
			BufferedWriter data = new BufferedWriter(new FileWriter(fpath));
			
			for(int i = 0 ; i < arr.length; i++) {
				if(arr[i] == 0)break;
				String x = String.valueOf(arr[i]);
				data.write(x);
				data.newLine();
			}
			data.flush();
			data.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void saveNdisplay(ArrayList<Byte> image) throws IOException {
		
		int len = image.size();
		JFrame frame = new JFrame("Ithaki Cam");
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel label = new JLabel("Loading...");
		frame.add(label, BorderLayout.SOUTH);
		frame.setVisible(true);
		
		byte[] imageBytes = new byte[len];
		for(int i = 0; i < len; i++) imageBytes[i] = image.get(i);
		
		InputStream input = new ByteArrayInputStream(imageBytes);
		BufferedImage img = ImageIO.read(input);
		
		JLabel pic = new JLabel(new ImageIcon(img));
		label.setText("Done!");
		frame.add(pic, BorderLayout.CENTER);
		
		ImageIO.write(img, "JPEG", new File("C:\\Users\\User\\Pictures\\Ithaki\\ithaki.JPEG"));
		System.out.println("Image saved successfully!");
	}
	
	
	
	public static String EchoRecv(String reqCode, double dur_min, boolean save) {
		
		int recv, cnt = 0;
		long t0, t1;
		double []respTime = new double[1000]; 
		double limit = dur_min * 60000;
		String message = "";
		
		Modem modem  = modemInit();
		long t_start = System.currentTimeMillis();
		
		for(;System.currentTimeMillis() < t_start + limit;)
		{
			modem.write(reqCode.getBytes());
			t0 = System.currentTimeMillis();
			while( true )
			{	
				try {
					recv = modem.read();
					if(recv == -1) break;
					message += ( (char)recv );
				} 
				catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			t1 = System.currentTimeMillis();
			System.out.println(message);
			if( save ) respTime[cnt++] = (double)( t1 - t0 );
			message = "";
		}
		
		if( save ) save2file(respTime, "response_time");
		modem.close();
		
		return message;
	}
	
	
	
	public static void ImageRecv(String reqCode) throws IOException {
		
		ArrayList<Byte> image = new ArrayList<>();
		boolean start = false;
		byte recv, prev;
		
		Modem modem = modemInit();
		modem.write(reqCode.getBytes());
		
		recv = (byte)modem.read();
		prev = recv;
		System.out.println("Downloading image...\n");
		
		while( true )
		{
			if( start ) 
			{
				try {
					recv = (byte)modem.read();
					if(recv == -39 && prev == -1) {
						image.add(recv);
						break;
					}
					else prev = recv;
					image.add(recv);
					
				} catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			else 
			{
				recv = (byte)modem.read();
				if(recv == -40 && prev == -1) {
					start = true;
					image.add( (byte) -1  );
					image.add( (byte) -40 );
				}
				else prev = recv;
			}
		}

		saveNdisplay(image);
		modem.close();
	}
	
	
	
	public static void GpsLocationRecv(String reqCode, int numtracks) throws IOException {
		
		int recv, offset = 25, messlen = 76;
		boolean start  = false;
		String locInfo = "", help = "", str, str2, ampl, len, imreq = "";
		
		Modem modem = modemInit();
		modem.write(reqCode.getBytes());
		System.out.println("Waiting for satellite response...\n");
		
		while( true )
		{
			if( start ) 
			{
				try {
					recv = modem.read();
					if(recv == -1) {
						System.out.println("Connection terminated!\n");
						break;
					}
					
					locInfo += (char)recv;
					
				} catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			else
			{
				recv = modem.read();
				help += (char)recv;
				if(help.indexOf("START") > 0) {
					locInfo += "START";
					start   = true;
				}
			}
		}
		
		for(int i = 0; i < numtracks; i++) {
			
			str  = locInfo.substring(offset+i*messlen+25,offset+i*messlen+29);
			str2 = locInfo.substring(offset+i*messlen+38,offset+i*messlen+42);
			ampl = String.valueOf((int)(Integer.parseInt(str ) * 0.006));
			len  = String.valueOf((int)(Integer.parseInt(str2) * 0.006));
			
			str  = locInfo.substring(offset+i*messlen+33,offset+i*messlen+37);
			str2 = locInfo.substring(offset+i*messlen+20,offset+i*messlen+24);
			imreq += "T=" + str + len + str2 + ampl; 
		}
		
		modem.close();
		System.out.println(locInfo);
		
		String newcode = reqCode.substring(0,5) + imreq + "\r";
		ImageRecv(newcode);
	}
	
	
	
	public static void AutoRepeatRequest(String ACK, String NACK, double dur_min, boolean save) {
		
		long t0, time     = 0;
		String message    = "";
		double[] respTime = new double[1000];
		double limit      = dur_min * 60000;
		int recv, npackets= 0, err = 0, cnt = 0;
		
		Modem modem = modemInit();
		modem.write( ACK.getBytes() );
		long t_start = System.currentTimeMillis();
		
		for(;System.currentTimeMillis() < t_start + limit;) 
		{
			t0 = System.currentTimeMillis();
			while( true ) 
			{	
				try {
					recv = modem.read();
					if(recv == -1) break;
					message += (char)recv;
				} 
				catch(Exception e) {
					e.printStackTrace();
					break;
				}
			}
			npackets ++;
			time = System.currentTimeMillis() - t0;
			
			if( DetectErr(message) ) {
				if(save) respTime[cnt++] += (double)( time );
				modem.write( ACK.getBytes() );
			}
			else {
				if(save) respTime[cnt] += (double)( time );
				modem.write( NACK.getBytes() );
				err ++;
			}
			message = "";
		}
		
		if(save) save2file(respTime, "responseTime");
		float BitErrorRate = (float)( err ) / (float)( npackets );
		System.out.println("\n~ Packets: "+npackets+"\n~ Errors : "
									+err+"\n~ B.E.R  : "+BitErrorRate);
		modem.close();
	}
	
	
	
	public static boolean DetectErr(String message) {
		
		boolean corr = false;
		
		String str1  = isolate(message, "<", ">");
		String str2  = isolate(message, "> ", " P");
		
		int testcode = XOR(str1);
		int truecode = Integer.parseInt(str2);
		System.out.println(message+"\nTrue: "+truecode+"\nTest: "+testcode);
		
		corr = (truecode == testcode) ? (true) : (false);
		return corr;
	}
	
	
	
	public static int XOR(String mes) {
		
		int ans = mes.charAt(0);
		
		for(int i = 1; i < mes.length(); i++) 
			ans = ( ans ^ (mes.charAt(i)) );
		
		return ans;
	}
	
	
	
	public static String isolate(String str, String start, String end) {
		
		String[] div1 = str.split(start, 2);
		return ( div1[1].split(end, 2) )[0];
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String rcode = "M2560\r",
			   ACK   = "Q4783\r",
			   NACK  = "R4853\r",
			   tid;
		int ntracks  = 5,
			nthrds   = 2;
		double tdur  = 1.5;
		
		
	/*	Modem modem = new Modem();
		modem.setSpeed(speed);
		modem.setTimeout(5000);
		
		multiThreadApp[] threads = new multiThreadApp[ nthrds ];
		
		for(int i = 0; i < nthrds; i++) {
			
			tid = String.valueOf(i);
			threads[i] = new multiThreadApp(tid, rcode, modem);
			if(i>=1)Thread.sleep(900);
			threads[i].start();
		} */
		
		
		
		//AutoRepeatRequest(ACK, NACK, tdur, false);
		//GpsLocationRecv(rcode, ntracks); //R=1003005
		//ImageRecv(rcode);
		//EchoRecv(rcode, tdur, false);
	}
		
}


package com.SampleCanvas;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;





import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SampleCanvasActivity extends Activity implements OnTouchListener {

	DrawPanel dp;
	Bitmap bmp;
	StartServer sts;
	int test=100;
	int[] fb_buf=new int[1000000];
	package_fb pkg_fb;
	class package_fb{
		int x;
		int y;
		int right;
		int bottom;
		int line_width;
		int index;
		int buf[]=new int[1000000];
		void draw()
		{
			int i=0;
			for(int iy=y;iy<=bottom;iy++)
			{
				for(int ix=x;ix<=right;ix++)
				{
					SampleCanvasActivity.this.fb_buf[iy*line_width+ix]=buf[i++];
				}
			}
			
		}
	}
	private ArrayList<Path> pointsToDraw = new ArrayList<Path>();
	private Paint mPaint;
	Path path;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		colors=new int[320*240*3];
		
		//bmp= Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565);
		dp = new DrawPanel(this); 
		
		dp.setOnTouchListener(this);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mPaint = new Paint();
		mPaint.setDither(false);
//		mPaint.setColor(Color.WHITE);
//		mPaint.setStyle(Paint.Style.STROKE);
//		mPaint.setStrokeJoin(Paint.Join.ROUND);
//		mPaint.setStrokeCap(Paint.Cap.ROUND);
//		mPaint.setStrokeWidth(30);
		FrameLayout fl = new FrameLayout(this);  
		fl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
		fl.addView(dp);  
		setContentView(fl);  
		
		pkg_fb=new package_fb();
		
		 sts=new StartServer();
		 sts.execute(8090);
		 
		 
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		dp.pause();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		dp.resume();
	}

	public boolean onTouch(View v, MotionEvent me) {
		// TODO Auto-generated method stub
		synchronized(pointsToDraw)
		{
			if(me.getAction() == MotionEvent.ACTION_DOWN){
				path = new Path();
				path.moveTo(me.getX(), me.getY());
				//path.lineTo(me.getX(), me.getY());
				pointsToDraw.add(path);
			}else if(me.getAction() == MotionEvent.ACTION_MOVE){
				path.lineTo(me.getX(), me.getY());
			}else if(me.getAction() == MotionEvent.ACTION_UP){
				//path.lineTo(me.getX(), me.getY());
			}
		}       
		return true;
	}

	int[] colors;
	boolean update;
	public class DrawPanel extends SurfaceView implements Runnable{

		Thread t = null;
		SurfaceHolder holder;
		boolean isItOk = false ;

		public DrawPanel(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			holder = getHolder();
			
		}

		public void run() {
			// TODO Auto-generated method stub
			while( isItOk == true){

				if(!holder.getSurface().isValid()){
					continue;
				}
				if(!update){
					continue;
				}
				else {
					update=false;
					Canvas c = holder.lockCanvas();
//					
//					c.drawARGB(255, 0, 0, 0);
					
					onDraw(c);
					holder.unlockCanvasAndPost(c);
				}
//

		}
		}
		int color;
		int x=0,y=0,width=0,bottom=0;
		
		int log_cnt=0;
		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			super.onDraw(canvas);
			canvas.drawBitmap(fb_buf, 0,320,  0, 0, 320, 240, false, null);
			
				//update=false;
			
			

		}
		public void pause(){
			isItOk = false;
			while(true){
				try{
					t.join();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				break;
			}
			t = null;
		}

		public void resume(){
			isItOk = true;  
			t = new Thread(this);
			t.start();

		}
	}
	
	
	
	
	
	EditText mPortEntry = null;
	TextView mClientSays = null;
	Button mServerSendButton = null;
	EditText mMessageText = null;
	Integer mPort = null;
	boolean mConnected = false;
	DatagramSocket mClient = null;
	DatagramPacket mClientIP = null;
	
	
	private class StartServer extends AsyncTask<Integer, String, DatagramSocket>
	{
		
		DatagramSocket s = null;
		//DrawPanel dpp;
		boolean started=false; 	
		int preIndex=0;
		int preCode=0;
		boolean bak;
		byte[] message = new byte[40000];
		
		byte[] message_bak=new byte[1];
		Boolean first_pkg=true;
		

		protected void onPreExecute()
		{
			
		}

		protected int rgb565to888(int rgb)
		{
			int retval=((rgb&0xf800)<<8)+((rgb&0x7e0)<<5)+((rgb&0x1f)<<3);			
			return retval;
		}
		
		protected void pkg_handle(byte[] pkg,int code,int start_flag,int index,int lenth)
		{
			if(code==0)
			{
				if(start_flag==1)
				{
					if(!started)
						started=true;
					if(lenth!=30004)
					{
						Log.e("miao","lenth error");
					}
					
					int len=(lenth-24)/2;
					ByteBuffer bf=ByteBuffer.wrap(pkg);
					short[] pkg_short;			
					pkg_short=new short[lenth/2];
					
					bf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pkg_short, 0, lenth/2);	
					
					pkg_fb.x=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(1);					
					pkg_fb.y=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(2);
					pkg_fb.right=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(3);
					pkg_fb.bottom=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(4);
					pkg_fb.line_width=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(5);

					for(int i=0;i<len;i++)
						pkg_fb.buf[i]=rgb565to888(pkg_short[i+12]);
					
					pkg_fb.index=len;
					Log.i("miao","x="+pkg_fb.x+"y="+pkg_fb.y+"right="+pkg_fb.right+"bottom"+pkg_fb.bottom);
				}
				else if(start_flag==2)
				{
					started=false;
					//24:4byte state+20byte fb_info
					int len=(lenth-24)/2;
					ByteBuffer bf=ByteBuffer.wrap(pkg);
					short[] pkg_short;
					
					pkg_short=new short[lenth/2];
					
					bf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pkg_short, 0, lenth/2);	
					
					//Log.i("miao", "lenth="+len);
					SampleCanvasActivity.this.update=true;
					pkg_fb.x=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(1);					
					pkg_fb.y=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(2);
					pkg_fb.right=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(3);
					pkg_fb.bottom=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(4);
					pkg_fb.line_width=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(5);
					
//					int i=0;
//					for(int iy=y;iy<=bottom;iy++)
//					{
//						for(int ix=x;ix<=right;ix++)
//						{
//							SampleCanvasActivity.this.fb_buf[iy*line_width+ix]=rgb565to888(pkg_short[i+12]);
//							i++;
//						}
//					}
					
					for(int i=0;i<len;i++)
						pkg_fb.buf[i]=rgb565to888(pkg_short[i+12]);//12:24byte=12short
					
					pkg_fb.draw();
					
					
					
//					Log.i("miao","pkg_short="+pkg_short[12]);
//					Log.i("miao","buf="+fb_buf[0]);
					Log.i("miao","x="+pkg_fb.x+"y="+pkg_fb.y+"right="+pkg_fb.right+"bottom"+pkg_fb.bottom);
						
				}
				else {
					Log.e("miao", "err start_flag"+start_flag);
				}
			}
			else{
				if(start_flag==0)
				{
					if(!started)
					{
						return ;
					}
					int len=(lenth-4)/2;
					
					ByteBuffer bf=ByteBuffer.wrap(pkg);
					short[] pkg_short;			
					pkg_short=new short[lenth/2];
					
					bf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pkg_short, 0, lenth/2);	
					
					for(int i=pkg_fb.index;i<pkg_fb.index+len;i++)
						pkg_fb.buf[i]=rgb565to888(pkg_short[i-pkg_fb.index+2]);
					
					pkg_fb.index+=len;
					
				}
				else if(start_flag==2)
				{
					if(!started)
					{
						return ;
					}
					int len=(lenth-4)/2;
					
					ByteBuffer bf=ByteBuffer.wrap(pkg);
					short[] pkg_short;			
					pkg_short=new short[lenth/2];
					
					bf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pkg_short, 0, lenth/2);	
					
					for(int i=pkg_fb.index;i<pkg_fb.index+len;i++)
						pkg_fb.buf[i]=rgb565to888(pkg_short[i-pkg_fb.index+2]);
					
					pkg_fb.draw();
					
				}
				
				
			}
			
		}
		protected DatagramSocket doInBackground(Integer... ports)
		{
			int port = 8090;
			//dpp=SampleCanvasActivity.this.dp;
			// Connect to port
			
//			InetAddress clientIP = null;
//			int clientPort = -1;
			try
			{
				// bind to local port
				s = new DatagramSocket(port);
				// Wait for connection requests
				//publishProgress("Waiting for clients.");
				while(true)
				{
					
					int code;
					int start_flag;
					int index;
					int lenth;
					DatagramPacket p = new DatagramPacket(message, message.length);
					
					s.receive(p);
					lenth=p.getLength();
					
					byte [] pkg=p.getData();
					
					code=pkg[0];
					start_flag=pkg[1];
					index=pkg[2];
					
					Log.i("miao","code="+code+"start_flag="+start_flag+"index="+index+"lenth="+lenth);
					if(bak)
					{
						bak=false;
						
						pkg_handle(pkg, code, start_flag, index,lenth);
						pkg_handle(message_bak, code, start_flag, index,lenth);
					}
					else {
						if((preIndex>index&&index!=128)||((code==0)&&(preCode==0)&&(index-preIndex!=1&&index-preIndex!=-255))&&!first_pkg)
						{
							message_bak=pkg;
							bak=true;
							Log.i("miao","bak="+bak);
						}
						else 
						{
							bak=false;
							pkg_handle(pkg, code, start_flag, index,lenth);
						}
							
					}
					preCode=pkg[0];
					preIndex=pkg[2];
					first_pkg=false;
						
					

						
					
					
//					message
//					
//					switch(pkg[1])
//					{
//					case 1://start
//						if(started==true)
//						{
//							if(code==0)
//						}
//						break;
//					case 0://not started						
//						if(started==true)
//						{
//							
//						}
//						break;
//					
//					}
				
					
					
//					String received = new String(p.getData(), 0, p.getLength());
//					publishProgress("Received message: " + received);
//					if(received.equals("client:KnockKnock"))
//					{
//						mConnected = true;
//						clientIP = p.getAddress();
//						clientPort = p.getPort();
//						// Store client data for later retrieval
//						mClientIP = p;
//						// Tell the client we hear them.
//						byte[] buf = new String("server:Welcome").getBytes();
//						p = new DatagramPacket(buf, buf.length, clientIP, clientPort);
//						s.send(p);
//						publishProgress("Valid client @" + clientIP.getHostAddress() + ":" + clientPort);
//					}
				}
			}
			catch(UnknownHostException e)
			{
				e.printStackTrace();
			}
			catch (SocketException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//return loadImageFromNetwork(urls[0]);
			return s;
		}
		
		protected void onProgressUpdate(String... values)
		{
			super.onProgressUpdate(values);
			//mClientSays.setText(values[0]);
		}
		
		protected void onPostExecute(DatagramSocket s)
		{
			mClient = s;
			if(mConnected == true)
			{
			  // mClientSays.setText("Connected to client.");
			   mServerSendButton.setEnabled(true);
			}
			else
			{
			  // mClientSays.setText("Session ended.");
			}
		}
		
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		sts.s.close();
		if(mClient != null)
		{
			mClient.close();
			
		}
	}
}

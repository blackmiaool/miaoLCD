package com.SampleCanvas;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Timer;

import javax.security.auth.login.LoginException;





import android.R.bool;
import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.TextView;

public class SampleCanvasActivity extends Activity implements OnTouchListener {
	int[] bf;
	Handler mMainHandler;
	ImageView jpgView;
	Bitmap bm;
	BitmapFactory.Options options;
	int WIDTH=854;
	int HEIGHT=480;

	StartServer sts;
	int test=100;

	package_fb pkg_fb;
	class package_fb{
		int x;
		int y;
		int right;
		int bottom;
		int line_width;
		int index;
		byte buf[]=new byte[1000000];
		Bitmap bm_bak;
		void draw()
		{
			bm_bak = BitmapFactory.decodeByteArray(buf, 0, index);	
			if(bm_bak==null)
				Log.e("miao","null bm");
			else
			{
				//bm=bm_bak;	
				Message toMain = mMainHandler.obtainMessage();
				mMainHandler.sendMessage(toMain);

			}

		}
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.main);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);



		pkg_fb=new package_fb();
		setContentView(R.layout.main);
		jpgView = (ImageView)findViewById(R.id.jpgview);
		//String myJpgPath = "/sdcard/out.jpg"; 
		options = new BitmapFactory.Options();
		options.inSampleSize = 1;


		bf=new int[WIDTH*HEIGHT];
		bm=Bitmap.createBitmap(WIDTH, HEIGHT, Config.RGB_565);
		mMainHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				//Log.i("miao", "Got an incoming message from the child thread - ");
				// 接收子线程的消息
				//info.setText((String) msg.obj);
				if(pkg_fb.bm_bak!=null)
				{

					if(pkg_fb.right==WIDTH-1&&pkg_fb.bottom==HEIGHT-1&&pkg_fb.y==0&&pkg_fb.x==0)//got whole picture
					{
				
						jpgView.setImageBitmap(pkg_fb.bm_bak);
						return;
					}
					else {
						
						
						Log.i("miao","height="+pkg_fb.bm_bak.getHeight());
						Log.i("miao","width="+pkg_fb.bm_bak.getWidth());
						pkg_fb.bm_bak.getPixels(bf, 0, WIDTH, 0, 0, pkg_fb.bm_bak.getWidth(), pkg_fb.bm_bak.getHeight());

						SampleCanvasActivity.this.bm.setPixels(bf, 0,WIDTH, pkg_fb.x, pkg_fb.y,pkg_fb.bm_bak.getWidth(), pkg_fb.bm_bak.getHeight());



					}
				}
				jpgView.setImageBitmap(bm);
			}

		};

		sts=new StartServer();
		sts.execute(8090);



	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	public boolean onTouch(View v, MotionEvent me) {

		return false;
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
		byte[] message = new byte[100000];

		byte[] message_bak;//=new byte[1];



		protected void onPreExecute()
		{

		}

		protected void pkg_handle(byte[] pkg,int code,int start_flag,int index,int lenth)
		{
			if(code==0)
			{
				if(start_flag==1)
				{

					if(!started)
						started=true;

					int len=(lenth-24);
					ByteBuffer bf=ByteBuffer.wrap(pkg);

					pkg_fb.x=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(1);					
					pkg_fb.y=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(2);
					pkg_fb.right=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(3);
					pkg_fb.bottom=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(4);
					pkg_fb.line_width=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(5);

					for(int i=0;i<len;i++)
						pkg_fb.buf[i]=pkg[i+24];

					pkg_fb.index=len;
					Log.i("miao","x="+pkg_fb.x+"y="+pkg_fb.y+"right="+pkg_fb.right+"bottom"+pkg_fb.bottom);
				}
				else if(start_flag==2)
				{
					int len=lenth-24;
					ByteBuffer bf=ByteBuffer.wrap(pkg);

					pkg_fb.x=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(1);					
					pkg_fb.y=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(2);
					pkg_fb.right=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(3);
					pkg_fb.bottom=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(4);
					pkg_fb.line_width=bf.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(5);
				
						pkg_fb.bm_bak = BitmapFactory.decodeByteArray(pkg, 24, len);	
					

					if(pkg_fb.bm_bak==null)
						Log.e("miao","null bm");
					else
					{
						//	bm=bm_bak;	
						Message toMain = mMainHandler.obtainMessage();
						mMainHandler.sendMessage(toMain);
						Log.i("miao","x="+pkg_fb.x+"y="+pkg_fb.y+"right="+pkg_fb.right+"bottom"+pkg_fb.bottom);
					

					}

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
					int len=(lenth-4);

					for(int i=pkg_fb.index;i<pkg_fb.index+len;i++)
						pkg_fb.buf[i]=pkg[i-pkg_fb.index+4];

					pkg_fb.index+=len;

				}
				else if(start_flag==2)
				{
					if(!started)
					{
						return ;
					}
					int len=(lenth-4);

					for(int i=pkg_fb.index;i<pkg_fb.index+len;i++)
						pkg_fb.buf[i]=pkg[i-pkg_fb.index+4];

					pkg_fb.index+=len;
					Log.i("miao","index="+pkg_fb.index);
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
					//					if(bak)
					//					{
					//						bak=false;
					//
					//						pkg_handle(pkg, code, start_flag, index,lenth);
					//						pkg_handle(message_bak, code, start_flag, index,lenth);
					//					}
					//					else {
					//						if((preIndex>index&&index!=-128)||((code==0)&&(preCode==0)&&(index-preIndex!=1&&index-preIndex!=-255))&&!first_pkg)
					//						{
					//							message_bak=pkg;
					//							bak=true;
					//							Log.i("miao","bak="+bak);
					//						}
					//						else 
					//						{
					//							bak=false;
					pkg_handle(pkg, code, start_flag, index,lenth);
					//						}
					//
					//					}
					preCode=pkg[0];
					preIndex=pkg[2];
					//first_pkg=false;






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

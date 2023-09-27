package diatar.eu.net;

import java.net.*;
import java.io.*;
import android.util.*;
import eu.diatar.library.*;

public class TcpThread extends Thread
{
	public volatile String mIpAddr;
	public volatile int mIpPort;
	public int mIndex;
	private TcpClient mParent;
	private volatile Socket mClient;
	private OutputStream mOuts;
	private InputStream mIns;
	private long mLastMS;
	public volatile boolean mRunning;
	public volatile boolean mRecIsWaiting;
	public volatile boolean mStateIsWaiting;
	public volatile boolean mBlankIsWaiting;
	
	public TcpThread(int idx, String adr, int port) {
		mIndex=idx;
		mIpAddr=adr; mIpPort=port;
		mLastMS=System.currentTimeMillis();
		mParent=TcpClient.getMe();
		if (mParent!=null) start();
	}
	
	private String getID() { return "Tcp#"+(mIndex+1); }
	
	private Socket createClient() {
		try {
			InetAddress ia = InetAddress.getByName(mIpAddr);
			Socket s = new Socket();
			s.connect(new InetSocketAddress(ia,mIpPort),3000);
			return s; //new Socket(ia,mIpPort);
		//} catch (SocketTimeoutException e) {
		//	return null;
		} catch (Exception e) {
			if (mRunning && mLastMS+15000+1000*mIndex < System.currentTimeMillis()) {
				mParent.Err(getID()+" kapcsolódás nem sikerült:\n"+e.getLocalizedMessage());
				System.out.println("C: Create error");
				mLastMS=System.currentTimeMillis();
			}
			return null;
		}
	}
	
	@Override
	public void run() {
		mClient=null;
		mRunning=true;
		
		mLastMS=System.currentTimeMillis();
		while(mRunning && mParent.mRunning) {
			try {
				if (mIpAddr==null || mIpAddr.isEmpty() || mIpPort<0) {
					if (mClient!=null) mClient.close();
					mClient=null;
					mRecIsWaiting=false;
					mStateIsWaiting=false;
					mBlankIsWaiting=false;
					sleep(10);
					continue;
				}
				if (mClient==null) {
					Log.d("TcpClient","Client... "+getID());
					mClient=createClient();
					if (mClient==null) {
						sleep(10);
						continue;
					}
					mParent.Msg(getID()+" Kapcsolódva!!!");
					mOuts=mClient.getOutputStream();
					mIns=mClient.getInputStream();
					mStateIsWaiting=true;
					mRecIsWaiting=false;
					mLastMS=System.currentTimeMillis();
				}
				if (!mClient.isConnected() || mClient.isClosed()) {
					mParent.Msg(getID()+" Szétkapcsolva!");
					mClient=null;
					continue;
				}
				//statusz?
				if (mStateIsWaiting) {
					mParent.Lock();
					try {
						mStateIsWaiting=false;
						RecHdr rh = new RecHdr();
						rh.setID();
						rh.setType(rh.itState);
						rh.setSize(mParent.mStateToSend.getMaxlen());
						mOuts.write(rh.buf);
						mOuts.write(mParent.mStateToSend.buf);
					} finally {
						mParent.Unlock();
					}
					mOuts.flush();
					mLastMS = System.currentTimeMillis();
				}
				//kuldendo rekord?
				if (mRecIsWaiting) {
					RecBase r;
					byte rt;
					mParent.Lock();
					try {
						mRecIsWaiting=false;
						r=mParent.mRecToSend;
						rt=mParent.mRtsType;
					} finally {
						mParent.Unlock();
					}
					if (r!=null) {
						RecHdr rh = new RecHdr();
						rh.setID();
						rh.setType(rt);
						rh.setSize(r.getMaxlen());
						mOuts.write(rh.buf);
						mOuts.write(r.buf);
						mOuts.flush();
						mLastMS = System.currentTimeMillis();
					}
				}
				//hatterkep?
				if (mBlankIsWaiting) {
					RecBlank r;
					mParent.Lock();
					try {
						mBlankIsWaiting=false;
						r=mParent.mBlankToSend;
					} finally {
						mParent.Unlock();
					}
					if (r!=null) {
						RecHdr rh = new RecHdr();
						rh.setID();
						rh.setType(rh.itBlank);
						rh.setSize(r.getMaxlen());
						mOuts.write(rh.buf);
						mOuts.write(r.buf);
						mOuts.flush();
						mLastMS = System.currentTimeMillis();
					}
				}
				//van input?
				if (mIns.available()>0) {
					if (mIns.read()<0) continue;
					mLastMS = System.currentTimeMillis();
				}
				//tul sok ido mult el?
				if (mLastMS+5000 < System.currentTimeMillis()) {
					try {
						RecHdr rh = new RecHdr();
						rh.setID();
						rh.setType(rh.itIdle);
						rh.setSize(0);
						mOuts.write(rh.buf);
						mOuts.flush();
					} catch(Exception e) {
						mClient=null;
						mParent.Msg(getID()+" Szétkapcsolva...");
						sleep(10);
					}
					mLastMS = System.currentTimeMillis();
				}
			} catch(Exception e) {
				mParent.Err(getID()+" Error: "+e.getLocalizedMessage());
				System.out.println("S: Error");
				e.printStackTrace();
			}
		}
		try {
			if (mClient!=null) mClient.close();
		} catch (Exception e) {}
		mClient=null;
		mParent=null;
	}
	
}

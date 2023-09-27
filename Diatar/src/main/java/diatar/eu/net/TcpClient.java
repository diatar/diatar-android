package diatar.eu.net;

import diatar.eu.*;
import java.util.concurrent.locks.*;
import java.util.*;
import eu.diatar.library.*;

public class TcpClient {
	private ArrayList<TcpThread> mTcpLst;
	private MainActivity mMain;
	private Lock mLock;
	
	public volatile RecBase mRecToSend;
	public volatile RecState mStateToSend;
	public volatile RecBlank mBlankToSend;
	public volatile byte mRtsType;
	public volatile boolean mRunning;
	
	static private TcpClient me = null;

	private TcpClient() {
		mLock = new ReentrantLock();
		mStateToSend = new RecState();
		mTcpLst = new ArrayList<TcpThread>();
		fillState();
		mRunning=true;
	}

	public static TcpClient get(MainActivity m) {
		if (me==null) me = new TcpClient();
		me.mMain=m;
		return me;
	}

	public static TcpClient getMe() { return me; }

	public void clearMain() { mMain=null; }

	public void Lock() { mLock.lock(); }
	public void Unlock() { mLock.unlock(); }
	
	public void Open() {
		for (TcpThread t : mTcpLst)
			t.mRunning=false;
		mTcpLst.clear();
		for (int i=0; i<G.sIpCnt; i++)
			mTcpLst.add(new TcpThread(i,G.sIpAddr[i],G.sIpPort[i]));
	}
	
	public void setBlankToSend(RecBlank r) {
		Lock();
		try {
			mBlankToSend=r;
			for (TcpThread t : mTcpLst)
				t.mBlankIsWaiting=true;
		} finally {
			Unlock();
		}
	}
	
	public void setRecToSend(RecBase r, byte rtype) {
		Lock();
		try {
			mRecToSend=r;
			mRtsType=rtype;
			for (TcpThread t : mTcpLst)
				t.mRecIsWaiting=true;
		} finally {
			Unlock();
		}
	}
	
	public void setStateProjecting(boolean on) {
		Lock();
		try {
			mStateToSend.setProjecting(on);
			mStateToSend.setWordToHighlight(G.sHighPos);
			mStateToSend.setEndProgram(0);
			sendState();
		} finally {
			Unlock();
		}
	}
	
	public void setStateHighlight(int idx) {
		Lock();
		try {
			mStateToSend.setWordToHighlight(idx);
			mStateToSend.setEndProgram(0);
			G.sHighPos=idx;
			sendState();
		} finally {
			Unlock();
		}
	}

	public void setStateStop(boolean wantshutdown) {
		Lock();
		try {
			mStateToSend.setEndProgram(wantshutdown ? RecState.epSHUTDOWN : RecState.epSTOP);
			sendState();
		} finally {
			Unlock();
		}
	}
	
	public void sendState() {
		for (TcpThread t : mTcpLst)
			t.mStateIsWaiting=true;
	}
	
	public void fillState() {
		StateFiller.fillState(mStateToSend);
		mStateToSend.setEndProgram(0);
		mStateToSend.setBkColor(G.sBkColor);
		mStateToSend.setTxtColor(G.sTxColor);
		mStateToSend.setBlankColor(G.sBlankColor);
		mStateToSend.setHiColor(G.sHighColor);
		mStateToSend.setWordToHighlight(G.sHighPos);
		mStateToSend.setFontSize(G.sFontSize);
		mStateToSend.setTitleSize(G.sTitleSize);
		mStateToSend.setLeftIndent(G.sIndent);
		mStateToSend.setSpacing100(G.sSpacing*10+100);
		mStateToSend.setAutoResize(G.sAutosize);
		mStateToSend.setVCenter(G.sVCenter);
		mStateToSend.setHCenter(G.sHCenter);
		mStateToSend.setUseAkkord(G.sUseAkkord);
		mStateToSend.setUseKotta(G.sUseKotta);
		mStateToSend.setHideTitle(!G.sUseTitle);
		mStateToSend.setBgMode(G.sBgMode);
		mStateToSend.setIsBlankPic(!G.sBlankPic.isEmpty());
		mStateToSend.setShowBlankPic(!G.sBlankPic.isEmpty());
		mStateToSend.setKottaArany(G.sKottaArany);
		mStateToSend.setAkkordArany(G.sAkkordArany);
		mStateToSend.setBackTransPerc(G.sBackTrans);
		mStateToSend.setBlankTransPerc(G.sBlankTrans);
		mStateToSend.setBorderL(G.sBorderL);
		mStateToSend.setBorderT(G.sBorderT);
		mStateToSend.setBorderR(G.sBorderR);
		mStateToSend.setBorderB(G.sBorderB);
		mStateToSend.setBoldText(G.sBoldText);
	}
	
	public void Msg(String txt) {
		if (mMain==null) return;
		final String txx = txt;
		mMain.runOnUiThread(new Runnable() {
				@Override public void run() {
					mMain.Msg(txx);
				}
			});
	}
	
	public void Err(String txt) {
		if (mMain==null) return;
		final String txx = txt;
		mMain.runOnUiThread(new Runnable() {
				@Override public void run() {
					mMain.Err(txx);
				}
			});
	}

	public void Stop() {
		mRunning=false;
		me=null;
	}

} // TcpClient

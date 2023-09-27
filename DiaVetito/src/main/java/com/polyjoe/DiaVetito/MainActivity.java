package com.polyjoe.DiaVetito;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.view.View.*;
import android.util.*;

import androidx.annotation.RequiresApi;

import eu.diatar.library.*;

public class MainActivity extends Activity 
{
	//public static final String VerStr = "v1.2";
	//public static final String VerTxt = "Verzió: "+VerStr;
	
	//shared pref
	public static final String spPORT ="Port";
	public static final String spBOOT ="Boot";
	public static final String spB2C = "B2C";
	public static final String spCLIPL ="ClipL";
	public static final String spCLIPR ="ClipR";
	public static final String spCLIPT ="ClipT";
	public static final String spCLIPB ="ClipB";
	public static final String spMIRROR = "Mirror";
	public static final String spROTATE = "Rotate";
	
	private ProjectedView mProjView;
	private TcpServer mTcp;
	public int TcpPort;
	public boolean mBoot;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		TxtSizer.setDensity(this);
		mTcp = TcpServer.get(this);
		mTcp.density = getResources().getDisplayMetrics().density;
		LoadConfig();
		convertClipDpToPx();
		mTcp.setPort(TcpPort);

		mProjView = new ProjectedView(this);
		setContentView(mProjView);
		mProjView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent m) {
				onSetting();
				return mProjView.onTouchEvent(m);
			}
		});
		if (savedInstanceState==null) {
			mTcp.Dia = new DiaLogo(mProjView);
			mTcp.G.Projecting=true;
		} else
			if (mTcp.Dia!=null) mTcp.Dia.OnRecalc();
		
		OnAskSize();
    }

	@Override
	protected void onDestroy()
	{
		mTcp.Stop();
		mTcp.clearMain();
		mTcp=null;
		super.onDestroy();
	}
	
	private void LoadConfig() {
		SharedPreferences sp = getSharedPreferences("settings",Context.MODE_PRIVATE);
		TcpPort=sp.getInt(spPORT,1024);
		mBoot=sp.getBoolean(spBOOT,false);
		mTcp.G.Border2Clip=sp.getBoolean(spB2C, false);
		mTcp.ClipLdp=sp.getFloat(spCLIPL,0f);
		mTcp.ClipRdp=sp.getFloat(spCLIPR,0f);
		mTcp.ClipTdp=sp.getFloat(spCLIPT,0f);
		mTcp.ClipBdp=sp.getFloat(spCLIPB,0f);
		mTcp.mMirror=sp.getBoolean(spMIRROR,false);
		mTcp.mRotate=sp.getInt(spROTATE,0);
	}
	
	private void SaveConfig() {
		SharedPreferences sp = getSharedPreferences("settings",Context.MODE_PRIVATE);
		SharedPreferences.Editor spe = sp.edit();
		spe.putInt(spPORT,TcpPort);
		spe.putBoolean(spBOOT,mBoot);
		spe.putBoolean(spB2C,mTcp.G.Border2Clip);
		spe.putFloat(spCLIPL,mTcp.ClipLdp);
		spe.putFloat(spCLIPR,mTcp.ClipRdp);
		spe.putFloat(spCLIPT,mTcp.ClipTdp);
		spe.putFloat(spCLIPB,mTcp.ClipBdp);
		spe.putBoolean(spMIRROR,mTcp.mMirror);
		spe.putInt(spROTATE,mTcp.mRotate);
		spe.commit();
	}
	
	public void convertClipDpToPx() {
		mTcp.ClipLpx=mTcp.ClipLdp*mTcp.density;
		mTcp.ClipRpx=mTcp.ClipRdp*mTcp.density;
		mTcp.ClipTpx=mTcp.ClipTdp*mTcp.density;
		mTcp.ClipBpx=mTcp.ClipBdp*mTcp.density;
	}

	private void stopProgram() {
		mTcp.Stop();
		finishAffinity();
		finishAndRemoveTask();
		OS.Abort();
	}
	private void shutdownOS() {
		Msg("Operációs rendszer leállítása...");
		OS.Shutdown(this);
	}
	private void restartOS() {
		Msg("Operációs rendszer újraindítása...");
		OS.Restart(this);
	}

	////////////////
	// tcpip activity
	////////////////
	public void OnState(RecState rec) {
		Log.d("Main","OnState");
		if (rec.getEndProgram() == RecState.epSTOP || rec.getEndProgram() == RecState.epSTOP+RecState.epSKIPSERIALOFF) {
			stopProgram();
		} else if (rec.getEndProgram() == RecState.epSHUTDOWN || rec.getEndProgram() == RecState.epSHUTDOWN+RecState.epSKIPSERIALOFF) {
			shutdownOS();
		}
		mTcp.G.LoadFromRec(rec);
		if (mTcp.G.Border2Clip && (
				mTcp.ClipLdp!=mTcp.G.BorderL || mTcp.ClipTdp!=mTcp.G.BorderT
				|| mTcp.ClipRdp!=mTcp.G.BorderR || mTcp.ClipBdp!=mTcp.G.BorderB
		)) {
			mTcp.ClipLdp=mTcp.G.BorderL;
			mTcp.ClipTdp=mTcp.G.BorderT;
			mTcp.ClipRdp=mTcp.G.BorderR;
			mTcp.ClipBdp=mTcp.G.BorderB;
			convertClipDpToPx();
			mProjView.Recalc();
		}
		if (mTcp.Dia!=null) mTcp.Dia.RecalcIfNeeded();
		mProjView.invalidate();
	}
	
	public void OnPic(RecPic rec) {
		Log.d("Main","OnPic");
		DiaPic d = new DiaPic();
		d.loadBmp(rec);
		mTcp.Dia=d;
		mProjView.invalidate();
	}
	
	public void OnBlank(RecBlank rec) {
		Log.d("Main","OnBlank");
		DiaBlank d = new DiaBlank();
		d.loadBmp(rec);
		mTcp.Blank=d;
		mProjView.invalidate();
	}
	
	public void OnText(RecText rec) {
		Log.d("Main","OnText");
		DiaText d = new DiaText(getResources());
		d.loadFromRec(rec);
		mTcp.Dia=d;
		mTcp.G.WordToHighlight=0;
		mProjView.invalidate();
	}
	
	public void OnAskSize() {
		Log.d("Main","OnAskSize");
		int w=mProjView.getWidth(), h=mProjView.getHeight();
		RecScrSize rs = new RecScrSize();
		rs.setWidth(w); rs.setHeight(h);
		rs.setKorusMode(false);
		mTcp.sendRec(rs,RecHdr.itScrSize);
	}
	///////////////
	// show messages
	///////////////
	
	public void Err(String txt) {
		Toast.makeText(getApplicationContext()
					   ,txt,
					   Toast.LENGTH_SHORT).show();
	}
	
	public void Msg(String txt) {
		Toast.makeText(getApplicationContext()
					   ,txt,
					   Toast.LENGTH_SHORT).show();
	}
	
	////////////////
	
	private final int REQUEST_SETTINGS = 1;
	
	public void onSetting() {
		Intent it = new Intent(this, SettingsActivity.class);
		it.putExtra(SettingsActivity.itIP,TcpServer.getIPAddress(true));
		it.putExtra(SettingsActivity.itPORT,TcpPort);
		it.putExtra(SettingsActivity.itB2C, mTcp.G.Border2Clip);
		it.putExtra(SettingsActivity.itCLIPL,mTcp.ClipLdp);
		it.putExtra(SettingsActivity.itCLIPR,mTcp.ClipRdp);
		it.putExtra(SettingsActivity.itCLIPT,mTcp.ClipTdp);
		it.putExtra(SettingsActivity.itCLIPB,mTcp.ClipBdp);
		it.putExtra(SettingsActivity.itMIRROR,mTcp.mMirror);
		it.putExtra(SettingsActivity.itROTATE,mTcp.mRotate);
		it.putExtra(SettingsActivity.itBOOT,mBoot);
		startActivityForResult(it,REQUEST_SETTINGS);
	}
	
	private void reqSettings(int resultCode, Intent data) {
		if (resultCode==RESULT_OK) {
			TcpPort=data.getIntExtra(SettingsActivity.itPORT,1024);
			mBoot=data.getBooleanExtra(SettingsActivity.itBOOT,false);
			mTcp.setPort(TcpPort);
			mTcp.G.Border2Clip=data.getBooleanExtra(SettingsActivity.itB2C, false);
			mTcp.ClipLdp=data.getFloatExtra(SettingsActivity.itCLIPL,0f);
			mTcp.ClipRdp=data.getFloatExtra(SettingsActivity.itCLIPR,0f);
			mTcp.ClipTdp=data.getFloatExtra(SettingsActivity.itCLIPT,0f);
			mTcp.ClipBdp=data.getFloatExtra(SettingsActivity.itCLIPB,0f);
			mTcp.mMirror=data.getBooleanExtra(SettingsActivity.itMIRROR,false);
			mTcp.mRotate=data.getIntExtra(SettingsActivity.itROTATE,0);
			convertClipDpToPx();
			SaveConfig();
			mProjView.Recalc();
			mProjView.invalidate();
		} else if (resultCode==SettingsActivity.RESULT_EXIT) {
			stopProgram();
		} else if (resultCode==SettingsActivity.RESULT_SHUTDOWN) {
			shutdownOS();
		} else if (resultCode==SettingsActivity.RESULT_REBOOT) {
			restartOS();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_SETTINGS) reqSettings(resultCode, data);
	}
}

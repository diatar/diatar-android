package com.polyjoe.DiaVetito;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.*;

import com.google.android.material.chip.Chip;

public class SettingsActivity extends Activity
{
	public static final String itIP = "IP";
	public static final String itPORT = "Port";
	public static final String itBOOT = "Boot";
	public static final String itB2C = "B2C";		//border->clip
	public static final String itCLIPL = "ClipL";
	public static final String itCLIPR = "ClipR";
	public static final String itCLIPT = "ClipT";
	public static final String itCLIPB = "ClipB";
	public static final String itMIRROR = "Mirror";
	public static final String itROTATE = "Rotate";
	
	public static final int RESULT_EXIT = RESULT_FIRST_USER;
	public static final int RESULT_SHUTDOWN = RESULT_FIRST_USER+1;
	public static final int RESULT_REBOOT = RESULT_FIRST_USER+2;
	
	private EditText PortEd, LEd, REd, TEd, BEd;
	private CheckBox mMirror;
	private Spinner mRotate;
	private CheckBox mBootCk;
	private CheckBox mC2BCk;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		setTitle("Beállítás");
		PortEd = findViewById(R.id.PortEd);
		mC2BCk = findViewById(R.id.B2CCk);
		LEd = findViewById(R.id.ClipL);
		REd = findViewById(R.id.ClipR);
		TEd = findViewById(R.id.ClipT);
		BEd = findViewById(R.id.ClipB);
		mMirror=findViewById(R.id.MirrorCb);
		mRotate=findViewById(R.id.RotLst);
		mBootCk = findViewById(R.id.BootCk);
		Intent it = getIntent();
		if (savedInstanceState!=null) {
			PortEd.setText(savedInstanceState.getString(itPORT));
			mC2BCk.setChecked(savedInstanceState.getBoolean(itB2C));
			LEd.setText(savedInstanceState.getString(itCLIPL));
			REd.setText(savedInstanceState.getString(itCLIPR));
			TEd.setText(savedInstanceState.getString(itCLIPT));
			BEd.setText(savedInstanceState.getString(itCLIPB));
			mMirror.setChecked(savedInstanceState.getBoolean(itMIRROR));
			mRotate.setSelection(savedInstanceState.getInt(itROTATE));
			mBootCk.setChecked(savedInstanceState.getBoolean(itBOOT));
		} else {
			PortEd.setText(new Integer(it.getIntExtra(itPORT,1024)).toString());
			mC2BCk.setChecked(it.getBooleanExtra(itB2C,false));
			LEd.setText(new Float(it.getFloatExtra(itCLIPL,0f)).toString());
			REd.setText(new Float(it.getFloatExtra(itCLIPR,0f)).toString());
			TEd.setText(new Float(it.getFloatExtra(itCLIPT,0f)).toString());
			BEd.setText(new Float(it.getFloatExtra(itCLIPB,0f)).toString());
			mMirror.setChecked(it.getBooleanExtra(itMIRROR,false));
			mRotate.setSelection(it.getIntExtra(itROTATE,0));
			mBootCk.setChecked(it.getBooleanExtra(itBOOT,false));
		}
		B2Cset();
		TextView vertv = findViewById(R.id.VerTxt);
		vertv.setText(TcpServer.getMe().getVerTxt(true));
		TextView iptv = findViewById(R.id.IpTxt);
		iptv.setText("IP: "+it.getStringExtra(itIP));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(itPORT,PortEd.getText().toString());
		outState.putBoolean(itB2C, mC2BCk.isChecked());
		outState.putString(itCLIPL,LEd.getText().toString());
		outState.putString(itCLIPR,REd.getText().toString());
		outState.putString(itCLIPT,TEd.getText().toString());
		outState.putString(itCLIPB,BEd.getText().toString());
		outState.putBoolean(itMIRROR,mMirror.isChecked());
		outState.putInt(itROTATE,mRotate.getSelectedItemPosition());
		outState.putBoolean(itBOOT,mBootCk.isChecked());
	}

	protected void B2Cset() {
		boolean on = mC2BCk.isChecked();
		LEd.setEnabled(!on);
		REd.setEnabled(!on);
		TEd.setEnabled(!on);
		BEd.setEnabled(!on);
	}

	public void onOk(View v) {
		int val = Integer.parseInt(PortEd.getText().toString());
		if (val<0 || val>65535) {
			Toast.makeText(getApplicationContext()
						   ,"0..65535 közötti portszám kell!",
						   Toast.LENGTH_SHORT).show();
			return;
		}
		float lval = Float.parseFloat(LEd.getText().toString());
		float rval = Float.parseFloat(REd.getText().toString());
		float tval = Float.parseFloat(TEd.getText().toString());
		float bval = Float.parseFloat(BEd.getText().toString());
		Intent it = new Intent();
		it.putExtra(itPORT,val);
		it.putExtra(itB2C, mC2BCk.isChecked());
		it.putExtra(itCLIPL,lval);
		it.putExtra(itCLIPR,rval);
		it.putExtra(itCLIPT,tval);
		it.putExtra(itCLIPB,bval);
		it.putExtra(itMIRROR,mMirror.isChecked());
		it.putExtra(itROTATE,mRotate.getSelectedItemPosition());
		it.putExtra(itBOOT,mBootCk.isChecked());
		setResult(RESULT_OK,it);
		finish();
	}
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onExit(View v) {
		setResult(RESULT_EXIT);
		finish();
	}
	
	public void onShutdown(View v) {
		setResult(RESULT_SHUTDOWN);
		finish();
	}
	
	public void onReboot(View v) {
		setResult(RESULT_REBOOT);
		finish();
	}

	public void onB2C(View v) {
		B2Cset();
	}
}

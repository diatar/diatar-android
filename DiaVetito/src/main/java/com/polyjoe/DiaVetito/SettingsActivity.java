package com.polyjoe.DiaVetito;

import android.app.*;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import android.content.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.diatar.library.MqttInterface;
import eu.diatar.library.StringListAdapter;

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
	public static final String itIPMODE = "IPmode";
	public static final String itSENDER = "Sender";
	public static final String itUSER = "Username";
	public static final String itCHANNEL = "Channel";

	public static final int RESULT_EXIT = RESULT_FIRST_USER;
	public static final int RESULT_SHUTDOWN = RESULT_FIRST_USER+1;
	public static final int RESULT_REBOOT = RESULT_FIRST_USER+2;
	
	private EditText PortEd, LEd, REd, TEd, BEd, SenderEd;
	private CheckBox mMirror;
	private Spinner mRotate;
	private CheckBox mBootCk;
	private CheckBox mC2BCk;
	private RadioButton mIpBtn, mMqttBtn;
	private RecyclerView mSenderLst;
	//private Spinner mChannelLst;
	private StringListAdapter mSenderLstAdapter;
	private ArrayAdapter<String> mChannelLstAdapter;
	private String mUsername, mChannel;

	private MqttInterface mMqtt;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		setTitle("Beállítás");
		mMqtt = MqttInterface.getInstance();

		mIpBtn = findViewById(R.id.IPmode);
		mMqttBtn = findViewById(R.id.MQTTmode);
		PortEd = findViewById(R.id.PortEd);
		SenderEd = findViewById(R.id.SenderEd);
		mSenderLst = findViewById(R.id.SenderLst);
		//mChannelLst = findViewById(R.id.ChannelLst);
		mC2BCk = findViewById(R.id.B2CCk);
		LEd = findViewById(R.id.ClipL);
		REd = findViewById(R.id.ClipR);
		TEd = findViewById(R.id.ClipT);
		BEd = findViewById(R.id.ClipB);
		mMirror=findViewById(R.id.MirrorCb);
		mRotate=findViewById(R.id.RotLst);
		mBootCk = findViewById(R.id.BootCk);

		PortEd.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				modeSet(true);
			}
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		});
		SenderEd.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable editable)
			{
				modeSet(editable.length()<=0);
				fillSenderLst();
			}
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		});
		mSenderLstAdapter = new StringListAdapter();
		mSenderLstAdapter.setClickCallback(this::onSenderLstClick);
		mSenderLst.setLayoutManager(new LinearLayoutManager(this));
		mSenderLst.setAdapter(mSenderLstAdapter);
		mChannelLstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
		mChannelLstAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//mChannelLst.setAdapter(mChannelLstAdapter);
		fillChannelLst();

		Intent it = getIntent();
		if (savedInstanceState!=null) {
			PortEd.setText(savedInstanceState.getString(itPORT));
			SenderEd.setText(savedInstanceState.getString(itSENDER));
			modeSet(savedInstanceState.getBoolean(itIPMODE));
			mC2BCk.setChecked(savedInstanceState.getBoolean(itB2C));
			LEd.setText(savedInstanceState.getString(itCLIPL));
			REd.setText(savedInstanceState.getString(itCLIPR));
			TEd.setText(savedInstanceState.getString(itCLIPT));
			BEd.setText(savedInstanceState.getString(itCLIPB));
			mMirror.setChecked(savedInstanceState.getBoolean(itMIRROR));
			mRotate.setSelection(savedInstanceState.getInt(itROTATE));
			mBootCk.setChecked(savedInstanceState.getBoolean(itBOOT));
			mUsername=savedInstanceState.getString(itUSER);
			mChannel=savedInstanceState.getString(itCHANNEL);
		} else {
			PortEd.setText(new Integer(it.getIntExtra(itPORT,1024)).toString());
			SenderEd.setText(it.getStringExtra(itSENDER));
			modeSet(it.getBooleanExtra(itIPMODE,true));
			mC2BCk.setChecked(it.getBooleanExtra(itB2C,false));
			LEd.setText(new Float(it.getFloatExtra(itCLIPL,0f)).toString());
			REd.setText(new Float(it.getFloatExtra(itCLIPR,0f)).toString());
			TEd.setText(new Float(it.getFloatExtra(itCLIPT,0f)).toString());
			BEd.setText(new Float(it.getFloatExtra(itCLIPB,0f)).toString());
			mMirror.setChecked(it.getBooleanExtra(itMIRROR,false));
			mRotate.setSelection(it.getIntExtra(itROTATE,0));
			mBootCk.setChecked(it.getBooleanExtra(itBOOT,false));
			mUsername=it.getStringExtra(itUSER);
			mChannel=it.getStringExtra(itCHANNEL);
		}
		SenderEd.setText(mUsername);
		modeSet(mUsername.isEmpty());
		B2Cset();

		TextView vertv = findViewById(R.id.VerTxt);
		vertv.setText(TcpServer.getMe().getVerTxt(true));
		TextView iptv = findViewById(R.id.IpTxt);
		iptv.setText("IP: "+it.getStringExtra(itIP));

		mMqtt.setErrCallback(txt -> {});
		mMqtt.setCompletedCallback(v -> fillSenderLst());
		mMqtt.fillUserList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(itPORT,PortEd.getText().toString());
		outState.putString(itSENDER, SenderEd.getText().toString());
		outState.putBoolean(itIPMODE, mIpBtn.isChecked());
		outState.putBoolean(itB2C, mC2BCk.isChecked());
		outState.putString(itCLIPL,LEd.getText().toString());
		outState.putString(itCLIPR,REd.getText().toString());
		outState.putString(itCLIPT,TEd.getText().toString());
		outState.putString(itCLIPB,BEd.getText().toString());
		outState.putBoolean(itMIRROR,mMirror.isChecked());
		outState.putInt(itROTATE,mRotate.getSelectedItemPosition());
		outState.putBoolean(itBOOT,mBootCk.isChecked());
		outState.putString(itUSER,mIpBtn.isChecked() ? "" : SenderEd.getText().toString());
		outState.putString(itCHANNEL,mChannel.isEmpty() ? getCurrChannel() : mChannel);
	}

	protected String getCurrChannel() {
		int idx = 0; //mChannelLst.getSelectedItemPosition();
		if (idx<0) return "";
		String txt = mChannelLstAdapter.getItem(idx);
		if (txt.length()<4) return "";
		return "1"; // txt.substring(3).trim();
	}

	protected void fillSenderLst() {
		List<String> ulst =mMqtt.usersLike(SenderEd.getText().toString());
		mSenderLstAdapter.setData(ulst);
		if (mUsername!=null && !mUsername.isEmpty() && ulst!=null && !ulst.isEmpty()) {
			int idx=mSenderLstAdapter.findItemPos(mUsername);
			if (idx>=0) {
				mSenderLstAdapter.setSelection(idx);
				fillChannelLst();
			}
			//mUsername="";
		}
	}

	protected void fillChannelLst() {
		int idx=0; //mChannelLst.getSelectedItemPosition();
		if (idx>0) {
			String arr[] = new String[10];
			for (int i = 0; i < 10; i++) {
				arr[i] = String.valueOf(i + 1) + ".";
			}
			int sel = mSenderLstAdapter.getSelection();
			if (sel != RecyclerView.NO_POSITION) {
				String uname = mSenderLstAdapter.getItem(sel);
				MqttInterface.tUserRec user = mMqtt.getUser(uname);
				if (user != null) {
					for (int i = 0; i < 10; i++) {
						String chname = user.Channels[i];
						if (!mChannel.isEmpty() && chname.equals(mChannel)) {
							idx = i;
							mChannel = "";
						}
						arr[i] = String.valueOf(i + 1) + ". " + chname;
					}
				}
			}
			mChannelLstAdapter.clear();
			mChannelLstAdapter.addAll(arr);
			mChannelLstAdapter.notifyDataSetChanged();
			//mChannelLst.setSelection(idx);
		}
	}

	protected void onSenderLstClick(int pos) {
		String txt = mSenderLstAdapter.getSelectionString();
		SenderEd.setText(txt);
		fillChannelLst();
	}

	protected void modeSet(boolean ipmode) {
		if (ipmode) {
			mIpBtn.setChecked(true);
			mMqttBtn.setChecked(false);
			//PortEd.setEnabled(true);
			//SenderEd.setEnabled(false);
			//mSenderLst.setEnabled(false);
		} else {
			mIpBtn.setChecked(false);
			mMqttBtn.setChecked(true);
			//PortEd.setEnabled(false);
			//SenderEd.setEnabled(true);
			//mSenderLst.setEnabled(true);
		}
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
		it.putExtra(itUSER,mIpBtn.isChecked() ? "" : mSenderLstAdapter.getSelectionString());
		it.putExtra(itCHANNEL,mChannel.isEmpty() ? getCurrChannel() : mChannel);
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

	public void onMode(View v) {
		modeSet(v==mIpBtn);
	}
}

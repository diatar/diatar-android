package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.text.*;
import android.view.*;
import android.util.*;
import diatar.eu.net.*;

public class SetNet extends Activity
{
	static private final int NADDR = 4;
	
	private EditText[] mAddrEd, mPortEd;
	private View[] mSepar;
	private ImageButton[] mDelBtn;
	private Button mNewBtn;
	private TextView mTV1, mTV2;
	
	private int mCnt;
	
	private int getID(String key) {
		return getResources().getIdentifier(key,
			"id",getPackageName());
	}
	
	@Override
    protected void onCreate(Bundle bd)
    {
        super.onCreate(bd);

		setContentView(diatar.eu.R.layout.setnet);
		setTitle("Hálózat beállítás");
		
		mAddrEd = new EditText[NADDR];
		mPortEd = new EditText[NADDR];
		mSepar = new View[NADDR];
		mDelBtn = new ImageButton[NADDR];
		
		mCnt=(bd==null ? G.sIpCnt : bd.getInt(G.idIPCNT));
		for (int i=0; i<NADDR; i++) {
			Log.d("SetNet","setip"+i);
			EditText ae = findViewById(getID("setIpAddr"+i));
			EditText pe = findViewById(getID("setIpPort"+i));
			mSepar[i]=findViewById(getID("setIpLine"+i));
			mDelBtn[i]=findViewById(getID("setIpDel"+i));
			mAddrEd[i]=ae;
			mPortEd[i]=pe;
			ae.setFilters(new InputFilter[]{
				new InputFilter.LengthFilter(15),
				new IpAddrFilter()
			});
			pe.setFilters(new InputFilter[]{new MinMaxFilter(0,65535) });
			String av = "0.0.0.0";
			int pv = 1024;
			if (G.sIpCnt>i) { av=G.sIpAddr[i]; pv=G.sIpPort[i]; }
			if (bd!=null) {
				av=bd.getString(G.idIPADDR+i,av);
				pv=bd.getInt(G.idIPPORT+i,pv);
			}
			ae.setText(av);
			pe.setText(String.valueOf(pv));
		}
		mNewBtn=findViewById(diatar.eu.R.id.setIpNewBtn);
		mTV1=findViewById(diatar.eu.R.id.setNetTV1);
		mTV2=findViewById(diatar.eu.R.id.setNetTV2);
		showByCnt();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(G.idIPCNT,mCnt);
		for (int i=0; i<NADDR; i++) {
			outState.putString(G.idIPADDR+i,mAddrEd[i].getText().toString());
			outState.putInt(G.idIPPORT+i,getPort(i));
		}
	}
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		for (int i=0; i<mCnt; i++) {
			String ipa = mAddrEd[i].getText().toString();
			if (!Patterns.IP_ADDRESS.matcher(ipa).matches()) {
				TxTar.Msg(this,"Hibás IP cím!");
				return;
			}
		}
		G.setIpCnt(mCnt);
		for (int i=0; i<mCnt; i++) {
			G.sIpAddr[i]=mAddrEd[i].getText().toString();
			G.sIpPort[i]=getPort(i);
		}
		
		setResult(RESULT_OK);
		finish();
	}
	
	public void onNew(View v) {
		mCnt++; showByCnt();
	}
	
	public void onDel(View v) {
		int idx = NADDR;
		while (idx-->0 && v!=mDelBtn[idx]) ;
		if (idx<0) return;
		while (idx<NADDR-1) {
			mAddrEd[idx].setText(mAddrEd[idx+1].getText());
			mPortEd[idx].setText(mPortEd[idx+1].getText());
			idx++;
		}
		mCnt--; showByCnt();
	}
	
	private int getPort(int idx) {
		int res = 0;
		try {
			res = Integer.valueOf(mPortEd[idx].getText().toString());
		} catch (Exception e) { res=0; }
		return res;
	}
	
	private void showByCnt() {
		if (mCnt>NADDR) mCnt=NADDR;
		for (int i=0; i<NADDR; i++) {
			mAddrEd[i].setVisibility(i<mCnt ? View.VISIBLE : View.GONE);
			mPortEd[i].setVisibility(i<mCnt ? View.VISIBLE : View.GONE);
			if (i>0)
				mSepar[i].setVisibility(i<mCnt ? View.VISIBLE : View.GONE);
			mDelBtn[i].setVisibility(i<mCnt ? View.VISIBLE : View.GONE);
		}
		mNewBtn.setEnabled(mCnt<NADDR);
		mTV2.setVisibility(mCnt>0 ? View.VISIBLE : View.GONE);
		mTV1.setText(mCnt>0 ? "IP:" : "Nincs IP cím megadva, adjon új címet!");
	}
}

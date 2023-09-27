package diatar.eu;

import android.app.*;
import android.widget.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.content.*;
import android.view.View.*;
import android.graphics.*;

public class SetProj extends Activity
{
	private Button mBkColorBtn,mTxColorBtn,mBlankColorBtn,mHighColorBtn;
	private EditText mFontSize;
	private ToggleButton mUseTitle;
	private EditText mTitleSize;
	private EditText mIndent;
	private Spinner mSpacing;
	private ToggleButton mAutosize;
	private ToggleButton mHCenter, mVCenter,mBold;
	private ToggleButton mUseAccord,mUseKotta;
	private TextView mBlankPic;
	private Spinner mBgMode;
	private int mIndex;
	private Spinner mKottaArany,mAkkordArany;
	private Spinner mBackTrans, mBlankTrans;
	private EditText mBorderL, mBorderT, mBorderR, mBorderB;
	private CheckBox mBkColorCk, mTxColorCk, mBlankColorCk, mHighColorCk,
		mFontSizeCk, mTitleSizeCk, mIndentCk, mSpacingCk,
		mHCenterCk, mVCenterCk;
	
	private String mPicDir;
	private boolean mPropMode;
	
	@Override
    protected void onCreate(Bundle bd)
    {
        super.onCreate(bd);

		setContentView(R.layout.setproject);
		setTitle("Vetítési beállítások");
		
		mBkColorBtn = findViewById(R.id.setBkColorBtn);
		mTxColorBtn = findViewById(R.id.setTxColorBtn);
		mBlankColorBtn = findViewById(R.id.setBlankColorBtn);
		mHighColorBtn = findViewById(R.id.setHighColorBtn);
		mFontSize = findViewById(R.id.setFontSize);
		mUseTitle = findViewById(R.id.setUseTitle);
		mTitleSize = findViewById(R.id.setTitleSize);
		mIndent = findViewById(R.id.setLeftIndent);
		mSpacing = findViewById(R.id.setSpacing);
		mAutosize = findViewById(R.id.setAutoSize);
		mBold = findViewById(R.id.setBold);
		mHCenter = findViewById(R.id.setHCenter);
		mVCenter = findViewById(R.id.setVCenter);
		mUseAccord = findViewById(R.id.setUseAccord);
		mUseKotta = findViewById(R.id.setUseKotta);
		mBlankPic = findViewById(R.id.setBlankPic);
		mBgMode = findViewById(R.id.setBgMode);
		mKottaArany = findViewById(R.id.setKottaArany);
		mAkkordArany = findViewById(R.id.setAkkordArany);
		mBackTrans = findViewById(R.id.setBackTrans);
		mBlankTrans = findViewById(R.id.setBlankTrans);
		mBorderL = findViewById(R.id.setBorderL);
		mBorderT = findViewById(R.id.setBorderT);
		mBorderR = findViewById(R.id.setBorderR);
		mBorderB = findViewById(R.id.setBorderB);
		
		mBkColorCk = findViewById(R.id.setBkColorCk);
		mTxColorCk = findViewById(R.id.setTxColorCk);
		mBlankColorCk = findViewById(R.id.setBlankColorCk);
		mHighColorCk = findViewById(R.id.setHighColorCk);
		mFontSizeCk = findViewById(R.id.setFontSizeCk);
		mTitleSizeCk = findViewById(R.id.setTitleSizeCk);
		mIndentCk = findViewById(R.id.setIndentCk);
		mSpacingCk = findViewById(R.id.setSpacingCk);
		mHCenterCk = findViewById(R.id.setHCenterCk);
		mVCenterCk = findViewById(R.id.setVCenterCk);
		
		MinMaxFilter.Add(mFontSize,12,128);
		MinMaxFilter.Add(mTitleSize,12,128);
		MinMaxFilter.Add(mIndent,0,10);
		
		int bkc = G.sBkColor, txc = G.sTxColor, blc = G.sBlankColor, hic = G.sHighColor;
		int fs = G.sFontSize, ts = G.sTitleSize;
		int li = G.sIndent, sp = G.sSpacing;
		byte as = DiaProp.BoolToB3(G.sAutosize);
		byte vc = DiaProp.BoolToB3(G.sVCenter);
		byte hc = DiaProp.BoolToB3(G.sHCenter);
		byte ua = DiaProp.BoolToB3(G.sUseAkkord);
		byte uk = DiaProp.BoolToB3(G.sUseKotta);
		byte ut = DiaProp.BoolToB3(G.sUseTitle);
		if (!G.sUseTitle) ts=-ts-1;
		String bp = G.sBlankPic;
		int bgm = G.sBgMode;
		int ka = G.sKottaArany, aa = G.sAkkordArany;
		int bat = G.sBackTrans, blt = G.sBlankTrans;
		int brdl= G.sBorderL, brdt = G.sBorderT;
		int brdr= G.sBorderR, brdb = G.sBorderB;
		boolean btxt=G.sBoldText;
		
		mPropMode=false;
		if (bd==null) {
			Intent it = getIntent();
			if (it.hasExtra(G.idBKCOLOR)) {
				mPropMode=true;
				bd=it.getExtras();
			}
		}
		if (bd!=null) {
			mPropMode=bd.getBoolean(G.idPROPMODE,mPropMode);
			bkc=bd.getInt(G.idBKCOLOR);
			txc=bd.getInt(G.idTXCOLOR);
			blc=bd.getInt(G.idBLANKCOLOR);
			hic=bd.getInt(G.idHIGHCOLOR);
			fs=bd.getInt(G.idFONTSIZE);
			ut=bd.getByte(G.idUSETITLE);
			ts=bd.getInt(G.idTITLESIZE);
			li=bd.getInt(G.idLEFTINDENT);
			sp=bd.getInt(G.idSPACING);
			as=bd.getByte(G.idAUTOSIZE);
			vc=bd.getByte(G.idVCENTER);
			hc=bd.getByte(G.idHCENTER);
			ua=bd.getByte(G.idUSEAKKORD);
			uk=bd.getByte(G.idUSEKOTTA);
			bp=bd.getString(G.idBLANKPIC);
			bgm=bd.getInt(G.idBGMODE);
			mIndex=bd.getInt(G.idINDEX,0);
			ka=bd.getInt(G.idKOTTAARANY,100);
			aa=bd.getInt(G.idAKKORDARANY,100);
			bat=bd.getInt(G.idBACKTRANS,0);
			blt=bd.getInt(G.idBLANKTRANS,0);
			brdl=bd.getInt(G.idBORDERL, 0);
			brdt=bd.getInt(G.idBORDERT, 0);
			brdr=bd.getInt(G.idBORDERR, 0);
			brdb=bd.getInt(G.idBORDERB, 0);
			btxt=bd.getBoolean(G.idBOLDTEXT,false);
		}
		
		if (mPropMode) {	//egyedi diajellemzok
			View v;
			v=findViewById(R.id.setUseTitleLbl);
			  v.setVisibility(View.GONE);
			  mUseTitle.setVisibility(View.GONE);
			v=findViewById(R.id.setAutoSizeLbl);
			  v.setVisibility(View.GONE);
			  mAutosize.setVisibility(View.GONE);
			v=findViewById(R.id.setBlankPicLbl);
			  v.setVisibility(View.GONE);
			  mBlankPic.setVisibility(View.GONE);
			v=findViewById(R.id.setBgModeLbl);
			  v.setVisibility(View.GONE);
			  mBgMode.setVisibility(View.GONE);
			v=findViewById(R.id.setUseAccordLbl);
			  v.setVisibility(View.GONE);
			  mUseAccord.setVisibility(View.GONE);
			v=findViewById(R.id.setUseKottaLbl);
			  v.setVisibility(View.GONE);
			  mUseKotta.setVisibility(View.GONE);
			v=findViewById(R.id.setKottaAranyLbl);
			  v.setVisibility(View.GONE);
			  mKottaArany.setVisibility(View.GONE);
			v=findViewById(R.id.setAkkordAranyLbl);
			  v.setVisibility(View.GONE);
			  mAkkordArany.setVisibility(View.GONE);
			v=findViewById(R.id.setBordersLbl);
			  v.setVisibility(View.GONE);
			v=findViewById(R.id.setBorderLLbl);
			  v.setVisibility(View.GONE);
			  mBorderL.setVisibility(View.GONE);
			v=findViewById(R.id.setBorderTLbl);
			  v.setVisibility(View.GONE);
			  mBorderT.setVisibility(View.GONE);
			v=findViewById(R.id.setBorderRLbl);
			  v.setVisibility(View.GONE);
			  mBorderR.setVisibility(View.GONE);
			v=findViewById(R.id.setBorderBLbl);
			  v.setVisibility(View.GONE);
			  mBorderB.setVisibility(View.GONE);
		} else {	//net state
			mBkColorCk.setVisibility(View.GONE);
			mTxColorCk.setVisibility(View.GONE);
			mBlankColorCk.setVisibility(View.GONE);
			mHighColorCk.setVisibility(View.GONE);
			mFontSizeCk.setVisibility(View.GONE);
			mTitleSizeCk.setVisibility(View.GONE);
			mIndentCk.setVisibility(View.GONE);
			mSpacingCk.setVisibility(View.GONE);
			mVCenterCk.setVisibility(View.GONE);
			mHCenterCk.setVisibility(View.GONE);

			bkc|=0xFF000000;
			txc|=0xFF000000;
			blc|=0xFF000000;
			hic|=0xFF000000;
			if (fs<0) fs=-fs-1;
			if (ts<0) ts=-ts-1;
			if (li<0) li=-li-1;
			if (sp<0) sp=-sp-1;
			if (as== DiaProp.b3UNUSED) as= DiaProp.b3TRUE;
			if (vc== DiaProp.b3UNUSED) vc= DiaProp.b3FALSE;
			if (hc== DiaProp.b3UNUSED) hc= DiaProp.b3FALSE;
			if (ua== DiaProp.b3UNUSED) ua= DiaProp.b3TRUE;
			if (uk== DiaProp.b3UNUSED) uk= DiaProp.b3TRUE;
		}
		setBkColor(bkc);
		setTxColor(txc);
		setBlankColor(blc);
		setHighColor(hic);
		setInt(fs,mFontSize,mFontSizeCk);
		setInt(ts,mTitleSize,mTitleSizeCk);
		setB3(ut,mUseTitle,null);
		setInt(li,mIndent,mIndentCk);
		setSpinner(sp,mSpacing,mSpacingCk);
		setB3(as,mAutosize,null);
		setB3(vc,mVCenter,mVCenterCk);
		setB3(hc,mHCenter,mHCenterCk);
		setB3(ua,mUseAccord,null);
		setB3(uk,mUseKotta,null);
		mBlankPic.setText(G.nameOf(bp));
		setSpinner(bgm,mBgMode,null);
		if (ka<10||ka>200) ka=100;
		if (aa<10||aa>200) aa=100;
		mKottaArany.setSelection((ka-10)/10);
		mAkkordArany.setSelection((aa-10)/10);
		if (bat<0||bat>100) bat=0;
		if (blt<0||blt>100) blt=0;
		mBackTrans.setSelection(bat/10);
		mBlankTrans.setSelection(blt/10);
		mBorderL.setText(String.valueOf(brdl));
		mBorderT.setText(String.valueOf(brdt));
		mBorderR.setText(String.valueOf(brdr));
		mBorderB.setText(String.valueOf(brdb));
		mBold.setChecked(btxt);

		mPicDir=G.dirOf(bp);

		TextView tv = findViewById(R.id.setBlankPicLbl);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { selectBPic(); }
		});
		mBlankPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { selectBPic(); }
		});
		mFontSize.addTextChangedListener(new EditorWatch(mFontSize,mFontSizeCk));
		mTitleSize.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean on) {
				if (on) onEdClick(v);
				return;
			}
		});
		mIndent.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean on) {
				if (on) onEdClick(v);
				return;
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(G.idPROPMODE,mPropMode);
		outState.putInt(G.idINDEX,mIndex);
		toBundle(outState);
	}
	
	private class EditorWatch implements TextWatcher {
		private EditText mEd;
		private CheckBox mCb;
		public EditorWatch(EditText ed, CheckBox cb) {
			mEd=ed; mCb = cb;
		}
		@Override public void afterTextChanged(Editable s) {}
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!mCb.isChecked()) {
				mCb.setChecked(true);
				setStrikeThrough(mEd,false);
			}
		}
	}
	
	//////////////////////////
	
	private void toBundle(Bundle bd) {
		bd.putInt(G.idBKCOLOR,getBkColor());
		bd.putInt(G.idTXCOLOR,getTxColor());
		bd.putInt(G.idBLANKCOLOR,getBlankColor());
		bd.putInt(G.idHIGHCOLOR,getHighColor());
		bd.putInt(G.idFONTSIZE,getInt(mFontSize,mFontSizeCk));
		bd.putInt(G.idTITLESIZE,getInt(mTitleSize,mTitleSizeCk));
		bd.putInt(G.idLEFTINDENT,getInt(mIndent,mIndentCk));
		bd.putInt(G.idSPACING,getSpinner(mSpacing,mSpacingCk));
		bd.putByte(G.idAUTOSIZE,getB3(mAutosize,null));
		bd.putByte(G.idVCENTER,getB3(mVCenter,mVCenterCk));
		bd.putByte(G.idHCENTER,getB3(mHCenter,mHCenterCk));
		bd.putByte(G.idUSEAKKORD,getB3(mUseAccord,null));
		bd.putByte(G.idUSEAKKORD,getB3(mUseKotta,null));
		String s = mBlankPic.getText().toString();
		bd.putString(G.idBLANKPIC,mPicDir+s);
		bd.putInt(G.idBGMODE,getSpinner(mBgMode,null));
		bd.putInt(G.idKOTTAARANY,getSpinner(mKottaArany,null)*10+10);
		bd.putInt(G.idAKKORDARANY,getSpinner(mAkkordArany,null)*10+10);
		bd.putInt(G.idBACKTRANS,getSpinner(mBackTrans,null)*10);
		bd.putInt(G.idBLANKTRANS,getSpinner(mBlankTrans,null)*10);
		bd.putInt(G.idBORDERL,getVal(mBorderL));
		bd.putInt(G.idBORDERT,getVal(mBorderT));
		bd.putInt(G.idBORDERR,getVal(mBorderR));
		bd.putInt(G.idBORDERB,getVal(mBorderB));
		bd.putBoolean(G.idBOLDTEXT,mBold.isChecked());
	}
	
	private void setStrikeThrough(TextView tv, boolean on) {
		if (tv==null) return;
		if (on)
			tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
	}
	
	private int getVal(EditText et) {
		int res = 0;
		try {
			res = Integer.valueOf(et.getText().toString());
		} catch (Exception e) { res=0; }
		return res;
	}
	
	private int getBkColor() {
		int v = G.getColorOfBtn(mBkColorBtn);
		if (!mBkColorCk.isChecked()) v&=0x00FFFFFF;
		return v;
	}
	
	private void setBkColor(int color) {
		mBkColorCk.setChecked((color & 0xFF000000)!=0);
		G.setColorOfBtn(mBkColorBtn,color);
		setStrikeThrough(mBkColorBtn,!mBkColorCk.isChecked());
	}
	
	private int getTxColor() {
		int v = G.getColorOfBtn(mTxColorBtn);
		if (!mTxColorCk.isChecked()) v&=0x00FFFFFF;
		return v;
	}
	
	private void setTxColor(int color) {
		mTxColorCk.setChecked((color & 0xFF000000)!=0);
		G.setColorOfBtn(mTxColorBtn,color);
		setStrikeThrough(mTxColorBtn,!mTxColorCk.isChecked());
	}

	private int getBlankColor() {
		int v = G.getColorOfBtn(mBlankColorBtn);
		if (!mBlankColorCk.isChecked()) v&=0x00FFFFFF;
		return v;
	}

	private void setBlankColor(int color) {
		mBlankColorCk.setChecked((color&0xFF000000)!=0);
		G.setColorOfBtn(mBlankColorBtn,color);
		setStrikeThrough(mBlankColorBtn,!mBlankColorCk.isChecked());
	}
	
	private int getHighColor() {
		int v = G.getColorOfBtn(mHighColorBtn);
		if (!mHighColorCk.isChecked()) v&=0x00FFFFFF;
		return v;
	}

	private void setHighColor(int color) {
		mHighColorCk.setChecked((color & 0xFF000000)!=0);
		G.setColorOfBtn(mHighColorBtn,color);
		setStrikeThrough(mHighColorBtn,!mHighColorCk.isChecked());
	}
	
	private void setInt(int v, EditText ed, CheckBox cb) {
		if (cb!=null) cb.setChecked(v>=0);
		if (v<0) v=-v-1;
		ed.setText(String.valueOf(v));
		if (cb!=null) setStrikeThrough(ed,!cb.isChecked());
	}
	
	private int getInt(EditText ed, CheckBox cb) {
		int res = getVal(ed);
		if (cb!=null && !cb.isChecked()) res=-res-1;
		return res;
	}
	
	private void setSpinner(int v, Spinner sp, CheckBox cb) {
		if (cb!=null) cb.setChecked(v<0);
		if (v<0) v=-v-1;
		sp.setSelection(v);
		
		if (cb!=null) setStrikeThrough((TextView)sp.getSelectedView(),cb.isChecked());
	}
	
	private int getSpinner(Spinner sp, CheckBox cb) {
		int res = sp.getSelectedItemPosition();
		if (cb!=null && !cb.isChecked()) res=-res-1;
		return res;
	}
	
	private void setB3(byte b3, ToggleButton tb, CheckBox cb) {
		if (cb!=null) cb.setChecked(b3!= DiaProp.b3UNUSED);
		tb.setChecked(DiaProp.B3ToBool(b3));
		if (cb!=null) setStrikeThrough(tb,!cb.isChecked());
	}
	
	private byte getB3(ToggleButton tb, CheckBox cb) {
		if (cb!=null && !cb.isChecked()) return DiaProp.b3UNUSED;
		return DiaProp.BoolToB3(tb.isChecked());
	}
	
	////////////////////////
	
	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		if (mPropMode) {
			Intent it = new Intent();
			Bundle bd = new Bundle();
			toBundle(bd);
			it.replaceExtras(bd);
			it.putExtra(G.idINDEX,mIndex);
			setResult(RESULT_OK,it);
			finish();
			return;
		}
		G.sBkColor=getBkColor();
		G.sTxColor=getTxColor();
		G.sBlankColor=getBlankColor();
		G.sHighColor=getHighColor();
		G.sFontSize=getVal(mFontSize);
		G.sTitleSize=getVal(mTitleSize);
		G.sIndent=getVal(mIndent);
		G.sSpacing=mSpacing.getSelectedItemPosition();
		G.sAutosize=mAutosize.isChecked();
		G.sVCenter=mVCenter.isChecked();
		G.sHCenter=mHCenter.isChecked();
		G.sUseAkkord=mUseAccord.isChecked();
		G.sUseKotta=mUseKotta.isChecked();
		G.sUseTitle=mUseTitle.isChecked();
		String s = mBlankPic.getText().toString();
		G.sBlankPic=(s.isEmpty() ? "" : mPicDir+s);
		if (!mPicDir.isEmpty()) G.sPicDir=mPicDir;
		G.sBgMode=mBgMode.getSelectedItemPosition();
		G.sKottaArany=mKottaArany.getSelectedItemPosition()*10+10;
		G.sAkkordArany=mAkkordArany.getSelectedItemPosition()*10+10;
		G.sBackTrans=mBackTrans.getSelectedItemPosition()*10;
		G.sBlankTrans=mBlankTrans.getSelectedItemPosition()*10;
		G.sBorderL=getVal(mBorderL);
		G.sBorderT=getVal(mBorderT);
		G.sBorderR=getVal(mBorderR);
		G.sBorderB=getVal(mBorderB);
		G.sBoldText=mBold.isChecked();
		setResult(RESULT_OK);
		finish();
	}
	
	public void onCbClick(View v) {
		CheckBox cb=(CheckBox)v;
		TextView tv = null;
		if (cb==mBkColorCk) tv=mBkColorBtn;
		else if (cb==mTxColorCk) tv=mTxColorBtn;
		else if (cb==mBlankColorCk) tv=mBlankColorBtn;
		else if (cb==mHighColorCk) tv=mHighColorBtn;
		else if (cb==mFontSizeCk) tv=mFontSize;
		else if (cb==mTitleSizeCk) tv=mTitleSize;
		else if (cb==mIndentCk) tv=mIndent;
		else if (cb==mSpacingCk) tv=(TextView)mSpacing.getSelectedView();
		else if (cb==mHCenterCk) tv=mHCenter;
		else if (cb==mVCenterCk) tv=mVCenter;
		
		if (tv!=null) setStrikeThrough(tv,!cb.isChecked());
	}
	
	public void onEdClick(View v) {
		CheckBox cb;
		if (v==mFontSize) cb=mFontSizeCk;
		else if (v==mTitleSize) cb=mTitleSizeCk;
		else if (v==mIndent) cb=mIndentCk;
		else if (v==mSpacing) { cb=mSpacingCk; v=mSpacing.getSelectedView(); }
		else if (v==mHCenter) cb=mHCenterCk;
		else if (v==mVCenter) cb=mVCenterCk;
		else return;
		cb.setChecked(true);
		setStrikeThrough((TextView)v, false);
	}
	
	///////////////////////////
	
	static private final int REQUEST_BCOLOR = 201;
	static private final int REQUEST_TCOLOR = 202;
	static private final int REQUEST_NCOLOR = 203;
	static private final int REQUEST_HCOLOR = 204;
	static private final int REQUEST_BFILE  = 205;
	
	public void selectBPic() {
		if (!mBlankPic.getText().toString().isEmpty()) {
			mBlankPic.setText("");
			return;
		}
		Intent it = new Intent(this,FileSelectorActivity.class);
		it.putExtra(G.idFTYPE,FileSelectorActivity.ftPIC);
		String s = (mPicDir.isEmpty() ? G.sPicDir : mPicDir);
		it.putExtra(G.idDIR,s);
		startActivityForResult(it,REQUEST_BFILE);
	}
	
	public void reqBFile(int resultCode, Intent data) {
		if (resultCode!=RESULT_OK) return;
		mPicDir=data.getStringExtra(G.idDIR);
		mBlankPic.setText(data.getStringExtra(G.idFNAME));
	}
	
	public void onColorBtn(View v) {
		int req;
		CheckBox cb;
		if (v==mBkColorBtn) {
			req=REQUEST_BCOLOR;
			cb=mBkColorCk;
		} else if (v==mTxColorBtn) {
			req=REQUEST_TCOLOR;
			cb=mTxColorCk;
		} else if (v==mBlankColorBtn) {
			req=REQUEST_NCOLOR;
			cb=mBlankColorCk;
		} else if (v==mHighColorBtn) {
			req=REQUEST_HCOLOR;
			cb=mHighColorCk;
		} else return;
		cb.setChecked(true);
		setStrikeThrough((TextView)v,false);
		Intent it = new Intent(this, ColorDlg.class);
		it.putExtra(G.idCOLOR,G.getColorOfBtn((Button)v));
		startActivityForResult(it,req);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode==REQUEST_BFILE) {
			reqBFile(resultCode,data);
			return;
		}
		Button b;
		if (requestCode==REQUEST_BCOLOR) b=mBkColorBtn;
		else if (requestCode==REQUEST_TCOLOR) b=mTxColorBtn;
		else if (requestCode==REQUEST_NCOLOR) b=mBlankColorBtn;
		else if (requestCode==REQUEST_HCOLOR) b=mHighColorBtn;
		else return;
		if (resultCode!=RESULT_OK) return;
		int c = data.getIntExtra(G.idCOLOR,0);
		G.setColorOfBtn(b,c);
	}
}

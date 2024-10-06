package diatar.eu;

import android.app.ActionBar;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.graphics.drawable.*;
import java.util.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import diatar.eu.net.*;
import eu.diatar.library.*;
import diatar.eu.utils.*;
import java.io.*;

public class MainActivity extends MainMenu 
{
	public TxTar Dtx;
	public Spinner DtxLst,EnekLst,DiaLst;
	private View mainPgFrame, mainPagerBg;
	private ViewPager DiaPager;
	private MainPagerAdapter MPAdapter;
	private ImageButton mShowBtn;
	private ImageButton mPrevHBtn, mNextHBtn;
	private LinearLayout mMainCtrl1, mMainCtrl2;
	
	TcpClient mTcp;
	//ujratolteshez
	private int posDtx,posEnek,posDia;
	//elozo listapoziciok
	private int DtxIdx=-2, EnekIdx=-2, DiaIdx=-2, PageIdx=-2;
	private int mCurrDtx; //0=dia v semmi
	
	private static class tGroup {
		String Name;
		ArrayList<Integer> DtxLst;
	}
	private ArrayList<tGroup> mGrpLst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		G.Load(this);
		Dtx = TxTar.Create(this);
		mTcp = TcpClient.get(this);
		TxtSizer.setDensity(this);
		if (G.sAlwaysOn)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);
		DtxLst = findViewById(R.id.DtxLst);
		EnekLst = findViewById(R.id.EnekLst);
		DiaLst = findViewById(R.id.DiaLst);
		DiaPager = findViewById(R.id.mainPager);
		mainPgFrame = findViewById(R.id.mainPgFrame);
		mainPagerBg = findViewById(R.id.mainPagerBg);
		mShowBtn = findViewById(R.id.ShowBtn);
		mPrevHBtn = findViewById(R.id.PrevHBtn);
		mNextHBtn = findViewById(R.id.NextHBtn);
		mMainCtrl1 = findViewById(R.id.MainControls1);
		mMainCtrl2 = findViewById(R.id.MainControls2);
		
		MPAdapter = new MainPagerAdapter(getSupportFragmentManager());
		DiaPager.setPageMargin(2);
		DiaPager.setPageMarginDrawable(new ColorDrawable(0xFFFFFFFF));
		DiaPager.setAdapter(MPAdapter);
		mainPagerBg.setBackgroundColor(G.sBkColor);
		
		FillGrpLst();
		
		posDtx=0; G.sDiaFname=""; mCurrDtx=DtxPos2Idx(0);
		if (savedInstanceState!=null)
			loadState(savedInstanceState);

		Reload(mCurrDtx,posEnek,posDia);

		DtxLst.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
				int ix = DtxPos2Idx(pos);
				if (ix==mCurrDtx) return;
				SetAllIdx(ix,-1,-1,-1);
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		EnekLst.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
					SetAllIdx(-1,pos,-1,-1);
				}
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
		DiaLst.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View v, int pos, long id) {
					SetAllIdx(-1,-1,pos,-1);
				}
				public void onNothingSelected(AdapterView<?> arg0) {}
			});
		DiaPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override public void onPageScrolled(int p1, float p2, int p3) {}
			@Override public void onPageScrollStateChanged(int p1) {}
			@Override public void onPageSelected(int p1) {
				SetAllIdx(-1,-1,-1,p1);
				DiaPager.invalidate();
			}
		});
		
		mTcp.Open();
		sendNetCurrDia();
		sendNetBlank();
		setShowState();
		
		if (savedInstanceState==null) ChkNet();
	}

	@Override
	protected void onDestroy()
	{
		mTcp.Stop();
		mTcp.clearMain();
		mTcp=null;
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		saveState(outState);
	}
	
	private void loadState(Bundle st) {
		mCurrDtx=st.getInt(G.idDTXINDEX);
		posDtx=DtxIdx2Pos(mCurrDtx);
		posEnek=st.getInt(G.idENEKINDEX);
		posDia=st.getInt(G.idDIAINDEX);
		G.sDiaFname=st.getString(G.idFNAME);
	}
	
	private void saveState(Bundle st) {
		st.putInt(G.idDTXINDEX,mCurrDtx);
		st.putInt(G.idENEKINDEX,EnekLst.getSelectedItemPosition());
		st.putInt(G.idDIAINDEX,DiaLst.getSelectedItemPosition());
		st.putString(G.idFNAME,G.sDiaFname);
	}
	
	public void ChkNet() {
		if (G.sDownWhen>=3) return;
		if (G.sDownWhen>0) {
			long diff = (System.currentTimeMillis()/(24*60*60*1000))-G.sDownLastDay;
			if (G.sDownWhen==1 && diff<1) return;
			if (G.sDownWhen==2 && diff<31) return;
		}
		ConnectInternet(false);
	}

	public void ConnectInternet(boolean verbose) {
		final DownloadDtxList ddl;
		ddl = new DownloadDtxList(this, verbose) {
			@Override
			public void Finished() {
				SelectForDownload sfd = new SelectForDownload(MainActivity.this, this);
				sfd.Run();
			}
		};
		ddl.execute();
	}
	
	public void EndNetRefresh() {
		G.sDownLastDay = System.currentTimeMillis()/(24*60*60*1000);
		G.Save(this);
	}
	
	private boolean InSetAllIdx = false;
	
	private void SetAllIdx(int kx, int ex, int dx, int px) {
		if (InSetAllIdx) return;
		InSetAllIdx=true;
		boolean poschanged=false;
		if (px>=0) { //page alapjan enek/dia
			int[] xx;
			if (isDia(mCurrDtx)) {
				DiaItem d = DiaItem.getByPos(px-1);
				if (px>PageIdx) {
					if (d!=null && d.mProps.mDblDia) px++;
					if (px>=DiaItem.getCount()) {
						px-=2;
						DiaPager.setCurrentItem(px);
					}
				} else {
					if (d!=null && d.mProps.mDblDia) px--;
				}
				xx = DiaItem.Pos2Idx(px);
			} else
				xx=Dtx.Pos2Idx(px);
			ex=xx[0]; dx=xx[1];
		}
		if (kx>=0 && kx!=DtxIdx) {
			DtxIdx=kx; poschanged=true;
			setCurrDtx(kx);
			EnekIdx=-2; fillEnekLst(kx);
			MPAdapter.Cnt=getCount();
			MPAdapter.notifyDataSetChanged();
			if (ex<0) ex=0;
		}
		if (ex>=0 && ex!=EnekIdx) {
			if (ex>EnekIdx) {
				while (ex+1<EnekLst.getCount()) {
					if (!EnekLst.getItemAtPosition(ex).toString().startsWith("-- ")) break;
					ex++;
					if (px>=0) px++;
				}
			} else {
				while (ex>0) {
					if (!EnekLst.getItemAtPosition(ex).toString().startsWith("-- ")) break;
					ex--;
					if (px>=0) px--;
				}
			}
			EnekIdx=ex; poschanged=true;
			if (ex!=EnekLst.getSelectedItemPosition())
				EnekLst.setSelection(ex);
			DiaIdx=-2; fillVersszakLst(ex);
			if (dx<0) dx=0;
		}
		if (dx>=0 && dx!=DiaIdx) {
			DiaIdx=dx; poschanged=true;
			if (dx!=DiaLst.getSelectedItemPosition())
				DiaLst.setSelection(dx);
		}
		if (px<0 && poschanged) {
			if (isDia(DtxIdx))
				px=DiaItem.Idx2Pos(EnekIdx,DiaIdx);
			else
				px=Dtx.Idx2Pos(EnekIdx,DiaIdx);
		}
		if (px>=0 && px!=PageIdx) {
			PageIdx=px;
			if (px!=DiaPager.getCurrentItem())
				DiaPager.setCurrentItem(px);
			sendNetCurrDia();
		}
		InSetAllIdx=false;
	}
	
	public void sendNetCurrDia() {
		int kx=DtxIdx, ex=EnekIdx, vx=DiaIdx;
		DiaItem d = null;
		if (isDia(kx)) {
			d=DiaItem.getByPos(PageIdx);
			if (d!=null) {
				kx=d.mKotet+1; ex=d.mVers; vx=d.mVszak;
			}
		} //else if (G.sDiaFname.length()>0) kx--;
		RecBase rec;
		byte rtyp;
		if (d!=null && d.mTipus==DiaItem.ditPIC) {
			File f = new File(d.mKnev,d.mVnev);
			RecPic rp = new RecPic(8+(int)f.length());
			rp.setLen(rp.getMaxlen());
			String ext = f.getName();
			ext=ext.substring(ext.lastIndexOf('.')+1);
			rp.setExt(ext);
			try {
				try (FileInputStream fis = new FileInputStream(f)) {
					fis.read(rp.buf, 8, (int) f.length());
				}
			} catch(Exception e) { }
			rec=rp; rtyp=RecHdr.itPic;
		} else {
			String title="";
			String[] arr=null;
			if (d == null || d.mTipus == DiaItem.ditDTX) {
				title = Dtx.getDiaTitle(this, kx - 1, ex, vx);
				arr = getDiaTxt(PageIdx);
			} else if (d.mTipus == DiaItem.ditTXT) {
				title = d.getDiaTitle(false);
				arr = getDiaTxt(PageIdx);
			}
			rec = RecText.Create(title,arr);
			rtyp=RecHdr.itText;
		}
		G.sHighPos=0;
		DiaItem.setupRecState(d,mTcp.mStateToSend);
		mTcp.sendState();
		mTcp.setRecToSend(rec,rtyp);
		//if (DiaItem.setupRecState(d,mTcp.mStateToSend))
		//mTcp.sendState();
	}
	
	public void sendNetBlank() {
		RecBlank rb = null;
		if (!G.sBlankPic.isEmpty()) {
			rb = new RecBlank(0);
			String err = rb.loadFile(G.sBlankPic);
			if (!err.isEmpty()) Err(err);
		}
		mTcp.setBlankToSend(rb);
	}
	
	public int DtxPos2Idx(int pos) {
		if (G.sDiaFname.length()>0) pos--;
		if (pos<0) return 0;
		for (tGroup g : mGrpLst) {
			pos--;
			for (Integer i : g.DtxLst) {
				pos--;
				if (pos<0) return i+1;
			}
		}
		return -1;
	}
	
	public int DtxIdx2Pos(int idx) {
		int res = (G.sDiaFname.length()>0 ? 0 : -1);
		if (idx<=0) return res;
		idx--;
		for (tGroup g : mGrpLst) {
			res++;
			for (Integer i : g.DtxLst) {
				res++;
				if (i==idx) return res;
			}
		}
		return -1;
	}
	
	//false = nem valtozott
	public boolean setCurrDtx(int dtxidx) {
		mCurrDtx=dtxidx;
		int p = DtxIdx2Pos(dtxidx);
		if (p==DtxLst.getSelectedItemPosition())
			return false;
		DtxLst.setSelection(p);
		return true;
	}
	
	public void FillGrpLst() {
		String[] narr = Dtx.getNames();
		String[] garr = Dtx.getGrpNames();
		mGrpLst = new ArrayList<>();
		tGroup egrp = new tGroup();
		egrp.Name="(nem besorolt)";
		egrp.DtxLst = new ArrayList<>();
		for (int i=0; i<narr.length; i++) {
			String gnam = garr[i];
			if (gnam.isEmpty()) {
				egrp.DtxLst.add(i);
				continue;
			}
			boolean found=false;
			for (tGroup g : mGrpLst) {
				if (g.Name.equalsIgnoreCase(gnam)) {
					g.DtxLst.add(i);
					found=true;
					break;
				}
			}
			if (found) continue;
			tGroup grp = new tGroup();
			grp.Name=gnam;
			grp.DtxLst = new ArrayList<>();
			grp.DtxLst.add(i);
			mGrpLst.add(grp);
		}
		if (egrp.DtxLst.size()>0) mGrpLst.add(egrp);
	}
	
	public void ReloadAll() {
		Dtx.LoadNames(this);
		FillGrpLst();
		Reload(0,0,0);
	}
	
	public void Reload(int px, int pe, int pd) {
		ArrayList<String> xlst = new ArrayList<>();
		if (G.sDiaFname.length()>0) xlst.add("DIA: "+G.sDiaFname);
		String[] xarr = Dtx.getNames();
		for (tGroup g : mGrpLst) {
			xlst.add(g.Name);
			for (Integer ix : g.DtxLst)
				xlst.add("    "+xarr[ix]);
		}
		//if (xlst.
		ArrayAdapter<String> adp = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,xlst);
		DtxLst.setAdapter(adp);
		DtxIdx=-2; PageIdx=-2;
		SetAllIdx(px,pe,pd,-1);
	}
	
	public void fillEnekLst(int DtxIndex) {
		String[] arr=null;
		if (G.sDiaFname.length()>0 && DtxIndex==0)
			arr=DiaItem.getEnekLst();
		if (DtxIndex>0) DtxIndex--;
		if (arr==null) arr=Dtx.getEnekLst(this,DtxIndex);
		ArrayAdapter<String> lst = new ArrayAdapter<>(this,
			android.R.layout.simple_spinner_item,
			arr);
		EnekLst.setAdapter(lst);
	}
	
	public void fillVersszakLst(int EnekIndex) {
		String[] arr=null;
		int DtxIndex = mCurrDtx;
		if (G.sDiaFname.length()>0 && DtxIndex==0)
			arr=DiaItem.getVersszakLst(EnekIndex);
		if (DtxIndex>0) DtxIndex--;
		if (arr==null) arr=Dtx.getVersszakLst(this,DtxIndex,EnekIndex);
		ArrayAdapter<String> lst = new ArrayAdapter<>(this,
			android.R.layout.simple_spinner_item,
			arr);
		DiaLst.setAdapter(lst);
		//DiaPager.setCurrentItem(0);
	}
	
	public boolean isDia(int kotet) {
		if (kotet<0) kotet=mCurrDtx;
		return (kotet==0 && G.sDiaFname.length()>0);
	}
	
	public int getCount() {
		if (isDia(-1))
			return DiaItem.getCount();
		else
			return TxTar.Get().getCount();
	}
	
	public int getDiaTip(int pos) {
		if (isDia(-1))
			return DiaItem.getDiaTip(pos);
		//minden enektar szoveg
		return DiaItem.ditTXT;
	}
	
	public DiaItem getDia(int pos) {
		if (isDia(-1)) return DiaItem.getByPos(pos);
		return null;
	}

	public String[] getDiaTxt(int pos) {
		String[] res;
		if (isDia(-1)) {
			res=DiaItem.getDiaTxt(this,pos);
			return res;
		}
		int[] xx=TxTar.Get().Pos2Idx(pos);
		res=TxTar.Get().getDiaTxt(this,TxTar.CurrKotet,xx[0],xx[1]);
		return res;
	}
	
	private void setShowState() {
		mShowBtn.setImageResource(
			G.sShowing ? R.drawable.pirosgomb2 : R.drawable.pirosgomb1);
		mainPgFrame.setBackgroundColor(
			G.sShowing ? 0xFFFF0000 : 0xFF00FF00);
		mTcp.setStateProjecting(G.sShowing);
	}
	
	private void setFullScr(boolean on) {
		if (on==G.sIsFullScr) return;
		G.sIsFullScr=on;
		if (on) {
			mMainCtrl1.setVisibility(View.GONE);
			mMainCtrl2.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT < 16) {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
									 WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				View decorView = getWindow().getDecorView();
				int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
					// Set the content to appear under the system bars so that the
					// content doesn't resize when the system bars hide and show.
					//| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					//| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					// Hide the nav bar and status bar
					//| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					;
				decorView.setSystemUiVisibility(uiOptions);
				// Remember that you should never show the action bar if the
				// status bar is hidden, so hide that too if necessary.
				ActionBar actionBar = getActionBar();
				if (actionBar!=null) actionBar.hide();
			}
		} else {
			mMainCtrl1.setVisibility(View.VISIBLE);
			mMainCtrl2.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT < 16) {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
									 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			} else {
				View decorView = getWindow().getDecorView();
				int uiOptions = 
					// Set the content to appear under the system bars so that the
					// content doesn't resize when the system bars hide and show.
					//View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					0 //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					;
				decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
				ActionBar actionBar = getActionBar();
				if (actionBar!=null) actionBar.show();
			}
		}
	}
	
	//////////////////////////////
	//////////////////////////////
	
	public void Msg(String txt) {
		TxTar.Msg(this,txt);
	}
	
	public void Err(String txt) {
		TxTar.Msg(this,txt);
	}
	
	public void flipShowState() {
		G.sShowing=!G.sShowing;
		setShowState();
	}
	
	public void flipFullScr() {
		setFullScr(!G.sIsFullScr);
	}
	
	//////////////////////////////
	//////////////////////////////
	
	public void onPrevEBtn(View v) {
		int p=EnekLst.getSelectedItemPosition()-1;
		while (p>=0) {
			if (!EnekLst.getItemAtPosition(p).toString().startsWith("-- ")) break;
			p--;
		}
		if (p>=0) EnekLst.setSelection(p);
	}
	
	public void onNextEBtn(View v) {
		int p=EnekLst.getSelectedItemPosition()+1;
		while (p<EnekLst.getCount()) {
			if (!EnekLst.getItemAtPosition(p).toString().startsWith("-- ")) break;
			p++;
		}
		if (p<EnekLst.getCount()) EnekLst.setSelection(p);
	}
	
	public void onPrevDBtn(View v) {
		int p=DiaLst.getSelectedItemPosition()-1;
		if (isDia(-1)) {
			DiaItem d = DiaItem.getByIndex(EnekLst.getSelectedItemPosition(), p+1);
			if (d!=null) d=d.mPrev;
			if (d!=null && d.mPrev!=null && d.mPrev.mProps.mDblDia) p--;
		}
		if (p>=0)
			DiaLst.setSelection(p);
		else {
			int ex=EnekLst.getSelectedItemPosition()-1;
			while (ex>=0) {
				if (!EnekLst.getItemAtPosition(ex).toString().startsWith("-- ")) break;
				ex--;
			}
			if (ex<0) return;
			EnekIdx=ex;
			EnekLst.setSelection(ex);
			fillVersszakLst(ex);
			p=(DiaLst.getCount()-1);
			DiaIdx=-1;
			if (isDia(-1)) {
				DiaItem d = DiaItem.getByIndex(ex,p);
				if (d!=null && d.mPrev!=null && d.mPrev.mProps.mDblDia) p--;
			}
			DiaLst.setSelection(p);
			SetAllIdx(-1,-1,p,-1);
		}
	}
	
	public void onNextDBtn(View v) {
		int p=DiaLst.getSelectedItemPosition()+1, n=DiaLst.getCount();
		if (isDia(-1)) {
			DiaItem d = DiaItem.getByIndex(EnekLst.getSelectedItemPosition(), p-1);
			if (d!=null && d.mProps.mDblDia) p++;
		}
		if (p<n)
			DiaLst.setSelection(p);
		else {
			p=EnekLst.getSelectedItemPosition()+1;
			while (p<EnekLst.getCount()) {
				if (!EnekLst.getItemAtPosition(p).toString().startsWith("-- ")) break;
				p++;
			}
			if (p>=EnekLst.getCount()) return;
			EnekLst.setSelection(p);
		}
	}
	
	public void onShowBtn(View v) {
		flipShowState();
	}
	
	public void onPrevHBtn(View v) {
		MainSlideFragment fr = MPAdapter.mCurrFragment;
		if (fr==null) return;
		View frv = fr.getView();
		if (frv instanceof MainTxtView) {
			MainTxtView tv = (MainTxtView)frv;
			int ix = tv.getHighPoint();
			if (ix>0) {
				G.sHighPos=tv.setHighPoint(ix-1);
				mTcp.setStateHighlight(G.sHighPos);
			}
		}
	}
	
	public void onNextHBtn(View v) {
		MainSlideFragment fr = MPAdapter.mCurrFragment;
		if (fr==null) return;
		View frv = fr.getView();
		if (frv instanceof MainTxtView) {
			MainTxtView tv = (MainTxtView)frv;
			int ix = tv.getHighPoint();
			if (ix<tv.getWordCnt()) {
				G.sHighPos=tv.setHighPoint(ix+1);
				mTcp.setStateHighlight(G.sHighPos);
			}
		}
	}
	
	///////////////////////////////
	///////////////////////////////
	
//	public void OnTcpConnect() {
//		mTcp.fillState();
//		sendNetCurrDia();
//		sendNetBlank();
//		mTcp.setStateProjecting(G.sShowing);
//	}

	///////////////////////////////
	///////////////////////////////

	//@Override
	//protected void onMenuEdit() {
	//	
	//}
	
	//@Override
	//protected void onMenuSave() {
	//	Intent it = new Intent(this,FileSelectorActivity.class);
	//	it.putExtra(G.idDIR,G.sLoadDir);
	//	it.putExtra(G.idFNAME,G.sDiaFname);
	//	it.putExtra(G.idSAVEMODE,true);
	//	startActivityForResult(it,REQUEST_FILESAVE);
	//}
	
	@Override
	protected void onMenuExit() {
		mTcp.Stop();
		mTcp=null;
		finish();
	}

	@Override
	protected void onMenuStopProg() {
		mTcp.setStateStop(false);
	}

	@Override
	protected void onMenuStopShutdown() {
		mTcp.setStateStop(true);
	}

	@Override
	protected void whenLoaded(boolean success, String fname) {
		if (success) {
			G.sDiaFname=FileSelectorActivity.SubDiaExt(fname);
			TxTar.Msg(this,G.sDiaFname+" betöltve.");
		} else {
			if (DiaItem.getCount()<=0) G.sDiaFname="";
		}
		ReloadAll();
		MPAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void whenSetNeted() {
		G.Save(this);
		mTcp.fillState();
		mTcp.Open();
		sendNetCurrDia();
		sendNetBlank();
		mTcp.setStateProjecting(G.sShowing);
	}
	
	@Override
	protected void whenSetDowned(boolean downloadnow) {
		G.Save(this);
		if (downloadnow) ConnectInternet(true);
		if (G.sAlwaysOn)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	protected void whenSetProjed() {
		G.Save(this);
		ReloadAll();
		mTcp.fillState();
		mTcp.setStateProjecting(G.sShowing);
		MPAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void whenEdited() {
		int ix=1;
		if (DiaItem.getCount()<=0) {
			G.sDiaFname="";
		} else {
			ix--;
			if (G.sDiaFname.isEmpty())
				G.sDiaFname="(nevtelen)";
		}
		//ReloadAll();
		Reload(ix,0,0);
	}

	///////////////////////
	// requests
	///////////////////////
	
	//private void reqFilesave(int resultCode, Intent data) {
	//	if (resultCode!=RESULT_OK) return;
	//	String dir=data.getStringExtra(G.idDIR);
	//	String fname=data.getStringExtra(G.idFNAME);
	//	DiaSaver ds = new DiaSaver();
	//	String res = ds.Save(dir+fname);
	//	if (res.isEmpty()) {
	//		res=fname+" mentve.";
	//		G.sLoadDir=dir; G.sDiaFname=fname;
	//	}
	//	TxTar.Msg(this,res);
	//}
	
	//////////////////////
	// MainPagerAdapter
	//////////////////////
	
	private static class MainPagerAdapter extends FragmentPagerAdapter {
		public MainSlideFragment mCurrFragment;
		
		private int Cnt;
		public MainPagerAdapter(androidx.fragment.app.FragmentManager fm) {
			super(fm);
		}
		public int getCount() {
			return Cnt;
		}
		
		@NonNull
		@Override
		public androidx.fragment.app.Fragment getItem(int pos) {
			MainSlideFragment fr = new MainSlideFragment();
			Bundle args = new Bundle();
			args.putInt(MainSlideFragment.argPOS,pos);
			fr.setArguments(args);
			return (androidx.fragment.app.Fragment)fr;
		}

		@Override
		public int getItemPosition(@NonNull Object object) {
			return POSITION_NONE;
		}
		
		@Override
		public void setPrimaryItem(@NonNull ViewGroup vg, int pos, @NonNull Object obj) {
			mCurrFragment=(MainSlideFragment)obj;
			if (mCurrFragment!=null && mCurrFragment.getView() instanceof MainTxtView) {
				MainTxtView v = (MainTxtView)mCurrFragment.getView();
				if (v!=null && v.getHighPoint()!=0) {
					v.setHighPoint(0);
					G.sHighPos=0;
					v.invalidate();
				}
			}
			super.setPrimaryItem(vg,pos,obj);
		}
		
	}
} /**/

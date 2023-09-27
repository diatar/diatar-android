package diatar.eu;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import java.io.*;
import java.util.*;
import android.*;
import androidx.core.app.ActivityCompat;

public class DiaLoader extends Activity
{
	public final static String itDIR = "dir";
	public final static String itFNAME = "fname";
	
	public TextView Lbl;
	public ProgressBar Prg;
	public String dir,fname;
	private static DI_Loader dil = null;
	private boolean alreadyfinished;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCompat.requestPermissions(
			this,
			new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
			1);
		
		setContentView(R.layout.dialoader);
		Lbl = (TextView)findViewById(R.id.dlLbl);
		Prg = (ProgressBar)findViewById(R.id.dlProgress);
		
		alreadyfinished=false;
		Intent it = getIntent();
		dir=it.getStringExtra(itDIR);
		fname=it.getStringExtra(itFNAME);
		
		setTitle("Betöltés: "+fname);
		dil = new DI_Loader();
		dil.execute(this);
	}
	
	public void onCancel(View v) {
		dil.cancel(false);
		finished(false);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		dil.cancel(false);
	}
	
	public void finished(boolean ok) {
		if (alreadyfinished) return;
		alreadyfinished=true;
		if (!ok) DiaItem.Clear();
		Intent rit = new Intent();
		rit.putExtra(itDIR,dir);
		rit.putExtra(itFNAME,fname);
		setResult(( ok ? RESULT_OK : RESULT_CANCELED),rit);
		finish();
	}
}

class DI_Loader extends AsyncTask<DiaLoader,String,String> {
	private DiaLoader ctx;
	private int perc;
	private String[] grplst;
	
	private final static String NODATA = "\n";
	
	@Override
	protected String doInBackground(DiaLoader[] p1) {
		ctx=p1[0];
		return LoadFile();
	}

	@Override
	protected void onCancelled() {
		ctx.finished(false);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.length()>0) TxTar.Msg(ctx,result);
		ctx.finished(true);
	}

	@Override
	protected void onProgressUpdate(String[] values) {
		ctx.Lbl.setText(values[0]);
		ctx.Prg.setProgress(perc);
	}
	
	private void SetPercent(String txt,int percent) {
		perc=percent;
		publishProgress(txt+String.format(" %d%%",percent));
	}
	
	private DiaProp LoadProp() {
		DiaProp res = new DiaProp();
		res.mBkColor=ReadInt("bkcolor",-1);
			res.mBkColor = (res.mBkColor<0 ? 0 : res.mBkColor|0xFF000000);
		res.mTxColor=ReadInt("txcolor",-1);
			res.mTxColor = (res.mTxColor<0 ? 0 : res.mTxColor|0xFF000000);
		res.mHiColor=ReadInt("hicolor",-1);
			res.mHiColor = (res.mHiColor<0 ? 0 : res.mHiColor|0xFF000000);
		res.mBlankColor=ReadInt("offcolor",-1);
			res.mBlankColor = (res.mBlankColor<0 ? 0 : res.mBlankColor|0xFF000000);
		res.mFontName=ReadStr("fontname","");
		res.mFontSize=ReadInt("fontsize",-1);
		res.mTitleSize=ReadInt("titlesize",-1);
		res.mIndent=ReadInt("indent",-1);
		res.mSpacing=ReadInt("spacing",-1);
		res.mFontBold=(byte)ReadInt("fontbold",res.b3UNUSED);
		res.mHCenter=(byte)ReadInt("hcenter",res.b3UNUSED);
		res.mVCenter=(byte)ReadInt("vcenter",res.b3UNUSED);
		res.mDblDia=ReadBool("dbldia", false);
		return res;
	}

	private DiaItem LoadItem() {
		String k,v,s,id;
		s=ReadStr("separator",NODATA);
		if (!s.equals(NODATA)) {
			DiaItem d = new DiaItem(DiaItem.ditSEPAR);
			d.mKnev=s;
			return d;
		}
		s=ReadStr("caption",NODATA);
		if (!s.equals(NODATA)) {
			DiaItem d = new DiaItem(DiaItem.ditTXT);
			d.mKnev=s;
			int n = ReadInt("lines",0);
			String[] txt = new String[n];
			for (int i=0; i<n; i++)
				txt[i]=ReadStr("line"+i,"");
			d.mTxt=txt;
			return d;
		}
		s=ReadStr("kep","");
		if (!s.isEmpty()) {
			DiaItem d = new DiaItem(DiaItem.ditPIC);
			File f = new File(s);
			d.mKnev=f.getParent()+f.separator;
			d.mVnev=f.getName();
			return d;
		}
		k=ReadStr("kotet","");
		v=ReadStr("enek","");
		s=ReadStr("versszak","");
		id=ReadStr("id","");
		if (k.length()>0 && v.length()>0) {
			DiaItem d = new DiaItem(DiaItem.ditDTXstr);
			d.mKnev=k; d.mVnev=v; d.mSnev=s; d.mSid=id;
			d.mProps=LoadProp();
			return d;
		}
		if (id.length()>0) {
			DiaItem d = new DiaItem(DiaItem.ditDTXid);
			d.mSid=id;
			d.mProps=LoadProp();
			return d;
		}
		
		return null;
	}
	
	private String LoadFile() {
		DiaItem.Clear();
		String err=ReadGrp("main");
		if (err.length()>0) return err;
		DiaItem.sCommonProps=LoadProp();
		int cnt = ReadInt("diaszam",0);
		for (int i=1; i<=cnt; i++) {
			SetPercent("Beolvasás...",(i*25)/cnt);
			if (isCancelled()) return "";
			err=ReadGrp(Integer.toString(i));
			if (err.length()>0) return err;
			DiaItem d = LoadItem();
			if (d!=null) d.AppendMe();
		}
		RealizeItemList();
		int nskip=cnt-DiaItem.getCount();
		if (nskip>0) return String.format("%d dia, ebből %d ismeretlen.",cnt,nskip);
		return "";
	}
	
	private void RealizeItemList() {
		if (!RealizeDtxId()) return;
		if (!RealizeDtxStr()) return;
		
		DiaItem d=DiaItem.sFirst;
		while(d!=null) {
			DiaItem dx=d.mNext;
			if (d.mTipus==d.ditDTXid || d.mTipus==d.ditDTXstr)
				d.KillMe();
			d=dx;
		}
	}
	
	private boolean RealizeDtxStr() {
		ArrayList<String> lst = new ArrayList<String>();
		DiaItem d = DiaItem.sFirst;
		while(d!=null) {
			if (d.mTipus==d.ditDTXstr) {
				if (lst.indexOf(d.mKnev)<0) lst.add(d.mKnev);
			}
			d=d.mNext;
		}
		
		TxTar Dtx = TxTar.Get();
		String[] karr = Dtx.getNames();
		List<String> klst = Arrays.asList(karr);
		int n=lst.size(), i=0;
		for (String kstr : lst) {
			SetPercent("Azonosítás...",25+(25*(i++))/n);
			if (isCancelled()) return false;
			int kx=klst.indexOf(kstr);
			if (kx<0) continue;
			String[] varr = Dtx.getEnekLst(null,kx);
			List<String> vlst = Arrays.asList(varr);
			d=DiaItem.sFirst;
			while(d!=null) {
				if (d.mTipus==d.ditDTXstr && d.mKnev.equals(kstr)) {
					int sx=0;
					int vx=vlst.indexOf(d.mVnev);
					if (vx>=0 && d.mSnev.length()>0) {
						String[] sarr = Dtx.getVersszakLst(null,kx,vx);
						sx=Arrays.asList(sarr).indexOf(d.mSnev);
					}
					if (vx>=0 && sx>=0) {
						d.mKotet=kx;
						d.mVers=vx;
						d.mVszak=sx;
						d.mTipus=d.ditDTX;
					}
				}
				d=d.mNext;
			}
		}
		return true;
	}
	
	private boolean RealizeDtxId() {
		String[] karr = TxTar.Get().getNames();
		int n = karr.length;
		for (int kx=0; kx<n; kx++) {
			SetPercent("Azonosítás...",50+(50*kx)/n);
			if (isCancelled()) return false;
			ArrayList<String> idlst = new ArrayList<String>();
			ArrayList<Integer> vxlst = new ArrayList<Integer>();
			ArrayList<Integer> sxlst = new ArrayList<Integer>();
			TxTar.Get().getIdList(null,kx,idlst,vxlst,sxlst);
			String[] varr = TxTar.Get().getEnekLst(null,kx);
			boolean anyid=false;
			DiaItem d = DiaItem.sFirst;
			while(d!=null) {
				if (d.mTipus==d.ditDTX && d.mKotet==kx && d.mSid.isEmpty()) {
					int p = Collections.binarySearch(vxlst,d.mVers);
					if (p>=0) {
						while (p<vxlst.size()-1 && vxlst.get(p+1)==d.mVers) p++;
						while (p>=0 && vxlst.get(p)==d.mVers && sxlst.get(p)!=d.mVszak) p--;
						if (p>=0 && vxlst.get(p)==d.mVers)
							d.mSid=idlst.get(p);
					}
				} else
				if (d.mTipus==d.ditDTXid || d.mTipus==d.ditDTXstr) {
					int ix=idlst.indexOf(d.mSid);
					if (ix<0) anyid=true; else {
						d.mKotet=kx;
						d.mVers=vxlst.get(ix);
						d.mVszak=sxlst.get(ix);
						d.mKnev=karr[kx];
						d.mVnev=varr[d.mVers];
						String[] sarr = TxTar.Get().getVersszakLst(null,kx,d.mVers);
						d.mSnev=sarr[d.mVszak];
						d.mTipus=d.ditDTX;
					}
				}
				d=d.mNext;
			}
			if (!anyid) return true;
		}
		return true;
	}
	
	private int ReadInt(String key, int defval) {
		return Integer.parseInt(ReadStr(key,Integer.toString(defval)));
	}

	private boolean ReadBool(String key, boolean defval) {
		return ReadInt(key, defval ? 1 : 0) != 0;
	}
	
	private String ReadStr(String key, String defval) {
		String k1 = key.toLowerCase()+"=";
		int klen = k1.length();
		for (String s : grplst) {
			if (s.length()<klen) continue;
			if (s.substring(0,klen).toLowerCase().equals(k1))
				return s.substring(klen);
		}
		return defval;
	}

	//TRUE=megtalalta a csoportot
	private String ReadGrp(String grp) {
		ArrayList<String> gl = new ArrayList<String>();
		boolean ingrp=false;
		try {
			InputStream fis;
			if (G.OPEN_DIA_BY_FILESELECTOR) {
				fis = new FileInputStream(ctx.dir + ctx.fname);
			} else {
				fis = ctx.getContentResolver().openInputStream(G.sLoadUri);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while((line=br.readLine())!=null) {
				if (line.startsWith("[")) {
					if (ingrp) break;
					if (line.toLowerCase().equals("["+grp.toLowerCase()+"]"))
						ingrp=true;
					continue;
				}
				if (ingrp) gl.add(line);
			}
		} catch(IOException e) {
			return "Fájl betöltési hiba!\n"+e.getLocalizedMessage();
		}
		int gls = gl.size();
		if (!ingrp || gls<=0) {
			return "Betöltés: '"+grp+"' csoport nem található.";
		}
		grplst = new String[gls];
		gl.toArray(grplst);
		return "";
	}
	
}

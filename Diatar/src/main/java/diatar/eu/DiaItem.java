package diatar.eu;
import android.os.*;
import java.util.*;
import android.content.*;
import eu.diatar.library.*;

public class DiaItem {
	public static DiaItem sFirst = null, sLast = null;
	public static int sCount = 0;
	public static DiaProp sCommonProps;
	private static DiaItem sCacheItem = null;
	private static int sCachePos = -1;

	public static final int ditDTX = 1;
	public static final int ditSEPAR = 2;
	public static final int ditTXT = 3;
	public static final int ditPIC = 4;
	
	//elokeszito fazis
	public static final int ditDTXstr = 101;
	public static final int ditDTXid = 102;
	
	
	public DiaItem mNext,mPrev;
	public int mTipus;
	//elvalaszto: mKnev
	//kep: mKnev+mVnev
	public String mKnev,mVnev,mSnev,mSid;
	public int mKotet,mVers,mVszak;
	public DiaProp mProps;
	public String[] mTxt;
	public boolean mEdSelected;
	
	public DiaItem() {
		mProps=new DiaProp();
		if (sCommonProps==null) sCommonProps=new DiaProp();
	}
	
	public DiaItem(int atipus) {
		mProps=new DiaProp();
		if (sCommonProps==null) sCommonProps=new DiaProp();
		mTipus=atipus;
	}
	
	public static void Clear() {
		DiaItem d = sFirst;
		while (d!=null) {
			DiaItem di = d.mNext;
			d.mNext=null; d.mPrev=null;
			d.mProps=null; d.mTxt=null;
			d=di;
		}
		sFirst=null; sLast=null;
		sCommonProps=null;
		sCount=0;
		sCacheItem=null; sCachePos=-1;
	}
	
	public static DiaProp getCommonProps() {
		if (sCommonProps==null) sCommonProps= new DiaProp();
		return sCommonProps;
	}
	
	public void AppendMe() {
		if (sLast==null)
			sFirst=this;
		else
			sLast.mNext=this;
		mPrev=sLast; sLast=this;
		sCount++;
		sCacheItem=null; sCachePos=-1;
	}
	
	public void InsertMe(DiaItem beforethis) {
		if (beforethis==null) {
			AppendMe();
			return;
		}
		this.mNext=beforethis;
		this.mPrev=beforethis.mPrev;
		if (beforethis.mPrev==null)
			sFirst=this;
		else
			beforethis.mPrev.mNext=this;
		beforethis.mPrev=this;
		sCount++;
		sCacheItem=null; sCachePos=-1;
	}
	
	public void RemoveMe() {
		if (this.mPrev==null)
			sFirst=this.mNext;
		else
			this.mPrev.mNext=this.mNext;
		if (this.mNext==null)
			sLast=this.mPrev;
		else
			this.mNext.mPrev=this.mPrev;
		this.mNext=null; this.mPrev=null;
		sCount--;
		sCacheItem=null; sCachePos=-1;
	}
	
	public void Destroy() {
		mNext=null; mPrev=null;
		mProps=null;
		mTxt=null;
		//if (sFirst==null) sCommonProps=null;
	}
	
	public void KillMe() {
		RemoveMe();
		Destroy();
	}
	
	public void MoveMe(int newpos) {
		RemoveMe();
		InsertMe(getByPos(newpos));
	}
	
	//100=0, 110=1, ...
	public static int SpacingToIdx(int sp100) {
		if (sp100<100) return 0;
		if (sp100>200) return 10;
		return (sp100-95)/10;
	}
	
	public static int getCount() {
	//	DiaItem d = sFirst;
	//	int res=0;
	//	while (d!=null) {
	//		res++;
	//		d=d.mNext;
	//	}
	//	return res;
	return sCount;
	}
	
	/////////////////
	// mentes es vissza
	
	static void toBundle(Bundle bd) {
		bd.putInt(G.idCOUNT,sCount);
		if (sCommonProps!=null)
			sCommonProps.toBundle(bd,0);
		DiaItem d = sFirst;
		int i=0;
		while (d!=null) {
			i++;
			d.toBundle(bd,i);
			d=d.mNext;
		}
	}
	
	private void toBundle(Bundle bd, int idx) {
		mProps.toBundle(bd,idx);
	}
	
	//ugyanennek az eneknek versszaka?
	public boolean isSameEnek(DiaItem d) {
		return mTipus==ditDTX && d!=null && d.mTipus==ditDTX &&
			d.mKotet==this.mKotet && d.mVers==this.mVers;
	}

	//a miutanunk kovetkezo v null
	public DiaItem getNextEnek() {
		DiaItem d = this;
		do {
			d=d.mNext;
		} while (isSameEnek(d));
		return d;
	}
	
	//eneklistahoz tipus szerint
	public String getDiaTitle(boolean allvszak) {
		if (mTipus==ditSEPAR) return "-- "+mKnev+" --";
		if (mTipus==ditTXT) return mKnev;
		if (mTipus==ditPIC) return mVnev;
		if (mTipus==ditDTX) {
			String s = TxTar.Get().getDtxLst()[mKotet].nick()+": "+mVnev;
			if (mSnev.length()>0) {
				s+="/"+mSnev;
				if (allvszak) {
					DiaItem d = mNext;
					while(isSameEnek(d)) {
						s+=", "+d.mSnev;
						d=d.mNext;
					}
				}
			}
			return s;
		}
		//ismeretlen tipus
		return "";
	}
	
	//enekek listaja (vszakok vesszovel a sorban)
	public static String[] getEnekLst() {
		ArrayList<String> lst = new ArrayList<String>();
		DiaItem d = sFirst;
		while(d!=null) {
			lst.add(d.getDiaTitle(true));
			d=d.getNextEnek();
		}
		
		if (lst.size()<=0) lst.add("");
		String[] res = new String[lst.size()];
		lst.toArray(res);
		return res;
	}
	
	//egy enek versszakai
	public static String[] getVersszakLst(int enek) {
		ArrayList<String> lst = new ArrayList<String>();
		DiaItem d=getEnekByIndex(enek);
		if (d!=null && d.mTipus==ditDTX) {
			DiaItem d0=d;
			do {
				lst.add(d.mSnev);
				d=d.mNext;
			} while (d0.isSameEnek(d));
		}
	
		if (lst.size()<=0) lst.add("---");
		String[] res = new String[lst.size()];
		lst.toArray(res);
		return res;
	}
	
	public static DiaItem getEnekByIndex(int ex) {
		DiaItem d = sFirst;
		while(d!=null && ex-->0)
			d=d.getNextEnek();
		return d;
	}
	
	//enek+dia (nullabazisu) indexebol dia sorszama
	public static int Idx2Pos(int ex, int dx) {
		DiaItem d=sFirst, dnext;
		int res=0;
		if (ex<0) return 0;
		while(d!=null) {
			dnext=d.getNextEnek();
			ex--;
			if (ex<0) {
				while(d!=dnext && dx-->0) {
					d=d.mNext;
					res++;
				}
				return res;
			}
			while(d!=dnext) {
				d=d.mNext;
				res++;
			}
		}
		return res;
	}
	
	public static int[] Pos2Idx(int pos) {
		int[] res = new int[2];
		res[0]=-1;
		DiaItem d = sFirst, dnext;
		while (d!=null) {
			dnext=d.getNextEnek();
			res[0]++; res[1]=0;
			while(d!=dnext) {
				pos--; if (pos<0) return res;
				d=d.mNext;
				res[1]++;
			}
		}
		res[1]=0;
		return res;
	}
	
	public static DiaItem getByPos(int pos) {
		int cnt = 0;
		DiaItem res = sFirst;
		if (sCacheItem!=null && sCachePos>=0) {
			if (cnt<sCachePos) {
				if (pos+pos>sCachePos) {
					cnt=sCachePos; res=sCacheItem;
				}
			} else {
				if (pos+pos>sCount+sCachePos) {
					cnt=sCount-1; res=sLast;
				} else {
					cnt=sCachePos; res=sCacheItem;
				}
			}
		}
		if (cnt<=pos) {
			while (res!=null && cnt++<pos) res=res.mNext;
		} else {
			while (res!=null && cnt-->pos) res=res.mPrev;
		}
		sCacheItem=res; sCachePos=pos;
		return res;
	}
	
	public static DiaItem getByIndex(int ex, int dx) {
		DiaItem d=sFirst, dnext;
		while(d!=null) {
			dnext=d.getNextEnek();
			ex--;
			if (ex<0) {
				while (d!=dnext && dx-->0) d=d.mNext;
				return d;
			}
			d=dnext;
		}
		return null;
	}
	
	public String[] getTxt(Context ctx) {
		if (mTipus==ditTXT) return mTxt;
		if (mTipus==ditDTX)
			return TxTar.Get().getDiaTxt(ctx,mKotet,mVers,mVszak);
		return new String[]{""};
	}
	
	public static String[] getDiaTxt(Context ctx, int pos) {
		DiaItem d = DiaItem.getByPos(pos);
		if (d==null) return new String[]{""};
		String[] res1 = d.getTxt(ctx);
		if (!d.mProps.mDblDia) return res1;
		DiaItem d2 = DiaItem.getByPos(pos+1);
		if (d2==null) return res1;
		String[] res2 = d2.getTxt(ctx);
		int l1 = res1.length, l2 = res2.length;
		String[] res3 = new String[l1+1+l2];
		System.arraycopy(res1,0,res3,0, l1);
		res3[l1]="";
		System.arraycopy(res2,0,res3,l1+1,l2);
		return res3;
	}
	
	public static int getDiaTip(int pos) {
		DiaItem d = DiaItem.getByPos(pos);
		if (d==null) return ditTXT;
		return d.mTipus;
	}
	
	public boolean setupRecState(RecState r) {
		return setupRecState(this,r);
	}
	
	//d lehet null
	//true==modosult valami
	public static boolean setupRecState(DiaItem d, RecState r) {
		int v;
		boolean res;
		
		v=G.sBkColor;
		if (d!=null) {
			v=d.mProps.mBkColor;
			if ((v&0xFF000000)==0) v=sCommonProps.mBkColor;
			if ((v&0xFF000000)==0) v=G.sBkColor;
		}
		v&=0x00FFFFFF;
		res = (v!=r.getBkColor());
		r.setBkColor(v);
		
		v=G.sTxColor;
		if (d!=null) {
			v=d.mProps.mTxColor;
			if ((v&0xFF000000)==0) v=sCommonProps.mTxColor;
			if ((v&0xFF000000)==0) v=G.sTxColor;
		}
		v&=0x00FFFFFF;
		res=res || v!=r.getTxtColor();
		r.setTxtColor(v);
		
		v=G.sBlankColor;
		if (d!=null) {
			v=d.mProps.mBlankColor;
			if ((v&0xFF000000)==0) v=sCommonProps.mBlankColor;
			if ((v&0xFF000000)==0) v=G.sBlankColor;
		}
		v&=0x00FFFFFF;
		res=res || v!=r.getBlankColor();
		r.setBlankColor(v);
		
		v=G.sFontSize;
		if (d!=null) {
			v=d.mProps.mFontSize;
			if (v<0) v=sCommonProps.mFontSize;
			if (v<0) v=G.sFontSize;
		}
		res=res || r.getFontSize()!=v;
		r.setFontSize(v);
		
		v=G.sTitleSize;
		if (d!=null) {
			v=d.mProps.mTitleSize;
			if (v<0) v=sCommonProps.mTitleSize;
			if (v<0) v=G.sTitleSize;
		}
		res=res || r.getTitleSize()!=v;
		r.setTitleSize(v);
		
		v=G.sIndent;
		if (d!=null) {
			v=d.mProps.mIndent;
			if (v<0) v=sCommonProps.mIndent;
			if (v<0) v=G.sIndent;
		}
		res=res || r.getLeftIndent()!=v;
		r.setLeftIndent(v);
		
		v=G.sSpacing;
		if (d!=null) {
			v=d.mProps.mSpacing;
			if (v<0) v=sCommonProps.mSpacing;
			if (v<0) v=G.sSpacing;
		}
		v=100+10*v;
		res=res || r.getSpacing100()!=v;
		r.setSpacing100(v);
		
		boolean b;
		byte b3;
		b=G.sHCenter;
		if (d!=null) {
			b3=d.mProps.mHCenter;
			if (b3== DiaProp.b3UNUSED) b3=sCommonProps.mHCenter;
			if (b3!= DiaProp.b3UNUSED) b= DiaProp.B3ToBool(b3);
		}
		res=res || r.getHCenter()!=b;
		r.setHCenter(b);
		
		b=G.sVCenter;
		if (d!=null) {
			b3=d.mProps.mVCenter;
			if (b3== DiaProp.b3UNUSED) b3=sCommonProps.mVCenter;
			if (b3!= DiaProp.b3UNUSED) b= DiaProp.B3ToBool(b3);
		}
		res=res || r.getVCenter()!=b;
		r.setVCenter(b);
		
		v=G.sHighPos;
		res=res || r.getWordToHighlight()!=v;
		r.setWordToHighlight(v);

		return res;
	}
}


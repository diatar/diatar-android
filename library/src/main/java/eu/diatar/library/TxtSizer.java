package eu.diatar.library;
import android.graphics.*;
import android.util.*;
import android.content.*;
import android.content.res.*;

public class TxtSizer
{
	private final Lines mLines;
	public boolean needrecalc;
	private float mFontSize, mTitleSize;
	private float yadd;  //for VCenter
	private float wsp, why;		//space width, hyphen width
	private float scrw, scrh, yspacing;		//screen size, extra space between rows
	
	static private float sDensity;
	
	public String mTitle;
	public String mScholaLine;
	
	public int gLeftIndent;
	public int gFontSize,gTitleSize;
	public int gSpacing100;
	public int gTxtColor, gHighColor;
	public boolean gUseAkkord,gUseKotta,gHideTitle;
	public boolean gHCenter,gVCenter;
	public boolean gAutoResize,gBoldText;
	public int gKottaArany, gAkkordArany;
	
	public float mBigStep;
	public float mSmallStep;
	
	private int mWordCnt, mWordIdx;
	public int mHighPoint;
	public Resources mRes;
	
	public Kotta mKotta;
	
	public TxtSizer() {
		mLines = new Lines();
		mKotta = new Kotta();
		needrecalc=true;
		mBigStep=0.05f;
		mSmallStep=0.005f;
		gKottaArany=100; gAkkordArany=100;
	}
	
	public int getWordCnt() { return mWordCnt; }
	
	static public void setDensity(Context ctx) {
			sDensity=ctx.getResources().getDisplayMetrics().density;
	}
	
	public void addLine(String txtline) {
		mWordCnt+=mLines.addLine(txtline);
		needrecalc=true;
	}
	
	private void CalcWidths() {
		mKotta = new Kotta();
		{
			Paint p = new Paint();
			p.setTextSize(mFontSize);
			Paint.FontMetrics fm = p.getFontMetrics();
			if (gKottaArany<10 || gKottaArany>200) gKottaArany=100;
			if (gAkkordArany<10 || gAkkordArany>200) gAkkordArany=100;
			mKotta.setHeight((2*(-fm.ascent+fm.descent)*gKottaArany)/100);
		}
		for (int r=0; r<mLines.mData.size(); r++) {
			mKotta.reset();
			Line row = mLines.mData.get(r);
			for (int i=0; i<row.mData.size(); i++) {
				WordGroup wg = row.mData.get(i);
				wg.brk=false; wg.width=0.0f;
				wg.keloW=0f;
				for (int j=0; j<wg.mData.size(); j++) {
					Word w = wg.mData.get(j);
					Paint p = w.getPaint(mFontSize,gTxtColor,gBoldText);
					w.width = w.txtw = p.measureText(w.txt);
					if (gUseKotta && w.kotta!=null && !w.kotta.isEmpty()) {
						float w3=mKotta.getWidth(w.kotta);
						if (mKotta.mPostX>0f) {
							w.keloW=mKotta.mPostX;
							wg.keloW=w.keloW;
							w.width+=mKotta.mPostX;
						}
						if (w3>w.width) w.width=w3;
					}
					if (gUseAkkord && w.akkord!=null) {
						float w2=w.akkord.CalcWidth((mFontSize*gAkkordArany)/100);
						if (w2>w.width) w.width=w2;
					}
					wg.width+=w.width;
				}
			} //i
		} //r
	}
	
	private float nextkelow;
	private boolean bTooLong;
	
	//return: next y
	private float RecalcLine(int r, float y) {
		if (r==0) nextkelow=0f;
		//Log.d("Draw","DrawLine start");
		Paint p = new Paint();
		p.setTextSize(mFontSize);
		Paint.FontMetrics fm = p.getFontMetrics();
		Line ln = mLines.mData.get(r);
		boolean useakk = gUseAkkord && mLines.mHasAkkord;
		boolean usekotta = gUseKotta && mLines.mHasKotta;
		int n = ln.mData.size(), i0 = 0;
		float x0 = nextkelow;
		while (i0<n) {
			if (useakk) y+=((fm.descent-fm.ascent)*gAkkordArany)/100;
			if (usekotta) y+=(2*(fm.descent-fm.ascent)*gKottaArany)/100;
			y+=-fm.ascent*yspacing;
			int i = i0, i1 = i0+1;
			float x = x0;
			WordGroup wg;
			while (i<n) {
				wg=ln.mData.get(i++);
				x+=wg.width;
				if (wg.keloW>0f) nextkelow=wg.keloW;
				if (x+(wg.endtype==Word.etHYPH ? why : 0f)>scrw) {
					if (i==i0+1) bTooLong=true;
					break;
				}
				if (wg.endtype!=Word.etCONT) i1=i;
				if (wg.endtype==Word.etBRK) x+=wsp; else
				if (wg.endtype==Word.etSPACE) x+=wsp;
			}
			wg=ln.mData.get(i1-1);
			wg.brk=true;
			i0=i1;
			y+=fm.descent*yspacing+fm.leading;
			x0=gLeftIndent*wsp+nextkelow;
		}
		if (n>0) mWordIdx++;
		return y+fm.descent*yspacing+fm.leading;
	}
	
	private void DrawArc(Canvas canvas, Paint p, float x1, float x2, float y) {
		Paint.FontMetrics fm = p.getFontMetrics();
		float x12 = (x1+x2)/2f, y2 = y+fm.descent, y12 = y+fm.descent*0.7f;
		float lw = (x12-x1)/2f;
		if (lw>fm.descent) lw=fm.descent;
		Path pt = new Path();
		pt.moveTo(x1,y);
		pt.cubicTo(x1+lw,y2, x1,y2, x12,y2);
		pt.cubicTo(x2,y2, x2-lw,y2, x2,y);
		pt.cubicTo(x2-lw,y12, x2,y12, x12,y12);
		pt.cubicTo(x1,y12, x1+lw,y12, x1,y);
		canvas.drawPath(pt,p);
	}
	
	private float DrawLine(int r, Canvas canvas, float y) {
		if (r==0) nextkelow=0f;
		Paint p = new Paint();
		p.setTextSize(mFontSize);
		Paint.FontMetrics fm = p.getFontMetrics();
		Line ln = mLines.mData.get(r);
		boolean useakk = gUseAkkord && mLines.mHasAkkord;
		boolean usekotta = gUseKotta && mLines.mHasKotta;
		int n = ln.mData.size();
		float x0 = 0f, endx = 9e9f;
		y+=-fm.ascent*yspacing;
		if (useakk && n>0) y+=((-fm.ascent+fm.descent)*gAkkordArany)/100;
		float kottay=y;
		float arcx0 = 0f;
		boolean inarc=false;
		boolean kstart=false;
		if (usekotta && n>0) y+=(2*(-fm.ascent+fm.descent)*gKottaArany)/100;
		boolean rowstart = true;
		boolean anywr=false;
		for (int i=0; i<n; i++) {
			WordGroup wg;
			if (gHCenter && rowstart) {
				float w = nextkelow;
				int j = i;
				while (j<n) {
					wg=ln.mData.get(j++);
					w+=wg.width;
					if (wg.brk) {
						if (wg.endtype==Word.etHYPH) w+=why;
						break;
					}
					if (wg.endtype==Word.etSPACE || wg.endtype==Word.etBRK) w+=wsp;
				}
				if (w<scrw) x0+=(scrw-w)/2f;
			}
			wg=ln.mData.get(i);
			for (int j=0; j<wg.mData.size(); j++) {
				Word w = wg.mData.get(j);
				p = w.getPaint(mFontSize,mWordIdx<mHighPoint ? gHighColor : gTxtColor,gBoldText);
				if (!w.txt.isEmpty()) anywr=true;
				float xt=x0;
				if (usekotta && !kstart) {
					mKotta.startDraw(mRes,canvas,x0,kottay+fm.ascent,gTxtColor);
					x0+=mKotta.startLine();
					xt=x0;
					kstart=true;
					//Log.v("Draw","Kotta start x0="+Float.toString(xt)+" , w="+Float.toString(x0-xt));
				}
				if (usekotta && w.kotta!=null && !w.kotta.isEmpty()) {
					//float xx=x0;
					mKotta.mX=x0;
					mKotta.Draw(w.kotta);
					xt=mKotta.mPostX;
					//Log.v("Draw","Kotta draw x0="+Float.toString(xx)+" , w="+Float.toString(mKotta.mX-xx)+" , xt="+Float.toString(xt));
				}
				if (endx<xt) canvas.drawLine(endx,y,xt,y,p);
				canvas.drawText(w.txt,xt,y,p);
				endx=xt+w.txtw;
				//Log.v("Draw","Txt='"+w.txt+"' x0="+Float.toString(x0)+" , xt="+Float.toString(xt)+" , w="+Float.toString(w.width));
				if (useakk && w.akkord!=null) {
					Paint pa = new Paint(p);
					pa.setTextSize((p.getTextSize()*gAkkordArany)/100f);
					w.akkord.Draw(canvas,pa,xt,y-(pa.descent()-p.ascent()));
					if (w.endtype==Word.etCONT) {
						float tw = p.measureText(w.txt);
						if (tw<w.width) canvas.drawLine(x0+tw,y,x0+w.width,y,p);
					}
				}
				if (inarc) {
					if (!w.style.getArc() || w.style.getABegin()) {
						DrawArc(canvas,p,arcx0,x0,y);
						inarc=w.style.getArc();
						arcx0=xt;
					}
				} else {
					if (w.style.getArc()||w.style.getABegin()) {
						inarc=true;
						arcx0=xt;
					}
				}
				x0+=w.width;
				if (w.keloW>0f) nextkelow=w.keloW;
			}
			if (wg.endtype!=Word.etCONT) endx=9e9f;
			if (wg.endtype==Word.etBRK || wg.endtype==Word.etEOL || wg.endtype==Word.etSPACE) {
				if (anywr) mWordIdx++;
			}
			if (wg.brk) {
				if (wg.endtype==Word.etHYPH)
					canvas.drawText("-",x0,y,p);
				if (inarc) DrawArc(canvas,p,arcx0,endx,y);
				y+=fm.descent*yspacing+fm.leading;
				if (i<n-1) y+=-fm.ascent*yspacing;
				if (useakk && i<n-1) y+=((-fm.ascent+fm.descent)*gAkkordArany)/100;
				if (kstart) mKotta.endDraw(x0);
				kstart=false;
				kottay=y;
				if (usekotta && i<n-1) y+=(2*(fm.descent-fm.ascent)*gKottaArany)/100;
				x0=gLeftIndent*wsp;
				arcx0=0f;
				endx=9e9f;
				rowstart=true;
			} else {
				rowstart=false;
				if (wg.endtype==Word.etSPACE || wg.endtype==Word.etBRK)
					x0+=wsp;
			}
		}
		if (kstart) mKotta.endDraw(x0);
		if (inarc) DrawArc(canvas,p,arcx0,endx,y);
		return y+fm.descent*yspacing+fm.leading;
	}
	
	//return: total height
	private float RecalcScreen(boolean logging) {
		float y = 0f;
		
		Paint p = new Paint();
		p.setTextSize(mTitleSize);
		Paint.FontMetrics fm = p.getFontMetrics();
		
		mKotta.setHeight((2*-fm.ascent*gKottaArany)/100);
		
		if (!gHideTitle) y=(-fm.ascent+fm.descent+fm.leading)*1.5f;

		for (int i=0; i<mLines.mData.size(); i++) {
			y=RecalcLine(i,y);
			if (logging)
				Log.d("Recalc",i+". "+y);
		}
		return y;
	}
	
	/////////////
	// sortoresi javaslat
	/////////////
	
	private Line jLN;
	private int jNBrk, jN;
	private boolean[] jBrks;
	
	//true = elfogadhato a tordeles
	private boolean TryBreaks(int level, int pos) {
		int pbrk=0, pmax=pos;
		float wsum= (level>0 ? gLeftIndent*wsp : 0f);
		while(pos<jN) {
			jBrks[pos]=false;
			WordGroup wg=jLN.mData.get(pos);
			wsum+=wg.width;
			if (wsum+(wg.endtype==Word.etHYPH ? why : 0f)>scrw)
				break;
			if (wg.endtype!=Word.etCONT) {
				if (wg.endtype==Word.etBRK) pbrk=pos;
				pmax=pos;
				if (wg.endtype==Word.etBRK) wsum+=wsp; else
				if (wg.endtype==Word.etSPACE) wsum+=wsp;
			}
			pos++;
		}
		if (pos>=jN) {  //sor vegen
			jBrks[jN-1]=true;
			return true;
		}
		if (level>=jNBrk) return false;
		if (pbrk>0) {
			if (TryBreaks(level+1,pbrk+1)) {
				jBrks[pbrk]=true;
				return true;
			}
			if (pbrk==pmax) return false;
		}
		jBrks[pmax]=true;
		return TryBreaks(level+1,pmax+1);
	}
	
	private void RecalcBreaks() {
		for (int l=0; l<mLines.mData.size(); l++) {
			jLN=mLines.mData.get(l);
			if (!jLN.mHasBrk) continue;
			jN=jLN.mData.size();
			jNBrk=0;
			for (int i=0; i<jN; i++)
				if (jLN.mData.get(i).brk) jNBrk++;
			if (jNBrk>1) {
				jBrks = new boolean[jN];
				if (TryBreaks(1,0))
					for (int i=0; i<jN; i++)
						jLN.mData.get(i).brk=jBrks[i];
			}
		}
		jLN=null; //ne hagyjunk koszt!
		jBrks=null;
	}
	
	private void Recalc() {
		Log.d("Draw","TxtSizer.Recalc");
		if (gKottaArany<10 || gKottaArany>200) gKottaArany=100;
		if (gAkkordArany<10 || gAkkordArany>200) gAkkordArany=100;
		
		float ptmul = 160f*sDensity/72f;
		mFontSize = gFontSize *ptmul;
		mTitleSize = gTitleSize *ptmul;
		Paint p = new Paint();
		p.setTextSize(mFontSize);
		wsp = p.measureText(" ");
		why = p.measureText("-");
		yspacing = gSpacing100*0.01f;
		float ytotal;
		if (gAutoResize && !mLines.mData.isEmpty()) {
			float fsorig = mFontSize, tsorig = mTitleSize;
			float sizemul = 1f, sizestep = mBigStep;
			Paint.FontMetrics fm = p.getFontMetrics();
			float lh = mLines.mData.size()*yspacing*(fm.descent-fm.ascent+fm.leading);
			if (scrh<lh) sizemul=scrh/lh;
			do {
				mFontSize=fsorig*sizemul;
				mTitleSize=tsorig*sizemul;
				p.setTextSize(mFontSize);
				wsp = p.measureText(" ");
				why = p.measureText("-");
				//Log.d("Draw","Calc " + sizemul);
				CalcWidths();
				//Log.d("Draw","Testdraw");
				bTooLong=false;
				ytotal=RecalcScreen(false);
				yadd=scrh-ytotal;
				if (yadd>=0f && !bTooLong) {
					if (sizestep==mSmallStep) break;
					sizemul+=sizestep;
					sizestep=mSmallStep;
				}
				sizemul-=sizestep;
				//Log.d("Draw","continue");
			} while (sizemul>0.01f);
		} else {
			CalcWidths();
			ytotal=RecalcScreen(false);
			yadd=scrh-ytotal;
			if (yadd<0f) yadd=0f;
		}
		RecalcScreen(true);
		RecalcBreaks();
		needrecalc=false;
		Log.d("Draw","TxtSizer.Recalc finished H="+((Float)ytotal));
	}
	
	//return: total height
	private float DrawScreen(Canvas canvas) {
		float y = 0f;
		if (gKottaArany<10 || gKottaArany>200) gKottaArany=100;
		if (gAkkordArany<10 || gAkkordArany>200) gAkkordArany=100;
		
		Paint p = new Paint();
		p.setTextSize(mTitleSize);
		Paint.FontMetrics fm = p.getFontMetrics();
		
		if (!gHideTitle) {
			y=-fm.ascent;
			p.setColor(gTxtColor);
			canvas.drawText(mTitle,0,y,p);
			y+=(fm.descent+fm.leading);
			y*=1.5f;
		}
		if (gVCenter) y+=yadd/2;
		mWordIdx=0;
		
		p.setTextSize(mFontSize);
		fm = p.getFontMetrics();
		mKotta = new Kotta();
		mKotta.setHeight((2*(fm.descent-fm.ascent)*gKottaArany)/100);

		for (int i=0; i<mLines.mData.size(); i++) {
			mKotta.reset();
			y=DrawLine(i,canvas,y);
			Log.d("Draw",i+". "+y);
		}
		return y;
	}
	
	public void Draw(Canvas canvas,float scrwidth, float scrheight) {
		Log.d("Draw","TxtSizer.Draw");
		scrw=scrwidth; scrh=scrheight;
		if (needrecalc) Recalc();
		float ytotal=DrawScreen(canvas);
		Log.d("Draw","TxtSizer.Draw finished H="+((Float)ytotal));
	}

}

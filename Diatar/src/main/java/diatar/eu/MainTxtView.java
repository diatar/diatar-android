package diatar.eu;

import eu.diatar.library.*;
import android.graphics.*;

public class MainTxtView extends DiaViewBase
{
	private TxtSizer mTxtSizer;
	
	public int getWordCnt() { return mTxtSizer.getWordCnt(); }
	public int getHighPoint() { return mTxtSizer.mHighPoint; }
	public int setHighPoint(int pos) {
		if (pos<0) pos=0;
		if (pos>mTxtSizer.getWordCnt()) pos=mTxtSizer.getWordCnt();
		mTxtSizer.mHighPoint=pos;
		invalidate();
		return pos;
	}
	
	public MainTxtView(MainActivity ma) {
		super(ma);
		
		mTxtSizer = new TxtSizer();
		mTxtSizer.mRes=ma.getResources();
		mTxtSizer.mTitle="";
		mTxtSizer.needrecalc=true;
		//mTxtSizer.mBigStep=0.1f;
		//mTxtSizer.mSmallStep=0.002f;
		
		mTxtSizer.gAutoResize=G.sAutosize;
		mTxtSizer.gFontSize=G.sFontSize;
		mTxtSizer.gHCenter=G.sHCenter;
		mTxtSizer.gHideTitle=true; //!G.sUseTitle;
		mTxtSizer.gLeftIndent=G.sIndent;
		mTxtSizer.gSpacing100=100+10*G.sSpacing;
		mTxtSizer.gTitleSize=G.sTitleSize;
		mTxtSizer.gTxtColor=G.sTxColor;
		mTxtSizer.gHighColor=G.sHighColor;
		mTxtSizer.gUseAkkord=G.sUseAkkord;
		mTxtSizer.gUseKotta=G.sUseKotta;
		mTxtSizer.gVCenter=G.sVCenter;
		mTxtSizer.gKottaArany=G.sKottaArany;
		mTxtSizer.gAkkordArany=G.sAkkordArany;
	}

	public void setProps(RecState r) {
		mTxtSizer.gFontSize=r.getFontSize();
		mTxtSizer.gHCenter=r.getHCenter();
		mTxtSizer.gLeftIndent=r.getLeftIndent();
		mTxtSizer.gSpacing100=r.getSpacing100();
		mTxtSizer.gTitleSize=r.getTitleSize();
		mTxtSizer.gTxtColor=r.getTxtColor()|0xFF000000;
		mTxtSizer.gHighColor=r.getHiColor()|0xFF000000;
		mTxtSizer.gVCenter=r.getVCenter();
	}
	
	public void setTxt(String[] arr) {
		for (int i=0; i<arr.length; i++)
			mTxtSizer.addLine(arr[i]);
	}
	
	@Override
	protected void drawDia(Canvas canvas) {
		mTxtSizer.Draw(canvas,canvas.getWidth(),canvas.getHeight());
		Paint p = new Paint();
		p.setColor(mTxtSizer.gTxtColor); // G.sTxColor);
		float w = canvas.getWidth(), h = canvas.getHeight();
		canvas.drawLine(w-0f,0f,w-0f,h,p);
	}
}

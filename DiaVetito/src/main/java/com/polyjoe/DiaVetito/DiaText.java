package com.polyjoe.DiaVetito;

import android.graphics.*;
import eu.diatar.library.*;
import android.content.res.*;

public class DiaText extends DiaBase
{
	public TxtSizer mTxtSizes;
	
	public DiaText(Resources res) {
		super();
		mTxtSizes = new TxtSizer();
		mTxtSizes.mRes=res;
		OnRecalc();
	}
	
	@Override
	public boolean RecalcIfNeeded() {
		Globals g = mTcp.G;

		boolean needed=false;
		if (mTxtSizes.gAutoResize!=g.AutoResize) {
			mTxtSizes.gAutoResize=g.AutoResize;
			needed=true;
		}
		if (mTxtSizes.gFontSize!=g.FontSize) {
			mTxtSizes.gFontSize=g.FontSize;
			needed=true;
		}
		if (mTxtSizes.gHCenter!=g.HCenter) {
			mTxtSizes.gHCenter=g.HCenter;
			needed=true;
		}
		if (mTxtSizes.gHideTitle!=g.HideTitle) {
			mTxtSizes.gHideTitle=g.HideTitle;
			needed=true;
		}
		if (mTxtSizes.gLeftIndent!=g.LeftIndent) {
			mTxtSizes.gLeftIndent=g.LeftIndent;
			needed=true;
		}
		if (mTxtSizes.gSpacing100!=g.Spacing100) {
			mTxtSizes.gSpacing100=g.Spacing100;
			needed=true;
		}
		if (mTxtSizes.gTitleSize!=g.TitleSize) {
			mTxtSizes.gTitleSize=g.TitleSize;
			needed=true;
		}
		if (mTxtSizes.gTxtColor!=g.TxtColor) {
			mTxtSizes.gTxtColor=g.TxtColor;
			needed=true;
		}
		if (mTxtSizes.gUseAkkord!=g.UseAkkord) {
			mTxtSizes.gUseAkkord=g.UseAkkord;
			needed=true;
		}
		if (mTxtSizes.gUseKotta!=g.UseKotta) {
			mTxtSizes.gUseKotta=g.UseKotta;
			needed=true;
		}
		if (mTxtSizes.gVCenter!=g.VCenter) {
			mTxtSizes.gVCenter=g.VCenter;
			needed=true;
		}
		if (mTxtSizes.gKottaArany!=g.KottaArany) {
			mTxtSizes.gKottaArany=g.KottaArany;
			needed=true;
		}
		if (mTxtSizes.gAkkordArany!=g.AkkordArany) {
			mTxtSizes.gAkkordArany=g.AkkordArany;
			needed=true;
		}
		if (mTxtSizes.gBoldText!=g.BoldText) {
			mTxtSizes.gBoldText=g.BoldText;
			needed=true;
		}

		if (needed) mTxtSizes.needrecalc=true;
		
		return needed;
	}

	@Override
	public void OnRecalc()
	{
		RecalcIfNeeded();
		mTxtSizes.needrecalc=true;
	}
	
	public void loadFromRec(RecText r) {
		r.NormalizeEOL();
		int n = r.getLineCount();
		mTxtSizes.mScholaLine=r.getLine(0);
		mTxtSizes.mTitle=r.getLine(1);
		String s;
		for (int i=2; i<n; i++) {
			s=r.getLine(i);
			mTxtSizes.addLine(s);
		}
		s=r.getLine(n);
		if (!s.isEmpty()) mTxtSizes.addLine(s);
	}

	@Override
	public void OnDrawClipped(Canvas canvas)
	{
		//Log.d("Draw","DiaText.Draw");
		canvas.drawColor(mTcp.G.BkColor);
		mTxtSizes.mHighPoint=mTcp.G.WordToHighlight;
		mTxtSizes.gTxtColor=mTcp.G.TxtColor;
		mTxtSizes.gHighColor=mTcp.G.HiColor;
		mTxtSizes.Draw(canvas,getWidth(canvas),getHeight(canvas));
		//Log.d("Draw","DiaText.Draw finished");
	}
}

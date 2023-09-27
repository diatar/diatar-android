package com.polyjoe.DiaVetito;

import eu.diatar.library.*;

public class Globals
{
	public int BkColor;
	public int TxtColor;
	public int BlankColor;
	public int FontSize;
	public int TitleSize;
	public int LeftIndent;
	public int Spacing100;
	public int HKey;
	public int WordToHighlight;
	public int BorderL, BorderT, BorderR, BorderB;
	public String FontName;
	public boolean IsBlankPic;
	public boolean AutoResize;
	public boolean Projecting;
	public boolean ShowBlankPic;
	public boolean HCenter, VCenter;
	public boolean ScholaMode;
	public boolean UseAkkord, UseKotta;
	public boolean UseTransitions;
	public int EndProgram;
	public boolean HideTitle;
	public boolean InverzKotta;
	public int BgMode;
	public int HiColor;
	public int KottaArany, AkkordArany;
	public boolean Border2Clip;		//use RecState.BorderXXX
	public boolean BoldText;

	public Globals() {
		BkColor=0xFF000000;
		TxtColor=0xFFFFFFFF;
		BlankColor=0xFF000000;
		HiColor=0xFF00FFFF;
		FontSize=20;
		TitleSize=10;
		LeftIndent=2;
		Spacing100=100;
		AutoResize=true;
		KottaArany=100;
		AkkordArany=100;
	}
	
	public void LoadFromRec(RecState r) {
		BkColor=r.getBkColor() | 0xFF000000;
		TxtColor=r.getTxtColor() | 0xFF000000;
		BlankColor=r.getBlankColor() | 0xFF000000;
		HiColor=r.getHiColor() | 0xFF000000;
		FontSize=r.getFontSize();
		TitleSize=r.getTitleSize();
		LeftIndent=r.getLeftIndent();
		Spacing100=r.getSpacing100();
		HKey=r.getHKey();
		WordToHighlight=r.getWordToHighlight();
		BorderL=r.getBorderL();
		BorderT=r.getBorderT();
		BorderR=r.getBorderR();
		BorderB=r.getBorderB();
		FontName=r.getFontName();
		IsBlankPic=r.getIsBlankPic();
		AutoResize=r.getAutoResize();
		Projecting=r.getProjecting();
		ShowBlankPic=r.getShowBlankPic();
		HCenter=r.getHCenter();
		VCenter=r.getVCenter();
		ScholaMode=r.getScholaMode();
		UseAkkord=r.getUseAkkord();
		UseKotta=r.getUseKotta();
		UseTransitions=r.getUseTransitions();
		EndProgram=r.getEndProgram();
		HideTitle=r.getHideTitle();
		InverzKotta=r.getInverzKotta();
		BgMode=r.getBgMode();
		KottaArany=r.getKottaArany();
		AkkordArany=r.getAkkordArany();
		BoldText=r.getBoldText();
	}
}

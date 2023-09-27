package eu.diatar.library;

public class RecState extends RecBase
{
	public static final int bgCENTER = 0;
	public static final int bgZOOM = 1;
	public static final int bgFULL = 2;
	public static final int bgCASCADE = 3;
	public static final int bgMIRROR = 4;

	public static final int epNOTHING = 0x00000000;
	public static final int epSTOP = 0xADD00ADD;
	public static final int epSHUTDOWN = 0xDEAD80FF;
	public static final int epSKIPSERIALOFF = 0x11111111;
	public static final int epPROJECTORON = 0xBEBEBEBE;
	public static final int epPROJECTOROFF = 0xD00FF0FF;
	
	public RecState() { setMaxlen(349); }

	public int getBkColor() { return getColor(0); }
	  public void setBkColor(int val) { setColor(0,val); }
	public int getTxtColor() { return getColor(4); }
	  public void setTxtColor(int val) { setColor(4,val); }
	public int getBlankColor() { return getColor(8); }
	  public void setBlankColor(int val) { setColor(8,val); }
	public int getFontSize() { return getInt(12); }
	  public void setFontSize(int val) { setInt(12,val); }
	public int getTitleSize() { return getInt(16); }
	  public void setTitleSize(int val) { setInt(16,val); }
	public int getLeftIndent() { return getInt(20); }
	  public void setLeftIndent(int val) { setInt(20,val); }
	public int getSpacing100() { return getInt(24); }
	  public void setSpacing100(int val) { setInt(24,val); }
	public int getHKey() { return getInt(28); }
	  public void setHKey(int val) { setInt(28,val); }
	public int getWordToHighlight() { return getInt(32); }
	  public void setWordToHighlight(int val) { setInt(32,val); }
	public int getBorderL() { return getInt(36); }
	  public void setBorderL(int val) { setInt(36,val); }
	public int getBorderT() { return getInt(40); }
	  public void setBorderT(int val) { setInt(40,val); }
	public int getBorderR() { return getInt(44); }
	  public void setBorderR(int val) { setInt(44,val); }
	public int getBorderB() { return getInt(48); }
	  public void setBorderB(int val) { setInt(48,val); }
	public String getFontName() { return getPasStr(52); }
	  public void setFontName(String val) { setPasStr(52,val); }
	public boolean getIsBlankPic() { return getBool(308); }
	  public void setIsBlankPic(boolean val) { setBool(308,val); }
	public boolean getAutoResize() { return getBool(309); }
	  public void setAutoResize(boolean val) { setBool(309,val); }
	public boolean getProjecting() { return getBool(310); }
	  public void setProjecting(boolean val) { setBool(310,val); }
	public boolean getShowBlankPic() { return getBool(311); }
	  public void setShowBlankPic(boolean val) { setBool(311,val); }
	public boolean getHCenter() { return getBool(312); }
	  public void setHCenter(boolean val) { setBool(312,val); }
	public boolean getVCenter() { return getBool(313); }
	  public void setVCenter(boolean val) { setBool(313,val); }
	public boolean getScholaMode() { return getBool(314); }
	  public void setScholaMode(boolean val) { setBool(314,val); }
	public boolean getUseAkkord() { return getBool(315); }
	  public void setUseAkkord(boolean val) { setBool(315,val); }
	public boolean getUseKotta() { return getBool(316); }
	  public void setUseKotta(boolean val) { setBool(316,val); }
	public boolean getUseTransitions() { return getBool(317); }
	  public void setUseTransitions(boolean val) { setBool(317,val); }
	public int getEndProgram() { return getInt(318); }
	  public void setEndProgram(int val) { setInt(318,val); }
	public boolean getHideTitle() { return getBool(322); }
	  public void setHideTitle(boolean val) { setBool(322,val); }
	public boolean getInverzKotta() { return getBool(323); }
	  public void setInverzKotta(boolean val) { setBool(323,val); }
	public int getBgMode() { return getInt(324); }
	  public void setBgMode(int val) { setInt(324,val); }
	public int getHiColor() { return getColor(328); }
	  public void setHiColor(int val) { setColor(328,val); }
	public int getKottaArany() { return getInt(332); }
	  public void setKottaArany(int val) { setInt(332,val); }
	public int getAkkordArany() { return getInt(336); }
	  public void setAkkordArany(int val) { setInt(336,val); }
	public int getBackTransPerc() { return getInt(340); }
	  public void setBackTransPerc(int val) { setInt(340,val); }
	public int getBlankTransPerc() { return getInt(344); }
	  public void setBlankTransPerc(int val) { setInt(344,val); }
	public boolean getBoldText() { return getBool(348); }
	  public void setBoldText(boolean val) { setBool(348, val); }
}

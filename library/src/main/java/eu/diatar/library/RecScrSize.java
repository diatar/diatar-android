package eu.diatar.library;

public class RecScrSize extends RecBase
{
	public RecScrSize() { setMaxlen(9); }

	public int getWidth() { return getInt(0); }
	public int getHeight() { return getInt(4); }
	public boolean getKorusMode() { return getBool(8); }

	public void setWidth(int val) { setInt(0,val); }
	public void setHeight(int val) { setInt(4,val); }
	public void setKorusMode(boolean val) { setBool(8,val); }
}

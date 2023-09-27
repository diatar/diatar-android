package eu.diatar.library;

public class RecBase
{
	public byte buf[];
	public int len;
	private int maxlen;

	public int getMaxlen() { return maxlen; }

	public void setMaxlen(int newlen) {
		try {
			buf = new byte[newlen];
			maxlen=newlen;
		} catch (Exception e) {
			maxlen=0;
			buf=null;
		}
		len=0;
	}

	public void clear() { len=0; }

	public int getLen() { return len; }

	public void setLen(int val) { len=val; }

	public boolean isFull() { return len>=maxlen; }

	public void append(int byteval) {
		if (len<maxlen) 
			buf[len++]=(byte)byteval;
	}

	public boolean getBool(int ofs) { return buf[ofs]!=0; }

	public int getInt(int ofs) {
		int res =
			((int)(buf[ofs]) & 0x000000FF)|
			(((int)(buf[ofs+1])<<8) & 0x0000FF00)|
			(((int)(buf[ofs+2])<<16) & 0x00FF0000)|
			(((int)(buf[ofs+3])<<24) & 0xFF000000);
		return res;
	}

	public int getColor(int ofs) {
		int res =
			((int)(buf[ofs+2]) & 0x000000FF)|
			(((int)(buf[ofs+1])<<8) & 0x0000FF00)|
			(((int)(buf[ofs+0])<<16) & 0x00FF0000);
		return res|0xFF000000;
	}

	public char getChar(int ofs) { return (char)(buf[ofs]); }

	public void setBool(int ofs, boolean val) {
		buf[ofs]=(byte)(val ? 1 : 0);
	}

	public String getPasStr(int ofs) {
		int l = buf[ofs];
		if (l<=0) return "";
		return new String(buf,ofs+1,l);
	}

	public void setInt(int ofs, int val) {
		buf[ofs]=(byte)(val & 0xFF);
		buf[ofs+1]=(byte)((val>>8) & 0xFF);
		buf[ofs+2]=(byte)((val>>16) & 0xFF);
		buf[ofs+3]=(byte)((val>>24) & 0xFF);
	}

	public void setColor(int ofs, int val) {
		buf[ofs+2]=(byte)(val & 0xFF);
		buf[ofs+1]=(byte)((val>>8) & 0xFF);
		buf[ofs+0]=(byte)((val>>16) & 0xFF);
		buf[ofs+3]=(byte)0;
	}

	public void setChar(int ofs, char val) {
		buf[ofs]=(byte)(val);
	}

	public void setPasStr(int ofs, String val) {
		int l = val.length();
		buf[ofs++]=(byte)l;
		int i=0;
		while (i<l) {
			buf[ofs++]=(byte)val.charAt(i++);
		}
	}
}

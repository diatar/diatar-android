package eu.diatar.library;

public class RecHdr extends RecBase
{
	public static final byte itState = (byte)0;
	public static final byte itScrSize = (byte)1;
	public static final byte itPic = (byte)2;
	public static final byte itBlank = (byte)3;
	public static final byte itText = (byte)4;
	public static final byte itAskSize = (byte)5;
	public static final byte itIdle = (byte)6;

	public RecHdr() {
		setMaxlen(12);
	}

	public boolean isIdOk() {
		if (len<4) return false;
		return
			buf[0]==(byte)(0xDA) &&
			getChar(1)=='i' &&
			getChar(2)=='p' &&
			getChar(3)=='J';
	}

	public boolean tryId() {
		while (len>0) {
			if (isIdOk()) return true;
			if (buf[0]!=(byte)0xDA ||
				(len>1 && getChar(1)!='i') ||
				(len>2 && getChar(2)!='p') ||
				(len>3 && getChar(3)!='J')
				) {
				for (int i=1; i<len; i++) buf[i-1]=buf[i];
				len--;
			} else
				return false;
		}
		return false;
	}

	public boolean isOk() {
		return len==12 && isIdOk();	
	}

	public void setID() {
		buf[0]=(byte)(0xDA);
		setChar(1,'i');
		setChar(2,'p');
		setChar(3,'J');
	}

	public byte getType() { return buf[4]; }

	public void setType(byte ittype) { buf[4]=ittype; }

	public int getSize() { return getInt(8); }

	public void setSize(int newval) { setInt(8,newval); }

}

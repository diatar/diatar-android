package diatar.eu;

import android.os.Bundle;

public class DiaProp {
    public static final int b3UNUSED = 0;
    public static final int b3FALSE = 1;
    public static final int b3TRUE = 2;

    public int mBkColor, mTxColor, mHiColor, mBlankColor;
    public String mFontName;
    public int mFontSize, mTitleSize;
    public int mIndent, mSpacing;
    public byte mFontBold, mHCenter, mVCenter;
    public boolean mDblDia;

    public DiaProp() {
        mBkColor = 0;
        mTxColor = 0x00FFFFFF;
        mHiColor = 0x00FFFFFF;
        mBlankColor = 0;
        mFontName = "";
        mFontSize = -1;
        mTitleSize = -1;
        mIndent = -1;
        mSpacing = -1;
        mFontBold = b3UNUSED;
        mHCenter = b3UNUSED;
        mVCenter = b3UNUSED;
        mDblDia = false;
    }

    public void toBundle(Bundle bd, int idx) {
        String fx = (idx > 0 ? String.valueOf(idx) : "");
        bd.putInt(G.idBKCOLOR + fx, mBkColor);
        bd.putInt(G.idTXCOLOR + fx, mTxColor);
        bd.putInt(G.idBLANKCOLOR + fx, mBlankColor);
        bd.putInt(G.idFONTSIZE + fx, mFontSize);
        bd.putInt(G.idTITLESIZE + fx, mTitleSize);
        bd.putInt(G.idLEFTINDENT + fx, mIndent);
        bd.putInt(G.idSPACING + fx, mSpacing);
        bd.putInt(G.idHCENTER + fx, mHCenter);
        bd.putInt(G.idVCENTER + fx, mVCenter);
    }

    public void fromBundle(Bundle bd, int idx) {
        String fx = (idx > 0 ? String.valueOf(idx) : "");
        mBkColor = bd.getInt(G.idBKCOLOR + fx, -1);
        mTxColor = bd.getInt(G.idTXCOLOR + fx, -1);
        mBlankColor = bd.getInt(G.idBLANKCOLOR + fx, -1);
        mFontSize = bd.getInt(G.idFONTSIZE + fx, -1);
        mTitleSize = bd.getInt(G.idTITLESIZE + fx, -1);
        mIndent = bd.getInt(G.idLEFTINDENT + fx, -1);
        mSpacing = bd.getInt(G.idSPACING + fx, -1);
        mHCenter = bd.getByte(G.idHCENTER + fx, (byte) b3UNUSED);
        mVCenter = bd.getByte(G.idVCENTER + fx, (byte) b3UNUSED);
    }

    public static byte BoolToB3(boolean val) {
        return (byte) (val ? b3TRUE : b3FALSE);
    }

    public static boolean B3ToBool(byte b3) {
        return (b3 == b3TRUE);
    }
}

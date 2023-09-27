package diatar.eu;

import android.text.*;
import android.view.*;
import android.widget.*;

public class MinMaxFilter implements InputFilter, View.OnFocusChangeListener
{
	private int mMin, mMax;
	
	public MinMaxFilter(int aMin, int aMax) {
		if (aMin<aMax) {
			mMin=aMin; mMax=aMax;
		} else {
			mMin=aMax; mMax=aMin;
		}
	}
	
	static public void Add(EditText et, int aMin, int aMax) {
		MinMaxFilter mm = new MinMaxFilter(aMin,aMax);
		et.setFilters(new InputFilter[]{mm});
		et.setOnFocusChangeListener(mm);
	}
	
	@Override
	public CharSequence filter(CharSequence src,
		int start, int end,
		Spanned dest, int ds, int de)
	{
		String s = dest.subSequence(0,ds).toString()
			+src.subSequence(start,end).toString()
			+dest.subSequence(de,dest.length()).toString();
		try {
			int v = Integer.parseInt(s);
			if (v<=mMax) return null;
		} catch(NumberFormatException e) {}
		return "";
	}
	
	@Override
    public void onFocusChange(View vw, boolean hasFocus) {
		if (hasFocus) return;
		EditText et = (EditText)vw;
		try {
			int v = Integer.parseInt(et.getText().toString());
			if (v>=mMin&&v<=mMax) return;
		} catch(NumberFormatException e) {}
		et.setText(String.valueOf(mMin));
	}
}

package diatar.eu.net;

import android.text.*;

public class IpAddrFilter implements InputFilter {
	@Override
	public CharSequence filter(CharSequence source, int start, int end,
							   android.text.Spanned dest, int dstart, int dend) {
		if (end > start) {
			String destTxt = dest.toString();
			String resultingTxt = destTxt.substring(0, dstart)
				+ source.subSequence(start, end)
				+ destTxt.substring(dend);
			if (!resultingTxt
				.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
				return "";
			} else {
				String[] splits = resultingTxt.split("\\.");
				for (int i = 0; i < splits.length; i++) {
					if (Integer.valueOf(splits[i]) > 255) {
						return "";
					}
				}
			}
		}
		return null;
	}

}

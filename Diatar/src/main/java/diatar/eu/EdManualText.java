package diatar.eu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

public class EdManualText extends Activity
{
	static private final int MYSTYLE_BOLD = 1;
	static private final int MYSTYLE_ITALIC = 2;
	static private final int MYSTYLE_UNDERLINE = 4;
	static private final int MYSTYLE_STRIKE = 8;

	//spec.chars
	static private final char chESCAPE = '\\';
	static private final char chLINEBREAK = '\u00B6'; //'\u2444'; //'\u2E3E';	//sortoresi javaslat
	static private final char chSOFTHYPHEN = '\u00AC';	//felteteles kotojel
	static private final char chNBSPACE = '\u2423';		//nemtorheto szokoz
	static private final char chNBHYPHEN = '\u2445';	//nemtorheto kotojel

	private EditText mCim;
	private EditDiaText mTxt;
	private ToggleButton mBoldBtn, mItalicBtn, mUnderlineBtn, mStrikeBtn;
	private boolean isBtoggled = false, isItoggled = false, isUtoggled = false, isStoggled = false;
	private Button mSoftHyphenBtn, mNBHyphenBtn, mNBSpaceBtn, mBreakBtn;

	@Override
	protected void onCreate(Bundle bd) {
		super.onCreate(bd);

		setContentView(R.layout.edmanualtext);
		setTitle("Szöveg");
		mCim = findViewById(R.id.emtCim);
		mTxt = findViewById(R.id.emtSzoveg);

		mBoldBtn = findViewById(R.id.emtBoldBtn);
		mItalicBtn = findViewById(R.id.emtItalicBtn);
		mUnderlineBtn = findViewById(R.id.emtUnderlineBtn);
		mStrikeBtn = findViewById(R.id.emtStrikeBtn);

		mSoftHyphenBtn = findViewById(R.id.emtSoftHyphen);
		mNBHyphenBtn = findViewById(R.id.emtNBHyphen);
		mNBSpaceBtn = findViewById(R.id.emtNBSpace);
		mBreakBtn = findViewById(R.id.emtBreak);

		mBoldBtn.setOnClickListener(view -> toggleB()); //toggleStyle(new StyleSpan(Typeface.BOLD)));
		mItalicBtn.setOnClickListener(view -> toggleI()); //toggleStyle(new StyleSpan(Typeface.ITALIC)));
		mUnderlineBtn.setOnClickListener(view -> toggleU()); //toggleStyle(new UnderlineSpan()));
		mStrikeBtn.setOnClickListener(view -> toggleS()); //toggleStyle(new StrikethroughSpan()));

		mSoftHyphenBtn.setText(""+chSOFTHYPHEN);
		mNBHyphenBtn.setText(""+chNBHYPHEN);
		mNBSpaceBtn.setText(""+chNBSPACE);
		mBreakBtn.setText(""+chLINEBREAK);
		mSoftHyphenBtn.setOnClickListener(view -> insertChar(chSOFTHYPHEN));
		mNBHyphenBtn.setOnClickListener(view -> insertChar(chNBHYPHEN));
		mNBSpaceBtn.setOnClickListener(view -> insertChar(chNBSPACE));
		mBreakBtn.setOnClickListener(view -> insertChar(chLINEBREAK));

		mTxt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count > 0) { // Ha új szöveg érkezett
					applyStyleToNewText(start, start + count);
				}
				updateToggleButtonStates();
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
		// Figyeljük a kijelölést
		mTxt.setOnSelectionChangedListener(this::updateToggleButtonStates);

		String knev;
		String txt;
		if (bd==null) {
			Intent it = getIntent();
			knev=it.getStringExtra(G.idCIM);
			txt=it.getStringExtra(G.idTXT);
			if (knev==null) knev = "";
			if (txt==null) txt = "";
		} else{
			knev=bd.getString(G.idCIM);
			txt=bd.getString(G.idTXT);
		}
		//txt="Ez \\Bvastag\\b ez meg \\Időlt\\i \\_ \\ és \\Ualá\\-húzott\\u\\.vagy \\Sáthúzott\\s.";
		mCim.setText(knev);
		mTxt.setText(encodeTxt(txt));
		//mCim.setText(decodeTxt(mTxt.getText()));
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(G.idCIM,mCim.getText().toString());
		outState.putString(G.idTXT,decodeTxt(mTxt.getText()));
	}

	public void onCancel(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOk(View v) {
		Intent it = new Intent();
		it.putExtra(G.idCIM, mCim.getText().toString());
		it.putExtra(G.idTXT, decodeTxt(mTxt.getText()));
		setResult(RESULT_OK,it);
		finish();
	}

	private void insertChar(char ch) {
		int start = Math.max(mTxt.getSelectionStart(),0);
		int end = Math.max(mTxt.getSelectionEnd(),0);
		mTxt.getText().replace(Math.min(start,end), Math.max(start,end), ""+ch);
	}

	private boolean mInApply = false;
	private void applyStyleToNewText(int start, int end) {
		if (mInApply) return;
		mInApply=true;

		boolean anyStyle = false;
		Spannable spannable = new SpannableStringBuilder(mTxt.getText());

		if (isBtoggled) {
			if (mBoldBtn.isChecked()) {
				spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				anyStyle=true;
			} else {
				StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);
				for (StyleSpan span : spans) {
					if (span.getStyle() != Typeface.BOLD) continue;
					int sp1 = spannable.getSpanStart(span), sp2 = spannable.getSpanEnd(span);
					spannable.removeSpan(span);
					if (sp1<start) spannable.setSpan(new StyleSpan(Typeface.BOLD), sp1,start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					if (end<sp2) spannable.setSpan(new StyleSpan(Typeface.BOLD), end,sp2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					anyStyle = true;
				}
			}
		}
		if (isItoggled) {
			if (mItalicBtn.isChecked()) {
				spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				anyStyle=true;
			} else {
				StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);
				for (StyleSpan span : spans) {
					if (span.getStyle() != Typeface.ITALIC) continue;
					int sp1 = spannable.getSpanStart(span), sp2 = spannable.getSpanEnd(span);
					spannable.removeSpan(span);
					if (sp1<start) spannable.setSpan(new StyleSpan(Typeface.ITALIC), sp1,start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					if (end<sp2) spannable.setSpan(new StyleSpan(Typeface.ITALIC), end,sp2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					anyStyle = true;
				}
			}
		}
		if (isUtoggled) {
			if (mUnderlineBtn.isChecked()) {
				spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				anyStyle=true;
			} else {
				UnderlineSpan[] spans = spannable.getSpans(start, end, UnderlineSpan.class);
				for (UnderlineSpan span : spans) {
					int sp1 = spannable.getSpanStart(span), sp2 = spannable.getSpanEnd(span);
					spannable.removeSpan(span);
					if (sp1<start) spannable.setSpan(new UnderlineSpan(), sp1,start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					if (end<sp2) spannable.setSpan(new UnderlineSpan(), end,sp2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					anyStyle = true;
				}
			}
		}
		if (isStoggled) {
			if (mStrikeBtn.isChecked()) {
				spannable.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				anyStyle=true;
			} else {
				StrikethroughSpan[] spans = spannable.getSpans(start, end, StrikethroughSpan.class);
				for (StrikethroughSpan span : spans) {
					int sp1 = spannable.getSpanStart(span), sp2 = spannable.getSpanEnd(span);
					spannable.removeSpan(span);
					if (sp1<start) spannable.setSpan(new StrikethroughSpan(), sp1,start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					if (end<sp2) spannable.setSpan(new StrikethroughSpan(), end,sp2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					anyStyle = true;
				}
			}
		}

		if (anyStyle) {
			mTxt.setText(spannable);
			mTxt.setSelection(end);
		}

		mInApply=false;
	}

	private void toggleB() {
		isBtoggled=toggleStyle(new StyleSpan(Typeface.BOLD));
	}

	private void toggleI() {
		isItoggled=toggleStyle(new StyleSpan(Typeface.ITALIC));
	}

	private void toggleU() {
		isUtoggled=toggleStyle(new UnderlineSpan());
	}

	private void toggleS() {
		isStoggled=toggleStyle(new StrikethroughSpan());
	}

	private boolean toggleStyle(Object spanType) {	// true = nincs kijeloles
		int start = mTxt.getSelectionStart();
		int end = mTxt.getSelectionEnd();
		if (start == end) return true; // Nincs kijelölt szöveg

		Spannable spannable = new SpannableStringBuilder(mTxt.getText());
		Object[] spans = spannable.getSpans(start, end, spanType.getClass());
		boolean styleExists = false;
		// Megnézzük, hogy már van-e ilyen stílus
		for (Object span : spans) {
			if (span instanceof StyleSpan && spanType instanceof StyleSpan &&
					((StyleSpan) span).getStyle() != ((StyleSpan) spanType).getStyle())
				continue;
			spannable.removeSpan(span);
			styleExists = true;
		}

		// Ha nem volt ilyen stílus, akkor hozzáadjuk
		if (!styleExists) {
			spannable.setSpan(spanType, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		mTxt.setText(spannable);
		mTxt.setSelection(start, end); // Visszaállítjuk a kijelölést
		updateToggleButtonStates();

		return false;
	}

	private void updateToggleButtonStates(int i, int i1) {
		updateToggleButtonStates();
	}

	private boolean mInUpdate = false;
	private void updateToggleButtonStates() {
		if (mInUpdate) return;
		mInUpdate=true;

		int start = mTxt.getSelectionStart();
		int end = mTxt.getSelectionEnd();

		isBtoggled=false; isItoggled=false; isUtoggled=false; isStoggled=false;
		updateToggleButtonState(mBoldBtn, start, end, StyleSpan.class, Typeface.BOLD);
		updateToggleButtonState(mItalicBtn, start, end, StyleSpan.class, Typeface.ITALIC);
		updateToggleButtonState(mUnderlineBtn, start, end, UnderlineSpan.class, -1);
		updateToggleButtonState(mStrikeBtn, start, end, StrikethroughSpan.class, -1);

		mInUpdate=false;
	}

	private boolean isSameSpan(Object span, Class<?> spanClass, int styleType) {
		if (span.getClass() != spanClass) return false;
		if (!(span instanceof StyleSpan)) return true;
		return ((StyleSpan)span).getStyle()==styleType;
	}

	private void updateToggleButtonState(ToggleButton button, int start, int end, Class<?> spanClass, int styleType) {
		if (start == end) {
			button.setAlpha(1.0f);
			if (start > 0) {
				Spannable spannable = new SpannableStringBuilder(mTxt.getText());
				Object[] spans = spannable.getSpans(start - 1, start, spanClass);

				for (Object span : spans) {
					if (isSameSpan(span,spanClass,styleType)) {
						button.setChecked(true);
						return;
					}
				}
			}
			button.setChecked(false);
			return;
		}

		Spannable spannable = new SpannableStringBuilder(mTxt.getText());

		boolean hasOn = false, hasOff = false;
		for (int i = start; i < end; i++) {
			Object[] charSpans = spannable.getSpans(i, i + 1, spanClass);
			boolean found = false;
			for (Object span : charSpans) {
				if (isSameSpan(span,spanClass,styleType)) {
					found = true;
					break;
				}
			}
			if (found) {
				hasOn=true;
				if (hasOff) break;
			} else {
				hasOff=true;
				if (hasOn) break;
			}
		}

		if (hasOn && hasOff) {
			button.setAlpha(0.5f); // Vegyes állapot
			button.setChecked(false);
		} else {
			button.setAlpha(1.0f);
			button.setChecked(hasOn);
		}
	}

	@NonNull
	static public Spannable encodeTxt(@NonNull String txt) {
		SpannableStringBuilder result = new SpannableStringBuilder();

		int posB = -1, posI = -1, posU = -1, posS = -1;
		int len = txt.length();
		boolean esc = false;
		int currpos=0;
		for (int i=0; i<len; i++) {
			char ch = txt.charAt(i);
			if (!esc) {
				if (ch==chESCAPE) {
					esc=true;
				} else {
					if (ch==13) ch='\n';
					result.append(ch);
					currpos++;
				}
				continue;
			}
			esc=false;
			switch (ch) {
				case 'B':
					if (posB<0) posB=currpos;
					break;
				case 'I':
					if (posI<0) posI=currpos;
					break;
				case 'U':
					if (posU<0) posU=currpos;
					break;
				case 'S':
					if (posS<0) posS=currpos;
					break;
				case 'b':
					if (posB>=0) {
						result.setSpan(new StyleSpan(Typeface.BOLD), posB, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						posB = -1;
					}
					break;
				case 'i':
					if (posI>=0) {
						result.setSpan(new StyleSpan(Typeface.ITALIC), posI, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						posI = -1;
					}
					break;
				case 'u':
					if (posU>=0) {
						result.setSpan(new UnderlineSpan(), posU, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						posU = -1;
					}
					break;
				case 's':
					if (posS>=0) {
						result.setSpan(new StrikethroughSpan(), posS, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						posS = -1;
					}
					break;
				case 'G':
				case 'K':
				case '?':
					while (i<len && txt.charAt(i)!=';') i++;
					break;
				case '(':
				case ')':
					//majd egyszer feldolgozzuk...
					break;
				case chESCAPE:
					result.append(ch);
					currpos++;
					break;
				case '.':	//sortores javaslat
					result.append(chLINEBREAK);
					currpos++;
					break;
				case ' ':	//nemtorheto szokoz
					result.append(chNBSPACE);
					currpos++;
					break;
				case '-':	//felteteles kotojel
					result.append(chSOFTHYPHEN);
					currpos++;
					break;
				case '_':	//nemtorheto kotojel
					result.append(chNBHYPHEN);
					currpos++;
					break;
			}
		}
		//sorvege
		if (posB>=0)
			result.setSpan(new StyleSpan(Typeface.BOLD), posB, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (posI>=0)
			result.setSpan(new StyleSpan(Typeface.ITALIC), posI, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (posU>=0)
			result.setSpan(new UnderlineSpan(), posU, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (posS>=0)
			result.setSpan(new StrikethroughSpan(), posS, currpos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return result;
	}

	@NonNull
	static public String decodeTxt(@NonNull Spannable spanned) {
		StringBuilder sb = new StringBuilder();

		int len = spanned.length();
		boolean isB = false, isI = false, isU = false, isS = false;
		int lastp = -1;
		boolean isNL = false;
		while (lastp<len) {
			int nextBI = spanned.nextSpanTransition(lastp, len, StyleSpan.class);
			int nextU = spanned.nextSpanTransition(lastp, len, UnderlineSpan.class);
			int nextS = spanned.nextSpanTransition(lastp, len, StrikethroughSpan.class);
			int minBIUS = Integer.min(Integer.min(nextBI, nextU),nextS);

			//minden karaktert kiirunk a kovetkezo formatum-atmenetig
			int prevp=lastp;
			while (lastp<minBIUS) {
				if (lastp<0) {
					lastp++;
					continue;
				}
				char ch = spanned.charAt(lastp++);
				//soremeles ket oldalan ki/bekapcsoljuk a formatumot
				if (ch=='\n') {
					if (!isNL) {
						if (isB) { sb.append(chESCAPE); sb.append('b'); }
						if (isI) { sb.append(chESCAPE); sb.append('i'); }
						if (isU) { sb.append(chESCAPE); sb.append('u'); }
						if (isS) { sb.append(chESCAPE); sb.append('s'); }
					}
					sb.append(ch); //((char)13);
					isNL=true;
					continue;
				}
				if (isNL) {
					if (isB) { sb.append(chESCAPE); sb.append('B'); }
					if (isI) { sb.append(chESCAPE); sb.append('I'); }
					if (isU) { sb.append(chESCAPE); sb.append('U'); }
					if (isS) { sb.append(chESCAPE); sb.append('S'); }
					isNL=false;
				}
				switch (ch) {
					case chESCAPE: sb.append(chESCAPE); sb.append(chESCAPE); break;
					case chLINEBREAK: sb.append(chESCAPE); sb.append('.'); break;
					case chNBSPACE: sb.append(chESCAPE); sb.append(' '); break;
					case chSOFTHYPHEN: sb.append(chESCAPE); sb.append('-'); break;
					case chNBHYPHEN: sb.append(chESCAPE); sb.append('_'); break;
					default: sb.append(ch); break;
				}
			}
			if (isNL) {
				if (isB) { sb.append(chESCAPE); sb.append('B'); }
				if (isI) { sb.append(chESCAPE); sb.append('I'); }
				if (isU) { sb.append(chESCAPE); sb.append('U'); }
				if (isS) { sb.append(chESCAPE); sb.append('S'); }
				isNL=false;
			}

			//formatum valtozik ezen a helyen
			if (prevp<0) prevp=0;
			if (minBIUS==nextBI) {
				StyleSpan[] spans = spanned.getSpans(prevp, nextBI+1, StyleSpan.class);
				for (StyleSpan span : spans) {
					if (span.getStyle() == Typeface.BOLD) {
						int ss = spanned.getSpanStart(span), se = spanned.getSpanEnd(span);
						if (spanned.getSpanStart(span)==nextBI) {
							sb.append(chESCAPE);
							sb.append('B');
							isB=true;
						} else if (spanned.getSpanEnd(span)==nextBI) {
							sb.append(chESCAPE);
							sb.append('b');
							isB=false;
						}
					} else if (span.getStyle() == Typeface.ITALIC) {
						if (spanned.getSpanStart(span)==nextBI) {
							sb.append(chESCAPE);
							sb.append('I');
							isI=true;
						} else if (spanned.getSpanEnd(span)==nextBI) {
							sb.append(chESCAPE);
							sb.append('i');
							isI=false;
						}
					}
				}
			}
			if (minBIUS==nextU) {
				UnderlineSpan[] spans = spanned.getSpans(prevp, nextU+1, UnderlineSpan.class);
				for (UnderlineSpan span : spans) {
					if (spanned.getSpanStart(span)==nextU) {
						sb.append(chESCAPE);
						sb.append('U');
						isU=true;
					} else if (spanned.getSpanEnd(span)==nextU) {
						sb.append(chESCAPE);
						sb.append('u');
						isU=false;
					}
				}
			}
			if (minBIUS==nextS) {
				StrikethroughSpan[] spans = spanned.getSpans(prevp, nextS+1, StrikethroughSpan.class);
				for (StrikethroughSpan span : spans) {
					if (spanned.getSpanStart(span)==nextS) {
						sb.append(chESCAPE);
						sb.append('S');
						isS=true;
					} else if (spanned.getSpanEnd(span)==nextS) {
						sb.append(chESCAPE);
						sb.append('s');
						isS=false;
					}
				}
			}
		}

		return sb.toString();
	}
}

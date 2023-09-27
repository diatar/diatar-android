package diatar.eu;

import android.os.*;
import android.view.*;
import androidx.fragment.app.Fragment;

import eu.diatar.library.RecState;

public class MainSlideFragment extends Fragment
{
	public static final String argPOS = "posnum";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//View v = inflater.inflate(
		//	R.layout.mainslidefragment, container, false);
		//Refresh(v);
		//if (v!=null) return v;
		
		MainActivity ma = (MainActivity)getActivity();
		Bundle args = getArguments();
		int mypos = args.getInt(argPOS);
		
		if (ma.getDiaTip(mypos)==DiaItem.ditPIC) {
			DiaItem d = DiaItem.getByPos(mypos);
			MainPicView pv = new MainPicView(ma,d.mKnev+d.mVnev);
			return pv;
		}
		MainTxtView tv = new MainTxtView(ma);
		DiaItem d = ma.getDia(mypos);
		RecState r = new RecState();
		DiaItem.setupRecState(d,r);
		tv.setProps(r);
		tv.setBackgroundColor(r.getBkColor()|0xFF000000);
		tv.setTxt(ma.getDiaTxt(mypos));
		return tv;
	}
	
	
}

package diatar.eu;
import android.graphics.*;
import android.view.*;
import android.util.*;
import androidx.core.view.GestureDetectorCompat;

public class DiaViewBase extends View
	implements GestureDetector.OnGestureListener
{
	protected MainActivity mMain;
	private GestureDetectorCompat mDetector;
	
	@Override
    public boolean onTouchEvent(MotionEvent event){
        if (mDetector.onTouchEvent(event))
            return true;
		return super.onTouchEvent(event);
    }

	@Override
	public boolean onDown(MotionEvent p1)
	{
		//Log.d("Gesture","onDown "+p1.toString());
		return true;
	}

	@Override
	public void onShowPress(MotionEvent p1)
	{
		//Log.d("Gesture","onShowPress "+p1.toString());
	}

	@Override
	public boolean onSingleTapUp(MotionEvent p1)
	{
		//Log.d("Gesture","onSingleTapUp "+p1.toString());
		mMain.flipShowState();
		if (G.sIsFullScr) invalidate();
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		//Log.d("Gesture","onScroll "+p1.toString()+" , "+p2.toString());
		return true;
	}

	@Override
	public void onLongPress(MotionEvent p1)
	{
		//Log.d("Gesture","onLongPress "+p1.toString());
	}

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		boolean isup=(p4<0f);
		if (G.sIsFullScr==isup) return false;
		mMain.flipFullScr();
		//Log.d("Gesture","onFling "+p1.toString()+" , "+p2.toString()+"/"+((Float)p3).toString()+"/"+((Float)p4).toString());
		return true;
	}

	
	
	public DiaViewBase(MainActivity ma) {
		super(ma);
		mMain=ma;
		mDetector = new GestureDetectorCompat(ma,this);
	}
	
	protected void drawDia(Canvas canvas) {}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (G.sIsFullScr && !G.sShowing) {
			canvas.drawColor(G.sBlankColor);
			return;
		}
		drawDia(canvas);
	}
}

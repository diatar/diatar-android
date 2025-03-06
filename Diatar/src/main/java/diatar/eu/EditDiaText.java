package diatar.eu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditDiaText extends EditText {

    private OnSelectionChangedListener selectionChangedListener;

    public EditDiaText(Context context) {
        super(context);
    }

    public EditDiaText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditDiaText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selStart, selEnd);
        }
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int start, int end);
    }
}
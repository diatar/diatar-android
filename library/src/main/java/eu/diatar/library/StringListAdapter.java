package eu.diatar.library;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StringListAdapter extends RecyclerView.Adapter<StringListAdapter.ViewHolder> {
    private List<String> mData = new ArrayList<>();
    private int selectedPos = RecyclerView.NO_POSITION;
    private Consumer<Integer> OnClickCallback;


    public void setData(List<String> newData) {
        String selstr=null;
        if (selectedPos!=RecyclerView.NO_POSITION) selstr=mData.get(selectedPos);
        selectedPos=RecyclerView.NO_POSITION;
        mData.clear();
        mData.addAll(newData);
        if (selstr!=null) selectedPos=mData.indexOf(selstr);
        notifyDataSetChanged();
    }

    public String getItem(int idx) {
        if (idx<0 || idx>=mData.size()) return "";
        return mData.get(idx);
    }

    public int findItemPos(String txt) {
        for (int i=0; i<mData.size(); i++) {
            if (mData.get(i).equals(txt))
                return i;
        }
        return RecyclerView.NO_POSITION;
    }

    public int getSelection() {
        return selectedPos;
    }

    public String getSelectionString() {
        return getItem(selectedPos);
    }

    public void setSelection(int newpos) {
        int oldsel = selectedPos;
        selectedPos=newpos;
        notifyItemChanged(oldsel);
        notifyItemChanged(selectedPos);
    }

    public void setClickCallback(Consumer<Integer> onClick) {
        OnClickCallback=onClick;
    }

    private void doOnClickCB(int pos) {
        if (OnClickCallback!=null) OnClickCallback.accept(pos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(16, 16, 16, 16);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(mData.get(position));
        holder.itemView.setBackgroundResource(R.drawable.senderlst);
        holder.itemView.setSelected(selectedPos==position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            setSelection(pos);
            doOnClickCB(pos);
        }

    }
}

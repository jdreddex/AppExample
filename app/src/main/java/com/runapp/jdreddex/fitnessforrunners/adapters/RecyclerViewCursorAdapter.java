package com.runapp.jdreddex.fitnessforrunners.adapters;


import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import com.runapp.jdreddex.fitnessforrunners.App;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    public long getSelectedItemId(){
        return getItemId(selectedItems.keyAt(0));
    }
    public long removeItem(){
        SQLiteStatement statement = App.getInstance().getDb().compileStatement("DELETE FROM reminder WHERE _id = ?");
        statement.bindLong(1, getItemId(selectedItems.keyAt(0)));
        try {
            statement.execute();
        } finally {
            statement.close();
        }
        long removedId = getItemId(selectedItems.keyAt(0));
        return removedId;
    }
    public boolean isSelected(int position){
        return getSelectedItems().contains(position);
    }

    public void toggleSelection(int position){
        int lastPos;
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            lastPos = selectedItems.keyAt(0);
            selectedItems.clear();
            selectedItems.put(position, true);
            notifyItemChanged(lastPos);
        }
        notifyItemChanged(position);
    }
    public void clearSelection(){
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }
    public List<Integer> getSelectedItems(){
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindViewHolder(VH holder, Cursor cursor, int position);

    public RecyclerViewCursorAdapter(Cursor cursor){
        setHasStableIds(true);
        swapCursor(cursor);
    }

    @Override
    public void onBindViewHolder(VH holder, int position){
        if(!mDataValid){
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if(!mCursor.moveToPosition(position)){
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(holder, mCursor, position);
    }

    @Override
    public long getItemId(int position){
        if(mDataValid && mCursor != null && mCursor.moveToPosition(position)){
            return mCursor.getLong(mRowIDColumn);
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount(){
        if(mDataValid && mCursor != null){
            return mCursor.getCount();
        }else{
            return 0;
        }
    }

    protected Cursor getCursor()
    {
        return mCursor;
    }

    public void changeCursor(Cursor cursor){
        Cursor old = swapCursor(cursor);
        if(old != null){
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor){
        if(newCursor == mCursor){
            return null;
        }
        Cursor oldCursor = mCursor;
        if(oldCursor != null){
            if(mDataSetObserver != null){
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;
        if(newCursor != null){
            if(mDataSetObserver != null){
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        }else{
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver(){
        @Override
        public void onChanged(){
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated(){
            mDataValid = false;
            notifyDataSetChanged();
        }
    };
}


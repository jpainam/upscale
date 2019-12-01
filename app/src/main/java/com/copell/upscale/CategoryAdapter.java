package com.copell.upscale;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.copell.upscale.model.Category;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private Context mContext;
    List<Category> mItems;
    private int selectedPos = RecyclerView.NO_POSITION;
    public CategoryAdapter(Context context, List<Category> list) {
        mContext = context;
        mItems = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_root_category, parent, false);

        return new CategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category l = mItems.get(position);
        holder.category_parent_name.setText(l.getName());
        holder.itemView.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        if(mItems != null) {
            return mItems.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_parent_name)
        TextView category_parent_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition() == RecyclerView.NO_POSITION){
                        return;
                    }
                    // Redraw the old selection and the new
                    notifyItemChanged(selectedPos);
                    selectedPos = getLayoutPosition();
                    notifyItemChanged(selectedPos);
                }
            });
        }
    }
}
package com.example.myinteriorapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myinteriorapp.R;
import com.example.myinteriorapp.models.Furniture;

import java.util.List;

public class FurnitureAdapter extends RecyclerView.Adapter<FurnitureAdapter.ViewHolder> {

    private Context context;
    private List<Furniture> furnitureList;

    public FurnitureAdapter(Context context, List<Furniture> furnitureList) {
        this.context = context;
        this.furnitureList = furnitureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_furniture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Furniture furniture = furnitureList.get(position);
        holder.furnitureName.setText(furniture.getName());
        holder.furnitureDescription.setText(furniture.getDescription());
        // TODO: Load furniture image into holder.furnitureImage using a library like Picasso or Glide
    }

    @Override
    public int getItemCount() {
        return furnitureList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView furnitureImage;
        TextView furnitureName;
        TextView furnitureDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            furnitureImage = itemView.findViewById(R.id.furnitureImage);
            furnitureName = itemView.findViewById(R.id.furnitureName);
            furnitureDescription = itemView.findViewById(R.id.furnitureDescription);
        }
    }
}


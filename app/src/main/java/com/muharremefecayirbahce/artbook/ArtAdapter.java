package com.muharremefecayirbahce.artbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muharremefecayirbahce.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).artName);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
            intent.putExtra("info", "old");
            intent.putExtra("artId", artArrayList.get(position).id);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public static class ArtHolder extends RecyclerView.ViewHolder {
        private final RecyclerRowBinding binding;

        public ArtHolder (RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

package com.example.chocominto.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.ItemVocabBinding;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final ArrayList<Vocab> vocabList = new ArrayList<>();
    private OnItemClickCallback onItemClickCallback;

    public void setVocabList(List<Vocab> vocabList) {
        this.vocabList.clear();
        this.vocabList.addAll(vocabList);
        notifyDataSetChanged();
    }

    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabBinding binding = ItemVocabBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vocab vocab = vocabList.get(position);
        holder.bind(vocab);
    }

    @Override
    public int getItemCount() {
        return vocabList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabBinding binding;

        public ViewHolder(ItemVocabBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Vocab vocab) {
            binding.tvCharacter.setText(vocab.getCharacter());
            binding.tvMeaning.setText(vocab.getMeaning());
            binding.tvReading.setText(vocab.getReading());
            binding.tvPartOfSpeech.setText(vocab.getPartOfSpeech());
            binding.tvLevel.setText(String.format("Level %d", vocab.getLevel()));

            itemView.setOnClickListener(v -> {
                if (onItemClickCallback != null) {
                    onItemClickCallback.onItemClicked(vocab);
                }
            });
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(Vocab vocab);
    }
}
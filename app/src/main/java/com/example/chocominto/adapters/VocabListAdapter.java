package com.example.chocominto.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.ItemVocabBinding;

import java.util.ArrayList;
import java.util.List;

public class VocabListAdapter extends RecyclerView.Adapter<VocabListAdapter.VocabListViewHolder> {

    private List<Vocab> vocabularyList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Vocab item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setVocabularyList(List<Vocab> vocabularyList) {
        this.vocabularyList = vocabularyList;
        notifyDataSetChanged();
    }

    public void addVocabs(List<Vocab> items) {
        int startPosition = vocabularyList.size();
        vocabularyList.addAll(items);
        notifyItemRangeInserted(startPosition, items.size());
    }

    @NonNull
    @Override
    public VocabListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabBinding binding = ItemVocabBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VocabListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabListViewHolder holder, int position) {
        Vocab item = vocabularyList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    class VocabListViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabBinding binding;

        public VocabListViewHolder(ItemVocabBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(vocabularyList.get(position));
                }
            });
        }

        public void bind(Vocab item) {
            binding.tvCharacter.setText(item.getCharacter());
            binding.tvReading.setText(item.getReading());
            binding.tvMeaning.setText(item.getMeaning());
            binding.tvPartOfSpeech.setText(item.getPartOfSpeech());
            binding.tvLevel.setText(String.format("Level %d", item.getLevel()));
        }
    }
}
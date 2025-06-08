package com.example.chocominto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chocominto.R;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.databinding.ItemVocabularyBinding;
import com.example.chocominto.utils.AudioHelper;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

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
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyBinding binding = ItemVocabularyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VocabularyAdapter.VocabularyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        Vocab item = vocabularyList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    class VocabularyViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabularyBinding binding;

        public VocabularyViewHolder(ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Vocab item) {
            binding.tvCharacter.setText(item.getCharacter());
            binding.tvReading.setText(item.getReading());
            binding.tvMeaning.setText(item.getMeaning());
            binding.tvPartOfSpeech.setText(item.getPartOfSpeech());
            binding.tvLevel.setText(String.format("Lvl %d", item.getLevel()));

            binding.btnPlayMale.setOnClickListener(v -> {
                String audioUrl = getMaleAudioUrl(item);
                if (audioUrl != null) {
                    AudioHelper.getInstance().playAudio(audioUrl);
                }
            });

            binding.btnPlayFemale.setOnClickListener(v -> {
                String audioUrl = getFemaleAudioUrl(item);
                if (audioUrl != null) {
                    AudioHelper.getInstance().playAudio(audioUrl);
                }
            });

            binding.btnPlayMale.setEnabled(getMaleAudioUrl(item) != null);
            binding.btnPlayFemale.setEnabled(getFemaleAudioUrl(item) != null);
        }

        private String getMaleAudioUrl(Vocab item) {
            return getAudioUrl(item, "male");
        }

        private String getFemaleAudioUrl(Vocab item) {
            return getAudioUrl(item, "female");
        }

        private String getAudioUrl(Vocab item, String gender) {
            if (item.getPronunciationAudios() == null) return null;

            for (Vocab.PronunciationAudio audio : item.getPronunciationAudios()) {
                if (gender.equals(audio.getGender()) && "audio/mpeg".equals(audio.getContentType())) {
                    return audio.getUrl();
                }
            }
            return null;
        }
    }
}
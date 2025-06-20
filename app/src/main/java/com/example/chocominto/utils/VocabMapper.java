package com.example.chocominto.utils;

import android.util.Log;

import com.example.chocominto.data.api.response.ContextSentencesItem;
import com.example.chocominto.data.api.response.Data;
import com.example.chocominto.data.api.response.MeaningsItem;
import com.example.chocominto.data.api.response.PronunciationAudiosItem;
import com.example.chocominto.data.api.response.ReadingsItem;
import com.example.chocominto.data.models.Vocab;
import com.example.chocominto.data.api.response.DataItem;
import com.example.chocominto.data.api.response.WaniKaniResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class VocabMapper {
    private static final String TAG = "VocabMapper";

    public static List<Vocab> mapFromResponse(WaniKaniResponse response) {
        List<Vocab> result = new ArrayList<>();

        if (response == null || response.getData() == null) {
            Log.e(TAG, "Response or response data is null");
            return result;
        }

        Log.d(TAG, "Raw JSON response: " + new Gson().toJson(response));

        for (DataItem dataItem : response.getData()) {
            if (dataItem == null) {
                Log.d(TAG, "Skipping null data item");
                continue;
            }

            try {
                int id = dataItem.getId();
                String character = dataItem.getData().getCharacters();
                int level = dataItem.getData().getLevel();

                Log.d(TAG, "Processing vocabulary: " + character + " (ID: " + id + ", Level: " + level + ")");

                String meaning = "";
                if (dataItem.getData().getMeanings() != null && !dataItem.getData().getMeanings().isEmpty()) {
                    for (int i = 0; i < dataItem.getData().getMeanings().size(); i++) {
                        if (dataItem.getData().getMeanings().get(i).isPrimary()) {
                            meaning = dataItem.getData().getMeanings().get(i).getMeaning();
                            break;
                        }
                    }
                    if (meaning.isEmpty() && !dataItem.getData().getMeanings().isEmpty()) {
                        meaning = dataItem.getData().getMeanings().get(0).getMeaning();
                    }
                }

                String reading = "";
                if (dataItem.getData().getReadings() != null && !dataItem.getData().getReadings().isEmpty()) {
                    for (int i = 0; i < dataItem.getData().getReadings().size(); i++) {
                        if (dataItem.getData().getReadings().get(i).isPrimary()) {
                            reading = dataItem.getData().getReadings().get(i).getReading();
                            break;
                        }
                    }
                    if (reading.isEmpty() && !dataItem.getData().getReadings().isEmpty()) {
                        reading = dataItem.getData().getReadings().get(0).getReading();
                    }
                }

                String partOfSpeech = "";
                if (dataItem.getData().getPartsOfSpeech() != null) {
                    partOfSpeech = String.join(", ", dataItem.getData().getPartsOfSpeech());
                } else {
                    Log.d(TAG, "No audio files available for " + character);
                }

                result.add(new Vocab(
                        id, character, meaning, reading, partOfSpeech, level));
            } catch (Exception e) {
                Log.e(TAG, "Error processing vocabulary item: " + e.getMessage(), e);
            }
        }
        Log.d(TAG, "Mapped " + result.size() + " vocabulary items");
        return result;
    }


    public static Vocab mapFromSingleSubject(DataItem response) {
        try {
            if (response == null || response.getData() == null) {
                Log.e(TAG, "Null response or data");
                return null;
            }

            int id = response.getId();

            Data data = response.getData();

            String character = data.getCharacters();

            StringBuilder meanings = new StringBuilder();
            if (data.getMeanings() != null) {
                for (MeaningsItem meaning : data.getMeanings()) {
                    if (meaning.isPrimary()) {
                        meanings.append(meaning.getMeaning());
                        meanings.append(", ");
                    }
                }
            }
            String meaning = meanings.length() > 0 ?
                    meanings.substring(0, meanings.length() - 2) : "";

            String reading = "";
            if (data.getReadings() != null && !data.getReadings().isEmpty()) {
                for (ReadingsItem r : data.getReadings()) {
                    if (r.isPrimary()) {
                        reading = r.getReading();
                        break;
                    }
                }
                if (reading.isEmpty()) {
                    reading = data.getReadings().get(0).getReading();
                }
            }

            String partOfSpeech = "";
            if (data.getPartsOfSpeech() != null && !data.getPartsOfSpeech().isEmpty()) {
                partOfSpeech = data.getPartsOfSpeech().get(0);
            }

            int level = data.getLevel();

            String meaningMnemonic = data.getMeaningMnemonic();
            String readingMnemonic = data.getReadingMnemonic();

            List<Vocab.ContextSentence> contextSentences = new ArrayList<>();
            if (data.getContextSentences() != null) {
                for (ContextSentencesItem sentence : data.getContextSentences()) {
                    contextSentences.add(new Vocab.ContextSentence(
                            sentence.getJa(),
                            sentence.getEn()
                    ));
                }
            }

            String audioUrl = "";

            if (data.getPronunciationAudios() != null && !data.getPronunciationAudios().isEmpty()) {
                PronunciationAudiosItem audio = data.getPronunciationAudios().get(0); // Ambil yang pertama
                audioUrl = audio.getUrl();
            }

            return new Vocab(
                    id,
                    character,
                    meaning,
                    reading,
                    partOfSpeech,
                    level,
                    meaningMnemonic,
                    readingMnemonic,
                    audioUrl,
                    contextSentences
            );

        } catch (Exception e) {
            Log.e(TAG, "Error mapping single subject: " + e.getMessage(), e);
            return null;
        }
    }
}
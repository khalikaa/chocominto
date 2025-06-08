package com.example.chocominto.utils;

import android.util.Log;

import com.example.chocominto.data.api.response.PronunciationAudiosItem;
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

//            if (!"vocabulary".equals(dataItem.getObject()) || dataItem.getData() == null) {
//                Log.d(TAG, "Skipping non-vocabulary item or null data: " + dataItem.getObject());
//                continue;
//            }

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
                }

                String meaningMnemonic = dataItem.getData().getMeaningMnemonic();
                String readingMnemonic = dataItem.getData().getReadingMnemonic();

                List<Vocab.ContextSentence> contextSentences = new ArrayList<>();
                if (dataItem.getData().getContextSentences() != null) {
                    for (int i = 0; i < dataItem.getData().getContextSentences().size(); i++) {
                        contextSentences.add(new Vocab.ContextSentence(
                                dataItem.getData().getContextSentences().get(i).getJa(),
                                dataItem.getData().getContextSentences().get(i).getEn()
                        ));
                    }
                }

                List<Vocab.PronunciationAudio> pronunciationAudios = new ArrayList<>();
                if (dataItem.getData().getPronunciationAudios() != null) {
                    int audioCount = dataItem.getData().getPronunciationAudios().size();
                    Log.d(TAG, "Found " + audioCount + " audio files for " + character);
//                    for (PronunciationAudiosItem audioRaw : dataItem.getData().getPronunciationAudios()) {
//                        if (audioRaw != null) {
//                            Log.d(TAG, "Raw audio: url=" + audioRaw.getUrl() +
//                                    ", type=" + audioRaw.getContentType() +
//                                    ", metadata=" + (audioRaw.getMetadata() != null));
//                        }
//                    }

                    for (int i = 0; i < dataItem.getData().getPronunciationAudios().size(); i++) {
                        PronunciationAudiosItem audioItem = dataItem.getData().getPronunciationAudios().get(i);
                        if (audioItem == null) {
                            Log.d(TAG, "Audio item is null, skipping");
                            continue;
                        }

                        String url = audioItem.getUrl();
                        String contentType = audioItem.getContentType();

                        if (audioItem.getMetadata() == null) {
                            Log.d(TAG, "Audio metadata is null, using default gender");
                            pronunciationAudios.add(new Vocab.PronunciationAudio(
                                    url, "unknown", contentType
                            ));
                            continue;
                        }

                        String gender = audioItem.getMetadata().getGender();

                        Log.d(TAG, String.format("Adding audio: URL=%s, Type=%s, Gender=%s", url, contentType, gender));

                        pronunciationAudios.add(new Vocab.PronunciationAudio(
                                url, gender, contentType
                        ));
                    }
                } else {
                    Log.d(TAG, "No audio files available for " + character);
                }

                result.add(new Vocab(
                        id, character, meaning, reading, partOfSpeech, level,
                        meaningMnemonic, readingMnemonic, contextSentences, pronunciationAudios
                ));

            } catch (Exception e) {
                Log.e(TAG, "Error processing vocabulary item: " + e.getMessage(), e);
                continue;
            }
        }

        Log.d(TAG, "Mapped " + result.size() + " vocabulary items");
        return result;
    }
}
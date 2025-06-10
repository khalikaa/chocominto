package com.example.chocominto.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Vocab implements Parcelable {
    private final int id;
    private final String character;
    private final String meaning;
    private final String reading;
    private final String partOfSpeech;
    private final int level;
    private final String meaningMnemonic;
    private final String readingMnemonic;
    private final String audioUrl;
    private final List<ContextSentence> contextSentences;

    public static class ContextSentence implements Parcelable {
        private final String ja;
        private final String en;

        public ContextSentence(String ja, String en) {
            this.ja = ja;
            this.en = en;
        }

        protected ContextSentence(Parcel in) {
            ja = in.readString();
            en = in.readString();
        }

        public static final Creator<ContextSentence> CREATOR = new Creator<ContextSentence>() {
            @Override
            public ContextSentence createFromParcel(Parcel in) {
                return new ContextSentence(in);
            }

            @Override
            public ContextSentence[] newArray(int size) {
                return new ContextSentence[size];
            }
        };

        public String getJa() { return ja; }
        public String getEn() { return en; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(ja);
            dest.writeString(en);
        }
    }

    public Vocab(int id, String character, String meaning, String reading,
                 String partOfSpeech, int level, String meaningMnemonic, String readingMnemonic, String audioUrl,
                 List<ContextSentence> contextSentences) {
        this.id = id;
        this.character = character;
        this.meaning = meaning;
        this.reading = reading;
        this.partOfSpeech = partOfSpeech;
        this.level = level;
        this.meaningMnemonic = meaningMnemonic;
        this.readingMnemonic = readingMnemonic;
        this.audioUrl = audioUrl;
        this.contextSentences = contextSentences;
    }

    public Vocab(int id, String character, String meaning, String reading,
                 String partOfSpeech, int level) {
        this.id = id;
        this.character = character;
        this.meaning = meaning;
        this.reading = reading;
        this.partOfSpeech = partOfSpeech;
        this.level = level;
        this.meaningMnemonic = "";
        this.readingMnemonic = "";
        this.audioUrl = "";
        this.contextSentences = List.of();
    }

    protected Vocab(Parcel in) {
        id = in.readInt();
        character = in.readString();
        meaning = in.readString();
        reading = in.readString();
        partOfSpeech = in.readString();
        level = in.readInt();
        meaningMnemonic = in.readString();
        readingMnemonic = in.readString();
        audioUrl = in.readString();
        contextSentences = new ArrayList<>();
        in.readTypedList(contextSentences, ContextSentence.CREATOR);
    }

    public static final Creator<Vocab> CREATOR = new Creator<Vocab>() {
        @Override
        public Vocab createFromParcel(Parcel in) {
            return new Vocab(in);
        }

        @Override
        public Vocab[] newArray(int size) {
            return new Vocab[size];
        }
    };

    public int getId() { return id; }
    public String getCharacter() { return character; }
    public String getMeaning() { return meaning; }
    public String getReading() { return reading; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public int getLevel() { return level; }
    public String getMeaningMnemonic() { return meaningMnemonic; }
    public String getReadingMnemonic() { return readingMnemonic; }
    public String getAudioUrl() { return audioUrl; }
    public List<ContextSentence> getContextSentences() { return contextSentences; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(character);
        dest.writeString(meaning);
        dest.writeString(reading);
        dest.writeString(partOfSpeech);
        dest.writeInt(level);
        dest.writeString(meaningMnemonic);
        dest.writeString(readingMnemonic);
        dest.writeString(audioUrl);
        dest.writeTypedList(contextSentences);
    }
}
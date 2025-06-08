package com.example.chocominto.data.models;

import java.util.List;

public class Vocab {
    private final int id;
    private final String character;
    private final String meaning;
    private final String reading;
    private final String partOfSpeech;
    private final int level;
    private final String meaningMnemonic;
    private final String readingMnemonic;
    private final List<ContextSentence> contextSentences;
    private final List<PronunciationAudio> pronunciationAudios;

    public static class ContextSentence {
        private final String ja;
        private final String en;

        public ContextSentence(String ja, String en) {
            this.ja = ja;
            this.en = en;
        }

        public String getJa() { return ja; }
        public String getEn() { return en; }
    }

    public static class PronunciationAudio {
        private final String url;
        private final String gender;
        private final String contentType;

        public PronunciationAudio(String url, String gender, String contentType) {
            this.url = url;
            this.gender = gender;
            this.contentType = contentType;
        }

        public String getUrl() { return url; }
        public String getGender() { return gender; }
        public String getContentType() { return contentType; }
    }

    public Vocab(int id, String character, String meaning, String reading,
                          String partOfSpeech, int level, String meaningMnemonic, String readingMnemonic,
                          List<ContextSentence> contextSentences, List<PronunciationAudio> pronunciationAudios) {
        this.id = id;
        this.character = character;
        this.meaning = meaning;
        this.reading = reading;
        this.partOfSpeech = partOfSpeech;
        this.level = level;
        this.meaningMnemonic = meaningMnemonic;
        this.readingMnemonic = readingMnemonic;
        this.contextSentences = contextSentences;
        this.pronunciationAudios = pronunciationAudios;
    }

    public int getId() { return id; }
    public String getCharacter() { return character; }
    public String getMeaning() { return meaning; }
    public String getReading() { return reading; }
    public String getPartOfSpeech() { return partOfSpeech; }
    public int getLevel() { return level; }
    public String getMeaningMnemonic() { return meaningMnemonic; }
    public String getReadingMnemonic() { return readingMnemonic; }
    public List<ContextSentence> getContextSentences() { return contextSentences; }
    public List<PronunciationAudio> getPronunciationAudios() { return pronunciationAudios; }
}
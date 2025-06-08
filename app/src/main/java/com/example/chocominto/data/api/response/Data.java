package com.example.chocominto.data.api.response;

import java.util.List;

public class Data{
	private List<String> partsOfSpeech;
	private int level;
	private List<ContextSentencesItem> contextSentences;
	private String createdAt;
	private List<MeaningsItem> meanings;
	private String characters;
	private List<ReadingsItem> readings;
	private List<PronunciationAudiosItem> pronunciationAudios;
	private String meaningMnemonic;
	private Object hiddenAt;
	private String readingMnemonic;
	private int spacedRepetitionSystemId;
	private int lessonPosition;
	private List<AuxiliaryMeaningsItem> auxiliaryMeanings;
	private List<Integer> componentSubjectIds;
	private String slug;
	private String documentUrl;

	public List<String> getPartsOfSpeech(){
		return partsOfSpeech;
	}

	public int getLevel(){
		return level;
	}

	public List<ContextSentencesItem> getContextSentences(){
		return contextSentences;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public List<MeaningsItem> getMeanings(){
		return meanings;
	}

	public String getCharacters(){
		return characters;
	}

	public List<ReadingsItem> getReadings(){
		return readings;
	}

	public List<PronunciationAudiosItem> getPronunciationAudios(){
		return pronunciationAudios;
	}

	public String getMeaningMnemonic(){
		return meaningMnemonic;
	}

	public Object getHiddenAt(){
		return hiddenAt;
	}

	public String getReadingMnemonic(){
		return readingMnemonic;
	}

	public int getSpacedRepetitionSystemId(){
		return spacedRepetitionSystemId;
	}

	public int getLessonPosition(){
		return lessonPosition;
	}

	public List<AuxiliaryMeaningsItem> getAuxiliaryMeanings(){
		return auxiliaryMeanings;
	}

	public List<Integer> getComponentSubjectIds(){
		return componentSubjectIds;
	}

	public String getSlug(){
		return slug;
	}

	public String getDocumentUrl(){
		return documentUrl;
	}
}
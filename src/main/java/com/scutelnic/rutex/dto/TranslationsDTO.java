package com.scutelnic.rutex.dto;

import java.util.Map;

public class TranslationsDTO {
	private String pageName;
	private String sourceLang;
	private String targetLang;
	private Map<String, String> translations;

	public TranslationsDTO() {}

	public TranslationsDTO(String pageName, String sourceLang, String targetLang, Map<String, String> translations) {
		this.pageName = pageName;
		this.sourceLang = sourceLang;
		this.targetLang = targetLang;
		this.translations = translations;
	}

	public String getPageName() { return pageName; }
	public void setPageName(String pageName) { this.pageName = pageName; }

	public String getSourceLang() { return sourceLang; }
	public void setSourceLang(String sourceLang) { this.sourceLang = sourceLang; }

	public String getTargetLang() { return targetLang; }
	public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

	public Map<String, String> getTranslations() { return translations; }
	public void setTranslations(Map<String, String> translations) { this.translations = translations; }
}



package hr.fer.oprpp1.jnotepadpp.local;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationProvider extends AbstractLocalizationProvider{
    private static LocalizationProvider instance;
    private String language;
    private ResourceBundle bundle;

    private LocalizationProvider() {setLanguage("en");}

    public static LocalizationProvider getInstance() {
        if (instance == null) {
            instance = new LocalizationProvider();
        }
        return instance;
    }

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }

    @Override
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        Locale locale = Locale.forLanguageTag(language);
        bundle = ResourceBundle.getBundle("hr.fer.oprpp1.hw08.jnotepadpp.local.translation", locale);
        fire();
    }
}

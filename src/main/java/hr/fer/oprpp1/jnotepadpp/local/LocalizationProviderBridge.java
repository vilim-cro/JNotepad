package hr.fer.oprpp1.jnotepadpp.local;

public class LocalizationProviderBridge extends AbstractLocalizationProvider{
    private boolean connected;
    private String language;
    private ILocalizationProvider provider;

    public LocalizationProviderBridge(ILocalizationProvider provider) {
        this.provider = provider;
        connect();
    }

    public void disconnect() {
        if (!connected) return;
        provider.removeLocalizationListener(this::fire);
        connected = false;
    }

    public void connect() {
        if (connected) return;
        provider.addLocalizationListener(this::fire);
        if (!provider.getLanguage().equals(language)) {
            language = provider.getLanguage();
            fire();
        }
        connected = true;
    }

    public String getString(String key) {
        return provider.getString(key);
    }

    @Override
    public String getLanguage() {
        return language;
    }
}

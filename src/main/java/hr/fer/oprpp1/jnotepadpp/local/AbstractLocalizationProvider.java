package hr.fer.oprpp1.jnotepadpp.local;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLocalizationProvider implements ILocalizationProvider {
    List<ILocalizationListener> listeners = new ArrayList<>();

    @Override
    public void addLocalizationListener(ILocalizationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeLocalizationListener(ILocalizationListener listener) {
        listeners.remove(listener);
    }

    public void fire() {
        for (ILocalizationListener listener : listeners) {
            listener.localizationChanged();
        }
    }
}

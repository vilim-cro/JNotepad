package hr.fer.oprpp1.jnotepadpp.local;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class LocalizableAction extends AbstractAction {
    public LocalizableAction(String key, ILocalizationProvider provider) {
        putValue(NAME, provider.getString(key));
        provider.addLocalizationListener(() -> putValue(NAME, provider.getString(key)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {}
}

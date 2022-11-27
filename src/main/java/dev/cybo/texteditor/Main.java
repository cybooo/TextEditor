package dev.cybo.texteditor;

import com.formdev.flatlaf.FlatDarkLaf;
import dev.cybo.texteditor.editor.Editor;
import dev.cybo.texteditor.extensions.ExtensionLoader;

import javax.swing.*;

public class Main {

    private static Editor editor;
    private static ExtensionLoader extensionLoader;

    public static void main(String[] args) {

        // Tmavý režim
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        editor = new Editor();
        extensionLoader = new ExtensionLoader();

        extensionLoader.loadExtensions();

    }

}

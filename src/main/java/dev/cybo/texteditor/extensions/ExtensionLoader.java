package dev.cybo.texteditor.extensions;

import dev.cybo.texteditor.extensions.interfaces.Extension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtensionLoader {

    private List<Extension> extensionList;

    public ExtensionLoader() {
        this.extensionList = new ArrayList<>();
    }

    public void loadExtensions() {
        System.out.println(getExecutionPath());
    }

    private static String getExecutionPath(){
        File file = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
        return file.getAbsolutePath();
    }

}

package GUI;

import javax.swing.*;

public interface Page extends GUICreator {
    JMenuBar createMenuBar();
    JMenuBar getMenuBar();
}

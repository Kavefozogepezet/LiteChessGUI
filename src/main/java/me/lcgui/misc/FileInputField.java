package me.lcgui.misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.FieldPosition;

public class FileInputField extends JPanel {
    public static final String FOLDER_SYMBOL = "\uD83D\uDCC1";

    JTextField field = new JTextField();
    JButton button = new JButton(FOLDER_SYMBOL);

    JFileChooser fileChooser = new JFileChooser();

    File selected = new File("");

    public FileInputField() {
        var layout = new GridBagLayout();
        var constraint = new GridBagConstraints();

        constraint.fill = GridBagConstraints.BOTH;
        constraint.weightx = 1.0f;
        layout.setConstraints(field, constraint);

        constraint.weightx = 0.0f;
        layout.setConstraints(button, constraint);

        setLayout(layout);
        add(field);
        add(button);

        button.addActionListener(this::openFileChooser);
        field.addActionListener((l) -> { selected = new File(field.getText()); });
    }

    public File getSelectedFile() { return selected; }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public JTextField getTextField() {
        return field;
    }

    private void openFileChooser(ActionEvent l) {
        int option = fileChooser.showOpenDialog(null);
        if(option == JFileChooser.APPROVE_OPTION) {
            selected = fileChooser.getSelectedFile();
            field.setText(selected.getPath());
        }
    }
}

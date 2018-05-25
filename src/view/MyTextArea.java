package view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextArea;

public class MyTextArea extends JTextArea {

	public MyTextArea() {
		setBackground(Color.WHITE);
	}

	public void setting(int size) {
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size - 1));
	}
}

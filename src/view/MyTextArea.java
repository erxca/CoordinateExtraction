package view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class MyTextArea extends JTextArea {

	public MyTextArea() {
		setBackground(Color.WHITE);
		setBorder(new LineBorder(new Color(0, 0, 200)));
	}

	public void setting(int size) {
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size - 1));
	}
}

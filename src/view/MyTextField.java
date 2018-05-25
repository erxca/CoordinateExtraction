package view;

import java.awt.Font;

import javax.swing.JTextField;

public class MyTextField extends JTextField {

	public MyTextField(int c, int size) {

		super(c);
		setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));

	}

}

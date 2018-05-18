package model;

import java.awt.EventQueue;

public class CoordinateExtraction {
	static DropFile window;

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					window = new DropFile();
					window.frame.setVisible(true);

				} catch (Exception e) {

					e.printStackTrace();

				}
			}
		});

	}
}

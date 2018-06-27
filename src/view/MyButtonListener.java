package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MyButtonListener implements ActionListener {
	JFrame frame;
	MyTextField tf;

	public MyButtonListener(JFrame frame, MyTextField tf) {

		this.frame = frame;
		this.tf = tf;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser filechooser = new JFileChooser(".\\data\\") {
			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				String fileName = f.getName();

				if (!fileName.endsWith(".csv")) {

					StringBuffer sb = new StringBuffer(".\\data\\");
					sb.append(fileName);
					sb.append(".csv");
					fileName = sb.toString();
					f = new File(fileName);
				}

				if (f.exists() && getDialogType() == SAVE_DIALOG) {
					String m;
					try {
						m = String.format("<html>%s はすでに存在しています。<br>上書きしてもよろしいですか？", f.getCanonicalPath());

						int rv = JOptionPane.showConfirmDialog(this, m, "Save As", JOptionPane.YES_NO_OPTION);
						if (rv != JOptionPane.YES_OPTION) {
							return;
						}
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
				super.approveSelection();
			}
		};

		int selected = filechooser.showSaveDialog(frame);

		if (selected == JFileChooser.APPROVE_OPTION) {

			File file = filechooser.getSelectedFile();
			tf.setText(checkExtension(file.getPath()));
			// tf.setText(file.getName());

		} else if (selected == JFileChooser.ERROR_OPTION) {

			tf.setText("エラー又は取消しがありました");

		}
	}

	private String checkExtension(String fileName) {

		if (!fileName.endsWith(".csv")) {

			StringBuffer sb = new StringBuffer(fileName);
			sb.append(".csv");
			fileName = sb.toString();
		}

		return fileName;

	}

}

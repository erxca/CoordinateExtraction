package model;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CoordinateExtraction {
	static DropFile window;
	private File newFile;
	private String newFileName;
	private ArrayList<String> coordinateList = new ArrayList<String>();
	private static String DIR = ".\\data\\";

	public CoordinateExtraction(File newFile, String newFileName) {

		this.newFile = newFile;
		this.newFileName = newFileName;

	}

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

	public void inputFile() {

		try {

			BufferedReader txt = new BufferedReader(new InputStreamReader(new FileInputStream(newFile), "MS932"));

			window.la.setText("");
			checkText(txt);

			txt.close();

		} catch (IOException ex) {

			// 例外発生時処理
			ex.printStackTrace();

		}

	}

	private String checkObject() {

		String obj = window.wordTf.getText();
		StringBuffer sbObj = new StringBuffer(obj);
		sbObj.append(":");

		return sbObj.toString();

	}

	private void checkText(BufferedReader txt) throws IOException {
		String line;
		String obj = window.wordTf.getText();

		int start = obj.length() + 1;

		while ((line = txt.readLine()) != null) {

			if (line.startsWith(obj)) {
				int idx = line.indexOf(":");
				coordinateList.add(line.substring(idx + 1));
				window.la.append(line.substring(idx + 1) + "\n");
				window.la.setCaretPosition(window.la.getText().length());
			}

		}

		// ファイル出力
		writeFile();
	}

	private void writeFile() throws FileNotFoundException {

		// 出力ファイル生成
		StringBuffer newFilePath = new StringBuffer();
		newFilePath.append(DIR);

		StringBuffer newFileNameCSV = new StringBuffer();
		newFileNameCSV.append(window.outputNameTf.getText());
		newFileNameCSV.append(".csv");
		newFilePath.append(newFileNameCSV);

		window.ra.append("> エクスポートしたファイル：" + newFileNameCSV.toString() + "\n");
		window.ra.append("データ数：" + coordinateList.size() + " 個\n\n");
		window.ra.setCaretPosition(window.ra.getText().length());

		try {

			FileOutputStream fos = new FileOutputStream(newFilePath.toString());
			OutputStreamWriter osw = new OutputStreamWriter(fos, "MS932");
			BufferedWriter bw = new BufferedWriter(osw);

			for (String coord : coordinateList) {
				bw.write(coord);
				bw.write("\n");
			}

			bw.close();

		} catch (UnsupportedEncodingException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

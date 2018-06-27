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

import view.DropFile;

public class CoordinateExtraction {
	static DropFile window;
	private File newFile;
	private ArrayList<String> coordinateList = new ArrayList<String>();
	private static String DIR = ".\\data\\";
	private BufferedReader txt;

	public CoordinateExtraction(File newFile) {

		this.newFile = newFile;

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

			txt = new BufferedReader(new InputStreamReader(new FileInputStream(newFile), "MS932"));

			window.la.setText("");
			checkCondition();

			txt.close();

		} catch (IOException ex) {

			// 例外発生時処理
			ex.printStackTrace();

		}

	}

	private void checkCondition() throws IOException {

		if (window.cb.isSelected()) {
			checkSurfacePCData();
		} else {
			checkWord();

		}

		// ファイル出力
		writeFile();

	}

	private void checkSurfacePCData() throws IOException {
		String line;
		int num = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer tmpsb = new StringBuffer();

		while ((line = txt.readLine()) != null) {

			if (line.indexOf("DocTitle") > -1) {

				sb = new StringBuffer();
				int start = line.indexOf("[");
				int end = line.indexOf("]");

				sb.append(line.substring(start + 1, end));
				sb.append(",");
				sb.append(line.substring(line.lastIndexOf(" ")));
				coordinateList.add(sb.toString());

				num = 0;

			} else if (line.indexOf("Trace: Valid =") > -1) {

				sb = new StringBuffer();
				num++;
				sb.append(" ");
				sb.append(",");
				sb.append(num);
				sb.append(",");

				sb.append(convertPercent(line));

			} else if (line.indexOf("Trace: Inner =") > -1) {

				tmpsb = convertPercent(line);

			} else if (line.indexOf("Trace: Valid Test") > -1) {

				sb.append(line.substring(line.lastIndexOf(" ")));
				sb.append(",");
				sb.append(tmpsb);

			} else if (line.indexOf("Trace: Inner Test") > -1) {

				sb.append(line.substring(line.lastIndexOf(" ")));
				coordinateList.add(sb.toString());

			}

			window.la.append(line + "\n");
			window.la.setCaretPosition(window.la.getText().length());
		}

	}

	private StringBuffer convertPercent(String line) {
		StringBuffer sb = new StringBuffer();

		Double d = Double.parseDouble(line.substring(line.lastIndexOf(" ")));
		Double percentD = Math.floor(d * 1000) / 10;

		sb.append(percentD.toString());
		sb.append(",");
		sb.append(" ");
		sb.append(",");

		return sb;

	}

	private void checkWord() throws IOException {
		String line;
		String obj = window.wordTf.getText();

		while ((line = txt.readLine()) != null) {

			if (line.startsWith(obj)) {
				int idx = line.indexOf(":");
				coordinateList.add(line.substring(idx + 1));
				window.la.append(line.substring(idx + 1) + "\n");
				window.la.setCaretPosition(window.la.getText().length());
			}

		}

	}

	private void writeFile() throws FileNotFoundException {
		String tfText = window.outputNameTf.getText();

		// 出力ファイル生成
		StringBuffer newFilePath = new StringBuffer();

		if (tfText.indexOf("\\") < 0) {
			newFilePath.append(DIR);
		}

		newFilePath.append(tfText);

		if (newFilePath.toString().indexOf(".csv") < 0) {
			newFilePath.append(".csv");
		}
		// newFilePath.append(newFileNameCSV);

		window.ra.append("> エクスポートしたファイル：" + newFilePath.toString() + "\n");
		window.ra.append("出力行数：" + coordinateList.size() + " 個\n\n");
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

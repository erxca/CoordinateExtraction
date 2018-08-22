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
	private ProcessSurfacePCLog spc;
	private boolean isSurfacePc;

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

	// 抽出条件チェック。Surface-PCのログなのかどうかをチェックし、適した抽出方法を行う
	private void checkCondition() throws IOException {

		if (isSurfacePc = window.cb.isSelected()) {

			spc = new ProcessSurfacePCLog(window, txt);
			spc.checkSurfacePCData();

		} else {

			checkLog();

		}

		// ファイル出力
		writeFile();

	}

	private void checkLog() throws IOException {
		String line;
		String object = "!!!!!";
		String word = window.wordTf.getText();
		StringBuffer sb = new StringBuffer();

		if (word.equals("Center") || word.equals("Normal") || word.equals("Radius")) {
			object = "WorkHole";
		} else if (word.equals("Dimension")) {
			object = "Work3D_Dim";
		}

		while ((line = txt.readLine()) != null) {

			if (line.indexOf(object) > -1) {
				// sb = new StringBuffer();

				int start = line.indexOf("[") + 1;
				int end = line.indexOf("]");
				sb.append(line.substring(start, end));
				sb.append(",");

			} else if (line.startsWith(word)) {
				int idx = line.indexOf(":");
				sb.append(line.substring(idx + 1));
				coordinateList.add(sb.toString());

				window.la.append(sb.toString() + "\n");
				window.la.setCaretPosition(window.la.getText().length());

				sb = new StringBuffer();
			}

		}

	}

	private void writeFile() throws FileNotFoundException {
		ArrayList<String> resultList;

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

		resultList = isSurfacePc ? spc.outputList : coordinateList;

		window.ra.append("> エクスポートしたファイル：" + newFilePath.toString() + "\n");
		window.ra.append("出力行数：" + resultList.size() + " 行\n\n");
		window.ra.setCaretPosition(window.ra.getText().length());

		try {

			FileOutputStream fos = new FileOutputStream(newFilePath.toString());
			OutputStreamWriter osw = new OutputStreamWriter(fos, "MS932");
			BufferedWriter bw = new BufferedWriter(osw);

			for (String coord : resultList) {
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

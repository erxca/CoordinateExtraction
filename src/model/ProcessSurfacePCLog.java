package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import view.DropFile;

public class ProcessSurfacePCLog {
	DropFile window;
	BufferedReader txt;
	ArrayList<String> coordinateList = new ArrayList<String>();
	private ArrayList<String> fileName = new ArrayList<String>();
	// private ArrayList<Integer> dataNum = new ArrayList<Integer>();
	boolean isRoop = false;

	// private int fileNum = -1; // amlファイルの番号
	private int lineNum = 0; //
	private int titleNum = 0; // 時刻、ファイル名を書く行番号
	private int currentfile = 0; // 現在見ているデータのファイル番号

	public ProcessSurfacePCLog(DropFile window, BufferedReader txt) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.window = window;
		this.txt = txt;
	}

	public void checkSurfacePCData() {
		try {
			makeLine();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void makeLine() throws IOException {
		String line;
		int num = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer tmpsb = new StringBuffer();

		while ((line = txt.readLine()) != null) {

			if (line.indexOf("DocTitle") > -1) {

				// if (dataNum.get(fileNum) == null) {
				// dataNum.set(fileNum, num);
				// }

				sb = new StringBuffer();
				int start = line.indexOf("[");
				int end = line.indexOf("]");

				sb.append(line.substring(start + 1, end));
				sb.append(",");

				String amlName = line.substring(line.lastIndexOf(" "));
				checkAmlFileName(amlName);

				sb.append(amlName);
				sb.append(", , , , , , ,");

				// coordinateList.add(sb.toString());
				addList(sb, titleNum);

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

				// coordinateList.add(sb.toString());
				addList(sb, titleNum + num);

			}

			window.la.append(line + "\n");
			window.la.setCaretPosition(window.la.getText().length());

			lineNum++;
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

	private void checkAmlFileName(String amlName) {
		boolean isExist = false;
		System.out.println("amlName: " + amlName);

		if (fileName.size() != 0) {
			for (String name : fileName) {

				if (name.equals(amlName)) {
					System.out.println(amlName);
					isExist = true;
					currentfile = fileName.indexOf(name);
					System.out.println(currentfile);

					isRoop = true;

					break;
				}
			}

			if (!isRoop && !isExist) {
				fileName.add(++currentfile, amlName);
			}

			if (isRoop && !isExist) {
				fileName = new ArrayList<String>();
				fileName.add(0, amlName);
				isRoop = false;
			}
		} else {
			fileName.add(0, amlName);
		}

		// fileNum++;
	}

	private void addList(StringBuffer sb, int line) {
		StringBuffer linedata = new StringBuffer();

		System.out.println(sb.toString() + " " + line + " " + currentfile);

		if (currentfile == 0) {

			coordinateList.add(sb.toString());

			if (line == titleNum) {

				titleNum = coordinateList.size() - 1;

			}

		} else {

			if (coordinateList.size() > line) {

				linedata.append(coordinateList.get(line));
				System.out.println(coordinateList.get(line));
				linedata.append(", ,");
				linedata.append(sb.toString());
				coordinateList.set(line, sb.toString());
				System.out.println(linedata.toString());

			} else {

				for (int i = 1; i < currentfile; i++) {

					linedata.append(", , , , , , , , ,");

				}

				linedata.append(sb.toString());
				coordinateList.add(sb.toString());

			}

		}

	}

}

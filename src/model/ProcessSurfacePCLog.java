package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import view.DropFile;

public class ProcessSurfacePCLog {
	DropFile window;
	BufferedReader txt;
	ArrayList<String> coordinateList = new ArrayList<String>();
	ArrayList<String> outputList = new ArrayList<String>();
	private ArrayList<String> tempList = new ArrayList<String>();
	private ArrayList<String> fileName = new ArrayList<String>();
	boolean isRoop = false;

	private int fileNum = -1; // amlファイルの番号
	private int roopNum = 0;

	public ProcessSurfacePCLog(DropFile window, BufferedReader txt) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.window = window;
		this.txt = txt;
	}

	public void checkSurfacePCData() {
		try {
			extractionData();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	// ログファイル内から必要なデータを抽出してリストへ入れる
	public void extractionData() throws IOException {
		String line;
		int num = 0;
		StringBuffer sb = new StringBuffer();
		StringBuffer inner = new StringBuffer();
		String amlName = "";

		while ((line = txt.readLine()) != null) {

			if (line.indexOf("DocTitle") > -1) {

				sb = new StringBuffer();
				int start = line.indexOf("[");
				int end = line.indexOf("]");

				sb.append(line.substring(start + 1, end));
				sb.append(",");

				String camlName = line.substring(line.lastIndexOf(" "));

				amlName = camlName;
				checkAmlFileName(amlName);

				sb.append(amlName);
				sb.append(", , , , , ,");

				if (coordinateList.size() != 0) {
					coordinateList.add("current:roop[" + roopNum + "] data[" + num + "] next:file[" + fileNum + "] num["
							+ fileName.size() + "]");
				}

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

				inner = convertPercent(line);

			} else if (line.indexOf("Trace: Valid Test") > -1) {

				sb.append(line.substring(line.lastIndexOf(" ")));
				sb.append(",");
				sb.append(inner);

			} else if (line.indexOf("Trace: Inner Test") > -1) {

				sb.append(line.substring(line.lastIndexOf(" ")));

				coordinateList.add(sb.toString());

			}

			window.la.append(line + "\n");
			window.la.setCaretPosition(window.la.getText().length());

		}

		coordinateList.add("current:roop[" + roopNum + "] data[" + num + "] next:file[0] num[");

		for (String s : coordinateList) {
			System.out.println(s);
		}
		makeOutputList();

	}

	// 小数表記をパーセント表記に直す
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

	// ファイル名がすでにリスト内にあるかをチェックする
	// リスト内になければ新しいループの始まりとみなす
	private void checkAmlFileName(String amlName) {
		boolean isExist = false;

		if (fileName.size() != 0) {
			for (String name : fileName) {

				if (name.equals(amlName)) {
					isExist = true;
					isRoop = true;

					break;
				}
			}

			if (!isRoop && !isExist) {
				fileName.add(amlName);
			}

			if (isRoop && !isExist) {
				fileName = new ArrayList<String>();
				fileName.add(0, amlName);
				isRoop = false;
				roopNum++;
			}
			fileNum = fileName.indexOf(amlName);
		} else {
			fileName.add(0, amlName);
			fileNum = 0;
		}

	}

	private void makeOutputList() {
		int startIdx = 0;
		int endIdx = 0;

		tempList.add("start");
		for (int i = 0; i < coordinateList.size(); i++) {
			if (coordinateList.get(i).indexOf("file[0]") != -1) {
				endIdx = i;
				String cl = coordinateList.get(i);

				int p1 = 0;
				int p2 = 0;
				int max = 0;
				for (int j = startIdx; j <= endIdx; j++) {
					cl = coordinateList.get(j);
					System.out.println("cl: " + cl);

					int si;
					if ((si = cl.indexOf("data[")) != -1) {
						p1 = p2;
						p2 = Integer.parseInt(cl.substring(si + 5, cl.indexOf("] next")));
						max = Math.max(p1, p2);
					}
				}

				int resultLine = 0;
				for (int j = startIdx; j <= endIdx; j++) {

					resultLine = insertBlankAndInfo(coordinateList.get(j), max, resultLine);

				}

				startIdx = endIdx + 1;
			}
		}

		for (String s : tempList) {
			System.out.println(s);
		}

		rearrange();

	}

	private int insertBlankAndInfo(String cl, int max, int resultLine) {

		if (cl.startsWith(" ,")) {
			tempList.add(cl);
			resultLine++;

		} else if (cl.startsWith("current:")) {
			for (int k = resultLine; k < max; k++) {
				tempList.add(" , , , , , , , ");
			}
			resultLine = 0;

			if (cl.substring(cl.indexOf("file[") + 5, cl.indexOf("] num")).equals("0")) {
				tempList.add("start");

			} else {
				tempList.add("change");
			}

		} else {
			tempList.add(cl);
		}

		return resultLine;

	}

	private void rearrange() {
		int opIdx = 0;
		int tempNum = 0;
		boolean isStart = false;

		for (String s : tempList) {

			if (s.equals("start")) {
				opIdx = outputList.size();
				isStart = true;
				tempNum = 0;

			} else if (s.equals("change")) {
				isStart = false;
				tempNum = 0;

			} else {
				if (isStart) {
					outputList.add(s);

				} else {
					StringBuffer sb = new StringBuffer();
					sb.append(outputList.get(opIdx + tempNum));
					sb.append(",");
					sb.append(s);
					outputList.set(opIdx + tempNum, sb.toString());
				}

				tempNum++;
			}
		}

		for (String s : outputList) {
			System.out.println(s);
		}
	}

}

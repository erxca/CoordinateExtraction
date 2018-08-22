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
	boolean isRoop = false; // ループの2週目以降のときtrue

	private int fileNum = -1; // amlファイルの番号
	private int roopNum = 0; // 処理に使ってないけど一応残しておく

	public ProcessSurfacePCLog(DropFile window, BufferedReader txt) {

		this.window = window;
		this.txt = txt;
	}

	public void checkSurfacePCData() {
		try {
			extractionData();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	// ログファイル内から必要なデータを抽出してリストへ入れる
	public void extractionData() throws IOException {
		String line;
		int num = 0;
		StringBuffer sb = new StringBuffer(); // 1回分のログを1行にまとめたもの
		StringBuffer inner = new StringBuffer();
		String amlName = "";

		while ((line = txt.readLine()) != null) {

			if (line.indexOf("DocTitle") > -1) { // ファイル名の行

				sb = new StringBuffer();

				// 日時取得
				int start = line.indexOf("[") + 1;
				int end = line.indexOf("]");
				sb.append(line.substring(start, end));
				sb.append(",");

				amlName = line.substring(line.lastIndexOf(" "));
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

		// ファイル名のリストにすでに中身がある
		if (fileName.size() != 0) {
			for (String name : fileName) {

				// リスト内にすでにあるファイル名のとき
				if (name.equals(amlName)) {
					isExist = true;
					isRoop = true;

					break;
				}
			}

			// まだループ2週目に入っていなくて、リスト内にないファイル名のとき
			if (!isRoop && !isExist) {
				fileName.add(amlName);
			}

			// ループ2週目以降で、リスト内にないファイル名のとき
			// ＝新しいループが始まるのでいろいろ初期化してから登録
			if (isRoop && !isExist) {
				fileName = new ArrayList<String>();
				fileName.add(0, amlName);
				isRoop = false;
				roopNum++;
			}
			fileNum = fileName.indexOf(amlName);

			// ファイル名のリストに中身がない
			// （一番最初のデータなのでスッと登録）
		} else {
			fileName.add(0, amlName);
			fileNum = 0;
		}

	}

	private void makeOutputList() {
		int startIdx = 0; // ループ内の1つ目のデータ
		int endIdx = 0; // ループ内の最後のデータ

		tempList.add("start");

		for (int i = 0; i < coordinateList.size(); i++) {
			if (coordinateList.get(i).indexOf("file[0]") != -1) {
				endIdx = i;
				String listData = coordinateList.get(i);

				int p1 = 0;
				// int p2 = 0;
				int max = 0;
				for (int j = startIdx; j <= endIdx; j++) {
					listData = coordinateList.get(j);
					// System.out.println("cl: " + listData);

					int si;
					if ((si = listData.indexOf("data[")) != -1) {
						// p1 = max;
						p1 = Integer.parseInt(listData.substring(si + 5, listData.indexOf("] next")));
						max = Math.max(p1, max);
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

		if (cl.startsWith(" ,")) { // データ行
			tempList.add(cl);
			resultLine++;

		} else if (cl.startsWith("current:")) { // 付加情報行
			for (int k = resultLine; k < max; k++) { // データ数が足りていないところに空行を足す
				tempList.add(" , , , , , , , ");
			}
			resultLine = 0;

			if (cl.substring(cl.indexOf("file[") + 5, cl.indexOf("] num")).equals("0")) {
				tempList.add("start");

			} else {
				tempList.add("change");
			}

		} else { // 日時とファイル名の行
			tempList.add(cl);
		}

		return resultLine;

	}

	private void rearrange() {
		int opIdx = 0;
		int diffLine = 0;
		boolean isStart = false;

		for (String s : tempList) {

			if (s.equals("start")) { // ループの先頭
				opIdx = outputList.size();
				isStart = true;
				diffLine = 0;

			} else if (s.equals("change")) { // ループ内でのファイルチェンジ
				isStart = false;
				diffLine = 0;

			} else {
				if (isStart) {
					outputList.add(s);

				} else {
					StringBuffer sb = new StringBuffer();
					sb.append(outputList.get(opIdx + diffLine));
					sb.append(",");
					sb.append(s);
					outputList.set(opIdx + diffLine, sb.toString());
				}

				diffLine++;
			}
		}

		for (String s : outputList) {
			System.out.println(s);
		}
	}

}

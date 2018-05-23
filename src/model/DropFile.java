package model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

public class DropFile {

	// CardConversionTool cct = new CardConversionTool();
	public JFrame frame;
	public MyTextArea ra, la;

	private int fWidth, fHeight;
	private int xPos; // 各コンポーネントのxの位置
	private int cWidth, blank, yPos;
	private int raSize = 0;

	MyThread thread;
	public JComboBox<String> combo;

	private final int FRAME_WIDTH_RATIO = 40; // フレームの画面に対する幅の割合
	private final int WIDTH_RATIO = 94; // 各コンポーネントのフレームに対する幅の割合
	private final int BRANK_RATIO = 3; // 各コンポーネントの間の余白の割合
	private final int X_RATIO = 3; // 横の余白の割合
	private final int LBL_RATIO = 7; // フレームに対する説明ラベルの割合
	private final int DATE_RATIO = 13;
	private final int DDLBL_RATIO = 15;
	private final int RESULT_RATIO = 25;
	private final int LOG_RATIO = 21;
	private final int NUM = 2 * 1 + 3 * 1; // 入力部があるラベルは3、ないラベルは2の割合で分割

	public DropFile() {

		// 画面サイズの取得
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screenSize.width;
		int h = screenSize.height;

		// フレームのサイズ、コンポーネントの位置・サイズ設定
		fWidth = w * FRAME_WIDTH_RATIO / 100;
		fHeight = h * 1 / 2;
		xPos = X_RATIO * fWidth / 100;
		cWidth = WIDTH_RATIO * fWidth / 100;
		blank = fHeight * BRANK_RATIO / 100;
		yPos = blank;

		// フレームの生成
		frame = new JFrame("オブジェクトデータ抽出ツール");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.getContentPane().setPreferredSize(new Dimension(fWidth, fHeight));
		frame.pack();

		// 説明ラベルの生成
		makeDescriptionLabel();

		// 日付設定部分生成
		makeConditionPart();

		// ファイルをドロップさせるためのラベル生成
		makeDdLabel();

		// 結果などを載せるためのテキストエリア生成
		makeResultArea();

		// 途中経過表示用テキストエリア
		makeLogArea();

	}

	// 説明ラベルの生成
	private void makeDescriptionLabel() {

		JLabel lbl = new JLabel("ログファイル（.txt）をインポートしてください。");
		int lblHeight = fHeight * LBL_RATIO / 100;
		lbl.setBounds(xPos, yPos, cWidth, lblHeight);

		lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, calcCharSize(lbl, lblHeight) - 1));
		frame.getContentPane().add(lbl);
		yPos += lblHeight + blank;

	}

	private void makeConditionPart() {

		int datePh = fHeight * DATE_RATIO / 100;
		int ph = datePh / NUM;

		JLabel selectLbl = new JLabel("オブジェクトを選択してください。");
		int lblHeight = ph * 2;
		selectLbl.setBounds(xPos, yPos, cWidth, ph * 2);

		selectLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, calcCharSize(selectLbl, lblHeight) - 1));
		frame.getContentPane().add(selectLbl);
		yPos += lblHeight + blank / 2;

		String[] elements = { "Hole", "Line", "Sample", "Sample2" };
		combo = new JComboBox<String>(elements);
		int size = calcCharSize(combo, ph * 2);
		combo.setBounds(xPos * 2, yPos, size * 10, ph * 2);
		combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size - 1));
		combo.setSelectedIndex(-1);

		frame.getContentPane().add(combo);
		yPos += lblHeight + blank * 3 / 2;
	}

	// ファイルをドロップさせるためのラベル生成
	private void makeDdLabel() {

		JLabel ddlbl = new JLabel("ここにファイルをドロップ");
		int ddlblHeight = fHeight * DDLBL_RATIO / 100;
		ddlbl.setBounds(xPos, yPos, cWidth, ddlblHeight);
		ddlbl.setBackground(Color.WHITE);
		ddlbl.setOpaque(true);
		ddlbl.setHorizontalAlignment(JLabel.CENTER);

		ddlbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, calcCharSize(ddlbl, ddlblHeight / 2) - 1));
		frame.getContentPane().add(ddlbl);
		yPos += ddlblHeight + blank;

		// ドロップ操作を有効にする
		ddlbl.setTransferHandler(new DropFileHandler());

	}

	// 結果などを載せるためのテキストエリア生成
	private void makeResultArea() {

		ra = new MyTextArea();

		JScrollPane jsp = new JScrollPane();
		int jspHeight = fHeight * RESULT_RATIO / 100;
		jsp.setBounds(xPos, yPos, cWidth, jspHeight);

		FontMetrics raFm;
		do {
			raSize++;
			raFm = ra.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, raSize));
		} while (raFm.getAscent() + raFm.getDescent() < (jspHeight / 7));

		ra.setting(raSize);
		jsp.setViewportView(ra);
		frame.getContentPane().add(jsp);
		yPos += jspHeight + blank;
	}

	// 途中経過表示用テキストエリア
	private void makeLogArea() {
		la = new MyTextArea();
		la.setting(raSize);

		la.setFocusable(false);

		JScrollPane jsp2 = new JScrollPane();
		int jsp2Height = fHeight * LOG_RATIO / 100;
		jsp2.setBounds(xPos, yPos, cWidth, jsp2Height);
		jsp2.setViewportView(la);
		frame.getContentPane().add(jsp2);
	}

	// 文字サイズ計算
	private int calcCharSize(Component comp, int compHeight) {

		int size = 0;
		FontMetrics fm;
		do {
			size++;
			fm = comp.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, size));
		} while (fm.getAscent() + fm.getDescent() < (compHeight));
		return size;

	}

	// ドロップ操作の処理を行うクラス
	private class DropFileHandler extends TransferHandler {

		// ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDrop()) {
				// ドロップ操作でない場合は受け取らない
				return false;
			}

			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				// ドロップされたのがファイルでない場合は受け取らない
				return false;
			}

			return true;
		}

		// ドロップされたファイルを受け取る
		@Override
		public boolean importData(TransferSupport support) {
			// 受け取っていいものか確認する
			if (!canImport(support)) {
				return false;
			}

			// ドロップ処理
			Transferable t = support.getTransferable();

			try {
				// ファイルを受け取る
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

				if (files.size() != 1) {
					ra.append("一度にインポートできるファイルは一つだけです。\n");
					return false;
				}

				String inputFileName = files.get(0).getName();
				Date date = new Date();
				SimpleDateFormat textAreaFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
				ra.append(textAreaFormat.format(date) + "\n");
				ra.append("> インポートしたファイル：" + inputFileName + "\n");

				StringBuffer newFileName = new StringBuffer(inputFileName.substring(0, inputFileName.indexOf(".")));
				newFileName.append("_" + fileNameFormat.format(date));

				thread = new MyThread(files.get(0), newFileName.toString());
				thread.start();

			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	private class MyThread extends Thread {
		File newFile;
		String newFileName;

		public MyThread(File newFile, String newFileName) {
			this.newFile = newFile;
			this.newFileName = newFileName;
		}

		public void run() {

			CoordinateExtraction ce = new CoordinateExtraction(newFile, newFileName);
			ce.inputFile();

		}

	}
}

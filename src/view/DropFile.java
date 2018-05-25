package view;

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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import model.CoordinateExtraction;

public class DropFile {

	public JFrame frame;
	public MyTextArea ra, la;
	public MyTextField wordTf, outputNameTf;

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
	private final int CONDITION_RATIO = 13;
	private final int DDLBL_RATIO = 15;
	private final int RESULT_RATIO = 25;
	private final int LOG_RATIO = 21;
	private final int NUM = 3 * 2; // 入力部があるラベルは3、ないラベルは2の割合で分割

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

		// 条件指定部分生成
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

	// 条件指定部分生成
	private void makeConditionPart() {

		int ph = fHeight * CONDITION_RATIO / 100 / NUM;
		int lblHeight = ph * 2;
		int pnlHeight = ph * 3;

		JPanel wordPnl = new JPanel();
		wordPnl.setBounds(xPos, yPos, cWidth, pnlHeight);
		frame.getContentPane().add(wordPnl);
		yPos += pnlHeight;

		int size = calcCharSize(wordPnl, lblHeight);

		JLabel wordLbl = new JLabel("探索ワード　　：");
		wordLbl.setBounds(xPos, yPos, cWidth, ph * 2);
		wordLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, calcCharSize(wordLbl, lblHeight) - 1));
		wordPnl.add(wordLbl);

		wordTf = new MyTextField(30, size);
		wordTf.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size - 1));
		wordPnl.add(wordTf);

		JPanel outputNamePnl = new JPanel();
		outputNamePnl.setBounds(xPos, yPos, cWidth, pnlHeight);
		frame.getContentPane().add(outputNamePnl);
		yPos += pnlHeight + blank;

		JLabel outputNameLbl = new JLabel("出力ファイル名：");
		outputNameLbl.setBounds(xPos, yPos, cWidth, ph * 2);
		outputNameLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, calcCharSize(wordLbl, lblHeight) - 1));
		outputNamePnl.add(outputNameLbl);

		outputNameTf = new MyTextField(25, size);
		outputNameTf.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size - 1));
		outputNamePnl.add(outputNameTf);

		JButton btn = new JButton("参照");
		btn.addActionListener(new MyButtonListener(frame, outputNameTf));
		outputNamePnl.add(btn);

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

				if (wordTf.getText().length() == 0) {
					ra.append("探索ワードを入力してください。\n");
					return false;
				} else if (outputNameTf.getText().length() == 0) {
					ra.append("出力ファイル名を入力してください。\n");
					return false;
				}

				if (files.size() != 1) {
					ra.append("一度にインポートできるファイルは一つだけです。\n");
					return false;
				}

				String inputFileName = files.get(0).getName();
				Date date = new Date();
				SimpleDateFormat textAreaFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				ra.append(textAreaFormat.format(date) + "\n");
				ra.append("> インポートしたファイル：" + inputFileName + "\n");

				thread = new MyThread(files.get(0));
				thread.start();

			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	private class MyThread extends Thread {
		File newFile;

		public MyThread(File newFile) {
			this.newFile = newFile;
		}

		public void run() {

			CoordinateExtraction ce = new CoordinateExtraction(newFile);
			ce.inputFile();

		}

	}
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClint {
	final int dialogWidth = 400;
	final int dialogHeight = 450;

	JTextArea inText = new JTextArea(5, 30);
	JTextArea outText = new JTextArea(20, 30);
	// JTextArea user = new JTextArea(10,20);

	public static void main(String[] args) {
		new ChatClint().start();
	}

	private void start() {
		// Get screen width and height
		ScreenSize sSize = new ScreenSize();
		int sWidth = sSize.getWidth();
		int sHeight = sSize.getHeight();

		// New dialog
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// �������
				MyFrame dlg = new MyFrame("Clint");
				dlg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				dlg.setLocation((sWidth - dialogWidth) / 2, (sHeight - dialogHeight) / 2);
				dlg.setSize(dialogWidth, dialogHeight);

				// ������

				outText.setEditable(false);
				inText.setLineWrap(true);
				outText.setLineWrap(true);

				JScrollPane outScroll = new JScrollPane(outText);
				JScrollPane inScroll = new JScrollPane(inText);
				outScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				inScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				dlg.add(outScroll, BorderLayout.CENTER);
				dlg.add(inScroll, BorderLayout.SOUTH);
				dlg.pack();
				// ��������
				Socket clintSocket = null;
				try {
					clintSocket = new Socket("192.168.2.102", 18888);
				} catch (UnknownHostException e) {
					outText.setText("IP�����޷����ӷ�����");
				} catch (IOException e) {
					outText.setText("�������޷�����");
				}
				// connect(clintSocket);

				// �����Ϣ�����߳�
				Scanner scan = null;
				try {
					scan = new Scanner(clintSocket.getInputStream());
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
				Thread receive = new Thread(new Receive(outText, scan));
				receive.start();

				// ����¼�����,����Ctrl+enterΪ����
				SendAction sendAction = new SendAction(inText, clintSocket);
				keyListener(sendAction);
			}
		});
	}

	private void keyListener(SendAction sa) {
		InputMap imap = inText.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		imap.put(KeyStroke.getKeyStroke("ctrl ENTER"), "inText");
		ActionMap amap = inText.getActionMap();
		amap.put("inText", sa);
	}

	class MyFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		public MyFrame(String name) {
			setTitle(name);
			setVisible(true);
		}
	}

	class SendAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		JTextArea inText, outText;
		PrintStream sendStream = null;

		public SendAction(JTextArea in, Socket clintSocket) {
			inText = in;
			try {
				sendStream = new PrintStream(clintSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void actionPerformed(ActionEvent e) {
			String s = inText.getText().trim();
			inText.setText("");

			sendStream.println(s);
			sendStream.flush();
		}
	}

	class Receive implements Runnable {
		Scanner inStream = null;
		JTextArea out;

		public Receive(JTextArea out, Scanner s) {
			inStream = s;
			this.out = out;
		}

		public void run() {
			while (inStream.hasNextLine()) {
				String str = inStream.nextLine();
				out.append(str + "\n");
				out.setCaretPosition(outText.getText().length());
			}
			inStream.close();
		}
	}
}

class ScreenSize {
	private int sWidth, sHeight;

	public ScreenSize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension sSize = kit.getScreenSize();
		sWidth = sSize.width;
		sHeight = sSize.height;
	}

	public int getWidth() {
		return sWidth;
	}

	public int getHeight() {
		return sHeight;
	}
}
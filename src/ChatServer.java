import java.io.*;
import javax.swing.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

public class ChatServer {
	public static int name = 0;
	ServerSocket ss = null;
	Socket s = null;
	List<Socket> clints = new ArrayList<Socket>();
	TextArea printArea = new TextArea("",100,100,TextArea.SCROLLBARS_VERTICAL_ONLY);

	public static void main(String[] args) {
		
		ChatServer cServer = new ChatServer();
		cServer.laughFrame();
		cServer.start();
		
	}
	
	private void laughFrame() {
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension sSize = kit.getScreenSize();
		
		//界面初始化
		Frame sFrame = new ServerFrame("ChatServer");
		sFrame.setSize(400,500);
		sFrame.setLocation((sSize.width-400)/2, (sSize.height-500)/2);
		sFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		
		//添加组件
		printArea.setEditable(false);
		sFrame.add(printArea);
		
	}

	//监听客户端连接，为连接的客户端开一个独立处理线程
	private void start() {

		try {
			ss = new ServerSocket(18888);
			
			while (!ss.isClosed()) {
				//System.out.println("Clint" + name + "已成功连接");
				if(name != 0)
					printArea.append("Clint" + name + "已成功连接\n\n");
				else
					printArea.append("服务器已启动\n\n");
				s = ss.accept();
				clints.add(s);
				name++;
				for (int i = 0; i < clints.size(); i++) {
					Socket clintSocket = clints.get(i);
					send("Clint" + name + "已成功连接\n", clintSocket);
				}
				// String name = newConnect(s);
				Thread cAccept = new Thread(new ClintConnect(s, ss, name, clints, printArea));
				cAccept.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//发送消息
	void send(String str, Socket s) {
		try {
			PrintStream clintStream = new PrintStream(s.getOutputStream());
			clintStream.println(str);
			clintStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ServerFrame extends Frame {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public ServerFrame(String title) {
			setTitle(title);
			setVisible(true);
		}
	}
}

//客户端事件处理线程类
class ClintConnect implements Runnable {
	int name;
	Socket s = null;
	ServerSocket ss = null;
	List<Socket> list = null;
	TextArea printArea = null;

	public ClintConnect(Socket s, ServerSocket ss, int name, List<Socket> list, TextArea printArea) {
		this.s = s;
		this.ss = ss;
		this.list = list;
		this.name = name;
		this.printArea = printArea;
	}

	private void send(String str, Socket s) {
		try {
			PrintStream clintStream = new PrintStream(s.getOutputStream());
			clintStream.println(str);
			clintStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//将从客户端接收到的消息格式化后，转发给每个 客户端
	public void run() {
		try {
			InputStream input = s.getInputStream();
			Scanner in = new Scanner(input);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			while (in.hasNextLine()) {
				String str = "***" + "Clint" + name + "***" + "       " + df.format(new Date()) + "\n" + in.nextLine()
						+ "\n";
				for (int i = 0; i < list.size(); i++) {
					Socket clintSocket = list.get(i);
					send(str, clintSocket);
				}
				printArea.append(str + "\n");
			}
			s.close();
			in.close();
			if (list.contains(s))
				list.remove(list.indexOf(s));
			printArea.append("Clint" + name + " exit");
			for (int i = 0; i < list.size(); i++) {
				Socket clintSocket = list.get(i);
				send("Clint" + name + " exit\n", clintSocket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package RemoteCtrl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.os.Bundle;

public class UdpActivity extends BaseActivity {
	protected int getUdpServerPort() {
		return 12345;
	}
	protected int getTimeoutMills() {
		return 2000;
	}
	DatagramSocket socket = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(getTimeoutMills());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void send(String udpMsg, String server) {
		try {
			InetAddress serverAddr;
			serverAddr = InetAddress.getByName(server);
			socket.send(new DatagramPacket(udpMsg.getBytes(),
					udpMsg.getBytes().length, serverAddr, getUdpServerPort()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String recv() {
		String recv_str = null;
		try {
			default_handler.sendEmptyMessage(MSG_BLOCK);

			byte[] recv_buffer = new byte[4096];
			DatagramPacket recv_packet = new DatagramPacket(recv_buffer,
					recv_buffer.length);
			// java.util.Arrays.fill(recv_buffer, (byte) 0);
			socket.receive(recv_packet);
			// recv_str = new String(recv_buffer); //this is wrong!!, use
			// following instead
			recv_str = new String(recv_buffer, 0, recv_packet.getLength());
		} catch (IOException e) {
			e.printStackTrace();
			MsgBox("网络连接有误，检查计算机上的服务程序是否开启, 并确认ip设置正确", false);
		} finally {
			default_handler.sendEmptyMessage(MSG_UNBLOCK);
		}
		return recv_str;
	}

	protected String send_recv(String udpMsg, String server) {
		send(udpMsg, server);
		return recv();
	}

	@Override
	public void setLayout(int layoutId) {
		// TODO Auto-generated method stub
	}
}

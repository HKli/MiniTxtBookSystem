package com.qf.server;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.qf.biz.UserBiz;
import com.qf.entity.User;
import com.qf.util.Contants;
import com.qf.util.Entity;

public class ServerThread implements Runnable {

	private Socket socket;

	private UserBiz ub = new UserBiz();

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			InputStream is = socket.getInputStream(); // 获取输入流 = 读取
			OutputStream os = socket.getOutputStream(); // 获取输出流 = 写

			ObjectInputStream ois = new ObjectInputStream(is);
			ObjectOutputStream oos = new ObjectOutputStream(os);

			Entity cmd = (Entity) ois.readObject(); // 获取客户端发送过来的对象数据
			Entity command = executeCommand(cmd);

			oos.writeObject(command);
			oos.flush();
			oos.close();
			ois.close();
			os.close();
			is.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 执行命令
	 * 
	 * @param cmd
	 */
	public Entity executeCommand(Entity cmd) {
		String command = cmd.getCommand(); // 获取命令
		if (command.equals(Contants.COMMAND_LOGIN)) { // 登录
			return doLogin(cmd);
		} else if (command.equals(Contants.COMMAND_REGISTER)) { // 注册
			return doRegister(cmd);
		}
		return new Entity();
	}

	// 注册
	public Entity doRegister(Entity entity) {
		String json = entity.getInfo(); // 获取客户端发送过来的JSON数据
		// 解析该JSON数据，获取用户名和密码
		String name = "";
		String pwd = "";
		try {
			JSONObject obj = new JSONObject(json);
			name = obj.getString("name");
			pwd = obj.getString("pwd");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 判断是否注册成功
		if (ub.register(name, pwd)) {
			entity.setIsSuccess(true);
			entity.setInfo("注册成功！");
			// 日志
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.print(date + "\t=>\t");
			// 获取客户端的IP地址
			InetAddress ia = socket.getInetAddress();
			System.out.println(ia.getHostAddress() + "注册成功");
		} else {
			entity.setIsSuccess(false);
			entity.setInfo("注册失败！");
			// 日志
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.print(date + "\t=>\t");
			// 获取客户端的IP地址
			InetAddress ia = socket.getInetAddress();
			System.out.println(ia.getHostAddress() + "注册失败了");
		}
		return entity;
	}

	// 登录
	public Entity doLogin(Entity entity) {
		User user = (User) entity.getObj();
		boolean canLogin = ub.login(user.getName(), user.getPassword());
		if (canLogin) {
			entity.setIsSuccess(true);
			entity.setInfo("登录成功！");
			// 日志
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.print(date + "\t=>\t");
			// 获取客户端的IP地址
			InetAddress ia = socket.getInetAddress();
			System.out.println(ia.getHostAddress() + "登录成功");
		} else {
			entity.setIsSuccess(false);
			entity.setInfo("登录失败！");
			// 日志
			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			System.out.print(date + "\t=>\t");
			// 获取客户端的IP地址
			InetAddress ia = socket.getInetAddress();
			System.out.println(ia.getHostAddress() + "登录失败");
		}
		return entity;
	}

}

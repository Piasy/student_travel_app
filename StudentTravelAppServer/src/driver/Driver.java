package driver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import model.CommitHandler;
import model.Constant;
import model.QueryHandler;
import model.Setting;
import model.SignupHandler;
import model.VerifyHandler;
import util.Util;

import com.sun.net.httpserver.HttpServer;

public class Driver
{
	public static void main(String[] args)
	{
		int level = 1;
		String logFilesDir = null;
		PrintStream out = System.out;
		
		try
		{

			try
			{
				for (int i = 0; i < args.length; i++) 
				{
					if (args[i].equals("-l")) 
					{
						level = Integer.parseInt(args[++ i]);
					}
					else if (args[i].equals("-o")) 
					{
						logFilesDir = args[++i];
					}
				}
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				System.out.println(usage());
				System.exit(0);
			}
			
			if (logFilesDir != null)
			{
				File logDir = new File(logFilesDir);
				
				if (!logDir.exists())
				{
					System.out.println("Dir not exsit");
					System.out.println(usage());
					System.exit(0);
				}
				else
				{
					File logFile = new File(logFilesDir + "/" + System.currentTimeMillis() + ".log");
					logFile.createNewFile();
					out = new PrintStream(logFile);
				}
			}

			Util.init(out, level);
			
			HttpServer server = HttpServer.create(new InetSocketAddress(Setting.SIGNUP_PORT), 0);
			server.createContext("/signup", new SignupHandler());
			server.createContext("/verify", new VerifyHandler());
			server.createContext("/query", new QueryHandler());
			server.createContext("/commit", new CommitHandler());
	        server.setExecutor(Executors.newFixedThreadPool(Constant.THREAD_POOL_SIZE));
	        server.start();
	        Util.Log(Constant.LOG_LEVEL_INFO, "Server start", "Driver");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Driver");
		}		
	}
	
	private static String usage() 
	{
		return ("\n"
				+ "Usage:  java -jar StudentTravelAppServer.jar [-l LEVEL -o LOG_FILES_DIR]"
				+ "\n");
	}
}

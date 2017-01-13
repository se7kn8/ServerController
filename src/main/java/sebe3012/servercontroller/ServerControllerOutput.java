package sebe3012.servercontroller;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerControllerOutput extends PrintStream {

	public ServerControllerOutput(PrintStream original) {
		super(original);
	}

	private Calendar cal = Calendar.getInstance();

	@Override
	public void println(String line) {
		
		if(ServerController.DEBUG){

			cal.setTimeInMillis(System.currentTimeMillis());

			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			StackTraceElement caller = stack[2];

			String callerString = "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")";

			super.println(new SimpleDateFormat("HH:mm:ss").format(cal.getTime()) + "  " + callerString + " : " + line);
		}else{
			super.println(line);
		}
		
	}

}

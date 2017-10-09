
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.IllegalArgumentException;

public class Xserver {
	
	public static String BadRequest() {
		String answer;
		answer ="HTTP/1.1 400 Bad Request";
		answer +="\n\r";
		return answer;
	}
	
	public static String OK() {
		String answer;
		answer ="HTTP/1.1 200 OK";
		answer +="\n\r";
		return answer;
	}
	
	public static void ChecknSend(MyURL url) {
		//TODO To complete
	}
	
	public static MyURL checkrequest(String request) {
		String _method = "(GET)\\s*";
		//TODO Form of requestURI to specify
		String _requestURI = "(\\* | ([^\\s\\r\\n]*) | ^$ )\\s*";
		String _httpVersion ="(HTTP)\\/(1.1)";
		String _end = "\\n\\r";
		String reg_exp =_method + _requestURI + _httpVersion + _end;
		Pattern pattern = Pattern.compile(reg_exp);
		Matcher matcher = pattern.matcher(request);
		
		MyURL _url= null;
		//Test request
	    if (matcher.find()) {
	        String requestURI = matcher.group(2);
	        try {
	        		_url = new MyURL(requestURI);
	        }catch(IllegalArgumentException e) {
	        		System.err.println(e.getMessage());
	        }
	    } else {
	        throw new IllegalArgumentException("Malformed request : " + request);
	    }
		return _url;
	}
	
	public static void checkhost(String hostline) {
		String _method = "(HOST):";
		//TODO Form of requestURI to specify
		String _hostname = "\\s([^\\n\\r\\s]*)";
		String _end = "\\n\\r";
		String reg_exp =_method + _hostname+_end;
		Pattern pattern = Pattern.compile(reg_exp);
		Matcher matcher = pattern.matcher(hostline);
		//Test request
	    if (!matcher.find()) {
	    		throw new IllegalArgumentException("Malformed Hostline : " + hostline);
	    } 
	}
	
	public static void handleConnection(Socket socket) {
		try {
			OutputStream output = socket.getOutputStream();
			PrintWriter print = new PrintWriter(output,true);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = br.readLine();
			MyURL _url = checkrequest(line);
			//Not sure of this case
			if(_url.getHost().equals("localhost")) {
				System.out.println(Xserver.OK());
				print.println(Xserver.OK());
			}
			
			//Check Hostline
			String hostline = br.readLine();
			Xserver.checkhost(hostline);
			
			//Scan the all header
			while (br.ready()) {
				if ((line = br.readLine()) != null){
					System.out.println(line);
				}
			}

			//Send the requested file or Not Found
			Xserver.ChecknSend(_url);
			
			//Last answer and close
			//TODO Set content length with the File downloaded
			int content_length=0;
			String answer = "Content-length: " + Integer.toString(content_length);
			answer +="\\r\\n";
			answer +="Connection: close";
			print.println(answer);
			
			output.close();
			print.close();
			
		// Exception from checkrequest()
		}catch(IllegalArgumentException e) {
			System.out.println(Xserver.BadRequest());
			try {
				socket.close();
			}catch(IOException err) {
				System.err.println(e.getMessage());
			}
		}catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
	}
	
	public static void main (String[] args){
		int serverPort = Integer.parseInt(args[0]);
		//TODO Define Backlog
		int backlog = 3; 
		
		try {
		ServerSocket server = new ServerSocket(serverPort,backlog);
		while (true) {
			Socket clientsocket = server.accept();
			handleConnection(clientsocket);
			server.close();	
		}
		}catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
	}

	
}

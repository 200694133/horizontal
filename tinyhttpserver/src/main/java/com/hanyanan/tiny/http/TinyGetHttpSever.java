package com.hanyanan.tiny.http;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TinyGetHttpSever {
	private static final String DEFAULT_HTTP_HOST = "127.0.0.1";
	private static final int DEFAULT_PORT = 80;
	public static final String LINE_DIVIDER = "\r\n";
    private boolean start = false;
	private int mPort;
	private final Map<String,HttpRequestHandler> mHandlerPathMap = new HashMap<String,HttpRequestHandler>();

    private ServerSocket localServer;
    private Thread mServerThread = null;
	public TinyGetHttpSever(int port) {
		mPort = port;
	}

	public TinyGetHttpSever() {
		mPort = DEFAULT_PORT;
	}

    public void registerRequestHandler(String path, HttpRequestHandler handler){
        mHandlerPathMap.put(path, handler);
    }

    public int getPort(){
        return mPort;
    }

	public void start() {
        start = true;
        mServerThread = new Thread(){
          public void run(){
              while(true) {
                  if(!start) return ;
                  try {
                      localServer = new ServerSocket(mPort);
                      localServer.setReuseAddress(false);
                      while (true) {
                          Socket localSocket = localServer.accept();
                          Log.d("new address " + localSocket.getPort());
                          doRequest(localSocket);
                      }
                  } catch (IOException e) {
                      e.printStackTrace();
                      ++mPort;
                  }
              }
          }
        };
        mServerThread.start();
	}


    public void stop() {
        start = false;
        try {
            localServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            localServer = null;
        }
        mServerThread.interrupt();
        mServerThread = null;
    }


    private void doRequest(Socket localSocket) {
		
		new Thread(new ServerRunnable(localSocket)).start();
	}
	
	
	public class ServerRunnable implements Runnable{
		private final Socket mSocket;
		public ServerRunnable(Socket socket){
			mSocket = socket;
		}
		@Override
		public void run() {
			Log.d("..........localSocket connected..........");
			InputStream in_localSocket = null;
			OutputStream out_localSocket = null;
			try {
				mSocket.setKeepAlive(false);
//				mSocket.setSoTimeout(5000);
//				mSocket.setSoLinger(true, 0);
				in_localSocket = mSocket.getInputStream();
			    out_localSocket = mSocket.getOutputStream();
					
				Map<String,String> headMap = readRequestHeaders(in_localSocket);
				mSocket.shutdownInput();

				String path = headMap.get("path");
				Log.d("path "+path);
                if(null == path){
                    //TODO
                }

                String method = headMap.get("method");
                if(!"get".equalsIgnoreCase(method)){
                    writeResponse(out_localSocket, null, new HttpRequestHandler.HandlerResult(400));//TODO
                    mSocket.shutdownOutput();
                    return ;
                }

                String paths[] = parseRequestPath(path);
                Log.d("Path1 "+paths[0]);
                Log.d("Path2 "+paths[1]);
                if(null == paths || paths.length == 0){
                    writeResponse(out_localSocket, null, new HttpRequestHandler.HandlerResult(404)); //TODO, parse failed, run default handler
                    mSocket.shutdownOutput();
                    return ;
                }

                HttpRequestHandler handler = mHandlerPathMap.get(paths[0]);
                if(null == handler){
                    writeResponse(out_localSocket, null, new HttpRequestHandler.HandlerResult(404));//TODO
                    mSocket.shutdownOutput();
                    return ;
                }
                Map<String,String> parseRequestParams = parseRequestParams(path);
                HttpRequestHandler.HandlerResult response = handler.handle(paths[1], parseRequestParams, headMap);
				writeResponse(out_localSocket, handler, response);
				mSocket.shutdownOutput();
				Log.d("..........terminal local Socket I/O..........");
			} catch (IOException e) {
				Log.e("try get input stream and output stream fialed.");
				Log.d("mSocket port  "+mSocket.getPort());
				Log.d("mSocket is closed "+mSocket.isClosed());
				Log.d("mSocket is isConnected "+mSocket.isConnected());
				Log.d("mSocket is isBound "+mSocket.isBound());
				Log.d("IS inputstream shutdown "+mSocket.isInputShutdown());
				Log.d("IS outputstream shutdown "+mSocket.isOutputShutdown());
				Log.e(e.toString());
				e.printStackTrace();
			} finally {
				try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if(null != in_localSocket) in_localSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if(null != out_localSocket) out_localSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}


    public static String[] parseRequestPath(String fullPath){
        int index = fullPath.indexOf('?');
        if(index >= 0){
            fullPath = fullPath.substring(0, index);
        }

        index = fullPath.indexOf('/');
        if(index < 0) return null;
        if(index == 0) {
        	fullPath = fullPath.substring(1);
            index = fullPath.indexOf('/');
        }

        if(index < 0){
            return new String[]{fullPath, ""};
        }

        if(index == 0){
            return parseRequestPath(fullPath);
        }
        String path = fullPath.substring(0,index);
        String last = fullPath.substring(index+1);
        return new String[]{path, last};
    }

	public Map<String, String> readRequestHeaders(InputStream inputStream) throws IOException {
		BufferedInputStream streamReader = new BufferedInputStream(inputStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(streamReader, "utf-8"));
		String line = null;
		int i = 0;
		Map<String, String> headMap = new HashMap<String, String>();
		while ((line = bufferedReader.readLine()) != null) {
			System.err.println(line);
			if(line.length() <= 0) break;
			if (i == 0) {
				String[] paths = line.split(" ");
				if (paths.length < 2) {
					Log.e("can not get url path");
					break;
				}
                headMap.put("method", paths[0]);
				headMap.put("path", paths[1]);
			} else {
				String s[] = line.split(":");
				if (s.length != 2)
					continue;

				headMap.put(s[0], s[1]);
			}
			++i;
		}

		return headMap;
	}

    public Map<String,String> parseRequestParams(String path){
        int index = path.indexOf('?');
        if(index >=0 && index <path.length()){
            path = path.substring(index+1);
        }
        Map<String,String> res = new HashMap<String,String>();
        parseDoubleFiled(path, "&", "=", res);
        return res;
    }
	
	public void writeResponse(OutputStream outputStream, HttpRequestHandler handler,
                              HttpRequestHandler.HandlerResult response) throws IOException {
		StringBuilder sb = new StringBuilder();
		int code = response.responseCode;
        sb.append("HTTP/1.1 ").append(code).append(" ").append(TinyHttpHelper.getHttpDesc(code)).append(LINE_DIVIDER);

		Map<String,String> map = response.mResponseHeaders;
		for(Map.Entry<String, String> entry : map.entrySet()){
//			if("Content-Range".equalsIgnoreCase(entry.getKey())){
//				sb.append(entry.getKey()).append("=").append(entry.getValue()).append(LINE_DIVIDER);
//			}else{
				sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(LINE_DIVIDER);
//			}
		}
		
//		if(response.inputStream != null &&response.fileLength > 0 && response.start >=0 && response.end > response.start){
//				//Content-Range=bytes 2000070-106786027/106786028
//			sb.append("Content-Range: bytes ").append(response.start).append("-").append(response.end)
//			.append("/").append(response.fileLength).append(LINE_DIVIDER);
//
//		}
		sb.append("Connection: close").append(LINE_DIVIDER); 
//		Date now = new Date();  
//		sb.append("Date: " + now).append(LINE_DIVIDER);  
//		sb.append("Connection: keep-alive").append(LINE_DIVIDER); 
        
		sb.append(LINE_DIVIDER);
		Log.d(sb.toString());
		
		outputStream.write(sb.toString().getBytes());
		outputStream.flush();
		if(response.inputStream != null){
            handler.getCopier().copy(response.inputStream , outputStream, response.size, null);
			response.inputStream .close();
		}		
	}



    /**
     * Parse a string to map structure, such as transfer a=1&b=2&c=3&d=4 to Map{a->1,b->2,c->3,d->4}
     * @param input
     * @param firstLevelDivider
     * @param secondLevelDivider
     * @param output
     */
    public static void parseDoubleFiled(String input, String firstLevelDivider,
                                        String secondLevelDivider, Map<String,String> output){
        if(input == null) return ;
        String []params = input.split(firstLevelDivider);
        if(null != params) {//parse request params
            for(String p : params){
                if(p == null) continue;
                String[] pp = p.split(secondLevelDivider);
                if(pp.length != 2) continue;
                output.put(pp[0], pp[1]);
            }
        }
    }
}

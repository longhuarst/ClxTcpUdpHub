package http;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class HttpFileServerHandler  extends
SimpleChannelInboundHandler<FullHttpRequest>  {

	    private final String url;

	    public HttpFileServerHandler(String url) {
		this.url = url;
	    }

	    @Override
	    public void messageReceived(ChannelHandlerContext ctx,
		    FullHttpRequest request) throws Exception {
	    	
	    	
	    	
		if (!request.getDecoderResult().isSuccess()) {
		    sendError(ctx, BAD_REQUEST);
		    System.out.println("BAD_REQUEST");
		    return;
		}
		if (request.getMethod() != GET) {
			if (request.getMethod() == POST) {
				System.out.println("post--->");
				
				 HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
				 
				 InterfaceHttpData data = decoder.next();
				 
				 System.out.println("type="+data.getHttpDataType());
				 if (data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)) {
					 System.out.println("检测到文件上传");
					 
					 FileUpload fileUpload = (FileUpload) data;
					 
					 
					 //检查文件是否已经传送完毕
					 if (fileUpload.isCompleted()) {
						 String filename = fileUpload.getFilename();
						 try {
							 
							 
							 	String path = sanitizeUri(request.getUri());
							 	System.out.println("path = "+path);
							 	while (path.endsWith("/")) {
							 		path = path.substring(0, path.length()-1);
							 	} 
			                    //File file = new File(path + "/"+filename);
			                    //System.out.println("path + \"/\"+filename = "+path + "/"+filename);
			                   // if (!file.getParentFile().exists()) {
			                   //     file.getParentFile().mkdirs();
			                    //    System.out.println("-mkdirs--");
			                   // }
			                    //if (!file.exists()) {
			                    //    file.createNewFile();//
			                        System.out.println("-createNewFile--");
			                   //} else {
			                    //	System.out.println("xxx");
			                        //writeResponse(ResponseModel.buildOk(filename));
			                    //    return;
			                    //}
			                    System.out.println("----");
			                    //fileUpload.renameTo(file);
			                    System.out.println("写入文件！");
			                    decoder.removeHttpDataFromClean(fileUpload);
//			                    System.out.println("---1");
//			                    FullHttpResponse response = new DefaultFullHttpResponse(
//						                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, null);
//			                    System.out.println("---2");
//			                    ctx.write(response);
//			                    System.out.println("---3");
//						        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
//						        System.out.println("-------");
			                    //ctx.close();
			                    
			                    sendOk(ctx, OK);
			                    
			                    
//			                    FullHttpResponse response = new DefaultFullHttpResponse(
//						                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, null);
			                    
			                   // writeResponse(ResponseModel.buildOk(filename));
			                } catch (Exception e) {
			                   System.out.println(e);
			                	//writeResponse(ResponseModel.buildOtherError());
			                }
						 
					 }else {
						 //还没有传输完毕
						 FullHttpResponse response = new DefaultFullHttpResponse(
					                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, null);
					        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
					        ctx.write(response);
					        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
					 }
					 return;//文件上传结束
				 }
				 
				 //System.out.println("xxxxxxx-----?>"+decoder.next().toString());
				 
				 
				 decoder.offer(request);
				 List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
				 Map<String, String> parmMap = new HashMap<>();
				 
		            for (InterfaceHttpData parm : parmList) {

		            	System.out.println("=====?>"+parm);
		            	
		            	
		            	
		                Attribute data1 = (Attribute) parm;
		                parmMap.put(data1.getName(), data1.getValue());
		                System.out.println("data:["+data1.getName()+"]  --- "+"value:["+data1.getValue()+"]");
		                
		                System.out.println("uri = "+request.getUri().toString());
		                
//		                if (data1.getName().equals("action") &&
//		                	data1.getValue().equals("delete")) {
//		                	
//		                }else {
//		                	System.out.println("param not found!");
//		                }
		            }
		            
		            
		            
		            //map获取HTTP参数
		            String action = parmMap.get("action");
		            String path = null;
		            if (action != null) {
		            	switch(action) {
		            	case "delete":
		            		//删除文件
		            		if ((path = parmMap.get("filename"))!=null){
		            			//删除文件
			                	File file = new File(path);
			                	System.out.println("delete file path = "+path);
			                	if (file.exists() && file.isFile()) {
			                		if (file.delete()) {
			                			System.out.println("delte ok !");
			                		}else {
			                			System.out.println("delete failed!");
			                		}
			                	}else {
			                		System.out.println("file not exist! exist="+file.exists()+" | isFile="+file.isFile());
			                	}
		            		}
		            		break;
		            	}
		            }
		            
		            
		            
		            
		
				return;
			}else {
				sendError(ctx, METHOD_NOT_ALLOWED);
			    System.out.println("METHOD_NOT_ALLOWED");
			    return;
			}	
		}
		String uri = request.getUri();
		System.out.println("1.uri = ["+uri+"]");
		
		String param = null;
		//分离uri 和参数
		String paramSplitRes[] = uri.split("\\?");
		if (paramSplitRes.length>= 2) {
			uri = paramSplitRes[0];
			param = paramSplitRes[1];
		}
		
		System.out.println("uri = ["+uri+"]");
		System.out.println("param = ["+param+"]");
		
		final String path = sanitizeUri(uri);
		if (path == null) {
		    sendError(ctx, FORBIDDEN);
		    System.out.println("FORBIDDEN");
		    return;
		}
		
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
		    sendError(ctx, NOT_FOUND);
		    System.out.println("NOT_FOUND");
	
		    return;
		}
		if (file.isDirectory()) {
		    if (uri.endsWith("/")) {
			sendListing(ctx, file,uri);
		    } else {
			sendRedirect(ctx, uri + '/');
		    }
		    return;
		}
		if (!file.isFile()) {
		    sendError(ctx, FORBIDDEN);
		    return;
		}
		RandomAccessFile randomAccessFile = null;
		try {
		    randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
		} catch (FileNotFoundException fnfe) {
		    sendError(ctx, NOT_FOUND);
		    return;
		}
		long fileLength = randomAccessFile.length();
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		setContentLength(response, fileLength);
		setContentTypeHeader(response, file);
		if (isKeepAlive(request)) {
		    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
		ctx.write(response);
		ChannelFuture sendFileFuture;
		sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0,
			fileLength, 8192), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
		    @Override
		    public void operationProgressed(ChannelProgressiveFuture future,
			    long progress, long total) {
			if (total < 0) { // total unknown
			    System.err.println("Transfer progress: " + progress);
			} else {
			    System.err.println("Transfer progress: " + progress + " / "
				    + total);
			}
		    }

		    @Override
		    public void operationComplete(ChannelProgressiveFuture future)
			    throws Exception {
			System.out.println("Transfer complete.");
		    }
		});
		ChannelFuture lastContentFuture = ctx
			.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!isKeepAlive(request)) {
		    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
		    throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
		    sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	    }

	    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	    private String sanitizeUri(String uri) {
		try {
		    uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    try {
			uri = URLDecoder.decode(uri, "ISO-8859-1");
		    } catch (UnsupportedEncodingException e1) {
			throw new Error();
		    }
		}
		if (!uri.startsWith(url)) {
		    return null;
		}
		if (!uri.startsWith("/")) {
		    return null;
		}
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.')
			|| uri.contains('.' + File.separator) || uri.startsWith(".")
			|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
		    return null;
		}
		return System.getProperty("user.dir") + File.separator + uri;
	    }

	    private static final Pattern ALLOWED_FILE_NAME = Pattern
		    .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

	    
	    
	    
	    private static void sendListing(ChannelHandlerContext ctx, File dir,String uri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
		StringBuilder buf = new StringBuilder();
		String dirPath = dir.getPath();
		buf.append("<!DOCTYPE html>\r\n");
		buf.append("<html><head><title>");
		buf.append(dirPath);
		buf.append(" 目录：");
		buf.append("</title></head>");
		
		buf.append("<script type=\"text/javascript\">\r\n"
				+ "	function clickx(filename) {\r\n"
				+ "		httpPost(filename,{\"action\":\"delete\",\"filename\":filename});"
				+ "		setTimeout(\"refresh()\", 1000);"
				+ "		"
				+ "	}\r\n"
				+ "\r\n"
				+ "	function refresh() {\r\n"
				+ "		history.go(0);\r\n"
				+ "	}\r\n"
				+ ""
				+ "");
				
		
		buf.append("function httpPost(URL, PARAMS) {\n" + 
				"		    var temp = document.createElement(\"form\");\n" + 
				"		    temp.action = URL;\n" + 
				"		    temp.method = \"post\";\n" + 
				"		    temp.style.display = \"none\";\n" + 
				"\n" + 
				"		    for (var x in PARAMS) {\n" + 
				"		        var opt = document.createElement(\"textarea\");\n" + 
				"		        opt.name = x;\n" + 
				"		        opt.value = PARAMS[x];\n" + 
				"		        temp.appendChild(opt);\n" + 
				"		    }\n" + 
				"\n" + 
				"		    document.body.appendChild(temp);\n" + 
				"		    temp.submit();\n" + 
				"\n" + 
				"		    return temp;\n" + 
				"		}\r\n");
		
		buf.append("</script>\r\n");
		
		buf.append("<body>\r\n");
		buf.append("<h3>");
		buf.append(dirPath).append(" 目录：");
		buf.append("</h3>\r\n");
		buf.append("<ul>");
		buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		
		
		File files[] = dir.listFiles();
		System.out.println("files size = "+files.length);
		for (int i=0;i<files.length;++i){
			
			System.out.println(files[i].getName()+"#"+files[i].canRead()+"#"+files[i].isHidden());
			
		}
		
		for (File f : dir.listFiles()) {
		    if (f.isHidden() || !f.canRead() || f.getName().equals("ftp.jar")) {
			continue;
		    }
		    String name = f.getName();
//		    if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
//		    	System.out.println("!ALLOWED_FILE_NAME = "+f.getName());
//			continue;
//		    }
		    buf.append("<li>链接：");
		    if (f.isFile()) {
		    	buf.append("<button type=\"button\" onclick=\"clickx('"+f.getAbsolutePath()+"')\">删除</button>");
		    }
//		    buf.append("<form action=\""+uri+"?action=delete"+"\" method=\"post\" >");
//			buf.append("<input type=\"submit\" value=\"删除\" />");
//			buf.append("</form>");
//			
		    buf.append("<a href=\"");
		    buf.append(name);
		    buf.append("\">");
		    buf.append(name);
		    buf.append("</a>");
		    
		    
		    
		    buf.append("</li>\r\n");
		}
		
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length()-1);
		}
		buf.append("<br><br><br><br><br>\r\n");
		buf.append("<form action=\""+uri+"\" method=\"post\" enctype=\"multipart/form-data\">");
		buf.append("<input type=\"file\" name=\"fileUpload\" />");
		buf.append("<input type=\"submit\" value=\"上传文件\" />");
		buf.append("</form>");
		buf.append("</ul></body></html>\r\n");
		ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
		response.content().writeBytes(buffer);
		buffer.release();
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	    }

	    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
		response.headers().set(LOCATION, newUri);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	    }

	    private static void sendError(ChannelHandlerContext ctx,
		    HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
			status, Unpooled.copiedBuffer("Failure: " + status.toString()
				+ "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	    }
	    
	    
	    
	    private static void sendOk(ChannelHandlerContext ctx,
			    HttpResponseStatus status) {
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, Unpooled.copiedBuffer("OK: " + status.toString()
					+ "\r\n", CharsetUtil.UTF_8));
			response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		    }
	    
	    

	    private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE,
			mimeTypesMap.getContentType(file.getPath()));
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
//	    private void writeResponse(ResponseModel<String> responseModel) {
//	        //String resp = GsonUtil.getGson().toJson(responseModel);
//	        //ByteBuf buf = Unpooled.copiedBuffer(resp, CharsetUtil.UTF_8);
//	        FullHttpResponse response = new DefaultFullHttpResponse(
//	                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
//	        //response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
//	        ctx.write(response);
//	        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
//	    }

}

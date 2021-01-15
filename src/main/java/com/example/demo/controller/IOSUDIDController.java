package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.utils.HttpUtil;
import com.example.demo.utils.JWTTokenUtil;
import io.vertx.core.json.JsonObject;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.boot.Banner.Mode.LOG;

@Controller
public class IOSUDIDController {

    private  final Logger logger = LoggerFactory.getLogger(this.getClass());
    @RequestMapping(value = {"/hello","/hello/{udid}"})
    public String hello(@PathVariable(name = "udid") Optional<String> udid, Model model,HttpServletResponse response) {
        if (udid.isPresent()) {
            model.addAttribute("udid",udid.get());
        }else {
            model.addAttribute("udid",null);
        }
        return "DownLoadConfig";
    }

    @RequestMapping("/newDevice/{udid}")
    public void newDevice(@PathVariable(name = "udid") String udid, HttpServletResponse response) {

        boolean result = registerUdid(udid);
        if (result){
            logger.info("注册设备成功");
        }else {
            logger.info("注册设备失败");
        }

        // 在这里触发打包脚本

        try {
            response.sendRedirect("/install");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @RequestMapping("/download")
    public String fileDownLoad(HttpServletResponse response) throws FileNotFoundException {
        //获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if(!path.exists()) path = new File("");
        logger.info(path.getAbsolutePath());

        //如果上传目录为/static/images/upload/，则可以如下获取：
        File downloadFile = new File(path.getAbsolutePath(),"static/mobileconfig.xml");
        if(!downloadFile.exists()){
            return "下载文件不存在";
        }
        response.reset();
        response.setContentType("application/x-apple-aspen-config");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) downloadFile.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + downloadFile.getName() );
        response.setStatus(200);

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(downloadFile));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
            os.close();
        } catch (IOException e) {
            return "下载失败";
        }
        return "下载成功";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file, @RequestParam("program") String program,  HttpServletResponse response) throws FileNotFoundException {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        //获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if(!path.exists()) path = new File("");
        logger.info(path.getAbsolutePath());

        String fileName = file.getOriginalFilename();
        String fileDir = path.getAbsolutePath() + "/static/" + program;
        File dir = new File(fileDir);
        if (!dir.exists()){
            dir.mkdirs();
        }
        String filePath = fileDir + "/" + fileName;
        File dest = new File(filePath);
        try {
            file.transferTo(dest);
            logger.info("上传成功");
            return "success";
        } catch (IOException e) {
            logger.error(e.toString(), e);
            return "fail";
        }
    }

    @RequestMapping("/app/parse_udid")
    public void   parseUdid(HttpServletRequest request,HttpServletResponse response) {

        response.setContentType("text/html;charset=UTF-8");
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logger.info("开始响应解析");
        //获取HTTP请求的输入流
        InputStream is = null;
        String udid = null;
        try {
            is = request.getInputStream();
            //已HTTP请求输入流建立一个BufferedReader对象
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuilder sb = new StringBuilder();
            //读取HTTP请求内容
            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                sb.append(buffer);
            }

            is.close();
            String content = sb.toString().substring(sb.toString().indexOf("<?xml"), sb.toString().indexOf("</plist>")+8);
            //content就是接收到的xml字符串
            //进行xml解析即可
            //logger.info(content);

            Document doc = null;

            try {
                doc = DocumentHelper.parseText(content);
                Element root = doc.getRootElement();
                Iterator it = root.elementIterator();
                // logger.info("是否有数据" + it.hasNext());
                while (it.hasNext()) {
                    Element element = (Element) it.next();// 一个Item节点
                    logger.info(element.getStringValue() + " : " + element.getTextTrim());
                    Integer index = 0;
                    Integer preciousIndex = 0;
                    List<String> key_value_elements = new ArrayList<>();

                    for(Iterator chilidIt=element.elementIterator();chilidIt.hasNext();){
                        Element childElement = (Element) chilidIt.next();
                        // logger.info(childElement.getStringValue() + " : " + childElement.getName());
                        key_value_elements.add(childElement.getStringValue());
                        if (childElement.getStringValue().contains("UDID")) {
                             preciousIndex = index;
                        }
                        index++;
                        // do something
                    }
                     udid = key_value_elements.get(preciousIndex + 1);
                }


            }catch (Exception e) {
                e.printStackTrace();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        if (udid != null) {
            //获取udid成功
            logger.info("我需要不同的");
            logger.info(udid);
        }
        response.setStatus(301);
        response.setHeader("Location","/hello/" + udid);
    }

    @RequestMapping("/install")
    public String installApp() {

        return "Install";
    }

//    @RequestMapping("/test")
//    @ResponseBody
    public boolean registerUdid(String device_udid) {
        logger.info("注册设备...");
        String token = JWTTokenUtil.createToken();
        String result = null;

        try {
            result = HttpUtil.sendPostRequest("https://api.appstoreconnect.apple.com/v1/devices",token);
            //在这里检测设备是否已经注册
            JSONObject jsonObject = JSON.parseObject(result);
            JSONArray devices = jsonObject.getJSONArray("data");
            boolean isRegisted = false;
            for(int i = 0;i < devices.size(); i++){
                JSONObject device = devices.getJSONObject(i);
                JSONObject attributes = device.getJSONObject("attributes");
                if (attributes != null){
                    String udid = attributes.getString("udid");
                    String status = attributes.getString("status");
                    if (udid == device_udid && status == "ENABLED"){
                        isRegisted = true;
                        break;
                    }
                }
            }

            if (isRegisted == false) {
                boolean ret = registDevice(token,device_udid);
                if (ret) {
                    return true;
                }else {
                    return true;
                }
            }else {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }
    }

    private  boolean registDevice(String token,String udid) {
        JSONObject jsonObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        dataObject.put("type","devices");
        JSONObject attributeObject = new JSONObject();
        attributeObject.put("name",udid);
        attributeObject.put("platform","IOS");
        attributeObject.put("udid",udid);
        dataObject.put("attributes",attributeObject);
        jsonObject.put("data",dataObject);
        try {
           String result = HttpUtil.sendPostWithJsonRequest("https://api.appstoreconnect.apple.com/v1/devices",jsonObject.toJSONString(),token);
           logger.info(result);
           JSONObject ret = JSON.parseObject(result);
           if (ret.getJSONArray("errors") != null) {
               return false;
           }else {
               return true;
           }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

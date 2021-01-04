package com.example.demo.controller;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @RequestMapping("/resign/{udid}")
    public void resign(@PathVariable(name = "udid") String udid, HttpServletResponse response) {
        String cmd = "./test.sh " + udid;
        try {
            logger.info("start regsin");
            Process pro = Runtime.getRuntime().exec(cmd,null,new File("/Users/hezhihui/Desktop/Project/CombodiaLife"));
            int status = pro.waitFor();
            if (status != 0){
                logger.info("执行shell 命令出错");
            }else {
                logger.info("执行成功");
            }

        } catch (IOException | InterruptedException e) {
            logger.info("执行出错");
            e.printStackTrace();
        }

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
    public String upload(@RequestParam("file") MultipartFile file,HttpServletResponse response) throws FileNotFoundException {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }

        //获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if(!path.exists()) path = new File("");
        logger.info(path.getAbsolutePath());

        String fileName = file.getOriginalFilename();
        String filePath = path.getAbsolutePath() + "/static/test.ipa";
        File dest = new File(filePath);
        try {
            file.transferTo(dest);
            logger.info("上传成功");
            return "上传成功";
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
        return "上传失败！";
    }

    @RequestMapping("/app/parse_udid")
    public void parseUdid(HttpServletRequest request,HttpServletResponse response) {

        response.setContentType("text/html;charset=UTF-8");
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
}

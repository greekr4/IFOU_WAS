package com.gaon.nifou.v3.ctrl.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gaon.nifou.v3.trans_ora_manager;
import com.gaon.nifou.v3.util_manager;

@WebServlet("/upload.gaon")
public class upload extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        trans_ora_manager oram = new trans_ora_manager();
        JSONArray jsonary = new JSONArray();
        PrintWriter out = response.getWriter();

        try {
            // JSON 데이터를 읽어옴
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // JSON 데이터를 JSONArray로 변환
            //jsonary = new JSONArray(sb.toString());

            // JSONArray를 이용한 DB 입력 로직
            // ...

            out.println("Success"); // 처리 완료 메시지 출력
        } catch (Exception e) {
            out.println("Error"); // 처리 실패 메시지 출력
            e.printStackTrace();
        }
    }
}
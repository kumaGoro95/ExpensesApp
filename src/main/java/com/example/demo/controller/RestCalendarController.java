package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.beans.DailySummary;
import com.example.demo.repository.MoneyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/event")
public class RestCalendarController {
	
	@Autowired
	private final MoneyRecordRepository moneyRecordRepository;
	
	/**
     * カレンダーに表示するDailySummary情報を取得
     * @return DailySummary情報をjsonエンコードした文字列
     */
    @GetMapping(value = "/all")
    public String getDailySummaries(Authentication loginUser) {
        String jsonMsg = null;
        try {
            List<DailySummary> dailySummaries = moneyRecordRepository.findDailySummaries(loginUser.getName());
            for(int i = 0; i < dailySummaries.size(); i++) {
            	DailySummary ds = dailySummaries.get(i);
            	String title = "<a th:href=\"@{/}\">" + ds.getTitle() + "</a>";
            	ds.setTitle(title);
            	
            }
            // FullCalendarにエンコード済み文字列を渡す
            ObjectMapper mapper = new ObjectMapper();
            jsonMsg =  mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dailySummaries);
        } catch (IOException ioex) {
            System.out.println(ioex.getMessage());
        }
        return jsonMsg;
    }

}

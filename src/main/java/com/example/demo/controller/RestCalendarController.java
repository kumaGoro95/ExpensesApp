package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        	List<DailySummary> dailyExpenseSummaries = moneyRecordRepository.findDailyExpenseSummaries(loginUser.getName());
            for(int i = 0; i < dailyExpenseSummaries.size(); i++) {
            	DailySummary ds = dailyExpenseSummaries.get(i);
            	String title = "<a class=\"expense\" href=\"/money-record/history/" + ds.getStart() + "\">" +"支出  " + ds.getTitle() + "</a>";
            	ds.setTitle(title);
            	
            }
            List<DailySummary> dailyIncomeSummaries = moneyRecordRepository.findDailyIncomeSummaries(loginUser.getName());
            for(int i = 0; i < dailyIncomeSummaries.size(); i++) {
            	DailySummary ds = dailyIncomeSummaries.get(i);
            	String title = "<a class=\"income\" href=\"/money-record/history/" + ds.getStart() + "\">" +"収入  " + ds.getTitle() + "</a>";
            	ds.setTitle(title);
            }
            
            List<DailySummary> dailySummaries= new ArrayList<DailySummary>();
            for(int i = 0; i < dailyExpenseSummaries.size(); i++) {
            	dailySummaries.add(dailyExpenseSummaries.get(i));
            }
            for(int i = 0; i < dailyIncomeSummaries.size(); i++) {
            	dailySummaries.add(dailyIncomeSummaries.get(i));
            }
            
            
            // FullCalendarにエンコード済み文字列を渡す
            ObjectMapper mapper = new ObjectMapper();
            jsonMsg =  mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dailySummaries);
        }catch(

	IOException ioex)
	{
            System.out.println(ioex.getMessage());
        }return jsonMsg;
}

}

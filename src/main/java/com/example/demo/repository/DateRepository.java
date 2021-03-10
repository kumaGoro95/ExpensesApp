package com.example.demo.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.MoneyRecord;

@Repository
public interface DateRepository extends JpaRepository<MoneyRecord, Long> {

	@Query(value = "select substring(adddate(:date,interval number month),1,7) as date "
			+ "from numbers where adddate(:date ,interval number month) "
			+ "between :date and curdate()", nativeQuery = true)
	public Object[] getAllMonths(String date);

	default String[] findAllMonths(String date) {
		List<String> list = new ArrayList<String>();
		Object[] monthsObj = getAllMonths(date);
		for (int i = 0; i < monthsObj.length; i++) {
			list.add(monthsObj[i].toString());
		}
		String months[] = list.toArray(new String[list.size()]);
		for (int i = 0; i < months.length; i++) {
		}
		return months;
	}

}

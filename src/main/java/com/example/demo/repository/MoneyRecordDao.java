package com.example.demo.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface MoneyRecordDao <T> extends Serializable {
	public List<T> getAll();
	public List<T> findByUsername(String username);
	public List<T> find(String fstr, String a);
<<<<<<< Updated upstream:src/main/java/com/example/demo/repository/MoneyRecordDao.java

=======
	public BigDecimal sumMonthExpense(String fstr, String a);
>>>>>>> Stashed changes:src/main/java/com/example/demo/dao/MoneyRecordDao.java
}
